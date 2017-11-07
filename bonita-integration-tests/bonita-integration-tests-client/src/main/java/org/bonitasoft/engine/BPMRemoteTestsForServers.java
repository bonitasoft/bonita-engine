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

import org.bonitasoft.engine.activity.ContractIT;
import org.bonitasoft.engine.activity.PendingTasksIT;
import org.bonitasoft.engine.business.application.ApplicationIT;
import org.bonitasoft.engine.business.data.BDRepositoryIT;
import org.bonitasoft.engine.command.AdvancedStartProcessCommandIT;
import org.bonitasoft.engine.command.CommandIT;
import org.bonitasoft.engine.command.ExecuteBDMQueryCommandIT;
import org.bonitasoft.engine.command.MultipleStartPointsProcessCommandIT;
import org.bonitasoft.engine.command.web.ExternalCommandsTests;
import org.bonitasoft.engine.event.SignalEventIT;
import org.bonitasoft.engine.identity.UserIT;
import org.bonitasoft.engine.login.LoginAPIIT;
import org.bonitasoft.engine.login.PlatformLoginAPIIT;
import org.bonitasoft.engine.operation.OperationIT;
import org.bonitasoft.engine.page.PageAPIIT;
import org.bonitasoft.engine.platform.PlatformLoginIT;
import org.bonitasoft.engine.platform.command.PlatformCommandIT;
import org.bonitasoft.engine.process.ProcessManagementIT;
import org.bonitasoft.engine.process.actor.ImportActorMappingIT;
import org.bonitasoft.engine.profile.ProfileIT;
import org.bonitasoft.engine.search.SearchProcessInstanceIT;
import org.bonitasoft.engine.supervisor.ProcessSupervisedIT;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ContractIT.class,
        PlatformLoginIT.class,
        RemoteEngineIT.class, // this class is only in remote (we test that server stack trace are reported in client side)
        ImportActorMappingIT.class,
        PlatformCommandIT.class,
        ProcessManagementIT.class,
        ProfileIT.class,
        SearchProcessInstanceIT.class,
        PendingTasksIT.class,
        SignalEventIT.class,
        UserIT.class,
        LoginAPIIT.class,
        PlatformLoginAPIIT.class,
        CommandIT.class,
        ExternalCommandsTests.class,
        ProcessSupervisedIT.class,
        OperationIT.class,
        MultiThreadCallsIT.class,
        AdvancedStartProcessCommandIT.class,
        MultipleStartPointsProcessCommandIT.class,
        PageAPIIT.class,
        ApplicationIT.class,
        BDRepositoryIT.class,
        ExecuteBDMQueryCommandIT.class
})
public class BPMRemoteTestsForServers {

}
