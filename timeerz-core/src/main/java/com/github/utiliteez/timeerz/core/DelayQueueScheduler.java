package com.github.utiliteez.timeerz.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DelayQueueScheduler {

    private static final Logger LOG = Logger.getLogger("DelayQueueScheduler LOG");

    private final DelayQueue<TimerObject> delayQueue = new DelayQueue<>();

    private Map<String, TimerObject> timers = Collections.synchronizedMap(new HashMap<>());

    private Thread thread;

    // TODO
    private Executor executor;

    public DelayQueueScheduler() {
    }

    public DelayQueueScheduler(Executor executor) {
        this.executor = executor;
    }

    public void startWithNewThread() {
        DelayQueueTaker delayQueueTaker = new DelayQueueTaker(delayQueue, executor);
        thread = new Thread(delayQueueTaker, "DelayQueueScheduler Default Daemon-Thread");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        thread.start();
    }

    public void startWithThreadFactory(ThreadFactory threadFactory) {
        DelayQueueTaker delayQueueTaker = new DelayQueueTaker(delayQueue, executor);
        thread = threadFactory.newThread(delayQueueTaker);
        thread.setName("DelayQueueTaker");
        thread.start();
    }

    public boolean add(TimerObject timerObject) {
        timers.put(timerObject.getId(), timerObject);
        return delayQueue.add(timerObject);
    }

    // todo: remove( timerId ), reconfigure( timerId, cronExpression )

    public List<TimerObject> allTimers() {
        ArrayList<TimerObject> timerObjects = new ArrayList<>(timers.values());
        timerObjects.sort(Comparator.comparing(TimerObject::getId));
        return timerObjects;
    }

    public void stop() {
        thread.interrupt();
    }

    public void debugPrint() {
        debugPrint("timers:");
    }

    public void debugPrint(String message) {
        LOG.log(Level.FINE, () -> message != null ? message : "timers:");
        for (TimerObject timerObject : delayQueue) {
            LOG.log(Level.FINE, "TimerObject: %s", timerObject);
        }
    }

    public boolean toggleActivation(final TimerObject toDeactivate) {
        return delayQueue.remove(toDeactivate);
    }

    public boolean toggleActivation(final String timerId) {
        TimerObject timerObject = timers.get(timerId);
        boolean activation = timerObject.toggleActivation();
        return activation ? delayQueue.add(timerObject) : delayQueue.remove(timerObject);
    }

    public int size() {
        return delayQueue.size();
    }

    private class DelayQueueTaker implements Runnable {

        private DelayQueue<TimerObject> delayQueue;

        private Executor executor;

        private DelayQueueTaker(DelayQueue<TimerObject> delayQueue, Executor executor) {
            this.delayQueue = delayQueue;
            this.executor = executor;
        }

        public void run() {
            try {
                while (!Thread.interrupted()) {
                    TimerObject timerObject = delayQueue.take();

                    if (timerObject.isActive()) {

                        // TODO use result (Object) of async job

                        timerObject.getEventConsumer().accept(System.nanoTime());

                        if (timerObject.getRunnableMethod() != null) {
                            executeJob(timerObject);
                        }

                        // TODO JMX monitoring ?

                        if (timerObject.isRepeat()) {
                            timerObject.reset();
                            delayQueue.add(timerObject);
                        }
                    }
                }
            } catch (InterruptedException e) {
                LOG.log(Level.WARNING, "Timer thread interrupted: ", e);
                Thread.currentThread().interrupt();
            }
        }

        private void executeJob(TimerObject timerObject) {
            // TODO list of N jobs allowed concurrently
            ConcurrentLinkedQueue<CompletableFuture> jobs = timerObject.getJobs();
            CompletableFuture job = null;
            if (timerObject.isExclusive() && jobs.peek() != null) {
                LOG.info("Exclusive job running.. skip timer invokation for " + timerObject.getId());
                return;
            }
            createAsyncJob(timerObject);
        }

        private void createAsyncJob(TimerObject timerObject) {
            CompletableFuture<Object> job = executor != null ? CompletableFuture.supplyAsync(timerObject.getRunnableMethod(), executor) : CompletableFuture.supplyAsync(timerObject.getRunnableMethod());
            ConcurrentLinkedQueue<CompletableFuture> jobs = timerObject.getJobs();
            jobs.add(job);
            System.out.println("jobs: " + jobs.size());
            job.thenRun(timerObject.getJobCompletionRunnable()).thenRun(() -> {
                jobs.remove(job);
            });
        }
    }

}
