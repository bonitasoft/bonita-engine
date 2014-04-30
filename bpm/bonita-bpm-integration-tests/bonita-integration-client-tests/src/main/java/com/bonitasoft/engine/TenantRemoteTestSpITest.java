/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import java.util.Date;

import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.BonitaTestRunner;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.platform.PlatformLoginException;
import org.bonitasoft.engine.session.PlatformSession;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.api.ProcessAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.platform.TenantCreator;
import com.bonitasoft.engine.platform.TenantDeactivationException;
import com.bonitasoft.engine.platform.TenantNotFoundException;

/**
 * @author Laurent Leseigneur
 */
@RunWith(BonitaTestRunner.class)
@Initializer(TestsInitializerSP.class)
public class TenantRemoteTestSpITest extends CommonAPISPTest {

    private static final String CRON_EXPRESSION_EACH_SECOND = "*/1 * * * * ?";

    private static final String PROCESS_TENANT = "process_tenant_";

    private static final String START_EVENT = "start event";

    private static final String END_EVENT = "end event";

    private static final String STEP1 = "step1";

    private static PlatformAPI platformAPI;

    private static PlatformLoginAPI platformLoginAPI;

    private static PlatformSession platFormSession;

    private static final Logger LOGGER = LoggerFactory
            .getLogger(TenantRemoteTestSpITest.class);

    @BeforeClass
    public static void beforeClass() throws BonitaException {
        platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
    }

    @AfterClass
    public static void afterClass() throws BonitaException {
        platformLoginAPI.logout(platFormSession);
    }

    public static class TenantParameter {

        private String tenantName;

        private String technicalUsername;

        private String technicalPassword;

        private String description;

        private long tenantId;

        private String actorUsername;

        private String actorPassword;

        private long processDefinitionId;

        public long getProcessDefinitionId() {
            return processDefinitionId;
        }

        public TenantParameter(final String tenantUniqueName) {
            this.setTenantName(tenantUniqueName);

            this.setTechnicalUsername("install");
            this.setTechnicalPassword("install");
            this.setDescription("description " + tenantName);
            this.setActorUsername("john");
            this.setActorPassword("password");
        }

        public String getTenantName() {
            return tenantName;
        }

        public void setTenantName(final String tenantName) {
            this.tenantName = tenantName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        public void setTentantId(final long tenantId) {
            this.tenantId = tenantId;
        }

        public long getTenantId() {
            return tenantId;
        }

        public String getTechnicalUsername() {
            return technicalUsername;
        }

        public void setTechnicalUsername(final String technicalUsername) {
            this.technicalUsername = technicalUsername;
        }

        public String getTechnicalPassword() {
            return technicalPassword;
        }

        public void setTechnicalPassword(final String technicalPassword) {
            this.technicalPassword = technicalPassword;
        }

        public String getActorUsername() {
            return actorUsername;
        }

        public void setActorUsername(final String actorUsername) {
            this.actorUsername = actorUsername;
        }

        public String getActorPassword() {
            return actorPassword;
        }

        public void setActorPassword(final String actorPassword) {
            this.actorPassword = actorPassword;
        }

        public void setProcessDefinitionId(final long processDefinitionId) {
            this.processDefinitionId = processDefinitionId;
        }
    }

    private TenantParameter createTenant(final String uniqueName)
            throws BonitaException {

        logAsPlatformAdmin();

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(uniqueName);
        stringBuilder.append("_");
        stringBuilder.append(new Date().getTime());
        final String tenantUniqueName = stringBuilder.toString();
        final TenantParameter tenantParameter = new TenantParameter(tenantUniqueName);

        long tenantId = platformAPI.createTenant(new TenantCreator(
                tenantParameter.getTenantName(), tenantParameter
                        .getDescription(), "testIconName", "testIconPath",
                tenantParameter.getTechnicalUsername(), tenantParameter
                        .getTechnicalPassword()));
        platformAPI.activateTenant(tenantId);
        tenantId = platformAPI.getTenantByName(tenantParameter.getTenantName())
                .getId();
        tenantParameter.setTentantId(tenantId);
        logAsTechnicalUserOnTenant(tenantParameter);
        getIdentityAPI().createUser("john", "password");

        return tenantParameter;
    }

