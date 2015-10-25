package com.bonitasoft.engine.test;

import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.test.junit.BonitaEngineSPRule;
import org.bonitasoft.engine.test.junit.BonitaEngineRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class TestEngineSPIT {

    @Rule
    public BonitaEngineRule bonitaEngineSPRule = BonitaEngineSPRule.create();

    @Test
    public void startStopEngine() throws Exception {
        TenantAPIAccessor.getLoginAPI().login(TestEngineSP.getInstance().getDefaultTenantId(),TestEngineSP.TECHNICAL_USER_NAME,TestEngineSP.TECHNICAL_USER_PASSWORD);
    }

}