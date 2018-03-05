/**
 * Copyright (C) 2016 BonitaSoft S.A.
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

package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.Arrays;

import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessInvolvementDelegateTest {

    private static final long PROCESS_INSTANCE_ID = 987564L;
    public static final long PROCESS_INITIATOR_USER_ID = 55L;
    public static final long WRONG_USER_ID = -2l;

    @Mock
    SProcessInstance sProcessInstance;

    @Mock
    ProcessInstanceService processInstanceService;

    @Mock
    SessionService sessionService;

    @Mock
    SessionAccessor sessionAccessor;

    @Mock
    ActivityInstanceService activityInstanceService;

    @Mock
    SAProcessInstance saProcessInstance;

    @Mock
    TenantServiceAccessor tenantServiceAccessor;

    @Spy
    @InjectMocks
    public ProcessInvolvementDelegate processInvolvementDelegate;

    @Before
    public void before() {
        doReturn(tenantServiceAccessor).when(processInvolvementDelegate).getTenantServiceAccessor();
        doReturn(processInstanceService).when(tenantServiceAccessor).getProcessInstanceService();
    }

    @Test
    public final void should_isProcessOrArchivedProcessInitiator_return_false_if_user_do_not_exists() throws Exception {
        //given
        doReturn(sProcessInstance).when(processInstanceService).getProcessInstance(PROCESS_INSTANCE_ID);
        doReturn(PROCESS_INITIATOR_USER_ID).when(sProcessInstance).getStartedBy();

        // When
        boolean involvedInProcessInstance = processInvolvementDelegate.isProcessOrArchivedProcessInitiator(WRONG_USER_ID, PROCESS_INSTANCE_ID);

        //then
        assertThat(involvedInProcessInstance).as("user should not be involved involved in human task").isFalse();
    }

    @Test
    public final void should_isProcessOrArchivedProcessInitiator_return_true_if_user_started_process() throws Exception {
        //given
        doReturn(sProcessInstance).when(processInstanceService).getProcessInstance(PROCESS_INSTANCE_ID);
        doReturn(PROCESS_INITIATOR_USER_ID).when(sProcessInstance).getStartedBy();

        // When
        boolean involvedInProcessInstance = processInvolvementDelegate.isProcessOrArchivedProcessInitiator(PROCESS_INITIATOR_USER_ID, PROCESS_INSTANCE_ID);

        //then
        assertThat(involvedInProcessInstance).as("user should not be involved involved in human task").isTrue();
    }

    @Test
    public final void should_isProcessOrArchivedProcessInitiator_return_true_when_process_is_archived() throws Exception {
        //given
        doThrow(SProcessInstanceNotFoundException.class).when(processInstanceService).getProcessInstance(PROCESS_INSTANCE_ID);
        doReturn(Arrays.asList(saProcessInstance)).when(processInstanceService).searchArchivedProcessInstances(any(QueryOptions.class));
        doReturn(PROCESS_INITIATOR_USER_ID).when(saProcessInstance).getStartedBy();
        // When
        boolean involvedInProcessInstance = processInvolvementDelegate.isProcessOrArchivedProcessInitiator(PROCESS_INITIATOR_USER_ID, PROCESS_INSTANCE_ID);

        //then
        assertThat(involvedInProcessInstance).as("should be involved in archived case").isTrue();
    }

    @Test
    public final void should_isProcessOrArchivedProcessInitiator_return_false_when_process_is_archived() throws Exception {
        //given
        doThrow(SProcessInstanceNotFoundException.class).when(processInstanceService).getProcessInstance(PROCESS_INSTANCE_ID);
        doReturn(Arrays.asList(saProcessInstance)).when(processInstanceService).searchArchivedProcessInstances(any(QueryOptions.class));
        doReturn(PROCESS_INITIATOR_USER_ID).when(saProcessInstance).getStartedBy();
        // When
        boolean involvedInProcessInstance = processInvolvementDelegate.isProcessOrArchivedProcessInitiator(WRONG_USER_ID, PROCESS_INSTANCE_ID);

        //then
        assertThat(involvedInProcessInstance).as("should be involved in archived case").isFalse();
    }

}
