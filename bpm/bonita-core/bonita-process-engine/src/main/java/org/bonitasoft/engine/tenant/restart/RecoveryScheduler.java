/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.tenant.restart;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.tenant.TenantElementsRestartSupervisor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RecoveryScheduler {

    private final TenantElementsRestartSupervisor tenantElementsRestartSupervisor;
    private final RecoveryService recoveryService;

    RecoveryScheduler(TenantElementsRestartSupervisor tenantElementsRestartSupervisor,
            RecoveryService recoveryService) {
        this.tenantElementsRestartSupervisor = tenantElementsRestartSupervisor;
        this.recoveryService = recoveryService;
    }

    @Scheduled(fixedDelayString = "${bonita.tenant.recover.delay_between_recovery:PT30M}", initialDelayString = "${bonita.tenant.recover.delay_before_first_recovery:PT2H}")
    public void triggerRecoveryAllElements() {
        try {
            if (tenantElementsRestartSupervisor.isResponsibleForRecovery()) {
                log.debug("Starting periodic recovery of elements...");
                recoveryService.recoverAllElements();
                log.debug("Completed periodic recovery of elements.");
            } else {
                log.debug("Periodic recovery of elements not executed, an other node is responsible for it.");
            }
        } catch (Exception e) {
            log.warn("Recovery of elements failed because of {} - {},  it will be re executed soon",
                    e.getClass().getName(), e.getMessage());
            log.debug("Cause by ", e);
        }
    }

}
