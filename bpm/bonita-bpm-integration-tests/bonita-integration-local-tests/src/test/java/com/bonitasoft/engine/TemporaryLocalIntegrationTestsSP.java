package com.bonitasoft.engine;

import org.bonitasoft.engine.BonitaSuiteRunner;
import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import com.bonitasoft.engine.bdr.BDRIT;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        BDRIT.class
})
@Initializer(TestsInitializerSP.class)
public class TemporaryLocalIntegrationTestsSP {

}
