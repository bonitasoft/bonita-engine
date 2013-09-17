package com.bonitasoft.engine;

import org.bonitasoft.engine.BonitaSuiteRunner;
import org.bonitasoft.engine.TestsInitializer;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.PlatformSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

public class TestsInitializerSP extends TestsInitializer {

    static ConfigurableApplicationContext springContext;

    private static TestsInitializerSP INSTANCE;

    public static void beforeAll() throws Exception {
        TestsInitializerSP.getInstance().before();
    }

    private static TestsInitializerSP getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TestsInitializerSP();
        }
        return INSTANCE;
    }

    public static void afterAll() throws Exception {
        TestsInitializerSP.getInstance().after();

    }

    @Override
    protected void deleteTenantAndPlatform() throws BonitaException {
        SPBPMTestUtil.destroyPlatformAndTenants();
        APITestSPUtil.deletePlatformStructure();
    }

    @Override
    protected void initPlatformAndTenant() throws Exception {
        try {
            APITestSPUtil.createPlatformStructure();
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(BonitaSuiteRunner.class);
            logger.error("unable to create platform", e);
            final PlatformSession session = APITestSPUtil.loginPlatform();
            final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
            platformAPI.stopNode();
            platformAPI.cleanPlatform();
            APITestSPUtil.deletePlatformStructure();
            APITestSPUtil.createPlatformStructure();
        }
        System.setProperty("delete.job.frequency", "0/30 * * * * ?");

        SPBPMTestUtil.createEnvironmentWithDefaultTenant();
    }

}
