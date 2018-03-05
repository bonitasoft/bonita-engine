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

package org.bonitasoft.engine.execution.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class TenantRestarterTest {

    @Mock
    private PlatformServiceAccessor platformServiceAccessor;
    @Mock
    private TenantServiceAccessor tenantServiceAccessor;
    @Mock
    private NodeConfiguration nodeConfiguration;
    @Mock
    private TenantRestartHandler tenantRestartHandler1;
    @Mock
    private TenantRestartHandler tenantRestartHandler2;
    @InjectMocks
    private TenantRestarter tenantRestarter;

    @Before
    public void before() throws Exception {
        doReturn(nodeConfiguration).when(platformServiceAccessor).getPlatformConfiguration();
        doReturn(Arrays.asList(tenantRestartHandler1, tenantRestartHandler2)).when(nodeConfiguration).getTenantRestartHandlers();
    }

    @Test
    public void should_execute_beforeServicesStart_on_handler() throws Exception {
        //when
        tenantRestarter.executeBeforeServicesStart();
        //then
        verify(tenantRestartHandler1).beforeServicesStart(platformServiceAccessor, tenantServiceAccessor);
        verify(tenantRestartHandler2).beforeServicesStart(platformServiceAccessor, tenantServiceAccessor);
    }

    @Test
    public void should_return_handlers_when_beforeServicesStart_is_executed() throws Exception {
        //when
        List<TenantRestartHandler> tenantRestartHandlers = tenantRestarter.executeBeforeServicesStart();
        //then
        assertThat(tenantRestartHandlers).containsOnly(tenantRestartHandler1, tenantRestartHandler2);
    }

}
