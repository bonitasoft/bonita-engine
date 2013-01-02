package com.bonitasoft.services;

import org.bonitasoft.engine.AllTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.bonitasoft.services.event.handler.IdentityServiceUsingEventServiceTest;
import com.bonitasoft.services.event.handler.RecorderAndEventServiceTest;

@RunWith(Suite.class)
@SuiteClasses({
    AllTests.class,
    IdentityServiceUsingEventServiceTest.class,
    RecorderAndEventServiceTest.class
})
public class AllTestsSP {

}
