package org.bonitasoft.engine.execution.work;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.incident.Incident;
import org.bonitasoft.engine.incident.IncidentService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.work.BonitaWork;
import org.junit.Before;
import org.junit.Test;

public class FailureHandlingBonitaWorkTest {

    private final BonitaWork wrappedWork = mock(BonitaWork.class);

    private FailureHandlingBonitaWork txBonitawork;

    private TenantServiceAccessor tenantAccessor;

    private SessionService sessionService;

    private IncidentService incidentService;

    private TechnicalLoggerService loggerService;

    private SessionAccessor sessionAccessor;

    @Before
    public void before() {
        tenantAccessor = mock(TenantServiceAccessor.class);
        txBonitawork = new FailureHandlingBonitaWorkExtended(wrappedWork, tenantAccessor);
        sessionService = mock(SessionService.class);
        incidentService = mock(IncidentService.class);
        loggerService = mock(TechnicalLoggerService.class);
        sessionAccessor = mock(SessionAccessor.class);
        when(tenantAccessor.getTechnicalLoggerService()).thenReturn(loggerService);
        when(tenantAccessor.getSessionAccessor()).thenReturn(sessionAccessor);
        when(tenantAccessor.getSessionService()).thenReturn(sessionService);
        when(tenantAccessor.getIncidentService()).thenReturn(incidentService);
    }

    @Test
    public void testWork() throws Exception {
        Map<String, Object> singletonMap = new HashMap<String, Object>();
        txBonitawork.work(singletonMap);
        verify(wrappedWork, times(1)).work(singletonMap);
    }

    @Test
    public void testWorkFailureIsHandled() throws Exception {
        Map<String, Object> singletonMap = new HashMap<String, Object>();
        Exception e = new Exception();
        doThrow(e).when(wrappedWork).work(singletonMap);
        txBonitawork.work(singletonMap);
        verify(wrappedWork, times(1)).work(singletonMap);
        verify(wrappedWork, times(1)).handleFailure(e, singletonMap);
    }

    @Test
    public void testFailureHandlingFail() throws Exception {
        Map<String, Object> singletonMap = new HashMap<String, Object>();
        Exception e1 = new Exception();
        Exception e2 = new Exception();
        doThrow(e1).when(wrappedWork).work(singletonMap);
        doThrow(e2).when(wrappedWork).handleFailure(e1, singletonMap);
        txBonitawork.work(singletonMap);
        verify(wrappedWork, times(1)).work(singletonMap);
        verify(wrappedWork, times(1)).handleFailure(e1, singletonMap);
        verify(incidentService, times(1)).report(any(Incident.class));
    }

    @Test
    public void testPutInMap() throws Exception {
        Map<String, Object> singletonMap = new HashMap<String, Object>();
        txBonitawork.work(singletonMap);
        assertEquals(tenantAccessor, singletonMap.get("tenantAccessor"));
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
