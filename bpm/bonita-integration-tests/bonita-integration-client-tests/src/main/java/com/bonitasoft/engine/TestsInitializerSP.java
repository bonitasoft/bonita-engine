package com.bonitasoft.engine;

import org.bonitasoft.engine.BonitaSuiteRunner;
import org.bonitasoft.engine.TestsInitializer;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.PlatformSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;

public class TestsInitializerSP extends TestsInitializer {

    static ConfigurableApplicationContext springContext;

    private static APITestSPUtil testUtil = new APITestSPUtil();

    private static TestsInitializerSP _INSTANCE;

    public static void beforeAll() throws Exception {
        TestsInitializerSP.getInstance().before();
    }

    private static TestsInitializerSP getInstance() {
        if (_INSTANCE == null) {
            _INSTANCE = new TestsInitializerSP();
        }
        return _INSTANCE;
    }

    public static void afterAll() throws Exception {
        TestsInitializerSP.getInstance().after();

    }

    @Override
    protected void deleteTenantAndPlatform() throws BonitaException {
        SPBPMTestUtil.destroyPlatformAndTenants();
        testUtil.deletePlatformStructure();
    }

    @Override
    protected void initPlatformAndTenant() throws Exception {
        try {
            testUtil.createPlatformStructure();
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(BonitaSuiteRunner.class);
            logger.error("unable to create platform", e);
            final PlatformSession session = testUtil.loginPlatform();
            final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
            platformAPI.stopNode();
            platformAPI.cleanPlatform();
            testUtil.deletePlatformStructure();
            testUtil.createPlatformStructure();
        }
        System.setProperty("delete.job.frequency", "0/30 * * * * ?");

        SPBPMTestUtil.createEnvironmentWithDefaultTenant();
    }

}
