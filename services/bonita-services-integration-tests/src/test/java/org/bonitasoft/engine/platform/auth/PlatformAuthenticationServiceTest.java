package org.bonitasoft.engine.platform.auth;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.platform.authentication.PlatformAuthenticationService;
import org.bonitasoft.engine.platform.authentication.SInvalidPasswordException;
import org.bonitasoft.engine.platform.authentication.SInvalidUserException;
import org.junit.Test;

public class PlatformAuthenticationServiceTest extends CommonServiceTest {

    private static PlatformAuthenticationService platformAuthService;

    static {
        platformAuthService = getServicesBuilder().buildPlatformAuthenticationService();
    }

    @Test
    public void testCheckValidUser() throws Exception {
        final String username = "platformAdmin";
        final String password = "platform";

        //getTransactionService().begin();
        platformAuthService.checkUserCredentials(username, password);
        //getTransactionService().complete();
    }

    @Test(expected = SInvalidPasswordException.class)
    public void testCheckUserWithWrongPassword() throws Exception {
        final String username = "platformAdmin";
        //getTransactionService().begin();
        platformAuthService.checkUserCredentials(username, "wrong");
        //getTransactionService().complete();
    }

    @Test(expected = SInvalidUserException.class)
    public void testCheckNonExistentUser() throws Exception {
        final String username = "anonyme";
        final String password = "bpm";
        //getTransactionService().begin();
        platformAuthService.checkUserCredentials(username, password);
        //getTransactionService().complete();

    }
}
