package testbeans;

import javax.ejb.Singleton;
import javax.enterprise.event.Observes;

import com.github.utiliteez.timeerz.jee.model.TimerFiredEvent;

@Singleton
public class ObserverSingleton {

    public void observeTimers(@Observes TimerFiredEvent timerFiredEvent) {
         System.out.println("Received timerFiredEvent = " + timerFiredEvent);
    }

}
