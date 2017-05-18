package com.github.utiliteez.timeerz.core;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Delayed;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface TimerObject extends Delayed {

    String getId();
    void reset();
    Consumer<Long> getEventConsumer();
    Supplier<Object> getRunnableMethod();
    List<CompletableFuture<Object>> getJobs();
    boolean isRepeat();
    boolean isExclusive();
    boolean isActive();
    boolean toggleActivation();
}
