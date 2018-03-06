/**
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
 **/
package org.bonitasoft.engine.api.impl.transaction;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.bonitasoft.engine.api.impl.TenantConfiguration;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SetServiceStateTest {

    @Mock
    private PlatformServiceAccessor platformServiceAccessor;
    @Mock
    private TenantServiceAccessor tenantServiceAccessor;
    @Mock
    private DependencyService dependencyService;
    @Mock
    private ProcessDefinitionService processDefinitionService;
    @Mock
    private ServiceStrategy serviceStrategy;
    private SetServiceState setServiceState;
    public static final long TENANT_ID = 635434L;

    @Before
    public void before() throws Exception {
        doReturn(tenantServiceAccessor).when(platformServiceAccessor).getTenantServiceAccessor(anyLong());

        when(tenantServiceAccessor.getClassLoaderService()).thenReturn(mock(ClassLoaderService.class));
        when(tenantServiceAccessor.getTenantConfiguration()).thenReturn(mock(TenantConfiguration.class));
        when(tenantServiceAccessor.getTechnicalLoggerService()).thenReturn(mock(TechnicalLoggerService.class));
        when(tenantServiceAccessor.getDependencyService()).thenReturn(dependencyService);
        when(tenantServiceAccessor.getProcessDefinitionService()).thenReturn(processDefinitionService);
        setServiceState = spy(new SetServiceState(TENANT_ID, serviceStrategy));
        doReturn(platformServiceAccessor).when(setServiceState).getPlatformAccessor();
        doReturn(true).when(serviceStrategy).shouldRefreshClassLoaders();
    }

    @Test
    public void callShouldRefreshClassloaderOnCurrentTenant() throws Exception {
        // given:
        doNothing().when(setServiceState).refreshClassloaderOfProcessDefinitions(tenantServiceAccessor);

        // Usefull only for test / mock purposes:

        // when:
        setServiceState.call();

        // then:
        verify(dependencyService).refreshClassLoader(ScopeType.TENANT, TENANT_ID);
    }

    @Test
    public void callShouldRefreshClassloaderOfProcesses() throws Exception {
        // given:
        InOrder order = inOrder(dependencyService);

        // Usefull only for test / mock purposes:
        doReturn(Arrays.asList(1L, 2L)).when(processDefinitionService).getProcessDefinitionIds(anyInt(), anyInt());

        // when:
        setServiceState.call();

        // then:
        order.verify(dependencyService).refreshClassLoader(ScopeType.PROCESS, 1L);
        order.verify(dependencyService).refreshClassLoader(ScopeType.PROCESS, 2L);
    }
}
