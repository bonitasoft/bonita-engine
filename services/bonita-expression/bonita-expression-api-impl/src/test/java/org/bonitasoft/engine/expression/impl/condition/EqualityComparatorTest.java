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

public class EqualityComparatorTest {

    private EqualityComparator comparator;

    @Before
    public void setUp() throws Exception {
        comparator = new EqualityComparator();
    }

    @Test
    public void areEquals_should_return_true_when_two_parameters_are_null() throws Exception {
        //given

        //when
        Boolean equals = comparator.areEquals(null, null);

        //then
        assertThat(equals).isTrue();
    }

    @Test
    public void areEquals_should_return_false_when_first_parameter_is_null_and_second_one_is_not_null() throws Exception {
        //given

        //when
        Boolean equals = comparator.areEquals(null, "not null");

        //then
        assertThat(equals).isFalse();
    }

    @Test
    public void areEquals_should_return_false_when_first_parameter_is_not_null_and_second_one_is_null() throws Exception {
        //given

        //when
        Boolean equals = comparator.areEquals("not null", null);

        //then
        assertThat(equals).isFalse();
    }

    @Test
    public void areEquals_should_return_true_when_two_parameters_are_equals() throws Exception {
        //given

        //when
        Boolean equals = comparator.areEquals("equal", "equal");

        //then
        assertThat(equals).isTrue();
    }

    @Test
    public void areEquals_should_return_false_when_two_parameters_are_different() throws Exception {
        //given

        //when
        Boolean equals = comparator.areEquals("not null", "not null, but different");

        //then
        assertThat(equals).isFalse();
    }

    @Test
    public void can_compare_different_types() throws Exception {
        //given

        //when
        Boolean equals = comparator.areEquals("not null", 5);

        //then
        assertThat(equals).isFalse();
    }

}
