/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
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
import org.bonitasoft.engine.bpm.model.ActivityDefinitionBuilder;
import org.bonitasoft.engine.bpm.model.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.model.TransitionDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.exception.platform.LoginException;
import org.bonitasoft.engine.exception.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.ClientEventUtil;

import com.bonitasoft.engine.api.LoginAPI;
import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.bpm.model.ProcessDefinitionBuilderExt;
import com.bonitasoft.engine.exception.TenantDeactivationException;
import com.bonitasoft.engine.exception.TenantNotActivatedException;
import com.bonitasoft.engine.platform.Tenant;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class SPBPMTestUtil {

    private static final String DEFAULT_TENANT_DESCRIPTION = "the default tenant";

    public static final String DEFAULT_TENANT = "default";

    public static final String ACTOR_NAME = "Actor1";

    public static final String PROCESS_VERSION = "1.0";

    public static final String PROCESS_NAME = "ProcessName";

    private static long defaultTenantId;

    public static void createEnvironmentWithDefaultTenant() throws BonitaException {
        final PlatformSession session = loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.initializePlatform();
        platformAPI.startNode();
        defaultTenantId = platformAPI.getDefaultTenant().getId();
        logoutPlatform(session);
        final APISession loginDefaultTenant = loginDefaultTenant();
        ClientEventUtil.deployCommand(loginDefaultTenant);
        logoutTenant(loginDefaultTenant);
    }

    public static void destroyPlatformAndTenants() throws BonitaException {
        final APISession loginDefaultTenant = loginDefaultTenant();
        ClientEventUtil.undeployCommand(loginDefaultTenant);
        logoutTenant(loginDefaultTenant);
        final PlatformSession session = loginPlatform();
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

            }
            if (!tenant.isDefaultTenant()) {
                platformAPI.deleteTenant(tenant.getId());
            }
        }
        platformAPI.stopNode();
        platformAPI.cleanPlatform();
        logoutPlatform(session);
    }

    public static void createEnvironmentWithoutTenant() throws BonitaException {
        final PlatformSession session = loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.createAndInitializePlatform();
        platformAPI.startNode();
        logoutPlatform(session);
    }

    public static void destroyEnvironmentWithoutTenant() throws BonitaException {
        final PlatformSession session = loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.stopNode();
        platformAPI.deletePlaftorm();
        logoutPlatform(session);
    }

    public static long constructTenant(final String tenantName, final String iconName, final String iconPath, final String techinalUsername,
            final String password) throws BonitaException {
        final PlatformSession session = loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        final long tenantId = platformAPI.createTenant(tenantName, DEFAULT_TENANT_DESCRIPTION, iconName, iconPath, techinalUsername, password);
        platformAPI.activateTenant(tenantId);
        logoutPlatform(session);
        return tenantId;
    }

    public static void destroyTenant(final long tenantId) throws BonitaException {
        final PlatformSession session = loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.deactiveTenant(tenantId);
        platformAPI.deleteTenant(tenantId);
        logoutPlatform(session);
    }

    public static APISession loginDefaultTenant() throws BonitaException {
        return loginTenant("install", "install", defaultTenantId);
    }

    public static APISession loginDefaultTenant(final String userName, final String password) throws BonitaException {
        return loginTenant(userName, password, defaultTenantId);
    }

    public static void logoutTenant(final APISession session) throws BonitaException {
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        loginAPI.logout(session);
    }

    public static PlatformSession loginPlatform() throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        return platformLoginAPI.login("platformAdmin", "platform");
    }

    public static void logoutPlatform(final PlatformSession session) throws BonitaException {
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
                } else {
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

    public static APISession loginTenant(final String userName, final String password, final long tenantId) throws LoginException, BonitaHomeNotSetException,
            ServerAPIException, UnknownAPITypeException, TenantNotActivatedException {
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        final APISession session = loginAPI.login(tenantId, userName, password);
        return session;
    }

    public static APISession loginTenant(final long tenantId) throws BonitaException {
        return loginTenant("install", "install", tenantId);
    }

    public static APISession loginOnDefaultTenant() throws BonitaException {
        return loginOnDefaultTenant("install", "install");
    }

    public static APISession loginOnDefaultTenant(final String userName, final String password) throws BonitaException {
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        final APISession session = loginAPI.login(defaultTenantId, userName, password);
        return session;
    }

    public static User createUserOnDefaultTenant(final String userName, final String password) throws BonitaException {
        final APISession session = SPBPMTestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final User user = identityAPI.createUser(userName, password);
        assertNull(user.getLastConnection());
        SPBPMTestUtil.logoutTenant(session);
        return user;
    }

    public static User createUserOnTenant(final String userName, final String password, final long tenantId, final String techUserName,
            final String techPassword) throws BonitaException {
        final APISession session = SPBPMTestUtil.loginTenant(techUserName, techPassword, tenantId);
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final User user = identityAPI.createUser(userName, password);
        assertNull(user.getLastConnection());
        SPBPMTestUtil.logoutTenant(session);
        return user;
    }

    public static void deactivateTenant(final long tenantId) throws BonitaException {
        final PlatformSession session = loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.deactiveTenant(tenantId);
        logoutPlatform(session);
    }

    public static void deactivateDefaultTenant() throws BonitaException {
        deactivateTenant(defaultTenantId);
    }

    public static void activateDefaultTenant() throws BonitaException {
        activateTenant(defaultTenantId);
    }

    public static void activateTenant(final long tenantId) throws BonitaException {
        final PlatformSession session = loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.activateTenant(tenantId);
        logoutPlatform(session);
    }

}
