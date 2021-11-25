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
package org.bonitasoft.engine.page;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Set;

import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IsAdminRuleTest {

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private SessionService sessionService;

    @Spy
    @InjectMocks
    IsAdminRule isAdminRule;

    @Test
    public void isAllowed_should_return_true_if_user_has_process_deploy_permission() throws Exception {
        // given
        final SSession session = mock(SSession.class);
        doReturn(session).when(isAdminRule).getSession();
        doReturn(Set.of(IsAdminRule.PROCESS_DEPLOY_PERMISSION)).when(session).getUserPermissions();

        // when
        final boolean allowed = isAdminRule.isAllowed("key", new HashMap<>());

        // then
        assertThat(allowed).isTrue();
    }

    @Test
    public void isAllowed_should_return_false_if_no_permissions_in_session() throws Exception {
        // given
        final SSession session = mock(SSession.class);
        doReturn(session).when(isAdminRule).getSession();
        doReturn(emptySet()).when(session).getUserPermissions();

        // when
        final boolean allowed = isAdminRule.isAllowed("key", new HashMap<>());

        // then
        assertThat(allowed).isFalse();
    }

    @Test
    public void getIdShouldReturnIsAdmin() {
        assertThat(isAdminRule.getId()).isEqualTo("IS_ADMIN");
    }
}
