package org.bonitasoft.engine.tenant;

import java.io.IOException;
import java.util.List;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.execution.work.TenantRestartHandler;
import org.bonitasoft.engine.execution.work.TenantRestarter;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Handles the restart of elements when the tenant is started (strategy is different in cluster)
 */
@Component
public class TenantElementsRestarter implements TenantLifecycleService {

    private NodeConfiguration nodeConfiguration;
    private long tenantId;
    //FIXME handle pause/resume activate/deactivate and in cluster
    private boolean areTenantsElementsAlreadyRestarted;

    public TenantElementsRestarter(NodeConfiguration nodeConfiguration, @Value("${tenantId}") long tenantId) {
        this.nodeConfiguration = nodeConfiguration;
        this.tenantId = tenantId;
    }

    void prepareRestartOfElements() throws Exception {
        if (areTenantsElementsAlreadyRestarted) {
            return;
        }
        // Here get all elements that are not "finished"
        // * FlowNodes that have flag: stateExecuting to true: call execute on them (connectors were executing)
        // * Process instances with token count == 0 (either not started again or finishing) -> same thing connectors were executing
        // * transitions that are in state created: call execute on them
        // * flow node that are completed and not deleted : call execute to make it create transitions and so on
        // * all element that are in not stable state
        PlatformServiceAccessor platformAccessor = getPlatformAccessor();
        new TenantRestarter(platformAccessor, platformAccessor.getTenantServiceAccessor(tenantId))
                .executeBeforeServicesStart();
    }

    // Here get all elements that are not "finished"
    // * FlowNodes that have flag: stateExecuting to true: call execute on them (connectors were executing)
    // * Process instances with token count == 0 (either not started again or finishing) -> same thing connectors were executing
    // * transitions that are in state created: call execute on them
    // * flow node that are completed and not deleted : call execute to make it create transitions and so on
    // * all element that are in not stable state
    void restartElements() throws Exception {
        if (areTenantsElementsAlreadyRestarted) {
            return;
        }
        List<TenantRestartHandler> tenantRestartHandlers = nodeConfiguration.getTenantRestartHandlers();
        PlatformServiceAccessor platformAccessor = getPlatformAccessor();
        new TenantRestarter(platformAccessor, platformAccessor.getTenantServiceAccessor(tenantId))
                .executeAfterServicesStart(tenantRestartHandlers);
        areTenantsElementsAlreadyRestarted = true;
    }

    protected PlatformServiceAccessor getPlatformAccessor()
            throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        areTenantsElementsAlreadyRestarted = false;
    }

    @Override
    public void pause() {
        areTenantsElementsAlreadyRestarted = false;
    }

    @Override
    public void resume() {
    }

}
