package org.bonitasoft.engine.bpm.test;

import org.bonitasoft.engine.bpm.*;
import org.bonitasoft.engine.bpm.DocumentContentServiceTest;
import org.bonitasoft.engine.bpm.connector.ConnectorInstanceServiceIntegrationTests;
import org.bonitasoft.engine.bpm.event.EventInstanceServiceTest;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceServiceTest;
import org.bonitasoft.engine.platform.login.PlatformLoginServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ProcessDefinitionServiceIntegrationTest.class,
        ProcessInstanceServiceIntegrationTest.class,
        TokenServiceIntegrationTest.class,
        ActorMappingServiceTest.class,
        OperationServiceIntegrationTest.class,
        GatewayInstanceServiceIntegrationTest.class,
        CategoryServiceIntegrationTest.class,
        CommentServiceTest.class,
        // DocumentMappingServiceTest.class, must add new tests here
        DocumentContentServiceTest.class,
        EventInstanceServiceTest.class,
        FlowNodeInstanceServiceTest.class,
        SupervisorServiceTest.class,
        NodeConfigurationTest.class,
        APIAccessorTest.class,
        ConnectorInstanceServiceIntegrationTests.class,
        PlatformLoginServiceTest.class
})
public class AllBPMTests {
}
