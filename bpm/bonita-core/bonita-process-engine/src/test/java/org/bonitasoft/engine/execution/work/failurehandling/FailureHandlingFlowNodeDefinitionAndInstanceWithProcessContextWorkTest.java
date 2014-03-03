/**
 * Copyright (C) 2014 Bonitasoft S.A.
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
package org.bonitasoft.engine.execution.work.failurehandling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.class)
public class FailureHandlingFlowNodeDefinitionAndInstanceWithProcessContextWorkTest {

    private static final long FLOW_NODE_DEFINITION_ID = 2;

    private static final long FLOW_NODE_INSTANCE_ID = 3;

    private static final String FLOW_NODE_NAME = "name";

    private static final long PROCESS_INSTANCE_ID = 2;

    private static final long ROOT_PROCESS_INSTANCE_ID = 3;

    private static final long PROCESS_DEFINITION_ID = 5;

    private static final String VERSION = "version";

    private static final String NAME = "name";

    @Mock
    private BonitaWork wrappedWork;

    @Mock
    private FailureHandlingBonitaWork txBonitawork;

    @Mock
    private TenantServiceAccessor tenantAccessor;

    @Mock
    private SessionService sessionService;

    @Mock
    private IncidentService incidentService;

    @Mock
    private TechnicalLoggerService loggerService;

    @Mock
    private ActivityInstanceService activityInstanceService;

    @Mock
    private SFlowNodeInstance flowNodeInstance;

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Mock
    private SProcessDefinitionDeployInfo sProcessDefinitionDeployInfo;

    @Before
    public void before() throws SBonitaException {
        doReturn(NAME).when(sProcessDefinitionDeployInfo).getName();
        doReturn(VERSION).when(sProcessDefinitionDeployInfo).getVersion();

        doReturn(sProcessDefinitionDeployInfo).when(processDefinitionService).getProcessDeploymentInfo(PROCESS_DEFINITION_ID);

        doReturn(processDefinitionService).when(tenantAccessor).getProcessDefinitionService();
        when(tenantAccessor.getTechnicalLoggerService()).thenReturn(loggerService);
        when(tenantAccessor.getSessionAccessor()).thenReturn(sessionAccessor);
        when(tenantAccessor.getSessionService()).thenReturn(sessionService);
        when(tenantAccessor.getIncidentService()).thenReturn(incidentService);
        when(tenantAccessor.getActivityInstanceService()).thenReturn(activityInstanceService);

        txBonitawork = spy(new FailureHandlingFlowNodeDefinitionAndInstanceWithProcessContextWork(wrappedWork, FLOW_NODE_INSTANCE_ID));
        doReturn("The description").when(txBonitawork).getDescription();
        doReturn(tenantAccessor).when(txBonitawork).getTenantAccessor();
        doReturn(false).when(loggerService).isLoggable(txBonitawork.getClass(), TechnicalLogSeverity.TRACE);
        doReturn(flowNodeInstance).when(activityInstanceService).getFlowNodeInstance(FLOW_NODE_INSTANCE_ID);
        doReturn(FLOW_NODE_DEFINITION_ID).when(flowNodeInstance).getFlowNodeDefinitionId();
        doReturn(FLOW_NODE_NAME).when(flowNodeInstance).getName();
        doReturn(PROCESS_INSTANCE_ID).when(flowNodeInstance).getParentProcessInstanceId();
        doReturn(ROOT_PROCESS_INSTANCE_ID).when(flowNodeInstance).getRootProcessInstanceId();
        doReturn(PROCESS_DEFINITION_ID).when(flowNodeInstance).getProcessDefinitionId();
    }

    @Test
    public void testWork() throws Exception {
        final Map<String, Object> singletonMap = new HashMap<String, Object>();
        txBonitawork.work(singletonMap);
        verify(wrappedWork, times(1)).work(singletonMap);
    }

    @Test
    public void testWorkFailureIsHandled() throws Throwable {
        final Map<String, Object> singletonMap = new HashMap<String, Object>();
        final Exception e = new Exception();
        doThrow(e).when(wrappedWork).work(singletonMap);
        txBonitawork.work(singletonMap);
        verify(wrappedWork, times(1)).work(singletonMap);
        verify(wrappedWork, times(1)).handleFailure(e, singletonMap);
    }

    @Test
    public void testFailureHandlingFail() throws Throwable {
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
    public void putInMap() throws SBonitaException {
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

    @Test
    public void doNotHandleFailureWhenGettingASFlowNodeNotFoundException() throws Throwable {
        final Map<String, Object> context = new HashMap<String, Object>();
        final SProcessDefinitionNotFoundException e = new SProcessDefinitionNotFoundException(new SFlowNodeNotFoundException(83));
        doThrow(e).when(wrappedWork).work(context);

        txBonitawork.work(context);

        assertTrue(e.getMessage().contains("FLOW_NODE_DEFINITION_ID = " + FLOW_NODE_DEFINITION_ID));
        assertTrue(e.getMessage().contains("FLOW_NODE_NAME = " + FLOW_NODE_NAME));
        assertTrue(e.getMessage().contains("FLOW_NODE_INSTANCE_ID = " + FLOW_NODE_INSTANCE_ID));
        assertTrue(e.getMessage().contains("PROCESS_INSTANCE_ID = " + PROCESS_INSTANCE_ID));
        assertTrue(e.getMessage().contains("ROOT_PROCESS_INSTANCE_ID = " + ROOT_PROCESS_INSTANCE_ID));
        assertTrue(e.getMessage().contains("PROCESS_DEFINITION_ID = " + PROCESS_DEFINITION_ID));
        assertTrue(e.getMessage().contains("PROCESS_NAME = " + NAME));
        assertTrue(e.getMessage().contains("PROCESS_VERSION = " + VERSION));
        verify(wrappedWork, never()).handleFailure(e, context);
        verify(loggerService).isLoggable(txBonitawork.getClass(), TechnicalLogSeverity.TRACE);
        verify(loggerService).isLoggable(txBonitawork.getClass(), TechnicalLogSeverity.DEBUG);
    }

    @Test
    public void doNotHandleFailureWhenGettingASProcessInstanceNotFoundException() throws Throwable {
        final Map<String, Object> context = new HashMap<String, Object>();
        final SProcessDefinitionNotFoundException e = new SProcessDefinitionNotFoundException(new SProcessInstanceNotFoundException(83));
        doThrow(e).when(wrappedWork).work(context);
        when(wrappedWork.getDescription()).thenReturn("");

        txBonitawork.work(context);

        assertTrue(e.getMessage().contains("FLOW_NODE_DEFINITION_ID = " + FLOW_NODE_DEFINITION_ID));
        assertTrue(e.getMessage().contains("FLOW_NODE_NAME = " + FLOW_NODE_NAME));
        assertTrue(e.getMessage().contains("FLOW_NODE_INSTANCE_ID = " + FLOW_NODE_INSTANCE_ID));
        assertTrue(e.getMessage().contains("PROCESS_INSTANCE_ID = " + PROCESS_INSTANCE_ID));
        assertTrue(e.getMessage().contains("ROOT_PROCESS_INSTANCE_ID = " + ROOT_PROCESS_INSTANCE_ID));
        assertTrue(e.getMessage().contains("PROCESS_DEFINITION_ID = " + PROCESS_DEFINITION_ID));
        assertTrue(e.getMessage().contains("PROCESS_NAME = " + NAME));
        assertTrue(e.getMessage().contains("PROCESS_VERSION = " + VERSION));
        verify(wrappedWork, never()).handleFailure(e, context);
        verify(loggerService).isLoggable(txBonitawork.getClass(), TechnicalLogSeverity.TRACE);
        verify(loggerService).isLoggable(txBonitawork.getClass(), TechnicalLogSeverity.DEBUG);
    }

    @Test
    public void doNotHandleFailureWhenGettingASProcessDefinitionNotFoundException() throws Throwable {
        final Map<String, Object> context = new HashMap<String, Object>();
        final SProcessDefinitionNotFoundException e = new SProcessDefinitionNotFoundException(new SProcessDefinitionNotFoundException("message"));
        doThrow(e).when(wrappedWork).work(context);

        txBonitawork.work(context);

        assertTrue(e.getMessage().contains("FLOW_NODE_DEFINITION_ID = " + FLOW_NODE_DEFINITION_ID));
        assertTrue(e.getMessage().contains("FLOW_NODE_NAME = " + FLOW_NODE_NAME));
        assertTrue(e.getMessage().contains("FLOW_NODE_INSTANCE_ID = " + FLOW_NODE_INSTANCE_ID));
        assertTrue(e.getMessage().contains("PROCESS_INSTANCE_ID = " + PROCESS_INSTANCE_ID));
        assertTrue(e.getMessage().contains("ROOT_PROCESS_INSTANCE_ID = " + ROOT_PROCESS_INSTANCE_ID));
        assertTrue(e.getMessage().contains("PROCESS_DEFINITION_ID = " + PROCESS_DEFINITION_ID));
        assertTrue(e.getMessage().contains("PROCESS_NAME = " + NAME));
        assertTrue(e.getMessage().contains("PROCESS_VERSION = " + VERSION));
        verify(wrappedWork, never()).handleFailure(e, context);
        verify(loggerService).isLoggable(txBonitawork.getClass(), TechnicalLogSeverity.TRACE);
        verify(loggerService).isLoggable(txBonitawork.getClass(), TechnicalLogSeverity.DEBUG);
    }

    @Test
    public void handleFailureForAllOtherExceptions() throws Throwable {
        final Map<String, Object> context = new HashMap<String, Object>();
        final SBonitaException e = new SBonitaException() {

            private static final long serialVersionUID = -6748168976371554636L;
        };
        doThrow(e).when(wrappedWork).work(context);

        txBonitawork.work(context);

        assertTrue(e.getMessage().contains("FLOW_NODE_DEFINITION_ID = " + FLOW_NODE_DEFINITION_ID));
        assertTrue(e.getMessage().contains("FLOW_NODE_NAME = " + FLOW_NODE_NAME));
        assertTrue(e.getMessage().contains("FLOW_NODE_INSTANCE_ID = " + FLOW_NODE_INSTANCE_ID));
        assertTrue(e.getMessage().contains("PROCESS_INSTANCE_ID = " + PROCESS_INSTANCE_ID));
        assertTrue(e.getMessage().contains("ROOT_PROCESS_INSTANCE_ID = " + ROOT_PROCESS_INSTANCE_ID));
        assertTrue(e.getMessage().contains("PROCESS_DEFINITION_ID = " + PROCESS_DEFINITION_ID));
        assertTrue(e.getMessage().contains("PROCESS_NAME = " + NAME));
        assertTrue(e.getMessage().contains("PROCESS_VERSION = " + VERSION));
        verify(wrappedWork, times(1)).handleFailure(e, context);
    }

}
