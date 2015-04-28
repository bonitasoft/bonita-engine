/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.tenant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.LocalServerTestsInitializer;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.test.runner.BonitaTestRunner;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
@RunWith(BonitaTestRunner.class)
@Initializer(LocalServerTestsInitializer.class)
public class TenantMaintenanceLocalIT extends TestWithUser {

    @Test
    public void should_pause_tenant_then_stop_start_node_dont_restart_elements_but_resume_restart_them() throws Exception {
        // given: tenant is paused
        long tenantId = getSession().getTenantId();
        WorkService workService = getTenantAccessor(tenantId).getWorkService();
        assertFalse(workService.isStopped());

        logoutThenloginAs(USERNAME, PASSWORD);

        final ProcessDefinitionBuilder pdb = new ProcessDefinitionBuilder().createNewInstance("loop process def", "1.0");
        pdb.addAutomaticTask("step1").addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(100));
        final DesignProcessDefinition dpd = pdb.done();
        final ProcessDefinition pd = deployAndEnableProcess(dpd);
        ProcessInstance processInstance = getProcessAPI().startProcess(pd.getId());
        logoutThenlogin();

        getTenantAdministrationAPI().pause();
        assertTrue(workService.isStopped());
        logoutOnTenant();

        // when: we stop and start the node
        stopAndStartPlatform();

        // then: work service is not running
        workService = getTenantAccessor(tenantId).getWorkService();
        assertTrue(workService.isStopped());

        // cleanup
        loginOnDefaultTenantWithDefaultTechnicalUser();
        getTenantAdministrationAPI().resume();

        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(pd);
    }

    protected TenantServiceAccessor getTenantAccessor(final long tenantId) {
        return TenantServiceSingleton.getInstance(tenantId);
    }

}
