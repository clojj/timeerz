package com.github.utiliteez.timeerz.jee.model;

import com.github.utiliteez.timeerz.core.TimerObject;

public class JobCompletedEvent {
    private TimerObject timerObject;

    public JobCompletedEvent(TimerObject timerObject) {
        this.timerObject = timerObject;
    }

    public TimerObject getTimerObject() {
        return timerObject;
    }

    @Override
    public String toString() {
        return "JobCompletedEvent{" +
                "timerObject=" + timerObject +
                '}';
    }
}
