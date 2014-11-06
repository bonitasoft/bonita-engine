package org.bonitasoft.engine.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.core.data.instance.TransientDataService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.impl.SFlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SUserTaskDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.definition.model.impl.STextDefinitionImpl;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceNotFoundException;
import org.bonitasoft.engine.data.instance.model.impl.SShortTextDataInstanceImpl;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransientDataLeftOperandHandlerTest {

    @Mock
    private TransientDataService transientDataService;

    @Mock
    private FlowNodeInstanceService flownodeInstanceService;

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Mock
    private BPMInstancesCreator bpmInstancesCreator;

    @Mock
    private TechnicalLoggerService logger;

    @InjectMocks
    private TransientDataLeftOperandHandler transientDataLeftOperandHandler;

    @Test
    public void should_update_call_transient_data_service() throws Exception {
        // given
        final SShortTextDataInstanceImpl data = createData();
        when(transientDataService.getDataInstance("myData", 42, "ctype")).thenReturn(data);
        // when
        transientDataLeftOperandHandler.update(createLeftOperand("myData"), "new Value", 42, "ctype");

        // then
        final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        entityUpdateDescriptor.addField("value", "new Value");
        verify(transientDataService, times(1)).updateDataInstance(eq(data), eq(entityUpdateDescriptor));
        verify(logger, times(1)).log(eq(TransientDataLeftOperandHandler.class), eq(TechnicalLogSeverity.WARNING), anyString());
    }

    private SLeftOperandImpl createLeftOperand(final String name) {
        final SLeftOperandImpl sLeftOperand = new SLeftOperandImpl();
        sLeftOperand.setName(name);
        return sLeftOperand;
    }

    @Test
    public void should_retrieve_get_data_from_transient_service() throws Exception {
        // given
        final SShortTextDataInstanceImpl data = createData();
        when(transientDataService.getDataInstance("myData", 42, "ctype")).thenReturn(data);
        // when
        final Object retrieve = transientDataLeftOperandHandler.retrieve(createLeftOperand("myData"), new SExpressionContext(42l, "ctype", 12l));

        // then
        assertThat(retrieve).isEqualTo(data);
    }

    private SShortTextDataInstanceImpl createData() {
        final SShortTextDataInstanceImpl data = new SShortTextDataInstanceImpl();
        data.setName("myData");
        data.setId(56);
        data.setValue("The data value");
        return data;
    }

    @Test
    public void should_retrieve_reevaluate_definition_if_not_found() throws Exception {
        // given
        final long processDefId = 12l;
        final long taskId = 42l;
        final long taskDefId = 55l;
        final SUserTaskInstanceImpl task = new SUserTaskInstanceImpl("myTask", taskDefId, 23l, 23l, 5l, STaskPriority.HIGHEST, processDefId, 23l);
        final SFlowElementContainerDefinitionImpl processContainer = new SFlowElementContainerDefinitionImpl();
        final SUserTaskDefinitionImpl taskDef = new SUserTaskDefinitionImpl(taskDefId, "myTasl", "actor");
        final STextDefinitionImpl sTextDefinitionImpl = new STextDefinitionImpl();
        sTextDefinitionImpl.setTransientData(true);
        sTextDefinitionImpl.setName("myData");
        sTextDefinitionImpl.setDefaultValueExpression(null);
        taskDef.addSDataDefinition(sTextDefinitionImpl);
        processContainer.addActivity(taskDef);
        final SProcessDefinitionImpl sProcessDefinitionImpl = new SProcessDefinitionImpl("MyProcess", "1.0");
        sProcessDefinitionImpl.setProcessContainer(processContainer);
        doReturn(task).when(flownodeInstanceService).getFlowNodeInstance(taskId);
        doReturn(sProcessDefinitionImpl).when(processDefinitionService).getProcessDefinition(processDefId);
        final SShortTextDataInstanceImpl data = createData();
        doThrow(SDataInstanceNotFoundException.class).doReturn(data).when(transientDataService).getDataInstance("myData", taskId, "ctype");

        // when
        final Object retrieve = transientDataLeftOperandHandler.retrieve(createLeftOperand("myData"), new SExpressionContext(taskId, "ctype", processDefId));

        // then
        assertThat(retrieve).isEqualTo(data);
        verify(bpmInstancesCreator, times(1)).createDataInstances(eq(Arrays.<SDataDefinition> asList(sTextDefinitionImpl)), eq(taskId),
                eq(DataInstanceContainer.ACTIVITY_INSTANCE), any(SExpressionContext.class));
        verify(logger).log(eq(TransientDataLeftOperandHandler.class), eq(TechnicalLogSeverity.WARNING), anyString());
    }

    @Test
    public void should_update_reevaluate_definition_if_not_found() throws Exception {
        // given
        final long processDefId = 12l;
        final long taskId = 42l;
        final long taskDefId = 55l;
        final SUserTaskInstanceImpl task = new SUserTaskInstanceImpl("myTask", taskDefId, 23l, 23l, 5l, STaskPriority.HIGHEST, processDefId, 23l);
        final SFlowElementContainerDefinitionImpl processContainer = new SFlowElementContainerDefinitionImpl();
        final SUserTaskDefinitionImpl taskDef = new SUserTaskDefinitionImpl(taskDefId, "myTasl", "actor");
        final STextDefinitionImpl sTextDefinitionImpl = new STextDefinitionImpl();
        sTextDefinitionImpl.setTransientData(true);
        sTextDefinitionImpl.setName("myData");
        sTextDefinitionImpl.setDefaultValueExpression(null);
        taskDef.addSDataDefinition(sTextDefinitionImpl);
        processContainer.addActivity(taskDef);
        final SProcessDefinitionImpl sProcessDefinitionImpl = new SProcessDefinitionImpl("MyProcess", "1.0");
        sProcessDefinitionImpl.setProcessContainer(processContainer);
        doReturn(task).when(flownodeInstanceService).getFlowNodeInstance(taskId);
        doReturn(sProcessDefinitionImpl).when(processDefinitionService).getProcessDefinition(processDefId);
        final SShortTextDataInstanceImpl data = createData();
        doThrow(SDataInstanceNotFoundException.class).doReturn(data).when(transientDataService).getDataInstance("myData", taskId, "ctype");

        // when
        transientDataLeftOperandHandler.update(createLeftOperand("myData"), "newValue", taskId, "ctype");

        // then
        verify(bpmInstancesCreator, times(1)).createDataInstances(eq(Arrays.<SDataDefinition> asList(sTextDefinitionImpl)), eq(taskId),
                eq(DataInstanceContainer.ACTIVITY_INSTANCE), any(SExpressionContext.class));
        verify(logger, times(2)).log(eq(TransientDataLeftOperandHandler.class), eq(TechnicalLogSeverity.WARNING), anyString());
    }

    @Test(expected = SOperationExecutionException.class)
    public void deleteThrowsAnExceptionNotYetSupported() throws Exception {
        transientDataLeftOperandHandler.delete(createLeftOperand("myData"), 45l, "container");
    }

    @Test
    public void handlerSupportsBatchUpdate() {
        assertThat(transientDataLeftOperandHandler.supportBatchUpdate()).isTrue();
    }

}
