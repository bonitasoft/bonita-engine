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

package org.bonitasoft.engine.core.operation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.core.operation.impl.OperationMockBuilder.buildExpression;
import static org.bonitasoft.engine.core.operation.impl.OperationMockBuilder.buildMockLeftOperand;
import static org.bonitasoft.engine.core.operation.impl.OperationMockBuilder.buildMockOperation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.bonitasoft.engine.core.operation.model.impl.SOperationImpl;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Test;

public class OperationsAnalyzerTest {

    private OperationsAnalyzer dependencyFinder = new OperationsAnalyzer();

    @Test
    public void findDependencyIndex_should_return_minus_one_if_index_is_out_of_list() throws Exception {
        //when
        int index = dependencyFinder.findBusinessDataDependencyIndex("address", 1, Collections.<SOperation> emptyList());

        //then
        assertThat(index).isEqualTo(-1);
    }

    @Test
    public void findDependencyIndex_should_return_minus_one_if_right_operand_is_null() throws Exception {
        //when
        int index = dependencyFinder.findBusinessDataDependencyIndex("address", 0,
                Arrays.<SOperation> asList(buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, (SExpressionImpl) null)));

        //then
        assertThat(index).isEqualTo(-1);
    }

    @Test
    public void findDependencyIndex_should_return_zero_if_expression_is_dependency_of_first_operation() throws Exception {
        //given
        String dataName1 = "address1";
        String dataName2 = "address2";
        ExpressionType type = ExpressionType.TYPE_BUSINESS_DATA;
        List<SExpression> dependencies = Collections.emptyList();
        SExpressionImpl rightOperand1 = buildExpression(dataName1, type, dependencies);
        SExpressionImpl rightOperand2 = buildExpression(dataName2, type, dependencies);
        List<SOperation> operations = Arrays.<SOperation> asList(buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, rightOperand1),
                buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, rightOperand2));

        //when
        int index = dependencyFinder.findBusinessDataDependencyIndex(dataName1, 0, operations);

        //then
        assertThat(index).isEqualTo(0);
    }

    @Test
    public void findDependencyIndex_should_return_lastIndex_if_expression_is_dependency_of_last_operation() throws Exception {
        //given
        String dataName1 = "address1";
        String dataName2 = "address2";
        ExpressionType type = ExpressionType.TYPE_BUSINESS_DATA;
        List<SExpression> dependencies = Collections.emptyList();
        SExpressionImpl rightOperand1 = buildExpression(dataName1, type, dependencies);
        SExpressionImpl rightOperand2 = buildExpression(dataName2, type, dependencies);
        List<SOperation> operations = Arrays.<SOperation> asList(buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, rightOperand1),
                buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, rightOperand2));

        //when
        int index = dependencyFinder.findBusinessDataDependencyIndex(dataName2, 0, operations);

        //then
        assertThat(index).isEqualTo(1);
    }

    @Test
    public void findDependencyIndex_should_return_minus_one_if_find_expression_with_same_name_but_different_type() throws Exception {
        //given
        String dataName1 = "address1";
        List<SExpression> dependencies = Collections.emptyList();
        SExpressionImpl rightOperand1 = buildExpression(dataName1, ExpressionType.TYPE_CONDITION, dependencies);
        List<SOperation> operations = Arrays.<SOperation> asList(buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, rightOperand1));

        //when
        int index = dependencyFinder.findBusinessDataDependencyIndex(dataName1, 0, operations);

        //then
        assertThat(index).isEqualTo(-1);
    }

    @Test
    public void findDependencyIndex_should_return_minus_one_if_expression_is_dependency_at_a_index_before_fromIndex() throws Exception {
        //given
        String dataName1 = "address1";
        String dataName2 = "address2";
        ExpressionType type = ExpressionType.TYPE_BUSINESS_DATA;
        List<SExpression> dependencies = Collections.emptyList();
        SExpressionImpl rightOperand1 = buildExpression(dataName1, type, dependencies);
        SExpressionImpl rightOperand2 = buildExpression(dataName2, type, dependencies);
        List<SOperation> operations = Arrays.<SOperation> asList(buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, rightOperand1),
                buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, rightOperand2));

        //when
        int index = dependencyFinder.findBusinessDataDependencyIndex(dataName1, 1, operations);

        //then
        assertThat(index).isEqualTo(-1);
    }

    @Test
    public void findDependencyIndex_should_look_at_first_dependency_level() throws Exception {
        //given
        String dataName1 = "address1";
        ExpressionType type = ExpressionType.TYPE_BUSINESS_DATA;
        SExpressionImpl dataExpression = buildExpression(dataName1, type, Collections.<SExpression> emptyList());
        SExpressionImpl rightOperand = buildExpression("myScript", ExpressionType.TYPE_READ_ONLY_SCRIPT,
                Collections.<SExpression> singletonList(dataExpression));
        List<SOperation> operations = Arrays.<SOperation> asList(buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, rightOperand));

        //when
        int index = dependencyFinder.findBusinessDataDependencyIndex(dataName1, 0, operations);

        //then
        assertThat(index).isEqualTo(0);
    }

    @Test
    public void findDependencyIndex_should_look_at_all_dependency_levels() throws Exception {
        //given
        String dataName1 = "address1";
        ExpressionType type = ExpressionType.TYPE_BUSINESS_DATA;
        SExpressionImpl dataExpression = buildExpression(dataName1, type, Collections.<SExpression> emptyList());
        SExpressionImpl dependencyL11 = buildExpression("dep11", type, Collections.<SExpression> emptyList());
        SExpressionImpl dependencyL12 = buildExpression("dep12", ExpressionType.TYPE_READ_ONLY_SCRIPT, Collections.<SExpression> singletonList(dataExpression));
        SExpressionImpl rightOperand = buildExpression("myScript", ExpressionType.TYPE_READ_ONLY_SCRIPT,
                Arrays.<SExpression> asList(dependencyL11, dependencyL12));
        List<SOperation> operations = Arrays.<SOperation> asList(buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, rightOperand));

        //when
        int index = dependencyFinder.findBusinessDataDependencyIndex(dataName1, 0, operations);

        //then
        assertThat(index).isEqualTo(0);
    }

    @Test
    public void calculate_indexes_should_return_positive_values_when_left_operand_is_found() throws Exception {
        //given
        SLeftOperandImpl data1 = buildMockLeftOperand(SLeftOperand.TYPE_BUSINESS_DATA, "data1");
        SOperationImpl op1 = buildMockOperation(data1);
        SOperationImpl op2 = buildMockOperation(data1);
        SOperationImpl op3 = buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, "data2");
        SOperationImpl op4 = buildMockOperation(data1);

        //when
        LeftOperandIndexes indexes = dependencyFinder.calculateIndexes(0, Arrays.<SOperation> asList(op1, op2, op3, op4));

        //then
        assertThat(indexes.getNextIndex()).isEqualTo(1);
        assertThat(indexes.getLastIndex()).isEqualTo(3);
    }

    @Test
    public void calculate_indexes_should_return_negative_value_for_nextIndex_when_is_last() throws Exception {
        //given
        SLeftOperandImpl data1 = buildMockLeftOperand(SLeftOperand.TYPE_BUSINESS_DATA, "data1");
        SOperationImpl op1 = buildMockOperation(data1);
        SOperationImpl op2 = buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, "data2");

        //when
        LeftOperandIndexes indexes = dependencyFinder.calculateIndexes(1, Arrays.<SOperation> asList(op1, op2));

        //then
        assertThat(indexes.getNextIndex()).isEqualTo(-1);
        assertThat(indexes.getLastIndex()).isEqualTo(1);
    }

}
