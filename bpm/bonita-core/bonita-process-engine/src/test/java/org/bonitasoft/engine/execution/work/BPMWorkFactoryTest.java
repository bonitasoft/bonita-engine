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
package org.bonitasoft.engine.execution.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SMessageInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SWaitingMessageEventImpl;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SWaitingSignalEventImpl;
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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Aurelien Pupier
 * @author Celine Souchet
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class BPMWorkFactoryTest {


    @InjectMocks
    private BPMWorkFactory workFactory;

    @Test
    public void createExecuteMessageCoupleWorkHasNoLockProcessInstanceWorkIfNoTargetProcess() {
        SWaitingMessageEventImpl waitingMessageEvent = createWaitingMessage();
        SMessageInstanceImpl messageInstance = createMessageInstance();
        final WrappingBonitaWork work = (WrappingBonitaWork) workFactory.create(workFactory.createExecuteMessageCoupleWorkDescriptor(messageInstance, waitingMessageEvent));
        final boolean containsLockProcessInstance = containsLockProcessInstanceWork(work);
        Assert.assertFalse("A lock Process Instance Work is used although there is no Target process", containsLockProcessInstance);
    }

    @Test
    public void createExecuteMessageCoupleWorkWithLockProcessInstanceWork() {
        SWaitingMessageEventImpl waitingMessageEvent = createWaitingMessage();
        waitingMessageEvent.setParentProcessInstanceId(5L);
        SMessageInstanceImpl messageInstance = createMessageInstance();
        final WrappingBonitaWork work = (WrappingBonitaWork) workFactory.create(workFactory.createExecuteMessageCoupleWorkDescriptor(messageInstance, waitingMessageEvent));
        final boolean containsLockProcessInstance = containsLockProcessInstanceWork(work);
        Assert.assertTrue("A lock Process Instance Work is missing although there is a Target process", containsLockProcessInstance);
    }

    @Test
    public void createExecuteMessageCoupleWork() {
        SWaitingMessageEventImpl waitingMessageEvent = createWaitingMessage();
        SMessageInstanceImpl messageInstance = createMessageInstance();
        final WrappingBonitaWork work = (WrappingBonitaWork) workFactory.create(workFactory.createExecuteMessageCoupleWorkDescriptor(messageInstance, waitingMessageEvent));
        Assert.assertTrue("A MessageInstanceContextWork is missing", containsFailureHandlingMessageInstance(work));
        Assert.assertTrue("A FailureHandlingProcessDefinitionCOntextWork is missing", containsFailureHandlingProcessDefinition(work));
    }

    private SMessageInstanceImpl createMessageInstance() {
        SMessageInstanceImpl sMessageInstance = new SMessageInstanceImpl("message", "myProcess", "flowNode", 1L, "throwFlowNode");
        sMessageInstance.setId(543L);
        return sMessageInstance;
    }

    private SWaitingMessageEventImpl createWaitingMessage() {
        SWaitingMessageEventImpl sWaitingMessageEvent = new SWaitingMessageEventImpl(SBPMEventType.INTERMEDIATE_CATCH_EVENT, 1L, "myProcess", 2, "flowNode", "message");
        sWaitingMessageEvent.setId(1234L);
        return sWaitingMessageEvent;
    }

    @Test
    public void createExecuteFlowNode() {
        final WrappingBonitaWork work = (WrappingBonitaWork) workFactory.create(workFactory.createExecuteFlowNodeWorkDescriptor(1L, 1L, 3));
        Assert.assertTrue("A ProcessDefinitionContextWork is missing", containsFailureHandlingProcessDefinition(work));
        Assert.assertTrue("A ProcessInstanceContextWork is missing", containsFailureHandlingProcessInstance(work));
        Assert.assertTrue("A ProcessInstanceContextWork is missing", containsFailureHandlingFlowNodeInstance(work));
    }

    @Test
    public void createExecuteConnectorOfProcess() {
        final WrappingBonitaWork work = (WrappingBonitaWork) workFactory.create(workFactory.createExecuteConnectorOfProcessDescriptor(1L, 2L, 4L, 3L, "connectorDefName", ConnectorEvent.ON_ENTER,
                null));
        Assert.assertTrue("A ProcessDefinitionContextWork is missing", containsFailureHandlingProcessDefinition(work));
    }

    @Test
    public void createExecuteConnectorOfActivity() {
        final WrappingBonitaWork work = (WrappingBonitaWork) workFactory.create(workFactory.createExecuteConnectorOfActivityDescriptor(1L, 3L, 4L, 5L, 6, "connectorDefName"));

        Assert.assertTrue("A ProcessDefinitionContextWork is missing", containsFailureHandlingProcessDefinition(work));
        Assert.assertTrue("A ProcessInstanceContextWork is missing", containsFailureHandlingProcessInstance(work));
    }

    @Test(expected = IllegalStateException.class)
    public void createExecuteConnectorOfActivity_with_missing_parameter() {
        workFactory.create(WorkDescriptor.create("EXECUTE_ACTIVITY_CONNECTOR"));
    }

    @Test
    public void createNotifyChildFinishedWork() {
        final WrappingBonitaWork work = (WrappingBonitaWork) workFactory.create(workFactory.createNotifyChildFinishedWorkDescriptor(1L, 2L, 3L, 4L, "parentType"));
        Assert.assertTrue("A ProcessDefinitionContextWork is missing", containsFailureHandlingProcessDefinition(work));
        Assert.assertTrue("A ProcessInstanceContextWork is missing", containsFailureHandlingProcessInstance(work));
        Assert.assertTrue("A ProcessInstanceContextWork is missing", containsFailureHandlingFlowNodeInstance(work));
    }

    @Test
    public void createTriggerSignalWork() {
        SWaitingSignalEventImpl listeningSignal = new SWaitingSignalEventImpl(SBPMEventType.INTERMEDIATE_CATCH_EVENT, 1L, "myProcess", 2L, "myFlowNode", "mySignal");
        listeningSignal.setId(123L);
        WrappingBonitaWork work = (WrappingBonitaWork) workFactory.create(workFactory.createTriggerSignalWorkDescriptor(listeningSignal));

        assertThat(getWorkOfClass(work, TriggerSignalWork.class).getDescription()).contains("waitingSignalEvent: 123");
        assertThat(containsLockProcessInstanceWork(work)).isFalse();
    }

    @Test
    public void createTriggerSignalWork_in_a_process_instance() {
        SWaitingSignalEventImpl listeningSignal = new SWaitingSignalEventImpl(SBPMEventType.INTERMEDIATE_CATCH_EVENT, 1L, "myProcess", 2L, "myFlowNode", "mySignal");
        listeningSignal.setId(123L);
        listeningSignal.setParentProcessInstanceId(456L);
        WrappingBonitaWork work = (WrappingBonitaWork) workFactory.create(workFactory.createTriggerSignalWorkDescriptor(listeningSignal));

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
        if (!(work instanceof WrappingBonitaWork)) {
            return null;
        }
        WrappingBonitaWork wrappingBonitaWork = ((WrappingBonitaWork) work);
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
