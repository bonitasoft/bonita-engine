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
package org.bonitasoft.engine.lock;

import java.util.concurrent.TimeUnit;

/**
 * This service allows to synchronize access to a resource using ReadWrite lock pattern
 * Creating a an exclusive lock allows to be the only one at a time accessing the resource
 * 
 * @see ReentrantReadWriteLock
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public interface LockService {

    void unlock(BonitaLock lock, long tenantId) throws SLockException;

    /**
     * Acquire the lock for the object having type and id in parameters<br>
     * 
     * This method wait for the lock to be available
     * 
     * @param objectToLockId
     * @param objectType
     * @param tenantId
     *            TODO
     * @return
     * @throws SLockException
     */
    BonitaLock lock(long objectToLockId, String objectType, long tenantId) throws SLockException;

    /**
     * Acquire the lock for the object having type and id in parameters waiting maximum timeout<br>
     * 
     * This method wait for the lock to be available. If it becomes available before the timeout expires the returns the obtained lock, else returns null
     * 
     * @param objectToLockId
     * @param objectType
     * @param timeout
     * @param timeUnit
     * @param tenantId
     *            TODO
     * @return the obtained lock if it has been acquired before the timeout expires or null if the timeout has expired.
     */
    BonitaLock tryLock(long objectToLockId, String objectType, long timeout, TimeUnit timeUnit, long tenantId);
}
