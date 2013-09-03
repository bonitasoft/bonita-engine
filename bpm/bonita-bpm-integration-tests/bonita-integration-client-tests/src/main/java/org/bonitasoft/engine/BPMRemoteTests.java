package org.bonitasoft.engine;

import org.bonitasoft.engine.accessors.TenantAccessorTest;
import org.bonitasoft.engine.activity.CallActivityTest;
import org.bonitasoft.engine.activity.LoopTest;
import org.bonitasoft.engine.activity.MultiInstanceTest;
import org.bonitasoft.engine.command.CommandTest;
import org.bonitasoft.engine.command.web.ExternalCommandsTest;
import org.bonitasoft.engine.connectors.RemoteConnectorExecutionTest;
import org.bonitasoft.engine.event.EventTests;
import org.bonitasoft.engine.identity.IdentityTests;
import org.bonitasoft.engine.login.LoginAPITest;
import org.bonitasoft.engine.login.PlatformLoginAPITest;
import org.bonitasoft.engine.operation.OperationTest;
import org.bonitasoft.engine.platform.RestartHandlersTests;
import org.bonitasoft.engine.platform.command.PlatformCommandTest;
import org.bonitasoft.engine.process.ProcessTests;
import org.bonitasoft.engine.process.SupervisorTest;
import org.bonitasoft.engine.process.task.ManualTasksTest;
import org.bonitasoft.engine.profile.ProfileTests;
import org.bonitasoft.engine.search.SearchEntitiesTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        RestartHandlersTests.class,
        RemoteConnectorExecutionTest.class,
        PlatformCommandTest.class,
        ProcessTests.class,
        ProfileTests.class,
        SearchEntitiesTests.class,
        EventTests.class,
        IdentityTests.class,
        LoginAPITest.class,
        PlatformLoginAPITest.class,
        CommandTest.class,
        ExternalCommandsTest.class,
        SupervisorTest.class,
        OperationTest.class,
        ManualTasksTest.class,
        CallActivityTest.class,
        LoopTest.class,
        MultiInstanceTest.class,
        TenantAccessorTest.class,
        MultiThreadCallsTest.class
})
public class BPMRemoteTests {

}
