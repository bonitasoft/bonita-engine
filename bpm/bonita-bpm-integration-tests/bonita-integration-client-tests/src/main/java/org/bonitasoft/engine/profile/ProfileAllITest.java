package org.bonitasoft.engine.profile;

import org.bonitasoft.engine.BonitaSuiteRunner;
import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.TestsInitializer;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({ ProfileITest.class,
        ProfileEntryITest.class,
        ProfileMemberITest.class })
@Initializer(TestsInitializer.class)
public class ProfileAllITest {

}
