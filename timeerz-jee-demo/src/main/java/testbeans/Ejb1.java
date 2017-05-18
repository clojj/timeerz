package testbeans;

import javax.ejb.Stateless;

@Stateless
public class Ejb1 {

    public Ejb1() {
    }

    // @Timeer(value = "0/1 * * * * ?", exclusive = false)
    public void ejb1Method() {
        System.out.println("ejb1Method in thread " + Thread.currentThread().getName());
        Utils.sleep(5000);
    }

}
