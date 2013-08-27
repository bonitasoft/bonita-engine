/**
 * Copyright (C) 2011 BonitaSoft S.A.
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

import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 */
public class ThreadLocalSessionAccessor implements SessionAccessor {

    private final Object mutex = new Object();

    private final ThreadLocal<Map.Entry<Long, Long>> sessionData = new ThreadLocal<Map.Entry<Long, Long>>();

    public ThreadLocalSessionAccessor() {
	    System.err.println("\n\n\n\n\n\n\n\n*********************\nCREATING A THREADLOCALSESSIONACCESSOR\n*****************\n\n\n\n\n\n\n\n");
    }
    
    @Override
    public long getSessionId() throws SessionIdNotSetException {
        Long sessionId = null;
        synchronized (mutex) {
            final Entry<Long, Long> entry = sessionData.get();
            if (entry == null) {
                throw new SessionIdNotSetException("No session set.");
            }
            sessionId = entry.getKey();
        }
        if (sessionId == null) {
            throw new SessionIdNotSetException("No session set.");
        }
        return sessionId;
    }

    @Override
    public void setSessionInfo(final long sessionId, final long tenantId) {
        synchronized (mutex) {
        	if (sessionData.get() != null) {
        		throw new IllegalStateException("Session is already set to: " + sessionData.get() + ". Impossible to set it to: " + sessionId + ". Please delete it before trying to set a new value.");
        	}
            sessionData.set(new SessionInfo(sessionId, tenantId));
        }
    }

    @Override
    public void deleteSessionId() {
        synchronized (mutex) {
            sessionData.remove();
        }
    }

    @Override
    public long getTenantId() throws TenantIdNotSetException {
        Long tenantId = null;
        synchronized (mutex) {
            final Entry<Long, Long> entry = sessionData.get();
            if (entry == null) {
                throw new TenantIdNotSetException("No tenantId set.");
            }
            tenantId = entry.getValue();
        }
        if (tenantId == null) {
            throw new TenantIdNotSetException("No tenantId set.");
        }
        return tenantId;
    }

}
