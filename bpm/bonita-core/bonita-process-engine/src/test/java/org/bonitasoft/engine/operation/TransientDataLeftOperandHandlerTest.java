package org.bonitasoft.engine.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.core.data.instance.TransientDataService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceNotFoundException;
import org.bonitasoft.engine.data.instance.model.impl.SShortTextDataInstanceImpl;
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

    @InjectMocks
    private TransientDataLeftOperandHandler transientDataLeftOperandHandler;

    @Test
    public void should_update_call_transient_data_service() throws Exception {
        // given
        SShortTextDataInstanceImpl data = createData();
        when(transientDataService.getDataInstance("myData", 42, "ctype")).thenReturn(data);
        // when
        transientDataLeftOperandHandler.update(createLeftOperand("myData"), "new Value", 42, "ctype");

        // then
        EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        entityUpdateDescriptor.addField("value", "new Value");
        verify(transientDataService, times(1)).updateDataInstance(eq(data), eq(entityUpdateDescriptor));
    }

    private SLeftOperandImpl createLeftOperand(final String name) {
        SLeftOperandImpl sLeftOperand = new SLeftOperandImpl();
        sLeftOperand.setName(name);
        return sLeftOperand;
    }

    @Test
    public void should_retrieve_get_data_from_transient_service() throws Exception {
        // given
        SShortTextDataInstanceImpl data = createData();
        when(transientDataService.getDataInstance("myData", 42, "ctype")).thenReturn(data);
        // when
        Object retrieve = transientDataLeftOperandHandler.retrieve(createLeftOperand("myData"), new SExpressionContext(42l, "ctype", 12l));

        // then
        assertThat(retrieve).isEqualTo(data);
    }

    private SShortTextDataInstanceImpl createData() {
        SShortTextDataInstanceImpl data = new SShortTextDataInstanceImpl();
        data.setName("myData");
        data.setId(56);
        data.setValue("The data value");
        return data;
    }

    @Test
    public void should_retrieve_reevaluate_definition_id_not_found() throws Exception {
        // given
        SShortTextDataInstanceImpl data = createData();
        doThrow(SDataInstanceNotFoundException.class).when(transientDataService).getDataInstance("myData", 42, "ctype");
        // when
        Object retrieve = transientDataLeftOperandHandler.retrieve(createLeftOperand("myData"), new SExpressionContext(42l, "ctype", 12l));

        // then
        // TODO add check on call of process instance service
        assertThat(retrieve).isEqualTo(data);
    }

}
