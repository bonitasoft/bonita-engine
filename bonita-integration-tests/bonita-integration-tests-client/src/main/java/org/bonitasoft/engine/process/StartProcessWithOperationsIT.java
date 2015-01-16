/*
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.bonitasoft.engine.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.BPMRemoteTests;
import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProcessRuntimeAPI;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.AutomaticTaskInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ComparisonOperator;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.expression.XPathReturnType;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

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
