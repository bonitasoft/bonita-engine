package org.bonitasoft.engine.test;

import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.test.junit.BonitaEngineRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class TestEngineIT {

    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.create();

    @Test
    public void checkEngineStarted() throws Exception {
        TenantAPIAccessor.getLoginAPI().login(TestEngine.TECHNICAL_USER_NAME,TestEngine.TECHNICAL_USER_PASSWORD);
    }

}
