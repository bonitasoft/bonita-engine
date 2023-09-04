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
package org.bonitasoft.engine.execution.work.failurewrapping;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.execution.work.TransactionServiceForTest;
import org.bonitasoft.engine.service.ServiceAccessor;
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
    protected ServiceAccessor serviceAccessor;

    @Spy
    private TransactionServiceForTest transactionService;

    protected TxInHandleFailureWrappingWork txBonitaWork;

    @Before
    public void before() throws SBonitaException {
        doReturn(transactionService).when(serviceAccessor).getUserTransactionService();

        doReturn("The description").when(txBonitaWork).getDescription();

    }

    @Test
    public void work() throws Exception {
        final Map<String, Object> singletonMap = new HashMap<>();
        txBonitaWork.work(singletonMap);
        verify(wrappedWork, times(1)).work(singletonMap);
    }

    @Test
    public void getDescription() {
        assertEquals("The description", txBonitaWork.getDescription());
    }

    @Test
    public void getRecoveryProcedure() {
        when(wrappedWork.getRecoveryProcedure()).thenReturn("recoveryProcedure");
        assertEquals("recoveryProcedure", txBonitaWork.getRecoveryProcedure());
    }

    @Test
    public void getTenantId() {
        when(wrappedWork.getTenantId()).thenReturn(12L);
        assertEquals(12, txBonitaWork.getTenantId());
    }

    @Test
    public void setTenantId() {
        txBonitaWork.setTenantId(12L);
        verify(wrappedWork).setTenantId(12L);
    }

    @Test
    public void getWrappedWork() {
        assertEquals(wrappedWork, txBonitaWork.getWrappedWork());
    }

    @Test
    public void testToString() {
        when(wrappedWork.toString()).thenReturn("the to string");
        assertEquals("the to string", txBonitaWork.toString());
    }

}
