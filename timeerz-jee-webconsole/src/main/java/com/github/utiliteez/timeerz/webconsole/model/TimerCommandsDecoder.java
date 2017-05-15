package com.github.utiliteez.timeerz.webconsole.model;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class TimerCommandsDecoder implements Decoder.Text<TimerCommandsMessage> {

    @Override
    public TimerCommandsMessage decode(String textMessage) throws DecodeException {
        TimerCommandsMessage timerCommandsMessage = new TimerCommandsMessage();
        List<TimerCommand> timerCommands = new ArrayList<>();
        JsonArray array;
        try (JsonReader jsonReader = Json.createReader(new StringReader(textMessage))) {
            array = jsonReader.readArray();
        }
        for (int i = 0; i < array.size(); i++) {
            JsonObject jsonObject = array.getJsonObject(i);
            TimerCommand timerCommand = new TimerCommand(jsonObject.getString("timerId"), jsonObject.getString("timerOp"));
            timerCommands.add(timerCommand);
        }
        timerCommandsMessage.getTimerCommands().addAll(timerCommands);
        return timerCommandsMessage;    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }
}
