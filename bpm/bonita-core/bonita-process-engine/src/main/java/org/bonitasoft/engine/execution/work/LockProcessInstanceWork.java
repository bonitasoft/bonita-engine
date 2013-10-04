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

import java.util.Map;

import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * Transactional work that lock the process instance
 * 
 * @author Charles Souillard
 * @author Baptiste Mesta
 */
public class LockProcessInstanceWork extends WrappingBonitaWork {

    private static final long serialVersionUID = -4604852239659029393L;

    protected final long processInstanceId;

    public LockProcessInstanceWork(final BonitaWork wrappedWork, final long processInstanceId) {
        super(wrappedWork);
        this.processInstanceId = processInstanceId;
    }

    @Override
    public void work(final Map<String, Object> context) throws Exception {
        LockService lockService = getTenantAccessor(context).getLockService();
        final String objectType = SFlowElementsContainerType.PROCESS.name();

        boolean lockObtained = false;
        BonitaLock lock = null;
        try {
        	lock = lockService.lock(processInstanceId, objectType);
        	lockObtained = true;
        	getWrappedWork().work(context);
        } catch (final SLockException e) {
        	//TODO: something to to to reschedule the work
        } finally {
        	if (lock != null && lockObtained) {
        		lockService.unlock(lock);
        	}
        }

    }

}
