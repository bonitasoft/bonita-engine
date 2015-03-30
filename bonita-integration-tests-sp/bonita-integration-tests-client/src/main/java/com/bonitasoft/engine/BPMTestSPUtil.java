/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import static org.junit.Assert.assertNull;

import java.util.List;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.ActivityDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.TransitionDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.ClientEventUtil;

import com.bonitasoft.engine.api.LoginAPI;
import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;
import com.bonitasoft.engine.platform.Tenant;
import com.bonitasoft.engine.platform.TenantCreator;
import com.bonitasoft.engine.platform.TenantDeactivationException;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class BPMTestSPUtil {

    private static final String DEFAULT_TECHNICAL_USER_USERNAME = "install";

    private static final String DEFAULT_TECHNICAL_USER_PASSWORD = "install";

    private static final String DEFAULT_TENANT_DESCRIPTION = "the default tenant";

    public static final String DEFAULT_TENANT = "default";

    public static final String ACTOR_NAME = "Actor1";

    public static final String PROCESS_VERSION = "1.0";

    public static final String PROCESS_NAME = "ProcessName";

    private static long defaultTenantId;

    public static void createEnvironmentWithDefaultTenant() throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.initializePlatform();
        platformAPI.startNode();
        defaultTenantId = platformAPI.getDefaultTenant().getId();
        logoutOnPlatform(session);
        final APISession loginDefaultTenant = loginOnDefaultTenantWithDefaultTechnicalUser();
        ClientEventUtil.deployCommand(loginDefaultTenant);
        logoutOnTenant(loginDefaultTenant);
    }

    public static void refreshDefaultTenantId() throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        setDefaultTenantId(platformAPI.getDefaultTenant().getId());
        logoutOnPlatform(session);
    }

    public static void setDefaultTenantId(final long defaultTenantId) {
        BPMTestSPUtil.defaultTenantId = defaultTenantId;
    }

    public static void destroyPlatformAndTenants() throws BonitaException {
        final APISession loginDefaultTenant = loginOnDefaultTenantWithDefaultTechnicalUser();
        ClientEventUtil.undeployCommand(loginDefaultTenant);
        logoutOnTenant(loginDefaultTenant);
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        List<Tenant> tenants = null;
        try {
            tenants = platformAPI.searchTenants(new SearchOptionsBuilder(0, 1000).done()).getResult();
        } catch (final SearchException e) {
            platformAPI.startNode();
            tenants = platformAPI.searchTenants(new SearchOptionsBuilder(0, 1000).done()).getResult();
        }
        for (final Tenant tenant : tenants) {
            try {
                if (!tenant.isDefaultTenant()) {
                    platformAPI.deactiveTenant(tenant.getId());
                }
            } catch (final TenantDeactivationException tde) {
                // Nothing to do
            }
            if (!tenant.isDefaultTenant()) {
                platformAPI.deleteTenant(tenant.getId());
            }
        }
        platformAPI.stopNode();
        platformAPI.cleanPlatform();
        logoutOnPlatform(session);
    }

    public static void createEnvironmentWithoutTenant() throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.createAndInitializePlatform();
        platformAPI.startNode();
        logoutOnPlatform(session);
    }

    public static void destroyEnvironmentWithoutTenant() throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.stopNode();
        platformAPI.deletePlatform();
        logoutOnPlatform(session);
    }

    public static long createAndActivateTenant(final String tenantName, final String iconName, final String iconPath, final String techinalUsername,
            final String password) throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        final long tenantId = platformAPI
                .createTenant(new TenantCreator(tenantName, DEFAULT_TENANT_DESCRIPTION, iconName, iconPath, techinalUsername, password));
        platformAPI.activateTenant(tenantId);
        logoutOnPlatform(session);
        return tenantId;
    }

    public static long createAndActivateTenantWithDefaultTechnicalLogger(final String tenantName) throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        final TenantCreator tenantCreator = new TenantCreator(tenantName);
        tenantCreator.setUsername(DEFAULT_TECHNICAL_USER_USERNAME);
        tenantCreator.setPassword(DEFAULT_TECHNICAL_USER_PASSWORD);
        final long tenantId = platformAPI.createTenant(tenantCreator);
        platformAPI.activateTenant(tenantId);
        logoutOnPlatform(session);
        final APISession apiSession = loginOnTenantWithDefaultTechnicalLogger(tenantId);
        ClientEventUtil.deployCommand(apiSession);
        logoutOnTenant(apiSession);
        return tenantId;
    }

    public static void logoutOnTenant(final APISession session) throws BonitaException {
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        loginAPI.logout(session);
    }

    public static PlatformSession loginOnPlatform() throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        return platformLoginAPI.login("platformAdmin", "platform");
    }

    public static void logoutOnPlatform(final PlatformSession session) throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        platformLoginAPI.logout(session);
    }

    public static DesignProcessDefinition createProcessDefinitionWithHumanAndAutomaticSteps(final String processName, final String processVersion,
            final List<String> stepNames, final List<Boolean> isHuman, final String actorName, final boolean addActorInitiator, final boolean parallelActivities)
            throws InvalidProcessDefinitionException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance(processName, processVersion);
        if (!isHuman.isEmpty() && isHuman.contains(true)) {
            processBuilder.addActor(actorName);
            if (addActorInitiator) {
                processBuilder.setActorInitiator(actorName);
            }
        }
        ActivityDefinitionBuilder activityDefinitionBuilder = null;
        for (int i = 0; i < stepNames.size(); i++) {
            final String stepName = stepNames.get(i);
            if (isHuman.get(i)) {
                if (activityDefinitionBuilder != null) {
                    activityDefinitionBuilder = activityDefinitionBuilder.addUserTask(stepName, actorName);
                } else {
                    activityDefinitionBuilder = processBuilder.addUserTask(stepName, actorName);
                }
            } else {
                if (activityDefinitionBuilder != null) {
                    activityDefinitionBuilder = activityDefinitionBuilder.addAutomaticTask(stepName);
                } else {
                    activityDefinitionBuilder = processBuilder.addAutomaticTask(stepName);
                }
            }
        }
        TransitionDefinitionBuilder transitionDefinitionBuilder = null;
        if (!parallelActivities) {
            for (int i = 0; i < stepNames.size() - 1; i++) {
                if (transitionDefinitionBuilder != null) {
                    transitionDefinitionBuilder = transitionDefinitionBuilder.addTransition(stepNames.get(i), stepNames.get(i + 1));
                } else if (activityDefinitionBuilder != null) {
                    transitionDefinitionBuilder = activityDefinitionBuilder.addTransition(stepNames.get(i), stepNames.get(i + 1));
                }
            }
        }
        final DesignProcessDefinition processDefinition;
        if (transitionDefinitionBuilder == null) {
            processDefinition = processBuilder.done();
        } else {
            processDefinition = transitionDefinitionBuilder.getProcess();
        }
        return processDefinition;
    }

    public static DesignProcessDefinition createProcessDefinitionWithHumanAndAutomaticSteps(final String processName, final String processVersion,
            final List<String> stepNames, final List<Boolean> isHuman, final String actorName, final boolean addActorInitiator)
            throws InvalidProcessDefinitionException {
        return createProcessDefinitionWithHumanAndAutomaticSteps(processName, processVersion, stepNames, isHuman, actorName, addActorInitiator, false);
    }

    public static DesignProcessDefinition createProcessDefinitionWithHumanAndAutomaticSteps(final String processName, final String processVersion,
            final List<String> stepNames, final List<Boolean> isHuman) throws InvalidProcessDefinitionException {
        return createProcessDefinitionWithHumanAndAutomaticSteps(processName, processVersion, stepNames, isHuman, ACTOR_NAME, false);
    }

    public static DesignProcessDefinition createProcessDefinitionWithHumanAndAutomaticSteps(final List<String> stepNames, final List<Boolean> isHuman)
            throws InvalidProcessDefinitionException {
        return createProcessDefinitionWithHumanAndAutomaticSteps(PROCESS_NAME, PROCESS_VERSION, stepNames, isHuman, ACTOR_NAME, false);
    }

    public static APISession loginOnTenant(final String userName, final String password, final long tenantId) throws LoginException, BonitaHomeNotSetException,
            ServerAPIException, UnknownAPITypeException {
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        return loginAPI.login(tenantId, userName, password);
    }

    public static APISession loginOnTenantWithDefaultTechnicalLogger(final long tenantId) throws BonitaException {
        return loginOnTenant(DEFAULT_TECHNICAL_USER_USERNAME, DEFAULT_TECHNICAL_USER_PASSWORD, tenantId);
    }

    public static APISession loginOnDefaultTenantWithDefaultTechnicalUser() throws BonitaException {
        return loginOnDefaultTenant(DEFAULT_TECHNICAL_USER_USERNAME, DEFAULT_TECHNICAL_USER_PASSWORD);
    }

    public static APISession loginOnDefaultTenant(final String userName, final String password) throws BonitaException {
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        return loginAPI.login(defaultTenantId, userName, password);
    }

    public static User createUserOnDefaultTenant(final String userName, final String password) throws BonitaException {
        final APISession session = BPMTestSPUtil.loginOnDefaultTenantWithDefaultTechnicalUser();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final User user = identityAPI.createUser(userName, password);
        assertNull(user.getLastConnection());
        BPMTestSPUtil.logoutOnTenant(session);
        return user;
    }

    public static void deleteUserOnDefaultTenant(final User user) throws BonitaException {
        final APISession session = BPMTestSPUtil.loginOnDefaultTenantWithDefaultTechnicalUser();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        identityAPI.deleteUser(user.getId());
        BPMTestSPUtil.logoutOnTenant(session);
    }

    public static User createUserOnTenant(final String userName, final String password, final long tenantId, final String techUserName,
            final String techPassword) throws BonitaException {
        final APISession session = BPMTestSPUtil.loginOnTenant(techUserName, techPassword, tenantId);
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final User user = identityAPI.createUser(userName, password);
        assertNull(user.getLastConnection());
        BPMTestSPUtil.logoutOnTenant(session);
        return user;
    }

    public static User createUserOnTenantWithDefaultTechnicalLogger(final String userName, final String password, final long tenantId) throws BonitaException {
        final APISession session = BPMTestSPUtil.loginOnTenant(DEFAULT_TECHNICAL_USER_USERNAME, DEFAULT_TECHNICAL_USER_PASSWORD, tenantId);
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final User user = identityAPI.createUser(userName, password);
        assertNull(user.getLastConnection());
        BPMTestSPUtil.logoutOnTenant(session);
        return user;
    }

    public static void deactivateTenant(final long tenantId) throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.deactiveTenant(tenantId);
        logoutOnPlatform(session);
    }

    public static void deactivateAndDeleteTenant(final long tenantId) throws BonitaException {
        final APISession apiSession = loginOnTenantWithDefaultTechnicalLogger(tenantId);
        ClientEventUtil.undeployCommand(apiSession);
        logoutOnTenant(apiSession);
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.deactiveTenant(tenantId);
        platformAPI.deleteTenant(tenantId);
        logoutOnPlatform(session);
    }

    public static void deactivateAndDeleteTenant(final long tenantId, final String techinalUsername, final String techinalPassword) throws BonitaException {
        final APISession apiSession = loginOnTenant(techinalUsername, techinalPassword, tenantId);
        ClientEventUtil.undeployCommand(apiSession);
        logoutOnTenant(apiSession);
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.deactiveTenant(tenantId);
        platformAPI.deleteTenant(tenantId);
        logoutOnPlatform(session);
    }

    public static void deactivateDefaultTenant() throws BonitaException {
        deactivateTenant(defaultTenantId);
    }

    public static void activateDefaultTenant() throws BonitaException {
        activateTenant(defaultTenantId);
    }

    public static void activateTenant(final long tenantId) throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.activateTenant(tenantId);
        logoutOnPlatform(session);
    }

}
