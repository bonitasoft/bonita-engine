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
package org.bonitasoft.engine.tenant;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SLifecycleException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class TenantServicesManagerTest {

    public static final long TENANT_ID = 12L;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private TransactionService transactionService;
    @Mock
    private TenantLifecycleService tenantService1;
    @Mock
    private TenantLifecycleService tenantService2;
    @Mock
    private TenantLifecycleService tenantService3;
    @Mock
    private TenantElementsRestarter tenantElementsRestarter;
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private SessionService sessionService;
    private TenantServicesManager tenantServicesManager;
    @Mock
    private ClassLoaderService classLoaderService;

    @Before
    public void before() throws Exception {
        when(transactionService.executeInTransaction(any()))
                .thenAnswer(invocationOnMock -> ((Callable) invocationOnMock.getArgument(0)).call());
        tenantServicesManager = new TenantServicesManager(sessionAccessor, sessionService, transactionService,
                classLoaderService,
                asList(tenantService1, tenantService2, tenantService3), TENANT_ID, tenantElementsRestarter);
        doReturn(true).when(sessionAccessor).isTenantSession();
    }

    @Test
    public void should_not_refresh_classloaders_on_start() throws Exception {
        tenantServicesManager.start();

        verify(classLoaderService).getLocalClassLoader(ScopeType.TENANT.name(), TENANT_ID);
        verifyNoMoreInteractions(classLoaderService);
    }

    @Test
    public void should_not_get_classloader_on_pause_and_stop() throws Exception {
        tenantServicesManager.stop();

        verifyNoMoreInteractions(classLoaderService);
    }

    @Test
    public void start_should_start_services_and_resume_elements() throws Exception {

        tenantServicesManager.start();

        InOrder inOrder = inOrder(tenantService1, tenantService2, tenantService3, tenantElementsRestarter);
        inOrder.verify(tenantElementsRestarter).prepareRestartOfElements();
        inOrder.verify(tenantService1).start();
        inOrder.verify(tenantService2).start();
        inOrder.verify(tenantService3).start();
        inOrder.verify(tenantElementsRestarter).restartElements();
    }

    @Test
    public void resume_should_resume_services_and_resume_elements() throws Exception {

        tenantServicesManager.resume();

        InOrder inOrder = inOrder(tenantService1, tenantService2, tenantService3, tenantElementsRestarter);
        inOrder.verify(tenantElementsRestarter).prepareRestartOfElements();
        inOrder.verify(tenantService1).resume();
        inOrder.verify(tenantService2).resume();
        inOrder.verify(tenantService3).resume();
        inOrder.verify(tenantElementsRestarter).restartElements();
    }

    @Test
    public void stop_should_stop_services_in_reverse_order() throws Exception {
        tenantServicesManager.start();

        tenantServicesManager.stop();

        InOrder inOrder = inOrder(tenantService1, tenantService2, tenantService3);
        inOrder.verify(tenantService3).stop();
        inOrder.verify(tenantService2).stop();
        inOrder.verify(tenantService1).stop();
    }

    @Test
    public void pause_should_stop_services() throws Exception {
        tenantServicesManager.start();

        tenantServicesManager.pause();

        InOrder inOrder = inOrder(tenantService1, tenantService2, tenantService3);
        inOrder.verify(tenantService3).pause();
        inOrder.verify(tenantService2).pause();
        inOrder.verify(tenantService1).pause();
    }

    @Test
    public void tenant_should_be_started_after_start() throws Exception {
        tenantServicesManager.start();

        assertThat(tenantServicesManager.isStarted()).isTrue();
    }

    @Test
    public void tenant_should_be_stopped_initially() {

        assertThat(tenantServicesManager.isStarted()).isFalse();
    }

    @Test
    public void tenant_should_be_stopped_after_stop() throws Exception {
        tenantServicesManager.start();
        tenantServicesManager.stop();

        assertThat(tenantServicesManager.isStarted()).isFalse();
    }

    @Test
    public void tenant_should_be_started_after_resume() throws Exception {
        tenantServicesManager.resume();

        assertThat(tenantServicesManager.isStarted()).isTrue();
    }

    @Test
    public void tenant_should_be_stopped_after_pause() throws Exception {
        tenantServicesManager.start();
        tenantServicesManager.pause();

        assertThat(tenantServicesManager.isStarted()).isFalse();
    }

    @Test
    public void should_not_start_tenant_that_is_already_started() throws Exception {
        tenantServicesManager.start();

        tenantServicesManager.start();
        tenantServicesManager.start();

        verify(tenantService1, times(1)).start();
    }

    @Test
    public void should_not_stop_tenant_that_is_already_stopped() throws Exception {
        tenantServicesManager.start();
        tenantServicesManager.stop();

        tenantServicesManager.stop();
        tenantServicesManager.stop();

        verify(tenantService1, times(1)).stop();
    }

    @Test
    public void should_stop_services_when_one_service_fail_to_start() throws Exception {
        doThrow(new IllegalStateException("Unable to start service 2")).when(tenantService2).start();

        assertThatThrownBy(() -> {
            tenantServicesManager.start();
        }).isInstanceOf(SLifecycleException.class);

        verify(tenantService1).start();
        verify(tenantService3, never()).start();

        verify(tenantService1).stop();
        verify(tenantService2).stop();
        verify(tenantService3).stop();
    }

    @Test
    public void should_report_exception_of_service_that_fails_to_start() throws Exception {
        doThrow(new UnsupportedOperationException("Unable to start service 1")).when(tenantService1).start();
        doThrow(new IllegalStateException("Unable to start service 2")).when(tenantService2).start();

        assertThatThrownBy(() -> {
            tenantServicesManager.start();
        }).isInstanceOf(SLifecycleException.class)
                .hasMessageContaining("Unable to START a service. All services are STOPPED again")
                .hasRootCauseExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void should_not_execute_restart_of_elements_when_one_service_fails_to_start() throws Exception {
        doThrow(new IllegalStateException("Unable to start service 2")).when(tenantService2).start();

        assertThatThrownBy(() -> {
            tenantServicesManager.start();
        }).isInstanceOf(SLifecycleException.class);

        verify(tenantElementsRestarter).prepareRestartOfElements();
        verify(tenantElementsRestarter, never()).restartElements();
    }

    @Test
    public void should_be_able_to_stop_services_when_one_service_failed_to_start() throws Exception {
        doThrow(new IllegalStateException("Unable to start service 2")).when(tenantService2).start();
        assertThatThrownBy(() -> {
            tenantServicesManager.start();
        }).isInstanceOf(SLifecycleException.class);

        tenantServicesManager.stop();

        verify(tenantService1).stop();
        verify(tenantService2).stop();
        verify(tenantService3).stop();
        assertThat(tenantServicesManager.isStarted()).isFalse();
    }

    @Test
    public void should_be_able_to_restart_again_services_after_a_failure() throws Exception {
        doThrow(new IllegalStateException("Unable to start service 2")).doNothing().when(tenantService2).start();
        assertThatThrownBy(() -> {
            tenantServicesManager.start();
        }).isInstanceOf(SLifecycleException.class);
        assertThat(tenantServicesManager.isStarted()).isFalse();

        tenantServicesManager.start();

        verify(tenantService1, times(2)).start();
        verify(tenantService2, times(2)).start();
        verify(tenantService3, times(1)).start();
        assertThat(tenantServicesManager.isStarted()).isTrue();
    }

    @Test
    public void should_have_the_tenant_stopped_when_one_service_did_not_start() throws Exception {
        doThrow(new IllegalStateException("Unable to start service 2")).when(tenantService2).start();

        assertThatThrownBy(() -> {
            tenantServicesManager.start();
        }).isInstanceOf(SLifecycleException.class);

        assertThat(tenantServicesManager.isStarted()).isFalse();
    }

}
