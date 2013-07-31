package com.bonitasoft.engine;

import org.bonitasoft.engine.BPMRemoteTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    BPMRemoteTests.class,
    BPMRemoteSPTests.class
})
public class BPMSPTests {

}
