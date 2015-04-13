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
 */

package org.bonitasoft.engine.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.junit.Test;

public class ExecutionContextIT extends TestWithUser {

    @Test
    public void evaluate_context_on_process_and_task() throws Exception {
        ProcessDefinitionBuilder p1Builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithContext", "1.0");
        p1Builder.addShortTextData("processData", new ExpressionBuilder().createConstantStringExpression("processDataValue"));
        p1Builder.addContextEntry("process_key1", new ExpressionBuilder().createDataExpression("processData", String.class.getName()));
        UserTaskDefinitionBuilder task1 = p1Builder.addUserTask("step1", "actor");
        task1.addShortTextData("task1Data", new ExpressionBuilder().createConstantStringExpression("task1DataValue"));
        task1.addContextEntry("task_key1", new ExpressionBuilder().createDataExpression("task1Data", String.class.getName()));
        task1.addContextEntry("task_key2", new ExpressionBuilder().createConstantStringExpression("constantValue"));
        UserTaskDefinitionBuilder task2 = p1Builder.addUserTask("step2", "actor");
        task2.addShortTextData("task2Data", new ExpressionBuilder().createConstantStringExpression("task2DataValue"));
        p1Builder.addActor("actor");
        ProcessDefinition processDefinition = deployAndEnableProcessWithActor(p1Builder.done(), "actor", user);
        ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        long step1 = waitForUserTask(processInstance1.getId(), "step1");
        long step2 = waitForUserTask(processInstance1.getId(), "step2");

        assertThat(getProcessAPI().getProcessInstanceExecutionContext(processInstance1.getId())).containsOnly(entry("process_key1", "processDataValue"));
        assertThat(getProcessAPI().getUserTaskExecutionContext(step1)).containsOnly(entry("task_key1", "task1DataValue"), entry("task_key2", "constantValue"));
        assertThat(getProcessAPI().getUserTaskExecutionContext(step2)).isEmpty();


        assignAndExecuteStep(step1, user.getId());
        assignAndExecuteStep(step2, user.getId());
        waitForProcessToFinish(processInstance1);
        Thread.sleep(10);
        ArchivedProcessInstance finalArchivedProcessInstance = getProcessAPI().getFinalArchivedProcessInstance(processInstance1.getId());
        ArchivedActivityInstance archivedStep1 = getProcessAPI().getArchivedActivityInstance(step1);
        ArchivedActivityInstance archivedStep2 = getProcessAPI().getArchivedActivityInstance(step2);
        assertThat(getProcessAPI().getArchivedProcessInstanceExecutionContext(finalArchivedProcessInstance.getId())).containsOnly(entry("process_key1", "processDataValue"));
        assertThat(getProcessAPI().getArchivedUserTaskExecutionContext(archivedStep1.getId())).containsOnly(entry("task_key1", "task1DataValue"), entry("task_key2", "constantValue"));
        assertThat(getProcessAPI().getArchivedUserTaskExecutionContext(archivedStep2.getId())).isEmpty();


        disableAndDeleteProcess(processDefinition);
    }

}
