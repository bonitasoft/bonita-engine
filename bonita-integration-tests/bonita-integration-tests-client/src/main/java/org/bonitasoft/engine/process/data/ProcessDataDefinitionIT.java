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

import static org.bonitasoft.engine.matchers.NameMatcher.nameIs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.junit.Test;

public class ProcessDataDefinitionIT extends TestWithUser {

    @Test
    public void getProcessDataDefinitions() throws Exception {
        final String intDataName = "luckyNum";
        final Expression intDefaultExp = new ExpressionBuilder().createConstantIntegerExpression(10);
        final String strDataName = "luckyColor";
        final Expression strDefaultExp = new ExpressionBuilder().createConstantStringExpression("blue");

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addIntegerData(intDataName, intDefaultExp);
        processDefinitionBuilder.addShortTextData(strDataName, strDefaultExp);
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final List<DataDefinition> dataDefList = getProcessAPI().getProcessDataDefinitions(processDefinition.getId(), 0, 5);
        assertEquals(2, dataDefList.size());

        final DataDefinition dataDef1 = dataDefList.get(0);
        assertEquals(intDataName, dataDef1.getName());
        assertEquals(Integer.class.getName(), dataDef1.getClassName());
        // assertEquals(intDefaultExp,dataDef1.getDefaultValueExpression());

        final DataDefinition dataDef2 = dataDefList.get(1);
        assertEquals(strDataName, dataDef2.getName());
        assertEquals(String.class.getName(), dataDef2.getClassName());
        // assertEquals(strDefaultExp,dataDef2.getDefaultValueExpression());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getProcessDataDefinitionsPaginated() throws Exception {
        final String intDataName = "luckyNum";
        final Expression intDefaultExp = new ExpressionBuilder().createConstantIntegerExpression(10);
        final String strDataName = "color";
        final Expression strDefaultExp = new ExpressionBuilder().createConstantStringExpression("blue");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addIntegerData(intDataName, intDefaultExp);
        for (int i = 1; i <= 10; i++) {
            processDefinitionBuilder.addShortTextData(strDataName + i, strDefaultExp);
        }
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        List<DataDefinition> processDataDefinitions = getProcessAPI().getProcessDataDefinitions(processDefinition.getId(), 0, 5);
        assertEquals(5, processDataDefinitions.size());
        assertThat(processDataDefinitions.get(0), nameIs("luckyNum"));
        assertThat(processDataDefinitions.get(1), nameIs("color1"));
        assertThat(processDataDefinitions.get(2), nameIs("color2"));
        assertThat(processDataDefinitions.get(3), nameIs("color3"));
        assertThat(processDataDefinitions.get(4), nameIs("color4"));
        processDataDefinitions = getProcessAPI().getProcessDataDefinitions(processDefinition.getId(), 5, 5);
        assertEquals(5, processDataDefinitions.size());
        assertThat(processDataDefinitions.get(0), nameIs("color5"));
        assertThat(processDataDefinitions.get(1), nameIs("color6"));
        assertThat(processDataDefinitions.get(2), nameIs("color7"));
        assertThat(processDataDefinitions.get(3), nameIs("color8"));
        assertThat(processDataDefinitions.get(4), nameIs("color9"));
        processDataDefinitions = getProcessAPI().getProcessDataDefinitions(processDefinition.getId(), 10, 5);
        assertEquals(1, processDataDefinitions.size());
        assertThat(processDataDefinitions.get(0), nameIs("color10"));
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getNumberOfProcessDataDefinitions() throws Exception {
        final String intDataName = "luckyNum";
        final Expression intDefaultExp = new ExpressionBuilder().createConstantIntegerExpression(10);
        final String strDataName = "luckyColor";
        final Expression strDefaultExp = new ExpressionBuilder().createConstantStringExpression("blue");

        final DesignProcessDefinition designProcessDefinition;
        // process level
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addIntegerData(intDataName, intDefaultExp);
        processDefinitionBuilder.addShortTextData(strDataName, strDefaultExp);

        designProcessDefinition = processDefinitionBuilder.done();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final int i = getProcessAPI().getNumberOfProcessDataDefinitions(processDefinition.getId());
        assertEquals(2, i);

        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = ProcessDefinitionNotFoundException.class)
    public void getProcessDataDefinitionsWithException() throws Exception {
        final String intDataName = "luckyNum";
        final Expression intDefaultExp = new ExpressionBuilder().createConstantIntegerExpression(10);
        final String strDataName = "luckyColor";
        final Expression strDefaultExp = new ExpressionBuilder().createConstantStringExpression("blue");

        final DesignProcessDefinition designProcessDefinition;
        // process level
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addIntegerData(intDataName, intDefaultExp);
        processDefinitionBuilder.addShortTextData(strDataName, strDefaultExp);

        designProcessDefinition = processDefinitionBuilder.done();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        try {
            getProcessAPI().getProcessDataDefinitions(processDefinition.getId() + 1, 0, 5);
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Test
    public void executeProcessWithNotInitializedData() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addAutomaticTask("step1");
        processDefinitionBuilder.addIntegerData("intdata", null);
        processDefinitionBuilder.addShortTextData("stringData", null);
        processDefinitionBuilder.addDateData("dateData", null);
        processDefinitionBuilder.addBlobData("blobData", null);
        processDefinitionBuilder.addBooleanData("booleanData", null);
        processDefinitionBuilder.addLongData("longData", null);
        processDefinitionBuilder.addDoubleData("doubleData", null);
        processDefinitionBuilder.addFloatData("floatData", null);
        processDefinitionBuilder.addXMLData("xmlData", null);
        processDefinitionBuilder.addData("javaData", "java.util.List", null);

        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();

        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);
        final List<DataDefinition> dataDefList = getProcessAPI().getProcessDataDefinitions(processDefinition.getId(), 0, 15);
        assertEquals(10, dataDefList.size());

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(processDefinition);
    }

}
