package org.bonitasoft.engine.execution.work;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.work.BonitaWork;
import org.junit.Before;
import org.junit.Test;

public class TxBonitaWorkTest {

    private final BonitaWork wrappedWork = mock(BonitaWork.class);

    private TxBonitaWork txBonitawork;

    private TenantServiceAccessor tenantAccessor;

    private TransactionService transactionService;

    @Before
    public void before() {
        txBonitawork = new TxBonitaWork(wrappedWork);
        tenantAccessor = mock(TenantServiceAccessor.class);
        transactionService = mock(TransactionService.class);
        when(tenantAccessor.getTransactionService()).thenReturn(transactionService);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testWork() throws Exception {
        Map<String, Object> singletonMap = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        txBonitawork.work(singletonMap);
        verify(transactionService, times(1)).executeInTransaction(any(Callable.class));
    }

    @Test
    public void testGetDescription() throws Exception {
        when(wrappedWork.getDescription()).thenReturn("The description");
        assertEquals("The description", txBonitawork.getDescription());
    }

    @Test
    public void testGetRecoveryProcedure() throws Exception {
        when(wrappedWork.getRecoveryProcedure()).thenReturn("recoveryProcedure");
        assertEquals("recoveryProcedure", txBonitawork.getRecoveryProcedure());
    }

    @Test
    public void testHandleFailure() throws Exception {
        Map<String, Object> context = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        Exception e = new Exception();
        txBonitawork.handleFailure(e, context);
        verify(wrappedWork).handleFailure(e, context);
    }

    @Test
    public void testGetTenantId() throws Exception {
        when(wrappedWork.getTenantId()).thenReturn(12l);
        assertEquals(12, txBonitawork.getTenantId());
    }

    @Test
    public void testSetTenantId() throws Exception {
        txBonitawork.setTenantId(12l);
        verify(wrappedWork).setTenantId(12l);
    }

    @Test
    public void testGetWrappedWork() throws Exception {
        assertEquals(wrappedWork, txBonitawork.getWrappedWork());
    }

    @Test
    public void testToString() throws Exception {
        when(wrappedWork.toString()).thenReturn("the to string");
        assertEquals("the to string", txBonitawork.toString());
    }

}
