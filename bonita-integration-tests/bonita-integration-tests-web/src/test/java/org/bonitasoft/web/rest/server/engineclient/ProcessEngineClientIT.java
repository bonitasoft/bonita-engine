/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.engineclient;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.test.toolkit.bpm.TestProcess;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.junit.Test;

/**
 * @author Colin PUY
 */
public class ProcessEngineClientIT extends AbstractConsoleTest {

    private ProcessEngineClient processEngineClient;

    @Override
    public void consoleTestSetUp() throws Exception {
        processEngineClient = new ProcessEngineClient(TenantAPIAccessor.getProcessAPI(getInitiator().getSession()));
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    private ProcessDefinition getProcessDefinition(final long processId) throws Exception {
        try {
            return TenantAPIAccessor.getProcessAPI(getInitiator().getSession()).getProcessDefinition(processId);
        } catch (final ProcessDefinitionNotFoundException e) {
            return null;
        }
    }

    @Test
    public void testCountResolvedProcesses() throws Exception {
        final long before = processEngineClient.countResolvedProcesses();
        create2resolvedProcesses();

        final long resolvedProcesses = processEngineClient.countResolvedProcesses();

        assertEquals(2L, resolvedProcesses - before);
    }

    private void create2resolvedProcesses() {
        TestProcessFactory.createRandomResolvedProcess(getInitiator());
        TestProcessFactory.createRandomResolvedProcess(getInitiator());
    }

    @Test
    public void testDeleteProcesses() throws Exception {
        final TestProcess deployedProcess = TestProcessFactory.getDefaultHumanTaskProcess().addActor(getInitiator());

        processEngineClient.deleteDisabledProcesses(asList(deployedProcess.getId()));

        assertNull(getProcessDefinition(deployedProcess.getId()));
        TestProcessFactory.getInstance().remove(deployedProcess);
    }

    @Test(expected = APIItemNotFoundException.class)
    public void getProcessDeploymentInfo_return_api_not_found_exception_if_process_is_not_found() throws Exception {
        final long unknownProcessId = 1L;

        processEngineClient.getProcessDeploymentInfo(unknownProcessId);
    }

    @Test
    public void getProcessDeploymentInfo_return_info_if_process_is_found() throws Exception {
        final TestProcess deployedProcess = TestProcessFactory.getDefaultHumanTaskProcess().addActor(getInitiator());

        final ProcessDeploymentInfo processDeploymentInfo = processEngineClient
                .getProcessDeploymentInfo(deployedProcess.getId());

        assertNotNull(processDeploymentInfo);
    }
}
