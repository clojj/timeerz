package com.github.clojj.timeerz.webconsole;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.logging.Logger;

@ServerEndpoint(value = "/timeerz", encoders = TimerInfoEncoder.class, decoders = TimerIdDecoder.class)
public class TimeerzEndpoint {

    private static final Logger LOG = Logger.getLogger("TimeerzEndpoint LOG");

    @OnOpen
    public void open(final Session session) {
        LOG.info("open");
    }

    @OnMessage
    public void onMessage(final Session session, final TimerIdMessage timerIdMessage) {
        try {
            for (Session s : session.getOpenSessions()) {
                if (s.isOpen()) {
                    TimerInfoMessage timerInfoMessage = new TimerInfoMessage("id", "data");
                    session.getBasicRemote().sendObject(timerInfoMessage);                }
            }
        } catch (IOException | EncodeException e) {
            LOG.warning("onMessage failed: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        LOG.info("Session " + session.getId() + " has ended");
    }
}
