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

import com.bonitasoft.engine.api.APIClient;
import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;
import com.bonitasoft.engine.platform.Tenant;
import com.bonitasoft.engine.platform.TenantCreator;
import com.bonitasoft.engine.platform.TenantDeactivationException;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PlatformLoginAPI;
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

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class BPMTestSPUtil {

    private static final String DEFAULT_TECHNICAL_USER_USERNAME = "install";

    private static final String DEFAULT_TECHNICAL_USER_PASSWORD = "install";

    private static final String DEFAULT_TENANT_DESCRIPTION = "the default tenant";

    private static long defaultTenantId;

    static final APIClient apiClient = new APIClient();

    public static void createEnvironmentWithDefaultTenant() throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.initializePlatform();
        platformAPI.startNode();
        defaultTenantId = platformAPI.getDefaultTenant().getId();
        logoutOnPlatform(session);
        final APISession loginDefaultTenant = loginOnDefaultTenantWithDefaultTechnicalUser();
        ClientEventUtil.deployCommand(loginDefaultTenant);
        logoutOnTenant();
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
        logoutOnTenant();
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
        final APISession apiSession = loginOnTenantWithDefaultTechnicalUser(tenantId);
        ClientEventUtil.deployCommand(apiSession);
        logoutOnTenant();
        return tenantId;
    }

    public static void logoutOnTenant() throws BonitaException {
        apiClient.logout();
    }

    public static PlatformSession loginOnPlatform() throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        return platformLoginAPI.login("platformAdmin", "platform");
    }

    public static void logoutOnPlatform(final PlatformSession session) throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        platformLoginAPI.logout(session);
    }

    public static APISession loginOnTenant(final String userName, final String password, final long tenantId) throws LoginException, BonitaHomeNotSetException,
            ServerAPIException, UnknownAPITypeException {
        apiClient.login(tenantId, userName, password);
        return apiClient.getSession();
    }

    public static APISession loginOnTenantWithDefaultTechnicalUser(final long tenantId) throws BonitaException {
        return loginOnTenant(DEFAULT_TECHNICAL_USER_USERNAME, DEFAULT_TECHNICAL_USER_PASSWORD, tenantId);
    }

    public static APISession loginOnDefaultTenantWithDefaultTechnicalUser() throws BonitaException {
        return loginOnDefaultTenant(DEFAULT_TECHNICAL_USER_USERNAME, DEFAULT_TECHNICAL_USER_PASSWORD);
    }

    public static APISession loginOnDefaultTenant(final String userName, final String password) throws BonitaException {
        apiClient.login(defaultTenantId, userName, password);
        return apiClient.getSession();
    }

    private static long getDefaultTenantId() {
        //TODO temporary fix
        final long defaultTenantId = BPMTestSPUtil.defaultTenantId;
        if(defaultTenantId == 0){
            return TestEngineSP.getInstance().getDefaultTenantId();
        }
        return defaultTenantId;
    }

    public static User createUserOnDefaultTenant(final String userName, final String password) throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        final IdentityAPI identityAPI = apiClient.getIdentityAPI();
        final User user = identityAPI.createUser(userName, password);
        assertNull(user.getLastConnection());
        logoutOnTenant();
        return user;
    }

    public static void deleteUserOnDefaultTenant(final User user) throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        final IdentityAPI identityAPI = apiClient.getIdentityAPI();
        identityAPI.deleteUser(user.getId());
        BPMTestSPUtil.logoutOnTenant();
    }

    public static User createUserOnTenant(final String userName, final String password, final long tenantId, final String techUserName,
            final String techPassword) throws BonitaException {
        apiClient.login(tenantId, techUserName, techPassword);
        final IdentityAPI identityAPI = apiClient.getIdentityAPI();
        final User user = identityAPI.createUser(userName, password);
        assertNull(user.getLastConnection());
        logoutOnTenant();
        return user;
    }

    public static User createUserOnTenantWithDefaultTechnicalLogger(final String userName, final String password, final long tenantId) throws BonitaException {
        loginOnTenant(DEFAULT_TECHNICAL_USER_USERNAME, DEFAULT_TECHNICAL_USER_PASSWORD, tenantId);
        final IdentityAPI identityAPI = apiClient.getIdentityAPI();
        final User user = identityAPI.createUser(userName, password);
        assertNull(user.getLastConnection());
        logoutOnTenant();
        return user;
    }

    public static void deactivateTenant(final long tenantId) throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.deactiveTenant(tenantId);
        logoutOnPlatform(session);
    }

    public static void deactivateAndDeleteTenant(final long tenantId) throws BonitaException {
        final APISession apiSession = loginOnTenantWithDefaultTechnicalUser(tenantId);
        ClientEventUtil.undeployCommand(apiSession);
        logoutOnTenant();
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.deactiveTenant(tenantId);
        platformAPI.deleteTenant(tenantId);
        logoutOnPlatform(session);
    }

    public static void deactivateAndDeleteTenant(final long tenantId, final String techinalUsername, final String techinalPassword) throws BonitaException {
        final APISession apiSession = loginOnTenant(techinalUsername, techinalPassword, tenantId);
        ClientEventUtil.undeployCommand(apiSession);
        logoutOnTenant();
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.deactiveTenant(tenantId);
        platformAPI.deleteTenant(tenantId);
        logoutOnPlatform(session);
    }

    public static void deactivateDefaultTenant() throws BonitaException {
        deactivateTenant(getDefaultTenantId());
    }

    public static void activateDefaultTenant() throws BonitaException {
        activateTenant(getDefaultTenantId());
    }

    public static void activateTenant(final long tenantId) throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.activateTenant(tenantId);
        logoutOnPlatform(session);
    }

}
