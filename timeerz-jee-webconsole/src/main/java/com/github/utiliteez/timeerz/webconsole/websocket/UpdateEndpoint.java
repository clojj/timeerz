package com.github.utiliteez.timeerz.webconsole.websocket;

import com.github.utiliteez.timeerz.jee.model.JobCompletedEvent;

import javax.enterprise.event.Observes;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

@ServerEndpoint(value = "/update")
public class UpdateEndpoint {

    private static final Logger LOG = Logger.getLogger("UpdateEndpoint LOG");

    private static ConcurrentLinkedQueue<Session> sessions = new ConcurrentLinkedQueue<>();

    @OnOpen
    public void open(final Session session) {
        sessions.add(session);
        LOG.info("open update");
    }

    public void observeJobCompletedEvent(@Observes JobCompletedEvent jobCompletedEvent) throws IOException {
        Session session = sessions.peek();
        if (session != null) {
            for (Session sess : sessions) {
                if (sess.isOpen()) {
                    try {
                        sess.getBasicRemote().sendText(jobCompletedEvent.getTimerObject().getId());
                    } catch (IOException e) {
                        LOG.severe("Error sending on WS 'update': " + e.getMessage());
                        throw e;
                    }
                } else {
                    sessions.remove(sess);
                }
            }
        }
    }

    @OnMessage
    public void onMessage(final Session session, final String message) {
        System.out.println("message = " + message);
    }

    @OnClose
    public void onClose(Session session) {
        LOG.info("Session " + session.getId() + " has ended");
    }
}
