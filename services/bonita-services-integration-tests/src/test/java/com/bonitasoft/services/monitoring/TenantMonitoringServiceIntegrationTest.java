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
    protected TenantMonitoringService getMonitoringService() throws Exception {
        return monitoringService;
    }

}
