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
package org.bonitasoft.engine.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.form.FormMappingDefinitionBuilder;
import org.bonitasoft.engine.bpm.form.FormMappingModelBuilder;
import org.bonitasoft.engine.bpm.parameter.ParameterCriterion;
import org.bonitasoft.engine.bpm.parameter.ParameterInstance;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.identity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProcessParameterIT extends CommonAPIIT {

    @After
    public void afterTest() throws BonitaException {
        logoutOnTenant();
    }

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    @Test
    public void getNoParametersWhenAddingNoParameters() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final int numberOfParamters = getProcessAPI().getNumberOfParameterInstances(definition.getId());
        assertEquals(0, numberOfParamters);

        deleteProcess(definition);
    }

    @Test
    public void getNumberOfParameters() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("key1", String.class.getCanonicalName()).addParameter("key2", String.class.getCanonicalName())
                .addParameter("key3", String.class.getCanonicalName()).addParameter("key4", String.class.getCanonicalName()).addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final int numberOfParamters = getProcessAPI().getNumberOfParameterInstances(definition.getId());
        assertEquals(4, numberOfParamters);

        deleteProcess(definition);
    }

    @Test(expected = RetrieveException.class)
    public void getNumberOfParametersThrowsAnExceptionBecauseTheProcessDoesNotExist() {
        getProcessAPI().getNumberOfParameterInstances(45);
    }

    @Test
    public void getNoParameters() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 20, ParameterCriterion.NAME_DESC);
        assertEquals(0, parameters.size());

        deleteProcess(definition);
    }

    @Test
    public void getParameters() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("key1", String.class.getCanonicalName()).addParameter("key.2", String.class.getCanonicalName())
                .addUserTask("userTask1", null);

        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("key1", "engine");
        params.put("key.2", "bos");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 20, ParameterCriterion.NAME_ASC);
        assertEquals(2, parameters.size());
        final ParameterInstance firstParameter = parameters.get(0);
        assertEquals("key.2", firstParameter.getName());
        assertEquals("bos", firstParameter.getValue());
        final ParameterInstance secondParameter = parameters.get(1);
        assertEquals("key1", secondParameter.getName());
        assertEquals("engine", secondParameter.getValue());

        deleteProcess(definition);
    }

    @Test
    public void getParameter() throws BonitaException {
        final String parameterValue = "a very important piece of information";
        final String parameterName = "myParam1";
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("getParameter", "1.0");
        processBuilder.addParameter("myParam1", String.class.getCanonicalName()).addDescription("Parameter description");
        processBuilder.addParameter("myParam2", String.class.getCanonicalName()).addUserTask("userTask1", null);

        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put(parameterName, parameterValue);
        params.put("myParam2", "an unused parameter");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final ParameterInstance parameter = getProcessAPI().getParameterInstance(definition.getId(), parameterName);
        assertEquals(parameterName, parameter.getName());
        assertEquals(parameterValue, parameter.getValue());
        assertEquals("Parameter description", parameter.getDescription());

        deleteProcess(definition);
    }

    @Test
    public void setProcessDataDefaultValueWithParameterValue() throws Exception {
        final User user = createUser("jules", "his_password");
        final String parameterName = "anotherParam";
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("setDataDefaultValueWithParameter", "9.23");
        processBuilder.addActor(ACTOR_NAME);
        final String aTask = "userTask1";
        final String dataName = "aData";
        processBuilder
                .addParameter(parameterName, String.class.getCanonicalName())
                .addData(dataName, String.class.getName(),
                        new ExpressionBuilder().createParameterExpression("takes value of default parameter value", parameterName, String.class.getName()))
                .addUserTask(aTask, ACTOR_NAME);

        final DesignProcessDefinition design = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(design);
        final Map<String, String> params = new HashMap<String, String>(1);
        final String paramValue = "4 is the answer";
        params.put(parameterName, paramValue);
        businessArchive.setParameters(params);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchive.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, aTask);
        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance(dataName, processInstance.getId());
        assertEquals(paramValue, dataInstance.getValue());

        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Test
    public void deployWithNullParamAndFormMappings() throws BonitaException {
        final String parameterName = "myParam1";
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("getParameter", "1.0");
        processBuilder.addParameter("myParam1", String.class.getCanonicalName()).addParameter("myParam2", String.class.getCanonicalName())
                .addUserTask("userTask1", null);

        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put(parameterName, null);
        businessArchive.setParameters(params);
        businessArchive.setFormMappings(FormMappingModelBuilder.buildFormMappingModel().withFormMapping(
                FormMappingDefinitionBuilder.buildFormMapping("somePage", FormMappingType.TASK, FormMappingTarget.INTERNAL).withTaskname("someTask").build()).build());

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());

        deleteProcess(definition);
    }

    @Test(expected = RetrieveException.class)
    public void getParameterOfAnUnknownProcess() throws BonitaException {
        getProcessAPI().getParameterInstance(123456789l, "unknown");
    }

    @Test(expected = NotFoundException.class)
    public void getUnknownParameter() throws BonitaException {
        final String parameterValue = "a very important piece of information";
        final String parameterName = "myParam1";
        final String wrongParameterName = "wrongParameterName";
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("getUnknownParameter", "1.0");
        processBuilder.addParameter("myParam1", String.class.getCanonicalName()).addParameter("myParam2", String.class.getCanonicalName())
                .addUserTask("userTask1", null);

        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put(parameterName, parameterValue);
        params.put("myParam2", "an unused parameter");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        try {
            getProcessAPI().getParameterInstance(definition.getId(), wrongParameterName);
        } finally {
            deleteProcess(definition);
        }
    }

    @Test
    public void sortParametersByNameAsc() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addParameter("bear", String.class.getCanonicalName())
                .addParameter("squirrel", String.class.getCanonicalName()).addParameter("donkey", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("donkey", "engine");
        params.put("bear", "bos");
        params.put("squirrel", "bee");
        params.put("bee", "busy");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 20, ParameterCriterion.NAME_ASC);
        assertEquals(4, parameters.size());
        assertEquals("bear", parameters.get(0).getName());
        assertEquals("bee", parameters.get(1).getName());
        assertEquals("donkey", parameters.get(2).getName());
        assertEquals("squirrel", parameters.get(3).getName());

        deleteProcess(definition);
    }

    @Test
    public void sortParametersByNameDesc() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addParameter("bear", String.class.getCanonicalName())
                .addParameter("squirrel", String.class.getCanonicalName()).addParameter("donkey", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("donkey", "engine");
        params.put("bear", "bos");
        params.put("squirrel", "bee");
        params.put("bee", "busy");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 20, ParameterCriterion.NAME_DESC);
        assertEquals(4, parameters.size());
        assertEquals("squirrel", parameters.get(0).getName());
        assertEquals("donkey", parameters.get(1).getName());
        assertEquals("bee", parameters.get(2).getName());
        assertEquals("bear", parameters.get(3).getName());

        deleteProcess(definition);
    }

    @Test
    public void getPageOne() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addParameter("bear", String.class.getCanonicalName())
                .addParameter("squirrel", String.class.getCanonicalName()).addParameter("donkey", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("donkey", "engine");
        params.put("bear", "bos");
        params.put("squirrel", "bee");
        params.put("bee", "busy");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 2, ParameterCriterion.NAME_DESC);
        assertEquals(2, parameters.size());
        assertEquals("squirrel", parameters.get(0).getName());
        assertEquals("donkey", parameters.get(1).getName());

        deleteProcess(definition);
    }

    @Test
    public void getPageTwo() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addParameter("bear", String.class.getCanonicalName())
                .addParameter("squirrel", String.class.getCanonicalName()).addParameter("donkey", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("donkey", "engine");
        params.put("bear", "bos");
        params.put("squirrel", "bee");
        params.put("bee", "busy");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 2, 2, ParameterCriterion.NAME_DESC);
        assertEquals(2, parameters.size());
        assertEquals("bee", parameters.get(0).getName());
        assertEquals("bear", parameters.get(1).getName());

        deleteProcess(definition);
    }

    @Test
    public void getPageTwoOutOfBound() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addParameter("bear", String.class.getCanonicalName())
                .addParameter("squirrel", String.class.getCanonicalName()).addParameter("donkey", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("donkey", "engine");
        params.put("bear", "bos");
        params.put("squirrel", "bee");
        params.put("bee", "busy");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final List<ParameterInstance> parameterInstances = getProcessAPI().getParameterInstances(definition.getId(), 8, 8, ParameterCriterion.NAME_ASC);
        assertEquals(0, parameterInstances.size());
        deleteProcess(definition);
    }

    @Test
    public void emptyParameterIsAValidValue() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("emptyParameterIsAValidValue", "1.7");
        processBuilder.addParameter("Astronaut", String.class.getCanonicalName()).addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>(1);
        params.put("Astronaut", "");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ConfigurationState.RESOLVED, processDeploymentInfo.getConfigurationState());

        deleteProcess(definition);
    }

    @Test
    public void resolvedDependencies() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addParameter("bear", String.class.getCanonicalName())
                .addParameter("squirrel", String.class.getCanonicalName()).addParameter("donkey", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("donkey", "engine");
        params.put("bear", "bos");
        params.put("squirrel", "bee");
        params.put("bee", "busy");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ConfigurationState.RESOLVED, processDeploymentInfo.getConfigurationState());

        deleteProcess(definition);
    }

    @Test
    public void showResolvedAndUnresolvedParameters() throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("bee", String.class.getCanonicalName()).addDescription("description").addParameter("bear", String.class.getCanonicalName())
                .addUserTask("userTask1", null);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("bee", "busy");
        businessArchive.setParameters(params);

        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());

        final List<ParameterInstance> parameters = getProcessAPI().getParameterInstances(definition.getId(), 0, 20, ParameterCriterion.NAME_DESC);
        assertEquals(2, parameters.size());
        final ParameterInstance parameter1 = parameters.get(0);
        assertEquals("bee", parameter1.getName());
        assertEquals("busy", parameter1.getValue());
        assertEquals("description", parameter1.getDescription());
        assertEquals(String.class.getCanonicalName(), parameter1.getType());
        final ParameterInstance parameter2 = parameters.get(1);
        assertEquals("bear", parameter2.getName());
        assertNull(parameter2.getValue());
        assertNull(parameter2.getDescription());
        assertEquals(String.class.getCanonicalName(), parameter2.getType());
        deleteProcess(definition);
    }

    @Test
    public void testParametersAreWellTyped() throws Exception {
        final String actor = "acting";
        final User jack = createUserAndLogin("jack", "leaking_caldron");
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(
                "testCantResolveDataInExpressionInDataDefaultValue", "1");
        processBuilder.addActor(actor).addDescription("Process to test archiving mechanism");
        processBuilder.addDoubleData("aData", null);
        processBuilder.addParameter("integerValue", String.class.getName());
        processBuilder.addParameter("booleanValue", String.class.getName());
        processBuilder.addParameter("doubleValue", String.class.getName());
        processBuilder.addUserTask("humanTask", actor);

        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("integerValue", "15");
        parameters.put("booleanValue", "true");
        parameters.put("doubleValue", "1.1");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndParameters(processBuilder.done(), actor, jack, parameters);

        final Expression integerParameter = new ExpressionBuilder().createParameterExpression("integerExpression", "integerValue", Integer.class.getName());
        final Expression isIntegerValueInteger = new ExpressionBuilder().createGroovyScriptExpression("testIntegerValueToBeInteger",
                "integerValue instanceof Integer", Boolean.class.getName(), integerParameter);

        final Expression booleanParameter = new ExpressionBuilder().createParameterExpression("booleanParameter", "booleanValue", Boolean.class.getName());
        final Expression isBooleanValueBoolean = new ExpressionBuilder().createGroovyScriptExpression("testBooleanValueToBeBoolean",
                "booleanValue instanceof Boolean", Boolean.class.getName(), booleanParameter);

        final Expression doubleParameter = new ExpressionBuilder().createParameterExpression("doubleExpression", "doubleValue", Double.class.getName());
        final Expression isDoubleValueDouble = new ExpressionBuilder().createGroovyScriptExpression("testDoubleValueToBeDouble",
                "doubleValue instanceof Double", Boolean.class.getName(), doubleParameter);

        final Map<String, Serializable> inputValues = new HashMap<String, Serializable>(0);
        assertThat((Boolean) getProcessAPI().evaluateExpressionOnProcessDefinition(isIntegerValueInteger, inputValues, processDefinition.getId()), is(true));
        assertThat((Boolean) getProcessAPI().evaluateExpressionOnProcessDefinition(isBooleanValueBoolean, inputValues, processDefinition.getId()), is(true));
        assertThat((Boolean) getProcessAPI().evaluateExpressionOnProcessDefinition(isDoubleValueDouble, inputValues, processDefinition.getId()), is(true));

        disableAndDeleteProcess(processDefinition);
        deleteUser(jack);
    }

}
