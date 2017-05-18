package com.github.utiliteez.timeerz.webconsole.websocket;

import com.github.utiliteez.timeerz.jee.TimeerzManager;
import com.github.utiliteez.timeerz.webconsole.model.TimerCommand;
import com.github.utiliteez.timeerz.webconsole.model.TimerCommandsDecoder;
import com.github.utiliteez.timeerz.webconsole.model.TimerCommandsMessage;
import com.github.utiliteez.timeerz.webconsole.model.TimerDataEncoder;

import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.List;
import java.util.logging.Logger;

@ServerEndpoint(value = "/timeerz", encoders = TimerDataEncoder.class, decoders = TimerCommandsDecoder.class)
public class TimeerzEndpoint {

    private static final Logger LOG = Logger.getLogger("TimeerzEndpoint LOG");

    @Inject
    private TimeerzManager timeerzManager;

    @OnOpen
    public void open(final Session session) {
        LOG.info("open timeerz");
    }

    @OnMessage
    public void onMessage(final Session session, final TimerCommandsMessage timerCommandsMessage) {
            for (Session s : session.getOpenSessions()) {
                if (s.isOpen()) {
                    List<TimerCommand> timerCommands = timerCommandsMessage.getTimerCommands();
                    for (TimerCommand timerCommand : timerCommands) {
                        if ("ToggleActivation".equals(timerCommand.getTimerOp())) {
                            boolean success = timeerzManager.toggleActivation(timerCommand.getTimerId());
                        }
                    }

                    // TODO send updated list
                    /*
                    try {
                        session.getBasicRemote().sendObject();
                    } catch (IOException | EncodeException e) {
                        LOG.warning("onMessage failed: " + e.getMessage());
                    }
                    */
                }
            }
    }

    @OnClose
    public void onClose(Session session) {
        LOG.info("Session " + session.getId() + " has ended");
    }
}
