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
package org.bonitasoft.engine.bpm.flownode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SPendingActivityMapping;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SGatewayInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SPendingActivityMappingBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SStartEventInstanceBuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class FlowNodeInstanceServiceTest extends CommonBPMServicesTest {

    private final UserTransactionService userTransactionService;

    private final ActivityInstanceService activityInstanceService;

    public FlowNodeInstanceServiceTest() {
        userTransactionService = getTenantAccessor().getUserTransactionService();
        activityInstanceService = getTenantAccessor().getActivityInstanceService();
    }

    private long getNbFlowNodeInstances(final QueryOptions countOptions) throws Exception {
        return userTransactionService.executeInTransaction(new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                return activityInstanceService.getNumberOfFlowNodeInstances(SFlowNodeInstance.class, countOptions);
            }
        });
    }

    @Test
    public void searchFlowNodeInstances() throws Exception {
        final SStartEventInstanceBuilderFactory startEventInstanceBuilder = BuilderFactory.get(SStartEventInstanceBuilderFactory.class);
        final SProcessInstance procInst1 = createSProcessInstance();
        final SProcessInstance procInst2 = createSProcessInstance();

        final OrderByOption oderByOption = new OrderByOption(SFlowNodeInstance.class, startEventInstanceBuilder.getNameKey(), OrderByType.ASC);
        final List<FilterOption> filterOptions = Collections.emptyList();
        final QueryOptions queryOptions = new QueryOptions(0, 10, Collections.singletonList(oderByOption), filterOptions, null);

        // search: no result expected
        List<SFlowNodeInstance> flowNodeInstances = searchFlowNodeInstances(queryOptions);
        assertTrue("There should not be any flownode instance instead of " + flowNodeInstances.size(), flowNodeInstances.isEmpty());

        // create flow nodes
        createFlowNodeInstances(procInst1, procInst2);

        // search: created flow nodes must be retrieved
        flowNodeInstances = searchFlowNodeInstances(queryOptions);
        assertEquals(10, flowNodeInstances.size());

        // delete process instances
        deleteSProcessInstance(procInst1);
        deleteSProcessInstance(procInst2);

        flowNodeInstances = searchFlowNodeInstances(queryOptions);
        assertEquals(0, flowNodeInstances.size());
    }

    @Test
    public void searchFlowNodeInstancesWithFilter() throws Exception {
        final SStartEventInstanceBuilderFactory startEventInstanceBuilder = BuilderFactory.get(SStartEventInstanceBuilderFactory.class);
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
        createFlowNodeInstances(procInst1, procInst2);

        // search: created flow nodes must be retrieved
        flowNodeInstances = searchFlowNodeInstances(queryOptions);
        nbFlowNodeInstances = getNbFlowNodeInstances(countOptions);
        assertEquals(7, flowNodeInstances.size());
        assertEquals(7, nbFlowNodeInstances);

        deleteSProcessInstance(procInst1);
        deleteSProcessInstance(procInst2);

    }

    private void createFlowNodeInstances(final SProcessInstance procInst1, final SProcessInstance procInst2) throws SBonitaException {
        // add flow nodes to procInst 1
        createSStartEventInstance("startEvent", 1, procInst1.getId(), 5, procInst1.getId());
        createSIntermediateCatchEventInstance("intermediateCatchEvent", 2, procInst1.getId(), 5, procInst1.getId());
        createSIntermediateThrowEventInstance("intermediateThrowEvent", 3, procInst1.getId(), 5, procInst1.getId());
        createSEndEventInstance("endEvent", 4, procInst1.getId(), 5, procInst1.getId());

        final SGatewayInstance gatewayInstance = BuilderFactory.get(SGatewayInstanceBuilderFactory.class)
                .createNewInstance("Gateway1", 5, procInst1.getId(), procInst1.getId(), SGatewayType.EXCLUSIVE, 2, procInst1.getId(), procInst1.getId())
                .setStateId(1).setHitBys("a,b,c").done();
        insertGatewayInstance(gatewayInstance);

        createSUserTaskInstance("userTask", 6, procInst1.getId(), 5, procInst1.getId(), 10);
        createSAutomaticTaskInstance("autoTask", 7, procInst1.getId(), 5, procInst1.getId());

        // add flow nodes to procInst 2
        createSStartEventInstance("startEvent", 8, procInst2.getId(), 5, procInst2.getId());
        createSAutomaticTaskInstance("autoTask", 9, procInst2.getId(), 5, procInst2.getId());
        createSEndEventInstance("endEvent", 10, procInst2.getId(), 5, procInst2.getId());
    }

    @Test
    public void isTaskPendingForUser() throws Exception {
        long flowNodeDefinitionId = 12355467l;
        long processDefinitionId = 123445566l;
        long rootProcessInstanceID = 7754l;
        long actorId = 5589l;
        SUserTaskInstance step1 = createSUserTaskInstance("step1", flowNodeDefinitionId, -1, processDefinitionId, rootProcessInstanceID, actorId);
        long userId = 4411l;
        //given
        getTransactionService().begin();
        final SPendingActivityMapping mapping = BuilderFactory.get(SPendingActivityMappingBuilderFactory.class)
                .createNewInstanceForUser(step1.getId(), userId).done();
        activityInstanceService.addPendingActivityMappings(mapping);
        getTransactionService().complete();
        //
        //when
        getTransactionService().begin();
        boolean taskPendingForUser = activityInstanceService.isTaskPendingForUser(step1.getId(), userId);
        getTransactionService().complete();
        //then
        assertTrue("task should be pending",taskPendingForUser);

    }


}
