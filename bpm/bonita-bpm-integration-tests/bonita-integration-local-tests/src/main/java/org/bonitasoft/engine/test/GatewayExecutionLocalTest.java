package org.bonitasoft.engine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GatewayExecutionLocalTest extends CommonAPITest {

    private User user;

    @After
    public void afterTest() throws BonitaException {
        deleteUser(user);
       logoutOnTenant();
    }

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        user = createUser(USERNAME, PASSWORD);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.GATEWAY, keywords = { "Log", "Gateway", "Failed", "Exception" }, jira = "ENGINE-1451")
    @Test
    public void exclusiveGatewayFailed() throws Exception {
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));
        try {
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
        } finally {
            System.setOut(stdout);
        }
        final String logs = myOut.toString();
        System.out.println(logs);
        assertTrue(
                "Should have written in logs : SExpressionEvaluationException",
                logs.contains("Expression mycondition with content = <fzdfsdfsdfsdfsdf> depends on fzdfsdfsdfsdfsdf is neither defined in the script nor in dependencies."));
    }

}
