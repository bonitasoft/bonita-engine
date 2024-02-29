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
package org.bonitasoft.engine.expression.impl.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LessThanComparatorTest {

    @Mock
    private InequalityComparator comparator;

    @InjectMocks
    private LessThanComparator evaluator;

    @Test
    public void evaluate_should_return_true_when_compare_is_less_than_zero() throws Exception {
        //given
        given(comparator.compareTo("a", "b")).willReturn(-1);

        //when
        Boolean value = evaluator.evaluate("a", "b");

        //then
        assertThat(value).isTrue();
    }

    @Test
    public void evaluate_should_return_false_when_compare_is_zero() throws Exception {
        //given
        given(comparator.compareTo("a", "b")).willReturn(0);

        //when
        Boolean value = evaluator.evaluate("a", "b");

        //then
        assertThat(value).isFalse();
    }

    @Test
    public void evaluate_should_return_false_when_compare_is_greater_than_zero() throws Exception {
        //given
        given(comparator.compareTo("a", "b")).willReturn(1);

        //when
        Boolean value = evaluator.evaluate("a", "b");

        //then
        assertThat(value).isFalse();
    }

    @Test
    public void evaluate_should_return_null_when_compare_is_null() throws Exception {
        //given
        given(comparator.compareTo("a", "b")).willReturn(null);

        //when
        Boolean value = evaluator.evaluate("a", "b");

        //then
        assertThat(value).isNull();
    }

}
