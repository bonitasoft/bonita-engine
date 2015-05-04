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
 */
package org.bonitasoft.engine.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.tuple;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.contract.ConstraintDefinition;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.bpm.contract.FileInputValue;
import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.InputDefinitionImpl;
import org.bonitasoft.engine.bpm.data.ArchivedDataInstance;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.flownode.FlowNodeExecutionException;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.UserTaskNotFoundException;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.impl.ContractDefinitionBuilder;
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

public class UserTaskContractITest extends CommonAPIIT {

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
    public void shouldHandleContractInputsAtProcessLevel() throws Exception {
        //given
        final String numberOfDaysProcessContractData = "numberOfDaysProcessContractData";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        // have a initial data value using process input, so that we ensure inputs are treated before data at process instantiation:
        builder.addData("nbDaysProcessData", Integer.class.getName(),
                new ExpressionBuilder().createContractInputExpression(numberOfDaysProcessContractData, Integer.class.getName()));
        builder.addData("multipleTextData", List.class.getName(), new ExpressionBuilder().createContractInputExpression("multipleText", List.class.getName()));
        builder.addData("complexData", Map.class.getName(), new ExpressionBuilder().createContractInputExpression("complex", Map.class.getName()));
        builder.addActor(ACTOR_NAME);
        builder.addUserTask(TASK1, ACTOR_NAME);
        final ContractDefinitionBuilder contract = builder.addContract();
        contract.addInput(numberOfDaysProcessContractData, Type.INTEGER, null);
        contract.addInput("multipleText", Type.TEXT, "a multiple text", true);
        contract.addInput("complex", "a complex input",
                Collections.<InputDefinition>singletonList(new InputDefinitionImpl("text", Type.TEXT, "text in complex")));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertThat(processDeploymentInfo.getConfigurationState()).isEqualTo(ConfigurationState.RESOLVED);
        assertThat(processDeploymentInfo.getActivationState()).isEqualTo(ActivationState.ENABLED);

        final Map<String, Serializable> inputs = new HashMap<>(1);
        final int value = 14;
        inputs.put(numberOfDaysProcessContractData, value);
        final ArrayList<String> multiples = new ArrayList<>(Arrays.asList("String1", "String2"));
        inputs.put("multipleText", multiples);
        final HashMap<Object, Object> map = new HashMap<>();
        map.put("text", "textValue");
        inputs.put("complex", map);
        final ProcessInstance processInstance = getProcessAPI().startProcessWithInputs(processDefinition.getId(), inputs);
        waitForUserTask(processInstance, TASK1);
        final DataInstance processDataValueInitializedFromInput = getProcessAPI().getProcessDataInstance("nbDaysProcessData", processInstance.getId());
        assertThat(getProcessAPI().getProcessDataInstance("multipleTextData", processInstance.getId()).getValue()).isEqualTo(multiples);
        assertThat(getProcessAPI().getProcessDataInstance("complexData", processInstance.getId()).getValue()).isEqualTo(map);
        assertThat(processDataValueInitializedFromInput.getValue()).isEqualTo(value);

        final Serializable processInstanciationInputValue = getProcessAPI().getProcessInputValueAfterInitialization(processInstance.getId(),
                numberOfDaysProcessContractData);
        assertThat(processInstanciationInputValue).isEqualTo(value);

        //clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void should_getUserTaskContract_return_the_contract() throws Exception {
        //given
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask(TASK1, ACTOR_NAME).addContract().addInput("numberOfDays", Type.INTEGER, null)
                .addConstraint("Mystical constraint", "true", null, "numberOfDays");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTaskAndGetIt(TASK1);

        //when
        final ContractDefinition contract = getProcessAPI().getUserTaskContract(userTask.getId());

        //then
        assertThat(contract.getInputs()).hasSize(1);
        final InputDefinition input = contract.getInputs().get(0);
        assertThat(input.getName()).isEqualTo("numberOfDays");
        assertThat(input.getType()).isEqualTo(Type.INTEGER);
        assertThat(input.getDescription()).isNull();

        assertThat(contract.getConstraints()).hasSize(1);
        final ConstraintDefinition mysticRule = contract.getConstraints().get(0);
        assertThat(mysticRule.getName()).isEqualTo("Mystical constraint");
        assertThat(mysticRule.getInputNames()).containsExactly("numberOfDays");
        assertThat(mysticRule.getExplanation()).isNull();

        //clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void should_getUserTaskContract_return_contract_with_complex_inputs() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        final InputDefinition expenseType = new InputDefinitionImpl("expenseType", Type.TEXT, "describe expense type");
        final InputDefinition expenseAmount = new InputDefinitionImpl("amount", Type.DECIMAL, "expense amount");
        final InputDefinition expenseDate = new InputDefinitionImpl("date", Type.DATE, "expense date");
        final InputDefinition complexSubIput = new InputDefinitionImpl("date", "expense date", Arrays.asList(expenseType));
        //given
        builder.addUserTask(TASK1, ACTOR_NAME).addContract()
                .addInput("expenseLine", "expense report line", true, Arrays.asList(expenseDate, expenseAmount, complexSubIput));

        //when
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTaskAndGetIt(TASK1);

        //then
        final ContractDefinition contract = getProcessAPI().getUserTaskContract(userTask.getId());
        assertThat(contract.getInputs()).hasSize(1);
        final InputDefinition complexInput = contract.getInputs().get(0);
        assertThat(complexInput.getName()).isEqualTo("expenseLine");
        assertThat(complexInput.isMultiple()).as("should be multiple").isTrue();
        assertThat(complexInput.getDescription()).isEqualTo("expense report line");
        assertThat(complexInput.getInputs()).as("should have 3 inputs").hasSize(3);

        //clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void should_create_a_contract_with_special_char() throws Exception {
        final long[] badValues = {0, 366};
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
        builder.addUserTask(TASK1, ACTOR_NAME).addContract().addInput("comment", Type.TEXT, null)
                .addConstraint("mandatory", "comment.equals(\"<tag>\")", expectedExplanation, "comment");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());

