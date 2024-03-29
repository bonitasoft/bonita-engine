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
package org.bonitasoft.engine.handler;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.commons.PlatformRestartHandler;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.transaction.UserTransactionService;

/**
 * @author Matthieu Chaffotte
 */
@Slf4j
public class SchedulerServiceRestartHandler implements PlatformRestartHandler {

    private final SchedulerService schedulerService;
    private UserTransactionService userTransactionService;

    public SchedulerServiceRestartHandler(SchedulerService schedulerService,
            UserTransactionService userTransactionService) {
        super();
        this.schedulerService = schedulerService;
        this.userTransactionService = userTransactionService;
    }

    @Override
    public void execute() {
        log.info("Rescheduling all scheduler Triggers in ERROR state");
        try {
            userTransactionService.executeInTransaction(() -> {
                schedulerService.rescheduleErroneousTriggers();
                return null;
            });
        } catch (Exception e) {
            log.warn(
                    "Unable to reschedule all erroneous triggers, call PlatformAPI.rescheduleErroneousTriggers to retry. Cause is {}",
                    e.getMessage());
            log.debug("Cause: ", e);
        }
    }

}
