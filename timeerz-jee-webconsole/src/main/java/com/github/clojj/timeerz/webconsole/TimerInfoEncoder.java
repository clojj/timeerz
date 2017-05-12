package com.github.clojj.timeerz.webconsole;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.StringWriter;

public class TimerInfoEncoder implements Encoder.Text<TimerInfoMessage> {
    @Override
    public String encode(TimerInfoMessage timerInfoMessage) throws EncodeException {

        JsonObject json = Json.createObjectBuilder()
                .add("timerId", timerInfoMessage.getTimerId())
                .add("timerData", timerInfoMessage.getTimerData()).build();
        StringWriter writer = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(writer)) {
            jsonWriter.write(json);
            return writer.toString();
        }
    }

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }
}
