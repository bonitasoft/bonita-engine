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
import java.util.Map;

import org.bonitasoft.engine.api.impl.TaskInvolvementDelegate;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class IsInvolvedInProcessInstanceRuleTest extends RuleTest {

    public static final long PROCESS_INSTANCE_ID = 189L;

    public static final long LOGGED_USER_ID = 7L;

    @Mock
    TaskInvolvementDelegate taskInvolvementDelegate;

    @Mock
    SessionService sessionService;

    @Mock
    SessionAccessor sessionAccessor;

    @InjectMocks
    IsInvolvedInProcessInstanceRule rule;

    @Before
    public void initMocks() throws Exception {
        when(sessionAccessor.getSessionId()).thenReturn(1L);
        SSession session = mock(SSession.class);
        when(session.getUserId()).thenReturn(LOGGED_USER_ID);
        when(sessionService.getSession(1L)).thenReturn(session);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegallArgumentIfProcessInstanceIdParamNotPresent() throws Exception {
        Map<String, Serializable> context = buildContext(null, null);

        rule.isAllowed("someKey", context);
    }

    @Test
    public void shouldNotAllowIfNotPendingOrAssignedTasks() throws Exception {
        Map<String, Serializable> context = buildContext(189L, null);

        final boolean allowed = rule.isAllowed("someKey", context);

        assertThat(allowed).isFalse();
    }

    @Test
    public void shouldAllowIfAtLeastOnePendingOrAssignedTask() throws Exception {
        Map<String, Serializable> context = buildContext(PROCESS_INSTANCE_ID, LOGGED_USER_ID);
        doReturn(true).when(taskInvolvementDelegate).hasUserPendingOrAssignedTasks(LOGGED_USER_ID, PROCESS_INSTANCE_ID);

        final boolean allowed = rule.isAllowed("someKey", context);

        assertThat(allowed).isTrue();
    }

    @Test(expected = SExecutionException.class)
    public void shouldThrowExecutionExceptionIfReadException() throws Exception {
        Map<String, Serializable> context = buildContext(PROCESS_INSTANCE_ID, LOGGED_USER_ID);
        doThrow(SExecutionException.class).when(taskInvolvementDelegate).hasUserPendingOrAssignedTasks(LOGGED_USER_ID, PROCESS_INSTANCE_ID);

        rule.isAllowed("someKey", context);
    }

    @Test
    public void getIdShouldReturnIsInvolvedInProcessInstance() throws Exception {
        assertThat(rule.getId()).isEqualTo("IS_INVOLVED_IN_PROCESS_INSTANCE");
    }

}
