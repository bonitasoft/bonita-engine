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
package org.bonitasoft.engine.tenant.restart;

import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * The Recovery handler is responsible for recovering all candidate elements
 * at Engine startup.
 * It is called only once in a cluster startup (handled by TenantElementsRestartSupervisor)
 */
@Component
public class RecoveryHandler implements TenantRestartHandler {

    private final RecoveryService recoveryService;
    private List<ElementToRecover> allElementsToRecover;

    public RecoveryHandler(RecoveryService recoveryService) {
        this.recoveryService = recoveryService;
    }

    @Override
    public void beforeServicesStart() {
        allElementsToRecover = recoveryService.getAllElementsToRecover(Duration.ZERO);
    }

    @Override
    public void afterServicesStart() {
        recoveryService.recover(allElementsToRecover);
    }
}
