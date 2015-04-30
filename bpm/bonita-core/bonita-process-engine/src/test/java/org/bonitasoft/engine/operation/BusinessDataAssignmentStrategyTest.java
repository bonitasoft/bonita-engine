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

package org.bonitasoft.engine.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.SOperatorType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.bonitasoft.engine.bdm.Entity;

@RunWith(MockitoJUnitRunner.class)
public class BusinessDataAssignmentStrategyTest {

    @Mock
    private EntitiesActionsExecutor actionsExecutor;

    @Mock
    private MergeEntityAction action;

    @InjectMocks
    private BusinessDataAssignmentStrategy strategy;

    @Mock
    private Entity entity;

    private Address mergedEntity = new Address(45L);

    @Mock
    SExpressionContext expressionContext;

    @Mock
    SOperation operation;

    @Test
    public void computeNewValueForLeftOperand_should_return_merged_entity_when_should_persist() throws Exception {
        //given
        given(actionsExecutor.executeAction(entity, null, action)).willReturn(mergedEntity);

        //when
        Object newValue = strategy.computeNewValueForLeftOperand(operation, entity, expressionContext, true);

        //then
        assertThat(newValue).isEqualTo(mergedEntity);
    }

    @Test
    public void computeNewValueForLeftOperand_should_return_current_entity_when_should_not_persist() throws Exception {
        //when
        Object newValue = strategy.computeNewValueForLeftOperand(operation, entity, expressionContext, false);

        //then
        assertThat(newValue).isEqualTo(entity);
    }

    @Test(expected = SOperationExecutionException.class)
    public void computeNewValueForLeftOperand_should_throws_operation_exception_when_merge_throws_exception() throws Exception {
        //given
        given(actionsExecutor.executeAction(entity, null, action)).willThrow(new SEntityActionExecutionException(""));

        //when
        strategy.computeNewValueForLeftOperand(operation, entity, expressionContext, true);

        //then
    }

    @Test
    public void getOperationType_should_return_business_data_assignment() throws Exception {
        assertThat(strategy.getOperationType()).isEqualTo(SOperatorType.ASSIGNMENT + "_" + SLeftOperand.TYPE_BUSINESS_DATA);
    }
}
