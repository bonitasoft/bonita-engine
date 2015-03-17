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
package org.bonitasoft.engine.command;

import static org.bonitasoft.engine.command.helper.designer.Transition.fails;
import static org.bonitasoft.engine.command.helper.designer.Transition.meet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.command.helper.ProcessDeployer;
import org.bonitasoft.engine.command.helper.designer.BoundaryEvent;
import org.bonitasoft.engine.command.helper.designer.Branch;
import org.bonitasoft.engine.command.helper.designer.Gateway;
import org.bonitasoft.engine.command.helper.designer.Signal;
import org.bonitasoft.engine.command.helper.designer.SimpleProcessDesigner;
import org.bonitasoft.engine.command.helper.designer.UserTask;
import org.bonitasoft.engine.command.helper.expectation.TestUtils;
import org.bonitasoft.engine.connectors.TestConnectorWithOutput;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Test;


/**
 * @author Elias Ricken de Medeiros
 */
public class MultipleStartPointsProcessCommandIT extends TestWithUser {

    private static final String CONNECTOR_OUTPUT_NAME = "output1";

    private static final String CONNECTOR_INPUT_NAME = "input1";

    private static final String CONNECTOR_WITH_OUTPUT_ID = "org.bonitasoft.connector.testConnectorWithOutput";

    private final SimpleProcessDesigner designer = new SimpleProcessDesigner(getProcessDefinitionBuilder());

    private final TestUtils wrapper = new TestUtils(this);

    private final ProcessDeployer processDeployer = getProcessDeployer();

    @After
    public void afterTest() throws BonitaException {
        processDeployer.clean();
        VariableStorage.clearAll();
    }

    @Test
    public void should_start_a_sequential_process() throws Exception {
        final ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"))
                .then(new UserTask("step 2"))
                .then(new UserTask("step 3"))
                .end());

        final TestUtils.Process process = startProcess(user.getId(), processDefinition.getId(), Arrays.asList("step 2"));

