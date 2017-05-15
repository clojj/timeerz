package com.github.utiliteez.timeerz.core;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TimerObjectInterval implements TimerObject {
    private final long interval;
    private TimeUnit timeUnit;
    private long startTime;
    private final boolean repeat;
    private final Consumer<Long> consumer;
	private boolean active;

	public TimerObjectInterval(long interval, TimeUnit timeUnit, boolean repeat, Consumer<Long> consumer) {
        this.interval = interval;
        this.timeUnit = timeUnit;
        this.startTime = currentTime(timeUnit) + this.interval;
        this.repeat = repeat;
        this.consumer = consumer;
    }

    @Override
    public String getId() {
        return "42";
    }

    public void reset() {
        this.startTime = currentTime(timeUnit) + interval;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = startTime - currentTime(timeUnit);
        return unit.convert(diff, timeUnit);
    }

    @Override
    public int compareTo(Delayed o) {
        if (this.startTime < ((TimerObjectInterval) o).startTime) {
            return -1;
        }
        if (this.startTime > ((TimerObjectInterval) o).startTime) {
            return 1;
        }
        return 0;
    }

    public Consumer<Long> getConsumer() {
        return consumer;
    }

    public boolean isRepeat() {
        return repeat;
    }

	@Override
	public synchronized boolean isActive() {
		return active;
	}

	@Override
	public synchronized boolean toggleActivation() {
        this.active = !this.active;
        return this.active;
	}

    private long currentTime(TimeUnit timeUnit) {
        // these units are not possible in Quartz cron.. so they are provided in TimerObjectInterval !
        switch (timeUnit) {
            case NANOSECONDS:
                return System.nanoTime();
            case MICROSECONDS:
                return System.nanoTime() / 1000;
            case MILLISECONDS:
                return System.currentTimeMillis();
        }
        throw new IllegalArgumentException("TimeUnit " + timeUnit + " not supported");
    }

    @Override
    public String toString() {
        return "TimerObjectInterval{" +
                "interval=" + interval +
                ", timeUnit=" + timeUnit +
                ", startTime=" + startTime +
                ", repeat=" + repeat +
                '}';
    }
}