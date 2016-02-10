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
package org.bonitasoft.engine.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.List;

import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAActivityInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAProcessInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAUserTaskInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SActivityInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SProcessInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
        doReturn(new SProcessInstanceImpl()).when(processInstanceService).getProcessInstance(5l);
        //when
        List<Pair<Long, String>> containerHierarchy = parentContainerResolver.getContainerHierarchy(Pair.of(5l, DataInstanceContainer.PROCESS_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(1);
        assertThat(containerHierarchy.get(0)).isEqualTo(Pair.of(5l, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }

    @Test
    public void getContainerHierarchy_on_message() throws Exception {
        //given
        //when
        List<Pair<Long, String>> containerHierarchy = parentContainerResolver.getContainerHierarchy(Pair.of(5l, DataInstanceContainer.MESSAGE_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(1);
        assertThat(containerHierarchy.get(0)).isEqualTo(Pair.of(5l, DataInstanceContainer.MESSAGE_INSTANCE.name()));
    }

    @Test
    public void getContainerHierarchy_on_process_with_caller_is_a_call_activity() throws Exception {
        //given
        processWithCaller(PROCESS_INSTANCE_ID, CALLER_ID);
        activityWithType(CALLER_ID, SFlowNodeType.CALL_ACTIVITY, 0, 0, 0, 0);
        //when
        List<Pair<Long, String>> containerHierarchy = parentContainerResolver.getContainerHierarchy(Pair.of(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(1);
        assertThat(containerHierarchy.get(0)).isEqualTo(Pair.of(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }

    private void activityWithType(long id, final SFlowNodeType type, final long parentProcessInstanceId, final long parentActivityInstanceId, final long parentContainerId, final long rootContainerId) throws SFlowNodeReadException, SFlowNodeNotFoundException {
        SActivityInstanceImpl activity = new SUserTaskInstanceImpl() {
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

    private void processWithCaller(long processInstanceId, long callerId) throws SProcessInstanceNotFoundException, SProcessInstanceReadException {
        SProcessInstanceImpl processInstance = new SProcessInstanceImpl();
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
        List<Pair<Long, String>> containerHierarchy = parentContainerResolver.getContainerHierarchy(Pair.of(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(2);
        assertThat(containerHierarchy.get(0)).isEqualTo(Pair.of(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }


    @Test
    public void getContainerHierarchy_on_activity() throws Exception {
        //given
        processWithCaller(PROCESS_INSTANCE_ID, -1);
        activityWithType(ACTIVITY_INSTANCE_ID, SFlowNodeType.USER_TASK, PROCESS_INSTANCE_ID, -1, PROCESS_INSTANCE_ID, PROCESS_INSTANCE_ID);
        //when
        List<Pair<Long, String>> containerHierarchy = parentContainerResolver.getContainerHierarchy(Pair.of(ACTIVITY_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(2);
        assertThat(containerHierarchy.get(0)).isEqualTo(Pair.of(ACTIVITY_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        assertThat(containerHierarchy.get(1)).isEqualTo(Pair.of(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }


    @Test
    public void getContainerHierarchy_on_sub_activity() throws Exception {
        //given
        processWithCaller(PROCESS_INSTANCE_ID, -1);
        activityWithType(ACTIVITY_INSTANCE_ID, SFlowNodeType.USER_TASK, PROCESS_INSTANCE_ID, -1, PROCESS_INSTANCE_ID , PROCESS_INSTANCE_ID);
        activityWithType(SUB_ACTIVITY_ID, SFlowNodeType.USER_TASK, PROCESS_INSTANCE_ID, ACTIVITY_INSTANCE_ID, ACTIVITY_INSTANCE_ID , PROCESS_INSTANCE_ID);
        //when
        List<Pair<Long, String>> containerHierarchy = parentContainerResolver.getContainerHierarchy(Pair.of(SUB_ACTIVITY_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(3);
        assertThat(containerHierarchy.get(0)).isEqualTo(Pair.of(SUB_ACTIVITY_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        assertThat(containerHierarchy.get(1)).isEqualTo(Pair.of(ACTIVITY_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        assertThat(containerHierarchy.get(2)).isEqualTo(Pair.of(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }



    //// archived



    @Test
    public void getArchivedContainerHierarchy_on_process_with_no_caller() throws Exception {
        //given
        doReturn(new SProcessInstanceImpl()).when(processInstanceService).getProcessInstance(5l);
        //when
        List<Pair<Long, String>> containerHierarchy = parentContainerResolver.getArchivedContainerHierarchy(Pair.of(5l, DataInstanceContainer.PROCESS_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(1);
        assertThat(containerHierarchy.get(0)).isEqualTo(Pair.of(5l, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }

    @Test
    public void getArchivedContainerHierarchy_on_message() throws Exception {
        //given
        //when
        List<Pair<Long, String>> containerHierarchy = parentContainerResolver.getArchivedContainerHierarchy(Pair.of(5l, DataInstanceContainer.MESSAGE_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(1);
        assertThat(containerHierarchy.get(0)).isEqualTo(Pair.of(5l, DataInstanceContainer.MESSAGE_INSTANCE.name()));
    }

    @Test
    public void getArchivedContainerHierarchy_on_process_with_caller_is_a_call_activity() throws Exception {
        //given
        processWithCaller(PROCESS_INSTANCE_ID, CALLER_ID);
        activityWithType(CALLER_ID, SFlowNodeType.CALL_ACTIVITY, 0, 0, 0, 0);
        //when
        List<Pair<Long, String>> containerHierarchy = parentContainerResolver.getArchivedContainerHierarchy(Pair.of(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(1);
        assertThat(containerHierarchy.get(0)).isEqualTo(Pair.of(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }

    private void archivedActivityWithType(long id, final SFlowNodeType type, final long parentProcessInstanceId, final long parentActivityInstanceId, final long parentContainerId, final long rootContainerId) throws SFlowNodeReadException, SFlowNodeNotFoundException, SBonitaReadException {
        SAActivityInstanceImpl activity = new SAUserTaskInstanceImpl() {
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

    private void archivedProcessWithCaller(long processInstanceId, long callerId) throws SProcessInstanceNotFoundException, SProcessInstanceReadException, SBonitaReadException {
        SAProcessInstanceImpl processInstance = new SAProcessInstanceImpl();
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
        List<Pair<Long, String>> containerHierarchy = parentContainerResolver.getArchivedContainerHierarchy(Pair.of(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(2);
        assertThat(containerHierarchy.get(0)).isEqualTo(Pair.of(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }


    @Test
    public void getArchivedContainerHierarchy_on_activity() throws Exception {
        //given
        archivedProcessWithCaller(PROCESS_INSTANCE_ID, -1);
        archivedActivityWithType(ACTIVITY_INSTANCE_ID, SFlowNodeType.USER_TASK, PROCESS_INSTANCE_ID, -1, PROCESS_INSTANCE_ID, PROCESS_INSTANCE_ID);
        //when
        List<Pair<Long, String>> containerHierarchy = parentContainerResolver.getArchivedContainerHierarchy(Pair.of(ACTIVITY_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(2);
        assertThat(containerHierarchy.get(0)).isEqualTo(Pair.of(ACTIVITY_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        assertThat(containerHierarchy.get(1)).isEqualTo(Pair.of(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }


    @Test
    public void getArchivedContainerHierarchy_on_sub_activity() throws Exception {
        //given
        archivedProcessWithCaller(PROCESS_INSTANCE_ID, -1);
        archivedActivityWithType(ACTIVITY_INSTANCE_ID, SFlowNodeType.USER_TASK, PROCESS_INSTANCE_ID, -1, PROCESS_INSTANCE_ID, PROCESS_INSTANCE_ID);
        archivedActivityWithType(SUB_ACTIVITY_ID, SFlowNodeType.USER_TASK, PROCESS_INSTANCE_ID, ACTIVITY_INSTANCE_ID, ACTIVITY_INSTANCE_ID, PROCESS_INSTANCE_ID);
        //when
        List<Pair<Long, String>> containerHierarchy = parentContainerResolver.getArchivedContainerHierarchy(Pair.of(SUB_ACTIVITY_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        //then
        assertThat(containerHierarchy).hasSize(3);
        assertThat(containerHierarchy.get(0)).isEqualTo(Pair.of(SUB_ACTIVITY_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        assertThat(containerHierarchy.get(1)).isEqualTo(Pair.of(ACTIVITY_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name()));
        assertThat(containerHierarchy.get(2)).isEqualTo(Pair.of(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name()));
    }

}