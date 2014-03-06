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

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.api.ProcessAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.platform.TenantCreator;

/**
 * @author Laurent Leseigneur
 */
@RunWith(BonitaTestRunner.class)
@Initializer(TestsInitializerSP.class)
public class TenantMaintenanceTestSP extends CommonAPISPTest {

	private static final String CRON_EXPRESSION_EACH_SECOND = "*/1 * * * * ?";

	private static final String PROCESS_TENANT = "process_tenant_";

	private static final String START_EVENT = "start event";

	private static final String END_EVENT = "end event";

	private static final String STEP1 = "step1";

	private static PlatformAPI platformAPI;

	private static PlatformLoginAPI platformLoginAPI;

	private static PlatformSession platFormSession;

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

		public TenantParameter(String tenantUniqueName) {
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

		public void setTenantName(String tenantName) {
			this.tenantName = tenantName;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public void setTentantId(long tenantId) {
			this.tenantId = tenantId;

		}

		public long getTenantId() {
			return tenantId;
		}

		public String getTechnicalUsername() {
			return technicalUsername;
		}

		public void setTechnicalUsername(String technicalUsername) {
			this.technicalUsername = technicalUsername;
		}

		public String getTechnicalPassword() {
			return technicalPassword;
		}

		public void setTechnicalPassword(String technicalPassword) {
			this.technicalPassword = technicalPassword;
		}

		public String getActorUsername() {
			return actorUsername;
		}

		public void setActorUsername(String actorUsername) {
			this.actorUsername = actorUsername;
		}

		public String getActorPassword() {
			return actorPassword;
		}

		public void setActorPassword(String actorPassword) {
			this.actorPassword = actorPassword;
		}

		public void setProcessDefinitionId(long processDefinitionId) {
			this.processDefinitionId = processDefinitionId;

		}

	}

	private TenantParameter createTenant(String uniqueName)
			throws BonitaException {

		logAsPlatformAdmin();

		TenantParameter tenantParameter = new TenantParameter(uniqueName);

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

	private void deactivateProcess(TenantParameter tenantParameter)
			throws Exception {

		logAsTechnicalUserOnTenant(tenantParameter);

		long processDefinitionId = tenantParameter.getProcessDefinitionId();
		getProcessAPI().disableProcess(processDefinitionId);

		waitActiveProcessCount(0, tenantParameter);

		getProcessAPI().deleteProcessInstances(processDefinitionId, 0, 100);
		getProcessAPI().deleteArchivedProcessInstances(processDefinitionId, 0,
				100);

		getProcessAPI().deleteProcessDefinition(processDefinitionId);

		getIdentityAPI().deleteUser(tenantParameter.getActorUsername());
	}

	@Test
	public void twoTenantMaintenanceMode() throws Exception {
		// given
		// 1 platform with 2 tenant in the same node
		platFormSession = platformLoginAPI.login("platformAdmin", "platform");
		platformAPI = PlatformAPIAccessor.getPlatformAPI(platFormSession);

		TenantParameter firstTenantParameter = createTenant("firstTenant");
		TenantParameter secondTenantParameter = createTenant("secondTenant");

		createProcess(firstTenantParameter);
		createProcess(secondTenantParameter);

		// given a timer triggered process
		waitArchivedProcessCount(1, secondTenantParameter);
		long numberOfArchivedJobsBefore = getNumberOfArchivedJobs(secondTenantParameter);

		// when a tenant is paused mode
		pauseTenant(firstTenantParameter);

		waitArchivedProcessCount(2, secondTenantParameter);
		long numberOfArchivedJobsAfter = getNumberOfArchivedJobs(secondTenantParameter);

		// then the other tenant is still working
		Assert.assertTrue("second tenant should work",
				numberOfArchivedJobsAfter >= numberOfArchivedJobsBefore);

		// cleanup
		resumeTenant(firstTenantParameter);
		deactivateProcess(firstTenantParameter);
		deactivateProcess(secondTenantParameter);
	}

	@Test
	public void oneTenantMaintenanceMode() throws Exception {

		platFormSession = platformLoginAPI.login("platformAdmin", "platform");
		platformAPI = PlatformAPIAccessor.getPlatformAPI(platFormSession);

		// given we have 1 platform with 1 tenant
		TenantParameter tenantParameter = createTenant("uniqueTenant");

		createProcess(tenantParameter);
		waitArchivedProcessCount(1, tenantParameter);
		long numberOfArchivedJobsBeforeTenantPause = getNumberOfArchivedJobs(tenantParameter);

		// when the tenant is paused and then resume
		pauseTenant(tenantParameter);
		resumeTenant(tenantParameter);

		// then process is resume
		waitArchivedProcessCount(2, tenantParameter);
		long numberOfArchivedJobsAfterTenantPauseAfterResume = getNumberOfArchivedJobs(tenantParameter);
		Assert.assertTrue(numberOfArchivedJobsAfterTenantPauseAfterResume >= numberOfArchivedJobsBeforeTenantPause);

		// cleanup
		deactivateProcess(tenantParameter);

	}

	private void waitArchivedProcessCount(long processCount,
			TenantParameter tenantParameter) throws Exception {
		final long timeout = (processCount + 1) * 1000;
		final long limit = new Date().getTime() + timeout;
		long count = 0;
		while (count < processCount && new Date().getTime() < limit) {
			count = getNumberOfArchivedJobs(tenantParameter);
		}
	}

	private void waitActiveProcessCount(long processCount,
			TenantParameter tenantParameter) throws Exception {
		final long timeout = (processCount + 1) * 1000;
		final long limit = new Date().getTime() + timeout;
		long count = 0;
		while (count < processCount && new Date().getTime() < limit) {
			count = getNumberOfActiveJobs(tenantParameter);
		}
	}

	private long getNumberOfActiveJobs(TenantParameter tenantParameter)
			throws Exception {
		logAsTechnicalUserOnTenant(tenantParameter);

		return getProcessAPI().getNumberOfProcessInstances();
	}

	private long getNumberOfArchivedJobs(TenantParameter tenantParameter)
			throws Exception {
		logAsTechnicalUserOnTenant(tenantParameter);

		return getProcessAPI().getNumberOfArchivedProcessInstances();
	}

	private void createProcess(TenantParameter tenantParameter)
			throws Exception {
		logAsNormalUserOnTenant(tenantParameter);
		setProcessAPI(TenantAPIAccessor.getProcessAPI(getSession()));
		String processName = new StringBuilder().append(PROCESS_TENANT)
				.append(tenantParameter.getTenantId()).toString();

		ProcessAPI processAPI = getProcessAPI();

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

		ActorInstance actorInstance = processAPI.getActors(
				processDefinition.getId(), 0, 10, ActorCriterion.NAME_ASC).get(
				0);

		processAPI.addUserToActor(actorInstance.getId(), getSession()
				.getUserId());

		processAPI.enableProcess(processDefinition.getId());

		tenantParameter.setProcessDefinitionId(processDefinition.getId());

	}

	private void logAsNormalUserOnTenant(TenantParameter tenantParameter)
			throws BonitaException {

		loginWith(tenantParameter.getActorUsername(),
				tenantParameter.getActorPassword(),
				tenantParameter.getTenantId());
	}

	private void pauseTenant(TenantParameter tenantParameter)
			throws BonitaException, UpdateException {
		logAsTechnicalUserOnTenant(tenantParameter);
		getTenantManagementAPI().pause();
	}

	private void resumeTenant(TenantParameter tenantParameter)
			throws BonitaException, UpdateException {
		logAsTechnicalUserOnTenant(tenantParameter);
		getTenantManagementAPI().resume();
	}

}
