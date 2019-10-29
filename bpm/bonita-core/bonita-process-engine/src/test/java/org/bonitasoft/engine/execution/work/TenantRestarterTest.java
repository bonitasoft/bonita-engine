/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.execution.work;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.tenant.TenantRestarter;
import org.bonitasoft.engine.tenant.restart.TenantRestartHandler;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class TenantRestarterTest {

    @Mock
    private TenantRestartHandler tenantRestartHandler1;
    @Mock
    private TenantRestartHandler tenantRestartHandler2;
    @Mock
    private TransactionService transactionService;
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private SessionService sessionService;
    @Mock
    private PlatformService platformService;
    private TenantRestarter tenantRestarter;

    @Before
    public void before() throws Exception {
        tenantRestarter = new TenantRestarter(1L, transactionService, sessionAccessor, sessionService, platformService,
                asList(tenantRestartHandler1, tenantRestartHandler2));
    }

    @Test
    public void should_execute_beforeServicesStart_on_handler_when_in_transaction() throws Exception {
        doReturn(true).when(transactionService).isTransactionActive();

        tenantRestarter.executeBeforeServicesStart();

        verify(tenantRestartHandler1).beforeServicesStart();
        verify(tenantRestartHandler2).beforeServicesStart();
    }

    @Test
    public void should_return_handlers_when_beforeServicesStart_is_executed_when_in_transaction() throws Exception {
        doReturn(true).when(transactionService).isTransactionActive();

        List<TenantRestartHandler> tenantRestartHandlers = tenantRestarter.executeBeforeServicesStart();

        assertThat(tenantRestartHandlers).containsOnly(tenantRestartHandler1, tenantRestartHandler2);
    }

    @Test
    public void should_execute_beforeServicesStart_on_handler_when_outside_of_transaction() throws Exception {
        doReturn(false).when(transactionService).isTransactionActive();

        tenantRestarter.executeBeforeServicesStart();

        verify(transactionService).executeInTransaction(any());
    }

}
