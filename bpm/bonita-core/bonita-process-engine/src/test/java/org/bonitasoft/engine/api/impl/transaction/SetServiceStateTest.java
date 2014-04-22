package org.bonitasoft.engine.api.impl.transaction;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.api.impl.TenantConfiguration;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Test;

public class SetServiceStateTest {

    @Test
    public void callShouldRefreshClassloaderOnCurrentTenant() throws Exception {
        // given:
        long tenantId = 635434L;
        SetServiceState setServiceState = spy(new SetServiceState(tenantId, mock(ServiceStrategy.class)));
        PlatformServiceAccessor platformAccessor = mock(PlatformServiceAccessor.class);
        TenantServiceAccessor tenantAccessor = mock(TenantServiceAccessor.class);
        DependencyService dependencyService = mock(DependencyService.class);
        doReturn(platformAccessor).when(setServiceState).getPlatformAccessor();
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
}
