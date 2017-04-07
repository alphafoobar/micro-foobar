package kiwi.ergo.fix.acceptor;

import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.RejectLogon;
import quickfix.SessionID;

public abstract class ApplicationAdapter extends quickfix.MessageCracker implements
    quickfix.Application {

    @Override
    public void onLogon(SessionID sessionID) {
    }

    @Override
    public void onLogout(SessionID sessionID) {
    }

    @Override
    public void toAdmin(quickfix.Message message, SessionID sessionID) {
    }

    @Override
    public void toApp(quickfix.Message message, SessionID sessionID) throws DoNotSend {
    }

    @Override
    public void fromAdmin(quickfix.Message message, SessionID sessionID)
        throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
    }
}
