/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.execution;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.bonitasoft.engine.service.platform.PlatformInformationManager;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinitionWithInputValues;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.model.impl.SProcessInstanceImpl;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.FlowNodeSelector;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.handler.SProcessInstanceHandler;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.platform.exception.SPlatformUpdateException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessExecutorExtTest {

    @Mock
    private PlatformInformationManager manager;

    @Mock
    private FlowNodeStateManager flowNodeStateManager;

    @Mock
    private EventsHandler eventsHandler;

    @Mock
    ContainerRegistry containerRegistry;

    @Mock
    Map<String, SProcessInstanceHandler<SEvent>> handlers;

    @InjectMocks
    private ProcessExecutorExt processExecutorExt;

    private ProcessExecutorExt spy;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        spy = spy(processExecutorExt);
        given(spy.getPlatformInformationManager()).willReturn(manager);
        given(handlers.entrySet()).willReturn(Collections.<Map.Entry<String,SProcessInstanceHandler<SEvent>>>emptySet());
    }

    @Test
    public void start_should_update_platform_info_when_is_root_process_instance() throws Exception {
        long callerId = -1;
        SProcessInstanceImpl processInstance = new SProcessInstanceImpl();
        processInstance.setCallerId(callerId);

        //given
        doReturn(processInstance).when(spy).startSuper(anyLong(), anyLong(), any(SExpressionContext.class), Matchers.<List<SOperation>>any(), Matchers.<Map<String, Object>>any(), Matchers.<List<ConnectorDefinitionWithInputValues>>any(), anyLong(), any(FlowNodeSelector.class),  Matchers.<Map<String, Serializable>>any());

        //when
        spy.start(-1, -1, new SExpressionContext(), null, null, null, callerId, null, null);

        //then
        verify(manager).update();
    }

    @Test
    public void start_should_not_update_platform_info_when_is_not_root_process_instance() throws Exception {
        long callerId = 4;
        SProcessInstanceImpl processInstance = new SProcessInstanceImpl();
        processInstance.setCallerId(callerId);

        //given
        doReturn(processInstance).when(spy).startSuper(anyLong(), anyLong(), any(SExpressionContext.class), Matchers.<List<SOperation>>any(), Matchers.<Map<String, Object>>any(), Matchers.<List<ConnectorDefinitionWithInputValues>>any(), anyLong(), any(FlowNodeSelector.class), Matchers.<Map<String, Serializable>>any());

        //when
        spy.start(-1, -1, new SExpressionContext(), null, null, null, callerId, null, null);

        //then
        verify(manager, never()).update();
    }

    @Test
    public void start_should_throw_SProcessInstanceCreationException_when_update_platform_info_throws_Exception() throws Exception {
        long callerId = -1;
        SProcessInstanceImpl processInstance = new SProcessInstanceImpl();
        processInstance.setCallerId(callerId);

        //given
        doReturn(processInstance).when(spy).startSuper(anyLong(), anyLong(), any(SExpressionContext.class), Matchers.<List<SOperation>>any(), Matchers.<Map<String, Object>>any(), Matchers.<List<ConnectorDefinitionWithInputValues>>any(), anyLong(), any(FlowNodeSelector.class), Matchers.<Map<String, Serializable>>any());
        SPlatformUpdateException updateException = new SPlatformUpdateException("unable to update");
        doThrow(updateException).when(manager).update();

        //then
        expectedException.expect(SProcessInstanceCreationException.class);
        expectedException.expectCause(equalTo(updateException));

        //when
        spy.start(-1, -1, new SExpressionContext(), null, null, null, callerId, null, null);

    }

}