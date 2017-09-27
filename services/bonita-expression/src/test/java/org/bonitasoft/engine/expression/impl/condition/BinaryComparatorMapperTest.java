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
package org.bonitasoft.engine.expression.impl.condition;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class BinaryComparatorMapperTest {

    private BinaryComparatorMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new BinaryComparatorMapper(new EqualityComparator(), new InequalityComparator());
    }

    @Test
    public void getEvaluator_for_equals_operator_should_return_EqualsEvaluator() throws Exception {
        //given

        //when
        BinaryComparator evaluator = mapper.getEvaluator("==");

        //then
        assertThat(evaluator).isInstanceOf(EqualsComparator.class);
    }

    @Test
    public void getEvaluator_for_Different_operator_should_return_DifferentEvaluator() throws Exception {
        //given

        //when
        BinaryComparator evaluator = mapper.getEvaluator("!=");

        //then
        assertThat(evaluator).isInstanceOf(DifferentComparator.class);
    }

    @Test
    public void getEvaluator_for_greaterThan_operator_should_return_GreaterThanEvaluator() throws Exception {
        //given

        //when
        BinaryComparator evaluator = mapper.getEvaluator(">");

        //then
        assertThat(evaluator).isInstanceOf(GreaterThanComparator.class);
    }

    @Test
    public void getEvaluator_for_greaterThanOrEquals_operator_should_return_GreaterThanOrEqualsEvaluator() throws Exception {
        //given

        //when
        BinaryComparator evaluator = mapper.getEvaluator(">=");

        //then
        assertThat(evaluator).isInstanceOf(GreaterThanOrEqualsComparator.class);
    }

    @Test
    public void getEvaluator_for_LessThan_operator_should_return_LessThanEvaluator() throws Exception {
        //given

        //when
        BinaryComparator evaluator = mapper.getEvaluator("<");

        //then
        assertThat(evaluator).isInstanceOf(LessThanComparator.class);
    }

    @Test
    public void getEvaluator_for_LessThanOrEquals_operator_should_return_LessThanOrEqualsEvaluator() throws Exception {
        //given

        //when
        BinaryComparator evaluator = mapper.getEvaluator("<=");

        //then
        assertThat(evaluator).isInstanceOf(LessThanOrEqualsComparator.class);
    }

}
