/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.tenant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.BonitaTestRunner;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.TestsInitializerSP;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
@RunWith(BonitaTestRunner.class)
@Initializer(TestsInitializerSP.class)
public class TenantMaintenanceLocalIT extends CommonAPISPTest {

    @Test
    public void should_pause_tenant_then_stop_start_node_dont_restart_elements() throws Exception {
        // given: 1 tenant that is paused
        final long tenantId = createAndActivateTenant("MyTenant_");
        loginOnTenantWithTechnicalLogger(tenantId);

        WorkService workService = getTenantAccessor(tenantId).getWorkService();
        assertFalse(workService.isStopped());
        getTenantManagementAPI().pause();
        assertTrue(workService.isStopped());
        logout();

        // when: we stop and start the node
        stopAndStartPlatform();

        // then: work service is not runnning
        workService = getTenantAccessor(tenantId).getWorkService();
        assertTrue(workService.isStopped());

        // cleanup
        loginOnTenantWithTechnicalLogger(tenantId);
        getTenantManagementAPI().resume();
        logout();
    }

    protected TenantServiceAccessor getTenantAccessor(final long tenantId) {
        return TenantServiceSingleton.getInstance(tenantId);
    }

}
