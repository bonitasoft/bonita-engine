package com.bonitasoft.engine.bdr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;

public class BDRIT extends CommonAPISPTest {

    private User matti;

    @Before
    public void setUp() throws Exception {
        login();
        matti = createUser("matti", "bpm");

        final InputStream stream = BDRIT.class.getResourceAsStream("/bdr-jar.bak");
        assertNotNull(stream);
        final byte[] jar = IOUtils.toByteArray(stream);
        getTenantManagementAPI().deployBusinessDataRepository(jar);
    }

    @After
    public void tearDown() throws BonitaException {
        deleteUser(matti);
        logout();
    }

    @Test
    @Ignore("Disabled until we support undeploy of a bdr, otherwise the second test fails")
    public void deployABDRAndExecuteAGroovyScriptWhichContainsAPOJOFromTheBDR() throws BonitaException, IOException {

        final Expression stringExpression = new ExpressionBuilder().createGroovyScriptExpression("alive",
                "import org.bonita.pojo.Employee; Employee e = new Employee(); e.firstName = 'John'; e.lastName = 'Doe'; return e.toString(); ",
                String.class.getName());
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>();
        expressions.put(stringExpression, new HashMap<String, Serializable>());

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addAutomaticTask("stepO");
        final ProcessDefinition processDefinition = getProcessAPI().deploy(processDefinitionBuilder.done());
        getProcessAPI().enableProcess(processDefinition.getId());
        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnProcessDefinition(processDefinition.getId(), expressions);
        assertEquals(1, result.size());

        final Set<Entry<String, Serializable>> entrySet = result.entrySet();
        final Entry<String, Serializable> entry = entrySet.iterator().next();
        assertEquals("Employee [id=null, firstName=John, lastName=Doe]", entry.getValue());

        disableAndDeleteProcess(processDefinition.getId());
    }

    @Test
    @Ignore("Disabled until we support undeploy of a bdr, otherwise the second test fails")
    public void deployABDRAndCreateABusinessData() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder()
                .createGroovyScriptExpression("createNewEmployee",
                        "import org.bonita.pojo.Employee; Employee e = new Employee(); e.firstName = 'John'; e.lastName = 'Doe'; return e;",
                        "org.bonita.pojo.Employee");

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addOperation(new LeftOperandBuilder().createNewInstance("myEmployee").done(),
                OperatorType.CREATE_BUSINESS_DATA, null, null, employeeExpression);

        final ProcessDefinition definition = deployAndEnableWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());

        final HumanTaskInstance userTask = waitForUserTask("step1", instance.getId());
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());
        getProcessAPI().executeFlowNode(userTask.getId());

        disableAndDeleteProcess(definition.getId());
    }

    @Test
    public void deployABDRAndCreateADefaultBusinessData() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder()
                .createGroovyScriptExpression("createNewEmployee",
                        "import org.bonita.pojo.Employee; Employee e = new Employee(); e.firstName = 'John'; e.lastName = 'Doe'; return e;",
                        "org.bonita.pojo.Employee");

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addBusinessData("myEmployee", "org.bonita.pojo.Employee", employeeExpression);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);

        final ProcessDefinition definition = deployAndEnableWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());

        waitForUserTask("step1", instance.getId());
        final Object businessDataInstance = getProcessAPI().getBusinessDataInstance("myEmployee", instance.getId());
        assertNotNull(businessDataInstance);

        disableAndDeleteProcess(definition.getId());
    }

}
