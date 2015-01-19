package org.bonitasoft.engine;

import org.bonitasoft.engine.accessors.TenantAccessorTest;
import org.bonitasoft.engine.activity.TaskTests;
import org.bonitasoft.engine.activity.UserTaskContractITest;
import org.bonitasoft.engine.command.CommandsTests;
import org.bonitasoft.engine.connectors.RemoteConnectorExecutionIT;
import org.bonitasoft.engine.event.EventTests;
import org.bonitasoft.engine.identity.IdentityTests;
import org.bonitasoft.engine.login.LoginAPIIT;
import org.bonitasoft.engine.login.PlatformLoginAPIIT;
import org.bonitasoft.engine.operation.OperationIT;
import org.bonitasoft.engine.platform.command.PlatformCommandIT;
import org.bonitasoft.engine.process.ProcessTests;
import org.bonitasoft.engine.profile.ProfileAllTest;
import org.bonitasoft.engine.search.SearchEntitiesTests;
import org.bonitasoft.engine.supervisor.SupervisorTests;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner.Initializer;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        RemoteConnectorExecutionIT.class,
        PlatformCommandIT.class,
        ProcessTests.class,
        ProfileAllTest.class,
        SearchEntitiesTests.class,
        EventTests.class,
        IdentityTests.class,
        LoginAPIIT.class,
        PlatformLoginAPIIT.class,
        CommandsTests.class,
        SupervisorTests.class,
        OperationIT.class,
        TaskTests.class,
        TenantAccessorTest.class,
        MultiThreadCallsIT.class,
        UserTaskContractITest.class
})
@Initializer(TestsInitializer.class)
public class BPMRemoteTests {

}
