package kiwi.ergo.fix.acceptor;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.Date;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.DefaultSessionFactory;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.LogFactory;
import quickfix.Message;
import quickfix.NoopStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.UnsupportedMessageType;
import quickfix.field.ClOrdID;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.QuoteReqID;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.QuoteRequest;

public class ApplicationTest extends SettingsTest {

    private static final SessionSettings settings = getSettings("/config/quickfixj/executor.cfg");
    private static final Application application = createApplication(settings, fixSession);

    @BeforeClass
    public static void initializeApplicationSession()
        throws ConfigError, FieldConvertError, IOException {
        LogFactory logFactory = new ScreenLogFactory(true, true, true);
        DefaultSessionFactory sessionFactory = Mockito.spy(
            new DefaultSessionFactory(application, new NoopStoreFactory(), logFactory,
                new DefaultMessageFactory()));

        SocketAcceptor acceptor = new SocketAcceptor(sessionFactory, settings);
        acceptor.start();
    }

    @Test
    public void onCreate() {
        application.onCreate(SESSION_ID);
    }

    @Test
    public void buyFromApp() throws Exception {
        NewOrderSingle order = createOrder(OrdType.LIMIT, Side.BUY, 12.789);

        reset(fixSession);

        application.fromApp(order, SESSION_ID);

        verify(fixSession, times(2)).sendMessage(eq(SESSION_ID), isA(ExecutionReport.class));
    }

    @Test
    public void sellFromApp() throws Exception {
        NewOrderSingle order = createOrder(OrdType.LIMIT, Side.SELL, 12.113);

        reset(fixSession);

        application.fromApp(order, SESSION_ID);

        verify(fixSession, times(2)).sendMessage(eq(SESSION_ID), isA(ExecutionReport.class));
    }

    @Test
    public void sellShortFromApp() throws Exception {
        NewOrderSingle order = createOrder(OrdType.LIMIT, Side.SELL_SHORT, 12.113);

        reset(fixSession);

        application.fromApp(order, SESSION_ID);

        verify(fixSession, times(2)).sendMessage(eq(SESSION_ID), isA(ExecutionReport.class));
    }

    @Test
    public void sellNoDealFromApp() throws Exception {
        NewOrderSingle order = createOrder(OrdType.LIMIT, Side.SELL, 12.789);

        reset(fixSession);

        application.fromApp(order, SESSION_ID);

        verify(fixSession).sendMessage(eq(SESSION_ID), isA(ExecutionReport.class));
    }

    @Test
    public void marketSellFromApp() throws Exception {
        NewOrderSingle order = createOrder(OrdType.MARKET, Side.SELL, 12.3);

        reset(fixSession);

        application.fromApp(order, SESSION_ID);

        verify(fixSession, times(2)).sendMessage(eq(SESSION_ID), isA(ExecutionReport.class));
    }

    @Test
    public void quoteRequestFromApp() throws Exception {
        Message message = new QuoteRequest(new QuoteReqID("test"));

        reset(fixSession);

        application.fromApp(message, SESSION_ID);

        verifyNoMoreInteractions(fixSession);
    }

    @Test
    public void unhandledOrderTypeFromApp()
        throws FieldNotFound, IncorrectTagValue, IncorrectDataFormat, UnsupportedMessageType {
        exception.expect(IncorrectTagValue.class);
        exception.expectMessage("Field [40] contains an incorrect tag value");

        NewOrderSingle order = createOrder(OrdType.STOP_LIMIT, Side.SELL, 12.3);

        application.fromApp(order, SESSION_ID);
    }

    @Test
    public void invalidOrderSideFromApp()
        throws FieldNotFound, IncorrectTagValue, IncorrectDataFormat, UnsupportedMessageType {
        exception.expect(IncorrectTagValue.class);
        exception.expectMessage("Invalid order side");

        NewOrderSingle order = createOrder(OrdType.LIMIT, Side.AS_DEFINED, 12.3);

        application.fromApp(order, SESSION_ID);
    }

    private NewOrderSingle createOrder(char orderType, char side, double price) {
        NewOrderSingle order = new NewOrderSingle(new ClOrdID("test"), new Side(side),
            new TransactTime(new Date()), new OrdType(orderType));
        order.set(new Symbol("APPLES/BANANAS"));
        order.set(new OrderQty(100.0));
        if (OrdType.LIMIT == orderType) {
            order.set(new Price(price));
        }
        return order;
    }

}