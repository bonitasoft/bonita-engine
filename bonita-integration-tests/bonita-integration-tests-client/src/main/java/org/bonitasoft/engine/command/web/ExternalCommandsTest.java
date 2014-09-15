package org.bonitasoft.engine.command.web;

import org.bonitasoft.engine.BonitaSuiteRunner;
import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.TestsInitializer;
import org.bonitasoft.engine.external.profile.command.ProfileImportCommandTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        UserPermissionCommandTest.class,
        ProfileImportCommandTest.class,
        ActorPermissionCommandTest.class,
        GetUpdatedVariableValuesForProcessDefinitionTest.class,
        GetUpdatedVariableValuesForActivityInstanceTest.class,
        GetUpdatedVariableValuesForProcessInstanceTest.class,
        ActivityCommandTest.class,
        EntityMemberCommandsTest.class
})
@Initializer(TestsInitializer.class)
public class ExternalCommandsTest {

}
