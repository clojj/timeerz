package com.github.utiliteez.timeerz.webconsole.model;

public class TimerInfoMessage {
    private String timerId;
    private String timerData;

    public TimerInfoMessage(String timerId, String timerData) {
        this.timerId = timerId;
        this.timerData = timerData;
    }

    public String getTimerId() {
        return timerId;
    }

    public void setTimerId(String timerId) {
        this.timerId = timerId;
    }

    public String getTimerData() {
        return timerData;
    }

    public void setTimerData(String timerData) {
        this.timerData = timerData;
    }

}
