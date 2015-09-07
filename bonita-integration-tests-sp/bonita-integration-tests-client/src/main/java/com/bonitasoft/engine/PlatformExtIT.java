/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.runner.BonitaTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Elias Ricken de Medeiros
 */
@RunWith(BonitaTestRunner.class)
public class PlatformExtIT {

    private APITestSPUtil apiTestUtil = new APITestSPUtil();
    private PlatformSession platformSession;
    private PlatformAPI platformAPI;

    @Before
    public void setUp() throws Exception {
        platformSession = apiTestUtil.loginOnPlatform();
        platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);

    }

    @After
    public void tearDown() throws Exception {
        if (platformSession != null) {
            apiTestUtil.logoutOnPlatform(platformSession);
        }
    }

    @Test
    public void getInfo_should_contains_platform_info() throws Exception {
        //given

        //when
        Map<String, String> information = platformAPI.getInformation();

        //then
        assertThat(information).containsKeys("subscriptionStartPeriod", "subscriptionEndPeriod", "caseCounterLimit", "caseCounter");
        assertThat(Integer.valueOf(information.get("caseCounter"))).isGreaterThanOrEqualTo(0);
        assertThat(Integer.valueOf(information.get("caseCounterLimit"))).isGreaterThan(0);
    }
}
