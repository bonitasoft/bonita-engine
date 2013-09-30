package org.bonitasoft.engine.test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

public class BlockingConnector extends AbstractConnector {

    public static Semaphore semaphore = new Semaphore(1);

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
    }

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        try {
            System.out.println("Try aqcuire");
            semaphore.tryAcquire(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        semaphore.release();
    }
}
