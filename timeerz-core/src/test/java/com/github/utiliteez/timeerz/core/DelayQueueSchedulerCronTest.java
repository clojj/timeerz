package com.github.utiliteez.timeerz.core;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DelayQueueSchedulerCronTest {
    
    private static final Cron CRON_SINGLE_SHOT_1_SEC = (new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))).parse("1 * * * * ?");
    private static final Cron CRON_SINGLE_SHOT_5_SEC = (new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))).parse("5 * * * * ?");
    
    private static final Cron CRON = (new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))).parse("0/2 * * * * ?");

    private DelayQueueScheduler delayQueueScheduler;
    private int consumed;

    @BeforeEach
    void setUp() {
        delayQueueScheduler = new DelayQueueScheduler();
        delayQueueScheduler.startWithNewThread();
        delayQueueScheduler.debugPrint("initial timers:");
    }

    @AfterEach
    void tearDown() {
        delayQueueScheduler.stop();
    }

	@Test
	void test_repeating() throws InterruptedException {
		delayQueueScheduler.add(new TimerObjectCron("id", CRON, this::consumer, null, false, null));
		delayQueueScheduler.debugPrint();
		Thread.sleep(4000);
		delayQueueScheduler.debugPrint();
		assertEquals(2, consumed);
	}
    
    @Test
    void test_non_repeating() throws InterruptedException {
        delayQueueScheduler.add(new TimerObjectCron("id 5 sec", CRON_SINGLE_SHOT_5_SEC, this::consumer, null, true, null));
        delayQueueScheduler.add(new TimerObjectCron("id 1 sec A", CRON_SINGLE_SHOT_1_SEC, this::consumer, null, true, null));
        delayQueueScheduler.add(new TimerObjectCron("id 1 sec B", CRON_SINGLE_SHOT_1_SEC, this::consumer, null, true, null));
        delayQueueScheduler.debugPrint();
        
        Thread.sleep(65500); // TODO
        delayQueueScheduler.debugPrint();
        assertEquals(3, consumed);
    }
    
    // TODO enable ordering of equal cron-expressions
    /*
    void test_coninciding_ordered() throws InterruptedException {
        List<Integer> results = new ArrayList<>();
        delayQueueScheduler.add(new TimerObjectCron("id2", CRON, createNumberedConsumer(2, results), null, false, null));
        delayQueueScheduler.add(new TimerObjectCron("id3", CRON, createNumberedConsumer(3, results), null, false, null));
        delayQueueScheduler.add(new TimerObjectCron("id1", CRON, createNumberedConsumer(1, results), null, false, null));

        delayQueueScheduler.debugPrint();
        Thread.sleep(3000);
        delayQueueScheduler.debugPrint();

        List<Integer> expected = new ArrayList<>();
        Collections.addAll(expected, 2, 3, 1);
        assertEquals(expected, results);
    }
    */

	// TODO enable ordering of equal cron-expressions
    /*
    void test_timers_are_ordered() throws InterruptedException {
	    TimerObjectCron timerObject1 = new TimerObjectCron((new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))).parse("0/20 * * * * ?"), this::consumer);
	    delayQueueScheduler.add(timerObject1);
	    TimerObjectCron timerObject2 = new TimerObjectCron((new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))).parse("0/10 * * * * ?"), this::consumer);
	    delayQueueScheduler.add(timerObject2);
	    TimerObjectCron timerObject3 = new TimerObjectCron((new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))).parse("0/5 * * * * ?"), this::consumer);
	    delayQueueScheduler.add(timerObject3);

	    List<TimerObject> result = new ArrayList<>();
	    delayQueueScheduler.getTimers().forEach((timerObject, aLong) -> {
	    	Cron cron = ((TimerObjectCron) timerObject).getCron();
		    System.out.println("cron = " + cron.asString());
		    result.add(timerObject);
	    });
	    assertThat(result).containsExactly(timerObject3, timerObject2, timerObject1);
    }
    */

    private void consumer(Long time) {
        System.out.println("    time = " + time);
        consumed++;
    }

    Consumer<Long> createNumberedConsumer(int n, List<Integer> result) {
        return aLong -> {
            System.out.println("consumer " + n + " receives " + aLong);
            result.add(n);
        };
    }

    public void currying() {
        // Create a function that adds 2 ints
        IntBinaryOperator adder = (a, b) -> a + b;

        // And a function that takes an integer and returns a function
        IntFunction<IntUnaryOperator> currier = a -> b -> adder.applyAsInt(a, b);

        // Call apply 4 to currier (to get a function back)
        IntUnaryOperator curried = currier.apply(4);

        // Results
        System.out.printf("int curry : %d\n", curried.applyAsInt(3)); // ( 4 + 3 )
    }
}