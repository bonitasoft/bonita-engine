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

public class InequalityComparatorTest {

    private InequalityComparator comparator;

    @Before
    public void setUp() throws Exception {
        comparator = new InequalityComparator();
    }

    @Test
    public void compareTo_should_return_positive_integer_when_left_is_greater_than_right() throws Exception {
        //given

        //when
        Integer value = comparator.compareTo(5, 4);

        //then
        assertThat(value).isGreaterThan(0);
    }

    @Test
    public void compareTo_should_return_negative_integer_when_left_is_greater_than_right() throws Exception {
        //given

        //when
        Integer value = comparator.compareTo(3, 4);

        //then
        assertThat(value).isLessThan(0);
    }

    @Test
    public void compareTo_should_return_null_when_first_parameter_is_null() throws Exception {
        //given

        //when
        Integer value = comparator.compareTo(null, 4);

        //then
        assertThat(value).isNull();
    }

    @Test
    public void compareTo_should_return_null_when_second_parameter_is_null() throws Exception {
        //given

        //when
        Integer value = comparator.compareTo(3, null);

        //then
        assertThat(value).isNull();
    }

    @Test(expected = SComparisonException.class)
    public void compareTo_should_return_throw_SComparisonException_when_parameters_does_not_implement_comparable() throws Exception {
        //given

        //when
        comparator.compareTo(new NotComparableClass(), new NotComparableClass());

        //then exception
    }

}
