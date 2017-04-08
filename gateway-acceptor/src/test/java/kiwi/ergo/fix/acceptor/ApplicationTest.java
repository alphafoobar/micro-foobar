package kiwi.ergo.fix.acceptor;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Date;
import kiwi.ergo.fix.marketdata.MarketDataFactory;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.DefaultSessionFactory;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.LogFactory;
import quickfix.NoopStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.UnsupportedMessageType;
import quickfix.field.ClOrdID;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.NewOrderSingle;

public class ApplicationTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static final SessionSettings settings = getSettings();
    private static final FixSession fixSession = Mockito.spy(new FixSession());
    private static final Application application = createApplication(settings, fixSession);
    private static final SessionID SESSION_ID = new SessionID("FIX.4.4", "EXEC", "BANZAI");

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

    private static SessionSettings getSettings() {
        try {
            return Executor.getSessionSettings("/config/quickfixj/executor.cfg");
        } catch (ConfigError | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Application createApplication(SessionSettings settings, FixSession fixSession) {
        try {
            return Mockito.spy(Application.createApplication(settings, fixSession,
                MarketDataFactory.createMarketDataProvider(settings)));
        } catch (FieldConvertError | ConfigError e) {
            throw new RuntimeException(e);
        }
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