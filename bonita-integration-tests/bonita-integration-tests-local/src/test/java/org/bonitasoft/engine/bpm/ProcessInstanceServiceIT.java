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
package org.bonitasoft.engine.bpm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.CallableWithException;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SCallActivityInstanceBuilderFactory;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilder;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilderFactory;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceNotFoundException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.SDataInstanceBuilder;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.transaction.STransactionCreationException;
import org.bonitasoft.engine.transaction.STransactionRollbackException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Emmanuel Duchastenier
 * @author Yanyan Liu
 */
public class ProcessInstanceServiceIT extends CommonBPMServicesTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(CommonBPMServicesTest.class);

    private TransactionService transactionService;
    private ProcessInstanceService processInstanceService;
    private ActivityInstanceService activityInstanceService;

    @Before
    public void setup() {
        transactionService = getTransactionService();
        processInstanceService = getTenantAccessor().getProcessInstanceService();
        activityInstanceService = getTenantAccessor().getActivityInstanceService();
    }

    /**
     * Clean up of all existing process instances
     */
    private void cleanUpAllProcessInstances() {
        try {
            List<SProcessInstance> processInstances = inTx(() -> getFirstProcessInstances(1000));

            inTx(() -> {
                for (final SProcessInstance sProcessInstance : processInstances) {
                    processInstanceService.deleteProcessInstance(sProcessInstance);
                }
                return null;
            });

            List<SAProcessInstance> archives = inTx(() -> getFirstArchivedProcessInstances(1000));

            inTx(() -> {
                for (final SAProcessInstance saProcessInstance : archives) {
                    processInstanceService.deleteArchivedProcessInstance(saProcessInstance);
                }
                return null;
            });

        } catch (final Exception e) {
            LOGGER.error("Error during clean-up. Ignoring...");
        }
    }

    private <T> T inTx(CallableWithException<T> callable) throws Exception {
        transactionService.begin();
        final T result = callable.call();
        transactionService.complete();
        return result;
    }

    @Test
    public void getNumberOfProcessInstances()
            throws STransactionCreationException, STransactionCommitException, SProcessInstanceCreationException,
            STransactionRollbackException, SBonitaReadException {
        transactionService.begin();
        final long processDefinitionId = 123L;
        SProcessInstance sProcessInstance = SProcessInstance.builder()
                .name("an instance name")
                .processDefinitionId(processDefinitionId).build();
        processInstanceService.createProcessInstance(sProcessInstance);
        final QueryOptions queryOptions = new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS);
        final long processInstanceNumber = processInstanceService.getNumberOfProcessInstances(queryOptions);
        transactionService.complete();

        // first test with one process:
        assertEquals(1, processInstanceNumber);

        transactionService.begin();

        // second test with 100 processes:
        for (int i = 0; i < 100; i++) {
            sProcessInstance = SProcessInstance.builder()
                    .name("process instance " + i)
                    .processDefinitionId(processDefinitionId).build();
            processInstanceService.createProcessInstance(sProcessInstance);
        }
        final long numberOfProcessInstances = processInstanceService.getNumberOfProcessInstances(queryOptions);
        transactionService.complete();
        assertEquals(101, numberOfProcessInstances);

        // clean up:
        cleanUpAllProcessInstances();
    }

    @Test
    public void getCorrectProcessInstancesOrder() throws Exception {
        // Creation of the process instances we want to retrieve:
        transactionService.begin();
        final long processDefinitionId = 123L;
        final SProcessInstance sProcessInstance0 = SProcessInstance.builder()
                .name("instance name 0")
                .processDefinitionId(processDefinitionId).build();
        final SProcessInstance sProcessInstance1 = SProcessInstance.builder()
                .name("instance name 1")
                .processDefinitionId(processDefinitionId).build();
        final SProcessInstance sProcessInstance2 = SProcessInstance.builder()
                .name("instance name 2")
                .processDefinitionId(processDefinitionId).build();
        processInstanceService.createProcessInstance(sProcessInstance0);
        processInstanceService.setState(sProcessInstance0, ProcessInstanceState.STARTED);
        // to ensure the date is not exactly the same as the previous one:
        Thread.sleep(5);
        processInstanceService.createProcessInstance(sProcessInstance1);
        processInstanceService.setState(sProcessInstance1, ProcessInstanceState.STARTED);
        // to ensure the date is not exactly the same as the previous one:
        Thread.sleep(5);
        processInstanceService.createProcessInstance(sProcessInstance2);
        processInstanceService.setState(sProcessInstance2, ProcessInstanceState.STARTED);
        transactionService.complete();

        // Retrieval of the previously created process instances:
        transactionService.begin();
        final List<SProcessInstance> processInstances = getFirstProcessInstances(20);
        transactionService.complete();

        // Verification of the number of process instances retrieved:
        assertEquals(3, processInstances.size());
        // Verification of the order:
        assertEquals(sProcessInstance0.getId(), processInstances.get(2).getId());
        assertEquals(sProcessInstance1.getId(), processInstances.get(1).getId());
        assertEquals(sProcessInstance2.getId(), processInstances.get(0).getId());

        // clean up:
        cleanUpAllProcessInstances();
    }

    @Test
    public void testSetState() {
        // TODO: not yet implemented
    }

    @Test
    public void testDeleteProcessInstance() throws SBonitaException {
        // Creation of a process instance:
        transactionService.begin();
        final long processDefinitionId = 123L;
        final SProcessInstance sProcessInstance = SProcessInstance.builder()
                .name("an instance name")
                .processDefinitionId(processDefinitionId).build();
        processInstanceService.createProcessInstance(sProcessInstance);
        transactionService.complete();

        // clean up:
        cleanUpAllProcessInstances();

        // retrieve the number of process instances:
        transactionService.begin();
        final QueryOptions queryOptions = new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS);
        final long processInstanceNumber = processInstanceService.getNumberOfProcessInstances(queryOptions);
        transactionService.complete();

        // Check that this number is 0:
        assertEquals(0, processInstanceNumber);
    }

    @Test
    public void testGetChildInstanceIdsOfProcessInstance() throws Exception {
        // first create parent process instance
        transactionService.begin();
        final SCallActivityInstanceBuilderFactory sCallActivityInstanceBuilder = BuilderFactory
                .get(SCallActivityInstanceBuilderFactory.class);
        final long processDefinitionId = 123L;
        final SProcessInstance parentProcessInstance = SProcessInstance.builder()
                .name("an instance name")
                .processDefinitionId(processDefinitionId).build();
        processInstanceService.createProcessInstance(parentProcessInstance);
        transactionService.complete();

        transactionService.begin();
        // second create 10 child processes:
        SActivityInstance activityInstance = sCallActivityInstanceBuilder
                .createNewCallActivityInstance("callActivity", 1,
                        parentProcessInstance.getContainerId(),
                        parentProcessInstance.getContainerId(), processDefinitionId, parentProcessInstance.getId(),
                        parentProcessInstance.getId())
                .done();
        activityInstanceService.createActivityInstance(activityInstance);
        final List<Long> childInstanceIds = new ArrayList<>();
        SProcessInstance childProcessInstance;
        for (int i = 0; i < 10; i++) {
            childProcessInstance = SProcessInstance.builder()
                    .name("child process instance " + i)
                    .processDefinitionId(processDefinitionId)
                    .containerId(parentProcessInstance.getId()).callerId(activityInstance.getId())
                    .callerType(SFlowNodeType.CALL_ACTIVITY).build();
            processInstanceService.createProcessInstance(childProcessInstance);
            childInstanceIds.add(childProcessInstance.getId());
        }
        transactionService.complete();
        // test get child by paging, order by name ASC
        final String nameField = SProcessInstance.NAME_KEY;
        transactionService.begin();
        final List<Long> childInstanceIdList1 = processInstanceService.getChildInstanceIdsOfProcessInstance(
                parentProcessInstance.getId(), 0, 4, nameField,
                OrderByType.ASC);
        assertEquals(4, childInstanceIdList1.size());
        assertEquals(childInstanceIds.get(0), childInstanceIdList1.get(0));
        assertEquals(childInstanceIds.get(1), childInstanceIdList1.get(1));
        assertEquals(childInstanceIds.get(2), childInstanceIdList1.get(2));
        assertEquals(childInstanceIds.get(3), childInstanceIdList1.get(3));

        final List<Long> childInstanceIdList2 = processInstanceService.getChildInstanceIdsOfProcessInstance(
                parentProcessInstance.getId(), 4, 4, nameField,
                OrderByType.ASC);
        assertEquals(4, childInstanceIdList2.size());
        assertEquals(childInstanceIds.get(4), childInstanceIdList2.get(0));
        assertEquals(childInstanceIds.get(5), childInstanceIdList2.get(1));
        assertEquals(childInstanceIds.get(6), childInstanceIdList2.get(2));
        assertEquals(childInstanceIds.get(7), childInstanceIdList2.get(3));

        final List<Long> childInstanceIdList3 = processInstanceService.getChildInstanceIdsOfProcessInstance(
                parentProcessInstance.getId(), 8, 4, nameField,
                OrderByType.ASC);
        assertEquals(2, childInstanceIdList3.size());
        assertEquals(childInstanceIds.get(8), childInstanceIdList3.get(0));
        assertEquals(childInstanceIds.get(9), childInstanceIdList3.get(1));

        // test DESC
        final List<Long> childInstanceIdList4 = processInstanceService.getChildInstanceIdsOfProcessInstance(
                parentProcessInstance.getId(), 0, 4, nameField,
                OrderByType.DESC);
        assertEquals(4, childInstanceIdList4.size());
        assertEquals(childInstanceIds.get(9), childInstanceIdList4.get(0));
        assertEquals(childInstanceIds.get(8), childInstanceIdList4.get(1));
        assertEquals(childInstanceIds.get(7), childInstanceIdList4.get(2));
        assertEquals(childInstanceIds.get(6), childInstanceIdList4.get(3));

        activityInstance = activityInstanceService.getActivityInstance(activityInstance.getId());
        activityInstanceService.deleteFlowNodeInstance(activityInstance);

        transactionService.complete();

        // clean up:
        cleanUpAllProcessInstances();

    }

    @Test
    public void testGetNumberOfChildInstancesOfProcessInstance() throws Exception {
        // first create parent process instance and test
        transactionService.begin();
        final SCallActivityInstanceBuilderFactory sCallActivityInstanceBuilder = BuilderFactory
                .get(SCallActivityInstanceBuilderFactory.class);
        final long processDefinitionId = 123L;
        final SProcessInstance parentProcessInstance = SProcessInstance.builder()
                .name("an instance name")
                .processDefinitionId(processDefinitionId).build();
        processInstanceService.createProcessInstance(parentProcessInstance);
        long numberOfChild = processInstanceService
                .getNumberOfChildInstancesOfProcessInstance(parentProcessInstance.getId());
        transactionService.complete();
        assertEquals(0, numberOfChild);

        transactionService.begin();
        // second create 10 child processes:
        final List<Long> childInstanceIds = new ArrayList<>();
        SProcessInstance childProcessInstance;
        SActivityInstance activityInstance = sCallActivityInstanceBuilder
                .createNewCallActivityInstance("callActivity", 1,
                        parentProcessInstance.getContainerId(),
                        parentProcessInstance.getContainerId(), processDefinitionId, parentProcessInstance.getId(),
                        parentProcessInstance.getId())
                .done();
        activityInstanceService.createActivityInstance(activityInstance);
        for (int i = 0; i < 10; i++) {
            childProcessInstance = SProcessInstance.builder()
                    .name("child process instance " + i)
                    .processDefinitionId(processDefinitionId)
                    .containerId(parentProcessInstance.getId()).callerId(activityInstance.getId())
                    .callerType(SFlowNodeType.CALL_ACTIVITY).build();
            processInstanceService.createProcessInstance(childProcessInstance);
            childInstanceIds.add(childProcessInstance.getId());
        }
        numberOfChild = processInstanceService
                .getNumberOfChildInstancesOfProcessInstance(parentProcessInstance.getId());
        activityInstanceService.deleteFlowNodeInstance(activityInstance);
        transactionService.complete();
        assertEquals(childInstanceIds.size(), numberOfChild);

        // clean up:
        cleanUpAllProcessInstances();
    }

    @Test
    public void testDeletProcessInstanceAlsoDeleteDataInstances() throws Exception {
        final long processDefinitionId = 123123123L;
        final String processName = "myProcInst";

        // create a process instance
        final SProcessInstance processInstance = createProcessInstanceInTransaction(processDefinitionId, processName);

        // create a data instance having the process instance as container
        final SDataInstance globalDataInstance = createDataInTransaction("myData", String.class.getName(),
                processInstance.getId(),
                DataInstanceContainer.PROCESS_INSTANCE);

        // create a automatic task
        final SActivityInstance taskInstance = createSAutomaticTaskInstance(
                "auto", 1234L, processInstance.getId(), processDefinitionId, processInstance.getId());
        final SDataInstance localDataInstance = createDataInTransaction("myLocalData", String.class.getName(),
                taskInstance.getId(),
                DataInstanceContainer.ACTIVITY_INSTANCE);

        // delete the process instance: the data instance is supposed to be deleted at same time
        deleteSProcessInstance(processInstance);

        // check that no more data is available for the deleted process instance and flow node instance
        checkDataDoesNotExist(globalDataInstance);
        checkDataDoesNotExist(localDataInstance);
        checkFlowNodeDoesNotExist(taskInstance);

    }

    private void checkDataDoesNotExist(final SDataInstance dataInstance) throws SBonitaException {
        try {
            getDataInstanceInTransaction(dataInstance.getId());
            fail("the data instance was not deleted");
        } catch (final SDataInstanceNotFoundException e) {
            // ok
        }
    }

    private void checkFlowNodeDoesNotExist(final SFlowNodeInstance flowNodeInstance) throws SBonitaException {
        try {
            getFlowNodeInstance(flowNodeInstance.getId());
            fail("the flowNode instance was not deleted");
        } catch (final SFlowNodeNotFoundException e) {
            // ok
        }
    }

    private SProcessInstance createProcessInstanceInTransaction(final long process_definition_id,
            final String processName)
            throws STransactionCreationException, SProcessInstanceCreationException, STransactionCommitException,
            STransactionRollbackException {
        getTransactionService().begin();
        // Creation of a process instance:
        final SProcessInstance processInstance = SProcessInstance.builder()
                .name(processName)
                .processDefinitionId(process_definition_id).build();
        processInstanceService.createProcessInstance(processInstance);
        getTransactionService().complete();
        return processInstance;
    }

    private SDataInstance createDataInTransaction(final String dataName, final String dataType, final long containerId,
            final DataInstanceContainer containerType) throws SBonitaException {
        getTransactionService().begin();
        final SDataDefinitionBuilder dataDefBuilder = BuilderFactory.get(SDataDefinitionBuilderFactory.class)
                .createNewInstance(dataName, dataType);

        final SDataInstanceBuilder dataInstanceBuilder = SDataInstanceBuilder.createNewInstance(dataDefBuilder.done());
        dataInstanceBuilder.setContainerId(containerId);
        dataInstanceBuilder.setContainerType(containerType.name());

        final SDataInstance dataInstance = dataInstanceBuilder.done();
        getTenantAccessor().getDataInstanceService().createDataInstance(dataInstance);

        getTransactionService().complete();
        return dataInstance;
    }

    private SDataInstance getDataInstanceInTransaction(final long dataInstanceId) throws SBonitaException {
        SDataInstance dataInstance;
        getTransactionService().begin();
        try {
            dataInstance = getTenantAccessor().getDataInstanceService().getDataInstance(dataInstanceId);
        } finally {
            getTransactionService().complete();
        }
        return dataInstance;
    }

}
