package com.github.utiliteez.timeerz.webconsole.model;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.StringWriter;

public class TimerDataEncoder implements Encoder.Text<TimerDataMessage> {

    private static StringWriter writer = new StringWriter();

    @Override
    public String encode(TimerDataMessage timerDataMessage) throws EncodeException {
        JsonObject json = Json.createObjectBuilder()
                .add("timerId", timerDataMessage.getTimerId())
                .add("active", timerDataMessage.isActive())
                .add("cronExpression", timerDataMessage.getCronExpression())
                .build();
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
