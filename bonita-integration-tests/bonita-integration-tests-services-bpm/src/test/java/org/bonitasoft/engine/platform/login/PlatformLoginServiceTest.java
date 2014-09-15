package org.bonitasoft.engine.platform.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.core.platform.login.PlatformLoginService;
import org.bonitasoft.engine.core.platform.login.SPlatformLoginException;
import org.bonitasoft.engine.platform.session.SSessionNotFoundException;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 * @author Emmanuel Duchastenier
 */
public class PlatformLoginServiceTest extends CommonBPMServicesTest {

    private final PlatformLoginService platformLoginService;

    public PlatformLoginServiceTest() {
        platformLoginService = getServicesBuilder().getPlatformLoginService();
    }

    @Test
    public void testLoginLogout() throws Exception {
        final String username = "platformAdmin";
        final String password = "platform";
        final SPlatformSession session = platformLoginService.login(username, password);
        assertNotNull(session);
        assertEquals(username, session.getUserName());

        platformLoginService.logout(session.getId());
    }

    @Test(expected = SPlatformLoginException.class)
    public void testLoginBadUser() throws Exception {
        final String username = "noAdmin";
        final String password = "platform";
        platformLoginService.login(username, password);
    }

    @Test(expected = SPlatformLoginException.class)
    public void testLoginBadPassword() throws Exception {
        final String username = "platformAdmin";
        final String password = "wrong";
        platformLoginService.login(username, password);
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testLogoutWrongSession() throws Exception {
        platformLoginService.logout(System.currentTimeMillis());
    }

}
