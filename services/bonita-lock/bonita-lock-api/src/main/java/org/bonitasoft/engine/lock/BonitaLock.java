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

import java.util.concurrent.locks.Lock;

/**
 * 
 * 
 * @author Baptiste Mesta
 * 
 */
public class BonitaLock {

    private final Lock lock;

    private final String objectType;

    private final long objectToLockId;

    public BonitaLock(final Lock lock, final String objectType, final long objectToLockId) {
        super();
        this.lock = lock;
        this.objectType = objectType;
        this.objectToLockId = objectToLockId;
    }

    public Lock getLock() {
        return lock;
    }

    public String getObjectType() {
        return objectType;
    }

    public long getObjectToLockId() {
        return objectToLockId;
    }

    @Override
    public String toString() {
        return "BonitaLock[" + objectType + ":" + objectToLockId + ", lock=" + lock.hashCode() + "]";
    }

}
