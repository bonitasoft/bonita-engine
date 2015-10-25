package com.bonitasoft.engine.test.junit;

import com.bonitasoft.engine.test.TestEngineSP;
import org.bonitasoft.engine.test.TestEngine;
import org.bonitasoft.engine.test.junit.BonitaEngineRule;

/**
 * @author Baptiste Mesta
 */
public class BonitaEngineSPRule extends BonitaEngineRule {

    @Override
    protected TestEngine getTestEngine() {
        return TestEngineSP.getInstance();
    }
}
