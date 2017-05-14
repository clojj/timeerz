package com.github.utiliteez.timeerz.jee.model;

import com.github.utiliteez.timeerz.core.TimerObject;

public class TimerFiredEvent {
    TimerObject timerObject;
    long now;

    public TimerFiredEvent(TimerObject timerObject, long now) {
        this.timerObject = timerObject;
        this.now = now;
    }

    public TimerObject getTimerObject() {
        return timerObject;
    }

    public long getNow() {
        return now;
    }

    @Override
    public String toString() {
        return "TimerFiredEvent{" +
                "timerObject=" + timerObject +
                ", now=" + now +
                '}';
    }
}
