package com.github.utiliteez.timeerz.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Delayed;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface TimerObject extends Delayed {

    String getId();
    void reset();
    Consumer<Long> getEventConsumer();
    Supplier<Object> getRunnableMethod();
    CompletableFuture<Object> getJob();
    void setJob(CompletableFuture<Object> job);
    boolean isRepeat();
    boolean isActive();
    boolean toggleActivation();
}
