/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.services.monitoring;

import com.bonitasoft.engine.monitoring.TenantMonitoringService;

public class TenantMonitoringServiceIntegrationTest extends TenantMonitoringServiceTest {

    private static TenantMonitoringService monitoringService;

    static {
        monitoringService = getServicesBuilder().buildTenantMonitoringService();
    }

    public TenantMonitoringServiceIntegrationTest() throws Exception {
        super();
    }

    @Override
    protected TenantMonitoringService getMonitoringService() {
        return monitoringService;
    }

}
