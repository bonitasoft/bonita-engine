package com.bonitasoft.engine.profile;

import org.bonitasoft.engine.BonitaSuiteRunner;
import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import com.bonitasoft.engine.TestsInitializerSP;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({ ProfileTest.class,
        ProfileEntryTest.class,
        ProfileMemberTest.class,
        ProfileImportAndExportTest.class })
@Initializer(TestsInitializerSP.class)
public class ProfileTests {

}
