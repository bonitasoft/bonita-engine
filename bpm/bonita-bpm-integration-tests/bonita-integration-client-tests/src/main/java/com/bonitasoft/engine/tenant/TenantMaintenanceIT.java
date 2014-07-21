/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.tenant;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.BonitaTestRunner;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonitasoft.engine.BPMTestSPUtil;
import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.TestsInitializerSP;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;

/**
 * @author Laurent Leseigneur
 * @author Celine Souchet
 */
@RunWith(BonitaTestRunner.class)
@Initializer(TestsInitializerSP.class)
public class TenantMaintenanceIT extends CommonAPISPTest {

    private static final String CRON_EXPRESSION_EACH_SECOND = "*/1 * * * * ?";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(TenantMaintenanceIT.class);

    @Before
    public void before() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalLogger();
    }

    private void disableAndDeleteProcess(final long tenantId, final long processDefinitionId) throws Exception {
        loginOnTenantWithTechnicalLogger(tenantId);
        disableAndDeleteProcess(processDefinitionId);
    }

    @Test
    public void twoTenantPauseMode() throws Exception {
        // given
        final long tenantId1 = BPMTestSPUtil.loginOnDefaultTenantWithDefaultTechnicalLogger().getTenantId();
        final User userForTenant1 = BPMTestSPUtil.createUserOnTenantWithDefaultTechnicalLogger(USERNAME, PASSWORD, tenantId1);
        final ProcessDefinition processDefinitionForTenant1 = createProcessOnTenant(tenantId1);

        final long tenantId2 = createAndActivateTenant("TenantPauseTestSP3");
        final User userForTenant2 = BPMTestSPUtil.createUserOnTenantWithDefaultTechnicalLogger(USERNAME, PASSWORD, tenantId2);
        final ProcessDefinition processDefinitionForTenant2 = createProcessOnTenant(tenantId2);

        // given a timer triggered process
        waitArchivedProcessCount(1, tenantId2);
        final long numberOfArchivedJobsBefore = getNumberOfArchivedJobs(tenantId2);

        // when a tenant is paused mode
        pauseTenant(tenantId1);
        waitForPauseTime();
        waitArchivedProcessCount(2, tenantId2);
        final long numberOfArchivedJobsAfter = getNumberOfArchivedJobs(tenantId2);

        // then the other tenant is still working
        Assert.assertTrue("second tenant should work",
                numberOfArchivedJobsAfter >= numberOfArchivedJobsBefore);

        // cleanup
        resumeTenant(tenantId1);
        disableAndDeleteProcess(tenantId1, processDefinitionForTenant1.getId());
        deleteUser(userForTenant1);
        disableAndDeleteProcess(tenantId2, processDefinitionForTenant2.getId());
        deleteUser(userForTenant2);
        logNumberOfProcess(tenantId2, "TenantPauseTestSP3");
        BPMTestSPUtil.deactivateAndDeleteTenant(tenantId2);
    }

    @Test
    public void oneTenantPauseMode() throws Exception {
        final long tenantId = BPMTestSPUtil.loginOnDefaultTenantWithDefaultTechnicalLogger().getTenantId();
        final User user = BPMTestSPUtil.createUserOnTenantWithDefaultTechnicalLogger(USERNAME, PASSWORD, tenantId);
        final ProcessDefinition processDefinition = createProcessOnTenant(tenantId);
        waitArchivedProcessCount(1, tenantId);
        final long numberOfArchivedJobsBeforeTenantPause = getNumberOfArchivedJobs(tenantId);

        // when the tenant is paused and then resume
        pauseTenant(tenantId);
        waitForPauseTime();
        resumeTenant(tenantId);

        // then process is resume
        waitArchivedProcessCount(2, tenantId);
        final long numberOfArchivedJobsAfterTenantPauseAfterResume = getNumberOfArchivedJobs(tenantId);
        Assert.assertTrue(numberOfArchivedJobsAfterTenantPauseAfterResume >= numberOfArchivedJobsBeforeTenantPause);

        // cleanup
        disableAndDeleteProcess(tenantId, processDefinition.getId());
        deleteUser(user);
        logNumberOfProcess(tenantId, "defaultTenant");
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Operation", "EngineExecutionContext" }, jira = "BS-8687")
    @Test
    public void can_executeConnectorOnActivityInstance_after_resume_tenant() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);
        final LeftOperand leftOperand = new LeftOperandBuilder().createNewInstance().setName("externalData").setType(LeftOperand.TYPE_EXTERNAL_DATA).done();
        final Operation operation = new OperationBuilder().createNewInstance().setLeftOperand(leftOperand)
                .setRightOperand(new ExpressionBuilder().createInputExpression(ExpressionConstants.PROCESS_DEFINITION_ID.getEngineConstantName(),
                        Long.class.getName()))
                .setType(OperatorType.ASSIGNMENT).done();

        final ProcessDefinitionBuilderExt processDefinitionBuilderExt = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilderExt.addActor(ACTOR_NAME);
        processDefinitionBuilderExt.addUserTask("step1", ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorEngineExecutionContext(processDefinitionBuilderExt,
                ACTOR_NAME, user);

        // when: put in maintenance the tenant
        getTenantManagementAPI().pause();

        // when: we stop and start the node
        stopAndStartPlatform();
        loginOnDefaultTenantWithDefaultTechnicalLogger();

        // when: resume the tenant
        getTenantManagementAPI().resume();

        // Start a process instance. The connector should work.
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance);

        final Map<String, Serializable> res = getProcessAPI().executeConnectorOnActivityInstance(
                "org.bonitasoft.connector.testConnectorEngineExecutionContext", "1.0", Collections.<String, Expression> emptyMap(),
                Collections.<String, Map<String, Serializable>> emptyMap(), Collections.singletonList(operation),
                Collections.<String, Serializable> emptyMap(), step1.getId());

        assertEquals(processDefinition.getId(), res.get("externalData"));

        // Clean up
        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    private void waitForPauseTime() throws InterruptedException {
        LOGGER.info("start pause time");
        Thread.sleep(3000);
        LOGGER.info("end pause time");
    }

    private void waitArchivedProcessCount(final long processCount, final long tenantId) throws Exception {
        final long timeout = (processCount + 1) * 1000;
        final long limit = new Date().getTime() + timeout;
        long count = 0;
        while (count < processCount && new Date().getTime() < limit) {
            count = getNumberOfArchivedJobs(tenantId);
        }
    }

    private void logNumberOfProcess(final long tenantId, final String tenantName) throws Exception {
        loginOnTenantWithTechnicalLogger(tenantId);
        long numberOfProcessInstances = getProcessAPI()
                .getNumberOfProcessInstances();
        long numberOfArchivedProcessInstances = getProcessAPI()
                .getNumberOfArchivedProcessInstances();

        LOGGER.info(String.format(
                "tenant: %d %s process instance:%d archived process:%d", tenantId, tenantName,
                numberOfProcessInstances, numberOfArchivedProcessInstances));
    }

    private long getNumberOfArchivedJobs(final long tenantId) throws Exception {
        loginOnTenantWithTechnicalLogger(tenantId);
        return getProcessAPI().getNumberOfArchivedProcessInstances();
    }

    private ProcessDefinition createProcessOnTenant(final long tenantId) throws Exception {
        loginOnTenantWith(USERNAME, PASSWORD, tenantId);
        String processName = new StringBuilder().append(PROCESS_NAME).append(tenantId).toString();
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

    private void pauseTenant(final long tenantId) throws BonitaException, UpdateException {
        loginOnTenantWithTechnicalLogger(tenantId);
        getTenantManagementAPI().pause();
    }

    private void resumeTenant(final long tenantId) throws BonitaException, UpdateException {
        loginOnTenantWithTechnicalLogger(tenantId);
        getTenantManagementAPI().resume();
    }
}
