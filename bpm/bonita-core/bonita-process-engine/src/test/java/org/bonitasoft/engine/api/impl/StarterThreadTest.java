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
package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.tenant.restart.TenantRestartHandler;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class StarterThreadTest {

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    private final STenant tenant = createTenant();
    @Mock
    private UserTransactionService transactionService;
    @Mock
    private PlatformService platformService;

    @Mock
    private TenantRestartHandler tenantRestartHandler1;

    @Mock
    private TenantRestartHandler tenantRestartHandler2;
    @Mock
    private SessionAccessor sessionAccessor;
    private StarterThread starterThread;

    @Before
    public void before() throws Exception {
        doAnswer(invocation -> ((Callable) invocation.getArgument(0)).call()).when(transactionService)
                .executeInTransaction(any());
        starterThread = new StarterThread(1L, sessionAccessor, transactionService,
                platformService, Arrays.asList(tenantRestartHandler1, tenantRestartHandler2));
        doReturn(tenant).when(platformService).getTenant(1L);
    }

    private STenant createTenant() {
        STenant sTenant = new STenant("tenant1", "system", 12345, "ACTIVATED", true);
        sTenant.setId(1L);
        return sTenant;
    }

    @Test
    public void should_call_all_restart_handlers() throws Exception {
        //given
        tenant.setStatus("ACTIVATED");
        //when
        starterThread.run();
        //then
        verify(tenantRestartHandler1).afterServicesStart();
        verify(tenantRestartHandler2).afterServicesStart();
    }

    @Test
    public void should_not_call_restart_handlers_on_paused_tenant() throws Exception {
        //given
        tenant.setStatus("PAUSED");
        //when
        starterThread.run();
        //then
        verify(tenantRestartHandler1, never()).afterServicesStart();
        verify(tenantRestartHandler2, never()).afterServicesStart();
    }

    @Test
    public void should_not_call_restart_handlers_on_deactivated_tenant() throws Exception {
        //given
        tenant.setStatus("DEACTIVATED");
        //when
        starterThread.run();
        //then
        verify(tenantRestartHandler1, never()).afterServicesStart();
        verify(tenantRestartHandler2, never()).afterServicesStart();
    }

    @Test
    public void should_call_all_restart_handlers_even_when_one_handler_fails() throws Exception {
        //given
        tenant.setStatus("ACTIVATED");
        doThrow(new RuntimeException("test")).when(tenantRestartHandler1).afterServicesStart();
        //when
        starterThread.run();
        //then
        verify(tenantRestartHandler1).afterServicesStart();
        verify(tenantRestartHandler2).afterServicesStart();

        assertThat(systemOutRule.getLog())
                .contains("The Restart Handler " + tenantRestartHandler1.getClass().getName() + " failed");
        assertThat(systemOutRule.getLog()).contains("java.lang.RuntimeException: test");
    }

    @Test
    public void should_call_all_restart_handlers_even_when_one_handler_fails_with_a_runtime_exception()
            throws Exception {
        //given
        tenant.setStatus("ACTIVATED");
        doThrow(new RuntimeException("test", new Exception())).when(tenantRestartHandler1).afterServicesStart();
        //when
        starterThread.run();
        //then
        verify(tenantRestartHandler1).afterServicesStart();
        verify(tenantRestartHandler2).afterServicesStart();

        assertThat(systemOutRule.getLog())
                .contains("The Restart Handler " + tenantRestartHandler1.getClass().getName() + " failed");
        assertThat(systemOutRule.getLog()).contains("RuntimeException", "test");
    }
}
