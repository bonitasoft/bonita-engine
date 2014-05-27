package org.bonitasoft.engine.test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.connector.AbstractConnector;

public class BlockingConnector extends AbstractConnector {

    public static Semaphore semaphore = new Semaphore(1);

    @Override
    public void validateInputParameters() {
    }

    @Override
    protected void executeBusinessLogic() {
        try {
            System.out.println("Try aqcuire in connector");
            semaphore.tryAcquire(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        semaphore.release();
        System.out.println("semaphore in connector released");
    }
}
