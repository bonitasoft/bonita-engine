/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.execution.work;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.work.BonitaWork;
import org.bonitasoft.engine.work.LockException;
import org.bonitasoft.engine.work.LockTimeoutException;

/**
 * Transactional work that lock the process instance
 * This work try to lock a process instance, if it can't be locked before the end of the TIMEOUT, we reschedule the fill
 * stack of work on the work service.
 *
 * @author Charles Souillard
 * @author Baptiste Mesta
 */
@Slf4j
public class LockProcessInstanceWork extends WrappingBonitaWork {

    protected final long processInstanceId;

    private static final long TIMEOUT = 20;

    private final TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    public LockProcessInstanceWork(final BonitaWork wrappedWork, final long processInstanceId) {
        super(wrappedWork);
        this.processInstanceId = processInstanceId;
    }

    @Override
    public CompletableFuture<Void> work(final Map<String, Object> context) throws Exception {
        final LockService lockService = getTenantAccessor(context).getLockService();
        final String objectType = SFlowElementsContainerType.PROCESS.name();

        BonitaLock lock = null;
        try {
            log.debug("{} trying to get lock for instance {}: {}", Thread.currentThread().getName(),
                    processInstanceId, getDescription());
            lock = lockService.tryLock(processInstanceId, objectType, TIMEOUT, timeUnit, getTenantId());
            if (lock == null) {
                throw new LockTimeoutException(
                        "Unable to lock process instance " + processInstanceId + ": " + getDescription());
            }
            log.debug("{} obtained lock for instance {}: {}", Thread.currentThread().getName(), processInstanceId,
                    getDescription());
            return getWrappedWork().work(context);
        } catch (SLockException e) {
            throw new LockException("Unexpected exception happened while trying to lock process instance "
                    + processInstanceId + ": " + getDescription(), e);
        } finally {
            if (lock != null) {
                lockService.unlock(lock, getTenantId());
                log.debug("{} has unlocked lock for instance {}: {}", Thread.currentThread().getName(),
                        processInstanceId, getDescription());
            }
        }

    }

}
