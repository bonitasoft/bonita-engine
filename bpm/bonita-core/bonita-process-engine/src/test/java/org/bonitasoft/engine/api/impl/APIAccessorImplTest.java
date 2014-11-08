/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.ThemeAPI;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.session.SSessionException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class APIAccessorImplTest {

    private static final long TENANT_ID = 45L;

    @Mock
    private SSession session;

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private SessionService sessionService;

    private APIAccessorImpl apiAccessor;

    private void initAPIAccessor() throws SBonitaException {
        when(sessionAccessor.getTenantId()).thenReturn(TENANT_ID);
        when(sessionService.createSession(TENANT_ID, APIAccessorImpl.class.getSimpleName())).thenReturn(session);
        when(session.getId()).thenReturn(78L);

        apiAccessor = new APIAccessorImpl(sessionAccessor, sessionService);
    }

    @Test
    public void constructorAPIAccessorImpl_should_create_a_new_session() throws Exception {
        initAPIAccessor();

        verify(sessionService).createSession(TENANT_ID, APIAccessorImpl.class.getSimpleName());
        verify(sessionAccessor).setSessionInfo(78L, TENANT_ID);
    }

    @Test(expected = SBonitaRuntimeException.class)
    public void constructorAPIAccessorImpl_should_throw_an_exception_if_one_occurs_during_session_creation() throws Exception {
        when(sessionAccessor.getTenantId()).thenReturn(TENANT_ID);
        when(sessionService.createSession(TENANT_ID, APIAccessorImpl.class.getSimpleName())).thenThrow(new SSessionException("error"));

        new APIAccessorImpl(sessionAccessor, sessionService);
    }

    @Test(expected = SBonitaRuntimeException.class)
    public void constructorAPIAccessorImpl_should_throw_an_exception_if_a_runtime_one_occurs_during_session_creation() throws Exception {
        when(sessionAccessor.getTenantId()).thenReturn(TENANT_ID);
        when(sessionService.createSession(TENANT_ID, APIAccessorImpl.class.getSimpleName())).thenThrow(new SBonitaRuntimeException("error"));

        try {
            new APIAccessorImpl(sessionAccessor, sessionService);
        } catch (final SBonitaRuntimeException bre) {
            assertThat(bre.getCause()).isNull();
            throw bre;
        }
    }

    @Test(expected = SBonitaRuntimeException.class)
    public void constructorAPIAccessorImpl_should_throw_an_exception_if_one_occurs_during_tenant_retrieving() throws Exception {
        when(sessionAccessor.getTenantId()).thenThrow(new STenantIdNotSetException("error"));

        new APIAccessorImpl(sessionAccessor, sessionService);
    }

    @Test
    public void getIdentityAPI_should_return_the_default_implementation() throws SBonitaException {
        initAPIAccessor();
        final IdentityAPI identityAPI = apiAccessor.getIdentityAPI();

        assertThat(identityAPI).isNotNull().isExactlyInstanceOf(IdentityAPIImpl.class);
    }

    @Test
    public void getProcessAPI_should_return_the_default_implementation() throws SBonitaException {
        initAPIAccessor();
        final ProcessAPI processAPI = apiAccessor.getProcessAPI();

        assertThat(processAPI).isNotNull().isExactlyInstanceOf(ProcessAPIImpl.class);
    }

    @Test
    public void getCommandAPI_should_return_the_default_implementation() throws SBonitaException {
        initAPIAccessor();
        final CommandAPI commandAPI = apiAccessor.getCommandAPI();

        assertThat(commandAPI).isNotNull().isExactlyInstanceOf(CommandAPIImpl.class);
    }

    @Test
    public void getProfileAPI_should_return_the_default_implementation() throws SBonitaException {
        initAPIAccessor();
        final ProfileAPI profileAPI = apiAccessor.getProfileAPI();

        assertThat(profileAPI).isNotNull().isExactlyInstanceOf(ProfileAPIImpl.class);
    }

    @Test
    public void getThemeAPI_should_return_the_default_implementation() throws SBonitaException {
        initAPIAccessor();
        final ThemeAPI themeAPI = apiAccessor.getThemeAPI();

        assertThat(themeAPI).isNotNull().isExactlyInstanceOf(ThemeAPIImpl.class);
    }

}
