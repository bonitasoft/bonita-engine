package com.bonitasoft.engine.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.LoginException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.session.APISession;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.SPBPMTestUtil;
import com.bonitasoft.engine.api.LoginAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;

/**
 * @author Matthieu Chaffotte
 */
public class SPUserTest extends CommonAPISPTest {

    @Test(expected = LoginException.class)
    public void loginFailsUsingWrongTenant() throws BonitaException {
        final String userName = "technical_user_username";
        final String password = "technical_user_password";
        final LoginAPI loginTenant = TenantAPIAccessor.getLoginAPI();
        loginTenant.login(2, userName, password);
    }

    @Test(expected = LoginException.class)
    public void loginFailsUsingWrongUser() throws BonitaException, BonitaHomeNotSetException {
        final String userName = "hannu";
        final String password = "technical_user_password";
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        loginAPI.login(1, userName, password);
    }

    @Test(expected = LoginException.class)
    public void loginFailsUsingWrongPassword() throws BonitaException, BonitaHomeNotSetException {
        final String userName = "technical_user_username";
        final String password = "suomi";
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        loginAPI.login(1, userName, password);
    }

    @Test(expected = LoginException.class)
    public void loginFailsDueToTenantDeactivation() throws BonitaException {
        final long tenantId = SPBPMTestUtil.constructTenant("suomi", "iconName", "iconPath", "hannu", "malminkartano");
        SPBPMTestUtil.deactivateTenant(tenantId);
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        try {
            loginAPI.login(tenantId, "matti", "tervetuloa");
            fail("The login method should throw a TenantNotActivatedException due to tenant deactivation");
        } finally {
            SPBPMTestUtil.activateTenant(tenantId);
            SPBPMTestUtil.destroyTenant(tenantId);
        }
    }

    @Test(expected = LoginException.class)
    public void loginFailsWithDeactivatedDefaultTenant() throws BonitaException {
        SPBPMTestUtil.deactivateDefaultTenant();
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        try {
            loginAPI.login("matti", "tervetuloa");
            fail("should be unable to login");
        } finally {
            SPBPMTestUtil.activateDefaultTenant();
        }
    }

    @Test
    public void userLoginTenant() throws BonitaException, InterruptedException {
        final String userName = "matti";
        final String password = "tervetuloa";
        final long tenantId = SPBPMTestUtil.constructTenant("suomi", "iconName", "iconPath", "revontuli", "paras");
        SPBPMTestUtil.createUserOnTenant(userName, password, tenantId, "revontuli", "paras");

        final Date now = new Date();
        Thread.sleep(300);
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        final APISession apiSession = loginAPI.login(tenantId, userName, password);
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(apiSession);
        final User user = identityAPI.getUserByUserName(userName);
        identityAPI.deleteUser(userName);

        assertEquals(userName, user.getUserName());
        assertEquals(password, user.getPassword());
        assertTrue(now.before(user.getLastConnection()));

        SPBPMTestUtil.destroyTenant(tenantId);
    }

    @Test
    public void aSameUserNameCanBeUseInTwoTenants() throws BonitaException {
        final String userName = "technical_user_username";
        final long tenantId1 = SPBPMTestUtil.constructTenant("tenant1", "iconName", "iconPath", userName, "technical_user_password");
        final APISession session1 = SPBPMTestUtil.loginTenant(tenantId1);
        final IdentityAPI identityAPI1 = TenantAPIAccessor.getIdentityAPI(session1);
        final User user1 = identityAPI1.createUser(userName, "bpm");

        final APISession session2 = SPBPMTestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI2 = TenantAPIAccessor.getIdentityAPI(session2);
        final User user2 = identityAPI2.createUser(userName, "bos");

        assertEquals(userName, user2.getUserName());
        assertEquals(user1.getUserName(), user2.getUserName());
        identityAPI1.deleteUser(user1.getId());
        identityAPI2.deleteUser(user2.getId());

        SPBPMTestUtil.logoutTenant(session1);
        SPBPMTestUtil.destroyTenant(tenantId1);
        SPBPMTestUtil.logoutTenant(session2);
    }

}
