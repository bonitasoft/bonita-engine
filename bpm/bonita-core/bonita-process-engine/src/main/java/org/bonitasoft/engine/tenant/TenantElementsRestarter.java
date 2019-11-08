package org.bonitasoft.engine.tenant;

import java.util.List;

import org.bonitasoft.engine.tenant.restart.TenantRestartHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles the restart of elements when the tenant is started (strategy is different in cluster)
 */
@Component
@Slf4j
public class TenantElementsRestarter {

    private List<TenantRestartHandler> tenantRestartHandlers;
    private TenantRestarter tenantRestarter;
    private TenantElementsRestartSupervisor tenantElementsRestartSupervisor;
    private long tenantId;

    public TenantElementsRestarter(List<TenantRestartHandler> tenantRestartHandlers, TenantRestarter tenantRestarter,
                                   TenantElementsRestartSupervisor tenantElementsRestartSupervisor, @Value("${tenantId}") long tenantId) {
        this.tenantRestartHandlers = tenantRestartHandlers;
        this.tenantRestarter = tenantRestarter;
        this.tenantElementsRestartSupervisor = tenantElementsRestartSupervisor;
        this.tenantId = tenantId;
    }

    void prepareRestartOfElements() throws Exception {
        if (tenantElementsRestartSupervisor.shouldRestartElements()) {
            // Here get all elements that are not "finished"
            // * FlowNodes that have flag: stateExecuting to true: call execute on them (connectors were executing)
            // * Process instances with token count == 0 (either not started again or finishing) -> same thing connectors were executing
            // * transitions that are in state created: call execute on them
            // * flow node that are completed and not deleted : call execute to make it create transitions and so on
            // * all element that are in not stable state
            log.info("Preparing restart of elements of tenant {}", tenantId);
            tenantRestarter.executeBeforeServicesStart();
        } else {
            log.info("Not preparing restart of elements of tenant {}, as another node already did it", tenantId);
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
            log.info("Restarting unfinished elements of tenant {}", tenantId);
            tenantRestarter.executeAfterServicesStart(tenantRestartHandlers);
        } else {
            log.info("Not restarting elements of tenant {}, as another node already did it", tenantId);
        }
    }
}