    private void logAsTechnicalUserOnTenant(
            final TenantParameter tenantParameter) throws BonitaException {
        loginWith(tenantParameter.getTechnicalUsername(),
                tenantParameter.getTechnicalPassword(),
                tenantParameter.getTenantId());
    }

    private void logAsPlatformAdmin() throws PlatformLoginException,
            BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        platFormSession = platformLoginAPI.login("platformAdmin", "platform");
        platformAPI = PlatformAPIAccessor.getPlatformAPI(platFormSession);
    }

    private void deactivateProcess(final TenantParameter tenantParameter)
            throws Exception {
        logAsTechnicalUserOnTenant(tenantParameter);
        disableAndDeleteProcess(tenantParameter.getProcessDefinitionId());
        return;
    }

    @Test
    public void twoTenantMaintenanceMode() throws Exception {
        // given
        // 1 platform with 2 tenant in the same node
        platFormSession = platformLoginAPI.login("platformAdmin", "platform");
        platformAPI = PlatformAPIAccessor.getPlatformAPI(platFormSession);

        final TenantParameter tenant1 = createTenant("TenantMaintenanceTestSP2");
        final TenantParameter tenant2 = createTenant("TenantMaintenanceTestSP3");

        createProcessOnTenant(tenant1);
        createProcessOnTenant(tenant2);

        // given a timer triggered process
        waitArchivedProcessCount(1, tenant2);
        final long numberOfArchivedJobsBefore = getNumberOfArchivedJobs(tenant2);

        // when a tenant is paused mode
        pauseTenant(tenant1);

        waitForMaintenanceTime();

        waitArchivedProcessCount(2, tenant2);
        final long numberOfArchivedJobsAfter = getNumberOfArchivedJobs(tenant2);

        // then the other tenant is still working
        Assert.assertTrue("second tenant should work",
                numberOfArchivedJobsAfter >= numberOfArchivedJobsBefore);

        // cleanup
        resumeTenant(tenant1);

        deactivateProcess(tenant1);
        logNumberOfProcess(tenant1);
        deactivateAndDeleteTenant(tenant1);

        deactivateProcess(tenant2);
        logNumberOfProcess(tenant2);
        deactivateAndDeleteTenant(tenant2);

    }

    @Test
    public void oneTenantMaintenanceMode() throws Exception {
        platFormSession = platformLoginAPI.login("platformAdmin", "platform");
        platformAPI = PlatformAPIAccessor.getPlatformAPI(platFormSession);

        // given we have 1 platform with 1 tenant
        final TenantParameter tenantParameter = createTenant("tenant1");

        createProcessOnTenant(tenantParameter);
        waitArchivedProcessCount(1, tenantParameter);
        final long numberOfArchivedJobsBeforeTenantPause = getNumberOfArchivedJobs(tenantParameter);

        // when the tenant is paused and then resume
        pauseTenant(tenantParameter);

        waitForMaintenanceTime();

        resumeTenant(tenantParameter);

        // then process is resume
        waitArchivedProcessCount(2, tenantParameter);
        final long numberOfArchivedJobsAfterTenantPauseAfterResume = getNumberOfArchivedJobs(tenantParameter);
        Assert.assertTrue(numberOfArchivedJobsAfterTenantPauseAfterResume >= numberOfArchivedJobsBeforeTenantPause);

        // cleanup
        deactivateProcess(tenantParameter);

        logNumberOfProcess(tenantParameter);
        deactivateAndDeleteTenant(tenantParameter);

    }

    private void waitForMaintenanceTime() throws InterruptedException {
        LOGGER.info("start maintenance time");
        Thread.sleep(3000);
        LOGGER.info("end maintenance time");
    }

