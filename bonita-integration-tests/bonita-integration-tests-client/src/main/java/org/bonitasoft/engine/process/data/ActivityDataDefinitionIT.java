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
import org.bonitasoft.engine.bpm.flownode.ActivityDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.AutomaticTaskDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.junit.Test;

public class ActivityDataDefinitionIT extends TestWithUser {

    @Test
    public void getDataDefinitionsHavingComplexeInitialValue() throws Exception {
        final Expression scriptExpression = new ExpressionBuilder().createGroovyScriptExpression("a+b", "a+b", String.class.getName(),
                new ExpressionBuilder().createDataExpression("a", String.class.getName()),
                new ExpressionBuilder().createDataExpression("b", String.class.getName()));

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addShortTextData("a", new ExpressionBuilder().createConstantStringExpression("avalue"));
        processDefinitionBuilder.addShortTextData("b", new ExpressionBuilder().createConstantStringExpression("bvalue"));
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addData("myData", String.class.getName(), scriptExpression);
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final List<DataDefinition> dataDefList = getProcessAPI().getActivityDataDefinitions(processDefinition.getId(), "step1", 0, 10);
        assertEquals(1, dataDefList.size());
        assertEquals(2, dataDefList.get(0).getDefaultValueExpression().getDependencies().size());
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getNumberOfActivityDataDefinitions() throws Exception {
        final String intDataName = "luckyNum";
        final Expression intDefaultExp = new ExpressionBuilder().createConstantIntegerExpression(10);
        final String strDataName = "luckyColor";
        final Expression strDefaultExp = new ExpressionBuilder().createConstantStringExpression("blue");
        final String taskName = "autoTask1";

        // process level
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        // activity level
        final AutomaticTaskDefinitionBuilder activityDefinitionBuilder = processDefinitionBuilder.addAutomaticTask(taskName);
        activityDefinitionBuilder.addIntegerData(intDataName, intDefaultExp);
        activityDefinitionBuilder.addShortTextData(strDataName, strDefaultExp);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);

        final int number = getProcessAPI().getNumberOfActivityDataDefinitions(processDefinition.getId(), taskName);
        assertEquals(2, number);

        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = ActivityDefinitionNotFoundException.class)
    public void getActivityDataDefinitionsWithException() throws Exception {
        final String intDataName = "luckyNum";
        final Expression intDefaultExp = new ExpressionBuilder().createConstantIntegerExpression(10);
        final String strDataName = "luckyColor";
        final Expression strDefaultExp = new ExpressionBuilder().createConstantStringExpression("blue");
        final String taskName = "autoTask1";

        // process level
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        // activity level
        final AutomaticTaskDefinitionBuilder activityDefinitionBuilder = processDefinitionBuilder.addAutomaticTask(taskName);
        activityDefinitionBuilder.addIntegerData(intDataName, intDefaultExp);
        activityDefinitionBuilder.addShortTextData(strDataName, strDefaultExp);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);
        try {
            getProcessAPI().getActivityDataDefinitions(processDefinition.getId(), taskName + "qwer", 0, 5);
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Test
    public void getActivityDataDefinitions() throws Exception {
        final String intDataName = "luckyNum";
        final Expression intDefaultExp = new ExpressionBuilder().createConstantIntegerExpression(10);
        final String strDataName = "luckyColor";
        final Expression strDefaultExp = new ExpressionBuilder().createConstantStringExpression("blue");
        final String taskName = "autoTask1";

        // process level
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        // activity level
        final AutomaticTaskDefinitionBuilder activityDefinitionBuilder = processDefinitionBuilder.addAutomaticTask(taskName);
        activityDefinitionBuilder.addIntegerData(intDataName, intDefaultExp);
        activityDefinitionBuilder.addShortTextData(strDataName, strDefaultExp);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);

        final List<DataDefinition> dataDefList = getProcessAPI().getActivityDataDefinitions(processDefinition.getId(), taskName, 0, 5);
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
    public void getActivityDataDefinitionsPaginated() throws Exception {
        final String intDataName = "luckyNum";
        final Expression intDefaultExp = new ExpressionBuilder().createConstantIntegerExpression(10);
        final String strDataName = "color";
        final Expression strDefaultExp = new ExpressionBuilder().createConstantStringExpression("blue");
        final String taskName = "autoTask1";

        // process level
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        // activity level
        final AutomaticTaskDefinitionBuilder activityDefinitionBuilder = processDefinitionBuilder.addAutomaticTask(taskName);
        activityDefinitionBuilder.addIntegerData(intDataName, intDefaultExp);
        for (int i = 1; i <= 10; i++) {
            activityDefinitionBuilder.addShortTextData(strDataName + i, strDefaultExp);
        }

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);
        List<DataDefinition> processDataDefinitions = getProcessAPI().getActivityDataDefinitions(processDefinition.getId(), taskName, 0, 5);
        assertEquals(5, processDataDefinitions.size());
        assertThat(processDataDefinitions.get(0), nameIs("luckyNum"));
        assertThat(processDataDefinitions.get(1), nameIs("color1"));
        assertThat(processDataDefinitions.get(2), nameIs("color2"));
        assertThat(processDataDefinitions.get(3), nameIs("color3"));
        assertThat(processDataDefinitions.get(4), nameIs("color4"));
        processDataDefinitions = getProcessAPI().getActivityDataDefinitions(processDefinition.getId(), taskName, 5, 5);
        assertEquals(5, processDataDefinitions.size());
        assertThat(processDataDefinitions.get(0), nameIs("color5"));
        assertThat(processDataDefinitions.get(1), nameIs("color6"));
        assertThat(processDataDefinitions.get(2), nameIs("color7"));
        assertThat(processDataDefinitions.get(3), nameIs("color8"));
        assertThat(processDataDefinitions.get(4), nameIs("color9"));
        processDataDefinitions = getProcessAPI().getActivityDataDefinitions(processDefinition.getId(), taskName, 10, 5);
        assertEquals(1, processDataDefinitions.size());
        assertThat(processDataDefinitions.get(0), nameIs("color10"));

        disableAndDeleteProcess(processDefinition);
    }

}
