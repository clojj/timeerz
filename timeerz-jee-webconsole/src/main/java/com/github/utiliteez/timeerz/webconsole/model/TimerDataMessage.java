package com.github.utiliteez.timeerz.webconsole.model;

public class TimerDataMessage {
    private String timerId;
    private boolean active;
    private String cronExpression;

    public TimerDataMessage(String timerId, boolean active, String cronExpression) {
        this.timerId = timerId;
        this.active = active;
        this.cronExpression = cronExpression;
    }

    public String getTimerId() {
        return timerId;
    }

    public void setTimerId(String timerId) {
        this.timerId = timerId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }
}
