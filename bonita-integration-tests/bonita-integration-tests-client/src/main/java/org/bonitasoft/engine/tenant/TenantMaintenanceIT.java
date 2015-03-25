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
package org.bonitasoft.engine.tenant;

import java.util.Date;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Laurent Leseigneur
 * @author Celine Souchet
 */
public class TenantMaintenanceIT extends TestWithUser {

    private static final String CRON_EXPRESSION_EACH_SECOND = "*/1 * * * * ?";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(TenantMaintenanceIT.class);

    @Test
    public void oneTenantPauseMode() throws Exception {
        final ProcessDefinition processDefinition = createProcessOnTenant();
        waitArchivedProcessCount(1);
        final long numberOfArchivedJobsBeforeTenantPause = getNumberOfArchivedJobs();

        // when the tenant is paused and then resume
        logoutThenlogin();
        pauseTenant();
        waitForPauseTime();
        resumeTenant();

        // then process is resume
        logoutThenloginAs(USERNAME, PASSWORD); // normal user
        waitArchivedProcessCount(2);
        final long numberOfArchivedJobsAfterTenantPauseAfterResume = getNumberOfArchivedJobs();
        Assert.assertTrue(numberOfArchivedJobsAfterTenantPauseAfterResume >= numberOfArchivedJobsBeforeTenantPause);

        // cleanup
        disableAndDeleteProcess(processDefinition.getId());
        logNumberOfProcess();
    }

    private void waitForPauseTime() throws InterruptedException {
        LOGGER.info("start pause time");
        Thread.sleep(3000);
        LOGGER.info("end pause time");
    }

    private void waitArchivedProcessCount(final long processCount) throws Exception {
        final long timeout = (processCount + 1) * 1000;
        final long limit = new Date().getTime() + timeout;
        long count = 0;
        while (count < processCount && new Date().getTime() < limit) {
            count = getNumberOfArchivedJobs();
        }
    }

    private void logNumberOfProcess() throws Exception {
        final long numberOfProcessInstances = getProcessAPI()
                .getNumberOfProcessInstances();
        final long numberOfArchivedProcessInstances = getProcessAPI()
                .getNumberOfArchivedProcessInstances();

        LOGGER.info(String.format(
                "tenant: process instance:%d archived process:%d",
                numberOfProcessInstances, numberOfArchivedProcessInstances));
    }

    private long getNumberOfArchivedJobs() throws Exception {
        return getProcessAPI().getNumberOfArchivedProcessInstances();
    }

    private ProcessDefinition createProcessOnTenant() throws Exception {
        final String processName = new StringBuilder().append(PROCESS_NAME).toString();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance(processName,
                        PROCESS_VERSION)
                .addActor(ACTOR_NAME)
                .addStartEvent("start event")
                .addTimerEventTriggerDefinition(
                        TimerType.CYCLE,
                        new ExpressionBuilder()
                                .createConstantStringExpression(CRON_EXPRESSION_EACH_SECOND))
                .addAutomaticTask("step1")
                .addEndEvent("end event").getProcess();

        return deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, getSession().getUserId());
    }

    private void pauseTenant() throws BonitaException {
        getTenantAdministrationAPI().pause();
    }

    private void resumeTenant() throws BonitaException {
        getTenantAdministrationAPI().resume();
    }
}
