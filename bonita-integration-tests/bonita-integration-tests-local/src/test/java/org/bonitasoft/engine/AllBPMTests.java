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
 */
package org.bonitasoft.engine;

import org.bonitasoft.engine.archive.ArchiveServiceTest;
import org.bonitasoft.engine.authentication.AuthenticationServiceTest;
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
import org.bonitasoft.engine.cache.CacheServiceTest;
import org.bonitasoft.engine.classloader.ClassLoaderServiceTest;
import org.bonitasoft.engine.command.CommandServiceIntegrationTest;
import org.bonitasoft.engine.continuation.WorkServiceTest;
import org.bonitasoft.engine.core.form.impl.FormMappingServiceIT;
import org.bonitasoft.engine.data.instance.DataInstanceServiceTest;
import org.bonitasoft.engine.data.instance.TransientDataInstanceServiceIT;
import org.bonitasoft.engine.dependency.DependencyServiceTest;
import org.bonitasoft.engine.expression.ExpressionServiceTest;
import org.bonitasoft.engine.identity.IdentityServiceTest;
import org.bonitasoft.engine.page.PageMappingServiceIT;
import org.bonitasoft.engine.persistence.PersistenceTests;
import org.bonitasoft.engine.platform.TenantManagementTest;
import org.bonitasoft.engine.platform.auth.PlatformAuthenticationServiceTest;
import org.bonitasoft.engine.platform.command.PlatformCommandServiceIntegrationTest;
import org.bonitasoft.engine.platform.login.PlatformLoginServiceTest;
import org.bonitasoft.engine.profile.ProfileServiceTest;
import org.bonitasoft.engine.recorder.RecorderTest;
import org.bonitasoft.engine.scheduler.impl.QuartzSchedulerExecutorITest;
import org.bonitasoft.engine.session.PlatformSessionServiceTest;
import org.bonitasoft.engine.session.SessionServiceTest;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@BonitaSuiteRunner.Initializer(LocalServerTestsInitializer.class)
@SuiteClasses({
        CacheServiceTest.class,
        PersistenceTests.class,
        ArchiveServiceTest.class,
        ClassLoaderServiceTest.class,
        ExpressionServiceTest.class,
        IdentityServiceTest.class,
        AuthenticationServiceTest.class,
        PlatformAuthenticationServiceTest.class,
        SessionServiceTest.class,
        PlatformSessionServiceTest.class,
        DataInstanceServiceTest.class,
        TransientDataInstanceServiceIT.class,
        DependencyServiceTest.class,
        WorkServiceTest.class,

        // -- SqlTest.class,
        // -- Tests using the scheduler
        RecorderTest.class,
        QuartzSchedulerExecutorITest.class,
        // JobTest.class, ignored as this was the last test method in that class
        CommandServiceIntegrationTest.class,
        PlatformCommandServiceIntegrationTest.class,
        ProfileServiceTest.class,
        TenantManagementTest.class,
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
        PlatformLoginServiceTest.class,
        FormMappingServiceIT.class,
        PageMappingServiceIT.class
})
public class AllBPMTests {
}
