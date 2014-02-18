package org.bonitasoft.engine;

import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.process.actor.ProcessActorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        ProcessActorTest.class })

@Initializer(TestsInitializer.class)
public class TemporaryTests  {

}
