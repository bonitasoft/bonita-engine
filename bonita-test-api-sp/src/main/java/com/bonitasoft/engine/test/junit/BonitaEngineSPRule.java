package com.bonitasoft.engine.test.junit;

import com.bonitasoft.engine.test.TestEngineSP;
import org.bonitasoft.engine.test.TestEngine;
import org.bonitasoft.engine.test.junit.BonitaEngineRule;

/**
 * @author Baptiste Mesta
 */
public class BonitaEngineSPRule extends BonitaEngineRule {



    public static BonitaEngineSPRule create(){
        return new BonitaEngineSPRule(TestEngineSP.getInstance());
    }

    public static BonitaEngineSPRule createWith(TestEngine testEngine){
        return new BonitaEngineSPRule(testEngine);
    }

    protected BonitaEngineSPRule(TestEngine testEngine) {
        super(testEngine);
    }
}
