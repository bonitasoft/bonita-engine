package org.bonitasoft.engine.authentication;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.SUserBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserBuilderFactory;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class AuthenticationServiceTest extends CommonServiceTest {

    private static GenericAuthenticationService authService;

    private static IdentityService identityService;

    static {
        identityService = getServicesBuilder().buildIdentityService();
        authService = getServicesBuilder().buildAuthenticationService();
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
