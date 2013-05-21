package com.bonitasoft.engine;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.session.PlatformSession;
import org.junit.Test;

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.platform.TenantCreator;

// @RunWith(PowerMockRunner.class)
// @PrepareForTest(Manager.class)
public class LicenseTest extends CommonAPISPTest {

    @Test
    public void invalidLicense() throws BonitaException {
        final PlatformSession session = loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        try {
            // License invalid == not exists
            platformAPI.createTenant(new TenantCreator("lumi corp", "The snow maker", null, null, "lumi", "lumi"));
        } catch (final Exception e) {
            logoutPlatform(session);
        }
        try {
            login();
        } catch (final LoginException le) {
            startNode();
            login();
            logout();
        }
    }

    private void startNode() throws BonitaException {
        final PlatformSession session2 = loginPlatform();
        final PlatformAPI platformAPI2 = PlatformAPIAccessor.getPlatformAPI(session2);
        platformAPI2.startNode();
        logoutPlatform(session2);
    }

}
