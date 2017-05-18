package testbeans;

import com.github.utiliteez.timeerz.jee.model.TimerFiredEvent;

import javax.ejb.Singleton;
import javax.enterprise.event.Observes;

@Singleton
public class ObserverSingleton {

    public void observeTimers(@Observes TimerFiredEvent timerFiredEvent) {
         System.out.println("Received timerFiredEvent = " + timerFiredEvent);
    }

}
