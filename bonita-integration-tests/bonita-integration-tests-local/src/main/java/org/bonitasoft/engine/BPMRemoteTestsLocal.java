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

import org.bonitasoft.engine.accessors.TenantAccessorTest;
import org.bonitasoft.engine.activity.TaskTests;
import org.bonitasoft.engine.activity.UserTaskContractITest;
import org.bonitasoft.engine.business.data.BDRepositoryIT;
import org.bonitasoft.engine.command.CommandsTests;
import org.bonitasoft.engine.command.ExecuteBDMQueryCommandIT;
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
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

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
        UserTaskContractITest.class,
        BDRepositoryIT.class,
        ExecuteBDMQueryCommandIT.class
})
@RunWith(BonitaSuiteRunner.class)
@BonitaSuiteRunner.Initializer(LocalServerTestsInitializer.class)
public class BPMRemoteTestsLocal {

}
