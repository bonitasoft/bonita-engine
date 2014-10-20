package org.bonitasoft.engine.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.contract.ComplexInputDefinition;
import org.bonitasoft.engine.bpm.contract.ConstraintDefinition;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.bpm.contract.SimpleInputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ComplexInputDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.SimpleInputDefinitionImpl;
import org.bonitasoft.engine.bpm.data.ArchivedDataInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeExecutionException;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.UserTaskNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.connectors.TestConnectorWithAPICall;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UserTaskContractITest extends CommonAPITest {

    private static final String TASK2 = "task2";
    private static final String TASK1 = "task1";
    private User matti;

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
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
    public void should_getUserTaskContract_return_the_contract() throws Exception {
        //given
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask(TASK1, ACTOR_NAME).addContract().addSimpleInput("numberOfDays", Type.INTEGER, null)
                .addMandatoryConstraint("numberOfDays");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask(TASK1);

        //when
        final ContractDefinition contract = getProcessAPI().getUserTaskContract(userTask.getId());

        //then
        assertThat(contract.getSimpleInputs()).hasSize(1);
        final SimpleInputDefinition input = contract.getSimpleInputs().get(0);
        assertThat(input.getName()).isEqualTo("numberOfDays");
        assertThat(input.getType()).isEqualTo(Type.INTEGER);
        assertThat(input.getDescription()).isNull();

        assertThat(contract.getConstraints()).hasSize(1);
        final ConstraintDefinition rule = contract.getConstraints().get(0);
        assertThat(rule.getName()).isEqualTo("numberOfDays");
        assertThat(rule.getInputNames()).containsExactly("numberOfDays");

        //clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void should_getUserTaskContract_return_contract_with_complex_inputs() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        final SimpleInputDefinition expenseType = new SimpleInputDefinitionImpl("expenseType", Type.TEXT, "describe expense type");
        final SimpleInputDefinition expenseAmount = new SimpleInputDefinitionImpl("amount", Type.DECIMAL, "expense amount");
        final SimpleInputDefinition expenseDate = new SimpleInputDefinitionImpl("date", Type.DATE, "expense date");
        final ComplexInputDefinition complexSubIput = new ComplexInputDefinitionImpl("date", "expense date", Arrays.asList(expenseType), null);
        //given
        builder.addUserTask(TASK1, ACTOR_NAME).addContract()
                .addComplexInput("expenseLine", "expense report line", true, Arrays.asList(expenseDate, expenseAmount), Arrays.asList(complexSubIput));

        //when
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask(TASK1);

        //then
        final ContractDefinition contract = getProcessAPI().getUserTaskContract(userTask.getId());
        assertThat(contract.getComplexInputs()).hasSize(1);
        final ComplexInputDefinition complexInput = contract.getComplexInputs().get(0);
        assertThat(complexInput.getName()).isEqualTo("expenseLine");
        assertThat(complexInput.isMultiple()).as("should be multiple").isTrue();
        assertThat(complexInput.getDescription()).isEqualTo("expense report line");
        assertThat(complexInput.getSimpleInputs()).as("should have 2 simples inputs").hasSize(2);
        assertThat(complexInput.getComplexInputs()).as("should have 1 complex input").hasSize(1);

        //clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void should_create_a_contract_with_special_char() throws Exception {
        final long[] badValues = { 0, 366 };
        for (final long badValue : badValues) {
            check_invalid_contract_with_special_char(badValue);
        }
    }

    @Test
    public void should_execute_a_contract_with_xml_tag_in_rule() throws Exception {
        //given
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        final String expectedExplanation = "numberOfDays must between one day and one year";
        builder.addUserTask(TASK1, ACTOR_NAME).addContract().addSimpleInput("comment", Type.TEXT, null)
                .addConstraint("mandatory", "comment.equals(\"<tag>\")", expectedExplanation, "comment");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());

        //when
        final HumanTaskInstance task = waitForUserTask(TASK1);
        getProcessAPI().assignUserTask(task.getId(), matti.getId());

        //then no exceptions
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("comment", "<tag>");
        getProcessAPI().executeUserTask(task.getId(), map);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void should_execute_a_contract_with_integer_in_decimal() throws Exception {
        //given
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        final UserTaskDefinitionBuilder userTask = builder.addUserTask(TASK1, ACTOR_NAME);
        userTask.addContract().addSimpleInput("decimal", Type.DECIMAL, null);
        userTask.addData("variable", Number.class.getName(), null);
        userTask.addOperation(new OperationBuilder().createSetDataOperation("variable",
                new ExpressionBuilder().createContractInputExpression("decimal", Number.class.getName())));

        //when
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance task = waitForUserTask(TASK1);
        getProcessAPI().assignUserTask(task.getId(), matti.getId());

        //then
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("decimal", 2);
        getProcessAPI().executeUserTask(task.getId(), map);

        //clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void should_invalid_contract_throw_ContractViolationException() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask(TASK1, ACTOR_NAME).addContract().addSimpleInput("numberOfDays", Type.INTEGER, null);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask(TASK1);
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());

        try {
            getProcessAPI().executeFlowNode(userTask.getId());
            fail("The contract is not enforced");
        } catch (final FlowNodeExecutionException e) {
            final String state = getProcessAPI().getActivityInstanceState(userTask.getId());
            assertThat(state).isEqualTo("ready");
        }
        try {
            getProcessAPI().executeUserTask(userTask.getId(), new HashMap<String, Object>());
            fail("The contract is not enforced");
        } catch (final ContractViolationException e) {
            final String state = getProcessAPI().getActivityInstanceState(userTask.getId());
            assertThat(state).isEqualTo("ready");
        }
        disableAndDeleteProcess(processDefinition);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void use_a_multiple_complex_input_in_user_tasks() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        final SimpleInputDefinition expenseType = new SimpleInputDefinitionImpl("expenseType", Type.TEXT, "describe expense type");
        final SimpleInputDefinition expenseAmount = new SimpleInputDefinitionImpl("expenseAmount", Type.DECIMAL, "expense amount");
        final SimpleInputDefinition expenseDate = new SimpleInputDefinitionImpl("expenseDate", Type.DATE, "expense date");
        final SimpleInputDefinition expenseProof = new SimpleInputDefinitionImpl("expenseProof", Type.BYTE_ARRAY, "expense proof");

        //given
        final UserTaskDefinitionBuilder userTaskDefinitionBuilder = builder.addUserTask(TASK1, ACTOR_NAME);
        userTaskDefinitionBuilder.addContract()
                .addComplexInput("expenseReport", "expense report with several expense lines", true,
                        Arrays.asList(expenseType, expenseDate, expenseAmount, expenseProof),
                        null)
                .addMandatoryConstraint("expenseAmount")
                .addMandatoryConstraint("expenseReport")
                .addMandatoryConstraint("expenseDate");

        final List<Map<String, Object>> expenses = new ArrayList<Map<String, Object>>();
        userTaskDefinitionBuilder.addData("expenseData", expenses.getClass().getName(), null);
        userTaskDefinitionBuilder.addOperation(new OperationBuilder().createSetDataOperation("expenseData",
                new ExpressionBuilder().createContractInputExpression("expenseReport", expenses.getClass().getName())));
        builder.addUserTask(TASK2, ACTOR_NAME).addTransition(TASK1, TASK2);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());

        //when
        final HumanTaskInstance userTask = waitForUserTask(TASK1);
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());

        final List<Map<String, Object>> expenseReport = new ArrayList<Map<String, Object>>();
        expenseReport.add(createExpenseLine("hotel", 150.3f, new Date(System.currentTimeMillis()), null));
        expenseReport.add(createExpenseLine("taxi", 25, new Date(System.currentTimeMillis()), new byte[0]));
        expenseReport.add(createExpenseLine("plane", 500, new Date(System.currentTimeMillis()), new byte[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1 }));

        final Map<String, Object> taskInput = new HashMap<String, Object>();
        taskInput.put("expenseReport", expenseReport);

        try {
            getProcessAPI().executeUserTask(userTask.getId(), taskInput);
        } catch (final ContractViolationException e) {
            System.err.println(e.getExplanations());
        }

        //then
        waitForUserTask(TASK2);
        assertThat((List<Map<String, Object>>) getProcessAPI().getArchivedActivityDataInstance("expenseData", userTask.getId()).getValue()).as(
                "should have my expense report data").hasSize(3);

        //clean up
        disableAndDeleteProcess(processDefinition);
    }

    private Map<String, Object> createExpenseLine(final String expenseType, final float expenseAmount, final Date expenseDate, final byte[] expenseProof) {
        final Map<String, Object> expenseLine = new HashMap<String, Object>();
        expenseLine.put("expenseType", expenseType);
        expenseLine.put("expenseAmount", expenseAmount);
        expenseLine.put("expenseDate", expenseDate);
        expenseLine.put("expenseProof", expenseProof);
        return expenseLine;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void use_a_multiple_simple_input_in_user_tasks() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);

        //given
        final UserTaskDefinitionBuilder userTaskDefinitionBuilder = builder.addUserTask(TASK1, ACTOR_NAME);
        userTaskDefinitionBuilder.addContract()
                .addSimpleInput("input", Type.TEXT, "multiple input", true);
        final List<String> inputs = new ArrayList<String>();
        userTaskDefinitionBuilder.addData("inputListData", inputs.getClass().getName(), null);
        userTaskDefinitionBuilder.addOperation(new OperationBuilder().createSetDataOperation("inputListData",
                new ExpressionBuilder().createContractInputExpression("input", inputs.getClass().getName())));
        builder.addUserTask(TASK2, ACTOR_NAME).addTransition(TASK1, TASK2);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());

        //when
        final HumanTaskInstance userTask = waitForUserTask(TASK1);
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());

        final Map<String, Object> taskInputs = new HashMap<String, Object>();
        final List<String> inputList = new ArrayList<String>();
        inputList.add("input1");
        inputList.add("input2");
        inputList.add("input3");
        inputList.add("input4");
        inputList.add("input5");
        taskInputs.put("input", inputList);
        try {
            getProcessAPI().executeUserTask(userTask.getId(), taskInputs);
        } catch (final ContractViolationException e) {
            fail(e.getExplanations().toString());
        }

        //then
        waitForUserTask(TASK2);

        final ArchivedDataInstance archivedResult = getProcessAPI().getArchivedActivityDataInstance("inputListData", userTask.getId());
        assertThat((List<String>) archivedResult.getValue()).as("should have a list of simple input").hasSameSizeAs(inputList);

        //clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void should_execute_user_task_when_contract_is_valid() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        final UserTaskDefinitionBuilder userTaskBuilder = builder.addUserTask(TASK1, ACTOR_NAME);
        userTaskBuilder.addContract().addSimpleInput("numberOfDays", Type.INTEGER, null)
                .addMandatoryConstraint("numberOfDays");
        userTaskBuilder.addData("result", BigDecimal.class.getName(), null);
        userTaskBuilder.addOperation(new OperationBuilder().createSetDataOperation("result",
                new ExpressionBuilder().createContractInputExpression("numberOfDays", Long.class.getName())));
        builder.addUserTask(TASK2, ACTOR_NAME).addTransition(TASK1, TASK2);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask(TASK1);
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());

        final long expectedValue = 8l;
        try {
            final Map<String, Object> inputs = new HashMap<String, Object>();
            inputs.put("numberOfDays", expectedValue);

            getProcessAPI().executeUserTask(userTask.getId(), inputs);
        } catch (final ContractViolationException e) {
            System.err.println(e.getExplanations());
        }
        waitForUserTask(TASK2);

        final ArchivedDataInstance archivedResult = getProcessAPI().getArchivedActivityDataInstance("result", userTask.getId());
        assertThat(archivedResult.getValue()).isEqualTo(expectedValue);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void execute_user_task_should_throw_UserTaskNotFoundException() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        final UserTaskDefinitionBuilder userTaskBuilder = builder.addAutomaticTask("automaticTask").addUserTask(TASK1, ACTOR_NAME);
        userTaskBuilder.addContract().addSimpleInput("numberOfDays", Type.INTEGER, null)
                .addConstraint("mandatory", "numberOfDays != null", "numberOfDays must be set", "numberOfDays");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask(TASK1);
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());

        try {
            final Map<String, Object> inputs = new HashMap<String, Object>();
            inputs.put("numberOfDays", 8);

            //when
            getProcessAPI().executeUserTask(-1l, inputs);
            fail("should have a UserTaskNotFoundException ");
        } catch (final UserTaskNotFoundException e) {
            //then
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Test
    public void should_ContractIsNotValidException_keep_task_ready() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask(TASK1, ACTOR_NAME).addContract().addSimpleInput("numberOfDays", Type.INTEGER, null)
                .addConstraint("mandatory", "numberOfDays != null", "numberOfDays must be set", "numberOfDays");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask(TASK1);
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());

        try {
            final Map<String, Object> inputs = new HashMap<String, Object>();
            inputs.put("numberOfDays", null);

            getProcessAPI().executeUserTask(userTask.getId(), inputs);
            fail("The contract is not enforced");
        } catch (final ContractViolationException e) {
            final String state = getProcessAPI().getActivityInstanceState(userTask.getId());
            assertThat(state).isEqualTo("ready");
            assertThat(e.getExplanations()).as("should get explainations").isNotEmpty();
        }

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void should_connector_use_input_values() throws Exception {
        final Expression processNameExpression = new ExpressionBuilder().createConstantStringExpression(PROCESS_NAME);
        final Expression processVersionExpression = new ExpressionBuilder().createContractInputExpression("inputVersion", String.class.getName());
        final Expression outputExpression = new ExpressionBuilder().createContractInputExpression("processInputId", BigInteger.class.getName());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addData("processData", BigInteger.class.getName(), null);
        final UserTaskDefinitionBuilder userTaskBuilder = designProcessDefinition.addUserTask("task3", ACTOR_NAME);
        userTaskBuilder.addConnector("myConnector", "org.bonitasoft.engine.connectors.TestConnectorWithAPICall", "1.0", ConnectorEvent.ON_FINISH)
                .addInput("processName", processNameExpression).addInput("processVersion", processVersionExpression)
                .addOutput(new OperationBuilder().createSetDataOperation("processData", outputExpression));
        userTaskBuilder.addContract().addSimpleInput("inputVersion", Type.TEXT, null).addSimpleInput("processInputId", Type.INTEGER, null);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnectorWithAPICall(designProcessDefinition);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask("task3");

        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());
        try {
            final Map<String, Object> inputs = new HashMap<String, Object>();
            inputs.put("inputVersion", PROCESS_VERSION);
            inputs.put("processInputId", BigInteger.valueOf(45L));

            getProcessAPI().executeUserTask(userTask.getId(), inputs);
        } catch (final ContractViolationException e) {
            System.err.println(e.getExplanations());
        }

        waitForProcessToBeInState(processInstance, ProcessInstanceState.COMPLETED);

        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployAndEnableProcessWithTestConnectorWithAPICall(final ProcessDefinitionBuilder processDefinitionBuilder)
            throws InvalidBusinessArchiveFormatException, BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnector(processDefinitionBuilder, ACTOR_NAME, matti, "TestConnectorWithAPICall.impl",
                TestConnectorWithAPICall.class, "TestConnectorWithAPICall.jar");
    }

    private void check_invalid_contract_with_special_char(final long inputValue) throws Exception {
        //given
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        final String expectedExplanation = "numberOfDays must between one day and one year";
        builder.addUserTask(TASK1, ACTOR_NAME).addContract().addSimpleInput("numberOfDays", Type.INTEGER, null)
                .addConstraint("mandatory", "numberOfDays>1 && numberOfDays<365", expectedExplanation, "numberOfDays");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());

        //when
        final HumanTaskInstance task = waitForUserTask(TASK1);
        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put("numberOfDays", inputValue);
        try {
            getProcessAPI().executeUserTask(task.getId(), inputs);
            fail("should throw ContractViolationException");
        } catch (final ContractViolationException e) {
            //then
            assertThat(e.getExplanations()).as("rule should be violated").isNotEmpty().contains(expectedExplanation);
        } finally {
            disableAndDeleteProcess(processDefinition);

        }

    }

}
