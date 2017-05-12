import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class TimerInfoEncoder implements Encoder.Text<TimerInfoMessage> {
    @Override
    public String encode(TimerInfoMessage timerInfoMessage) throws EncodeException {
        return Json.createObjectBuilder()
                .add("timerId", timerInfoMessage.getTimerId())
                .add("timerData", timerInfoMessage.getTimerData())
                .toString();
    }

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }
}
