package kiwi.ergo.fix.acceptor;

import org.junit.Test;
import quickfix.SessionID;

/**
 * This test class just runs the FixSession API. which only exists to wrap the QuickFix/J Session
 * class that doesn't expose any instance members, this makes invasive testing more difficult. Hence
 * The {@code FixSession} wrapper.
 */
public class FixSessionTest {

    private static final SessionID SESSION_ID = new SessionID("FIX.4.4", "EXEC", "BANZAI");

    @Test
    public void onCreate_sessionNotFound() {
        new FixSession().onCreate(SESSION_ID);
    }

    @Test
    public void sendMessage_sessionNotFound() throws Exception {
        new FixSession().sendMessage(SESSION_ID, null);
    }

}