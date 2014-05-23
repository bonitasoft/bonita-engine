/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.command.helper.ProcessDeployer;
import org.bonitasoft.engine.command.helper.designer.Gateway;
import org.bonitasoft.engine.command.helper.designer.SimpleProcessDesigner;
import org.bonitasoft.engine.command.helper.designer.UserTask;
import org.bonitasoft.engine.command.helper.expectation.TestUtils;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Vincent Elcrin
 * Date: 11/12/13
 * Time: 09:45
 */
public class AdvancedStartProcessCommandIntegrationTest extends CommonAPITest {

    User john;

    SimpleProcessDesigner designer = new SimpleProcessDesigner(getProcessDefinitionBuilder());

    TestUtils wrapper = new TestUtils(this);

    ProcessDeployer processDeployer = getProcessDeployer();

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalLogger();
        john = createUser(USERNAME, PASSWORD);
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
    }

    @After
    public void afterTest() throws BonitaException {
        processDeployer.clean();
        deleteUser(USERNAME);
        VariableStorage.clearAll();
        logoutOnTenant();
    }

    @Test
    public void should_start_a_sequential_process() throws Exception {
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"))
                .then(new UserTask("step 2"))
                .then(new UserTask("step 3"))
                .end());

        TestUtils.Process process = startProcess(john.getId(), processDefinition.getId(), "step 2");

        process.expect("step 2").toBeReady();
        process.expect("start", "step 1").toNotHaveArchives();
    }

    @Test
    public void should_start_a_sequential_process_with_variables() throws Exception {
        ProcessDefinitionBuilder builder = getProcessDefinitionBuilder();
        builder.addShortTextData("variable", new ExpressionBuilder().createConstantStringExpression("default"));
        SimpleProcessDesigner designer = new SimpleProcessDesigner(builder);
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"))
                .then(new UserTask("step 2"))
                .then(new UserTask("step 3"))
                .end());

        TestUtils.Process process = startProcess(john.getId(), processDefinition.getId(),
                "step 2",
                Arrays.asList(createSetDataOperation("variable", "Done!")),
                Collections.<String, Serializable> emptyMap());

        process.expectVariable("variable").toBe("Done!");
    }

    @Test
    public void should_be_able_to_start_a_sequential_process_with_a_document() throws Exception {
        ProcessDefinitionBuilder builder = getProcessDefinitionBuilder();
        builder.addDocumentDefinition("document");
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"))
                .then(new UserTask("step 2"))
                .then(new UserTask("step 3"))
                .end());

        TestUtils.Process process = startProcess(john.getId(),
                processDefinition.getId(),
                "step 2",
                Collections.singletonList(new OperationBuilder().createSetDocument("document",
                        new ExpressionBuilder().createInputExpression("value", DocumentValue.class.getName()))),
                Collections.<String, Serializable> singletonMap(
                        "value", new DocumentValue("content".getBytes(), "plain/text", "document")));

        process.expectDocument("document").toBe("content");
    }

    @Test
    public void should_be_able_to_start_a_process_with_a_parallel_split() throws Exception {
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"))
                .then(new Gateway("split", GatewayType.PARALLEL))
                .then(new UserTask("step 2"), new UserTask("step 3"))
                .end());

        TestUtils.Process process = startProcess(john.getId(), processDefinition.getId(), "step 1");
        process.execute(john, "step 1", "step 2", "step 3");

        process.isExpected().toFinish();
        process.expect("start").toNotHaveArchives();
    }

    @Test
    public void should_be_able_to_start_a_process_with_an_exclusive_merge() throws Exception {
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"), new UserTask("step 2"))
                .then(new Gateway("exclusive", GatewayType.EXCLUSIVE))
                .then(new UserTask("step 3"))
                .end());

        TestUtils.Process process = startProcess(john.getId(), processDefinition.getId(), "step 2");
        process.execute(john, "step 2", "step 3");

        process.isExpected().toFinish();
        process.expect("start").toNotHaveArchives();
        process.expect("step 3").toBeExecuted(1);
    }

    @Test
    public void should_be_able_to_start_a_process_with_an_exclusive_split() throws Exception {
        Expression condition = new ExpressionBuilder().createConstantBooleanExpression(true);
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"))
                .then(new Gateway("exclusive", GatewayType.EXCLUSIVE))
                .then(
                        new UserTask("step 2").when("exclusive", fails()),
                        new UserTask("step 3").when("exclusive", meet(condition)))
                .end());

        TestUtils.Process process = startProcess(john.getId(), processDefinition.getId(), "step 1");
        process.execute(john, "step 1", "step 3");

        process.isExpected().toFinish();
        process.expect("start").toNotHaveArchives();
        process.expect("step 3").toBeExecuted(1);
    }

    @Test
    public void should_be_able_to_start_a_process_before_an_inclusive() throws Exception {
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"))
                .then(new Gateway("inclusive 1", GatewayType.INCLUSIVE))
                .then(new UserTask("step 2"), new UserTask("step 3"))
                .then(new Gateway("inclusive 2", GatewayType.INCLUSIVE))
                .then(new UserTask("step 4"))
                .end());

        TestUtils.Process process = startProcess(john.getId(), processDefinition.getId(), "step 1");
        process.execute(john, "step 1", "step 2", "step 3", "step 4");

        process.isExpected().toFinish();
        process.expect("start").toNotHaveArchives();
    }

    private Operation createSetDataOperation(String name, String value) throws InvalidExpressionException {
        return new OperationBuilder().createSetDataOperation(name, new ExpressionBuilder().createConstantStringExpression(value));
    }

    private ProcessDefinitionBuilder getProcessDefinitionBuilder() {
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("Designed by designer", "1.0")
                .addActor("actor")
                .addDescription("Coding all-night-long");
        return builder;
    }

    private TestUtils.Process startProcess(long startedBy,
            long processDefinitionId,
            String activityName) throws Exception {
        return startProcess(startedBy, processDefinitionId, activityName, null, null);
    }

    private TestUtils.Process startProcess(
            long startedBy,
            long processDefinitionId,
            String activityName,
            List<Operation> operations,
            Map<String, Serializable> context) throws Exception {

        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("started_by", startedBy);
        parameters.put("process_definition_id", processDefinitionId);
        parameters.put("activity_name", activityName);
        if (operations != null) {
            parameters.put("operations", new ArrayList<Operation>(operations));
        }
        if (context != null) {
            parameters.put("context", new HashMap<String, Serializable>(context));
        }

        return wrapper.wrap((ProcessInstance) getCommandAPI().execute("advancedStartProcessCommand", parameters));
    }

    private ProcessDeployer getProcessDeployer() {
        return new ProcessDeployer() {

            @Override
            public ProcessDefinition deploy(DesignProcessDefinition design) throws BonitaException {
                return deployAndEnableProcessWithActor(design, "actor", john);
            }

            @Override
            public void clean(ProcessDefinition processDefinition) throws BonitaException {
                disableAndDeleteProcess(processDefinition);
            }
        };
    }
}
