package org.bonitasoft.engine.command;

import org.bonitasoft.engine.TestsInitializer;
import org.bonitasoft.engine.command.web.ExternalCommandsTests;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner.Initializer;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        CommandIT.class,
        ExternalCommandsTests.class,
        AdvancedStartProcessCommandIT.class
})
@Initializer(TestsInitializer.class)
public class CommandsTests {

}
