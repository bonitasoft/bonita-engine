/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import org.bonitasoft.engine.event.InterruptingTimerBoundaryEventIT;
import org.bonitasoft.engine.event.MessageBoundaryEventIT;
import org.bonitasoft.engine.event.MessageEventIT;
import org.bonitasoft.engine.event.MessageEventSubProcessIT;
import org.bonitasoft.engine.event.NonInterruptingTimerBoundaryEventIT;
import org.bonitasoft.engine.event.TimerBoundaryEventIT;
import org.bonitasoft.engine.event.TimerEventIT;
import org.bonitasoft.engine.event.TimerEventSubProcessIT;
import org.bonitasoft.engine.platform.PlatformIT;
import org.bonitasoft.engine.platform.PlatformLoginIT;
import org.bonitasoft.engine.tenant.TenantMaintenanceIT;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuiteClasses({
        // Specific slow test suites below:
        NonInterruptingTimerBoundaryEventIT.class,
        InterruptingTimerBoundaryEventIT.class,
        TimerBoundaryEventIT.class,
        TimerEventIT.class,
        MessageEventIT.class,
        MessageBoundaryEventIT.class,
        TimerEventSubProcessIT.class,
        MessageEventSubProcessIT.class,
        PlatformLoginIT.class,
        PlatformIT.class,
        TenantMaintenanceIT.class,

        // Same suites as in LocalIntegrationTests below:
        LocalIntegrationTests.class,
})
@RunWith(Suite.class)
public class SlowExecutionLocalIntegrationTests {

    @BeforeClass
    public static void beforeClass() {
        System.err.println("=================== Slow-execution Local Integration Test Suite");
    }

    @AfterClass
    public static void afterClass() {
        System.err.println("=================== Slow-execution Local Integration Test Suite clean-up");
    }

}
