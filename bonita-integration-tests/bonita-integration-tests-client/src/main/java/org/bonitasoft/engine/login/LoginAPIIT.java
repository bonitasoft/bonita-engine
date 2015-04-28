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
package org.bonitasoft.engine.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.PlatformCommandAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.command.DependencyNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserUpdater;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.session.Session;
import org.bonitasoft.engine.session.SessionNotFoundException;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class LoginAPIIT extends CommonAPIIT {

    private static final String COMMAND_NAME = "deleteSessionCommand";

    private static PlatformSession platformSession;

    @Before
    public void before() throws BonitaException, IOException {
        platformSession = loginOnPlatform();
    }

    @After
    public void after() throws BonitaException {
        logoutOnPlatform(platformSession);
    }

    @Test(expected = SessionNotFoundException.class)
    public void testSessionNotFoundExceptionIsThrownAfterSessionDeletion() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        // login to create a session
        final long sessionId = getSession().getId();

        // delete the session created by the login
        deleteSession(sessionId);

        // will throw SessionNotFoundException
        logoutOnTenant();
    }

    private void deleteSession(final long sessionId) throws IOException, AlreadyExistsException, CreationException, CreationException,
            CommandNotFoundException, CommandParameterizationException, CommandExecutionException, DeletionException, DependencyNotFoundException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        // execute a command to delete a session
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("sessionId", sessionId);
        final CommandAPI commandAPI = TenantAPIAccessor.getCommandAPI(getSession());
        commandAPI.execute(COMMAND_NAME, parameters);
    }

    @Test(expected = LoginException.class)
    public void loginFailsWithNullUsername() throws BonitaException {
        final LoginAPI loginTenant = TenantAPIAccessor.getLoginAPI();
        loginTenant.login(null, null);
    }

    @Test(expected = LoginException.class)
    public void loginFailsWithEmptyUsername() throws BonitaException {
        final LoginAPI loginTenant = TenantAPIAccessor.getLoginAPI();
        loginTenant.login("", null);
    }

    @Cover(classes = LoginAPI.class, concept = BPMNConcept.NONE, keywords = { "Login", "Password" }, story = "Try to login with null password", jira = "ENGINE-622")
    @Test(expected = LoginException.class)
    public void loginFailsWithNullPassword() throws BonitaException {
        final LoginAPI loginTenant = TenantAPIAccessor.getLoginAPI();
        loginTenant.login("matti", null);
    }

    @Cover(classes = LoginAPI.class, concept = BPMNConcept.NONE, keywords = { "Login", "Password" }, story = "Try to login with wrong password", jira = "ENGINE-622")
    @Test(expected = LoginException.class)
    public void loginFailsWithWrongPassword() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        final String userName = "Truc";
        createUser(userName, "goodPassword");
        try {
            final LoginAPI loginTenant = TenantAPIAccessor.getLoginAPI();
            loginTenant.login(userName, "WrongPassword");
            fail("Should not be reached");
        } finally {
            loginOnDefaultTenantWithDefaultTechnicalUser();
            getIdentityAPI().deleteUser(userName);
        }
    }

    @Cover(classes = LoginAPI.class, concept = BPMNConcept.NONE, keywords = { "Login", "Password" }, story = "Try to login with empty password", jira = "ENGINE-622")
    @Test(expected = LoginException.class)
    public void loginFailsWithEmptyPassword() throws BonitaException {
        final LoginAPI loginTenant = TenantAPIAccessor.getLoginAPI();
        loginTenant.login("matti", "");
    }

    @Test
    public void userLoginDefaultTenant() throws BonitaException, InterruptedException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        final String userName = "matti";
        final String password = "tervetuloa";
        createUser(userName, password);

        final Date now = new Date();
        Thread.sleep(300);
        loginOnDefaultTenantWith(userName, password);
        final User user = getIdentityAPI().getUserByUserName(userName);
        getIdentityAPI().deleteUser(userName);

        assertEquals(userName, user.getUserName());
        assertNotSame(password, user.getPassword());
        assertTrue(now.before(user.getLastConnection()));
        logoutOnTenant();
    }

    @Test
    public void loginOnDefaultTenantWithExistingUserAndCheckId() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        final String userName = "corvinus";
        final String password = "underworld";
        final User user = createUser(userName, password);
        final LoginAPI loginTenant = TenantAPIAccessor.getLoginAPI();
        final APISession login = loginTenant.login(userName, password);
        assertTrue("userId should be valuated", user.getId() != -1);
        assertEquals(user.getId(), login.getUserId());

        getIdentityAPI().deleteUser(user.getId());
        logoutOnTenant();
    }

    @Test
    public void loginOnDefaultTenantWithNonTechnicalUser() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        final User user = createUser("matti", "kieli");
        logoutOnTenant();

        loginOnDefaultTenantWith("matti", "kieli");
        assertTrue("Should be logged in as a NON-Technical user", !getSession().isTechnicalUser());
        logoutOnTenant();

        loginOnDefaultTenantWithDefaultTechnicalUser();
        getIdentityAPI().deleteUser(user.getId());
        logoutOnTenant();
    }

    @Test
    public void loginOnDefaultTenantWithTechnicalUser() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        assertTrue("Should be logged in as Technical user", getSession().isTechnicalUser());
        logoutOnTenant();
    }

    @Cover(jira = "ENGINE-1653", classes = { User.class, LoginAPI.class }, concept = BPMNConcept.NONE, keywords = { "disable user", "login" })
    @Test(expected = LoginException.class)
    public void unableToLoginWhenTheUserIsDisable() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        final String userName = "matti";
        final String password = "bpm";
        final User user = getIdentityAPI().createUser(userName, password);
        final UserUpdater updater = new UserUpdater();
        updater.setEnabled(false);
        getIdentityAPI().updateUser(user.getId(), updater);
        final LoginAPI loginTenant = TenantAPIAccessor.getLoginAPI();
        try {
            loginTenant.login(userName, password);
            fail("It is not possible to login when the user is disable.");
        } finally {
            getIdentityAPI().deleteUser(user.getId());
            logoutOnTenant();
        }
    }

}
