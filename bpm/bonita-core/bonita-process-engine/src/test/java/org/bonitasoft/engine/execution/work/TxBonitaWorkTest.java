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
package org.bonitasoft.engine.execution.work;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.BonitaWork;
import org.junit.Before;
import org.junit.Test;

public class TxBonitaWorkTest {

    private final BonitaWork wrappedWork = mock(BonitaWork.class);

    private TxBonitaWork txBonitawork;

    private ServiceAccessor serviceAccessor;

    private UserTransactionService userTransactionService;

    @Before
    public void before() {
        txBonitawork = new TxBonitaWork(wrappedWork);
        serviceAccessor = mock(ServiceAccessor.class);
        userTransactionService = mock(UserTransactionService.class);
        when(serviceAccessor.getUserTransactionService()).thenReturn(userTransactionService);
    }

    @Test
    public void testWork() throws Exception {
        Map<String, Object> singletonMap = Collections.singletonMap("serviceAccessor", serviceAccessor);
        txBonitawork.work(singletonMap);
        verify(userTransactionService, times(1)).executeInTransaction(any());
    }

    @Test
    public void getDescription() {
        when(wrappedWork.getDescription()).thenReturn("The description");
        assertEquals("The description", txBonitawork.getDescription());
    }

    @Test
    public void getRecoveryProcedure() {
        when(wrappedWork.getRecoveryProcedure()).thenReturn("recoveryProcedure");
        assertEquals("recoveryProcedure", txBonitawork.getRecoveryProcedure());
    }

    @Test
    public void handleFailure() throws Throwable {
        Map<String, Object> context = Collections.singletonMap("serviceAccessor", serviceAccessor);
        Exception e = new Exception();
        txBonitawork.handleFailure(e, context);
        verify(wrappedWork).handleFailure(e, context);
    }

    @Test
    public void getTenantId() {
        when(wrappedWork.getTenantId()).thenReturn(12L);
        assertEquals(12, txBonitawork.getTenantId());
    }

    @Test
    public void setTenantId() {
        txBonitawork.setTenantId(12L);
        verify(wrappedWork).setTenantId(12L);
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
