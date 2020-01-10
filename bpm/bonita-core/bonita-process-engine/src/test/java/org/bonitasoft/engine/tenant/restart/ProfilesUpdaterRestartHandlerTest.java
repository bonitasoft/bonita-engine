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
package org.bonitasoft.engine.tenant.restart;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.log.technical.TechnicalLoggerSLF4JImpl;
import org.bonitasoft.engine.profile.DefaultProfilesUpdater;
import org.bonitasoft.engine.profile.ProfilesImporter;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProfilesUpdaterRestartHandlerTest {

    @Mock
    public TenantServiceAccessor tenantServiceAccessor;
    @Mock
    private DefaultProfilesUpdater defaultProfilesUpdater;
    @Mock
    private ProfilesImporter profilesImporter;
    @Mock
    private TransactionService transactionService;
    private ProfilesUpdaterRestartHandler profilesUpdaterRestartHandler;

    @Before
    public void before() throws Exception {
        doAnswer(invocation -> ((Callable) invocation.getArgument(0)).call()).when(transactionService)
                .executeInTransaction(any());
    }

    @Test
    public void should_execute_default_profiles_update_after_service_start() throws Exception {
        profilesUpdaterRestartHandler = spy(new ProfilesUpdaterRestartHandler(1L, new TechnicalLoggerSLF4JImpl(),
                profilesImporter, transactionService));
        doReturn(defaultProfilesUpdater).when(profilesUpdaterRestartHandler).getDefaultProfilesUpdater();

        profilesUpdaterRestartHandler.afterServicesStart();

        verify(transactionService).executeInTransaction(any());
        verify(defaultProfilesUpdater).execute();
    }

}
