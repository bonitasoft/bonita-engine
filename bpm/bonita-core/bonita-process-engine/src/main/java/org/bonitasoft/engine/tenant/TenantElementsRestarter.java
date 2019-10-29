package org.bonitasoft.engine.tenant;

import java.util.List;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.tenant.restart.TenantRestartHandler;
import org.springframework.stereotype.Component;

/**
 * Handles the restart of elements when the tenant is started (strategy is different in cluster)
 */
@Component
public class TenantElementsRestarter {

    private List<TenantRestartHandler> tenantRestartHandlers;
    private TenantRestarter tenantRestarter;
    private TenantElementsRestarterSupervisor tenantElementsRestarterSupervisor;

    public TenantElementsRestarter(List<TenantRestartHandler> tenantRestartHandlers,
                                   TenantRestarter tenantRestarter,
                                   TenantElementsRestarterSupervisor tenantElementsRestarterSupervisor) {
        this.tenantRestartHandlers = tenantRestartHandlers;
        this.tenantRestarter = tenantRestarter;
        this.tenantElementsRestarterSupervisor = tenantElementsRestarterSupervisor;
    }

    void prepareRestartOfElements() throws Exception {
        if (!tenantElementsRestarterSupervisor.shouldRestartElements()) {
            return;
        }
        // Here get all elements that are not "finished"
        // * FlowNodes that have flag: stateExecuting to true: call execute on them (connectors were executing)
        // * Process instances with token count == 0 (either not started again or finishing) -> same thing connectors were executing
        // * transitions that are in state created: call execute on them
        // * flow node that are completed and not deleted : call execute to make it create transitions and so on
        // * all element that are in not stable state
        tenantRestarter.executeBeforeServicesStart();
    }

    // Here get all elements that are not "finished"
    // * FlowNodes that have flag: stateExecuting to true: call execute on them (connectors were executing)
    // * Process instances with token count == 0 (either not started again or finishing) -> same thing connectors were executing
    // * transitions that are in state created: call execute on them
    // * flow node that are completed and not deleted : call execute to make it create transitions and so on
    // * all element that are in not stable state
    void restartElements() throws Exception {
        if (!tenantElementsRestarterSupervisor.shouldRestartElements()) {
            return;
        }
        tenantRestarter.executeAfterServicesStart(tenantRestartHandlers);
        tenantElementsRestarterSupervisor.notifyElementsAreRestarted();
    }
}
