package kiwi.ergo.fix.acceptor;

import java.io.IOException;
import kiwi.ergo.fix.marketdata.MarketDataFactory;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.SessionID;
import quickfix.SessionSettings;

public abstract class AbstractSettingsTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    protected static final FixSession fixSession = Mockito.spy(new FixSession());
    protected static final SessionID SESSION_ID = new SessionID("FIX.4.4", "EXEC", "BANZAI");

    static SessionSettings getSettings(String configPath) {
        try {
            return Executor.getSessionSettings(configPath);
        } catch (ConfigError | IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    static Application createApplication(SessionSettings settings, FixSession fixSession) {
        try {
            return Mockito.spy(Application.createApplication(settings, fixSession,
                MarketDataFactory.createMarketDataProvider(settings)));
        } catch (FieldConvertError | ConfigError exception) {
            throw new RuntimeException(exception);
        }
    }

}
