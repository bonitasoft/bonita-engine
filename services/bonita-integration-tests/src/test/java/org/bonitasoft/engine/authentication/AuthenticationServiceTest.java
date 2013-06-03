package org.bonitasoft.engine.authentication;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserBuilder;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class AuthenticationServiceTest extends CommonServiceTest {

    private static AuthenticationService authService;

    private static IdentityService identityService;

    private static IdentityModelBuilder identityBuilder;

    static {
        identityService = getServicesBuilder().buildIdentityService();
        identityBuilder = getServicesBuilder().buildIdentityModelBuilder();
        authService = getServicesBuilder().buildAuthenticationService();
    }

    @Test
    public void testCheckValidUser() throws Exception {
        final String username = "john";
        final String password = "bpm";
        final SUser user = createUser(username, password);

        getTransactionService().begin();
        authService.checkUserCredentials(username, password);
        getTransactionService().complete();

        deleteUser(user);
    }

    private SUser createUser(final String username, final String password) throws Exception {
        getTransactionService().begin();
        final SUserBuilder userBuilder = identityBuilder.getUserBuilder().createNewInstance().setUserName(username).setPassword(password);
        final SUser user = identityService.createUser(userBuilder.done());
        getTransactionService().complete();

        return user;
    }

    private void deleteUser(final SUser user) throws Exception {
        getTransactionService().begin();
        identityService.deleteUser(user);
        getTransactionService().complete();
    }

    @Test(expected = AuthenticationException.class)
    public void testCheckUserWithWrongPassword() throws Exception {
        final String username = "james";
        final String password = "bpm";
        final SUser user = createUser(username, password);

        getTransactionService().begin();
        authService.checkUserCredentials(username, "wrong");
        getTransactionService().complete();

        deleteUser(user);
    }

    @Test(expected = AuthenticationException.class)
    public void testCheckNonExistentUser() throws Exception {
        final String username = "anonyme";
        final String password = "bpm";
        getTransactionService().begin();
        authService.checkUserCredentials(username, password);
        getTransactionService().complete();

    }

}
