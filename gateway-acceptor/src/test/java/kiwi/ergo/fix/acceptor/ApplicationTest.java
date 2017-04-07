package kiwi.ergo.fix.acceptor;

import java.io.IOException;
import java.util.Date;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldConvertError;
import quickfix.LogFactory;
import quickfix.NoopStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.field.ClOrdID;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix44.NewOrderSingle;

public class ApplicationTest {

    private static final SessionSettings settings = getSettings();
    private static final Application application = createApplication(settings);
    public static final SessionID SESSION_ID = new SessionID("FIX.4.4", "EXEC", "BANZAI");


    private static DefaultMessageFactory messageFactory;

    @BeforeClass
    public static void initializeApplicationSession()
        throws ConfigError, FieldConvertError, IOException {
        LogFactory logFactory = new ScreenLogFactory(true, true, true);
        messageFactory = Mockito.spy(DefaultMessageFactory.class);
        new SocketAcceptor(application, new NoopStoreFactory(), settings, logFactory,
            messageFactory).start();
    }

    private static SessionSettings getSettings() {
        try {
            return Executor.getSessionSettings("/config/quickfixj/executor.cfg");
        } catch (ConfigError | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Application createApplication(SessionSettings settings) {
        try {
            return Application.createApplication(settings);
        } catch (FieldConvertError | ConfigError e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void onCreate() {
        application.onCreate(SESSION_ID);
    }

    @Test
    public void fromApp() throws Exception {
        NewOrderSingle order = new NewOrderSingle(new ClOrdID("test"), new Side(Side.BUY),
            new TransactTime(new Date()), new OrdType(OrdType.LIMIT));
        order.set(new Symbol("APPLES/BANANAS"));
        order.set(new OrderQty(100.0));
        order.set(new Price(0.789));
        application.fromApp(order, SESSION_ID);
    }

}