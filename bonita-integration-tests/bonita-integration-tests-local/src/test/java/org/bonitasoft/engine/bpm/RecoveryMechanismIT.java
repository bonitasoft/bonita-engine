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
package org.bonitasoft.engine.bpm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.tenant.restart.RecoveryService;
import org.bonitasoft.engine.test.CommonAPILocalIT;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RecoveryMechanismIT extends CommonAPILocalIT {

    private RecoveryService recoveryService;

    @Before
    public void before() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        recoveryService = getTenantAccessor().lookup("recoveryService");
        recoveryService.setConsiderElementsOlderThan("PT0S");
    }

    @After
    public void after() {
        recoveryService.setConsiderElementsOlderThan("PT1H");
    }

    @Test
    public void should_recover_elements_after_incident() throws Exception {
        ProcessDefinitionBuilder definitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processRecovered", "1.0");
        definitionBuilder.addAutomaticTask("auto1").addMultiInstance(false,
                new ExpressionBuilder().createConstantIntegerExpression(100));
        ProcessDefinition processDefinition = deployAndEnableProcess(definitionBuilder.done());

        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        TimeUnit.MILLISECONDS.sleep(100);

        getTenantAccessor().getWorkExecutorService().pause();
        getTenantAccessor().getWorkExecutorService().resume();

        TimeUnit.MILLISECONDS.sleep(500);
        assertThat(getProcessAPI().getProcessInstance(processInstance.getId()).getState())
                .isEqualToIgnoringCase(ProcessInstanceState.STARTED.name());

        recoveryService.recoverAllElements();

        waitForProcessToFinish(processInstance);

        recoveryService.recoverAllElements();
    }
}
