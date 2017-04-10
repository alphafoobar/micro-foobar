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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.management.JMException;
import kiwi.ergo.fix.marketdata.MarketDataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.CachedFileStoreFactory;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldConvertError;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;

public class Executor {

    private static final Logger log = LoggerFactory.getLogger(Executor.class);
    private final SocketAcceptor acceptor;

    public Executor(SessionSettings settings) throws ConfigError, FieldConvertError, JMException {
        Application application = Application.createApplication(settings, new FixSession(),
            MarketDataFactory.createMarketDataProvider(settings));

        MessageStoreFactory messageStoreFactory = new CachedFileStoreFactory(settings);
        LogFactory logFactory = new ScreenLogFactory(true, true, true);
        MessageFactory messageFactory = new DefaultMessageFactory();

        acceptor = new SocketAcceptor(application, messageStoreFactory, settings, logFactory,
                messageFactory);
    }

    private void start() throws ConfigError {
        acceptor.start();
    }

    private void stop() {
        acceptor.stop();
    }

    public static void main(String[] args) throws Exception {
        try {
            SessionSettings settings = getSessionSettings(getConfigFromArgsOrDefault(args));

            Executor executor = new Executor(settings);
            executor.start();

            pressAnyKeyToQuit();

            executor.stop();
        } catch (ConfigError | IOException exception) {
            log.error(exception.getMessage(), exception);
        }
    }

    private static String getConfigFromArgsOrDefault(String[] args) {
        return args.length == 0 ? "/config/quickfixj/executor.cfg" : args[0];
    }

    static SessionSettings getSessionSettings(String name) throws ConfigError, IOException {
        try (InputStream inputStream = getSettingsInputStream(name)) {
            return new SessionSettings(inputStream);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void pressAnyKeyToQuit() throws IOException {
        System.out.println("press <enter> to quit");
        System.in.read();
    }

    private static InputStream getSettingsInputStream(String name) throws FileNotFoundException {
        InputStream inputStream = Executor.class.getResourceAsStream(name);
        if (inputStream == null) {
            System.out.println("usage: " + Executor.class.getName() + " [configFile].");
            throw new RuntimeException("Config file not declared.");
        }
        return inputStream;
    }
}
