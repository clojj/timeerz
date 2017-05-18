package testbeans;

import com.github.utiliteez.timeerz.jee.model.JobCompletedEvent;
import com.github.utiliteez.timeerz.jee.model.TimerFiredEvent;

import javax.ejb.Singleton;
import javax.enterprise.event.Observes;

@Singleton
public class ObserverSingleton {

    public void observeTimerFiredEvent(@Observes TimerFiredEvent timerFiredEvent) {
         System.out.println("observed timerFiredEvent = " + timerFiredEvent);
    }

    public void observeJobCompletedEvent(@Observes JobCompletedEvent jobCompletedEvent) {
         System.out.println("observed jobCompletedEvent = " + jobCompletedEvent);
    }

}
