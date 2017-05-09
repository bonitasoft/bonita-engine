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
package org.bonitasoft.engine.execution.work.failurewrapping;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.execution.work.TransactionServiceForTest;
import org.bonitasoft.engine.incident.IncidentService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

/**
 * @author Aurelien Pupier
 */
public abstract class AbstractContextWorkTest {

    @Mock
    protected TxInHandleFailureWrappingWork wrappedWork;

    @Mock
    protected TenantServiceAccessor tenantAccessor;

    @Mock
    private SessionService sessionService;

    @Mock
    private IncidentService incidentService;

    @Mock
    private TechnicalLoggerService loggerService;

    @Mock
    private SessionAccessor sessionAccessor;

    @Spy
    private TransactionServiceForTest transactionService;

    protected TxInHandleFailureWrappingWork txBonitawork;

    @Before
    public void before() throws SBonitaException {
        doReturn(transactionService).when(tenantAccessor).getUserTransactionService();

        doReturn("The description").when(txBonitawork).getDescription();

    }

    @Test
    public void work() throws Exception {
        final Map<String, Object> singletonMap = new HashMap<String, Object>();
        txBonitawork.work(singletonMap);
        verify(wrappedWork, times(1)).work(singletonMap);
    }

    @Test
    public void getDescription() {
        assertEquals("The description", txBonitawork.getDescription());
    }

    @Test
    public void getRecoveryProcedure() {
        when(wrappedWork.getRecoveryProcedure()).thenReturn("recoveryProcedure");
        assertEquals("recoveryProcedure", txBonitawork.getRecoveryProcedure());
    }

    @Test
    public void getTenantId() {
        when(wrappedWork.getTenantId()).thenReturn(12l);
        assertEquals(12, txBonitawork.getTenantId());
    }

    @Test
    public void setTenantId() {
        txBonitawork.setTenantId(12l);
        verify(wrappedWork).setTenantId(12l);
    }

    @Test
    public void getWrappedWork() {
        assertEquals(wrappedWork, txBonitawork.getWrappedWork());
    }

    @Test
    public void testToString() {
        when(wrappedWork.toString()).thenReturn("the to string");
        assertEquals("the to string", txBonitawork.toString());
    }

}
