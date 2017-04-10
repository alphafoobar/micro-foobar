package kiwi.ergo.fix.acceptor;

import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.DefaultSessionFactory;
import quickfix.FieldConvertError;
import quickfix.LogFactory;
import quickfix.NoopStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;

public class SettingsTest extends AbstractSettingsTest {

    private static final SessionSettings settings = getSettings("/config/quickfixj/test.cfg");
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

}
