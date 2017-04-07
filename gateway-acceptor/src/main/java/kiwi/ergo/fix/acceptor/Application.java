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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.ConfigError;
import quickfix.DataDictionaryProvider;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.FixVersions;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.LogUtil;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
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

    private static final String DEFAULT_MARKET_PRICE_KEY = "DefaultMarketPrice";
    private static final String ALWAYS_FILL_LIMIT_KEY = "AlwaysFillLimitOrders";
    private static final String VALID_ORDER_TYPES_KEY = "ValidOrderTypes";

    private final Set<String> validOrderTypes = Sets.newHashSet();
    private final MarketDataProvider marketDataProvider;
    private final boolean alwaysFillLimitOrders;

    private Application(SessionSettings settings) throws ConfigError, FieldConvertError {
        validOrderTypes.addAll(getValidOrderTypes(settings));
        marketDataProvider = getMarketDataProvider(settings);
        alwaysFillLimitOrders = isAlwaysFillLimitOrders(settings);
    }

    static Application createApplication(SessionSettings settings)
        throws FieldConvertError, ConfigError {
        return new Application(settings);
    }

    private static boolean isAlwaysFillLimitOrders(SessionSettings settings)
        throws ConfigError, FieldConvertError {
        return settings.isSetting(ALWAYS_FILL_LIMIT_KEY) && settings.getBool(ALWAYS_FILL_LIMIT_KEY);
    }

    private static MarketDataProvider getMarketDataProvider(SessionSettings settings)
        throws ConfigError, FieldConvertError {
        if (settings.isSetting(DEFAULT_MARKET_PRICE_KEY)) {
            double defaultMarketPrice = settings.getDouble(DEFAULT_MARKET_PRICE_KEY);
            return new MarketDataProvider() {
                public double getAsk(String symbol) {
                    return defaultMarketPrice;
                }

                public double getBid(String symbol) {
                    return defaultMarketPrice;
                }
            };
        }
        log.warn("Ignoring " + DEFAULT_MARKET_PRICE_KEY + " since no provider defined.");
        return null;
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
    public void onCreate(SessionID sessionID) {
        Session.lookupSession(sessionID).getLog().onEvent("Valid order types: " + validOrderTypes);
    }

    @Override
    public void fromApp(quickfix.Message message, SessionID sessionID)
        throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        if (message instanceof NewOrderSingle) {
            onMessage((NewOrderSingle) message, sessionID);
        }
        crack(message, sessionID);
    }

    private Price getPrice(Message message) throws FieldNotFound {
        Price price;
        if (message.getChar(OrdType.FIELD) == OrdType.LIMIT && alwaysFillLimitOrders) {
            price = new Price(message.getDouble(Price.FIELD));
        } else {
            if (marketDataProvider == null) {
                throw new RuntimeException("No market data provider specified for market order");
            }
            char side = message.getChar(Side.FIELD);
            if (side == Side.BUY) {
                price = new Price(marketDataProvider.getAsk(message.getString(Symbol.FIELD)));
            } else if (side == Side.SELL || side == Side.SELL_SHORT) {
                price = new Price(marketDataProvider.getBid(message.getString(Symbol.FIELD)));
            } else {
                throw new RuntimeException("Invalid order side: " + side);
            }
        }
        return price;
    }

    private void sendMessage(SessionID sessionID, Message message) {
        try {
            Session session = Session.lookupSession(sessionID);
            if (session == null) {
                throw new SessionNotFound(sessionID.toString());
            }

            DataDictionaryProvider dataDictionaryProvider = session.getDataDictionaryProvider();
            if (dataDictionaryProvider != null) {
                dataDictionaryProvider.getApplicationDataDictionary(
                    MessageUtils.toApplVerID(FixVersions.BEGINSTRING_FIX44))
                    .validate(message, true);
            }

            session.send(message);
        } catch (SessionNotFound | FieldNotFound | IncorrectTagValue | IncorrectDataFormat e) {
            log.error(e.getMessage(), e);
        }
    }

    private void validateOrder(Message order) throws IncorrectTagValue, FieldNotFound {
        OrdType ordType = new OrdType(order.getChar(OrdType.FIELD));
        if (!validOrderTypes.contains(Character.toString(ordType.getValue()))) {
            log.error("Order type not in ValidOrderTypes setting");
            throw new IncorrectTagValue(ordType.getField());
        }
        if (ordType.getValue() == OrdType.MARKET && marketDataProvider == null) {
            log.error("DefaultMarketPrice setting not specified for market order");
            throw new IncorrectTagValue(ordType.getField());
        }
    }

    private void onMessage(NewOrderSingle order, SessionID sessionID) {
        try {
            validateOrder(order);

            OrderQty orderQty = order.getOrderQty();
            Price price = getPrice(order);
            OrderID orderID = Id.createOrderID();
            ExecID execID = Id.createExecID();

            ExecutionReport accept = new ExecutionReport(orderID, execID,
                new ExecType(ExecType.NEW), new OrdStatus(OrdStatus.NEW), order.getSide(),
                new LeavesQty(order.getOrderQty().getValue()), new CumQty(0), new AvgPx(0));
            accept.set(order.getClOrdID());
            accept.set(order.getSymbol());
            sendMessage(sessionID, accept);

            if (isOrderExecutable(order, price)) {
                ExecutionReport executionReport = new ExecutionReport(
                    orderID,
                    execID, new ExecType(ExecType.FILL), new OrdStatus(OrdStatus.FILLED),
                    order.getSide(),
                    new LeavesQty(0), new CumQty(orderQty.getValue()), new AvgPx(price.getValue()));

                executionReport.set(order.getClOrdID());
                executionReport.set(order.getSymbol());
                executionReport.set(orderQty);
                executionReport.set(new LastQty(orderQty.getValue()));
                executionReport.set(new LastPx(price.getValue()));

                sendMessage(sessionID, executionReport);
            }
        } catch (IncorrectTagValue | FieldNotFound e) {
            LogUtil.logThrowable(sessionID, e.getMessage(), e);
        }
    }
}
