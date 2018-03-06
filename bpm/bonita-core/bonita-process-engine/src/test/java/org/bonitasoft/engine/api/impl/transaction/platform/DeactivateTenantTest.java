/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.engine.api.impl.transaction.platform;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class DeactivateTenantTest {

    private final long tenantId = 17L;
    @Mock
    private PlatformService platformService;
    @Mock
    private SchedulerService schedulerService;
    @Mock
    private WorkService workService;

    private DeactivateTenant deactivateTenant;

    @Before
    public void setup() {
        deactivateTenant = new DeactivateTenant(tenantId, platformService, schedulerService);
    }

    @Test
    public void should_pause_jobs_of_the_tenant() throws Exception {
        doReturn(true).when(schedulerService).isStarted();

        deactivateTenant.execute();

        verify(schedulerService).pauseJobs(tenantId);
    }

    @Test
    public void should_not_delete_jobs_other_than_default_jobs() throws Exception {
        doReturn(true).when(schedulerService).isStarted();

        deactivateTenant.execute();

        verify(schedulerService).delete(anyString());
        verify(schedulerService).delete(ActivateTenant.CLEAN_INVALID_SESSIONS);
    }
}
