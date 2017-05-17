package com.github.utiliteez.timeerz.core;

import java.util.*;
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
		DelayQueueTaker delayQueueTaker = new DelayQueueTaker(delayQueue);
		thread = new Thread(delayQueueTaker, "DelayQueueScheduler Default Daemon-Thread");
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.setDaemon(true);
		thread.start();
	}

	public void startWithThreadFactory(ThreadFactory threadFactory) {
		DelayQueueTaker delayQueueTaker = new DelayQueueTaker(delayQueue);
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

		private DelayQueueTaker(DelayQueue<TimerObject> delayQueue) {
			this.delayQueue = delayQueue;
		}

		public void run() {
			try {
				while (!Thread.interrupted()) {
					TimerObject timerObject = delayQueue.take();

					if (timerObject.isActive()) {

						// 1) callback object
						timerObject.getConsumer().accept(System.nanoTime());

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
	}

}
