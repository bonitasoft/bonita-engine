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
package org.bonitasoft.engine.profile;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProfilesUpdaterRestartHandlerTest {

    @Mock
    public PlatformServiceAccessor platformServiceAccessor;
    @Mock
    public TenantServiceAccessor tenantServiceAccessor;
    @Mock
    public DefaultProfilesUpdater defaultProfilesUpdater;

    @Spy
    ProfilesUpdaterRestartHandler profilesUpdaterRestartHandler;

    @Test
    public void should_execute_default_profiles_update_after_service_start() throws Exception {
        doReturn(defaultProfilesUpdater).when(profilesUpdaterRestartHandler).getProfileUpdater(platformServiceAccessor, tenantServiceAccessor);

        profilesUpdaterRestartHandler.afterServicesStart(platformServiceAccessor, tenantServiceAccessor);

        verify(defaultProfilesUpdater).execute(true);
    }
}
