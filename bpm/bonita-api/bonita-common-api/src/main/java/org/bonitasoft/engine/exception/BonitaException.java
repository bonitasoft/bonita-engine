/**
 * Copyright (C) 2011-2014 Bonitasoft S.A.
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
package org.bonitasoft.engine.exception;

/**
 * The class BonitaException and its subclasses are a form of Throwable that indicates conditions that a reasonable application might want to catch.
 * The class BonitaException and its subclasses that are not also subclasses of {@link RuntimeException} are checked exceptions.
 * Checked exceptions need to be declared in a method or constructor's {@literal throws} clause if they can be thrown by the execution of the method or
 * constructor and propagate outside the method or constructor boundary.
 * 
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Aurelien Pupier
 */
public class BonitaException extends Exception implements BonitaContextException {

    private static final long serialVersionUID = -5413586694735909486L;

    private long tenantId = -1;

    private String hostname = "";

    private String userName = "";

    private long threadId = -1;

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param message
     *            The detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     * @param cause
     *            The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A null value is permitted, and indicates that the
     *            cause is nonexistent or unknown.)
     */
    public BonitaException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified detail message.
     * 
     * @param message
     *            The detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     */
    public BonitaException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail cause.
     * 
     * @param cause
     *            The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A null value is permitted, and indicates that the
     *            cause is nonexistent or unknown.)
     */
    public BonitaException(final Throwable cause) {
        super(cause);
    }

    /**
     * @see org.bonitasoft.engine.exception.BonitaContextException#getTenantId()
     */
    @Override
    public long getTenantId() {
        return tenantId;
    }

    /**
     * @see org.bonitasoft.engine.exception.BonitaContextException#setTenantId(long)
     */
    @Override
    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * @see org.bonitasoft.engine.exception.BonitaContextException#getHostname()
     */
    @Override
    public String getHostname() {
        return hostname;
    }

    /**
     * @see org.bonitasoft.engine.exception.BonitaContextException#setHostname(java.lang.String)
     */
    @Override
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * @see org.bonitasoft.engine.exception.BonitaContextException#getUserName()
     */
    @Override
    public String getUserName() {
        return userName;
    }

    /**
     * @see org.bonitasoft.engine.exception.BonitaContextException#setUserName(java.lang.String)
     */
    @Override
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public long getThreadId() {
        return threadId;
    }

    @Override
    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    @Override
    public String getMessage() {
        return getThreadIdMessage() + getHostNameMessage() + getTenantIdMessage() + getUserNameMessage() + super.getMessage();
    }

    private String getThreadIdMessage() {
        return threadId != -1 ? "THREAD_ID=" + threadId + " | " : "";
    }

    private String getHostNameMessage() {
        return hostname != null && !hostname.isEmpty() ? "HOSTNAME=" + hostname + " | " : "";
    }

    private String getUserNameMessage() {
        return userName != null && !userName.isEmpty() ? "USERNAME=" + userName + " | " : "";
    }

    private String getTenantIdMessage() {
        return tenantId != -1 ? "TENANT_ID=" + tenantId + " | " : "";
    }

}
