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
package org.bonitasoft.engine.handler;

import org.bonitasoft.engine.commons.RestartHandler;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.SchedulerService;

/**
 * @author Matthieu Chaffotte
 */
public class SchedulerServiceRestartHandler implements RestartHandler {

    private final SchedulerService schedulerService;

    private final TechnicalLoggerService technicalLoggerService;

    public SchedulerServiceRestartHandler(final SchedulerService schedulerService, final TechnicalLoggerService technicalLoggerService) {
        super();
        this.schedulerService = schedulerService;
        this.technicalLoggerService = technicalLoggerService;
    }

    @Override
    public void execute() throws SBonitaException {
        technicalLoggerService.log(this.getClass(), TechnicalLogSeverity.INFO, "Rescheduling all scheduler Triggers in ERROR state");
        schedulerService.rescheduleErroneousTriggers();
    }

}
