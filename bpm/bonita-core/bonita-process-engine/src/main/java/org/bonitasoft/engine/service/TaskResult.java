/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.service;

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
