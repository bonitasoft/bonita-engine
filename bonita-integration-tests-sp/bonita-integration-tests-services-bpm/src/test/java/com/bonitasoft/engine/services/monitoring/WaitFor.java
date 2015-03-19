/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.services.monitoring;

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
