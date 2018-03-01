/**
 * Copyright (C) 2017 Bonitasoft S.A.
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

package org.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.api.impl.ProcessInvolvementDelegate;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.IdentityService;
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
 * @author Danila Mazour
 */
@RunWith(MockitoJUnitRunner.class)
public class IsManagerOfUserInvolvedInProcessInstanceRuleTest extends RuleTest {

    private static final Long PROCESS_INSTANCE_ID = 1111L;
    private static final Long LOGGED_USER_ID = 2222L;
    private Map<String, Serializable> context;

    @Mock
    SessionService sessionService;

    @Mock
    SessionAccessor sessionAccessor;

    @Mock
    IdentityService identityService;

    @Mock
    ProcessInvolvementDelegate processInvolvementDelegate;
    @InjectMocks
    IsManagerOfUserInvolvedInProcessInstanceRule rule;

    @Before
    public void initMocks() throws Exception {
        when(sessionAccessor.getSessionId()).thenReturn(1L);
        SSession session = mock(SSession.class);
        when(session.getUserId()).thenReturn(LOGGED_USER_ID);
        when(sessionService.getSession(1L)).thenReturn(session);

        context = buildContext(PROCESS_INSTANCE_ID, LOGGED_USER_ID);
    }

    @Test
    public void should_return_the_right_rule_id() throws Exception {
        // when:
        final String ruleId = rule.getId();

        // then:
        assertThat(ruleId).isEqualTo(AuthorizationRuleConstants.IS_MANAGER_OF_USER_INVOLVED_IN_PROCESS_INSTANCE);
    }

    @Test
    public void should_allow_manager_if_processInvolvementDelegate_returns_true() throws Exception {
        // given:
        doReturn(true).when(processInvolvementDelegate).isManagerOfUserInvolvedInProcessInstance(LOGGED_USER_ID, PROCESS_INSTANCE_ID);

        // when:
        final boolean allowed = rule.isAllowed(null, context);

        // then:
        assertThat(allowed).isTrue();
        verify(processInvolvementDelegate).isManagerOfUserInvolvedInProcessInstance(LOGGED_USER_ID, PROCESS_INSTANCE_ID);
    }

    @Test
    public void should_not_allow_manager_if_processInvolvementDelegate_returns_false() throws Exception {
        // given:
        doReturn(false).when(processInvolvementDelegate).isManagerOfUserInvolvedInProcessInstance(LOGGED_USER_ID, PROCESS_INSTANCE_ID);

        // when:
        final boolean allowed = rule.isAllowed(null, context);

        // then:
        assertThat(allowed).isFalse();
        verify(processInvolvementDelegate).isManagerOfUserInvolvedInProcessInstance(LOGGED_USER_ID, PROCESS_INSTANCE_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_illegal_argument_if_processInstanceId_param_not_present() throws Exception {

        rule.isAllowed(null, buildContext(null, null));
    }

    @Test(expected = SExecutionException.class)
    public void should_throw_ExecutionException_if_processInvolvementDelegate_fails() throws Exception {
        doThrow(new BonitaException("test")).when(processInvolvementDelegate).isManagerOfUserInvolvedInProcessInstance(LOGGED_USER_ID, PROCESS_INSTANCE_ID);

        rule.isAllowed(null, context);
    }
}
