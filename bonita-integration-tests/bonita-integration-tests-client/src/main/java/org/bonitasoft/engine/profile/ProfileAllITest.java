package org.bonitasoft.engine.profile;

import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.TestsInitializer;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({ ProfileIT.class,
        ProfileEntryIT.class,
        ProfileMemberIT.class })
@Initializer(TestsInitializer.class)
public class ProfileAllITest {

}
