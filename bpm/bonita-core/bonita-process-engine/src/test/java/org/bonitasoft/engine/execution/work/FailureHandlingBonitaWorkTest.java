package org.bonitasoft.engine.execution.work;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.core.process.definition.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.execution.SIllegalStateTransition;
import org.bonitasoft.engine.incident.Incident;
import org.bonitasoft.engine.incident.IncidentService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
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
        final Map<String, Object> singletonMap = new HashMap<String, Object>();
        txBonitawork.work(singletonMap);
        verify(wrappedWork, times(1)).work(singletonMap);
    }

    @Test
    public void testWorkFailureIsHandled() throws Exception {
        final Map<String, Object> singletonMap = new HashMap<String, Object>();
        final Exception e = new Exception();
        doThrow(e).when(wrappedWork).work(singletonMap);
        txBonitawork.work(singletonMap);
        verify(wrappedWork, times(1)).work(singletonMap);
        verify(wrappedWork, times(1)).handleFailure(e, singletonMap);
    }

    @Test
    public void testFailureHandlingFail() throws Exception {
        final Map<String, Object> singletonMap = new HashMap<String, Object>();
        final Exception e1 = new Exception();
        final Exception e2 = new Exception();
        doThrow(e1).when(wrappedWork).work(singletonMap);
        doThrow(e2).when(wrappedWork).handleFailure(e1, singletonMap);
        txBonitawork.work(singletonMap);
        verify(wrappedWork, times(1)).work(singletonMap);
        verify(wrappedWork, times(1)).handleFailure(e1, singletonMap);
        verify(incidentService, times(1)).report(eq(0l), any(Incident.class));
    }

    @Test
    public void putInMap() {
        final Map<String, Object> singletonMap = new HashMap<String, Object>();
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
    public void handleFailure() throws Exception {
        final Map<String, Object> context = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        final Exception e = new Exception();
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

    @Test
    public void doNotHandleFailureWhenGettingASFlowNodeNotFoundException() throws Exception {
        final Map<String, Object> context = new HashMap<String, Object>();
        final Exception e = new Exception(new SFlowNodeNotFoundException(83));
        doThrow(e).when(wrappedWork).work(context);
        txBonitawork.work(context);
        verify(wrappedWork, never()).handleFailure(e, context);
        verify(loggerService).isLoggable(FailureHandlingBonitaWorkExtended.class, TechnicalLogSeverity.DEBUG);
    }

    @Test
    public void doNotHandleFailureWhenGettingASFlowNodeNotFoundExceptionAsMainCause() throws Exception {
        final Map<String, Object> context = new HashMap<String, Object>();
        final Exception flownodeNotFound = new SFlowNodeNotFoundException(11547L);
        doThrow(flownodeNotFound).when(wrappedWork).work(context);
        txBonitawork.work(context);
        verify(wrappedWork, never()).handleFailure(flownodeNotFound, context);
        verify(loggerService).isLoggable(FailureHandlingBonitaWorkExtended.class, TechnicalLogSeverity.DEBUG);
    }

    @Test
    public void doNotHandleFailureWhenGettingASProcessInstanceNotFoundException() throws Exception {
        final Map<String, Object> context = new HashMap<String, Object>();
        final Exception e = new Exception(new SProcessInstanceNotFoundException(83));
        doThrow(e).when(wrappedWork).work(context);
        when(wrappedWork.getDescription()).thenReturn("");
        txBonitawork.work(context);
        verify(wrappedWork, never()).handleFailure(e, context);
        verify(loggerService).isLoggable(FailureHandlingBonitaWorkExtended.class, TechnicalLogSeverity.DEBUG);
    }

    @Test
    public void doNotHandleFailureWhenGettingASProcessDefinitionNotFoundException() throws Exception {
        final Map<String, Object> context = new HashMap<String, Object>();
        final Exception e = new Exception(new SProcessDefinitionNotFoundException("message"));
        doThrow(e).when(wrappedWork).work(context);
        txBonitawork.work(context);
        verify(wrappedWork, never()).handleFailure(e, context);
        verify(loggerService).isLoggable(FailureHandlingBonitaWorkExtended.class, TechnicalLogSeverity.DEBUG);
    }

    @Test
    public void doNotHandleFailureWhenIsIllegalTransitionFromTerminalState() throws Exception {
        final Map<String, Object> context = new HashMap<String, Object>();
        final Exception e = new Exception(new SIllegalStateTransition("message", true));
        doThrow(e).when(wrappedWork).work(context);
        txBonitawork.work(context);
        verify(wrappedWork, never()).handleFailure(e, context);
        verify(loggerService).isLoggable(FailureHandlingBonitaWorkExtended.class, TechnicalLogSeverity.DEBUG);
    }

    @Test
    public void handleFailureWhenIsIllegalTransitionFromNonTerminalState() throws Exception {
        final Map<String, Object> context = new HashMap<String, Object>();
        final Exception e = new Exception(new SIllegalStateTransition("message", false));
        doThrow(e).when(wrappedWork).work(context);
        txBonitawork.work(context);
        verify(wrappedWork, times(1)).handleFailure(e, context);
    }

    @Test
    public void handleFailureForAllOtherExceptions() throws Exception {
        final Map<String, Object> context = new HashMap<String, Object>();
        final Exception e = new Exception();
        doThrow(e).when(wrappedWork).work(context);
        txBonitawork.work(context);
        verify(wrappedWork, times(1)).handleFailure(e, context);
    }

}
