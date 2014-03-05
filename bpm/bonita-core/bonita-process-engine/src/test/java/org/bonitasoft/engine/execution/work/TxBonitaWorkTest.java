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
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.BonitaWork;
import org.junit.Before;
import org.junit.Test;

public class TxBonitaWorkTest {

    private final BonitaWork wrappedWork = mock(BonitaWork.class);

    private TxBonitaWork txBonitawork;

    private TenantServiceAccessor tenantAccessor;

    private UserTransactionService userTransactionService;

    @Before
    public void before() {
        txBonitawork = new TxBonitaWork(wrappedWork);
        tenantAccessor = mock(TenantServiceAccessor.class);
        userTransactionService = mock(UserTransactionService.class);
        when(tenantAccessor.getUserTransactionService()).thenReturn(userTransactionService);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testWork() throws Exception {
        Map<String, Object> singletonMap = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        txBonitawork.work(singletonMap);
        verify(userTransactionService, times(1)).executeInTransaction(any(Callable.class));
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
        Map<String, Object> context = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        Exception e = new Exception();
        txBonitawork.handleFailure(e, context);
        verify(wrappedWork).handleFailure(e, context);
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
