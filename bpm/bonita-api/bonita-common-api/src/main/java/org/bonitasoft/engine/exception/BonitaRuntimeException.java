/**
 * Copyright (C) 2011, 2014 BonitaSoft S.A.
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
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @author Aurelien Pupier
 */
public class BonitaRuntimeException extends RuntimeException implements BonitaContextException {

    private static final long serialVersionUID = -5413586694735909486L;

    private long tenantId = -1;

    private String hostname = "";

    private String userName = "";

    private long threadId = -1;

    public BonitaRuntimeException(final String message) {
        super(message);
    }

    public BonitaRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public BonitaRuntimeException(final Throwable cause) {
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
