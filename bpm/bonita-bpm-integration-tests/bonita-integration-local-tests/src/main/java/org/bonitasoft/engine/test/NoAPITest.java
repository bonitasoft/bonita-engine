package org.bonitasoft.engine.test;

import org.bonitasoft.engine.api.impl.IdentityAPIImpl;
import org.bonitasoft.engine.api.impl.LoginAPIImpl;
import org.bonitasoft.engine.api.impl.PlatformAPIImpl;
import org.bonitasoft.engine.api.impl.PlatformLoginAPIImpl;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Test;

public class NoAPITest {

    @Test
    public void directLoginTest() throws Exception {
        final ServiceAccessorFactory instance = ServiceAccessorFactory.getInstance();
        final SessionAccessor sessionAccessor = instance.createSessionAccessor();
        final PlatformLoginAPIImpl platformLoginAPIImpl = new PlatformLoginAPIImpl();
        final PlatformSession platformSession = platformLoginAPIImpl.login("platformAdmin", "platform");
        final PlatformAPIImpl platformAPIImpl = new PlatformAPIImpl();
        sessionAccessor.setSessionInfo(platformSession.getId(), -1);
        platformAPIImpl.createAndInitializePlatform();
        platformLoginAPIImpl.logout(platformSession);

        final LoginAPIImpl loginAPIImpl = new LoginAPIImpl();
        final APISession login = loginAPIImpl.login("install", "install");
        final IdentityAPIImpl identityAPIImpl = new IdentityAPIImpl();
        sessionAccessor.setSessionInfo(login.getId(), login.getTenantId());
        identityAPIImpl.getNumberOfUsers();
        loginAPIImpl.logout(login);
    }
}
