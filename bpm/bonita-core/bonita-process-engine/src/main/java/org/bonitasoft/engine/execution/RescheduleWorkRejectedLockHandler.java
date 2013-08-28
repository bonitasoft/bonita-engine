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
package org.bonitasoft.engine.execution;

import org.bonitasoft.engine.lock.RejectedLockHandler;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.work.BonitaWork;
import org.bonitasoft.engine.work.WorkRegisterException;
import org.bonitasoft.engine.work.WorkService;

/**
 * 
 * A RejectedLockHandler that reschedule the work when the call to lockService.tryLock was unable to acquire the lock
 * 
 * @author Charles Souillard
 * @author Baptiste Mesta
 * 
 */
public class RescheduleWorkRejectedLockHandler implements RejectedLockHandler {

    private final TechnicalLoggerService logger;

    private final WorkService workService;

    private final BonitaWork work;

    public RescheduleWorkRejectedLockHandler(final TechnicalLoggerService logger, final WorkService workService, final BonitaWork work) {
        super();
        this.logger = logger;
        this.workService = workService;
        this.work = work;
    }

    @Override
    public void executeOnLockFree() throws SLockException {
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Failed to lock, the work will be rescheduled: " + work.getDescription());
        }
        try {
            workService.executeWork(work);
        } catch (WorkRegisterException e) {
            throw new SLockException(e);
        }
    }
}
