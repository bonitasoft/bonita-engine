/*
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
 */
package org.bonitasoft.engine.test;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(BonitaEngineRunner.class)
public class TheTest {

    protected User user;
    protected UserTaskAPI userTaskAPI;
    @InjectEngine
    protected BonitaEngineTester bonitaEngineTester;
    private ProcessDeployerAPITest processDeployer;
    private APITestProcessAnalyserImpl processAnalyser;

    @Before
    public void before() throws Exception {
        userTaskAPI = bonitaEngineTester.getUserTaskAPI();
        processDeployer = bonitaEngineTester.getProcessDeployer();
        user = bonitaEngineTester.getIdentityAPI().createUser("william.jobs", "bpm");
        processAnalyser = (APITestProcessAnalyserImpl) bonitaEngineTester.getAPITestProcessAnalyser();
    }

    @After
    public void after() throws DeletionException {
        bonitaEngineTester.getIdentityAPI().deleteUser("william.jobs");
    }

    @Test
    public void aTest() throws Exception {
        ProcessAPI processAPI = bonitaEngineTester.getProcessAPI();
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.1").addActor("william.jobs")
                .addDescription("Delivery all day and night long").addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(1))
                .addUserTask("step1", "william.jobs").addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(2)).getProcess();
        final ProcessDefinition processDefinition = processDeployer.deployAndEnableProcessWithActor(processDef, "william.jobs", user);
        final ProcessDeploymentInfo processDeploymentInfo = processAPI.getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // test execution
        StartedProcess process = processAnalyser.startProcess(processDeploymentInfo.getProcessId());
        process.waitForUserTask("step1").hasOnlyActivityDataInstance("var1").hasValue(2);
        processDeployer.disableAndDeleteProcess(processDeploymentInfo.getProcessId());
    }

    @Test
    public void anotherTest() throws Exception {
    }

}
