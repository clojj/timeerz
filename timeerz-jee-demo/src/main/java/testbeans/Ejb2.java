package testbeans;

import com.github.utiliteez.timeerz.jee.annotation.Timeer;

import javax.ejb.Stateless;

@Stateless
public class Ejb2 {

    public Ejb2() {
    }

    @Timeer(value = "0/2 * * * * ?", exclusive = false)
    public void ejb2Method() {
        System.out.println("ejb2Method in thread " + Thread.currentThread().getName());
        Utils.sleep(1000);
    }

}
