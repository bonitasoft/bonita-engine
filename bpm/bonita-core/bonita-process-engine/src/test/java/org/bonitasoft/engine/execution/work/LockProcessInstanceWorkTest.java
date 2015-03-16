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
package org.bonitasoft.engine.execution.work;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.BonitaWork;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class LockProcessInstanceWorkTest {

    private static final String PROCESS = "PROCESS";

    private final long processInstanceId = 123l;

    private final BonitaWork wrappedWork = mock(BonitaWork.class);

    private LockProcessInstanceWork lockProcessInstanceWork;

    private TenantServiceAccessor tenantAccessor;

    private LockService lockService;

    private WorkService workService;

    private TechnicalLoggerService loggerService;

    private static final long TENANT_ID = 1;

    @Before
    public void before() {
        lockProcessInstanceWork = new LockProcessInstanceWork(wrappedWork, processInstanceId);
        when(wrappedWork.getTenantId()).thenReturn(TENANT_ID);
        tenantAccessor = mock(TenantServiceAccessor.class);
        lockService = mock(LockService.class);
        workService = mock(WorkService.class);
        loggerService = mock(TechnicalLoggerService.class);
        when(tenantAccessor.getLockService()).thenReturn(lockService);
        when(tenantAccessor.getWorkService()).thenReturn(workService);
        when(tenantAccessor.getTechnicalLoggerService()).thenReturn(loggerService);
    }

    @Test
    public void testWork() throws Exception {
        BonitaLock bonitaLock = new BonitaLock(new ReentrantLock(), PROCESS, processInstanceId);
        when(lockService.tryLock(eq(processInstanceId), eq(PROCESS), eq(20L), eq(TimeUnit.MILLISECONDS), eq(TENANT_ID))).thenReturn(
                bonitaLock);
        Map<String, Object> singletonMap = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        lockProcessInstanceWork.work(singletonMap);
        verify(lockService, times(1)).tryLock(eq(processInstanceId), eq(PROCESS), eq(20L), eq(TimeUnit.MILLISECONDS), eq(TENANT_ID));
        verify(lockService, times(1)).unlock(bonitaLock, TENANT_ID);
        verify(wrappedWork, times(1)).work(singletonMap);
    }

    @Test
    public void workDidNotLock() throws Exception {
        when(lockService.tryLock(eq(processInstanceId), eq(PROCESS), eq(20L), eq(TimeUnit.MILLISECONDS), eq(TENANT_ID))).thenReturn(null);
        Map<String, Object> singletonMap = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        lockProcessInstanceWork.work(singletonMap);
        verify(lockService, times(1)).tryLock(eq(processInstanceId), eq(PROCESS), eq(20L), eq(TimeUnit.MILLISECONDS), eq(TENANT_ID));
        verify(wrappedWork, times(0)).work(singletonMap);
    }

    @Test
    public void getDescription() {
        when(wrappedWork.getDescription()).thenReturn("The description");
        assertEquals("The description", lockProcessInstanceWork.getDescription());
    }

    @Test
    public void getRecoveryProcedure() {
        when(wrappedWork.getRecoveryProcedure()).thenReturn("recoveryProcedure");
        assertEquals("recoveryProcedure", lockProcessInstanceWork.getRecoveryProcedure());
    }

    @Test
    public void handleFailure() throws Throwable {
        Map<String, Object> context = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        Exception e = new Exception();
        lockProcessInstanceWork.handleFailure(e, context);
        verify(wrappedWork).handleFailure(e, context);
    }

    @Test
    public void getTenantId() {
        when(wrappedWork.getTenantId()).thenReturn(12l);
        assertEquals(12, lockProcessInstanceWork.getTenantId());
    }

    @Test
    public void setTenantId() {
        lockProcessInstanceWork.setTenantId(12l);
        verify(wrappedWork).setTenantId(12l);
    }

    @Test
    public void getWrappedWork() {
        assertEquals(wrappedWork, lockProcessInstanceWork.getWrappedWork());
    }

    @Test
    public void testToString() {
        when(wrappedWork.toString()).thenReturn("the to string");
        assertEquals("the to string", lockProcessInstanceWork.toString());
    }

    @Test
    public void testRescheduleWorkOnLockTimeout() throws Exception {
        // On first try to lock : exception to reschedule the work
        // On the second try : return a correct lock
        when(lockService.tryLock(eq(processInstanceId), eq(PROCESS), eq(20L), eq(TimeUnit.MILLISECONDS), eq(TENANT_ID))).thenReturn(null);
        Map<String, Object> context = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);

        LockProcessInstanceWork spiedWork = Mockito.spy(lockProcessInstanceWork);
        spiedWork.work(context);

        // in fact we should ensure the rootWork of the lockProcessInstanceWork has been re-executed. We should check the method getRootWork has been called
        verify(lockService).tryLock(eq(processInstanceId), eq(PROCESS), eq(20L), eq(TimeUnit.MILLISECONDS), eq(TENANT_ID));
        verify(workService).executeWork(spiedWork);
        verify(spiedWork).getRootWork();
    }

}
