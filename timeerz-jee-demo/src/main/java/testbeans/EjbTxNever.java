package testbeans;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateless
public class EjbTxNever {

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void ejbTxNever() {
        System.out.println("IN ejbTxNever");
    }
}
