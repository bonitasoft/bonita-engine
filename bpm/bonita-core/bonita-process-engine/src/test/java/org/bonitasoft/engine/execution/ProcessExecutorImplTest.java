package org.bonitasoft.engine.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.util.Lists;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinitionWithInputValues;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SExceptionContext;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSubProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.builder.SDocumentMappingBuilder;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.handler.SProcessInstanceHandler;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.work.WorkService;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessExecutorImplTest {

	@Rule
    public ExpectedException thrown = ExpectedException.none();
	
    @Mock
    private ActivityInstanceService activityInstanceService;

    @Mock
    private ArchiveService archiveService;

    @Mock
    private BPMInstancesCreator bpmInstancesCreator;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private ClassLoaderService classLoaderService;

    @Mock
    private ConnectorInstanceService connectorInstanceService;

    @Mock
    private ConnectorService connectorService;

    @Mock
    private ContainerRegistry containerRegistry;

    @Mock
    private SDocumentMappingBuilder documentBuilder;

    @Mock
    private EventInstanceService eventInstanceService;

    @Mock
    private EventService eventService;

    @Mock
    private EventsHandler eventsHandler;

    @Mock
    private ExpressionResolverService expressionResolverService;

    @Mock
    private FlowNodeExecutor flowNodeExecutor;

    @Mock
    private FlowNodeStateManager flowNodeStateManager;

    @Mock
    private GatewayInstanceService gatewayInstanceService;

    @Mock
    private Map<String, SProcessInstanceHandler<SEvent>> handlers;

    @Mock
    private LockService lockService;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private OperationService operationService;

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Mock
    private DocumentService documentService;

    @Mock
    private ProcessInstanceService processInstanceService;

    @Mock
    private ReadSessionAccessor sessionAccessor;

    @Mock
    private TokenService tokenService;

    @Mock
    private TransactionExecutor transactionExecutor;

    @Mock
    private TransitionService transitionService;

    @Mock
    private WorkService workService;

    @InjectMocks
    private ProcessExecutorImpl processExecutorImpl;

    @Test
    public void startProcessWithOperationsAndContext() throws Exception {
        final long starterId = 1L;
        final long starterSubstituteId = 9L;
        final List<SOperation> operations = new ArrayList<SOperation>(1);
        operations.add(mock(SOperation.class));
        final Map<String, Object> context = new HashMap<String, Object>(1);
        context.put("input", "value");

        final ProcessExecutorImpl mockedProcessExecutorImpl = mock(ProcessExecutorImpl.class, withSettings().spiedInstance(processExecutorImpl));
        final SProcessDefinition sProcessDefinition = mock(SProcessDefinition.class);
        final SProcessInstance sProcessInstance = mock(SProcessInstance.class);
        FlowNodeSelector selector = new FlowNodeSelector(sProcessDefinition, null);
        when(mockedProcessExecutorImpl.start(starterId, starterSubstituteId, null, operations, context, null, -1, selector)).thenReturn(
                sProcessInstance);

        // Let's call it for real:
        doCallRealMethod().when(mockedProcessExecutorImpl).start(starterId, starterSubstituteId, operations, context, null, selector);
        final SProcessInstance result = mockedProcessExecutorImpl.start(starterId, starterSubstituteId, operations, context, null, selector);

        Assert.assertNotNull(result);
        Assert.assertEquals(sProcessInstance, result);
    }

    @Test
    public void startProcessWithOperationsAndContextAndExpressionContextAndConnectors() throws Exception {
        final long starterId = 1L;
        final long starterSubstituteId = 9L;
        final List<SOperation> operations = new ArrayList<SOperation>(1);
        operations.add(mock(SOperation.class));
        final Map<String, Object> context = new HashMap<String, Object>(1);
        context.put("input", "value");
        final SExpressionContext expressionContext = mock(SExpressionContext.class);
        final List<ConnectorDefinitionWithInputValues> connectors = new ArrayList<ConnectorDefinitionWithInputValues>();
        connectors.add(mock(ConnectorDefinitionWithInputValues.class));
        final long callerId = 1L;
        final long subProcessDefinitionId = 1L;

        final ProcessExecutorImpl mockedProcessExecutorImpl = mock(ProcessExecutorImpl.class, withSettings().spiedInstance(processExecutorImpl));
        final SProcessDefinition sProcessDefinition = mock(SProcessDefinition.class);
        SSubProcessDefinition subProcessDef = mock(SSubProcessDefinition.class);
        SFlowElementContainerDefinition rootContainerDefinition = mock(SFlowElementContainerDefinition.class);
        doReturn(rootContainerDefinition).when(sProcessDefinition).getProcessContainer();
        doReturn(subProcessDef).when(rootContainerDefinition).getFlowNode(subProcessDefinitionId);
        final SProcessInstance sProcessInstance = mock(SProcessInstance.class);
        FlowNodeSelector selector = new FlowNodeSelector(sProcessDefinition, null, subProcessDefinitionId);
        when(mockedProcessExecutorImpl.startElements(sProcessInstance, selector)).thenReturn(sProcessInstance);
        when(mockedProcessExecutorImpl.createProcessInstance(sProcessDefinition, starterId, starterSubstituteId, subProcessDefinitionId)).thenReturn(
                sProcessInstance);

        // Let's call it for real:
        doCallRealMethod().when(mockedProcessExecutorImpl).start(starterId, starterSubstituteId, expressionContext, operations, context,
                connectors, callerId, selector);
        final SProcessInstance result = mockedProcessExecutorImpl.start(starterId, starterSubstituteId, expressionContext, operations,
                context, connectors, callerId, selector);

        // and check methods are called:
        verify(mockedProcessExecutorImpl, times(1)).startElements(any(SProcessInstance.class), any(FlowNodeSelector.class));
        verify(mockedProcessExecutorImpl).createProcessInstance(sProcessDefinition, starterId, starterSubstituteId, subProcessDefinitionId);

        Assert.assertNotNull(result);
        Assert.assertEquals(sProcessInstance, result);
    }

    @Test(expected = NullPointerException.class)
    public void testEvaluateOutgoingTransitions_should_throw_NPE() throws Exception {
        processExecutorImpl.evaluateOutgoingTransitions(null, null, null);
    }

    @Test
    public void testEvaluateOutgoingTransitions_with_empty_params_should_return_empty_list() throws Exception {
        SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        SFlowNodeInstance flowNodeInstance = mock(SFlowNodeInstance.class);
        List<STransitionDefinition> transitionDefinitions = Lists.newArrayList();
        List<STransitionDefinition> results = processExecutorImpl.evaluateOutgoingTransitions(transitionDefinitions, processDefinition, flowNodeInstance);
        assertThat(results).isEmpty();
    }

    @Test
    public void testEvaluateOutgoingTransitions_with_empty_transitions_should_return_default_transition() throws Exception {
        SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        SFlowNodeInstance flowNodeInstance = mock(SFlowNodeInstance.class);
        STransitionDefinition defaultTransition = mock(STransitionDefinition.class);
        List<STransitionDefinition> transitionDefinitions = Lists.newArrayList();
        processExecutorImpl = spy(processExecutorImpl);

        doReturn(defaultTransition).when(processExecutorImpl).getDefaultTransition(processDefinition, flowNodeInstance);
        doReturn(null).when(processExecutorImpl).evaluateTransitionsForImpliciteGateway(eq(processDefinition), eq(flowNodeInstance), eq(transitionDefinitions),
                any(SExpressionContext.class));
        when(flowNodeInstance.getStateCategory()).thenReturn(SStateCategory.NORMAL);
        List<STransitionDefinition> results = processExecutorImpl.evaluateOutgoingTransitions(transitionDefinitions, processDefinition, flowNodeInstance);

        assertThat(results).containsExactly(defaultTransition);
        verify(processExecutorImpl, times(1)).getDefaultTransition(processDefinition, flowNodeInstance);
    }
    
    
    
    @Test
    public void testEvaluateTransitionsForImpliciteGateway_without_valid_transitions_should_throw_SActivityExecutionException() throws Exception {
        
    	// Given
    	final String processName = "Faulty Process";
    	final String processVersion = "6.3.1";
    	SProcessDefinition processDefinition = mock(SProcessDefinition.class);
    	when(processDefinition.getName()).thenReturn(processName);
		when(processDefinition.getVersion()).thenReturn(processVersion);

    	SFlowNodeInstance flowNodeInstance = mock(SFlowNodeInstance.class);
    	when(flowNodeInstance.getParentProcessInstanceId()).thenReturn(42L);
    	
    	
    	thrown.expect(SActivityExecutionException.class);
    	thrown.expect(new BaseMatcher<Object>() {

    		long expectedProcessInstanceID = 42L;

			@Override
			public boolean matches(Object item) {
				
				
				if(item instanceof SActivityExecutionException) {
					SActivityExecutionException exception = (SActivityExecutionException) item;
					
					Map<SExceptionContext, Serializable> context = exception.getContext();
					return (hasProcessNameInContext(processName, context) && hasProcessInstanceIDInContext(expectedProcessInstanceID, context) && hasProcessVersionInContext(processVersion, context));
				}
				return false;
			}

			private boolean hasProcessVersionInContext(String processVersion, Map<SExceptionContext, Serializable> context) {
				return processVersion.equals(context.get(SExceptionContext.PROCESS_VERSION));
			}

			private boolean hasProcessInstanceIDInContext(long processInstanceId, Map<SExceptionContext, Serializable> context) {
				return processInstanceId == (Long)context.get(SExceptionContext.PROCESS_INSTANCE_ID);
			}

			private boolean hasProcessNameInContext(final String processName, Map<SExceptionContext, Serializable> context) {
				return processName.equals(context.get(SExceptionContext.PROCESS_NAME));
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("Having context containing Process Name: " + processName + " and Version: " + processVersion + " and Process Instance ID: " + expectedProcessInstanceID);
				
			}
			});
    	

        processExecutorImpl = spy(processExecutorImpl);
        doReturn(null).when(processExecutorImpl).getDefaultTransition(processDefinition, flowNodeInstance);
        
    	// When
    	processExecutorImpl.evaluateTransitionsInclusively(processDefinition, flowNodeInstance, Collections.<STransitionDefinition>emptyList(), null);
    	
    }

    @Test
    public void testEvaluateOutgoingTransitions_with_some_transition_and_default_transition_should_not_return_default_transition() throws Exception {
        SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        SFlowNodeInstance flowNodeInstance = mock(SFlowNodeInstance.class);
        STransitionDefinition defaultTransition = mock(STransitionDefinition.class);
        STransitionDefinition transition1 = mock(STransitionDefinition.class);
        STransitionDefinition transition2 = mock(STransitionDefinition.class);
        STransitionDefinition transition3 = mock(STransitionDefinition.class);
        List<STransitionDefinition> transitionDefinitions = Lists.newArrayList(transition1, transition2, transition3);
        processExecutorImpl = spy(processExecutorImpl);

        doReturn(defaultTransition).when(processExecutorImpl).getDefaultTransition(processDefinition, flowNodeInstance);
        doReturn(transitionDefinitions).when(processExecutorImpl).evaluateTransitionsForImpliciteGateway(eq(processDefinition), eq(flowNodeInstance),
                eq(transitionDefinitions),
                any(SExpressionContext.class));
        when(flowNodeInstance.getStateCategory()).thenReturn(SStateCategory.NORMAL);
        List<STransitionDefinition> results = processExecutorImpl.evaluateOutgoingTransitions(transitionDefinitions, processDefinition, flowNodeInstance);

        assertThat(results).isEqualTo(transitionDefinitions);
        verify(processExecutorImpl, times(0)).getDefaultTransition(processDefinition, flowNodeInstance);
        verify(processExecutorImpl, times(1)).evaluateTransitionsForImpliciteGateway(eq(processDefinition), eq(flowNodeInstance), eq(transitionDefinitions),
                any(SExpressionContext.class));
    }
}
