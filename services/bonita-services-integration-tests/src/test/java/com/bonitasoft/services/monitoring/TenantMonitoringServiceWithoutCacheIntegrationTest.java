package com.bonitasoft.services.monitoring;

import com.bonitasoft.engine.monitoring.TenantMonitoringService;

public class TenantMonitoringServiceWithoutCacheIntegrationTest extends TenantMonitoringServiceTest {

    private static TenantMonitoringService monitoringService;

    static {
        monitoringService = getServicesBuilder().buildTenantMonitoringService(false);
    }

    public TenantMonitoringServiceWithoutCacheIntegrationTest() throws Exception {
        super();
    }

    @Override
    protected TenantMonitoringService getMonitoringService() throws Exception {
        return monitoringService;
    }

}
