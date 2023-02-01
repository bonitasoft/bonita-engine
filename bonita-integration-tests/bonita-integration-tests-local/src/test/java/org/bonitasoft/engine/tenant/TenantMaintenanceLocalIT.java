/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class TenantMaintenanceLocalIT extends TestWithUser {

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Test
    public void should_pause_tenant_then_stop_start_node_start_pageService_again_but_dont_restart_elements_but_resume_restart_them()
            throws Exception {
        // given: tenant is paused
        WorkService workService = getTenantAccessor().getWorkService();
        assertFalse(workService.isStopped());

        logoutThenloginAs(USERNAME, PASSWORD);

        final ProcessDefinitionBuilder pdb = new ProcessDefinitionBuilder().createNewInstance("loop process def",
                "1.0");
        pdb.addAutomaticTask("step1").addMultiInstance(false,
                new ExpressionBuilder().createConstantIntegerExpression(100));
        final DesignProcessDefinition dpd = pdb.done();
        final ProcessDefinition pd = deployAndEnableProcess(dpd);
        ProcessInstance processInstance = getProcessAPI().startProcess(pd.getId());
        logoutThenlogin();

        getTenantAdministrationAPI().pause();
        assertTrue(workService.isStopped());
        logoutOnTenant();

        // clear logs before restarting the node
        systemOutRule.clearLog();

        // when: we stop and start the node
        stopAndStartPlatform();

        // assert the page service has been restarted even though the tenant is paused, and that provided pages have been re-imported
        assertTrue(systemOutRule.getLog().contains("Import of Bonita mandatory pages completed"));
        assertTrue(systemOutRule.getLog().contains("Import of Bonita default pages completed"));
        // then: work service is not running
        workService = getTenantAccessor().getWorkService();
        assertTrue(workService.isStopped());

        // cleanup
        loginOnDefaultTenantWithDefaultTechnicalUser();
        getTenantAdministrationAPI().resume();

        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(pd);
    }

    protected TenantServiceAccessor getTenantAccessor() {
        return TenantServiceSingleton.getInstance();
    }

}
