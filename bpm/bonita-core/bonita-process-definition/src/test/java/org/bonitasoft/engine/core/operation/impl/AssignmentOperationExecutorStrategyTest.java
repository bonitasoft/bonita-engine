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
package org.bonitasoft.engine.core.operation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.SOperatorType;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssignmentOperationExecutorStrategyTest {

    @Mock
    private DataInstanceService dataInstanceService;

    @InjectMocks
    private AssignmentOperationExecutorStrategy assignmentOperationExecutorStrategy;

    private SLeftOperand leftOperand;

    private SOperation operation;

    private String value;

    private SExpressionContext expressionContext;

    @Before
    public void initMocks() {
        operation = mock(SOperation.class);
        value = "value";
        expressionContext = mock(SExpressionContext.class);
        leftOperand = mock(SLeftOperand.class);
        when(operation.getLeftOperand()).thenReturn(leftOperand);
        when(leftOperand.getName()).thenReturn("var");
    }

    @Test
    public void testGetValue() throws Exception {
        when(expressionContext.getInputValues()).thenReturn(Collections.<String, Object> singletonMap("var", "value"));
        when(leftOperand.getType()).thenReturn(SLeftOperand.TYPE_DATA);
        Object returnedValue = assignmentOperationExecutorStrategy.computeNewValueForLeftOperand(operation, value,
                expressionContext, false);
        assertThat(returnedValue).isEqualTo("value");
    }

    @Test
    public void testGetValueOnExternalData() throws Exception {
        // return type is not compatible
        when(leftOperand.getType()).thenReturn(SLeftOperand.TYPE_EXTERNAL_DATA);
        Object returnedValue = assignmentOperationExecutorStrategy.computeNewValueForLeftOperand(operation, value,
                expressionContext, false);
        assertThat(returnedValue).isEqualTo("value");
    }

    @Test(expected = SOperationExecutionException.class)
    public void testGetValueWithWrongType() throws Exception {
        // return type is not compatible
        when(expressionContext.getInputValues())
                .thenReturn(Collections.<String, Object> singletonMap("var", new java.util.TreeMap<String, Object>()));
        when(leftOperand.getType()).thenReturn(SLeftOperand.TYPE_DATA);
        assignmentOperationExecutorStrategy.computeNewValueForLeftOperand(operation, value, expressionContext, false);
    }

    @Test
    public void operationType_should_be_ASSIGNMENT() throws Exception {
        assertThat(assignmentOperationExecutorStrategy.getOperationType()).isEqualTo(SOperatorType.ASSIGNMENT.name());
    }

    @Test
    public void should_not_persist_on_null() throws Exception {
        assertThat(assignmentOperationExecutorStrategy.shouldPersistOnNullValue()).isFalse();
    }

}
