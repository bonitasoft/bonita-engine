package org.bonitasoft.engine.test;

import java.util.Date;

/**
 * @author Baptiste Mesta
 */
@Deprecated
public abstract class WaitUntil {

    private final int timeout;

    private final int repeatEach;

    private final boolean throwExceptions;

    /**
     * @param repeatEach
     *            time to wait for before retrying, if condition not fullfilled, in milliseconds
     * @param timeout
     *            max time to wait for, in milliseconds
     */
    public WaitUntil(final int repeatEach, final int timeout) {
        this(repeatEach, timeout, true);
    }

    /**
     * @param repeatEach
     *            time to wait for before retrying, if condition not fullfilled, in milliseconds
     * @param timeout
     *            max time to wait for, in milliseconds
     * @param throwExceptions
     *            can the check condition throw exceptions?
     */
    public WaitUntil(final int repeatEach, final int timeout, final boolean throwExceptions) {
        this.throwExceptions = throwExceptions;
        if (repeatEach > timeout) {
            throw new AssertionError("timeout " + timeout + " cannot be smaller than repeatEach " + repeatEach);
        }
        this.repeatEach = repeatEach;
        this.timeout = timeout;
    }

    public boolean waitUntil() throws Exception {
        final long limit = new Date().getTime() + timeout;
        while (new Date().getTime() < limit) {
            Thread.sleep(repeatEach);
            if (checkCondition()) {
                return true;
            }
        }
        return checkCondition();
    }

    protected boolean checkCondition() throws Exception {
        if (throwExceptions) {
            return check();
        }
        try {
            return check();
        } catch (final Exception e) {
            // do nothing
        }
        return false;
    }

    /**
     * Condition to check for.
     * 
     * @return true if condition is true, false otherwise.
     * @throws Exception
     */
    protected abstract boolean check() throws Exception;
}
