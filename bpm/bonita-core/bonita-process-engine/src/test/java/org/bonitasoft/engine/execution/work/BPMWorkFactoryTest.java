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
package org.bonitasoft.engine.execution.work;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SStartEventDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SFlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.model.SAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingSignalEvent;
import org.bonitasoft.engine.execution.FlowNodeNameFilter;
import org.bonitasoft.engine.execution.FlowNodeSelector;
import org.bonitasoft.engine.execution.work.failurewrapping.FlowNodeDefinitionAndInstanceContextWork;
import org.bonitasoft.engine.execution.work.failurewrapping.MessageInstanceContextWork;
import org.bonitasoft.engine.execution.work.failurewrapping.ProcessDefinitionContextWork;
import org.bonitasoft.engine.execution.work.failurewrapping.ProcessInstanceContextWork;
import org.bonitasoft.engine.execution.work.failurewrapping.TriggerSignalWork;
import org.bonitasoft.engine.work.BonitaWork;
import org.bonitasoft.engine.work.WorkDescriptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Aurelien Pupier
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class BPMWorkFactoryTest {

    @InjectMocks
    private BPMWorkFactory workFactory;

    @Test
    public void createExecuteMessageCoupleWorkHasNoLockProcessInstanceWorkIfNoTargetProcess() {
        SWaitingMessageEvent waitingMessageEvent = createWaitingMessage();
        SMessageInstance messageInstance = createMessageInstance();
        final WrappingBonitaWork work = (WrappingBonitaWork) workFactory
                .create(workFactory.createExecuteMessageCoupleWorkDescriptor(messageInstance, waitingMessageEvent));
        final boolean containsLockProcessInstance = containsLockProcessInstanceWork(work);
        Assert.assertFalse("A lock Process Instance Work is used although there is no Target process",
                containsLockProcessInstance);
    }

    @Test
    public void createExecuteMessageCoupleWorkWithLockProcessInstanceWork() {
        SWaitingMessageEvent waitingMessageEvent = createWaitingMessage();
        waitingMessageEvent.setParentProcessInstanceId(5L);
        SMessageInstance messageInstance = createMessageInstance();
        final WrappingBonitaWork work = (WrappingBonitaWork) workFactory
                .create(workFactory.createExecuteMessageCoupleWorkDescriptor(messageInstance, waitingMessageEvent));
        final boolean containsLockProcessInstance = containsLockProcessInstanceWork(work);
        Assert.assertTrue("A lock Process Instance Work is missing although there is a Target process",
                containsLockProcessInstance);
    }

    @Test
    public void createExecuteMessageCoupleWork() {
        SWaitingMessageEvent waitingMessageEvent = createWaitingMessage();
        SMessageInstance messageInstance = createMessageInstance();
        final WrappingBonitaWork work = (WrappingBonitaWork) workFactory
                .create(workFactory.createExecuteMessageCoupleWorkDescriptor(messageInstance, waitingMessageEvent));
        Assert.assertTrue("A MessageInstanceContextWork is missing", containsFailureHandlingMessageInstance(work));
        Assert.assertTrue("A FailureHandlingProcessDefinitionCOntextWork is missing",
                containsFailureHandlingProcessDefinition(work));
    }

    private SMessageInstance createMessageInstance() {
        SMessageInstance sMessageInstance = new SMessageInstance("message", "myProcess", "flowNode", 1L,
                "throwFlowNode");
        sMessageInstance.setId(543L);
        return sMessageInstance;
    }

    private SWaitingMessageEvent createWaitingMessage() {
        SWaitingMessageEvent sWaitingMessageEvent = new SWaitingMessageEvent(SBPMEventType.INTERMEDIATE_CATCH_EVENT, 1L,
                "myProcess", 2, "flowNode", "message");
        sWaitingMessageEvent.setId(1234L);
        return sWaitingMessageEvent;
    }

    @Test
    public void createExecuteFlowNode() {
        SAutomaticTaskInstance flowNodeInstance = new SAutomaticTaskInstance("task", 5432L, 631L, 52311, 33L, 441L);
        flowNodeInstance.setLogicalGroup4(3452L);
        final WrappingBonitaWork work = (WrappingBonitaWork) workFactory
                .create(workFactory.createExecuteFlowNodeWorkDescriptor(flowNodeInstance));
        Assert.assertTrue("A ProcessDefinitionContextWork is missing", containsFailureHandlingProcessDefinition(work));
        Assert.assertTrue("A ProcessInstanceContextWork is missing", containsFailureHandlingProcessInstance(work));
        Assert.assertTrue("A ProcessInstanceContextWork is missing", containsFailureHandlingFlowNodeInstance(work));
    }

    @Test
    public void createExecuteConnectorOfProcess() {
        final WrappingBonitaWork work = (WrappingBonitaWork) workFactory.create(workFactory
                .createExecuteConnectorOfProcessDescriptor(1L, 2L, 4L, 3L, "connectorDefName", ConnectorEvent.ON_ENTER,
                        null));
        Assert.assertTrue("A ProcessDefinitionContextWork is missing", containsFailureHandlingProcessDefinition(work));
    }

    @Test
    public void createExecuteConnectorOfProcessWithFlowNodeSelector() {
        SProcessDefinitionImpl definition = new SProcessDefinitionImpl("name", "version");
        SFlowElementContainerDefinitionImpl processContainer = new SFlowElementContainerDefinitionImpl();
        definition.setProcessContainer(processContainer);
        SStartEventDefinitionImpl start1 = new SStartEventDefinitionImpl(1L, "start1");
        SStartEventDefinitionImpl start2 = new SStartEventDefinitionImpl(2L, "start2");
        SStartEventDefinitionImpl start3 = new SStartEventDefinitionImpl(3L, "start3");
        processContainer.addEvent(start1);
        processContainer.addEvent(start2);
        processContainer.addEvent(start3);
        FlowNodeSelector flowNodeSelector = new FlowNodeSelector(definition,
                new FlowNodeNameFilter(Arrays.asList("start1", "start2")));

        final WrappingBonitaWork work = (WrappingBonitaWork) workFactory.create(workFactory
                .createExecuteConnectorOfProcessDescriptor(1L, 2L, 4L, 3L, "connectorDefName", ConnectorEvent.ON_ENTER,
                        flowNodeSelector));

        assertThat(getWorkOfClass(work, ExecuteConnectorOfProcess.class).filterFlowNodeDefinitions.mustSelect(start1))
                .isTrue();
        assertThat(getWorkOfClass(work, ExecuteConnectorOfProcess.class).filterFlowNodeDefinitions.mustSelect(start2))
                .isTrue();
        assertThat(getWorkOfClass(work, ExecuteConnectorOfProcess.class).filterFlowNodeDefinitions.mustSelect(start3))
                .isFalse();
    }

    @Test
    public void should_be_able_to_create_execute_connector_work_with_empty_flownode_selector() {
        SProcessDefinitionImpl definition = new SProcessDefinitionImpl("name", "version");
        SFlowElementContainerDefinitionImpl processContainer = new SFlowElementContainerDefinitionImpl();
        definition.setProcessContainer(processContainer);
        SStartEventDefinitionImpl start1 = new SStartEventDefinitionImpl(1L, "start1");
        processContainer.addEvent(start1);
        FlowNodeSelector flowNodeSelector = new FlowNodeSelector(definition,
                new FlowNodeNameFilter(emptyList()));

        final WrappingBonitaWork work = (WrappingBonitaWork) workFactory.create(workFactory
                .createExecuteConnectorOfProcessDescriptor(1L, 2L, 4L, 3L, "connectorDefName", ConnectorEvent.ON_ENTER,
                        flowNodeSelector));

        assertThat(getWorkOfClass(work, ExecuteConnectorOfProcess.class).filterFlowNodeDefinitions.mustSelect(start1))
                .isFalse();
    }

    @Test
    public void createExecuteConnectorOfActivity() {
        final WrappingBonitaWork work = (WrappingBonitaWork) workFactory
                .create(workFactory.createExecuteConnectorOfActivityDescriptor(1L, 3L, 4L, 5L, 6, "connectorDefName"));

        Assert.assertTrue("A ProcessDefinitionContextWork is missing", containsFailureHandlingProcessDefinition(work));
        Assert.assertTrue("A ProcessInstanceContextWork is missing", containsFailureHandlingProcessInstance(work));
    }

    @Test(expected = IllegalStateException.class)
    public void createExecuteConnectorOfActivity_with_missing_parameter() {
        workFactory.create(WorkDescriptor.create("EXECUTE_ACTIVITY_CONNECTOR"));
    }

    @Test
    public void createNotifyChildFinishedWork() {
        final WrappingBonitaWork work = (WrappingBonitaWork) workFactory
                .create(workFactory.createNotifyChildFinishedWorkDescriptor(new SAutomaticTaskInstance()));
        Assert.assertTrue("A ProcessDefinitionContextWork is missing", containsFailureHandlingProcessDefinition(work));
        Assert.assertTrue("A ProcessInstanceContextWork is missing", containsFailureHandlingProcessInstance(work));
        Assert.assertTrue("A ProcessInstanceContextWork is missing", containsFailureHandlingFlowNodeInstance(work));
    }

    @Test
    public void createTriggerSignalWork() {
        SWaitingSignalEvent listeningSignal = new SWaitingSignalEvent(SBPMEventType.INTERMEDIATE_CATCH_EVENT, 1L,
                "myProcess", 2L, "myFlowNode", "mySignal");
        listeningSignal.setId(123L);
        WrappingBonitaWork work = (WrappingBonitaWork) workFactory
                .create(workFactory.createTriggerSignalWorkDescriptor(listeningSignal));

        assertThat(getWorkOfClass(work, TriggerSignalWork.class).getDescription()).contains("waitingSignalEvent: 123");
        assertThat(containsLockProcessInstanceWork(work)).isFalse();
    }

    @Test
    public void createTriggerSignalWork_in_a_process_instance() {
        SWaitingSignalEvent listeningSignal = new SWaitingSignalEvent(SBPMEventType.INTERMEDIATE_CATCH_EVENT, 1L,
                "myProcess", 2L, "myFlowNode", "mySignal");
        listeningSignal.setId(123L);
        listeningSignal.setParentProcessInstanceId(456L);
        WrappingBonitaWork work = (WrappingBonitaWork) workFactory
                .create(workFactory.createTriggerSignalWorkDescriptor(listeningSignal));

        assertThat(getWorkOfClass(work, TriggerSignalWork.class).getDescription()).contains("waitingSignalEvent: 123");
        assertThat(getWorkOfClass(work, LockProcessInstanceWork.class).processInstanceId).isEqualTo(456L);
    }

    private boolean containsFailureHandlingFlowNodeInstance(BonitaWork work) {
        return containsWorkOfClass(work, FlowNodeDefinitionAndInstanceContextWork.class);
    }

    private boolean containsFailureHandlingProcessInstance(BonitaWork work) {
        return containsWorkOfClass(work, ProcessInstanceContextWork.class);
    }

    private boolean containsFailureHandlingProcessDefinition(BonitaWork work) {
        return containsWorkOfClass(work, ProcessDefinitionContextWork.class);
    }

    private boolean containsFailureHandlingMessageInstance(BonitaWork work) {
        return containsWorkOfClass(work, MessageInstanceContextWork.class);
    }

    private boolean containsLockProcessInstanceWork(BonitaWork work) {
        return containsWorkOfClass(work, LockProcessInstanceWork.class);
    }

    private boolean containsWorkOfClass(BonitaWork work, final Class<?> clazz) {
        return getWorkOfClass(work, clazz) != null;
    }

    private <T> T getWorkOfClass(BonitaWork work, Class<T> clazz) {
        if (clazz.isAssignableFrom(work.getClass())) {
            return ((T) work);
        }
        if (!(work instanceof WrappingBonitaWork wrappingBonitaWork)) {
            return null;
        }
        return getWorkOfClass(wrappingBonitaWork.getWrappedWork(), clazz);
    }

    @Test
    public void should_create_work_using_extensions() {
        BonitaWork bonitaWork = new BonitaWork() {

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public CompletableFuture<Void> work(Map<String, Object> context) {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public void handleFailure(Throwable e, Map<String, Object> context) {
            }
        };
        workFactory.addExtension("MyWork", workDescriptor -> bonitaWork);

        assertThat(workFactory.create(WorkDescriptor.create("MyWork"))).isEqualTo(bonitaWork);
    }

}
