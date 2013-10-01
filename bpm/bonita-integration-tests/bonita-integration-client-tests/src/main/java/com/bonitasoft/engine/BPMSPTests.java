package com.bonitasoft.engine;

import org.bonitasoft.engine.BPMRemoteTests;
import org.bonitasoft.engine.BonitaSuiteRunner;
import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        BPMRemoteTests.class,
        BPMRemoteSPTests.class
})
@Initializer(TestsInitializerSP.class)
public class BPMSPTests {

}
