package org.bonitasoft.engine.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowNodeExecutionException;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UserTaskContractTest extends CommonAPITest {

    private User matti;

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalLogger();
        matti = createUser("matti", "bpm");
        logoutOnTenant();
        loginOnDefaultTenantWith(matti.getUserName(), "bpm");
    }

    @After
    public void afterTest() throws BonitaException {
        deleteUser(matti);
        logoutOnTenant();
    }

    @Test
    public void createAContractAndGetIt() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("task1", ACTOR_NAME).addContract().addInput("numberOfDays", Integer.class.getName(), null);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask("task1");

        final ContractDefinition contract = getProcessAPI().getUserTaskContract(userTask.getId());

        assertThat(contract.getInputs()).hasSize(1);
        final InputDefinition input = contract.getInputs().get(0);
        assertThat(input.getName()).isEqualTo("numberOfDays");
        assertThat(input.getType()).isEqualTo(Integer.class.getName());
        assertThat(input.getDescription()).isNull();

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getExceptionWhenContractIsNotValidUsingDefaultMethod() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("task1", ACTOR_NAME).addContract().addInput("numberOfDays", Integer.class.getName(), null);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask("task1");
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());

        try {
            getProcessAPI().executeFlowNode(userTask.getId());
            fail("The contract is not enforced");
        } catch (final FlowNodeExecutionException e) {
            e.printStackTrace();

        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

}
