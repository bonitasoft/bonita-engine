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

    /**
     * Create an exclusive lock for the given object id and type. This method wait until a token is available and take it. Once the token is taken all call to
     * createExclusiveLockAccess or createSharedLockAccess will be blocked until the token is released by calling releaseExclusiveLockAccess.
     * 
     * @param objectToLockId
     *            the id of object to be locked
     * @param objectType
     *            the type of the object to be locked
     * @throws SLockException
     *             if an exception occurs while acquiring the lock
     * @since 6.0
     */
    void createExclusiveLockAccess(long objectToLockId, String objectType) throws SLockException;

    /**
     * Release the exclusive lock taken by createExclusiveLockAccess
     * 
     * @param objectToLockId
     *            the id of object to be released
     * @param objectType
     *            the type of the object to be released
     * @throws SLockException
     *             if an exception occurs while releasing the lock
     * @since 6.0
     */
    void releaseExclusiveLockAccess(long objectToLockId, String objectType) throws SLockException;

    /**
     * Create a shared lock for the given object id and type. This method wait until a token is available and take it. Once the token is taken calls to
     * createSharedLockAccess will be allowed, but calls to createExclusiveLockAccess will be blocked until token is released by calling releaseSharedLockAccess
     * 
     * @param objectToLockId
     *            the id of object to be locked
     * @param objectType
     *            the type of the object to be locked
     * @throws SLockException
     *             if an exception occurs while acquiring the lock
     * @since 6.0
     */
    void createSharedLockAccess(long objectToLockId, String objectType) throws SLockException;

    /**
     * Release the shared lock taken by createSharedLockAccess. If there are no more shared lock executing the token is released and calls to
     * createExclusiveLockAccess can be executed
     * 
     * @param objectToLockId
     *            the id of object to be released
     * @param objectType
     *            the type of the object to be released
     * @throws SLockException
     *             if an exception occurs while releasing the lock
     * @since 6.0
     */
    void releaseSharedLockAccess(long objectToLockId, String objectType) throws SLockException;

}
