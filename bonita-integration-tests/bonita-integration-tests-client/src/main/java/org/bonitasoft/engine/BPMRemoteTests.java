package org.bonitasoft.engine;

import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.accessors.TenantAccessorTest;
import org.bonitasoft.engine.activity.CallActivityTest;
import org.bonitasoft.engine.activity.LoopTest;
import org.bonitasoft.engine.activity.MultiInstanceTest;
import org.bonitasoft.engine.command.AdvancedStartProcessCommandIT;
import org.bonitasoft.engine.command.CommandTest;
import org.bonitasoft.engine.command.web.ExternalCommandsTest;
import org.bonitasoft.engine.connectors.RemoteConnectorExecutionTest;
import org.bonitasoft.engine.event.EventTests;
import org.bonitasoft.engine.identity.IdentityTests;
import org.bonitasoft.engine.login.LoginAPITest;
import org.bonitasoft.engine.login.PlatformLoginAPITest;
import org.bonitasoft.engine.operation.OperationTest;
import org.bonitasoft.engine.platform.command.PlatformCommandTest;
import org.bonitasoft.engine.process.ProcessTests;
import org.bonitasoft.engine.process.task.ManualTasksTest;
import org.bonitasoft.engine.profile.ProfileAllITest;
import org.bonitasoft.engine.search.SearchEntitiesTests;
import org.bonitasoft.engine.supervisor.SupervisorTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        RemoteConnectorExecutionTest.class,
        PlatformCommandTest.class,
        ProcessTests.class,
        ProfileAllITest.class,
        SearchEntitiesTests.class,
        EventTests.class,
        IdentityTests.class,
        LoginAPITest.class,
        PlatformLoginAPITest.class,
        CommandTest.class,
        ExternalCommandsTest.class,
        SupervisorTests.class,
        OperationTest.class,
        ManualTasksTest.class,
        CallActivityTest.class,
        LoopTest.class,
        MultiInstanceTest.class,
        TenantAccessorTest.class,
        MultiThreadCallsTest.class,
        AdvancedStartProcessCommandIT.class
})
@Initializer(TestsInitializer.class)
public class BPMRemoteTests {

}
