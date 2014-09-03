package org.bonitasoft.engine;

import org.bonitasoft.engine.activity.MultiInstanceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        MultiInstanceTest.class
})
public class TemporaryTomcatTests extends BPMRemoteTestsForServers {

}