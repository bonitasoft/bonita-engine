package org.bonitasoft.engine.execution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinitionWithInputValues;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilders;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.BPMDefinitionBuilders;
import org.bonitasoft.engine.core.process.document.api.ProcessDocumentService;
import org.bonitasoft.engine.core.process.document.model.builder.SProcessDocumentBuilder;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.handler.SProcessInstanceHandler;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessExecutorImplTest {

    @Mock
    private ActivityInstanceService activityInstanceService;

    @Mock
    private ArchiveService archiveService;

    @Mock
    private BPMDefinitionBuilders bpmDefinitionBuilders;

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
    private SProcessDocumentBuilder documentBuilder;

    @Mock
    private EventInstanceService eventInstanceService;

    @Mock
    private EventService eventService;

    @Mock
    private EventsHandler eventsHandler;

    @Mock
    private SExpressionBuilders expressionBuilders;

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
    private BPMInstanceBuilders instanceBuilders;

    @Mock
    private LockService lockService;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private SOperationBuilders operationBuilders;

    @Mock
    private OperationService operationService;

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Mock
    private ProcessDocumentService processDocumentService;

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

    // @Test
    // public void startProcessWithOperationsAndContext() throws Exception {
    // long userId = 1L;
    // List<SOperation> operations = new ArrayList<SOperation>(1);
    // operations.add(mock(SOperation.class));
    // Map<String, Object> context = new HashMap<String, Object>(1);
    // context.put("input", "value");
    //
    // ProcessExecutorImpl mockedProcessExecutorImpl = mock(ProcessExecutorImpl.class, withSettings().spiedInstance(processExecutorImpl));
    // SProcessDefinition sDefinition = mock(SProcessDefinition.class, RETURNS_MOCKS);
    // when(mockedProcessExecutorImpl.start(userId, sDefinition, operations, context)).then();
    // SProcessInstance processInstance = mockedProcessExecutorImpl.start(userId, sDefinition, operations, context);
    //
    // start(userId, sDefinition, null, operations, context, null, -1, -1);
    // assertNotNull(processInstance);
    // }

    @Test
    public void startProcessWithOperationsAndContext2() throws Exception {
        long userId = 1L;
        List<SOperation> operations = new ArrayList<SOperation>(1);
        operations.add(mock(SOperation.class));
        Map<String, Object> context = new HashMap<String, Object>(1);
        context.put("input", "value");

        SExpressionContext expressionContext = mock(SExpressionContext.class);
        List<ConnectorDefinitionWithInputValues> connectors = new ArrayList<ConnectorDefinitionWithInputValues>();
        connectors.add(mock(ConnectorDefinitionWithInputValues.class));
        long callerId = 1L;
        long subProcessDefinitionId = 1L;

        ProcessExecutorImpl mockedProcessExecutorImpl = mock(ProcessExecutorImpl.class, withSettings().spiedInstance(processExecutorImpl));
        SProcessDefinition sProcessDefinition = mock(SProcessDefinition.class);
        SProcessInstance sInstance = mock(SProcessInstance.class);
        when(mockedProcessExecutorImpl.startElements(sProcessDefinition, sInstance, subProcessDefinitionId)).thenReturn(sInstance);
        when(mockedProcessExecutorImpl.createProcessInstance(userId, sProcessDefinition, callerId)).thenReturn(sInstance);

        doCallRealMethod().when(mockedProcessExecutorImpl).start(userId, sProcessDefinition, expressionContext, operations, context, connectors, callerId,
                subProcessDefinitionId);

        // Let's call it for real:
        SProcessInstance processInstance = mockedProcessExecutorImpl.start(userId, sProcessDefinition, expressionContext, operations, context, connectors,
                callerId, subProcessDefinitionId);

        // and check methods are called:
        verify(mockedProcessExecutorImpl, times(1)).startElements(any(SProcessDefinition.class), any(SProcessInstance.class), anyLong());
        verify(mockedProcessExecutorImpl).createProcessInstance(userId, sProcessDefinition, callerId);

        assertNotNull(processInstance);
        assertEquals(sInstance, processInstance);
    }

}