    private void deactivateAndDeleteTenant(final TenantParameter tenantParameter)
            throws PlatformLoginException, BonitaHomeNotSetException,
            ServerAPIException, UnknownAPITypeException,
            TenantNotFoundException, TenantDeactivationException,
            DeletionException {
        logAsPlatformAdmin();
        platformAPI.deactiveTenant(tenantParameter.getTenantId());
        platformAPI.deleteTenant(tenantParameter.getTenantId());
    }

    private void waitArchivedProcessCount(final long processCount,
            final TenantParameter tenantParameter) throws Exception {
        final long timeout = (processCount + 1) * 1000;
        final long limit = new Date().getTime() + timeout;
        long count = 0;
        while (count < processCount && new Date().getTime() < limit) {
            count = getNumberOfArchivedJobs(tenantParameter);
        }
    }

    private void logNumberOfProcess(final TenantParameter tenantParameter)
            throws Exception {
        logAsTechnicalUserOnTenant(tenantParameter);

        final long numberOfProcessInstances = getProcessAPI()
                .getNumberOfProcessInstances();
        final long numberOfArchivedProcessInstances = getProcessAPI()
                .getNumberOfArchivedProcessInstances();

        LOGGER.info(String.format(
                "tenant: %d %s process instance:%d archived process:%d",
                tenantParameter.getTenantId(), tenantParameter.getTenantName(),
                numberOfProcessInstances, numberOfArchivedProcessInstances));
    }

    private long getNumberOfArchivedJobs(final TenantParameter tenantParameter)
            throws Exception {
        logAsTechnicalUserOnTenant(tenantParameter);
        return getProcessAPI().getNumberOfArchivedProcessInstances();
    }

    private void createProcessOnTenant(final TenantParameter tenantParameter)
            throws Exception {
        logAsNormalUserOnTenant(tenantParameter);
        setProcessAPI(TenantAPIAccessor.getProcessAPI(getSession()));
        final String processName = new StringBuilder().append(PROCESS_TENANT)
                .append(tenantParameter.getTenantId()).toString();

        final ProcessAPI processAPI = getProcessAPI();

        final ProcessDefinition processDefinition = processAPI
                .deploy(new BusinessArchiveBuilder()
                        .createNewBusinessArchive()
                        .setProcessDefinition(
                                new ProcessDefinitionBuilder()
                                        .createNewInstance(processName,
                                                PROCESS_VERSION)
                                        .addActor(ACTOR_NAME)
                                        .addStartEvent(START_EVENT)
                                        .addTimerEventTriggerDefinition(
                                                TimerType.CYCLE,
                                                new ExpressionBuilder()
                                                        .createConstantStringExpression(CRON_EXPRESSION_EACH_SECOND))
                                        .addAutomaticTask(STEP1)
                                        .addEndEvent(END_EVENT).getProcess())
                        .done());

        final ActorInstance actorInstance = processAPI.getActors(
                processDefinition.getId(), 0, 10, ActorCriterion.NAME_ASC).get(
                0);

        processAPI.addUserToActor(actorInstance.getId(), getSession()
                .getUserId());

        processAPI.enableProcess(processDefinition.getId());

        tenantParameter.setProcessDefinitionId(processDefinition.getId());
    }

    private void logAsNormalUserOnTenant(final TenantParameter tenantParameter)
            throws BonitaException {
        loginWith(tenantParameter.getActorUsername(),
                tenantParameter.getActorPassword(),
                tenantParameter.getTenantId());
    }

    private void pauseTenant(final TenantParameter tenantParameter)
            throws BonitaException, UpdateException {
        logAsTechnicalUserOnTenant(tenantParameter);
        getTenantManagementAPI().pause();
    }

    private void resumeTenant(final TenantParameter tenantParameter)
            throws BonitaException, UpdateException {
        logAsTechnicalUserOnTenant(tenantParameter);
        getTenantManagementAPI().resume();
    }
}
