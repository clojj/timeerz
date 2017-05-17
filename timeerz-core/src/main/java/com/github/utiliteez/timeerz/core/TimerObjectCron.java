package com.github.utiliteez.timeerz.core;

import com.cronutils.model.Cron;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.time.ExecutionTime;

import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TimerObjectCron implements TimerObject {

    private final String id;
    private long startTime;
    private Cron cron;
    private final boolean repeat;

    private Consumer<Long> eventConsumer;
    private Supplier<Object> runnableMethod;
    private CompletableFuture<Object> job;

	private boolean active;

	public TimerObjectCron(String id, Cron cron, Consumer<Long> eventConsumer, Supplier<Object> runnableMethod) {
        this.id = id;
        this.cron = cron;
        this.startTime = ExecutionTime.forCron(cron).nextExecution(ZonedDateTime.now()).toInstant().toEpochMilli();
        this.repeat = cron.retrieveFieldsAsMap().values().stream().anyMatch(cronField -> cronField.getExpression() instanceof Every);
        this.eventConsumer = eventConsumer;
        this.runnableMethod = runnableMethod;
        this.active = true;
    }

    public TimerObjectCron(String id, Cron cron) {
        this(id, cron, null, null);
    }

    public String getId() {
        return id;
    }

    public void setEventConsumer(Consumer<Long> eventConsumer) {
        this.eventConsumer = eventConsumer;
    }

    @Override
    public Supplier<Object> getRunnableMethod() {
        return runnableMethod;
    }

    @Override
    public Consumer<Long> getEventConsumer() {
        return eventConsumer;
    }

    public CompletableFuture<Object> getJob() {
        return job;
    }

    public void setJob(CompletableFuture<Object> job) {
        this.job = job;
    }

    @Override
    public void reset() {
        this.startTime = ExecutionTime.forCron(cron).nextExecution(ZonedDateTime.now()).toInstant().toEpochMilli();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long delta = startTime - System.currentTimeMillis();
        return unit.convert(delta, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        TimerObjectCron timerObjectCron = (TimerObjectCron) o;
        return compareStartTime(timerObjectCron);
    }

    private int compareStartTime(TimerObjectCron timerObjectCron) {
        if (this.startTime < timerObjectCron.startTime) {
            return -1;
        }
        if (this.startTime > timerObjectCron.startTime) {
            return 1;
        }
        return 0;
    }

    @Override
    public boolean isRepeat() {
        return repeat;
    }

	@Override
	public synchronized boolean isActive() {
		return active;
	}

	public synchronized boolean toggleActivation() {
		this.active = !this.active;
		return this.active;
	}

    @Override
    public String toString() {
        return "TimerObjectCron{" +
                "id='" + id + '\'' +
                ", startTime=" + startTime +
                ", cron=" + cron +
                ", repeat=" + repeat +
                '}';
    }

}