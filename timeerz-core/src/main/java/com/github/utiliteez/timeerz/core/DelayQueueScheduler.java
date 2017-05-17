package com.github.utiliteez.timeerz.core;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
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
		thread.start();
	}

	public boolean add(TimerObject timerObject) {
		timers.put(timerObject.getId(), timerObject);
		return delayQueue.add(timerObject);
	}

	// todo: add( timerId), remove( timerId ), reconfigure( timerId, cronExpression )

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

						// 0) call async job-method
						// TODO use result (Object) of async job

						if (timerObject.getRunnableMethod() != null) {
							executeJob(timerObject);
						}

						// 1) call event consumer
						timerObject.getEventConsumer().accept(System.nanoTime());

						// 2) TODO create global callback/event
						// this should enable monitoring all Timer-events (JMX, CDI, ...?)

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
			CompletableFuture<Object> job = timerObject.getJob();
			if (job != null) {
                // TODO "exclusive job" as an option
                // TODO list of N jobs allowed concurrently
                if (job.isDone()) {
                    job = createAsyncJob(timerObject);
                    LOG.info("Start next Job");
                } else {
                    LOG.info("Job running.. skipping timer");
                }
            } else {
				job = createAsyncJob(timerObject);
                LOG.info("Start next Job");
            }
			timerObject.setJob(job);
		}

		private CompletableFuture<Object> createAsyncJob(TimerObject timerObject) {
			return executor != null ? CompletableFuture.supplyAsync(timerObject.getRunnableMethod(), executor) : CompletableFuture.supplyAsync(timerObject.getRunnableMethod());
		}
	}

}
