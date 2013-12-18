package org.bonitasoft.engine.repair;

import java.util.Arrays;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.RepairAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.repair.helper.ProcessDeployer;
import org.bonitasoft.engine.repair.helper.TestUtils;
import org.bonitasoft.engine.repair.helper.designer.Gateway;
import org.bonitasoft.engine.repair.helper.designer.SimpleProcessDesigner;
import org.bonitasoft.engine.repair.helper.designer.UserTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Vincent Elcrin
 * Date: 11/12/13
 * Time: 09:45
 */
public class RepairAPITest extends CommonAPITest {

    RepairAPI repairAPI;

    static final String JOHN = "john";

    User john;

    SimpleProcessDesigner designer = new SimpleProcessDesigner(getProcessDefinitionBuilder());

    TestUtils wrapper = new TestUtils(this);

    ProcessDeployer processDeployer = getProcessDeployer();

    @Before
    public void beforeTest() throws BonitaException {
        login();
        john = createUser(JOHN, "bpm");
        logout();
        loginWith(JOHN, "bpm");
        repairAPI = TenantAPIAccessor.getRepairAPI(getSession());
    }

    @After
    public void afterTest() throws BonitaException {
        processDeployer.clean();
        deleteUser(JOHN);
        VariableStorage.clearAll();
        logout();
    }

    /*
     * start -> step 1 -> step 2(s-e) -> step 3 -> end
     */
    @Test
    public void should_start_a_process_giving_an_activity_name_to_start_from() throws Exception {
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"))
                .then(new UserTask("step 2"))
                .then(new UserTask("step 3"))
                .end());

        TestUtils.Process process = wrapper.wrap(
                repairAPI.startProcess(john.getId(), processDefinition.getId(), Arrays.asList("step 2"), null, null));

        process.expect("step 2").toBeStarted();
    }

    /*
     * start -> task 1(s) // task 2(s) + task 3(e) -> end
     */
    @Test
    public void should_be_able_to_start_a_process_containing_a_parallel_gateway_which_merge_steps() throws Exception {
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"), new UserTask("step 2"))
                .then(new Gateway("merge", GatewayType.PARALLEL))
                .then(new UserTask("step 3"))
                .end());

        TestUtils.Process process = wrapper.wrap(
                repairAPI.startProcess(john.getId(), processDefinition.getId(), Arrays.asList("step 1", "step 2"), null, null));
        process.execute(john, "step 1", "step 2");

        process.expect("step 3").toBeStarted();
    }

    /*
     * start -> task 1(s) + task 2(e) // task 3(e) -> end
     */
    @Test
    public void should_be_able_to_start_a_process_containing_a_parallel_gateway_which_split_steps() throws Exception {
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"))
                .then(new Gateway("split", GatewayType.PARALLEL))
                .then(new UserTask("step 2"), new UserTask("step 3"))
                .end());

        TestUtils.Process process = wrapper.wrap(
                repairAPI.startProcess(john.getId(), processDefinition.getId(), Arrays.asList("step 1"), null, null));
        process.execute(john, "step 1");

        process.expect("step 2", "step 3").toBeStarted();
    }

    /*
     * start -> step 1(s) o step 2 // step 3 o step 4(e) -> end
     */
    @Test
    public void should_be_able_to_start_a_process_containing_an_inclusive_gateway() throws Exception {
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"))
                .then(new Gateway("inclusive 1", GatewayType.INCLUSIVE))
                .then(new UserTask("step 2"), new UserTask("step 3"))
                .then(new Gateway("inclusive 2", GatewayType.INCLUSIVE))
                .then(new UserTask("step 4"))
                .end());

        TestUtils.Process process = wrapper.wrap(
                repairAPI.startProcess(john.getId(), processDefinition.getId(), Arrays.asList("step 1"), null, null));
        process.execute(john, "step 1", "step 2", "step 3");

        process.expect("step 4").toBeStarted();
    }

    /*
     * start -> step 1 // step 2(s) x step 3(e) -> end
     */
    @Test
    public void should_be_able_to_start_a_process_containing_an_exclusive_gateway_which_merge() throws Exception {
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"), new UserTask("step 2"))
                .then(new Gateway("exclusive", GatewayType.EXCLUSIVE))
                .then(new UserTask("step 3"))
                .end());

        TestUtils.Process process = wrapper.wrap(
                repairAPI.startProcess(john.getId(), processDefinition.getId(), Arrays.asList("step 2"), null, null));
        process.execute(john, "step 2");

        process.expect("step 3").toBeStarted();
    }

    /*
     * start -> step 1(s) x step 2(d) // step 3(c-e) -> end
     */
    @Test
    public void should_be_able_to_start_a_process_containing_an_exclusive_gateway_which_split() throws Exception {
        Expression TRUE = new ExpressionBuilder().createConstantBooleanExpression(true);
        ProcessDefinition processDefinition = processDeployer.deploy(designer
                .start()
                .then(new UserTask("step 1"))
                .then(new Gateway("exclusive", GatewayType.EXCLUSIVE))
                .then(
                        new UserTask("step 2").setDefault(true),
                        new UserTask("step 3").setCondition(TRUE))
                .end());

        TestUtils.Process process = wrapper.wrap(
                repairAPI.startProcess(john.getId(), processDefinition.getId(), Arrays.asList("step 1"), null, null));
        process.execute(john, "step 1");

        process.expect("step 3").toBeStarted();
    }

    private ProcessDefinitionBuilder getProcessDefinitionBuilder() {
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("Designed by designer", "1.0")
                .addActor("actor")
                .addDescription("Coding all-night-long");
        return builder;
    }

    private ProcessDeployer getProcessDeployer() {
        return new ProcessDeployer() {

            @Override
            public ProcessDefinition deploy(DesignProcessDefinition design) throws BonitaException {
                return deployAndEnableWithActor(design, "actor", john);
            }

            @Override
            public void clean(ProcessDefinition processDefinition) throws BonitaException {
                disableAndDeleteProcess(processDefinition);
            }
        };
    }
}