        //when
        final HumanTaskInstance task = waitForUserTaskAndGetIt(TASK1);
        getProcessAPI().assignUserTask(task.getId(), matti.getId());

        //then no exceptions
        final Map<String, Serializable> map = new HashMap<>();
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
        userTask.addContract().addInput("decimal", Type.DECIMAL, null);
        userTask.addData("variable", Number.class.getName(), null);
        userTask.addOperation(new OperationBuilder().createSetDataOperation("variable",
                new ExpressionBuilder().createContractInputExpression("decimal", Number.class.getName())));

        //when
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance task = waitForUserTaskAndGetIt(TASK1);
        getProcessAPI().assignUserTask(task.getId(), matti.getId());

        //then
        final Map<String, Serializable> map = new HashMap<>();
        map.put("decimal", 2);
        getProcessAPI().executeUserTask(task.getId(), map);

        //clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void should_invalid_contract_throw_ContractViolationException() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask(TASK1, ACTOR_NAME).addContract().addInput("numberOfDays", Type.INTEGER, null);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTaskAndGetIt(TASK1);
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());

        try {
            getProcessAPI().executeFlowNode(userTask.getId());
            fail("The contract is not enforced");
        } catch (final FlowNodeExecutionException e) {
            final String state = getProcessAPI().getActivityInstanceState(userTask.getId());
            assertThat(state).isEqualTo("ready");
        }
        try {
            getProcessAPI().executeUserTask(userTask.getId(), new HashMap<String, Serializable>());
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
        final InputDefinition expenseType = new InputDefinitionImpl("expenseType", Type.TEXT, "describe expense type");
        final InputDefinition expenseAmount = new InputDefinitionImpl("expenseAmount", Type.DECIMAL, "expense amount");
        final InputDefinition expenseDate = new InputDefinitionImpl("expenseDate", Type.DATE, "expense date");
        final InputDefinition expenseProof = new InputDefinitionImpl("expenseProof", Type.BYTE_ARRAY, "expense proof");
        builder.addDocumentDefinition("reportAsDoc");
        builder.addDocumentListDefinition("receiptsAsDoc");
        //given
        final UserTaskDefinitionBuilder userTaskDefinitionBuilder = builder.addUserTask(TASK1, ACTOR_NAME);
        userTaskDefinitionBuilder.addContract()
                .addInput("expenseReport", "expense report with several expense lines", true,
                        Arrays.asList(expenseType, expenseDate, expenseAmount, expenseProof))
                .addFileInput("report", "the report")
                .addFileInput("receipts", "the receipts", true);

        userTaskDefinitionBuilder.addData("expenseData", List.class.getName(), null);
        userTaskDefinitionBuilder.addData("reportData", List.class.getName(), null);
        userTaskDefinitionBuilder.addData("receiptsData", List.class.getName(), null);
        userTaskDefinitionBuilder.addOperation(new OperationBuilder().createSetDataOperation("expenseData",
                new ExpressionBuilder().createContractInputExpression("expenseReport", List.class.getName())));
        userTaskDefinitionBuilder.addOperation(new OperationBuilder().createSetDataOperation("reportData",
                new ExpressionBuilder().createContractInputExpression("report", FileInputValue.class.getName())));
        userTaskDefinitionBuilder.addOperation(new OperationBuilder().createSetDataOperation("receiptsData",
                new ExpressionBuilder().createContractInputExpression("receipts", List.class.getName())));
        userTaskDefinitionBuilder.addOperation(new OperationBuilder().createSetDocument("reportAsDoc",
                new ExpressionBuilder().createContractInputExpression("report", FileInputValue.class.getName())));
        userTaskDefinitionBuilder.addOperation(new OperationBuilder().createSetDocumentList("receiptsAsDoc",
                new ExpressionBuilder().createContractInputExpression("receipts", List.class.getName())));
        builder.addUserTask(TASK2, ACTOR_NAME).addTransition(TASK1, TASK2);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        //when
        final HumanTaskInstance userTask = waitForUserTaskAndGetIt(TASK1);
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());

        final List<Map<String, Serializable>> expenseReport = new ArrayList<>();
        expenseReport.add(createExpenseLine("hotel", 150.3f, new Date(System.currentTimeMillis()), new byte[0]));
        expenseReport.add(createExpenseLine("taxi", 25, new Date(System.currentTimeMillis()), new byte[0]));
        expenseReport.add(createExpenseLine("plane", 500, new Date(System.currentTimeMillis()), new byte[]{0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1}));

        final Map<String, Serializable> taskInput = new HashMap<>();
        taskInput.put("expenseReport", (Serializable) expenseReport);
        final FileInputValue reportFile = new FileInputValue("report.pdf", new byte[]{0, 1, 2, 3});
        taskInput.put("report", reportFile);
        final FileInputValue receipt1 = new FileInputValue("receipt1.pdf", new byte[]{0, 1, 2, 4});
        final FileInputValue receipt2 = new FileInputValue("receipt2.pdf", new byte[]{0, 1, 2, 5});
        taskInput.put("receipts", (Serializable) Arrays.asList(receipt1, receipt2));

        getProcessAPI().executeUserTask(userTask.getId(), taskInput);

        //then
        waitForUserTaskAndGetIt(TASK2);
        assertThat((List<Map<String, Object>>) getProcessAPI().getArchivedActivityDataInstance("expenseData", userTask.getId()).getValue()).as("should have my expense report data").hasSize(3);
        final Serializable reportData = getProcessAPI().getArchivedActivityDataInstance("reportData", userTask.getId()).getValue();
        assertThat(reportData).as("should have single file").isEqualTo(reportFile);
        assertThat((List<Object>) getProcessAPI().getArchivedActivityDataInstance("receiptsData", userTask.getId()).getValue()).as("should have multiple file").containsExactly(receipt1, receipt2);
        final List<Document> receiptsAsDoc = getProcessAPI().getDocumentList(processInstance.getId(), "receiptsAsDoc", 0, 100);
        Document reportAsDoc = getProcessAPI().getLastDocument(processInstance.getId(), "reportAsDoc");

        assertThat(reportAsDoc.getContentFileName()).as("document file name").isEqualTo("report.pdf");
        assertThat(getProcessAPI().getDocumentContent(reportAsDoc.getContentStorageId())).as("document content").isEqualTo(new byte[]{0, 1, 2, 3});
        assertThat(receiptsAsDoc).hasSize(2);
        assertThat(receiptsAsDoc).extracting("contentFileName", "index").containsExactly(tuple("receipt1.pdf", 0), tuple("receipt2.pdf", 1));
        assertThat(getProcessAPI().getDocumentContent(receiptsAsDoc.get(0).getContentStorageId())).as("document content").isEqualTo(new byte[]{0, 1, 2, 4});
        assertThat(getProcessAPI().getDocumentContent(receiptsAsDoc.get(1).getContentStorageId())).as("document content").isEqualTo(new byte[]{0, 1, 2, 5});

        //clean up
        disableAndDeleteProcess(processDefinition);
    }

    private Map<String, Serializable> createExpenseLine(final String expenseType, final float expenseAmount, final Date expenseDate, final byte[] expenseProof) {
        final Map<String, Serializable> expenseLine = new HashMap<>();
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
                .addInput("input", Type.TEXT, "multiple input", true);
        final List<String> inputs = new ArrayList<String>();
        userTaskDefinitionBuilder.addData("inputListData", inputs.getClass().getName(), null);
        userTaskDefinitionBuilder.addOperation(new OperationBuilder().createSetDataOperation("inputListData",
                new ExpressionBuilder().createContractInputExpression("input", inputs.getClass().getName())));
        builder.addUserTask(TASK2, ACTOR_NAME).addTransition(TASK1, TASK2);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());

        //when
        final HumanTaskInstance userTask = waitForUserTaskAndGetIt(TASK1);
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());

        final Map<String, Serializable> taskInputs = new HashMap<>();
        final List<String> inputList = new ArrayList<>();
        inputList.add("input1");
        inputList.add("input2");
        inputList.add("input3");
        inputList.add("input4");
        inputList.add("input5");
        taskInputs.put("input", (Serializable) inputList);
        getProcessAPI().executeUserTask(userTask.getId(), taskInputs);

        //then
        waitForUserTaskAndGetIt(TASK2);

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
        userTaskBuilder.addData("result", BigDecimal.class.getName(), null);
        userTaskBuilder.addOperation(new OperationBuilder().createSetDataOperation("result",
                new ExpressionBuilder().createContractInputExpression("numberOfDays", Long.class.getName())));
        builder.addUserTask(TASK2, ACTOR_NAME).addTransition(TASK1, TASK2);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTaskAndGetIt(TASK1);
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());

        final long expectedValue = 8l;
        final Map<String, Serializable> inputs = new HashMap<>();
        inputs.put("numberOfDays", expectedValue);

        getProcessAPI().executeUserTask(userTask.getId(), inputs);
        waitForUserTaskAndGetIt(TASK2);

        final ArchivedDataInstance archivedResult = getProcessAPI().getArchivedActivityDataInstance("result", userTask.getId());
        assertThat(archivedResult.getValue()).isEqualTo(expectedValue);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void execute_user_task_should_throw_UserTaskNotFoundException() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor(ACTOR_NAME);
        final UserTaskDefinitionBuilder userTaskBuilder = builder.addAutomaticTask("automaticTask").addUserTask(TASK1, ACTOR_NAME);
        userTaskBuilder.addContract().addInput("numberOfDays", Type.INTEGER, null)
                .addConstraint("mandatory", "numberOfDays != null", "numberOfDays must be set", "numberOfDays");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTaskAndGetIt(TASK1);
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());

        try {
            final Map<String, Serializable> inputs = new HashMap<>();
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
        builder.addUserTask(TASK1, ACTOR_NAME).addContract().addInput("numberOfDays", Type.INTEGER, null)
                .addConstraint("mandatory", "numberOfDays != null", "numberOfDays must be set", "numberOfDays");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTaskAndGetIt(TASK1);
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());

        try {
            final Map<String, Serializable> inputs = new HashMap<>();
            inputs.put("numberOfDays", null);

            getProcessAPI().executeUserTask(userTask.getId(), inputs);
            fail("The contract is not enforced");
        } catch (final ContractViolationException e) {
            final String state = getProcessAPI().getActivityInstanceState(userTask.getId());
            assertThat(state).isEqualTo("ready");
            assertThat(e.getExplanations()).as("should get explanations").isNotEmpty();
            assertThat(e.getMessage()).contains("numberOfDays");
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
        userTaskBuilder.addContract().addInput("inputVersion", Type.TEXT, null).addInput("processInputId", Type.INTEGER, null);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnectorWithAPICall(designProcessDefinition);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTaskAndGetIt("task3");

        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());
        try {
            final Map<String, Serializable> inputs = new HashMap<>();
            inputs.put("inputVersion", PROCESS_VERSION);
            inputs.put("processInputId", BigInteger.valueOf(45L));

            getProcessAPI().executeUserTask(userTask.getId(), inputs);
        } catch (final ContractViolationException e) {
            System.err.println(e.getExplanations());
        }

        waitForProcessToBeInState(processInstance, ProcessInstanceState.COMPLETED);

        final BigInteger processInputId = (BigInteger) getProcessAPI().getUserTaskContractVariableValue(userTask.getId(), "processInputId");
        assertThat(processInputId).isEqualTo(BigInteger.valueOf(45L));

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
        builder.addUserTask(TASK1, ACTOR_NAME).addContract().addInput("numberOfDays", Type.INTEGER, null)
                .addConstraint("mandatory", "numberOfDays>1 && numberOfDays<365", expectedExplanation, "numberOfDays");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        getProcessAPI().startProcess(processDefinition.getId());

        //when
        final HumanTaskInstance task = waitForUserTaskAndGetIt(TASK1);
        final Map<String, Serializable> inputs = new HashMap<>();
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
