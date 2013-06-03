package org.bonitasoft.engine.bpm.flownode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.BPMServicesBuilder;
import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SStartEventInstanceBuilder;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class FlowNodeInstanceServiceTest extends CommonBPMServicesTest {

    private final TransactionService transactionService;

    private final ActivityInstanceService activityInstanceService;

    private final BPMInstanceBuilders bpmInstanceBuilders;

    private final BPMServicesBuilder servicesBuilder;

    public FlowNodeInstanceServiceTest() {
        servicesBuilder = getServicesBuilder();
        transactionService = servicesBuilder.getTransactionService();
        bpmInstanceBuilders = servicesBuilder.getBPMInstanceBuilders();
        activityInstanceService = servicesBuilder.getActivityInstanceService();
    }

    private long getNbFlowNodeInstances(final QueryOptions countOptions) throws SBonitaException {
        transactionService.begin();
        final long nbOfFlowNodeInst = activityInstanceService.getNumberOfFlowNodeInstances(SFlowNodeInstance.class, countOptions);
        transactionService.complete();

        return nbOfFlowNodeInst;
    }

    @Test
    public void testSearchFlowNodeInstances() throws Exception {
        final SStartEventInstanceBuilder startEventInstanceBuilder = bpmInstanceBuilders.getSStartEventInstanceBuilder();
        final SProcessInstance procInst1 = createSProcessInstance();
        final SProcessInstance procInst2 = createSProcessInstance();

        final OrderByOption oderByOption = new OrderByOption(SFlowNodeInstance.class, startEventInstanceBuilder.getNameKey(), OrderByType.ASC);
        final List<FilterOption> filterOptions = Collections.emptyList();
        final QueryOptions queryOptions = new QueryOptions(0, 10, Collections.singletonList(oderByOption), filterOptions, null);
        final QueryOptions countOptions = new QueryOptions(0, 10);

        // search: no result expected
        List<SFlowNodeInstance> flowNodeInstances = searchFlowNodeInstances(queryOptions);
        long nbFlowNodeInstances = getNbFlowNodeInstances(countOptions);
        assertTrue(flowNodeInstances.isEmpty());
        assertEquals(0, nbFlowNodeInstances);

        // create flow nodes
        createFlowNodeInstances(startEventInstanceBuilder, procInst1, procInst2);

        // search: created flow nodes must be retrieved
        flowNodeInstances = searchFlowNodeInstances(queryOptions);
        nbFlowNodeInstances = getNbFlowNodeInstances(countOptions);
        assertEquals(10, flowNodeInstances.size());
        assertEquals(10, nbFlowNodeInstances);

        // delete process instances
        deleteSProcessInstance(procInst1);
        deleteSProcessInstance(procInst2);

        flowNodeInstances = searchFlowNodeInstances(queryOptions);
        nbFlowNodeInstances = getNbFlowNodeInstances(countOptions);
        assertEquals(0, flowNodeInstances.size());
        assertEquals(0, nbFlowNodeInstances);
    }

    @Test
    public void testSearchFlowNodeInstancesWithFilter() throws Exception {
        final SStartEventInstanceBuilder startEventInstanceBuilder = bpmInstanceBuilders.getSStartEventInstanceBuilder();
        final SProcessInstance procInst1 = createSProcessInstance();
        final SProcessInstance procInst2 = createSProcessInstance();

        final OrderByOption oderByOption = new OrderByOption(SFlowNodeInstance.class, startEventInstanceBuilder.getNameKey(), OrderByType.ASC);
        final FilterOption filterOption = new FilterOption(SFlowNodeInstance.class, startEventInstanceBuilder.getParentProcessInstanceKey(), procInst1.getId());
        final QueryOptions queryOptions = new QueryOptions(0, 10, Collections.singletonList(oderByOption), Collections.singletonList(filterOption), null);
        final QueryOptions countOptions = new QueryOptions(0, 10, null, Collections.singletonList(filterOption), null);

        // search: no result expected
        List<SFlowNodeInstance> flowNodeInstances = searchFlowNodeInstances(queryOptions);
        long nbFlowNodeInstances = getNbFlowNodeInstances(countOptions);
        assertTrue(flowNodeInstances.isEmpty());
        assertEquals(0, nbFlowNodeInstances);

        // create flow nodes
        createFlowNodeInstances(startEventInstanceBuilder, procInst1, procInst2);

        // search: created flow nodes must be retrieved
        flowNodeInstances = searchFlowNodeInstances(queryOptions);
        nbFlowNodeInstances = getNbFlowNodeInstances(countOptions);
        assertEquals(7, flowNodeInstances.size());
        assertEquals(7, nbFlowNodeInstances);

        deleteSProcessInstance(procInst1);
        deleteSProcessInstance(procInst2);

    }

    private void createFlowNodeInstances(final SStartEventInstanceBuilder startEventInstanceBuilder, final SProcessInstance procInst1,
            final SProcessInstance procInst2) throws SBonitaException {
        // add flow nodes to procInst 1
        createSStartEventInstance(startEventInstanceBuilder, "startEvent", 1, procInst1.getId(), 5, procInst1.getId());
        createSIntermediateCatchEventInstance(bpmInstanceBuilders.getSIntermediateCatchEventInstanceBuilder(), "intermediateCatchEvent", 2, procInst1.getId(),
                5, procInst1.getId());
        createSIntermediateThrowEventInstance(bpmInstanceBuilders.getSIntermediateThrowEventInstanceBuilder(), "intermediateThrowEvent", 3, procInst1.getId(),
                5, procInst1.getId());
        createSEndEventInstance(bpmInstanceBuilders.getSEndEventInstanceBuilder(), "endEvent", 4, procInst1.getId(), 5, procInst1.getId());

        final SGatewayInstance gatewayInstance = bpmInstanceBuilders.getSGatewayInstanceBuilder()
                .createNewInstance("Gateway1", 5, procInst1.getId(), procInst1.getId(), SGatewayType.EXCLUSIVE, 2, procInst1.getId(), procInst1.getId())
                .setStateId(1).setHitBys("a,b,c").done();
        insertGatewayInstance(gatewayInstance);

        createSUserTaskInstance(bpmInstanceBuilders.getUserTaskInstanceBuilder(), "userTask", 6, procInst1.getId(), 5, procInst1.getId(), 10);
        createSAutomaticTaskInstance(bpmInstanceBuilders.getSAutomaticTaskInstanceBuilder(), "autoTask", 7, procInst1.getId(), 5, procInst1.getId());

        // add flow nodes to procInst 2
        createSStartEventInstance(startEventInstanceBuilder, "startEvent", 8, procInst2.getId(), 5, procInst2.getId());
        createSAutomaticTaskInstance(bpmInstanceBuilders.getSAutomaticTaskInstanceBuilder(), "autoTask", 9, procInst2.getId(), 5, procInst2.getId());
        createSEndEventInstance(bpmInstanceBuilders.getSEndEventInstanceBuilder(), "endEvent", 10, procInst2.getId(), 5, procInst2.getId());
    }

}
