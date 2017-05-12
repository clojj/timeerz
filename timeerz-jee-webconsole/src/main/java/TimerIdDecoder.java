import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.StringReader;

public class TimerIdDecoder implements Decoder.Text<TimerIdMessage> {

    @Override
    public TimerIdMessage decode(String textMessage) throws DecodeException {
        TimerIdMessage timerIdMessage = new TimerIdMessage();
        JsonObject obj = Json.createReader(new StringReader(textMessage)).readObject();
        timerIdMessage.setTimerId(obj.getString("timerId"));
        return timerIdMessage;    }

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
