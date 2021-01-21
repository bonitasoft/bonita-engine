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
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.work.BonitaWork;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InSessionBonitaWorkTest {

    @Mock
    private BonitaWork wrappedWork;
    @Mock
    private TenantServiceAccessor tenantAccessor;
    @Mock
    private SessionAccessor sessionAccessor;

    private InSessionBonitaWork txBonitawork;

    @Before
    public void before() {
        txBonitawork = spy(new InSessionBonitaWork(wrappedWork));

        when(tenantAccessor.getSessionAccessor()).thenReturn(sessionAccessor);
        doReturn(tenantAccessor).when(txBonitawork).getTenantAccessor();
    }

    @Test
    public void testWork() throws Exception {
        final Map<String, Object> singletonMap = new HashMap<>();
        txBonitawork.work(singletonMap);
        verify(wrappedWork, times(1)).work(singletonMap);
    }

    @Test
    public void testWorkFailureIsHandled() throws Throwable {
        final Map<String, Object> singletonMap = new HashMap<>();
        final Exception e = new Exception();
        txBonitawork.handleFailure(e, singletonMap);
        verify(wrappedWork).handleFailure(e, singletonMap);
    }

    @Test(expected = Exception.class)
    public void testFailureHandlingFail() throws Throwable {
        final Map<String, Object> singletonMap = new HashMap<>();
        final Exception e1 = new Exception();
        final Exception e2 = new Exception();
        doThrow(e2).when(wrappedWork).handleFailure(e1, singletonMap);
        txBonitawork.handleFailure(e1, singletonMap);
        verify(wrappedWork).handleFailure(e1, singletonMap);
    }

    @Test
    public void putInMap() throws Exception {
        final Map<String, Object> singletonMap = new HashMap<>();
        txBonitawork.work(singletonMap);
        assertEquals(tenantAccessor, singletonMap.get("tenantAccessor"));
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
        final Map<String, Object> context = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        final SBonitaException e = new SBonitaException() {

            private static final long serialVersionUID = -6748168976371554636L;
        };
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

    @Test(expected = Exception.class)
    public void should_throw_the_exception_of_wrapped_work() throws Throwable {
        final Map<String, Object> context = new HashMap<>();
        final Exception e = new Exception();
        doThrow(e).when(wrappedWork).work(context);
        txBonitawork.work(context);
    }

}
