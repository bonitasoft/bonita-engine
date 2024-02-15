/**
 * Copyright (C) 2020 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.activity.work;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta.
 */
public class RetryWorkIT extends CommonAPIIT {

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    @Test
    public void should_retry_exception_in_groovy_scripts() throws Exception {
        String script = "public class AScriptThatFailsOneTime{\n" +
                "   static alreadyExecutedOnce = false\n" +
                "\n" +
                "   String exec(){\n" +
                "       if(alreadyExecutedOnce){\n" +
                "           return \"ok\"\n" +
                "       }\n" +
                "      alreadyExecutedOnce = true\n" +
                "       throw new org.bonitasoft.engine.commons.exceptions.SRetryableException('something')\n" +
                "   }\n" +
                "}\n" +
                "new AScriptThatFailsOneTime().exec()";
        DesignProcessDefinition process = new ProcessDefinitionBuilder().createNewInstance("processToRetry", "1.0")
                .addAutomaticTask("theTask").addDisplayName(new ExpressionBuilder()
                        .createGroovyScriptExpression("failonetime", script, String.class.getName()))
                .getProcess();
        ProcessDefinition processDefinition = getProcessAPI().deployAndEnableProcess(process);

        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        //the task should fail one but be retried
        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(processDefinition);
    }

}
