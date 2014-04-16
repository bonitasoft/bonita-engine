/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.BonitaTestRunner;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.platform.TenantCreator;

/**
 * @author Baptiste Mesta
 */
@RunWith(BonitaTestRunner.class)
@Initializer(TestsInitializerSP.class)
public class TenantMaintenanceLocalTestSP extends CommonAPISPTest {

    private static final String TECH_USER = "install";

    @Test
    public void should_pause_tenant_then_stop_start_node_dont_restart_elements() throws Exception {
        // given: 1 tenant that is paused
        PlatformSession loginPlatform = loginPlatform();
        PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(loginPlatform);

        final long tenantId = platformAPI.createTenant(new TenantCreator("MyTenant_" + System.currentTimeMillis(), "desc", "icon", "iconPath", TECH_USER,
                TECH_USER));
        platformAPI.activateTenant(tenantId);
        logoutPlatform(loginPlatform);

        loginWith(TECH_USER, TECH_USER, tenantId);

        WorkService workService = getTenantAccessor(tenantId).getWorkService();
        assertFalse(workService.isStopped());
        getTenantManagementAPI().pause();
        assertTrue(workService.isStopped());
        logout();

        // when: we stop and start the node
        loginPlatform = loginPlatform();
        platformAPI = PlatformAPIAccessor.getPlatformAPI(loginPlatform);
        platformAPI.stopNode();
        platformAPI.startNode();
        logoutPlatform(loginPlatform);

        // then: work service is not runnning
        workService = getTenantAccessor(tenantId).getWorkService();
        assertTrue(workService.isStopped());

        // cleanup
        loginWith(TECH_USER, TECH_USER, tenantId);
        getTenantManagementAPI().resume();
        logout();
    }

    protected TenantServiceAccessor getTenantAccessor(final long tenantId) {
        return TenantServiceSingleton.getInstance(tenantId);
    }

}
