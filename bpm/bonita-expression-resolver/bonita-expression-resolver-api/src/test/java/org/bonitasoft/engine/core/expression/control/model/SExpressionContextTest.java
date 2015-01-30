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
    public void should_toString_return_a_human_readable_context() {
        // given
        final SExpressionContext expressionContext = new SExpressionContext(123L, "typeOfTheContainer", 456L);
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doReturn("ProcessName").when(processDefinition).getName();
        doReturn("1.0").when(processDefinition).getVersion();
        doReturn(mock(SFlowElementContainerDefinition.class)).when(processDefinition).getProcessContainer();
        expressionContext.setProcessDefinition(processDefinition);

        // when
        final String string = expressionContext.toString();
        // then
        assertThat(string).isEqualTo(
                "context [containerId=123, containerType=typeOfTheContainer, processDefinitionId=456, processDefinition=ProcessName -- 1.0]");
    }

}
