package org.bonitasoft.engine.identity;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.LoginException;
import org.junit.Test;

/**
 * @author Matthieu Chaffotte
 */
public class UserTestSP extends CommonAPITest {

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

}
