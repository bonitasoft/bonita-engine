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
package org.bonitasoft.engine.bpm.flownode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.ToIntFunction;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SPendingActivityMapping;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SGatewayInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SStartEventInstanceBuilderFactory;
import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class FlowNodeInstanceServiceIT extends CommonBPMServicesTest {

    private UserTransactionService userTransactionService;

    private ActivityInstanceService activityInstanceService;

    @Before
    public void setup() {
        userTransactionService = getServiceAccessor().getUserTransactionService();
        activityInstanceService = getServiceAccessor().getActivityInstanceService();
    }

    private long getNbFlowNodeInstances(final QueryOptions countOptions) throws Exception {
        return userTransactionService.executeInTransaction(
                () -> activityInstanceService.getNumberOfFlowNodeInstances(SFlowNodeInstance.class, countOptions));
    }

    @Test
    public void searchFlowNodeInstances() throws Exception {
        final SStartEventInstanceBuilderFactory startEventInstanceBuilder = BuilderFactory
                .get(SStartEventInstanceBuilderFactory.class);
        final SProcessInstance procInst1 = createSProcessInstance();
        final SProcessInstance procInst2 = createSProcessInstance();

        final OrderByOption oderByOption = new OrderByOption(SFlowNodeInstance.class,
                startEventInstanceBuilder.getNameKey(), OrderByType.ASC);
        final List<FilterOption> filterOptions = Collections.emptyList();
        final QueryOptions queryOptions = new QueryOptions(0, 10, Collections.singletonList(oderByOption),
                filterOptions, null);

        // search: no result expected
        List<SFlowNodeInstance> flowNodeInstances = searchFlowNodeInstances(queryOptions);
        assertTrue("There should not be any flownode instance instead of " + flowNodeInstances.size(),
                flowNodeInstances.isEmpty());

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
        final SStartEventInstanceBuilderFactory startEventInstanceBuilder = BuilderFactory
                .get(SStartEventInstanceBuilderFactory.class);
        final SProcessInstance procInst1 = createSProcessInstance();
        final SProcessInstance procInst2 = createSProcessInstance();

        final OrderByOption oderByOption = new OrderByOption(SFlowNodeInstance.class,
                startEventInstanceBuilder.getNameKey(), OrderByType.ASC);
        final FilterOption filterOption = new FilterOption(SFlowNodeInstance.class,
                startEventInstanceBuilder.getParentProcessInstanceKey(), procInst1.getId());
        final QueryOptions queryOptions = new QueryOptions(0, 10, Collections.singletonList(oderByOption),
                Collections.singletonList(filterOption), null);
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

    private void createFlowNodeInstances(final SProcessInstance procInst1, final SProcessInstance procInst2)
            throws SBonitaException {
        // add flow nodes to procInst 1
        createSStartEventInstance("startEvent", 1, procInst1.getId(), 5, procInst1.getId());
        createSIntermediateCatchEventInstance("intermediateCatchEvent", 2, procInst1.getId(), 5, procInst1.getId());
        createSIntermediateThrowEventInstance("intermediateThrowEvent", 3, procInst1.getId(), 5, procInst1.getId());
        createSEndEventInstance("endEvent", 4, procInst1.getId(), 5, procInst1.getId());

        final SGatewayInstance gatewayInstance = BuilderFactory.get(SGatewayInstanceBuilderFactory.class)
                .createNewInstance("Gateway1", 5, procInst1.getId(), procInst1.getId(), SGatewayType.EXCLUSIVE, 2,
                        procInst1.getId(), procInst1.getId())
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
        long flowNodeDefinitionId = 12355467L;
        long processDefinitionId = 123445566L;
        long rootProcessInstanceID = 7754L;
        long actorId = 5589L;
        SUserTaskInstance step1 = createSUserTaskInstance("step1", flowNodeDefinitionId, -1, processDefinitionId,
                rootProcessInstanceID, actorId);
        long userId = 4411L;
        //given
        getTransactionService().begin();
        final SPendingActivityMapping mapping = SPendingActivityMapping.builder().activityId(step1.getId())
                .userId(userId).build();
        activityInstanceService.addPendingActivityMappings(mapping);
        getTransactionService().complete();
        //
        //when
        getTransactionService().begin();
        boolean taskPendingForUser = activityInstanceService.isTaskPendingForUser(step1.getId(), userId);
        getTransactionService().complete();
        //then
        assertTrue("task should be pending", taskPendingForUser);

        // clean-up:
        getTransactionService().begin();
        activityInstanceService.deleteAllPendingMappings();
        activityInstanceService.deleteFlowNodeInstance(step1);
        getTransactionService().complete();
    }

    @Test
    public void lastUpdateDate_should_be_persisted_when_modifying_flowNode() throws Exception {
        // given: create a user task
        long flowNodeDefinitionId = 12355467L;
        long processDefinitionId = 123445566L;
        long rootProcessInstanceID = 7754L;
        long actorId = 5589L;
        SUserTaskInstance task = createSUserTaskInstance("taskWithLastUpdateDate", flowNodeDefinitionId, -1,
                processDefinitionId, rootProcessInstanceID, actorId);

        // Verify initial lastUpdateDate is set during creation
        getTransactionService().begin();
        SFlowNodeInstance initialTask = activityInstanceService.getFlowNodeInstance(task.getId());
        long initialLastUpdateDate = initialTask.getLastUpdateDate();
        getTransactionService().complete();

        assertThat(initialLastUpdateDate).as("Initial lastUpdateDate should be > 0").isGreaterThan(0);

        // Wait a bit to ensure timestamp difference
        Thread.sleep(10);

        // when: modify the flow node using setExecuting
        getTransactionService().begin();
        SFlowNodeInstance taskToModify = activityInstanceService.getFlowNodeInstance(task.getId());
        activityInstanceService.setExecuting(taskToModify);
        getTransactionService().complete();

        // then: re-fetch from database and verify lastUpdateDate was updated
        getTransactionService().begin();
        SFlowNodeInstance updatedTask = activityInstanceService.getFlowNodeInstance(task.getId());
        long updatedLastUpdateDate = updatedTask.getLastUpdateDate();
        getTransactionService().complete();

        assertThat(updatedLastUpdateDate).as("Updated lastUpdateDate should be > 0").isGreaterThan(0);
        assertThat(updatedLastUpdateDate).as("lastUpdateDate should be updated after modification")
                .isGreaterThanOrEqualTo(initialLastUpdateDate);

        // clean-up:
        getTransactionService().begin();
        activityInstanceService.deleteFlowNodeInstance(updatedTask);
        getTransactionService().complete();
    }

    @Test
    public void lastUpdateDate_should_be_persisted_when_assigning_humanTask_with_strict_query() throws Exception {
        // given: create a user task
        long flowNodeDefinitionId = 12355467L;
        long processDefinitionId = 123445566L;
        long rootProcessInstanceID = 7754L;
        long actorId = 5589L;
        SUserTaskInstance task = createSUserTaskInstance("taskForAssignment", flowNodeDefinitionId, -1,
                processDefinitionId, rootProcessInstanceID, actorId);

        // Verify initial lastUpdateDate is set during creation
        getTransactionService().begin();
        SFlowNodeInstance initialTask = activityInstanceService.getFlowNodeInstance(task.getId());
        long initialLastUpdateDate = initialTask.getLastUpdateDate();
        getTransactionService().complete();

        assertThat(initialLastUpdateDate).as("Initial lastUpdateDate should be > 0").isGreaterThan(0);

        // Wait a bit to ensure timestamp difference
        Thread.sleep(10);

        // when: assign the task using assignHumanTaskIfNotAssigned (uses updateStrictHuman query)
        long userId = 12345L;
        getTransactionService().begin();
        activityInstanceService.assignHumanTaskIfNotAssigned(task.getId(), userId);
        getTransactionService().complete();

        // then: re-fetch from database and verify lastUpdateDate was updated
        getTransactionService().begin();
        SFlowNodeInstance assignedTask = activityInstanceService.getFlowNodeInstance(task.getId());
        long assignedLastUpdateDate = assignedTask.getLastUpdateDate();
        long assigneeId = ((SUserTaskInstance) assignedTask).getAssigneeId();
        getTransactionService().complete();

        assertThat(assigneeId).as("Assignee should be set").isEqualTo(userId);
        assertThat(assignedLastUpdateDate).as("Assigned lastUpdateDate should be > 0").isGreaterThan(0);
        assertThat(assignedLastUpdateDate).as("lastUpdateDate should be updated after assignment")
                .isGreaterThanOrEqualTo(initialLastUpdateDate);

        // clean-up:
        getTransactionService().begin();
        activityInstanceService.deleteFlowNodeInstance(assignedTask);
        getTransactionService().complete();
    }

    // Thread-safety tests for the multi-instance counter updates. Verify that atomic SQL
    // (SET counter = counter + :n) prevents lost updates both without the process instance
    // lock and with it (the production Lock → Tx → Work pattern).

    @Test
    public void concurrent_addMultiInstanceNumberOfCompletedActivities_without_lock_should_not_lose_updates()
            throws Exception {
        runConcurrentMiCounterTest(
                "miActivity", /* useLock */ false,
                (svc, mi) -> svc.addMultiInstanceNumberOfCompletedActivities(mi, 1),
                SMultiInstanceActivityInstance::getNumberOfCompletedInstances,
                "completions");
    }

    @Test
    public void concurrent_addMultiInstanceNumberOfTerminatedActivities_without_lock_should_not_lose_updates()
            throws Exception {
        runConcurrentMiCounterTest(
                "miActivityTerminated", /* useLock */ false,
                (svc, mi) -> svc.addMultiInstanceNumberOfTerminatedActivities(mi, 1),
                SMultiInstanceActivityInstance::getNumberOfTerminatedInstances,
                "terminations");
    }

    @Test
    public void concurrent_addMultiInstanceNumberOfCompletedActivities_with_lock_should_not_lose_updates()
            throws Exception {
        runConcurrentMiCounterTest(
                "miActivityLockedCompleted", /* useLock */ true,
                (svc, mi) -> svc.addMultiInstanceNumberOfCompletedActivities(mi, 1),
                SMultiInstanceActivityInstance::getNumberOfCompletedInstances,
                "completions");
    }

    @Test
    public void concurrent_addMultiInstanceNumberOfTerminatedActivities_with_lock_should_not_lose_updates()
            throws Exception {
        runConcurrentMiCounterTest(
                "miActivityLockedTerminated", /* useLock */ true,
                (svc, mi) -> svc.addMultiInstanceNumberOfTerminatedActivities(mi, 1),
                SMultiInstanceActivityInstance::getNumberOfTerminatedInstances,
                "terminations");
    }

    private void runConcurrentMiCounterTest(
            final String activityName,
            final boolean useLock,
            final MiCounterOp op,
            final ToIntFunction<SMultiInstanceActivityInstance> counterGetter,
            final String counterLabel) throws Exception {
        final int threadCount = 50;

        final SProcessInstance processInstance = createSProcessInstance();
        try {
            final SMultiInstanceActivityInstance miActivity = createMultiInstanceActivity(
                    activityName, processInstance, threadCount);

            final LockService lockService = useLock ? getServiceAccessor().getLockService() : null;
            final String objectType = SFlowElementsContainerType.PROCESS.name();
            final List<Throwable> errors = new CopyOnWriteArrayList<>();
            final CyclicBarrier barrier = new CyclicBarrier(threadCount);
            final CountDownLatch done = new CountDownLatch(threadCount);
            final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            final long sessionId = getSessionAccessor().getSessionId();

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        getSessionAccessor().setSessionId(sessionId);
                        barrier.await(30, TimeUnit.SECONDS);

                        final BonitaLock lock = useLock
                                ? lockService.lock(processInstance.getId(), objectType)
                                : null;
                        try {
                            getTransactionService().begin();
                            try {
                                final SMultiInstanceActivityInstance freshMi = (SMultiInstanceActivityInstance) activityInstanceService
                                        .getFlowNodeInstance(miActivity.getId());
                                op.apply(activityInstanceService, freshMi);
                                getTransactionService().complete();
                            } catch (final Exception e) {
                                getTransactionService().setRollbackOnly();
                                getTransactionService().complete();
                                throw e;
                            }
                        } finally {
                            if (useLock) {
                                lockService.unlock(lock);
                            }
                        }
                    } catch (final Throwable t) {
                        errors.add(t);
                    } finally {
                        done.countDown();
                    }
                });
            }

            done.await(60, TimeUnit.SECONDS);
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            assertThat(errors).as("Unexpected errors in worker threads").isEmpty();

            getTransactionService().begin();
            final SMultiInstanceActivityInstance result = (SMultiInstanceActivityInstance) activityInstanceService
                    .getFlowNodeInstance(miActivity.getId());
            getTransactionService().complete();

            assertThat(counterGetter.applyAsInt(result))
                    .as("All %d %s should be counted", threadCount, counterLabel)
                    .isEqualTo(threadCount);
            assertThat(result.getNumberOfActiveInstances())
                    .as("Active instances should reach 0")
                    .isEqualTo(0);
        } finally {
            deleteSProcessInstance(processInstance);
        }
    }

    @FunctionalInterface
    private interface MiCounterOp {

        void apply(ActivityInstanceService svc, SMultiInstanceActivityInstance mi) throws SBonitaException;
    }

    private SMultiInstanceActivityInstance createMultiInstanceActivity(
            final String name, final SProcessInstance processInstance, final int numberOfActiveInstances)
            throws SBonitaException {
        final SMultiInstanceActivityInstance miActivity = new SMultiInstanceActivityInstance(
                name, 1L, processInstance.getId(), processInstance.getId(),
                1L, processInstance.getId(), false);
        miActivity.setNumberOfActiveInstances(numberOfActiveInstances);
        miActivity.setLoopCardinality(numberOfActiveInstances);
        miActivity.setStateId(28); // EXECUTING state
        miActivity.setLogicalGroup(3, processInstance.getId()); // parent process instance

        getTransactionService().begin();
        activityInstanceService.createActivityInstance(miActivity);
        getTransactionService().complete();

        return miActivity;
    }

}
