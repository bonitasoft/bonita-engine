package org.bonitasoft.engine.process.actor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
    ProcessActorTest.class,
    ImportActorMappingTest.class,
    ExportActorMappingTest.class
})
public class ActorTests {

}
