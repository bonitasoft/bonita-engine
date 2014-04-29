package org.bonitasoft.engine.core.expression.control.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SExpressionContextTest {

    @Test
    public void should_toString_return_a_human_readable_context() throws Exception {
        // given
        SExpressionContext expressionContext = new SExpressionContext(123L, "typeOfTheContainer", 456L);
        SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doReturn("ProcessName").when(processDefinition).getName();
        doReturn("1.0").when(processDefinition).getVersion();
        doReturn(mock(SFlowElementContainerDefinition.class)).when(processDefinition).getProcessContainer();
        expressionContext.setProcessDefinition(processDefinition);

        // when
        String string = expressionContext.toString();
        // then
        assertThat(string).isEqualTo(
                "context [containerId=123, containerType=typeOfTheContainer, processDefinitionId=456, processDefinition=ProcessName -- 1.0]");
    }

}
