package org.bonitasoft.engine.filter.user;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.connectors.TestConnector;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.test.check.CheckNbAssignedTaskOf;
import org.bonitasoft.engine.test.check.CheckNbPendingTaskOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 */
public class UserFilterTest extends CommonAPITest {

    private static final String JOHN = "john";

    private static final String JACK = "jack";

    private static final String JAMES = "james";

    private User john;

    private User jack;

    private User james;

    @After
    public void afterTest() throws BonitaException {
        deleteUser(JOHN);
        deleteUser(JACK);
        deleteUser(JAMES);
        VariableStorage.clearAll();
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
        john = createUser(JOHN, "bpm");
        jack = createUser(JACK, "bpm");
        james = createUser(JAMES, "bpm");
        logout();
        loginWith(JOHN, "bpm");
    }

    @Test
    public void filterTask() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithUserFilter", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addAutomaticTask("step1");
        final UserTaskDefinitionBuilder addUserTask = designProcessDefinition.addUserTask("step2", delivery);
        addUserTask.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilter", "1.0").addInput("userId",
                new ExpressionBuilder().createConstantLongExpression(jack.getId()));
        designProcessDefinition.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployProcessWithTestFilter(delivery, john.getId(), designProcessDefinition, "TestFilter");

        getProcessAPI().startProcess(processDefinition.getId());

        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 1, jack).waitUntil());
        List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());
        disableAndDeleteProcess(processDefinition);
    }

    /*
     * Task must be in failed state even for any exception thrown is the user filter
     */
    @Test
    public void filterTaskWithUserFilterNotFound() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithUserFilter", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addAutomaticTask("step1");
        final UserTaskDefinitionBuilder addUserTask = designProcessDefinition.addUserTask("step2", delivery);
        addUserTask.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilterWithClassNotFound", "1.0").addInput("userId",
                new ExpressionBuilder().createConstantLongExpression(jack.getId()));
        designProcessDefinition.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployProcessWithTestFilter(delivery, john.getId(), designProcessDefinition, "TestFilterWithClassNotFound");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        waitForTaskToFail(processInstance);

        disableAndDeleteProcess(processDefinition.getId());
    }

    /*
     * Task must be in failed state even for any exception thrown is the user filter
     */
    @Test
    public void filterTaskWithUserFilterThatThrowException() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder processWithRuntime = new ProcessDefinitionBuilder().createNewInstance("processWithUserFilterRuntimeException", "1.0");
        processWithRuntime.addActor(delivery).addDescription("Delivery all day and night long");
        processWithRuntime.addAutomaticTask("step1");
        final UserTaskDefinitionBuilder addUserTask = processWithRuntime.addUserTask("step2", delivery);
        addUserTask.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilterThatThrowException", "1.0").addInput("exception",
                new ExpressionBuilder().createConstantStringExpression("runtime"));
        processWithRuntime.addTransition("step1", "step2");
        final ProcessDefinition processDefinitionWithRuntime = deployProcessWithTestFilter(delivery, john.getId(), processWithRuntime,
                "TestFilterThatThrowException");
        final ProcessDefinitionBuilder processWithException = new ProcessDefinitionBuilder().createNewInstance("processWithUserFilterNormalException", "1.0");
        processWithException.addActor(delivery).addDescription("Delivery all day and night long");
        processWithException.addAutomaticTask("step1");
        final UserTaskDefinitionBuilder addUserTask2 = processWithException.addUserTask("step2", delivery);
        addUserTask2.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilterThatThrowException", "1.0").addInput("exception",
                new ExpressionBuilder().createConstantStringExpression("normal"));
        processWithException.addTransition("step1", "step2");
        final ProcessDefinition processDefinitionWithException = deployProcessWithTestFilter(delivery, john.getId(), processWithException,
                "TestFilterThatThrowException");
        final ProcessDefinitionBuilder processWithNoClassDef = new ProcessDefinitionBuilder().createNewInstance("processWithUserFilterNoClassDef", "1.0");
        processWithNoClassDef.addActor(delivery).addDescription("Delivery all day and night long");
        processWithNoClassDef.addAutomaticTask("step1");
        final UserTaskDefinitionBuilder addUserTask3 = processWithNoClassDef.addUserTask("step2", delivery);
        addUserTask3.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilterThatThrowNoClassDef", "1.0").addInput("exception",
                new ExpressionBuilder().createConstantStringExpression("normal"));
        processWithNoClassDef.addTransition("step1", "step2");
        final ProcessDefinition processDefinitionNoClassDef = deployProcessWithTestFilter(delivery, john.getId(), processWithNoClassDef,
                "TestFilterThatThrowNoClassDef");

        final ProcessInstance processInstanceException = getProcessAPI().startProcess(processDefinitionWithException.getId());
        final ProcessInstance processInstanceRuntime = getProcessAPI().startProcess(processDefinitionWithRuntime.getId());
        final ProcessInstance processInstanceNoClassDef = getProcessAPI().startProcess(processDefinitionNoClassDef.getId());

        waitForTaskToFail(processInstanceRuntime);
        waitForTaskToFail(processInstanceException);
        waitForTaskToFail(processInstanceNoClassDef);

        disableAndDeleteProcess(processDefinitionWithRuntime);
        disableAndDeleteProcess(processDefinitionWithException);
        disableAndDeleteProcess(processDefinitionNoClassDef);
    }

    @Test
    public void filterTaskWithAutoAssign() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithUserFilterWithAutoAssign", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addAutomaticTask("step1");
        final UserTaskDefinitionBuilder addUserTask = designProcessDefinition.addUserTask("step2", delivery);
        addUserTask.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilterWithAutoAssign", "1.0").addInput("userId",
                new ExpressionBuilder().createConstantLongExpression(jack.getId()));
        designProcessDefinition.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployProcessWithTestFilter(delivery, john.getId(), designProcessDefinition, "TestFilterWithAutoAssign");

        getProcessAPI().startProcess(processDefinition.getId());

        assertTrue(new CheckNbAssignedTaskOf(getProcessAPI(), 50, 5000, false, 1, jack).waitUntil());
        List<HumanTaskInstance> tasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, tasks.size());
        tasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(1, tasks.size());
        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void filterTaskWithNullInput() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithUserFilterUsingActorName", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addAutomaticTask("step1");
        final UserTaskDefinitionBuilder addUserTask = designProcessDefinition.addUserTask("step2", delivery);
        addUserTask.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilterUsingActorName", "1.0").addInput("userIds", null);
        designProcessDefinition.addTransition("step1", "step2");
        deployProcessWithTestFilter(delivery, john.getId(), designProcessDefinition, "TestFilterUsingActorName");
    }

    @Test
    public void filterTaskUsingFilterName() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithUserFilterUsingActorName", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addAutomaticTask("step1");
        final UserTaskDefinitionBuilder addUserTask = designProcessDefinition.addUserTask("step2", delivery);
        addUserTask.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilterUsingActorName", "1.0").addInput(
                "userIds",
                new ExpressionBuilder().createGroovyScriptExpression("myScript", "['" + delivery + "':" + james.getId() + "l,'notDelivery':" + jack.getId()
                        + "l]", Map.class.getName()));
        designProcessDefinition.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployProcessWithTestFilter(delivery, john.getId(), designProcessDefinition, "TestFilterUsingActorName");

        getProcessAPI().startProcess(processDefinition.getId());

        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 6000, false, 1, james).waitUntil());
        List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(james.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());
        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployProcessWithTestFilter(final String actorName, final long userId, final ProcessDefinitionBuilder designProcessDefinition,
            final String filterName) throws BonitaException, IOException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                designProcessDefinition.done());
        final List<BarResource> impl = generateFilterImplementations(filterName);
        for (final BarResource barResource : impl) {
            businessArchiveBuilder.addUserFilters(barResource);
        }
        final List<BarResource> generateFilterDependencies = generateFilterDependencies();
        for (final BarResource barResource : generateFilterDependencies) {
            businessArchiveBuilder.addClasspathResource(barResource);
        }

        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchiveBuilder.done());
        addMappingOfActorsForUser(actorName, userId, processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    private List<BarResource> generateFilterDependencies() throws IOException {
        final List<BarResource> resources = new ArrayList<BarResource>(1);
        byte[] data = IOUtil.generateJar(TestFilterThatThrowException.class);
        resources.add(new BarResource("TestFilterThatThrowException.jar", data));
        data = IOUtil.generateJar(TestFilter.class);
        resources.add(new BarResource("TestFilter.jar", data));
        data = IOUtil.generateJar(TestFilterWithAutoAssign.class);
        resources.add(new BarResource("TestFilterWithAutoAssign.jar", data));
        data = IOUtil.generateJar(TestFilterUsingActorName.class);
        resources.add(new BarResource("TestFilterUsingActorName.jar", data));
        return resources;
    }

    private List<BarResource> generateFilterImplementations(final String filterName) throws IOException {
        final List<BarResource> resources = new ArrayList<BarResource>(1);
        final InputStream inputStream = TestConnector.class.getClassLoader().getResourceAsStream("org/bonitasoft/engine/filter/user/" + filterName + ".impl");
        final byte[] data = IOUtil.getAllContentFrom(inputStream);
        inputStream.close();
        resources.add(new BarResource(filterName + ".impl", data));
        return resources;
    }

}
