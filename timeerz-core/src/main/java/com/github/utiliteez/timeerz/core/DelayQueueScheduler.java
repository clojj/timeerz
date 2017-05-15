package com.github.utiliteez.timeerz.core;

import java.util.*;
import java.util.concurrent.DelayQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DelayQueueScheduler {

	private final DelayQueue<TimerObject> delayQueue;

	private Map<String, TimerObject> timers = Collections.synchronizedMap(new HashMap<>());

	private Thread thread = null;
	private DelayQueueTaker delayQueueTaker;

	private static final Logger LOG = Logger.getLogger("DelayQueueScheduler LOG");

	public DelayQueueScheduler() {
		delayQueue = new DelayQueue<>();
	}

	public Thread createDefaultThread(boolean isDaemon, Runnable runnable) {
		Thread thread = new Thread(runnable, "DelayQueueScheduler-Thread");
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.setDaemon(isDaemon);
		return thread;
	}

	public boolean add(TimerObject timerObject) {
		timers.put(timerObject.getId(), timerObject);
		return delayQueue.add(timerObject);
	}

	// todo: add( timerId), remove( timerId ), reconfigure( timerId, cronExpression )

	public Map<String, TimerObject> getTimers() {
		return timers;
	}

	public List<TimerObject> allTimers() {
		ArrayList<TimerObject> timerObjects = new ArrayList<>(timers.values());
		timerObjects.sort(Comparator.comparing(TimerObject::getId));
		return timerObjects;
	}

	public void startWith(Thread thread) {
		this.thread = thread;
		thread.start();
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

	public DelayQueueTaker timerThreadInstance() {
		if (delayQueueTaker == null) {
			delayQueueTaker = new DelayQueueTaker();
		}
		return delayQueueTaker;
	}

	public class DelayQueueTaker implements Runnable {

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
