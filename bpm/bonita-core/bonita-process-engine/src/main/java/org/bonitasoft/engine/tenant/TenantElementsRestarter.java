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
package org.bonitasoft.engine.tenant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles the restart of elements when the tenant is started (strategy is different in cluster)
 */
@Component
@Slf4j
public class TenantElementsRestarter {

    private final TenantRestarter tenantRestarter;
    private final TenantElementsRestartSupervisor tenantElementsRestartSupervisor;

    public TenantElementsRestarter(TenantRestarter tenantRestarter,
            TenantElementsRestartSupervisor tenantElementsRestartSupervisor) {
        this.tenantRestarter = tenantRestarter;
        this.tenantElementsRestartSupervisor = tenantElementsRestartSupervisor;
    }

    void prepareRestartOfElements() throws Exception {
        if (tenantElementsRestartSupervisor.shouldRestartElements()) {
            // Here get all elements that are not "finished"
            // * FlowNodes that have flag: stateExecuting to true: call execute on them (connectors were executing)
            // * Process instances with token count == 0 (either not started again or finishing) -> same thing connectors were executing
            // * transitions that are in state created: call execute on them
            // * flow node that are completed and not deleted : call execute to make it create transitions and so on
            // * all element that are in not stable state
            log.info("Preparing restart of elements");
            tenantRestarter.executeBeforeServicesStart();
        } else {
            log.info("Not preparing restart of elements, as another node already did it");
        }
    }

    // Here get all elements that are not "finished"
    // * FlowNodes that have flag: stateExecuting to true: call execute on them (connectors were executing)
    // * Process instances with token count == 0 (either not started again or finishing) -> same thing connectors were executing
    // * transitions that are in state created: call execute on them
    // * flow node that are completed and not deleted : call execute to make it create transitions and so on
    // * all element that are in not stable state
    void restartElements() throws Exception {
        if (tenantElementsRestartSupervisor.willRestartElements()) {
            log.info("Restarting unfinished elements");
            tenantRestarter.executeAfterServicesStart();
        } else {
            log.info("Not restarting elements of tenant, as another node already did it");
        }
    }
}
