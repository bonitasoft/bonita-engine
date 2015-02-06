/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service;

import java.util.concurrent.TimeUnit;

/**
 * 
 * result of the execution of a task using the broadcast service
 * 
 * @author Baptiste Mesta
 * 
 */
public class TaskResult<T> {

    private Throwable throwable;

    private T result;

    private Long timeout;

    private TimeUnit timeunit;

    public TaskResult(final Throwable e) {
        this.throwable = e;
    }

    /**
     * @param result
     */
    public TaskResult(final T result) {
        this.result = result;
    }

    /**
     * @param timeout
     * @param timeunit
     */
    public TaskResult(final Long timeout, final TimeUnit timeunit) {
        this.timeout = timeout;
        this.timeunit = timeunit;
    }

    public static <T> TaskResult<T> error(final Throwable e) {
        return new TaskResult<T>(e);
    }

    public static <T> TaskResult<T> ok(final T result) {
        return new TaskResult<T>(result);
    }

    public static <T> TaskResult<T> timeout(final long timeout, final TimeUnit timeunit) {
        return new TaskResult<T>(timeout, timeunit);
    }

    public boolean isError() {
        return throwable != null;
    }

    public boolean isOk() {
        return !isError() && !isTimeout();
    }

    public boolean isTimeout() {
        return timeout != null;
    }

    /**
     * @return the result
     */
    public T getResult() {
        return result;
    }

    /**
     * @return the throwable
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * @return the timeout
     */
    public Long getTimeout() {
        return timeout;
    }

    /**
     * @return the timeunit
     */
    public TimeUnit getTimeunit() {
        return timeunit;
    }

}
