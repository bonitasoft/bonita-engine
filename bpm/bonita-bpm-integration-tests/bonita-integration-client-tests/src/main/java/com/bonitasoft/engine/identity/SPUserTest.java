/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.session.APISession;
import org.junit.Test;

import com.bonitasoft.engine.BPMTestSPUtil;
import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.api.LoginAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;

/**
 * @author Matthieu Chaffotte
 */
public class SPUserTest extends CommonAPISPTest {

    @Test(expected = LoginException.class)
    public void loginFailsUsingWrongTenant() throws BonitaException {
        final String userName = "install";
        final String password = "install";
        final LoginAPI loginTenant = TenantAPIAccessor.getLoginAPI();
        loginTenant.login(2, userName, password);
    }

    @Test(expected = LoginException.class)
    public void loginFailsUsingWrongUser() throws BonitaException, BonitaHomeNotSetException {
        final String userName = "hannu";
        final String password = "install";
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        loginAPI.login(1, userName, password);
    }

    @Test(expected = LoginException.class)
    public void loginFailsDueToTenantDeactivation() throws BonitaException {
        final long tenantId = BPMTestSPUtil.createAndActivateTenant("suomi", "iconName", "iconPath", "hannu", "malminkartano");
        BPMTestSPUtil.deactivateTenant(tenantId);
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        try {
            loginAPI.login(tenantId, "matti", "tervetuloa");
            fail("The login method should throw a TenantNotActivatedException due to tenant deactivation");
        } finally {
            BPMTestSPUtil.activateTenant(tenantId);
            BPMTestSPUtil.deactivateAndDeleteTenant(tenantId, "hannu", "malminkartano");
        }
    }

    @Test(expected = LoginException.class)
    public void loginFailsWithDeactivatedDefaultTenant() throws BonitaException {
        BPMTestSPUtil.deactivateDefaultTenant();
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        try {
            loginAPI.login("matti", "tervetuloa");
            fail("should be unable to login");
        } finally {
            BPMTestSPUtil.activateDefaultTenant();
        }
    }

    @Test
    public void userLoginTenant() throws BonitaException, InterruptedException {
        final String userName = "matti";
        final String password = "tervetuloa";
        final long tenantId = BPMTestSPUtil.createAndActivateTenant("suomi", "iconName", "iconPath", "revontuli", "paras");
        BPMTestSPUtil.createUserOnTenant(userName, password, tenantId, "revontuli", "paras");

        final Date now = new Date();
        Thread.sleep(300);
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        final APISession apiSession = loginAPI.login(tenantId, userName, password);
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(apiSession);
        final User user = identityAPI.getUserByUserName(userName);
        identityAPI.deleteUser(userName);

        assertEquals(userName, user.getUserName());
        assertNotSame(password, user.getPassword());
        assertTrue(now.before(user.getLastConnection()));

        BPMTestSPUtil.deactivateAndDeleteTenant(tenantId, "revontuli", "paras");
    }

    @Test
    public void aSameUserNameCanBeUseInTwoTenants() throws BonitaException {
        final long tenantId1 = BPMTestSPUtil.createAndActivateTenantWithDefaultTechnicalLogger("tenant1");
        final APISession session1 = BPMTestSPUtil.loginOnTenantWithDefaultTechnicalLogger(tenantId1);
        final IdentityAPI identityAPI1 = TenantAPIAccessor.getIdentityAPI(session1);
        final User user1 = identityAPI1.createUser(USERNAME, "bpm");

        final APISession session2 = BPMTestSPUtil.loginOnDefaultTenantWithDefaultTechnicalLogger();
        final IdentityAPI identityAPI2 = TenantAPIAccessor.getIdentityAPI(session2);
        final User user2 = identityAPI2.createUser(USERNAME, "bos");

        assertEquals(USERNAME, user2.getUserName());
        assertEquals(user1.getUserName(), user2.getUserName());
        identityAPI1.deleteUser(user1.getId());
        identityAPI2.deleteUser(user2.getId());

        BPMTestSPUtil.logoutOnTenant(session1);
        BPMTestSPUtil.deactivateAndDeleteTenant(tenantId1);
        BPMTestSPUtil.logoutOnTenant(session2);
    }

}
