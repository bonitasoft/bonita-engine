package org.bonitasoft.engine.api.impl.transaction;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.bonitasoft.engine.api.impl.TenantConfiguration;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Test;
import org.mockito.InOrder;

public class SetServiceStateTest {

    @Test
    public void callShouldRefreshClassloaderOnCurrentTenant() throws Exception {
        // given:
        long tenantId = 635434L;
        ServiceStrategy mock = mock(ServiceStrategy.class);
        doReturn(true).when(mock).shouldRefreshClassLoaders();
        SetServiceState setServiceState = spy(new SetServiceState(tenantId, mock));
        PlatformServiceAccessor platformAccessor = mock(PlatformServiceAccessor.class);
        TenantServiceAccessor tenantAccessor = mock(TenantServiceAccessor.class);
        DependencyService dependencyService = mock(DependencyService.class);
        doReturn(platformAccessor).when(setServiceState).getPlatformAccessor();
        doNothing().when(setServiceState).refreshClassloaderOfProcessDefinitions(tenantAccessor);
        when(platformAccessor.getTenantServiceAccessor(tenantId)).thenReturn(tenantAccessor);

        // Usefull only for test / mock purposes:
        when(tenantAccessor.getClassLoaderService()).thenReturn(mock(ClassLoaderService.class));
        when(tenantAccessor.getTenantConfiguration()).thenReturn(mock(TenantConfiguration.class));
        when(tenantAccessor.getTechnicalLoggerService()).thenReturn(mock(TechnicalLoggerService.class));
        when(tenantAccessor.getDependencyService()).thenReturn(dependencyService);

        // when:
        setServiceState.call();

        // then:
        verify(dependencyService).refreshClassLoader(ScopeType.TENANT, tenantId);
    }

    @Test
    public void callShouldRefreshClassloaderOfProcesses() throws Exception {
        // given:
        long tenantId = 635434L;
        ServiceStrategy mock = mock(ServiceStrategy.class);
        doReturn(true).when(mock).shouldRefreshClassLoaders();
        SetServiceState setServiceState = spy(new SetServiceState(tenantId, mock));
        PlatformServiceAccessor platformAccessor = mock(PlatformServiceAccessor.class);
        TenantServiceAccessor tenantAccessor = mock(TenantServiceAccessor.class);
        DependencyService dependencyService = mock(DependencyService.class);
        ProcessDefinitionService processDefinitionService = mock(ProcessDefinitionService.class);
        doReturn(platformAccessor).when(setServiceState).getPlatformAccessor();
        when(platformAccessor.getTenantServiceAccessor(tenantId)).thenReturn(tenantAccessor);
        InOrder order = inOrder(dependencyService);

        // Usefull only for test / mock purposes:
        when(tenantAccessor.getClassLoaderService()).thenReturn(mock(ClassLoaderService.class));
        when(tenantAccessor.getTenantConfiguration()).thenReturn(mock(TenantConfiguration.class));
        when(tenantAccessor.getTechnicalLoggerService()).thenReturn(mock(TechnicalLoggerService.class));
        when(tenantAccessor.getDependencyService()).thenReturn(dependencyService);
        when(tenantAccessor.getProcessDefinitionService()).thenReturn(processDefinitionService);
        doReturn(Arrays.asList(1L, 2L)).when(processDefinitionService).getProcessDefinitionIds(anyInt(), anyInt());

        // when:
        setServiceState.call();

        // then:
        order.verify(dependencyService).refreshClassLoader(ScopeType.PROCESS, 1L);
        order.verify(dependencyService).refreshClassLoader(ScopeType.PROCESS, 2L);
    }
}
