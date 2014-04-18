package org.bonitasoft.engine.command.web;

import org.bonitasoft.engine.external.profile.command.ProfileImportCommandTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(Suite.class)
@SuiteClasses({
        ActorCommandTest.class,
        ProfileImportCommandTest.class,
        ActorPermissionCommandTest.class,
        GetUpdatedVariableValuesForProcessDefinitionTest.class,
        GetUpdatedVariableValuesForActivityInstanceTest.class,
        GetUpdatedVariableValuesForProcessInstanceTest.class,
        ActivityCommandTest.class,
        EntityMemberCommandsTest.class
})
public class ExternalCommandsTest {

}
