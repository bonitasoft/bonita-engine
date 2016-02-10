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
package org.bonitasoft.engine.sessionaccessor;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 */
public class ThreadLocalSessionAccessor implements SessionAccessor {

    private final ThreadLocal<Long> sessionData = new ThreadLocal<Long>();

    private final ThreadLocal<Long> tenantData = new ThreadLocal<Long>();

    @Override
    public long getSessionId() throws SessionIdNotSetException {
        Long sessionId = sessionData.get();
        if (sessionId == null) {
            throw new SessionIdNotSetException("No session set.");
        }
        return sessionId;
    }

    @Override
    public void setSessionInfo(final long sessionId, final long tenantId) {
        sessionData.set(sessionId);
        tenantData.set(tenantId);
    }

    @Override
    public void setTenantId(final long tenantId) {
        tenantData.set(tenantId);
    }

    @Override
    public void deleteSessionId() {
        sessionData.remove();
    }

    @Override
    public void deleteTenantId() {
        tenantData.remove();
    }

    @Override
    public long getTenantId() throws STenantIdNotSetException {
        final Long tenantId = tenantData.get();
        if (tenantId == null) {
            throw new STenantIdNotSetException("No tenantId set.");
        }
        return tenantId;
    }

}
