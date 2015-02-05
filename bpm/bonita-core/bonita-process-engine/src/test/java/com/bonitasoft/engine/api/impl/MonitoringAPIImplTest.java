/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.exception.BonitaException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.monitoring.TenantMonitoringService;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.manager.Features;

@RunWith(MockitoJUnitRunner.class)
public class MonitoringAPIImplTest {

    @Mock
    private TenantMonitoringService monitoringService;

    @Mock
    private TenantServiceAccessor tenantServiceAccessor;

    @Spy
    private final MonitoringAPIImpl monitoringAPI = new MonitoringAPIImpl();

    @Test
    public void getNumberOfActiveTransactions() throws BonitaException {
        doNothing().when(monitoringAPI).checkLicenceAndFeature(Features.SERVICE_MONITORING);
        doReturn(tenantServiceAccessor).when(monitoringAPI).getTenantServiceAccessor();
        when(tenantServiceAccessor.getTenantMonitoringService()).thenReturn(monitoringService);
        when(monitoringService.getNumberOfActiveTransactions()).thenReturn(0L);

        final long numberOfActiveTransactions = monitoringAPI.getNumberOfActiveTransactions();
        assertThat(numberOfActiveTransactions).isEqualTo(0L);
    }

}
