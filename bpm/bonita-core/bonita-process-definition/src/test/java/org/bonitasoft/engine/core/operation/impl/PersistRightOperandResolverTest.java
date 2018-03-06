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
import static org.bonitasoft.engine.core.operation.impl.OperationMockBuilder.buildMockOperation;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.impl.SOperationImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PersistRightOperandResolverTest {

    @Mock
    private OperationsAnalyzer operationsAnalyzer;

    @InjectMocks
    private PersistRightOperandResolver resolver;

    @Test
    public void should_not_persist_if_not_business_data() throws Exception {
        //when
        boolean persist = resolver.shouldPersistByPosition(0, Arrays.<SOperation>asList(buildMockOperation(SLeftOperand.TYPE_DATA)));

        //then
        assertThat(persist).isFalse();
    }

    @Test
    public void should_persist_bo_if_is_last_operation_for_this_left_operand() throws Exception {
        //given
        SOperationImpl addressOp1 = buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, "address");
        SOperationImpl employeeOp1 = buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, "employee");
        SOperationImpl addressOp2 = buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, "address");

        List<SOperation> operations = Arrays.<SOperation> asList(addressOp1, employeeOp1, addressOp2);
        LeftOperandIndexes indexes = new LeftOperandIndexes();
        indexes.setLastIndex(2);
        given(operationsAnalyzer.calculateIndexes(2, operations)).willReturn(indexes);

        //when
        boolean persist = resolver.shouldPersistByPosition(2, operations);

        //then
        assertThat(persist).isTrue();
        verify(operationsAnalyzer, never()).findBusinessDataDependencyIndex(anyString(), anyInt(), anyList());

    }

    @Test
    public void should_persist_bo_when_next_operation_on_this_left_operand_is_after_next_usage_as_dependency() throws Exception {
        //given
        SOperationImpl addressOp1 = buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, "address");
        SOperationImpl employeeOp1 = buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, "employee");
        SOperationImpl addressOp2 = buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, "address");
        SOperationImpl employeeOp2 = buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, "employee");

        List<SOperation> operations = Arrays.<SOperation> asList(addressOp1, employeeOp1, addressOp2, employeeOp2);

        int currentIndex = 0;
        given(operationsAnalyzer.findBusinessDataDependencyIndex("address", currentIndex + 1, operations)).willReturn(1);
        LeftOperandIndexes indexes = new LeftOperandIndexes();
        indexes.setNextIndex(2);
        given(operationsAnalyzer.calculateIndexes(0, operations)).willReturn(indexes);

        //when
        boolean persist = resolver.shouldPersistByPosition(currentIndex, operations);

        //then
        assertThat(persist).isTrue();
    }

    @Test
    public void should_not_persist_bo_when_is_not_last_operation_for_this_left_operand_neither_is_dependency() throws Exception {
        //given
        SOperationImpl addressOp1 = buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, "address");
        SOperationImpl employeeOp1 = buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, "employee");
        SOperationImpl addressOp2 = buildMockOperation(SLeftOperand.TYPE_BUSINESS_DATA, "address");

        int indexOfCurrentOperation = 0;
        List<SOperation> operations = Arrays.<SOperation> asList(addressOp1, employeeOp1, addressOp2);
        LeftOperandIndexes indexes = new LeftOperandIndexes();
        indexes.setLastIndex(2);
        indexes.setNextIndex(2);
        given(operationsAnalyzer.calculateIndexes(indexOfCurrentOperation, operations)).willReturn(indexes);

        given(operationsAnalyzer.findBusinessDataDependencyIndex("address", indexOfCurrentOperation + 1, operations)).willReturn(-1);

        //when
        boolean persist = resolver.shouldPersistByPosition(indexOfCurrentOperation, operations);

        //then
        assertThat(persist).isFalse();

    }

    @Test
    public void should_not_persist_when_right_operand_is_null_and_should_not_persist_on_null() throws Exception {
        //then
        assertThat(resolver.shouldPersistByValue(null, true, false)).isFalse();
    }

    @Test
    public void should_not_persist_when_right_operand_is_null_and_should_not_persist_by_position() throws Exception {
        //then
        assertThat(resolver.shouldPersistByValue(null, false, true)).isFalse();
    }

    @Test
    public void should_persist_when_right_operand_is_null_and_should_not_persist_by_position_and_on_null() throws Exception {
        //then
        assertThat(resolver.shouldPersistByValue(null, true, true)).isTrue();
    }

    @Test
    public void should_persist_if_right_operand_is_not_null_and_should_persist_by_position() throws Exception {
        //then
        assertThat(resolver.shouldPersistByValue("anyNotNullValue", true, true)).isTrue();
    }

    @Test
    public void should_not_persist_if_right_operand_is_not_null_and_should_not_persist_by_position() throws Exception {
        //then
        assertThat(resolver.shouldPersistByValue("anyNotNullValue", false, true)).isFalse();
    }
}
