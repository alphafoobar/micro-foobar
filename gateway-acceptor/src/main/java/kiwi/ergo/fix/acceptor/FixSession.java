package kiwi.ergo.fix.acceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.DataDictionaryProvider;
import quickfix.FieldNotFound;
import quickfix.FixVersions;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;

public class FixSession {

    private static final Logger log = LoggerFactory.getLogger(FixSession.class);

    private volatile Session session;

    void onCreate(SessionID sessionId) {
        session = Session.lookupSession(sessionId);
        if (session == null) {
            log.error("Session not found, sessionId=" + sessionId.toString());
        } else {
            session.getLog().onEvent("Session created, sessionId=" + sessionId.toString());
        }
    }

    void sendMessage(SessionID sessionId, Message message) {
        try {
            if (session == null || !session.getSessionID().equals(sessionId)) {
                throw new SessionNotFound(sessionId.toString());
            }

            DataDictionaryProvider dataDictionaryProvider = session.getDataDictionaryProvider();
            if (dataDictionaryProvider != null) {
                dataDictionaryProvider.getApplicationDataDictionary(
                    MessageUtils.toApplVerID(FixVersions.BEGINSTRING_FIX44))
                    .validate(message, true);
            }
            session.send(message);
        } catch (SessionNotFound | FieldNotFound | IncorrectTagValue | IncorrectDataFormat exception) {
            log.error(exception.getMessage(), exception);
        }
    }
}
