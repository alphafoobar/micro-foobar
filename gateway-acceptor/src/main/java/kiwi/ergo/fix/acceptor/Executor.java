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

import static quickfix.Acceptor.SETTING_ACCEPTOR_TEMPLATE;
import static quickfix.Acceptor.SETTING_SOCKET_ACCEPT_ADDRESS;
import static quickfix.Acceptor.SETTING_SOCKET_ACCEPT_PORT;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.management.JMException;
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
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider.TemplateMapping;

public class Executor {

    private static final Logger log = LoggerFactory.getLogger(Executor.class);
    private final SocketAcceptor acceptor;
    private final Map<InetSocketAddress, List<TemplateMapping>> dynamicSessionMappings = new HashMap<>();

    public Executor(SessionSettings settings) throws ConfigError, FieldConvertError, JMException {
        Application application = Application.createApplication(settings);

        MessageStoreFactory messageStoreFactory = new CachedFileStoreFactory(settings);
        LogFactory logFactory = new ScreenLogFactory(true, true, true);
        MessageFactory messageFactory = new DefaultMessageFactory();

        acceptor = new SocketAcceptor(application, messageStoreFactory, settings, logFactory,
                messageFactory);

        configureDynamicSessions(settings, application, messageStoreFactory, logFactory,
                messageFactory);
    }

    private void configureDynamicSessions(SessionSettings settings, Application application,
            MessageStoreFactory messageStoreFactory, LogFactory logFactory,
            MessageFactory messageFactory) throws ConfigError, FieldConvertError {

        // If a session template is detected in the settings, then set up a dynamic session provider.
        Iterator<SessionID> sectionIterator = settings.sectionIterator();
        while (sectionIterator.hasNext()) {
            SessionID sessionId = sectionIterator.next();
            if (isSessionTemplate(settings, sessionId)) {
                InetSocketAddress address = getAcceptorSocketAddress(settings, sessionId);
                getMappings(address).add(new TemplateMapping(sessionId, sessionId));
            }
        }

        for (Map.Entry<InetSocketAddress, List<TemplateMapping>> entry : dynamicSessionMappings
                .entrySet()) {
            acceptor.setSessionProvider(entry.getKey(), new DynamicAcceptorSessionProvider(
                    settings, entry.getValue(), application, messageStoreFactory, logFactory,
                    messageFactory));
        }
    }

    private List<TemplateMapping> getMappings(InetSocketAddress address) {
        return dynamicSessionMappings.computeIfAbsent(address, k -> new ArrayList<>());
    }

    private InetSocketAddress getAcceptorSocketAddress(SessionSettings settings,
        SessionID sessionId)
            throws ConfigError, FieldConvertError {
        String acceptorHost = "0.0.0.0";
        if (settings.isSetting(sessionId, SETTING_SOCKET_ACCEPT_ADDRESS)) {
            acceptorHost = settings.getString(sessionId, SETTING_SOCKET_ACCEPT_ADDRESS);
        }
        int acceptorPort = (int) settings.getLong(sessionId, SETTING_SOCKET_ACCEPT_PORT);

        return new InetSocketAddress(acceptorHost, acceptorPort);
    }

    private boolean isSessionTemplate(SessionSettings settings, SessionID sessionId)
            throws ConfigError, FieldConvertError {
        return settings.isSetting(sessionId, SETTING_ACCEPTOR_TEMPLATE)
            && settings.getBool(sessionId, SETTING_ACCEPTOR_TEMPLATE);
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

    static SessionSettings getSessionSettings(String name)
        throws ConfigError, IOException {
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
