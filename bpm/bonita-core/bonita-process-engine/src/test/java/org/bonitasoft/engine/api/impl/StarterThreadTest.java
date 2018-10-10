/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.api.impl;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.bonitasoft.engine.execution.work.TenantRestartHandler;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.model.impl.STenantImpl;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class StarterThreadTest {

    @Mock
    private PlatformServiceAccessor platformAccessor;
    @Mock
    private NodeConfiguration platformConfiguration;
    private final STenantImpl tenant = createTenant();

    private STenantImpl createTenant() {
        STenantImpl sTenant = new STenantImpl("tenant1", "system", 12345, "ACTIVATED", true);
        sTenant.setId(1L);
        return sTenant;
    }

    @Mock
    private TenantServiceAccessor tenantServiceAccessor;
    @Mock
    private TenantRestartHandler tenantRestartHandler1;
    @Mock
    private TenantRestartHandler tenantRestartHandler2;
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private SessionService sessionService;
    @Mock
    private TechnicalLoggerService technicalLoggerService;

    private StarterThread starterThread;

    @Before
    public void before() throws Exception {
        starterThread = spy(new StarterThread(platformAccessor, tenantServiceAccessor, Arrays.asList(tenantRestartHandler1, tenantRestartHandler2)));
        doReturn(tenantServiceAccessor).when(platformAccessor).getTenantServiceAccessor(1L);
        doReturn(sessionService).when(tenantServiceAccessor).getSessionService();
        doReturn(SSession.builder().id(54L).tenantId(1).userName("SYSTEM").userId(12).build()).when(sessionService).createSession(anyLong(), anyString());
        doReturn(tenant).when(starterThread).getTenant(1L);
        doReturn(1L).when(tenantServiceAccessor).getTenantId();
        doReturn(technicalLoggerService).when(tenantServiceAccessor).getTechnicalLoggerService();
        doReturn(sessionAccessor).when(tenantServiceAccessor).getSessionAccessor();
    }

    @Test
    public void should_call_all_restart_handlers() throws Exception {
        //given
        tenant.setStatus("ACTIVATED");
        //when
        starterThread.run();
        //then
        verify(tenantRestartHandler1).afterServicesStart(platformAccessor, tenantServiceAccessor);
        verify(tenantRestartHandler2).afterServicesStart(platformAccessor, tenantServiceAccessor);
    }

    @Test
    public void should_not_call_restart_handlers_on_paused_tenant() throws Exception {
        //given
        tenant.setStatus("PAUSED");
        //when
        starterThread.run();
        //then
        verify(tenantRestartHandler1, never()).afterServicesStart(platformAccessor, tenantServiceAccessor);
        verify(tenantRestartHandler2, never()).afterServicesStart(platformAccessor, tenantServiceAccessor);
    }

    @Test
    public void should_not_call_restart_handlers_on_deactivated_tenant() throws Exception {
        //given
        tenant.setStatus("DEACTIVATED");
        //when
        starterThread.run();
        //then
        verify(tenantRestartHandler1, never()).afterServicesStart(platformAccessor, tenantServiceAccessor);
        verify(tenantRestartHandler2, never()).afterServicesStart(platformAccessor, tenantServiceAccessor);
    }

}
