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
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;

public class BDRIT extends CommonAPISPTest {

    @Before
    public void before() throws Exception {
        login();
    }

    @After
    public void after() throws BonitaException {
        logout();
    }

    @Test
    public void deployABDRAndExecuteAGroovyScriptWhichContainsAPOJOFromTheBDR() throws BonitaException, IOException {
        final InputStream stream = BDRIT.class.getResourceAsStream("/bdr-jar.bak");
        assertNotNull(stream);
        final byte[] jar = IOUtils.toByteArray(stream);
        getTenantManagementAPI().deployBusinessDataRepository(jar);

        final Expression stringExpression = new ExpressionBuilder().createGroovyScriptExpression("alive",
                "import org.bonita.pojo.Employee; Employee e = new Employee(); e.firstName = 'John'; e.lastName = 'Doe'; return e.toString(); ",
                String.class.getName());
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>();
        expressions.put(stringExpression, new HashMap<String, Serializable>());

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("test", "1.2-alpha");
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

}
