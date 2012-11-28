package org.bonitasoft.engine.bpm.test;

import org.bonitasoft.engine.bpm.ActorMappingServiceTest;
import org.bonitasoft.engine.bpm.ActorPrivilegeServiceIntegrationTest;
import org.bonitasoft.engine.bpm.CategoryServiceIntegrationTest;
import org.bonitasoft.engine.bpm.GatewayInstanceServiceIntegrationTest;
import org.bonitasoft.engine.bpm.OperationServiceIntegrationTest;
import org.bonitasoft.engine.bpm.ProcessDefinitionServiceIntegrationTest;
import org.bonitasoft.engine.bpm.ProcessDocumentServiceTest;
import org.bonitasoft.engine.bpm.ProcessInstanceServiceIntegrationTest;
import org.bonitasoft.engine.bpm.SupervisorServiceTest;
import org.bonitasoft.engine.bpm.event.EventInstanceServiceTest;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceServiceTest;
import org.bonitasoft.engine.bpm.transition.TransitionInstanceServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    ProcessDefinitionServiceIntegrationTest.class,
//    ParameterAndDataExpressionIntegrationTest.class,
    ProcessInstanceServiceIntegrationTest.class,
    ActorMappingServiceTest.class,
    OperationServiceIntegrationTest.class,
    GatewayInstanceServiceIntegrationTest.class,
    CategoryServiceIntegrationTest.class,
//        DocumentMappingServiceTest.class, must add new tests here
    ProcessDocumentServiceTest.class,
    EventInstanceServiceTest.class,
    FlowNodeInstanceServiceTest.class,
    TransitionInstanceServiceTest.class,
    ActorPrivilegeServiceIntegrationTest.class,
    SupervisorServiceTest.class
})
public class AllBPMSPTests {

}