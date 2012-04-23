package org.bonitasoft.engine.test;

import org.bonitasoft.engine.test.event.handler.IdentityServiceUsingEventServiceTest;
import org.bonitasoft.engine.test.event.handler.RecorderAndEventServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
    IdentityServiceUsingEventServiceTest.class,
    RecorderAndEventServiceTest.class
    })
public class AllTestsSP {

}
