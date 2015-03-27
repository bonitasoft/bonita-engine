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
package org.bonitasoft.engine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.bonitasoft.engine.LocalServerTestsInitializer;
import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BonitaTestRunner.class)
@BonitaSuiteRunner.Initializer(LocalServerTestsInitializer.class)
public class GatewayExecutionLocalIT extends TestWithUser {

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.GATEWAY, keywords = { "Log", "Gateway", "Failed", "Exception" }, jira = "ENGINE-1451")
    @Test
    public void exclusiveGatewayFailed() throws Exception {
            final Expression scriptExpression = new ExpressionBuilder()
                    .createGroovyScriptExpression("mycondition", "fzdfsdfsdfsdfsdf", Boolean.class.getName());
            final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
                    .createNewInstance("My_Process_with_exclusive_gateway", PROCESS_VERSION)
                    .addActor(ACTOR_NAME).addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME)
                    .addUserTask("step3", ACTOR_NAME).addGateway("gateway1", GatewayType.EXCLUSIVE).addTransition("step1", "gateway1")
                    .addTransition("gateway1", "step2", scriptExpression).addDefaultTransition("gateway1", "step3")
                    .getProcess();

            final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
            final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
            assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

            final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
            final FlowNodeInstance failFlowNodeInstance = waitForFlowNodeInFailedState(processInstance);
            assertEquals("gateway1", failFlowNodeInstance.getName());
            disableAndDeleteProcess(processDefinition);
    }

}
