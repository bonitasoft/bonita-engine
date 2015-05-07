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
package org.bonitasoft.engine.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinitionWithInputValues;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilder;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSubProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SDocumentDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.handler.SProcessInstanceHandler;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Assert;
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
    private SDocumentBuilder documentBuilder;

    @Mock
    private EventInstanceService eventInstanceService;

    @Mock
    private EventService eventService;

    @Mock
    private EventsHandler eventsHandler;

    @Mock
    private ExpressionResolverService expressionResolverService;

    @Mock
    private ExpressionService expressionService;

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
    private TransactionExecutor transactionExecutor;

    @Mock
    private TransitionService transitionService;

    @Mock
    private WorkService workService;

    @Mock
    private BusinessDataRepository businessDataRepository;

    @Mock
    private RefBusinessDataService refBusinessDataService;

    @InjectMocks
    private ProcessExecutorImpl processExecutorImpl;

    @Test
    public void startProcessShouldInitializeBusinessData() throws Exception {
        final long starterId = 1L;
        final long starterSubstituteId = 9L;
        final List<SOperation> operations = new ArrayList<SOperation>(1);
        operations.add(mock(SOperation.class));
        final Map<String, Object> context = new HashMap<String, Object>(1);
        context.put("input", "value");

        final ProcessExecutorImpl mockedProcessExecutorImpl = mock(ProcessExecutorImpl.class, withSettings().spiedInstance(processExecutorImpl));
        final SProcessDefinition sProcessDefinition = mock(SProcessDefinition.class);
        final SProcessInstance sProcessInstance = mock(SProcessInstance.class);
        final FlowNodeSelector selector = new FlowNodeSelector(sProcessDefinition, null);
        when(mockedProcessExecutorImpl.start(starterId, starterSubstituteId, null, operations, context, null, -1, selector, null)).thenReturn(
                sProcessInstance);

        // Let's call it for real:
        doCallRealMethod().when(mockedProcessExecutorImpl).start(starterId, starterSubstituteId, operations, context, null, selector, null);
        final SProcessInstance result = mockedProcessExecutorImpl.start(starterId, starterSubstituteId, operations, context, null, selector, null);

        Assert.assertNotNull(result);
        Assert.assertEquals(sProcessInstance, result);
    }

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
        final FlowNodeSelector selector = new FlowNodeSelector(sProcessDefinition, null);
        when(mockedProcessExecutorImpl.start(starterId, starterSubstituteId, null, operations, context, null, -1, selector, null)).thenReturn(
                sProcessInstance);

        // Let's call it for real:
        doCallRealMethod().when(mockedProcessExecutorImpl).start(starterId, starterSubstituteId, operations, context, null, selector, null);
        final SProcessInstance result = mockedProcessExecutorImpl.start(starterId, starterSubstituteId, operations, context, null, selector, null);

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
        final SSubProcessDefinition subProcessDef = mock(SSubProcessDefinition.class);
        final SFlowElementContainerDefinition rootContainerDefinition = mock(SFlowElementContainerDefinition.class);
        doReturn(rootContainerDefinition).when(sProcessDefinition).getProcessContainer();
        doReturn(subProcessDef).when(rootContainerDefinition).getFlowNode(subProcessDefinitionId);
        final SProcessInstance sProcessInstance = mock(SProcessInstance.class);
        final FlowNodeSelector selector = new FlowNodeSelector(sProcessDefinition, null, subProcessDefinitionId);
        when(mockedProcessExecutorImpl.startElements(sProcessInstance, selector)).thenReturn(sProcessInstance);
        when(mockedProcessExecutorImpl.createProcessInstance(sProcessDefinition, starterId, starterSubstituteId, subProcessDefinitionId)).thenReturn(
                sProcessInstance);

        final Map<String, Serializable> processInputs = new HashMap<>(0);
        doNothing().when(mockedProcessExecutorImpl).validateContractInputs(processInputs, sProcessDefinition);

        // Let's call it for real:
        doCallRealMethod().when(mockedProcessExecutorImpl).start(starterId, starterSubstituteId, expressionContext, operations, context,
                connectors, callerId, selector, processInputs);
        final SProcessInstance result = mockedProcessExecutorImpl.start(starterId, starterSubstituteId, expressionContext, operations,
                context, connectors, callerId, selector, processInputs);

        // and check methods are called:
        verify(mockedProcessExecutorImpl, times(1)).startElements(any(SProcessInstance.class), any(FlowNodeSelector.class));
        verify(mockedProcessExecutorImpl).createProcessInstance(sProcessDefinition, starterId, starterSubstituteId, subProcessDefinitionId);
        verify(mockedProcessExecutorImpl).validateContractInputs(processInputs, sProcessDefinition);

        Assert.assertNotNull(result);
        Assert.assertEquals(sProcessInstance, result);
    }

    @Test
    public void should_getInitialDocumentValue_return_the_document_value_from_expression() throws BonitaHomeNotSetException, STenantIdNotSetException,
            IOException {
        //given
        final SProcessDefinition sProcessDefinition = mock(SProcessDefinition.class);
        final SExpression initialValueExpression = new SExpressionImpl();
        final DocumentValue theUrl = new DocumentValue("theUrl");
        Map<SExpression, DocumentValue> evaluatedDocumentValues = Collections.singletonMap(initialValueExpression, theUrl);
        SDocumentDefinitionImpl documentDefinition = new SDocumentDefinitionImpl("myDoc");
        documentDefinition.setUrl("toto");//check it overrides it
        documentDefinition.setInitialValue(initialValueExpression);
        //when
        final DocumentValue initialDocumentValue = processExecutorImpl.getInitialDocumentValue(sProcessDefinition, evaluatedDocumentValues, documentDefinition);
        //then
        assertThat(initialDocumentValue).isEqualTo(theUrl);
    }

    @Test
    public void should_getInitialDocumentValue_return_the_document_value_from_url() throws BonitaHomeNotSetException, STenantIdNotSetException, IOException {
        //given
        final SProcessDefinition sProcessDefinition = mock(SProcessDefinition.class);
        SDocumentDefinitionImpl documentDefinition = new SDocumentDefinitionImpl("myDoc");
        documentDefinition.setUrl("url for the file");
        //when
        final DocumentValue initialDocumentValue = processExecutorImpl.getInitialDocumentValue(sProcessDefinition,
                Collections.<SExpression, DocumentValue> emptyMap(), documentDefinition);
        //then
        assertThat(initialDocumentValue).isEqualTo(new DocumentValue("url for the file"));
    }

    @Test
    public void should_getInitialDocumentValue_return_the_document_value_from_file() throws BonitaHomeNotSetException, STenantIdNotSetException, IOException {
        //given
        ProcessExecutorImpl processExecutor = spy(processExecutorImpl);
        final SProcessDefinition sProcessDefinition = mock(SProcessDefinition.class);
        SDocumentDefinitionImpl documentDefinition = new SDocumentDefinitionImpl("myDoc");
        documentDefinition.setFile("toto.txt");
        documentDefinition.setFileName("myFile.txt");
        final byte[] content = {1, 2, 3};
        doReturn(content).when(processExecutor).getProcessDocumentContent(sProcessDefinition, documentDefinition);
        //when
        final DocumentValue initialDocumentValue = processExecutor.getInitialDocumentValue(sProcessDefinition,
                Collections.<SExpression, DocumentValue> emptyMap(), documentDefinition);
        //then
        assertThat(initialDocumentValue).isEqualTo(new DocumentValue(content, null, "myFile.txt"));
    }

    @Test
    public void validateContractInputsShouldDoNothingIfcontractDefinitionIsNull() throws Exception {
        final SProcessDefinition mock = mock(SProcessDefinition.class);
        doReturn(null).when(mock).getContract();
        processExecutorImpl.validateContractInputs(new HashMap<String, Serializable>(), mock);
    }

    @Test
    public void validateContractInputsShouldValidateContractWithDefinition() throws Exception {
        final SProcessDefinition processDef = mock(SProcessDefinition.class);
        doReturn(mock(SContractDefinition.class)).when(processDef).getContract();
        processExecutorImpl.validateContractInputs(new HashMap<String, Serializable>(), processDef);

        // then, we call the validator:
        verify(processDef).getId();
    }
}
