package org.bonitasoft.engine.bpm.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    ParameterAndDataExpressionIntegrationTest.class,
    AllBPMTests.class
})
public class AllBPMSPTests {

}