/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine;

import org.bonitasoft.engine.bpm.ActorMappingServiceTest;
import org.bonitasoft.engine.bpm.CategoryServiceIntegrationTest;
import org.bonitasoft.engine.bpm.DocumentServiceTest;
import org.bonitasoft.engine.bpm.GatewayInstanceServiceIntegrationTest;
import org.bonitasoft.engine.bpm.NodeConfigurationTest;
import org.bonitasoft.engine.bpm.OperationServiceIntegrationTest;
import org.bonitasoft.engine.bpm.ProcessDefinitionServiceIntegrationTest;
import org.bonitasoft.engine.bpm.ProcessInstanceServiceIntegrationTest;
import org.bonitasoft.engine.bpm.SupervisorServiceTest;
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
        ActorMappingServiceTest.class,
        OperationServiceIntegrationTest.class,
        GatewayInstanceServiceIntegrationTest.class,
        CategoryServiceIntegrationTest.class,
        DocumentServiceTest.class,
        EventInstanceServiceTest.class,
        FlowNodeInstanceServiceTest.class,
        SupervisorServiceTest.class,
        NodeConfigurationTest.class,
        ConnectorInstanceServiceIntegrationTests.class,
        PlatformLoginServiceTest.class
})
public class AllBPMTests {
}
