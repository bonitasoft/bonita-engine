package org.bonitasoft.engine.execution.work;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.RejectedLockHandler;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.BonitaWork;
import org.junit.Before;
import org.junit.Test;

public class LockProcessInstanceWorkTest {

    private static final String PROCESS = "PROCESS";

    private final long processInstanceId = 123l;

    private final BonitaWork wrappedWork = mock(BonitaWork.class);

    private LockProcessInstanceWork lockProcessInstanceWork;

    private TenantServiceAccessor tenantAccessor;

    private LockService lockService;

    @Before
    public void before() {
        lockProcessInstanceWork = new LockProcessInstanceWork(wrappedWork, processInstanceId);
        tenantAccessor = mock(TenantServiceAccessor.class);
        lockService = mock(LockService.class);
        when(tenantAccessor.getLockService()).thenReturn(lockService);
    }

    @Test
    public void testWork() throws Exception {
        BonitaLock bonitaLock = new BonitaLock(new ReentrantLock(), PROCESS, processInstanceId);
        when(lockService.tryLock(eq(processInstanceId), eq(PROCESS), any(RejectedLockHandler.class))).thenReturn(
                bonitaLock);
        Map<String, Object> singletonMap = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        lockProcessInstanceWork.work(singletonMap);
        verify(lockService, times(1)).tryLock(eq(processInstanceId), eq(PROCESS), any(RejectedLockHandler.class));
        verify(lockService, times(1)).unlock(bonitaLock);
        verify(wrappedWork, times(1)).work(singletonMap);
    }

    @Test
    public void testWorkDidNotLock() throws Exception {
        Map<String, Object> singletonMap = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        lockProcessInstanceWork.work(singletonMap);
        verify(lockService, times(1)).tryLock(eq(processInstanceId), eq(PROCESS), any(RejectedLockHandler.class));
        verify(wrappedWork, times(0)).work(singletonMap);
    }

    @Test
    public void testGetDescription() throws Exception {
        when(wrappedWork.getDescription()).thenReturn("The description");
        assertEquals("The description", lockProcessInstanceWork.getDescription());
    }

    @Test
    public void testGetRecoveryProcedure() throws Exception {
        when(wrappedWork.getRecoveryProcedure()).thenReturn("recoveryProcedure");
        assertEquals("recoveryProcedure", lockProcessInstanceWork.getRecoveryProcedure());
    }

    @Test
    public void testHandleFailure() throws Exception {
        Map<String, Object> context = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        Exception e = new Exception();
        lockProcessInstanceWork.handleFailure(e, context);
        verify(wrappedWork).handleFailure(e, context);
    }

    @Test
    public void testGetTenantId() throws Exception {
        when(wrappedWork.getTenantId()).thenReturn(12l);
        assertEquals(12, lockProcessInstanceWork.getTenantId());
    }

    @Test
    public void testSetTenantId() throws Exception {
        lockProcessInstanceWork.setTenantId(12l);
        verify(wrappedWork).setTenantId(12l);
    }

    @Test
    public void testGetWrappedWork() throws Exception {
        assertEquals(wrappedWork, lockProcessInstanceWork.getWrappedWork());
    }

    @Test
    public void testToString() throws Exception {
        when(wrappedWork.toString()).thenReturn("the to string");
        assertEquals("the to string", lockProcessInstanceWork.toString());
    }

}
