/*
 * Copyright (c) quickfixengine.org  All rights reserved.
 *
 * This file is part of the QuickFIX FIX Engine
 *
 * This file may be distributed under the terms of the quickfixengine.org
 * license as defined by quickfixengine.org and appearing in the file
 * LICENSE included in the packaging of this file.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING
 * THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See http://www.quickfixengine.org/LICENSE for licensing information.
 *
 * Contact ask@quickfixengine.org if any conditions of this licensing
 * are not clear to you.
 */

package kiwi.ergo.fix.acceptor;

import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import kiwi.ergo.fix.marketdata.MarketDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.LogUtil;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;
import quickfix.field.AvgPx;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.LastPx;
import quickfix.field.LastQty;
import quickfix.field.LeavesQty;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.NewOrderSingle;

public class Application extends ApplicationAdapter {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private static final String VALID_ORDER_TYPES_KEY = "ValidOrderTypes";

    private final Set<String> validOrderTypes = Sets.newHashSet();
    private final MarketDataProvider marketDataProvider;
    private final FixSession fixSession;

    private Application(@Nonnull SessionSettings settings, @Nonnull FixSession fixSession,
        @Nonnull MarketDataProvider marketDataProvider) throws ConfigError, FieldConvertError {
        this.fixSession = fixSession;
        validOrderTypes.addAll(getValidOrderTypes(settings));
        this.marketDataProvider = marketDataProvider;
    }

    static Application createApplication(SessionSettings settings, FixSession fixSession,
        MarketDataProvider marketDataProvider)
        throws FieldConvertError, ConfigError {
        return new Application(settings, fixSession, marketDataProvider);
    }

    private static List<String> getValidOrderTypes(SessionSettings settings)
        throws FieldConvertError, ConfigError {
        if (settings.isSetting(VALID_ORDER_TYPES_KEY)) {
            return Arrays
                .asList(settings.getString(VALID_ORDER_TYPES_KEY).trim().split("\\s*,\\s*"));
        }
        return Collections.singletonList(OrdType.LIMIT + "");
    }

    private boolean isOrderExecutable(Message order, Price price) throws FieldNotFound {
        if (order.getChar(OrdType.FIELD) == OrdType.LIMIT) {
            BigDecimal limitPrice = new BigDecimal(order.getString(Price.FIELD));
            char side = order.getChar(Side.FIELD);
            BigDecimal thePrice = new BigDecimal("" + price.getValue());

            return (side == Side.BUY && thePrice.compareTo(limitPrice) <= 0)
                || ((side == Side.SELL || side == Side.SELL_SHORT)
                && thePrice.compareTo(limitPrice) >= 0);
        }
        return true;
    }

    @Override
    public void onCreate(SessionID sessionId) {
        fixSession.onCreate(sessionId);
    }

    @Override
    public void fromApp(quickfix.Message message, SessionID sessionId)
        throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        if (message instanceof NewOrderSingle) {
            onMessage((NewOrderSingle) message, sessionId);
        } else {
            log.warn("[{}] Unsupported message: {}", sessionId, message);
        }
    }

    private Price getPrice(Message message) throws FieldNotFound, IncorrectTagValue {
        char side = message.getChar(Side.FIELD);
        if (side == Side.BUY) {
            return new Price(
                marketDataProvider.getAsk(message.getString(Symbol.FIELD)).doubleValue());
        } else if (side == Side.SELL || side == Side.SELL_SHORT) {
            return new Price(
                marketDataProvider.getBid(message.getString(Symbol.FIELD)).doubleValue());
        }
        throw new IncorrectTagValue("Invalid order side: " + side);
    }

    private void validateOrder(Message order) throws IncorrectTagValue, FieldNotFound {
        OrdType ordType = new OrdType(order.getChar(OrdType.FIELD));
        if (!validOrderTypes.contains(Character.toString(ordType.getValue()))) {
            log.error("Order type not in ValidOrderTypes setting");
            throw new IncorrectTagValue(ordType.getField());
        }
    }

    private void onMessage(NewOrderSingle order, SessionID sessionId)
        throws IncorrectTagValue, FieldNotFound {
        try {
            validateOrder(order);

            OrderQty orderQty = order.getOrderQty();
            OrderID orderId = Id.createOrderId();
            ExecID execId = Id.createExecId();

            ExecutionReport accept = new ExecutionReport(orderId, execId,
                new ExecType(ExecType.NEW), new OrdStatus(OrdStatus.NEW), order.getSide(),
                new LeavesQty(order.getOrderQty().getValue()), new CumQty(0), new AvgPx(0));
            accept.set(order.getClOrdID());
            accept.set(order.getSymbol());
            fixSession.sendMessage(sessionId, accept);

            Price price = getPrice(order);
            if (isOrderExecutable(order, price)) {
                ExecutionReport executionReport = new ExecutionReport(
                    orderId,
                    execId, new ExecType(ExecType.FILL), new OrdStatus(OrdStatus.FILLED),
                    order.getSide(),
                    new LeavesQty(0), new CumQty(orderQty.getValue()), new AvgPx(price.getValue()));

                executionReport.set(order.getClOrdID());
                executionReport.set(order.getSymbol());
                executionReport.set(orderQty);
                executionReport.set(new LastQty(orderQty.getValue()));
                executionReport.set(new LastPx(price.getValue()));

                fixSession.sendMessage(sessionId, executionReport);
            }
        } catch (IncorrectTagValue | FieldNotFound exception) {
            LogUtil.logThrowable(sessionId, exception.getMessage(), exception);
            throw exception;
        }
    }
}
