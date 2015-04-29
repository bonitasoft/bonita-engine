/*
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
 */
package org.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class IsProcessInitiatorRuleTest extends RuleTest {

    @Mock
    ProcessInstanceService processInstanceService;
    
    @Mock
    SessionService sessionService;

    @Mock
    SessionAccessor sessionAccessor;

    @InjectMocks
    IsProcessInitiatorRule rule;
    
    long loggedUserId = 7L;
    
    @Before
    public void initMocks() throws Exception {
        when(sessionAccessor.getSessionId()).thenReturn(1L);
        SSession session = mock(SSession.class);
        when(session.getUserId()).thenReturn(loggedUserId);
        when(sessionService.getSession(1L)).thenReturn(session);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegallArgumentIfProcessInstanceIdParamNotPresent() throws Exception {
        Map<String, Serializable> context = buildContext(null, null);

        final boolean allowed = rule.isAllowed("someKey", context);
    }

    @Test
    public void shouldNotBeAllowedIfProcessInstanceInitiatorIsNotGivenUser() throws Exception {
        final long processInstanceId = 541L;

        Map<String, Serializable> context = buildContext(processInstanceId, null);
        final SProcessInstance processInstance = mock(SProcessInstance.class);
        doReturn(444L).when(processInstance).getStartedBy();
        doReturn(processInstance).when(processInstanceService).getProcessInstance(processInstanceId);

        final boolean allowed = rule.isAllowed("someKey", context);

        assertThat(allowed).isFalse();
    }

    @Test
    public void shouldBeAllowedIfProcessInstanceInitiatorIsPreciselyTheGivenUser() throws Exception {
        final long processInstanceId = 541L;

        Map<String, Serializable> context = buildContext(processInstanceId, null);
        final SProcessInstance processInstance = mock(SProcessInstance.class);
        doReturn(loggedUserId).when(processInstance).getStartedBy();
        doReturn(processInstance).when(processInstanceService).getProcessInstance(processInstanceId);

        final boolean allowed = rule.isAllowed("someKey", context);

        assertThat(allowed).isTrue();
    }

    @Test(expected = SExecutionException.class)
    public void shouldThrowExecutionExceptionIfServiceProblemOccurs() throws Exception {
        final long processInstanceId = 541L;

        final long userId = 11L;
        Map<String, Serializable> context = buildContext(processInstanceId, userId);
        doThrow(SProcessInstanceReadException.class).when(processInstanceService).getProcessInstance(processInstanceId);

        final boolean allowed = rule.isAllowed("exception raised", context);
    }

    @Test
    public void shouldEnsureArchivedProcessInstanceStartedBy() throws Exception {
        final long processInstanceId = 541L;

        Map<String, Serializable> context = buildContext(processInstanceId, null);
        doThrow(SProcessInstanceNotFoundException.class).when(processInstanceService).getProcessInstance(processInstanceId);
        final SAProcessInstance saProcessInstance = mock(SAProcessInstance.class);
        doReturn(loggedUserId).when(saProcessInstance).getStartedBy();
        doReturn(Collections.singletonList(saProcessInstance)).when(processInstanceService).searchArchivedProcessInstances(any(QueryOptions.class));

        final boolean allowed = rule.isAllowed("someKey", context);

        assertThat(allowed).isTrue();
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldThrowNotFoundIfProcessNotFoundInTheArchivesEither() throws Exception {
        final long processInstanceId = 541L;

        Map<String, Serializable> context = buildContext(processInstanceId, null);
        doThrow(SProcessInstanceNotFoundException.class).when(processInstanceService).getProcessInstance(processInstanceId);
        doReturn(Collections.emptyList()).when(processInstanceService).searchArchivedProcessInstances(any(QueryOptions.class));

        expectedException.expect(SExecutionException.class);
        expectedException.expectCause(CoreMatchers.<Throwable> instanceOf(SProcessInstanceNotFoundException.class));

        rule.isAllowed("exception raised", context);

    }

    @Test
    public void getIdShouldReturnIsProcessInitiator() throws Exception {
        assertThat(rule.getId()).isEqualTo("IS_PROCESS_INITIATOR");
    }
}
