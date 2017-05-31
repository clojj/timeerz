package com.github.utiliteez.timeerz.core;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Delayed;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.cronutils.model.Cron;

public interface TimerObject extends Delayed {

    String getId();
    void reset();
    boolean isRepeat();
    boolean isActive();
    boolean toggleActivation();
    void changeCron(Cron cron);
    
    Consumer<Long> getTimerEventConsumer();
    Supplier<Object> getJobMethod();
    Queue<CompletableFuture> getJobs();
    boolean isExclusive();
	Runnable getJobCompletionRunnable();
}
