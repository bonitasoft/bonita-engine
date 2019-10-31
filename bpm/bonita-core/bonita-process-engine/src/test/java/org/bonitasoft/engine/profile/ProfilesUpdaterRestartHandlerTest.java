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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.execution.TransactionServiceMock;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProfilesUpdaterRestartHandlerTest {

    @Mock
    public PlatformServiceAccessor platformServiceAccessor;
    @Mock
    public TenantServiceAccessor tenantServiceAccessor;
    @Mock
    private DefaultProfilesUpdater defaultProfilesUpdater;
    @Spy
    private ProfilesUpdaterRestartHandler profilesUpdaterRestartHandler;

    @Test
    public void should_execute_default_profiles_update_after_service_start() throws Exception {
        doReturn(new TransactionServiceMock()).when(platformServiceAccessor).getTransactionService();
        doReturn(defaultProfilesUpdater).when(profilesUpdaterRestartHandler).getProfileUpdater(platformServiceAccessor, tenantServiceAccessor);

        profilesUpdaterRestartHandler.afterServicesStart(platformServiceAccessor, tenantServiceAccessor);

        verify(defaultProfilesUpdater).execute();
    }

    @Test
    public void should_execute_in_transaction() throws Exception {
        TransactionService transactionService = mock(TransactionService.class);
        doReturn(transactionService).when(platformServiceAccessor).getTransactionService();

        profilesUpdaterRestartHandler.afterServicesStart(platformServiceAccessor, tenantServiceAccessor);

        verify(transactionService).executeInTransaction(any());
    }

}
