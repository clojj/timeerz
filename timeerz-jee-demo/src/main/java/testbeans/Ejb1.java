package testbeans;

import com.github.utiliteez.timeerz.jee.annotation.Timeer;

import javax.ejb.Stateless;

@Stateless
public class Ejb1 {

    public Ejb1() {
    }

    @Timeer(value = "0/1 * * * * ?")
    public void ejb1Method() {
        System.out.println("ejb1Method in thread " + Thread.currentThread().getName());
        Utils.sleep(1000);
    }

}
