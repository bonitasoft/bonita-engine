package com.bonitasoft.engine;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.BonitaSuiteRunner;
import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.util.APITypeManager;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({ ClusterTests.class })
@Initializer(ClusteredTestSuiteIT.class)
public class ClusteredTestSuiteIT {

    public static void beforeAll() throws Exception {
        System.err.println("=================== ClusteredTestSuiteIT.beforeClass()");
        APITestUtil.createPlatformStructure();
        SPBPMTestUtil.createEnvironmentWithDefaultTenant();
        // init the context here
        changeToNode2();
        PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        PlatformSession platformSession = platformLoginAPI.login("platformAdmin", "platform");
        PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        platformAPI.startNode();
        platformLoginAPI.logout(platformSession);
        changeToNode1();
        // platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        // platformSession = platformLoginAPI.login("platformAdmin", "platform");
        // platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        // platformAPI.startNode();
        // platformLoginAPI.logout(platformSession);
    }

    public static void afterAll() throws Exception {
        System.err.println("=================== ClusteredTestSuiteIT.afterClass()");
        SPBPMTestUtil.destroyPlatformAndTenants();
        APITestSPUtil.deletePlatformStructure();
    }

    protected static void changeToNode1() throws Exception {
        setConnectionPort("7180");
    }

    protected static void changeToNode2() throws Exception {
        setConnectionPort("7280");
    }

    private static void setConnectionPort(final String port) throws Exception {
        Map<String, String> parameters = new HashMap<String, String>(2);
        parameters.put("server.url", "http://localhost:" + port);
        parameters.put("application.name", "bonita");
        APITypeManager.setAPITypeAndParams(ApiAccessType.HTTP, parameters);
    }

}
