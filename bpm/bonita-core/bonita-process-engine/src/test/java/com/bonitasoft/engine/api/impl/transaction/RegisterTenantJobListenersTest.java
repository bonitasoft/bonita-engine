/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.api.impl.transaction;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.api.impl.TenantConfiguration;
import org.bonitasoft.engine.api.impl.scheduler.TenantJobListenerManager;
import org.bonitasoft.engine.scheduler.AbstractBonitaTenantJobListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RegisterTenantJobListenersTest {

    @Mock
    private TenantJobListenerManager manager;

    @Mock
    private PlatformServiceAccessor platformServiceAccessor;

    @Mock
    private TenantServiceAccessor tenantServiceAccessor;

    @Mock
    private TenantConfiguration tenantConfiguration;

    private RegisterTenantJobListeners register;

    private long TENANT_ID = 5L;

    @Before
    public void setUp() throws Exception {
        register = spy(new RegisterTenantJobListeners(TENANT_ID));
        doReturn(platformServiceAccessor).when(register).getPlatformAccessor();
        doReturn(tenantServiceAccessor).when(platformServiceAccessor).getTenantServiceAccessor(TENANT_ID);
        given(tenantServiceAccessor.getTenantConfiguration()).willReturn(tenantConfiguration);
        doReturn(manager).when(register).getTenantJobListenerManager(tenantServiceAccessor);
    }

    @Test
    public void call_should_call_tenantJobListenerManager_register() throws Exception {
        //given
        List<AbstractBonitaTenantJobListener> listeners = Arrays.asList(mock(AbstractBonitaTenantJobListener.class));
        given(tenantConfiguration.getJobListeners()).willReturn(listeners);

        //when
        register.call();

        //then
        verify(manager).registerListeners(listeners, TENANT_ID);
    }
}