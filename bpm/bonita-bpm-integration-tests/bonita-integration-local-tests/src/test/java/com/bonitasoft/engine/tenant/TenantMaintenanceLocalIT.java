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
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Before;
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

    protected User USER;
    
    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalLogger();
        USER = createUser(USERNAME, PASSWORD);
    }
    
    @Test
    public void should_pause_tenant_then_stop_start_node_dont_restart_elements() throws Exception {
        // given: 1 tenant that is paused
        final long tenantId = createAndActivateTenant("MyTenant_");
        loginOnTenantWithTechnicalLogger(tenantId);

        WorkService workService = getTenantAccessor(tenantId).getWorkService();
        assertFalse(workService.isStopped());

        logoutThenloginAs(USERNAME, PASSWORD);

        ProcessDefinitionBuilder pdb = new ProcessDefinitionBuilder().createNewInstance("loop process def", "1.0");
        pdb.addAutomaticTask("step1").addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(100));
        DesignProcessDefinition dpd = pdb.done();
        ProcessDefinition pd = deployAndEnableProcess(dpd);
        getProcessAPI().startProcess(pd.getId());
        logoutOnTenant();

        loginOnTenantWithTechnicalLogger(tenantId);
        
        getTenantManagementAPI().pause();
        assertTrue(workService.isStopped());
       logoutOnTenant();

        // when: we stop and start the node
        stopAndStartPlatform();

        // then: work service is not runnning
        workService = getTenantAccessor(tenantId).getWorkService();
        assertTrue(workService.isStopped());

        // cleanup
        loginOnTenantWithTechnicalLogger(tenantId);
        getTenantManagementAPI().resume();
        logoutThenloginAs(USERNAME, PASSWORD);
        disableAndDeleteProcess(pd);
        deleteUser(USER);
       logoutOnTenant();
    }

    protected TenantServiceAccessor getTenantAccessor(final long tenantId) {
        return TenantServiceSingleton.getInstance(tenantId);
    }

}
