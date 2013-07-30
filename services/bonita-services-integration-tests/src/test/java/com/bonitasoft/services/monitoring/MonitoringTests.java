package com.bonitasoft.services.monitoring;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        TenantMonitoringServiceWithCacheIntegrationTest.class,
        TenantMonitoringServiceWithoutCacheIntegrationTest.class,

        // Beans
        SEntityMXBeanTest.class,
        SJvmMXBeanTest.class,
        SServiceMXBeanTest.class,
        SPlatformServiceMXBeanTest.class
})
public class MonitoringTests {

}
