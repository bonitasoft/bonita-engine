/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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

import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    void unlock(BonitaLock lock) throws SLockException;

    /**
     * 
     * Try to acquire the lock for the object having type and id in parameters<br>
     * 
     * This method don't wait for the lock to be available
     * 
     * If the lock was not acquired the rejectedLockHandler is called
     * 
     * @param objectToLockId
     *            The id of the object to lock
     * @param objectType
     *            the type of the object to lock
     * @param rejectedLockHandler
     * @return
     * @throws SLockException
     */
    BonitaLock tryLock(long objectToLockId, String objectType, RejectedLockHandler rejectedLockHandler) throws SLockException;

    /**
     * Acquire the lock for the object having type and id in parameters<br>
     * 
     * This method wait for the lock to be available
     * 
     * @param objectToLockId
     * @param objectType
     * @return
     * @throws SLockException
     */
    BonitaLock lock(long objectToLockId, String objectType) throws SLockException;

}
