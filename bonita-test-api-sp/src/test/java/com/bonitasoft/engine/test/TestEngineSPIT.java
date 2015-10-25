package com.bonitasoft.engine.test;

import com.bonitasoft.engine.api.TenantAPIAccessor;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class TestEngineSPIT {

    @Test
    public void startStopEngine() throws Exception {
        final TestEngineSP testEngineSP = new TestEngineSP();
        testEngineSP.start();

        TenantAPIAccessor.getLoginAPI().login(testEngineSP.getDefaultTenantId(),TestEngineSP.TECHNICAL_USER_NAME,TestEngineSP.TECHNICAL_USER_PASSWORD);

        testEngineSP.stop();
    }

}