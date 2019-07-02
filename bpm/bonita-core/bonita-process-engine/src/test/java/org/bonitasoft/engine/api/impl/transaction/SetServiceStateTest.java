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

import static java.util.Arrays.asList;
import static org.bonitasoft.engine.api.impl.transaction.SetServiceState.ServiceAction.PAUSE;
import static org.bonitasoft.engine.api.impl.transaction.SetServiceState.ServiceAction.RESUME;
import static org.bonitasoft.engine.api.impl.transaction.SetServiceState.ServiceAction.START;
import static org.bonitasoft.engine.api.impl.transaction.SetServiceState.ServiceAction.STOP;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import org.bonitasoft.engine.api.impl.TenantConfiguration;
import org.bonitasoft.engine.api.impl.transaction.SetServiceState.ServiceAction;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
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
    private TenantConfiguration tenantConfiguration;
    @Mock
    private TenantLifecycleService tenantService1;
    @Mock
    private TenantLifecycleService tenantService2;
    @Mock
    private ClassLoaderService classLoaderService;
    public static final long TENANT_ID = 635434L;

    @Before
    public void before() throws Exception {
        doReturn(tenantServiceAccessor).when(platformServiceAccessor).getTenantServiceAccessor(anyLong());

        when(tenantServiceAccessor.getClassLoaderService()).thenReturn(classLoaderService);
        when(tenantServiceAccessor.getTenantConfiguration()).thenReturn(tenantConfiguration);
        when(tenantConfiguration.getLifecycleServices()).thenReturn(asList(tenantService1, tenantService2));
        when(tenantServiceAccessor.getTechnicalLoggerService()).thenReturn(mock(TechnicalLoggerService.class));
    }

    private SetServiceState createService(ServiceAction action) throws Exception {
        SetServiceState setServiceState = spy(new SetServiceState(TENANT_ID, action));
        doReturn(platformServiceAccessor).when(setServiceState).getPlatformAccessor();
        return setServiceState;
    }


    @Test
    public void should_not_refresh_classloaders_on_start() throws Exception {
        // when:
        createService(START).call();

        // then:
        verify(classLoaderService).getLocalClassLoader(ScopeType.TENANT.name(), TENANT_ID);
        verifyNoMoreInteractions(classLoaderService);
    }

    @Test
    public void should_not_get_classloader_on_pause_and_stop() throws Exception {
        createService(PAUSE).call();
        createService(STOP).call();

        verifyZeroInteractions(classLoaderService);
    }

    @Test
    public void should_call_start_on_tenant_services() throws Exception {
        createService(START).call();

        verify(tenantService1).start();
        verify(tenantService2).start();
    }
    @Test
    public void should_call_stop_on_tenant_services() throws Exception {
        createService(STOP).call();

        verify(tenantService1).stop();
        verify(tenantService2).stop();
    }
    @Test
    public void should_call_resume_on_tenant_services() throws Exception {
        createService(RESUME).call();

        verify(tenantService1).resume();
        verify(tenantService2).resume();
    }
    @Test
    public void should_call_pause_on_tenant_services() throws Exception {
        createService(PAUSE).call();

        verify(tenantService1).pause();
        verify(tenantService2).pause();
    }
}
