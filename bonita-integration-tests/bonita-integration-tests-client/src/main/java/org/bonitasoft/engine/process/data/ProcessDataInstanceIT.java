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
package org.bonitasoft.engine.process.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.data.ArchivedDataInstance;
import org.bonitasoft.engine.bpm.data.ArchivedDataNotFoundException;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.ExceptionContext;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

public class ProcessDataInstanceIT extends TestWithUser {

    @Test
    public void getIntegerDataInstanceFromProcess() throws Exception {
        final String className = Integer.class.getName();
        final ProcessDefinition processDefinition = operateProcess(user, "var1", new ExpressionBuilder().createConstantIntegerExpression(1), className);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        final List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertFalse(processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(1, processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getShortTextDataInstanceFromProcess() throws Exception {
        final String className = String.class.getName();
        final ProcessDefinition processDefinition = operateProcess(user, "var1", new ExpressionBuilder().createConstantStringExpression("aaa"), className);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        final List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertFalse(processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals("aaa", processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void processWithShortAndLongTextData() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("processWithlongAndshortText", "1.0");
        builder.addUserTask("step1", ACTOR_NAME);
        builder.addActor(ACTOR_NAME);
        final String shortTextValue = "shortTextValue";
        builder.addShortTextData("shortTextData", new ExpressionBuilder().createConstantStringExpression(shortTextValue));
        final String longTextValue = "longTextValue";
        final StringBuilder longBuilder = new StringBuilder(longTextValue);
        for (int i = 0; i < 10; i++) {
            longBuilder.append(longTextValue);
        }
        builder.addLongTextData("longTextData", new ExpressionBuilder().createConstantStringExpression(longBuilder.toString()));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        assertEquals(shortTextValue, getProcessAPI().getProcessDataInstance("shortTextData", processInstance.getId()).getValue());
        assertEquals(longBuilder.toString(), getProcessAPI().getProcessDataInstance("longTextData", processInstance.getId()).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getDoubleDataInstanceFromProcess() throws Exception {
        final String className = Double.class.getName();
        final ProcessDefinition processDefinition = operateProcess(user, "var1", new ExpressionBuilder().createConstantDoubleExpression(1.5), className);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        final List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertFalse(processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(1.5, processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class, DataInstance.class }, concept = BPMNConcept.PROCESS, keywords = { "Float", "DataInstance" }, jira = "ENGINE-563")
    @Test
    public void getFloatDataInstanceFromProcess() throws Exception {
        final String className = Float.class.getName();
        final ProcessDefinition processDefinition = operateProcess(user, "var1", new ExpressionBuilder().createConstantFloatExpression(1.5f), className);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        final List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertFalse(processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(1.5f, processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getBooleanDataInstanceFromProcess() throws Exception {
        final String className = Boolean.class.getName();
        final ProcessDefinition processDefinition = operateProcess(user, "var1", new ExpressionBuilder().createConstantBooleanExpression(true), className);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        final List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertFalse(processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(true, processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { Date.class, DataInstance.class, ProcessAPI.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "Date", "Data", "Expression" }, jira = "ENGINE-1559, ENGINE-1099")
    @Test
    public void getDateDataInstanceFromProcess() throws Exception {
        final ProcessDefinition processDefinition = operateProcess(user, "var1",
                new ExpressionBuilder().createConstantDateExpression("2013-07-18T14:49:26.86+02:00"), Date.class.getName());
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        final List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertFalse(processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertNotNull(processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getLongDataInstanceFromProcess() throws Exception {
        final String className = Long.class.getName();
        final ProcessDefinition processDefinition = operateProcess(user, "var1", new ExpressionBuilder().createConstantLongExpression(1), className);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        final List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertFalse(processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(1L, processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void updateProcessDataInstance() throws Exception {
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(ACTOR_NAME)
                .addDescription("Delivery all day and night long").addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(1))
                .addUserTask("step1", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);

        // test execution
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // verify the retrieved data
        List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertTrue(!processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(1, processDataInstances.get(0).getValue());

        getProcessAPI().updateProcessDataInstance("var1", processInstance.getId(), 2);

        // retrieve data after the update
        processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertTrue(!processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals(2, processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.DATA, jira = "BS-1984", keywords = { "update", "process data", "wrong type" })
    @Test
    public void cantUpdateProcessDataInstanceWithWrongValue() throws Exception {
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(ACTOR_NAME)
                .addDescription("Delivery all day and night long")
                .addData("data", List.class.getName(), null)
                .addUserTask("step1", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);

        // test execution
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");

        // verify the retrieved data
        try {
            getProcessAPI().updateProcessDataInstance("data", processInstance.getId(), "wrong value");
            fail();
        } catch (final UpdateException e) {
            assertEquals("USERNAME=" + USERNAME + " | DATA_NAME=data | DATA_CLASS_NAME=java.util.List | The type of new value [" + String.class.getName()
                    + "] is not compatible with the type of the data.", e.getMessage());
            final Map<ExceptionContext, Serializable> exceptionContext = e.getContext();
            assertEquals(List.class.getName(), exceptionContext.get(ExceptionContext.DATA_CLASS_NAME));
            assertEquals("data", exceptionContext.get(ExceptionContext.DATA_NAME));
        }

        // retrieve data after the update
        final List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertEquals(1, processDataInstances.size());
        assertEquals(null, processDataInstances.get(0).getValue());

        // Evaluate the data
        final List<Expression> dependencies = Collections.singletonList(new ExpressionBuilder().createDataExpression("data", List.class.getName()));
        final Expression longExpression = new ExpressionBuilder().createGroovyScriptExpression("Script",
                "data = new ArrayList<String>(); data.add(\"plop\"); return data;", List.class.getName(), dependencies);
        final Map<Expression, Map<String, Serializable>> expressions = Collections.singletonMap(longExpression, Collections.<String, Serializable> emptyMap());
        getProcessAPI().evaluateExpressionsOnActivityInstance(step1Id, expressions);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void updateProcessDataInstanceTwice() throws Exception {
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(ACTOR_NAME)
                .addDescription("Delivery all day and night long").addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(1))
                .addUserTask("step1", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // test execution
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        // verify the retrieved data
        List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertTrue(!processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(1, processDataInstances.get(0).getValue());

        getProcessAPI().updateProcessDataInstance("var1", processInstance.getId(), 2);

        // retrieve data after the update
        processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertTrue(!processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals(2, processDataInstances.get(0).getValue());

        getProcessAPI().updateProcessDataInstance("var1", processInstance.getId(), 3);

        // retrieve data after the update
        processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertTrue(!processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals(3, processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = RetrieveException.class)
    public void dataNotAvailableAfterArchiveFromProcess() throws Exception {
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(ACTOR_NAME)
                .addDescription("Delivery all day and night long").addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(1))
                .addUserTask("step1", ACTOR_NAME).getProcess();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // test execution
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        // verify the retrieved data
        List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertTrue(!processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(1, processDataInstances.get(0).getValue());

        // Execute pending task
        waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        waitForProcessToFinish(processInstance);

        // retrieve data after process has finished
        try {
            processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    private ProcessDefinition operateProcess(final User user, final String dataName, final Expression expression, final String className) throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0");
        if (className.equals(Integer.class.getName())) {
            processDefinitionBuilder.addIntegerData(dataName, expression);
        } else if (className.equals(Long.class.getName())) {
            processDefinitionBuilder.addLongData(dataName, expression);
        } else if (className.equals(Double.class.getName())) {
            processDefinitionBuilder.addDoubleData(dataName, expression);
        } else if (className.equals(Float.class.getName())) {
            processDefinitionBuilder.addFloatData(dataName, expression);
        } else if (className.equals(Boolean.class.getName())) {
            processDefinitionBuilder.addBooleanData(dataName, expression);
        } else if (className.equals(Date.class.getName())) {
            processDefinitionBuilder.addDateData(dataName, expression);
        } else if (className.equals(String.class.getName())) {
            processDefinitionBuilder.addShortTextData(dataName, expression);
        }
        final DesignProcessDefinition processDef = processDefinitionBuilder.addActor(ACTOR_NAME).addDescription("Delivery all day and night long")
                .addUserTask("step1", ACTOR_NAME).getProcess();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        return processDefinition;
    }

    @Cover(jira = "ENGINE-1820", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
            "process instance" })
    @Test
    public void getArchivedProcessDataInstance() throws Exception {
        final String dataName = "title";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("1"));
        builder.addActor("actor");
        builder.addUserTask("step", "actor");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.getProcess(), "actor", user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().updateProcessDataInstance(dataName, processInstance.getId(), "2");

        final ArchivedDataInstance archivedData = getProcessAPI().getArchivedProcessDataInstance(dataName, processInstance.getId());
        assertEquals("2", archivedData.getValue());
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1820", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
            "process instance" })
    @Test
    public void getArchivedProcessDataInstanceFromAnArchivedProcess() throws Exception {
        final String dataName = "title";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("1"));
        builder.addAutomaticTask("system");

        final ProcessDefinition processDefinition = deployAndEnableProcess(builder.getProcess());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForProcessToFinish(processInstance);

        final ArchivedDataInstance archivedData = getProcessAPI().getArchivedProcessDataInstance(dataName, processInstance.getId());
        assertEquals("1", archivedData.getValue());
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1820, ENGINE-1946", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = {
            "Not archived", "transient data", "process instance" })
    @Test
    public void dontArchivedTransientProcessDataInstance() throws Exception {
        final String dataName = "test";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("1")).isTransient();
        builder.addActor("actor");
        builder.addUserTask("step", "actor");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.getProcess(), "actor", user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().updateProcessDataInstance(dataName, processInstance.getId(), "2");
        waitForUserTaskAndExecuteIt(processInstance, "step", user);
        waitForProcessToFinish(processInstance);

        try {
            getProcessAPI().getArchivedProcessDataInstance(dataName, processInstance.getId());
        } catch (final ArchivedDataNotFoundException e) {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Cover(jira = "ENGINE-1820", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
            "process instance" })
    @Test
    public void getUnknownArchivedProcessDataInstance() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addAutomaticTask("system");

        final ProcessDefinition processDefinition = deployAndEnableProcess(builder.getProcess());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForProcessToFinish(processInstance);

        try {
            getProcessAPI().getArchivedProcessDataInstance("o", processInstance.getId());
            fail("The data named 'o' does not exists");
        } catch (final ArchivedDataNotFoundException dnfe) {
            // Do nothing
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Cover(jira = "ENGINE-1822", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
            "process instance" })
    @Test
    public void getArchivedProcessDataInstances() throws Exception {
        final String dataName = "title";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("1"));
        builder.addShortTextData("job", new ExpressionBuilder().createConstantStringExpression("job"));
        builder.addShortTextData("desc", new ExpressionBuilder().createConstantStringExpression("desc"));
        builder.addActor("actor");
        builder.addUserTask("step", "actor");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.getProcess(), "actor", user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().updateProcessDataInstance(dataName, processInstance.getId(), "2");

        List<ArchivedDataInstance> archivedDataInstances = getProcessAPI().getArchivedProcessDataInstances(processInstance.getId(), 0, 10);
        assertEquals(3, archivedDataInstances.size());
        ArchivedDataInstance archivedDataInstance = getArchivedDataInstance(archivedDataInstances, dataName);
        assertEquals("2", archivedDataInstance.getValue());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "job");
        assertEquals("job", archivedDataInstance.getValue());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "desc");
        assertEquals("desc", archivedDataInstance.getValue());

        archivedDataInstances = getProcessAPI().getArchivedProcessDataInstances(processInstance.getId(), 0, 1);
        assertEquals(1, archivedDataInstances.size());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, dataName);
        assertEquals("2", archivedDataInstance.getValue());

        archivedDataInstances = getProcessAPI().getArchivedProcessDataInstances(processInstance.getId(), 1, 10);
        assertEquals(2, archivedDataInstances.size());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "job");
        assertEquals("job", archivedDataInstance.getValue());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "desc");
        assertEquals("desc", archivedDataInstance.getValue());

        waitForUserTaskAndExecuteIt(processInstance, "step", user);
        waitForProcessToFinish(processInstance);

        archivedDataInstances = getProcessAPI().getArchivedProcessDataInstances(processInstance.getId(), 0, 10);
        assertEquals(3, archivedDataInstances.size());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, dataName);
        assertEquals("2", archivedDataInstance.getValue());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "job");
        assertEquals("job", archivedDataInstance.getValue());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "desc");
        assertEquals("desc", archivedDataInstance.getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1822", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
            "process instance" })
    @Test
    public void getEmptyArchivedProcessDataInstances() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addActor("actor");
        builder.addUserTask("step", "actor");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.getProcess(), "actor", user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final List<ArchivedDataInstance> archivedDataInstances = getProcessAPI().getArchivedProcessDataInstances(processInstance.getId(), 0, 10);
        assertEquals(0, archivedDataInstances.size());

        disableAndDeleteProcess(processDefinition);
    }

}
