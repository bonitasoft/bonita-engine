package org.bonitasoft.engine.platform;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.commons.PlatformLifecycleService;
import org.bonitasoft.engine.platform.exception.STenantActivationException;
import org.bonitasoft.engine.platform.exception.STenantDeactivationException;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.impl.SPlatformPropertiesImpl;
import org.bonitasoft.engine.tenant.TenantManager;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class PlatformManagerTest {

    private final Long TENANT_1 = 1L;
    private final Long TENANT_2 = 2L;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Mock
    private NodeConfiguration nodeConfiguration;
    @Mock
    private TransactionService transactionService;
    @Mock
    private PlatformService platformService;
    @Mock
    private TenantManager tenant1Manager;
    @Mock
    private TenantManager tenant2Manager;
    @Mock
    private PlatformLifecycleService platformLifecycleService1;
    @Mock
    private PlatformLifecycleService platformLifecycleService2;
    @Mock
    private PlatformStateProvider platformStateProvider;

    private PlatformManager platformManager;
    private STenant tenant1;
    private STenant tenant2;

    @Before
    public void before() throws Exception {
        platformManager = spy(new PlatformManager(nodeConfiguration, transactionService, platformService,
                asList(platformLifecycleService1, platformLifecycleService2), platformStateProvider));
        when(transactionService.executeInTransaction(any()))
                .thenAnswer(invocationOnMock -> ((Callable) invocationOnMock.getArgument(0)).call());
        doReturn(tenant1Manager).when(platformManager).getTenantManager(argThat(t -> t.getId() == TENANT_1));
        doReturn(tenant2Manager).when(platformManager).getTenantManager(argThat(t -> t.getId() == TENANT_2));
        doReturn(new SPlatform("1.3.0", "1.2.0", "1.1.0", "someUser", 123455)).when(platformService).getPlatform();
        doReturn(new SPlatformPropertiesImpl("1.3.0")).when(platformService).getSPlatformProperties();
        tenant1 = new STenant();
        tenant1.setId(TENANT_1);
        tenant2 = new STenant();
        tenant2.setId(TENANT_2);
        doReturn(asList(tenant1, tenant2)).when(platformService).getTenants(any());
    }

    @Test
    public void should_start_scheduler_when_starting_node() throws Exception {
        doReturn(true).when(platformStateProvider).initializeStart();

        boolean started = platformManager.start();

        verify(platformLifecycleService1).start();
        verify(platformLifecycleService2).start();
        verify(platformStateProvider).setStarted();
        assertThat(started).isTrue();
    }

    @Test
    public void should_start_platform_only_once() throws Exception {
        //doReturn(false).when(platformStateProvider).initializeStart();

        boolean started = platformManager.start();

        verifyZeroInteractions(platformLifecycleService1);
        verifyZeroInteractions(platformLifecycleService2);
        verify(platformStateProvider, never()).setStarted();
        assertThat(started).isFalse();
    }

    @Test
    public void should_stop_services_when_stopping_platform() throws Exception {
        doReturn(true).when(platformStateProvider).initializeStop();

        boolean stopped = platformManager.stop();

        verify(platformLifecycleService1).stop();
        verify(platformLifecycleService2).stop();
        verify(platformStateProvider).setStopped();
        assertThat(stopped).isTrue();
    }

    @Test
    public void should_stop_platform_only_once() throws Exception {
        //doReturn(false).when(platformStateProvider).initializeStop();

        boolean stopped = platformManager.stop();

        verifyZeroInteractions(platformLifecycleService1);
        verifyZeroInteractions(platformLifecycleService2);
        verify(platformStateProvider, never()).setStopped();
        assertThat(stopped).isFalse();
    }

    @Test
    public void should_activate_tenant_using_tenantManager() throws Exception {
        doReturn(deactivated(tenant1)).when(platformService).getTenant(TENANT_1);

        platformManager.activateTenant(TENANT_1);

        verify(tenant1Manager).activate();
    }

    @Test
    public void should_throw_exception_when_activating_already_activated_Tenant() throws Exception {
        doReturn(activated(new STenant())).when(platformService).getTenant(TENANT_1);

        assertThatThrownBy(() -> platformManager.activateTenant(TENANT_1))
                .isInstanceOf(STenantActivationException.class);
    }

    @Test
    public void should_throw_exception_when_deactivating_already_deactivated_Tenant() throws Exception {
        doReturn(deactivated(new STenant())).when(platformService).getTenant(TENANT_1);

        assertThatThrownBy(() -> platformManager.deactivateTenant(TENANT_1))
                .isInstanceOf(STenantDeactivationException.class);
    }

    @Test
    public void should_throw_not_found_when_deactivating_non_existing_tenant() throws Exception {
        doThrow(STenantNotFoundException.class).when(platformService).getTenant(TENANT_1);

        assertThatThrownBy(() -> platformManager.deactivateTenant(TENANT_1))
                .isInstanceOf(STenantNotFoundException.class);
    }

    @Test
    public void should_throw_not_found_when_activating_non_existing_tenant() throws Exception {
        doThrow(STenantNotFoundException.class).when(platformService).getTenant(TENANT_1);

        assertThatThrownBy(() -> platformManager.activateTenant(TENANT_1))
                .isInstanceOf(STenantNotFoundException.class);
    }

    @Test
    public void should_deactivate_tenant_using_tenantManager() throws Exception {
        doReturn(activated(tenant1)).when(platformService).getTenant(TENANT_1);

        platformManager.deactivateTenant(TENANT_1);

        verify(tenant1Manager).deactivate();
    }

    @Test
    public void start_should_start_platform_and_tenant_services_in_the_right_order() throws Exception {
        // given:
        doReturn(true).when(platformStateProvider).initializeStart();

        // when:
        platformManager.start();

        // then:
        InOrder inOrder = inOrder(platformStateProvider, tenant1Manager, tenant2Manager, platformLifecycleService1,
                platformLifecycleService2);
        inOrder.verify(platformStateProvider).initializeStart();
        inOrder.verify(platformLifecycleService1).start();
        inOrder.verify(platformLifecycleService2).start();
        inOrder.verify(platformStateProvider).setStarted();
        inOrder.verify(tenant1Manager).start();
        inOrder.verify(tenant2Manager).start();
    }

    private STenant deactivated(STenant tenant) {
        tenant.setStatus(STenant.DEACTIVATED);
        return tenant;
    }

    private STenant activated(STenant tenant) {
        tenant.setStatus(STenant.ACTIVATED);
        return tenant;
    }

}
