/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.test.toolkit.organization;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.identity.ContactDataCreator;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCreator;
import org.bonitasoft.engine.platform.LogoutException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.session.SessionNotFoundException;
import org.bonitasoft.test.toolkit.bpm.TestActor;
import org.bonitasoft.test.toolkit.exception.TestToolkitException;

/**
 * @author Vincent Elcrin
 */
public class TestUser implements TestActor {

    private final String userName;

    private final String password;

    private User user;

    private APISession apiSession;

    private boolean loggedIn = false;

    protected TestUser(final String userName, final String password) {
        this.userName = userName;
        this.password = password;
    }

    public TestUser(final APISession apiSession, final String userName, final String password) {
        this.user = createUser(apiSession, userName, password);
        this.userName = userName;
        this.password = password;
        /*
         * System.err.println("\n\n");
         * System.err.println("Building user: " + user.getUserName());
         * Thread.dumpStack();
         * System.err.println("\n\n");
         */
    }

    public TestUser(final APISession apiSession, UserCreator userCreator, final ContactDataCreator personalInfoCreator,
            final ContactDataCreator professionalInfoCreator) {
        this.user = createUser(apiSession, userCreator, personalInfoCreator, professionalInfoCreator);
        this.userName = (String) userCreator.getFields().get(UserCreator.UserField.NAME);
        this.password = (String) userCreator.getFields().get(UserCreator.UserField.PASSWORD);
        /*
         * System.err.println("\n\n");
         * System.err.println("Building user: " + user.getUserName());
         * Thread.dumpStack();
         * System.err.println("\n\n");
         */
    }

    public TestUser(final APISession apiSession, final UserCreator userBuilder) {
        this(apiSession, userBuilder, new ContactDataCreator(), new ContactDataCreator());
    }

    private APISession logIn(final String userName, final String password) {
        LoginAPI loginAPI;
        APISession apiSession;
        try {
            loginAPI = TenantAPIAccessor.getLoginAPI();
            apiSession = loginAPI.login(userName, password);
        } catch (final Exception e) {
            throw new TestToolkitException("Can't log user <" + userName + "> in", e);
        }

        this.loggedIn = true;
        return apiSession;
    }

    private void logOut(final APISession apiSession) {
        LoginAPI loginAPI;
        try {
            loginAPI = TenantAPIAccessor.getLoginAPI();
            loginAPI.logout(apiSession);
        } catch (final BonitaHomeNotSetException e) {
            throw new TestToolkitException("Can't get api to log out. Bonita home not set", e);
        } catch (final ServerAPIException e) {
            throw new TestToolkitException("Can't get api to log out. Server api exception", e);
        } catch (final UnknownAPITypeException e) {
            throw new TestToolkitException("Can't get api to log out. Unknown api type", e);
        } catch (final LogoutException e) {
            throw new TestToolkitException("Can't get log out user <" + apiSession.getUserName() + ">", e);
        } catch (SessionNotFoundException e) {
            throw new TestToolkitException("Can't find the session to log out", e);
        }
        this.loggedIn = false;
    }

    public APISession logIn() {
        this.apiSession = logIn(this.userName, this.password);
        return this.apiSession;
    }

    public void logOut() {
        if (this.apiSession != null) {
            logOut(this.apiSession);
        }
    }

    public APISession getSession() {
        if (!this.loggedIn) {
            logIn();
        }
        return this.apiSession;
    }

    protected static IdentityAPI getIdentityAPI(final APISession apiSession) {
        IdentityAPI identityAPI;
        try {
            identityAPI = TenantAPIAccessor.getIdentityAPI(apiSession);
        } catch (final InvalidSessionException e) {
            throw new TestToolkitException("Can't get identity api. Invalid session", e);
        } catch (final BonitaHomeNotSetException e) {
            throw new TestToolkitException("Can't get identity api. Bonita home not set", e);
        } catch (final ServerAPIException e) {
            throw new TestToolkitException("Can't get identity api. Server api exception", e);
        } catch (final UnknownAPITypeException e) {
            throw new TestToolkitException("Can't get identity api. Unknown api type", e);
        }
        return identityAPI;
    }

    // //////////////////////////////////////////////////////////////////////////////////
    // / Users creation
    // //////////////////////////////////////////////////////////////////////////////////

    /**
     * Create user with only username & password
     */
    private User createUser(final APISession apiSession, final String userName, final String password) {
        final IdentityAPI identityAPI = getIdentityAPI(apiSession);
        User newUser;
        try {
            newUser = identityAPI.createUser(userName, password);
        } catch (final AlreadyExistsException e) {
            try {
                newUser = identityAPI.getUserByUserName(userName);
            } catch (final Exception e1) {
                throw new TestToolkitException("Can't get user <" + userName + ">", e);
            }
        } catch (final Exception e) {
            throw new TestToolkitException("Can't create user <" + userName + ">", e);
        }
        return newUser;
    }

    /**
     * Create user with user engine's builder
     */
    private User createUser(final APISession apiSession, final UserCreator creator,
            final ContactDataCreator personalInfoCreator,
            final ContactDataCreator professionalInfoBuilder) {
        final IdentityAPI identityAPI = getIdentityAPI(apiSession);
        User newUser;
        try {
            try {
                creator.setPersonalContactData(personalInfoCreator);
                creator.setPersonalContactData(professionalInfoBuilder);
                newUser = identityAPI.createUser(creator);
            } catch (final AlreadyExistsException e) {
                try {
                    String userName = (String) creator.getFields().get(UserCreator.UserField.NAME);
                    newUser = identityAPI.getUserByUserName(userName);
                } catch (final NotFoundException getEx) {
                    throw new TestToolkitException("User <" + userName + "> not found", e);
                }
            } catch (final CreationException e) {
                throw new TestToolkitException("Can't create user", e);
            }
        } catch (final InvalidSessionException e) {
            throw new TestToolkitException("Can't get identity api to create user. Invalid session", e);
        }

        return newUser;
    }

    public TestUser createUser(final UserCreator userBuilder) {
        return new TestUser(getSession(), userBuilder);
    }

    public TestUser createUser(final String userName, final String password) {
        return new TestUser(getSession(), userName, password);
    }

    public void delete(final TestUser testUser) {
        final IdentityAPI identityAPI = getIdentityAPI(getSession());
        try {
            identityAPI.deleteUser(testUser.getUser().getId());
        } catch (final Exception e) {
            throw new TestToolkitException("Can't delete user", e);
        }
    }

    public User getUser() {
        return this.user;
    }

    public long getId() {
        return this.user.getId();
    }

    public String getUserName() {
        return userName;
    }

}
