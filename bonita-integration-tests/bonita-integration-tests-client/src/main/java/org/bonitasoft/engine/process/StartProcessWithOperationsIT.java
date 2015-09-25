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

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Baptiste Mesta
 */
public class StartProcessWithOperationsIT extends TestWithUser {

    @Test
    public void startProcessWithJavaOperations() throws Exception {
        ProcessDefinitionBuilder processWithOps = new ProcessDefinitionBuilder().createNewInstance("processWithOps", "1.0");
        User john = createUser("john", "bpm");
        processWithOps.addActor("actor");
        processWithOps.addData("data1", ArrayList.class.getName(), new ExpressionBuilder().createGroovyScriptExpression("createList", "new java.util.ArrayList<String>()", ArrayList.class.getName()));
        processWithOps.addUserTask("step1", "actor");
        ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processWithOps.done(), "actor", john);

        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId(),
                Arrays.asList(
                        new OperationBuilder().createJavaMethodOperation("data1", "add", String.class.getName(), new ExpressionBuilder().createConstantStringExpression("listValue"))),
                new HashMap<String, Serializable>());

        waitForUserTask("step1");
        DataInstance data1 = getProcessAPI().getProcessDataInstance("data1", processInstance.getId());
        assertThat(((List) data1.getValue()).get(0)).isEqualTo("listValue");
        disableAndDeleteProcess(processDefinition);
        deleteUser(john);

    }

}