        process.expect("step 2").toBeReady();
        process.expect("start", "step 1").toNotHaveArchives();
    }

    @Test
    public void should_start_a_sequential_process_with_variables() throws Exception {
        final ProcessDefinitionBuilder builder = getProcessDefinitionBuilder();
        builder.addShortTextData("variable", new ExpressionBuilder().createConstantStringExpression("default"));
        final SimpleProcessDesigner designer = new SimpleProcessDesigner(builder);
        final ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"))
                .then(new UserTask("step 2"))
                .then(new UserTask("step 3"))
                .end());

        final TestUtils.Process process = startProcess(user.getId(), processDefinition.getId(),
                Arrays.asList("step 2"),
                Arrays.asList(createSetDataOperation("variable", "Done!")),
                Collections.<String, Serializable> emptyMap());

        process.expectVariable("variable").toBe("Done!");
    }

    @Test
    public void should_be_able_to_start_a_sequential_process_with_a_document() throws Exception {
        final ProcessDefinitionBuilder builder = getProcessDefinitionBuilder();
        builder.addDocumentDefinition("document");
        final ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"))
                .then(new UserTask("step 2"))
                .then(new UserTask("step 3"))
                .end());

        final TestUtils.Process process = startProcess(user.getId(),
                processDefinition.getId(),
                Arrays.asList("step 2"),
                Collections.singletonList(new OperationBuilder().createSetDocument("document",
                        new ExpressionBuilder().createInputExpression("value", DocumentValue.class.getName()))),
                Collections.<String, Serializable> singletonMap(
                        "value", new DocumentValue("content".getBytes(), "plain/text", "document")));

        process.expectDocument("document").toBe("content");
    }

    @Test
    public void should_be_able_to_start_a_process_before_a_parallel_merge() throws Exception {
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new Gateway("split", GatewayType.PARALLEL))
                .then(new UserTask("step 1"), new UserTask("step 2"))
                .then(new Gateway("merge", GatewayType.PARALLEL))
                .then(new UserTask("step 3"))
                .end());

        TestUtils.Process process = startProcess(user.getId(), processDefinition.getId(), Arrays.asList("step 1", "step 2"));
        process.execute(user, "step 1", "step 2");
        process.execute(user, "step 3");

        process.isExpected().toFinish();
        process.expect("start", "split").toNotHaveArchives();
        process.expect("merge").toBeExecuted(1);
    }

    @Test
    public void should_be_able_to_start_a_process_before_consecutive_parallel_merges() throws Exception {
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(
                        new UserTask("step 1"),
                        new Branch().start(new Gateway("parallel 1", GatewayType.PARALLEL))
                                .then(new UserTask("step 2"), new UserTask("step 3"))
                                .then(new Gateway("parallel 2", GatewayType.PARALLEL)))
                .then(new Gateway("parallel 3", GatewayType.PARALLEL))
                .then(new UserTask("step 4"))
                .end());

        TestUtils.Process process = startProcess(user.getId(), processDefinition.getId(), Arrays.asList("step 1", "step 2", "step 3"));
        process.execute(user, "step 1", "step 2", "step 3", "step 4");

        process.isExpected().toFinish();
        process.expect("step 1", "step 2", "step 3", "step 4").toBeExecuted(1);
        process.expect("start", "parallel 1").toNotHaveArchives();
    }

    @Test
    public void should_be_able_to_start_a_process_before_a_parallel_split() throws Exception {
        final ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"))
                .then(new Gateway("split", GatewayType.PARALLEL))
                .then(new UserTask("step 2"), new UserTask("step 3"))
                .end());

        final TestUtils.Process process = startProcess(user.getId(), processDefinition.getId(), Arrays.asList("step 1"));
        process.execute(user, "step 1", "step 2", "step 3");

        process.isExpected().toFinish();
        process.expect("start").toNotHaveArchives();
    }

    @Test
    public void should_be_able_to_start_a_process_before_an_exclusive_merge_with_only_one_active_branch() throws Exception {
        final ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"), new UserTask("step 2"))
                .then(new Gateway("exclusive", GatewayType.EXCLUSIVE))
                .then(new UserTask("step 3"))
                .end());

        final TestUtils.Process process = startProcess(user.getId(), processDefinition.getId(), Arrays.asList("step 2"));
        process.execute(user, "step 2", "step 3");

        process.isExpected().toFinish();
        process.expect("start").toNotHaveArchives();
        process.expect("step 3").toBeExecuted(1);
    }

    @Test
    public void should_be_able_to_start_a_process_before_an_exclusive_merge_with_execute_all_incoming_branches() throws Exception {
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"), new UserTask("step 2"))
                .then(new Gateway("exclusive", GatewayType.EXCLUSIVE))
                .then(new UserTask("step 3"))
                .end());

        TestUtils.Process process = startProcess(user.getId(), processDefinition.getId(), Arrays.asList("step 1", "step 2"));
        process.execute(user, "step 1", "step 2", "step 3", "step 3");

        process.isExpected().toFinish();
        process.expect("start").toNotHaveArchives();
        process.expect("step 3").toBeExecuted(2);
    }

    @Test
    public void should_be_able_to_start_a_process_before_an_exclusive_merge_with_conditions() throws Exception {
        Expression condition = new ExpressionBuilder().createConstantBooleanExpression(true);
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"), new UserTask("step 2"))
                .then(new Gateway("exclusive", GatewayType.EXCLUSIVE)
                        .when("step 1", fails().toMeet(condition).goingTo(new UserTask("step 3")))
                        .when("step 2", meet(condition).otherwise(new UserTask("step 4"))))
                .then(new UserTask("step 5"))
                .end());

        TestUtils.Process process = startProcess(user.getId(), processDefinition.getId(), Arrays.asList("step 1", "step 2"));
        process.execute(user, "step 1", "step 2", "step 3", "step 5");

        process.isExpected().toFinish();
        process.expect("start").toNotHaveArchives();
        process.expect("step 3", "step 5").toBeExecuted(1);
    }

    @Test
    public void should_be_able_to_start_a_process_before_an_exclusive_split() throws Exception {
        final Expression condition = new ExpressionBuilder().createConstantBooleanExpression(true);
        final ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"))
                .then(new Gateway("exclusive", GatewayType.EXCLUSIVE))
                .then(
                        new UserTask("step 2").when("exclusive", fails()),
                        new UserTask("step 3").when("exclusive", meet(condition)))
                .end());

        final TestUtils.Process process = startProcess(user.getId(), processDefinition.getId(), Arrays.asList("step 1"));
        process.execute(user, "step 1", "step 3");

        process.isExpected().toFinish();
        process.expect("start").toNotHaveArchives();
        process.expect("step 3").toBeExecuted(1);
    }

    @Test
    public void should_be_able_to_start_a_process_before_an_inclusive() throws Exception {
        final ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"))
                .then(new Gateway("inclusive 1", GatewayType.INCLUSIVE))
                .then(new UserTask("step 2"), new UserTask("step 3"))
                .then(new Gateway("inclusive 2", GatewayType.INCLUSIVE))
                .then(new UserTask("step 4"))
                .end());

        final TestUtils.Process process = startProcess(user.getId(), processDefinition.getId(), Arrays.asList("step 1"));
        process.execute(user, "step 1", "step 2", "step 3", "step 4");

        process.isExpected().toFinish();
        process.expect("start").toNotHaveArchives();
    }

    @Test
    public void should_be_able_to_start_before_an_inclusive_merge_with_all_incoming_branches() throws Exception {
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"))
                .then(new Gateway("inclusive 1", GatewayType.INCLUSIVE))
                .then(new UserTask("step 2"), new UserTask("step 3"))
                .then(new Gateway("inclusive 2", GatewayType.INCLUSIVE))
                .then(new UserTask("step 4"))
                .end());

        TestUtils.Process process = startProcess(user.getId(), processDefinition.getId(), Arrays.asList("step 2", "step 3"));
        process.execute(user, "step 2", "step 3", "step 4");

        process.isExpected().toFinish();
        process.expect("step 1").toNotHaveArchives();
        process.expect("step 4").toBeExecuted(1);
    }

    @Test
    public void should_be_able_to_start_before_an_inclusive_with_sub_set_of_incoming_branches() throws Exception {
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"))
                .then(new Gateway("inclusive 1", GatewayType.INCLUSIVE))
                .then(new UserTask("step 2"), new UserTask("step 3"))
                .then(new Gateway("inclusive 2", GatewayType.INCLUSIVE))
                .then(new UserTask("step 4"))
                .end());

        TestUtils.Process process = startProcess(user.getId(), processDefinition.getId(), Arrays.asList("step 2"));
        process.execute(user, "step 2", "step 4");

        process.isExpected().toFinish();
        process.expect("step 1").toNotHaveArchives();
        process.expect("step 4").toBeExecuted(1);
    }

    @Test
    public void should_be_able_to_start_an_inclusive_with_boundary() throws Exception {
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"))
                .then(new Gateway("inclusive 1", GatewayType.INCLUSIVE))
                .then(
                        new UserTask("step 2"),
                        new UserTask("step 3").with(
                                new BoundaryEvent("boundary", new UserTask("step 4")).triggeredBy(new Signal("foo"))))
                .then(new Gateway("inclusive 2", GatewayType.INCLUSIVE))
                .then(new UserTask("step 5"))
                .end());

        TestUtils.Process process = startProcess(user.getId(), processDefinition.getId(), Arrays.asList("step 2", "step 3"));
        process.execute(user, "step 2");
        process.sendSignal("foo");
        process.execute(user, "step 4", "step 5");

        process.isExpected().toFinish();
        process.expect("step 2", "step 4", "step 5").toBeExecuted(1);
        process.expect("step 3").toBeAborted();
    }

    private Operation createSetDataOperation(final String name, final String value) throws InvalidExpressionException {
        return new OperationBuilder().createSetDataOperation(name, new ExpressionBuilder().createConstantStringExpression(value));
    }

    private ProcessDefinitionBuilder getProcessDefinitionBuilder() {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("Designed by designer", "1.0")
                .addActor("actor")
                .addDescription("Coding all-night-long");
        return builder;
    }

    private TestUtils.Process startProcess(final long startedBy, final long processDefinitionId, final List<String> activityNames) throws Exception {
        return startProcess(startedBy, processDefinitionId, activityNames, null, null);
    }

    private TestUtils.Process startProcess(final long startedBy, final long processDefinitionId, final List<String> activityNames,
            final List<Operation> operations,
            final Map<String, Serializable> context) throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("started_by", startedBy);
        parameters.put("process_definition_id", processDefinitionId);
        parameters.put("activity_names", new ArrayList<String>(activityNames));
        if (operations != null) {
            parameters.put("operations", new ArrayList<Operation>(operations));
        }
        if (context != null) {
            parameters.put("context", new HashMap<String, Serializable>(context));
        }

        return wrapper.wrap((ProcessInstance) getCommandAPI().execute("multipleStartPointsProcessCommand", parameters));
    }

    private ProcessDeployer getProcessDeployer() {
        return new ProcessDeployer() {

            @Override
            public ProcessDefinition deploy(final DesignProcessDefinition design) throws BonitaException {
                return deployAndEnableProcessWithActor(design, "actor", user);
            }

            @Override
            public void clean(final ProcessDefinition processDefinition) throws BonitaException {
                disableAndDeleteProcess(processDefinition);
            }
        };
    }

    @Cover(classes = { CommandAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "AdvancedStartProcessCommand",
            "Connector", "Enter" }, jira = "BS-9188")
    @Test
    public void advancedStartProcessCommandWithConnectorOnEnterOnProcess() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addShortTextData("outputOfConnector", null);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addConnector("myConnector", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_ENTER)
                .addInput(CONNECTOR_INPUT_NAME, new ExpressionBuilder().createConstantStringExpression("a"))
                .addOutput(new LeftOperandBuilder().createNewInstance().setName("outputOfConnector").done(), OperatorType.ASSIGNMENT, "=", "",
                        new ExpressionBuilder().createInputExpression(CONNECTOR_OUTPUT_NAME, String.class.getName()));
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndConnector(processDefinitionBuilder, ACTOR_NAME, user,
                "TestConnectorWithOutput.impl", TestConnectorWithOutput.class, "TestConnectorWithOutput.jar");

        // Start the process with the command on the step2
        final Map<String, Serializable> parametersCommand = new HashMap<String, Serializable>();
        parametersCommand.put("started_by", user.getId());
        parametersCommand.put("process_definition_id", processDefinition.getId());
        parametersCommand.put("activity_names", new ArrayList<String>(Arrays.asList("step2")));
        // command API execution
        getCommandAPI().execute("multipleStartPointsProcessCommand", parametersCommand);

        waitForUserTask("step2");

        // Clean
        disableAndDeleteProcess(processDefinition);
    }
}
