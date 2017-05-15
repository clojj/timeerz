package com.github.utiliteez.timeerz.webconsole.model;

public class TimerCommand {
    private String timerId;
    private String timerOp;

    public TimerCommand(String timerId, String timerOp) {
        this.timerId = timerId;
        this.timerOp = timerOp;
    }

    public String getTimerId() {
        return timerId;
    }

    public void setTimerId(String timerId) {
        this.timerId = timerId;
    }

    public String getTimerOp() {
        return timerOp;
    }

    public void setTimerOp(String timerOp) {
        this.timerOp = timerOp;
    }
}
