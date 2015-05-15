/*
 * Copyright (C) 2015 Bonitasoft S.A.
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
 */
package org.bonitasoft.engine.bpm.process.impl.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.bpm.businessdata.impl.BusinessDataDefinitionImpl;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.impl.ConnectorDefinitionImpl;
import org.bonitasoft.engine.bpm.context.ContextEntryImpl;
import org.bonitasoft.engine.bpm.data.impl.DataDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.UserTaskDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;
import org.bonitasoft.engine.operation.impl.OperationImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class ExpressionFinderTest {

    private ExpressionFinder expressionFinder = spy(new ExpressionFinder());

    @Test
    public void findShouldRecurseTreeUntilFound() throws Exception {
        DesignProcessDefinitionImpl def = new DesignProcessDefinitionImpl("name", "version");
        final ContextEntryImpl contextEntry = new ContextEntryImpl();
        contextEntry.setExpression(new ExpressionImpl());
        def.addContextEntry(contextEntry);
        final ExpressionImpl stringIndex = new ExpressionImpl();
        def.setStringIndex(1, "label1", stringIndex);
        final FlowElementContainerDefinitionImpl processContainer = new FlowElementContainerDefinitionImpl();
        def.setProcessContainer(processContainer);
        final UserTaskDefinitionImpl userTask = new UserTaskDefinitionImpl("task", "actor");
        final ContextEntryImpl taskContextEntry = new ContextEntryImpl();
        userTask.addContextEntry(taskContextEntry);
        processContainer.addActivity(userTask);
        final ExpressionImpl bizDataDefaultValue = new ExpressionImpl();
        processContainer.addBusinessDataDefinition(new BusinessDataDefinitionImpl("bizData", bizDataDefaultValue));
        final ExpressionImpl dataDefaultValue = new ExpressionImpl();
        processContainer.addDataDefinition(new DataDefinitionImpl("data", dataDefaultValue));
        final ConnectorDefinitionImpl connectorDefinition = new ConnectorDefinitionImpl("connector", "connDefId", "conneVersion", ConnectorEvent.ON_ENTER);
        final ExpressionImpl connectorInput = new ExpressionImpl();
        connectorDefinition.addInput("input", connectorInput);
        final OperationImpl operation = new OperationImpl();
        final ExpressionImpl connectorOperationRightOperand = new ExpressionImpl();
        operation.setRightOperand(connectorOperationRightOperand);
        connectorDefinition.addOutput(operation);
        processContainer.addConnector(connectorDefinition);

        expressionFinder.find(def, 999L);

        verify(expressionFinder).find(contextEntry, 999L);
        verify(expressionFinder).find(stringIndex, 999L);
        verify(expressionFinder).find(userTask, 999L);
        verify(expressionFinder).find(taskContextEntry, 999L);
        verify(expressionFinder).find(bizDataDefaultValue, 999L);
        verify(expressionFinder).find(dataDefaultValue, 999L);
        verify(expressionFinder).find(connectorDefinition, 999L);
        verify(expressionFinder).find(connectorInput, 999L);
        verify(expressionFinder).find(operation, 999L);
        verify(expressionFinder).find(connectorOperationRightOperand, 999L);
    }

    @Test
    public void findShouldNotSearchAnymoreWhenFound() throws Exception {
        DesignProcessDefinitionImpl def = new DesignProcessDefinitionImpl("name", "version");
        final ContextEntryImpl contextEntry = new ContextEntryImpl();
        long expressionFinderId = 6987451354L;
        final ExpressionImpl expressionToBeFound = new ExpressionImpl(expressionFinderId);
        contextEntry.setExpression(expressionToBeFound);
        def.addContextEntry(contextEntry);
        final ExpressionImpl stringIndex = new ExpressionImpl();
        def.setStringIndex(1, "label1", stringIndex);
        final FlowElementContainerDefinitionImpl processContainer = new FlowElementContainerDefinitionImpl();
        def.setProcessContainer(processContainer);
        final UserTaskDefinitionImpl userTask = new UserTaskDefinitionImpl("task", "actor");
        final ContextEntryImpl taskContextEntry = new ContextEntryImpl();
        userTask.addContextEntry(taskContextEntry);
        processContainer.addActivity(userTask);
        final ExpressionImpl bizDataDefaultValue = new ExpressionImpl();
        processContainer.addBusinessDataDefinition(new BusinessDataDefinitionImpl("bizData", bizDataDefaultValue));

        final Expression expression = expressionFinder.find(def, expressionFinderId);
        assertThat(expression).isEqualTo(expressionToBeFound);

        verify(expressionFinder).find(contextEntry, expressionFinderId);
        verify(expressionFinder).find(expressionToBeFound, expressionFinderId);
    }
}
