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
package org.bonitasoft.engine.authentication;

import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.SUserBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserBuilderFactory;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class AuthenticationServiceTest extends CommonBPMServicesTest {

    private static GenericAuthenticationService authService;

    private static IdentityService identityService;

    public AuthenticationServiceTest() {
        identityService = getTenantAccessor().getIdentityService();
        authService = getTenantAccessor().getAuthenticationService();
    }

    @Test
    public void testCheckValidUser() throws Exception {
        final String username = "john";
        final String password = "bpm";
        final SUser user = createUser(username, password);

        getTransactionService().begin();
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        credentials.put(AuthenticationConstants.BASIC_USERNAME, username);
        authService.checkUserCredentials(credentials);
        getTransactionService().complete();

        deleteUser(user);
    }

    private SUser createUser(final String username, final String password) throws Exception {
        getTransactionService().begin();
        final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName(username).setPassword(password);
        final SUser user = identityService.createUser(userBuilder.done());
        getTransactionService().complete();

        return user;
    }

    private void deleteUser(final SUser user) throws Exception {
        getTransactionService().begin();
        identityService.deleteUser(user);
        getTransactionService().complete();
    }

    @Test
    public void testCheckUserWithWrongPassword() throws Exception {
        final String username = "james";
        final String password = "bpm";
        final SUser user = createUser(username, password);

        getTransactionService().begin();
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, "wrong");
        credentials.put(AuthenticationConstants.BASIC_USERNAME, username);
        final String userNameResult = authService.checkUserCredentials(credentials);
        getTransactionService().complete();
        assertNull(userNameResult);

        deleteUser(user);
    }

    @Test
    public void testCheckNonExistentUser() throws Exception {
        final String username = "anonyme";
        final String password = "bpm";
        getTransactionService().begin();
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        credentials.put(AuthenticationConstants.BASIC_USERNAME, username);
        final String userNameResult = authService.checkUserCredentials(credentials);
        getTransactionService().complete();
        assertNull(userNameResult);
    }

}
