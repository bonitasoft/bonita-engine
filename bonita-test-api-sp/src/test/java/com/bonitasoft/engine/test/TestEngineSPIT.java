package com.bonitasoft.engine.test;

import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.test.junit.BonitaEngineSPRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class TestEngineSPIT {

    @Rule
    public BonitaEngineSPRule bonitaEngineSPRule = new BonitaEngineSPRule();

    @Test
    public void startStopEngine() throws Exception {
        TenantAPIAccessor.getLoginAPI().login(TestEngineSP.getInstance().getDefaultTenantId(),TestEngineSP.TECHNICAL_USER_NAME,TestEngineSP.TECHNICAL_USER_PASSWORD);
    }

}