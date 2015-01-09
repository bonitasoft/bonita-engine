package org.bonitasoft.engine.process.actor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
    ProcessActorIT.class,
    ImportActorMappingIT.class,
    ExportActorMappingIT.class
})
public class ActorTests {

}
