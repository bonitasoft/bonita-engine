package org.bonitasoft.engine.command.web;

import org.bonitasoft.engine.TestsInitializer;
import org.bonitasoft.engine.external.profile.command.ProfileImportCommandIT;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner.Initializer;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        UserPermissionCommandIT.class,
        ProfileImportCommandIT.class,
        ActorPermissionCommandIT.class,
        ActivityCommandIT.class,
        EntityMemberCommandsIT.class
})
@Initializer(TestsInitializer.class)
public class ExternalCommandsTests {

}
