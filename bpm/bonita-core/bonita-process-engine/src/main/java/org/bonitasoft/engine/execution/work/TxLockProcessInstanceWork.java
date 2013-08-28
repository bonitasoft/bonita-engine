/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution.work;

import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.execution.RescheduleWorkRejectedLockHandler;
import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.RejectedLockHandler;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.work.WorkService;

/**
 * 
 * Transactional work that lock the process instance
 * 
 * @author Charles Souillard
 * @author Baptiste Mesta
 * 
 */
public abstract class TxLockProcessInstanceWork extends TxBonitaWork {

    private static final long serialVersionUID = -4604852239659029393L;

    protected final long processInstanceId;

    private transient LockService lockService;

    private transient BonitaLock lock;

    public TxLockProcessInstanceWork(long processInstanceId) {
        super();
        this.processInstanceId = processInstanceId;

    }

    @Override
    protected boolean preWork() throws Exception {
        this.lockService = getTenantAccessor().getLockService();
        final WorkService workService = getTenantAccessor().getWorkService();
        final TechnicalLoggerService logger = getTenantAccessor().getTechnicalLoggerService();
        final String objectType = SFlowElementsContainerType.PROCESS.name();

        final RejectedLockHandler handler = new RescheduleWorkRejectedLockHandler(logger, workService, this);

        this.lock = lockService.tryLock(processInstanceId, objectType, handler);
        if (lock == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void afterWork() throws Exception {
        lockService.unlock(lock);
    }
}
