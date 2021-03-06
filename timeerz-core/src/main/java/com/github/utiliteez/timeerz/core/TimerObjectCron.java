package com.github.utiliteez.timeerz.core;

import java.time.ZonedDateTime;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.cronutils.model.Cron;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.time.ExecutionTime;

public class TimerObjectCron implements TimerObject {

    private final String id;
	private Cron cron;

	private Long startTime;
    private boolean repeat;

    private Consumer<Long> eventConsumer;
    private Supplier<Object> runnableMethod;
    private boolean exclusive;
    private ConcurrentLinkedQueue<CompletableFuture> jobs = new ConcurrentLinkedQueue<>();
	private Runnable jobCompletionRunnable;

	private boolean active;

	public TimerObjectCron(String id, final Cron cron, Consumer<Long> eventConsumer, Supplier<Object> runnableMethod, boolean exclusive, final Runnable jobCompletionRunnable) {
        this.id = id;
        this.cron = cron;
        this.eventConsumer = eventConsumer;
        this.runnableMethod = runnableMethod;
        this.exclusive = exclusive;
		this.jobCompletionRunnable = jobCompletionRunnable;
		this.active = true;
    }

    public String getId() {
        return id;
    }
    
    public void setEventConsumer(Consumer<Long> eventConsumer) {
        this.eventConsumer = eventConsumer;
    }

    @Override
    public Supplier<Object> getJobMethod() {
        return runnableMethod;
    }

    @Override
    public Consumer<Long> getTimerEventConsumer() {
        return eventConsumer;
    }

    @Override
    public Queue<CompletableFuture> getJobs() {
        return jobs;
    }

    @Override
    public void reset() {
        this.startTime = null;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long delta;
        initializeStartTime();
        delta = startTime - System.currentTimeMillis();
        return unit.convert(delta, TimeUnit.MILLISECONDS);
    }
    
    private void initializeStartTime() {
        if (this.startTime == null) {
        	synchronized (this) {
		        this.startTime = ExecutionTime.forCron(cron).nextExecution(ZonedDateTime.now()).toInstant().toEpochMilli();
		        this.repeat = cron.retrieveFieldsAsMap().values().stream().anyMatch(cronField -> cronField.getExpression() instanceof Every);
	        }
        }
    }
    
    @Override
    public int compareTo(Delayed o) {
        TimerObjectCron timerObjectCron = (TimerObjectCron) o;
        initializeStartTime();
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
    public boolean isExclusive() {
        return exclusive;
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
	public synchronized void changeCron(final Cron cron) {
		this.cron = cron;
	}

	@Override
	public Runnable getJobCompletionRunnable() {
		return jobCompletionRunnable;
	}

    public void setJobCompletionRunnable(Runnable jobCompletionRunnable) {
        this.jobCompletionRunnable = jobCompletionRunnable;
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