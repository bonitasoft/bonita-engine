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

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.service.ServiceAccessor;
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
    private ServiceAccessor serviceAccessor;
    @Mock
    private SessionAccessor sessionAccessor;

    private InSessionBonitaWork txBonitaWork;

    @Before
    public void before() {
        txBonitaWork = spy(new InSessionBonitaWork(wrappedWork));

        when(serviceAccessor.getSessionAccessor()).thenReturn(sessionAccessor);
        doReturn(serviceAccessor).when(txBonitaWork).getServiceAccessor();
    }

    @Test
    public void testWork() throws Exception {
        final Map<String, Object> singletonMap = new HashMap<>();
        txBonitaWork.work(singletonMap);
        verify(wrappedWork, times(1)).work(singletonMap);
    }

    @Test
    public void testWorkFailureIsHandled() throws Throwable {
        final Map<String, Object> singletonMap = new HashMap<>();
        final Exception e = new Exception();
        txBonitaWork.handleFailure(e, singletonMap);
        verify(wrappedWork).handleFailure(e, singletonMap);
    }

    @Test(expected = Exception.class)
    public void testFailureHandlingFail() throws Throwable {
        final Map<String, Object> singletonMap = new HashMap<>();
        final Exception e1 = new Exception();
        final Exception e2 = new Exception();
        doThrow(e2).when(wrappedWork).handleFailure(e1, singletonMap);
        txBonitaWork.handleFailure(e1, singletonMap);
        verify(wrappedWork).handleFailure(e1, singletonMap);
    }

    @Test
    public void putInMap() throws Exception {
        final Map<String, Object> singletonMap = new HashMap<>();
        txBonitaWork.work(singletonMap);
        assertEquals(serviceAccessor, singletonMap.get("serviceAccessor"));
    }

    @Test
    public void getDescription() {
        when(wrappedWork.getDescription()).thenReturn("The description");
        assertEquals("The description", txBonitaWork.getDescription());
    }

    @Test
    public void getRecoveryProcedure() {
        when(wrappedWork.getRecoveryProcedure()).thenReturn("recoveryProcedure");
        assertEquals("recoveryProcedure", txBonitaWork.getRecoveryProcedure());
    }

    @Test
    public void handleFailure() throws Throwable {
        final Map<String, Object> context = Map.of("serviceAccessor", serviceAccessor);
        final SBonitaException e = new SBonitaException() {

            private static final long serialVersionUID = -6748168976371554636L;
        };
        txBonitaWork.handleFailure(e, context);
        verify(wrappedWork).handleFailure(e, context);
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

    @Test(expected = Exception.class)
    public void should_throw_the_exception_of_wrapped_work() throws Throwable {
        final Map<String, Object> context = new HashMap<>();
        final Exception e = new Exception();
        doThrow(e).when(wrappedWork).work(context);
        txBonitaWork.work(context);
    }

}
