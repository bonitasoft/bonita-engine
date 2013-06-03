package org.bonitasoft.engine.monitoring;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

public abstract class WaitFor {

    private final int timeout;

    private final int repeatEach;

    public WaitFor(final int repeatEach, final int timeout) {
        assertTrue("timeout is not big enough", repeatEach < timeout);
        this.repeatEach = repeatEach;
        this.timeout = timeout;
    }

    public boolean waitFor() throws InterruptedException, AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
        final long limit = new Date().getTime() + timeout;
        while (new Date().getTime() < limit) {
            Thread.sleep(repeatEach);
            if (check()) {
                return true;
            }
        }
        return check();
    }

    abstract boolean check() throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException;

}
