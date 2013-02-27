package org.bonitasoft.engine.test;

import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.PlatformSession;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.bonitasoft.engine.BPMSPTests;
import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;

@RunWith(Suite.class)
@SuiteClasses({
    BPMSPTests.class,
    BPMLocalSuiteTests.class,
    LocalLogTest.class,
    APIMethodSPTest.class,
    ConnectorExecutionTimeOutTest.class
})
public class LocalIntegrationTestsSP  {

        @BeforeClass
        public static void beforeClass() throws BonitaException {
            System.err.println("=================== LocalIntegrationTestsSP.beforeClass()");
            PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
            PlatformSession session = platformLoginAPI.login("platformAdmin", "platform");
            PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
            platformAPI.createPlatform();
            platformLoginAPI.logout(session);

            System.setProperty("delete.job.frequency", "0/30 * * * * ?");
        }

        @AfterClass
        public static void afterClass() throws BonitaException {
            System.err.println("=================== LocalIntegrationTestsSP.afterClass()");
            PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
            PlatformSession session = platformLoginAPI.login("platformAdmin", "platform");
            PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
            platformAPI.deletePlaftorm();
            platformLoginAPI.logout(session);
        }

}
