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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.TenantStatusException;
import org.junit.Test;

/**
 * @author Laurent Leseigneur
 * @author Celine Souchet
 */
public class TenantMaintenanceIT extends TestWithUser {

    @Test
    public void should_be_able_to_start_process_after_pause_resume() throws Exception {
        //given
        final ProcessDefinition processDefinition = createProcessOnTenant();

        //when
        logoutThenlogin();
        pauseTenant();

        //then
        assertCannotLoginOnTenant();

        //when
        resumeTenant();

        // then
        assertCanLoginOnTenantAndStartProcess(processDefinition);

        // cleanup
        loginOnDefaultTenantWithDefaultTechnicalUser();
        disableAndDeleteProcess(processDefinition.getId());
    }

    private void assertCannotLoginOnTenant() throws Exception{
        try {
            loginOnDefaultTenantWith(USERNAME, PASSWORD);
            fail("Expected that user is not able to do login, but he is");
        } catch (Exception e) {
            assertThat(e.getCause()).isInstanceOf(TenantStatusException.class);
            assertThat(e.getCause().getMessage()).contains("in pause");
        }
    }

    private void assertCanLoginOnTenantAndStartProcess(final ProcessDefinition processDefinition) throws Exception{
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "step1");
        logoutOnTenant();
    }

    private ProcessDefinition createProcessOnTenant() throws Exception {
        final String processName = new StringBuilder().append(PROCESS_NAME).toString();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance(processName,
                        PROCESS_VERSION)
                .addActor(ACTOR_NAME)
                .addStartEvent("start event")
                .addUserTask("step1", ACTOR_NAME)
                .addEndEvent("end event").getProcess();

        return deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, getSession().getUserId());
    }

    private void pauseTenant() throws BonitaException {
        getTenantAdministrationAPI().pause();
    }

    private void resumeTenant() throws BonitaException {
        getTenantAdministrationAPI().resume();
    }
}
