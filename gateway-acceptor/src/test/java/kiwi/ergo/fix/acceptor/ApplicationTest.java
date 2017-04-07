package kiwi.ergo.fix.acceptor;

import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldConvertError;
import quickfix.LogFactory;
import quickfix.NoopStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;

public class ApplicationTest {

    private static final SessionSettings settings = getSettings();
    private static final Application application = createApplication(settings);


    @BeforeClass
    public static void initializeApplicationSession()
        throws ConfigError, FieldConvertError, IOException {
        LogFactory logFactory = new ScreenLogFactory(true, true, true);
        new SocketAcceptor(application, new NoopStoreFactory(), settings, logFactory,
            new DefaultMessageFactory()).start();
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
        application.onCreate(new SessionID("FIX.4.4", "EXEC", "BANZAI"));
    }

    @Test
    public void fromApp() throws Exception {

    }

}