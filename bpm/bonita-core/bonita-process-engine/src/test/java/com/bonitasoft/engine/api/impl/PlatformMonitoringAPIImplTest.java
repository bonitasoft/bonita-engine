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
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.monitoring.PlatformMonitoringService;
import com.bonitasoft.engine.service.impl.LicenseChecker;
import com.bonitasoft.manager.Features;

@RunWith(MockitoJUnitRunner.class)
public class PlatformMonitoringAPIImplTest {

    @Mock
    private LicenseChecker licenceChecker;

    @Mock
    private PlatformMonitoringService platformMonitoringService;

    private PlatformMonitoringAPIImpl monitoringAPIImpl;

    @Before
    public void setUp() throws Exception {
        monitoringAPIImpl = spy(new PlatformMonitoringAPIImpl());
        doReturn(licenceChecker).when(monitoringAPIImpl).getLicenseChecker();
        doReturn(platformMonitoringService).when(monitoringAPIImpl).getPlatformMonitoringService();

        doNothing().when(licenceChecker).checkLicenseAndFeature(Features.SERVICE_MONITORING);
    }

    @Test
    public void getNumberOfActiveTransactions_should_reurn_nb_of_active_transactions_from_monitoring_service() throws Exception {
        // given
        doReturn(6L).when(platformMonitoringService).getNumberOfActiveTransactions();

        // when
        long activeTransactions = monitoringAPIImpl.getNumberOfActiveTransactions();

        // then
        assertThat(activeTransactions).isEqualTo(6);
    }

}
