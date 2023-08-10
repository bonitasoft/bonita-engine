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
package org.bonitasoft.engine.platform;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.PlatformLifecycleService;
import org.bonitasoft.engine.commons.PlatformRestartHandler;
import org.bonitasoft.engine.platform.configuration.NodeConfiguration;
import org.bonitasoft.engine.platform.exception.STenantActivationException;
import org.bonitasoft.engine.platform.exception.STenantDeactivationException;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.impl.SPlatformPropertiesImpl;
import org.bonitasoft.engine.service.BonitaTaskExecutor;
import org.bonitasoft.engine.service.RunnableWithException;
import org.bonitasoft.engine.tenant.TenantStateManager;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class PlatformManagerTest {

    private final Long TENANT_ID = 1L;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();
    @Rule
    public RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Mock
    private NodeConfiguration nodeConfiguration;
    @Mock
    private TransactionService transactionService;
    @Mock
    private PlatformService platformService;
    @Mock
    private TenantStateManager tenantManager;
    @Mock
    private PlatformLifecycleService platformLifecycleService1;
    @Mock
    private PlatformLifecycleService platformLifecycleService2;
    @Mock
    private PlatformStateProvider platformStateProvider;
    @Mock
    private BonitaTaskExecutor bonitaTaskExecutor;
    @Mock
    private PlatformVersionChecker platformVersionChecker;

    private PlatformManager platformManager;
    private STenant tenant;
    @Mock
    private PlatformRestartHandler platformRestartHandler1;
    @Mock
    private PlatformRestartHandler platformRestartHandler2;

    @Before
    public void before() throws Exception {
        doReturn(asList(platformRestartHandler1, platformRestartHandler2)).when(nodeConfiguration)
                .getPlatformRestartHandlers();
        platformManager = spy(new PlatformManager(nodeConfiguration, transactionService, platformService,
                asList(platformLifecycleService1, platformLifecycleService2), platformStateProvider,
                bonitaTaskExecutor, platformVersionChecker));
        when(transactionService.executeInTransaction(any()))
                .thenAnswer(invocationOnMock -> ((Callable) invocationOnMock.getArgument(0)).call());
        when(bonitaTaskExecutor.execute(any(RunnableWithException.class)))
                .thenAnswer(invocationOnMock -> {
                    ((RunnableWithException) invocationOnMock.getArgument(0)).run();
                    return null;
                });
        doReturn(tenantManager).when(platformManager).getDefaultTenantStateManager();
        doReturn(new SPlatform("1.3", "1.1.0", "someUser", 123455, "0.0.0")).when(platformService).getPlatform();
        doReturn(new SPlatformPropertiesImpl("1.3.0")).when(platformService).getSPlatformProperties();
        tenant = new STenant();
        tenant.setId(TENANT_ID);
        doReturn(tenant).when(platformService).getDefaultTenant();
        doReturn(true).when(platformVersionChecker).verifyPlatformVersion();
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
    public void start_should_execute_platform_restart_handlers_in_an_other_thread() throws Exception {
        doReturn(true).when(platformStateProvider).initializeStart();

        platformManager.start();

        verify(platformRestartHandler1).execute();
        verify(platformRestartHandler2).execute();
        verify(bonitaTaskExecutor, times(2)).execute(any(RunnableWithException.class));
    }

    @Test
    public void should_activate_tenant_using_tenantManager() throws Exception {
        doReturn(deactivated(tenant)).when(platformService).getDefaultTenant();

        platformManager.activateTenant();

        verify(tenantManager).activate();
    }

    @Test
    public void should_throw_exception_when_activating_already_activated_Tenant() throws Exception {
        doReturn(activated(new STenant())).when(platformService).getDefaultTenant();

        assertThatThrownBy(() -> platformManager.activateTenant())
                .isInstanceOf(STenantActivationException.class);
    }

    @Test
    public void should_throw_exception_when_deactivating_already_deactivated_Tenant() throws Exception {
        doReturn(deactivated(new STenant())).when(platformService).getDefaultTenant();

        assertThatThrownBy(() -> platformManager.deactivateTenant())
                .isInstanceOf(STenantDeactivationException.class);
    }

    @Test
    public void should_throw_not_found_when_deactivating_non_existing_tenant() throws Exception {
        doThrow(STenantNotFoundException.class).when(platformService).getDefaultTenant();

        assertThatThrownBy(() -> platformManager.deactivateTenant())
                .isInstanceOf(STenantNotFoundException.class);
    }

    @Test
    public void should_throw_not_found_when_activating_non_existing_tenant() throws Exception {
        doThrow(STenantNotFoundException.class).when(platformService).getDefaultTenant();

        assertThatThrownBy(() -> platformManager.activateTenant())
                .isInstanceOf(STenantNotFoundException.class);
    }

    @Test
    public void should_deactivate_tenant_using_tenantManager() throws Exception {
        doReturn(activated(tenant)).when(platformService).getDefaultTenant();

        platformManager.deactivateTenant();

        verify(tenantManager).deactivate();
    }

    @Test
    public void start_should_start_platform_and_tenant_services_in_the_right_order() throws Exception {
        // given:
        doReturn(true).when(platformStateProvider).initializeStart();

        // when:
        platformManager.start();

        // then:
        InOrder inOrder = inOrder(platformStateProvider, tenantManager, platformLifecycleService1,
                platformLifecycleService2, platformRestartHandler1, platformRestartHandler2);
        inOrder.verify(platformStateProvider).initializeStart();
        inOrder.verify(platformLifecycleService1).start();
        inOrder.verify(platformLifecycleService2).start();
        inOrder.verify(platformStateProvider).setStarted();
        inOrder.verify(tenantManager).start();
        inOrder.verify(platformRestartHandler1).execute();
        inOrder.verify(platformRestartHandler2).execute();
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
