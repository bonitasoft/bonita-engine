/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.event;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.test.CommonAPILocalTest;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class LocalTimerEventTest extends CommonAPILocalTest {

    @Before
    public void setUp() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalLogger();
    }

    @After
    public void tearDown() throws BonitaException {
       logoutOnTenant();
    }

    @Test
    public void testTimerJobsAreDeleteOnDisable() throws Exception {
        final ProcessDefinitionBuilder definitionBuilder = new ProcessDefinitionBuilder().createNewInstance("proc", "1.0");
        final String timerEventName = "startTimer";
        definitionBuilder.addStartEvent(timerEventName).addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(2000));
        definitionBuilder.addAutomaticTask("auto");
        definitionBuilder.addTransition(timerEventName, "auto");

        final ProcessDefinition processDefinition = deployAndEnableProcess(definitionBuilder.done());
        assertTrue(containsTimerJob(processDefinition, timerEventName));

        getProcessAPI().disableProcess(processDefinition.getId());
        assertFalse(containsTimerJob(processDefinition, timerEventName));

        getProcessAPI().enableProcess(processDefinition.getId());
        assertTrue(containsTimerJob(processDefinition, timerEventName));

        disableAndDeleteProcess(processDefinition.getId());
        assertFalse(containsTimerJob(processDefinition, timerEventName));

    }

    private boolean containsTimerJob(final ProcessDefinition processDefinition, final String timerEventName) throws Exception {
        setSessionInfo(getSession());
        final SchedulerService schedulerService = getPlatformAccessor().getSchedulerService();
        TransactionService transactionService = getPlatformAccessor().getTransactionService();
        transactionService.begin();
        try {
            final List<String> jobs = schedulerService.getJobs();
            for (final String jobName : jobs) {
                if (jobName.contains("Timer_Ev_" + processDefinition.getId() + timerEventName)) {
                    return true;
                }
            }
        } finally {
            transactionService.complete();
        }
        return false;
    }

}
