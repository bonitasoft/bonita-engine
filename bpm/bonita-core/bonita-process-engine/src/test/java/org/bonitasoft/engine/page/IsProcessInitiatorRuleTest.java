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
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.impl.ProcessInvolvementDelegate;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class IsProcessInitiatorRuleTest extends RuleTest {

    public static final long PROCESS_INSTANCE_ID = 541L;
    public static final long LOGGED_USER_ID = 7L;

    @Mock
    ProcessInvolvementDelegate processInvolvementDelegate;

    @Mock
    SessionService sessionService;

    @Mock
    SessionAccessor sessionAccessor;

    @InjectMocks
    @Spy
    IsProcessInitiatorRule rule;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void initMocks() throws Exception {
        when(sessionAccessor.getSessionId()).thenReturn(1L);
        SSession session = mock(SSession.class);
        when(session.getUserId()).thenReturn(LOGGED_USER_ID);
        when(sessionService.getSession(1L)).thenReturn(session);
    }

    @Test
    public void should_not_be_allowed_if_process_instance_initiator_is_not_given_user() throws Exception {

        Map<String, Serializable> context = buildContext(PROCESS_INSTANCE_ID, null);
        final SProcessInstance processInstance = mock(SProcessInstance.class);
        doReturn(false).when(processInvolvementDelegate).isProcessOrArchivedProcessInitiator(LOGGED_USER_ID, PROCESS_INSTANCE_ID);

        final boolean allowed = rule.isAllowed("someKey", context);

        assertThat(allowed).isFalse();
    }

    @Test
    public void should_allow_if_process_instance_initiator_is_precisely_the_given_user() throws Exception {

        Map<String, Serializable> context = buildContext(PROCESS_INSTANCE_ID, null);
        final SProcessInstance processInstance = mock(SProcessInstance.class);
        doReturn(true).when(processInvolvementDelegate).isProcessOrArchivedProcessInitiator(LOGGED_USER_ID, PROCESS_INSTANCE_ID);

        final boolean allowed = rule.isAllowed("someKey", context);

        assertThat(allowed).isTrue();
    }

    @Test
    public void should_throw_exception_if_service_problem_occurs() throws Exception {
        //given
        Map<String, Serializable> context = buildContext(PROCESS_INSTANCE_ID, LOGGED_USER_ID);

        ProcessInstanceNotFoundException processInstanceNotFoundException = new ProcessInstanceNotFoundException("message");
        doThrow(processInstanceNotFoundException).when(processInvolvementDelegate).isProcessOrArchivedProcessInitiator(LOGGED_USER_ID,
                PROCESS_INSTANCE_ID);

        //expect
        expectedException.expect(SExecutionException.class);

        //when
        rule.isAllowed("exception raised", context);
    }

    @Test
    public void should_throw_exception_if_process_instance_id_is_missing() throws Exception {
        //given
        Map<String, Serializable> context = new HashMap<>();

        //expect
        expectedException.expect(IllegalArgumentException.class);

        //when
        rule.isAllowed("exception raised", context);
    }

}
