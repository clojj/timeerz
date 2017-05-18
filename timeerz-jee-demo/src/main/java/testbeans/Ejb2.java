package testbeans;

import javax.ejb.Stateless;

@Stateless
public class Ejb2 {

    public Ejb2() {
    }

    // @Timeer(value = "0/3 * * * * ?")
    public void ejb2Method() {
        System.out.println("ejb2Method in thread " + Thread.currentThread().getName());
        Utils.sleep(5000);
    }

}
