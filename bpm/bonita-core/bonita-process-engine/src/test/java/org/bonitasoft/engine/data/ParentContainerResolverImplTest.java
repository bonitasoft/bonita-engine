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
package org.bonitasoft.engine.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAUserTaskInstance;
import org.bonitasoft.engine.data.instance.api.DataContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ParentContainerResolverImplTest {

    private static final long PROCESS_INSTANCE_ID = 5l;
    private static final long CALLER_ID = 12l;
    private static final long ACTIVITY_INSTANCE_ID = 59l;
    private static final long SUB_ACTIVITY_ID = 60l;
    @Mock
    private ProcessInstanceService processInstanceService;
    @Mock
    private FlowNodeInstanceService flowNodeInstanceService;
    @InjectMocks
    private ParentContainerResolverImpl parentContainerResolver;

    @Test
    public void getContainerHierarchy_on_process_with_no_caller() throws Exception {
        //given
        doReturn(SProcessInstance.builder().build()).when(processInstanceService).getProcessInstance(5L);
        //when
        List<DataContainer> containerHierarchy = parentContainerResolver
                .getContainerHierarchy(new DataContainer(5L, DataInstanceContainer.PROCESS_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(1);
        assertThat(containerHierarchy.get(0))
                .isEqualTo(new DataContainer(5L, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }

    @Test
    public void getContainerHierarchy_on_message() throws Exception {
        //given
        //when
        List<DataContainer> containerHierarchy = parentContainerResolver
                .getContainerHierarchy(new DataContainer(5l, DataInstanceContainer.MESSAGE_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(1);
        assertThat(containerHierarchy.get(0))
                .isEqualTo(new DataContainer(5l, DataInstanceContainer.MESSAGE_INSTANCE.name()));
    }

    @Test
    public void getContainerHierarchy_on_process_with_caller_is_a_call_activity() throws Exception {
        //given
        processWithCaller(PROCESS_INSTANCE_ID, CALLER_ID);
        activityWithType(CALLER_ID, SFlowNodeType.CALL_ACTIVITY, 0, 0, 0, 0);
        //when
        List<DataContainer> containerHierarchy = parentContainerResolver
                .getContainerHierarchy(
                        new DataContainer(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(1);
        assertThat(containerHierarchy.get(0))
                .isEqualTo(new DataContainer(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }

    private void activityWithType(long id, final SFlowNodeType type, final long parentProcessInstanceId,
            final long parentActivityInstanceId,
            final long parentContainerId, final long rootContainerId)
            throws SFlowNodeReadException, SFlowNodeNotFoundException {
        SActivityInstance activity = new SUserTaskInstance() {

            @Override
            public SFlowNodeType getType() {
                return type;
            }
        };
        activity.setId(id);
        doReturn(activity).when(flowNodeInstanceService).getFlowNodeInstance(id);
        activity.setLogicalGroup(3, parentProcessInstanceId);
        activity.setLogicalGroup(2, parentActivityInstanceId);
        activity.setParentContainerId(parentContainerId);
        activity.setRootContainerId(rootContainerId);
    }

    private void processWithCaller(long processInstanceId, long callerId)
            throws SProcessInstanceNotFoundException, SProcessInstanceReadException {
        SProcessInstance processInstance = new SProcessInstance();
        processInstance.setCallerId(callerId);
        doReturn(processInstance).when(processInstanceService).getProcessInstance(processInstanceId);
    }

    @Test
    public void getContainerHierarchy_on_process_with_caller_is_a_sub_process() throws Exception {
        //given
        processWithCaller(PROCESS_INSTANCE_ID, CALLER_ID);
        processWithCaller(45l, -1);
        activityWithType(CALLER_ID, SFlowNodeType.SUB_PROCESS, 45l, -1, 45l, 45l);
        //when
        List<DataContainer> containerHierarchy = parentContainerResolver
                .getContainerHierarchy(
                        new DataContainer(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(2);
        assertThat(containerHierarchy.get(0))
                .isEqualTo(new DataContainer(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }

    @Test
    public void getContainerHierarchy_on_activity() throws Exception {
        //given
        processWithCaller(PROCESS_INSTANCE_ID, -1);
        activityWithType(ACTIVITY_INSTANCE_ID, SFlowNodeType.USER_TASK, PROCESS_INSTANCE_ID, -1, PROCESS_INSTANCE_ID,
                PROCESS_INSTANCE_ID);
        //when
        List<DataContainer> containerHierarchy = parentContainerResolver
                .getContainerHierarchy(
                        new DataContainer(ACTIVITY_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(2);
        assertThat(containerHierarchy.get(0))
                .isEqualTo(new DataContainer(ACTIVITY_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        assertThat(containerHierarchy.get(1))
                .isEqualTo(new DataContainer(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }

    @Test
    public void getContainerHierarchy_on_sub_activity() throws Exception {
        //given
        processWithCaller(PROCESS_INSTANCE_ID, -1);
        activityWithType(ACTIVITY_INSTANCE_ID, SFlowNodeType.USER_TASK, PROCESS_INSTANCE_ID, -1, PROCESS_INSTANCE_ID,
                PROCESS_INSTANCE_ID);
        activityWithType(SUB_ACTIVITY_ID, SFlowNodeType.USER_TASK, PROCESS_INSTANCE_ID, ACTIVITY_INSTANCE_ID,
                ACTIVITY_INSTANCE_ID, PROCESS_INSTANCE_ID);
        //when
        List<DataContainer> containerHierarchy = parentContainerResolver
                .getContainerHierarchy(
                        new DataContainer(SUB_ACTIVITY_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(3);
        assertThat(containerHierarchy.get(0))
                .isEqualTo(new DataContainer(SUB_ACTIVITY_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        assertThat(containerHierarchy.get(1))
                .isEqualTo(new DataContainer(ACTIVITY_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        assertThat(containerHierarchy.get(2))
                .isEqualTo(new DataContainer(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }

    //// archived

    @Test
    public void getArchivedContainerHierarchy_on_process_with_no_caller() throws Exception {
        //when
        List<DataContainer> containerHierarchy = parentContainerResolver
                .getArchivedContainerHierarchy(new DataContainer(5l, DataInstanceContainer.PROCESS_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(1);
        assertThat(containerHierarchy.get(0))
                .isEqualTo(new DataContainer(5l, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }

    @Test
    public void getArchivedContainerHierarchy_on_message() throws Exception {
        //given
        //when
        List<DataContainer> containerHierarchy = parentContainerResolver
                .getArchivedContainerHierarchy(new DataContainer(5l, DataInstanceContainer.MESSAGE_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(1);
        assertThat(containerHierarchy.get(0))
                .isEqualTo(new DataContainer(5l, DataInstanceContainer.MESSAGE_INSTANCE.name()));
    }

    @Test
    public void getArchivedContainerHierarchy_on_process_with_caller_is_a_call_activity() throws Exception {
        //given
        processWithCaller(PROCESS_INSTANCE_ID, CALLER_ID);
        activityWithType(CALLER_ID, SFlowNodeType.CALL_ACTIVITY, 0, 0, 0, 0);
        //when
        List<DataContainer> containerHierarchy = parentContainerResolver
                .getArchivedContainerHierarchy(
                        new DataContainer(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(1);
        assertThat(containerHierarchy.get(0))
                .isEqualTo(new DataContainer(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }

    private void archivedActivityWithType(long id, final SFlowNodeType type, final long parentProcessInstanceId,
            final long parentActivityInstanceId,
            final long parentContainerId, final long rootContainerId)
            throws SFlowNodeReadException, SFlowNodeNotFoundException, SBonitaReadException {
        SAActivityInstance activity = new SAUserTaskInstance() {

            @Override
            public SFlowNodeType getType() {
                return type;
            }
        };
        activity.setId(id);
        doReturn(activity).when(flowNodeInstanceService).getLastArchivedFlowNodeInstance(SAFlowNodeInstance.class, id);
        activity.setLogicalGroup(3, parentProcessInstanceId);
        activity.setLogicalGroup(2, parentActivityInstanceId);
        activity.setParentContainerId(parentContainerId);
        activity.setRootContainerId(rootContainerId);
    }

    private void archivedProcessWithCaller(long processInstanceId, long callerId)
            throws SProcessInstanceNotFoundException, SProcessInstanceReadException, SBonitaReadException {
        SAProcessInstance processInstance = new SAProcessInstance();
        processInstance.setCallerId(callerId);
        doReturn(processInstance).when(processInstanceService).getLastArchivedProcessInstance(processInstanceId);
    }

    @Test
    public void getArchivedContainerHierarchy_on_process_with_caller_is_a_sub_process() throws Exception {
        //given
        archivedProcessWithCaller(PROCESS_INSTANCE_ID, CALLER_ID);
        archivedProcessWithCaller(45l, -1);
        archivedActivityWithType(CALLER_ID, SFlowNodeType.SUB_PROCESS, 45l, -1, 45l, 45l);
        //when
        List<DataContainer> containerHierarchy = parentContainerResolver
                .getArchivedContainerHierarchy(
                        new DataContainer(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(2);
        assertThat(containerHierarchy.get(0))
                .isEqualTo(new DataContainer(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }

    @Test
    public void getArchivedContainerHierarchy_on_activity() throws Exception {
        //given
        archivedProcessWithCaller(PROCESS_INSTANCE_ID, -1);
        archivedActivityWithType(ACTIVITY_INSTANCE_ID, SFlowNodeType.USER_TASK, PROCESS_INSTANCE_ID, -1,
                PROCESS_INSTANCE_ID, PROCESS_INSTANCE_ID);
        //when
        List<DataContainer> containerHierarchy = parentContainerResolver
                .getArchivedContainerHierarchy(
                        new DataContainer(ACTIVITY_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(2);
        assertThat(containerHierarchy.get(0))
                .isEqualTo(new DataContainer(ACTIVITY_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        assertThat(containerHierarchy.get(1))
                .isEqualTo(new DataContainer(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }

    @Test
    public void getArchivedContainerHierarchy_on_sub_activity() throws Exception {
        //given
        archivedProcessWithCaller(PROCESS_INSTANCE_ID, -1);
        archivedActivityWithType(ACTIVITY_INSTANCE_ID, SFlowNodeType.USER_TASK, PROCESS_INSTANCE_ID, -1,
                PROCESS_INSTANCE_ID, PROCESS_INSTANCE_ID);
        archivedActivityWithType(SUB_ACTIVITY_ID, SFlowNodeType.USER_TASK, PROCESS_INSTANCE_ID, ACTIVITY_INSTANCE_ID,
                ACTIVITY_INSTANCE_ID,
                PROCESS_INSTANCE_ID);
        //when
        List<DataContainer> containerHierarchy = parentContainerResolver
                .getArchivedContainerHierarchy(
                        new DataContainer(SUB_ACTIVITY_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(3);
        assertThat(containerHierarchy.get(0))
                .isEqualTo(new DataContainer(SUB_ACTIVITY_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        assertThat(containerHierarchy.get(1))
                .isEqualTo(new DataContainer(ACTIVITY_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        assertThat(containerHierarchy.get(2))
                .isEqualTo(new DataContainer(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }

}
