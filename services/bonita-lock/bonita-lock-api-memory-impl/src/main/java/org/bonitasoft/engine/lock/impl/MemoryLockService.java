/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.lock.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;

/**
 * This service must be configured as a singleton.
 * 
 * @author Elias Ricken de Medeiros
 */
public class MemoryLockService extends AbstractLockService {

    private static final Object lock = new Object();

    public MemoryLockService(final TechnicalLoggerService logger, ReadSessionAccessor sessionAccessor, int lockTimeout) {
        super(logger, sessionAccessor, lockTimeout);
    }

    final Map<String, ReentrantLock> locks = new HashMap<String, ReentrantLock>();

    @Override
    protected Lock getLock(final String key) {
        synchronized (lock) {
            if (!locks.containsKey(key)) {
                // use fair mode?
                locks.put(key, new ReentrantLock());
            }
            return locks.get(key);
        }
    }

    @Override
    protected void removeLockFromMapIfnotUsed(final String key) {
        synchronized (lock) {
            ReentrantLock reentrantLock = locks.get(key);
            if (reentrantLock != null && !reentrantLock.hasQueuedThreads()) {
                locks.remove(key);
            }
        }
    }
}
