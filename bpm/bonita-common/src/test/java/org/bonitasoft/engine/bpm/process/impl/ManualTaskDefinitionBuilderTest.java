/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.bpm.process.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.bpm.flownode.ManualTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Laurent Leseigneur
 */
public class ManualTaskDefinitionBuilderTest {

    ProcessDefinitionBuilder processDefinitionBuilder;

    FlowElementContainerDefinitionImpl container;

    Expression expression;

    @Before
    public void before() throws Exception {
        processDefinitionBuilder = new ProcessDefinitionBuilder();
        container = new FlowElementContainerDefinitionImpl();
        expression = new ExpressionBuilder().createConstantLongExpression(456);
    }

    @Test
    public void should_addExpectedDuration_as_long_create_expression() throws Exception {
        //given
        ManualTaskDefinitionBuilder manualTaskDefinitionBuilder = new ManualTaskDefinitionBuilder(
                processDefinitionBuilder, container, "task", "actor");

        //when
        manualTaskDefinitionBuilder.addExpectedDuration(123L);

        //then
        ManualTaskDefinition manualTaskDefinition = getManualTaskDefinition(manualTaskDefinitionBuilder);
        checkExpressionIsCreated(manualTaskDefinition);

    }

    @Test
    public void should_addExpectedDuration_create_expression_with_new_id() throws Exception {
        //given
        ManualTaskDefinitionBuilder manualTaskDefinitionBuilder = new ManualTaskDefinitionBuilder(
                processDefinitionBuilder, container, "task", "actor");

        //when
        manualTaskDefinitionBuilder.addExpectedDuration(expression);

        //then
        ManualTaskDefinition manualTaskDefinition = getManualTaskDefinition(manualTaskDefinitionBuilder);
        checkExpressionIsCreated(manualTaskDefinition);
        assertThat(manualTaskDefinition.getExpectedDuration().getId()).as("should have a new ID for xsd validation")
                .isNotEqualTo(expression.getId());

    }

    private ManualTaskDefinition getManualTaskDefinition(ManualTaskDefinitionBuilder manualTaskDefinitionBuilder) {
        return (ManualTaskDefinition) manualTaskDefinitionBuilder.getActivity();
    }

    private void checkExpressionIsCreated(ManualTaskDefinition manualTaskDefinition) {
        assertThat(manualTaskDefinition.getExpectedDuration()).as("should add expression").isNotNull();
        assertThat(manualTaskDefinition.getExpectedDuration().getId()).as("should have a unique ID for xsd validation")
                .isGreaterThan(0);
    }

}
