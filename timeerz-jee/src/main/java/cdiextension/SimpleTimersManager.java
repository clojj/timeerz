package cdiextension;

import de.clojj.simpletimers.DelayQueueScheduler;
import de.clojj.simpletimers.TimerObjectCron;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Logger;

@ApplicationScoped
public class SimpleTimersManager {

    // TODO use message catalog
    private static final Logger LOG = Logger.getLogger("SimpleTimersManager LOG");

    @Resource
    private ManagedThreadFactory managedThreadFactory;

    @Resource
    private ManagedExecutorService mes;

    @Inject
    Event<TimerFiredEvent> timerFiredEvent;

    private List<ScheduledMethod> scheduledMethods = new ArrayList<>();

    private DelayQueueScheduler delayQueueScheduler;

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {

        delayQueueScheduler = new DelayQueueScheduler();
        delayQueueScheduler.debugPrint("Starting timers...");
        Thread thread = managedThreadFactory.newThread(delayQueueScheduler.timerThreadInstance());
        delayQueueScheduler.startWith(thread);

        // get all bean instances
        for (ScheduledMethod scheduledMethod : scheduledMethods) {
            Object instance = null;
            if (scheduledMethod.getType() == BeanType.EJB) {
                try {
                    InitialContext ctx = new InitialContext();
                    instance = ctx.lookup("java:module/" + scheduledMethod.getClazz().getSimpleName());
                } catch (NamingException e) {
                    throw new RuntimeException("EJB not found: ", e);
                }

            } else if (scheduledMethod.getType() == BeanType.CDI) {
                instance = CDI.current().select(scheduledMethod.getClazz()).get();
            } else {
                throw new IllegalStateException();
            }
            scheduledMethod.setInstance(instance);
        }

        // queue all timers
        for (ScheduledMethod scheduledMethod : scheduledMethods) {
            TimerObjectCron timerObject = new TimerObjectCron(scheduledMethod.getMethod().getJavaMember().toString(), scheduledMethod.getCron());
            timerObject.setConsumer(now -> {
                // TODO "exclusive job" as an option
                // TODO list of N futures... N jobs allowed concurrently
                CompletableFuture<Object> job = timerObject.getCompletableFuture();
                if (job != null) {
                    if (job.isDone()) {
                        createJob(scheduledMethod, timerObject);
                    } else {
                        LOG.info("Job running.. skipping timer");
                    }
                } else {
                    createJob(scheduledMethod, timerObject);
                }

                // Future<?> future = mes.submit(runnableMethod(scheduledMethod));

                timerFiredEvent.fire(new TimerFiredEvent(timerObject, now));
            });

            delayQueueScheduler.add(timerObject);
        }
    }

    private void createJob(ScheduledMethod scheduledMethod, TimerObjectCron timerObject) {
        CompletableFuture<Object> completableFuture = CompletableFuture.supplyAsync(runnableMethod(scheduledMethod), mes);
        timerObject.setCompletableFuture(completableFuture);
        LOG.info("Start next Job");
    }

    private Supplier<Object> runnableMethod(ScheduledMethod scheduledMethod) {
        return () -> {
            try {
                return scheduledMethod.getMethod().getJavaMember().invoke(scheduledMethod.getInstance());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("method call failed: ", e);
            }
        };
    }

    public List<ScheduledMethod> getScheduledMethods() {
        return scheduledMethods;
    }

    public void destroy(@Observes @Destroyed(ApplicationScoped.class) Object init) {
        delayQueueScheduler.stop();
    }
}