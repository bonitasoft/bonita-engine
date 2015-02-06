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
package org.bonitasoft.engine.business.application.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.bonitasoft.engine.business.application.impl.MenuIndex;
import org.bonitasoft.engine.business.application.impl.MenuIndexValidator;
import org.junit.Test;

public class MenuIndexValidatorTest {

    private MenuIndexValidator validator = new MenuIndexValidator();

    @Test
    public void isValid_should_return_problem_when_new_index_is_less_than_or_equal_zero() throws Exception {
        //given
        MenuIndex oldIndex = new MenuIndex(null, 2, 5);
        MenuIndex newIndex = new MenuIndex(null, 0, 5);

        //when
        List<String> problems = validator.validate(oldIndex, newIndex);

        //then
        assertThat(problems)
                .containsExactly(
                        "Invalid menu index: 0. It must be between 1 and the number of menu in your application having the same parent. The last valid index for parent null is 5");
    }

    @Test
    public void isValid_should_return_problem_when_parent_is_same_and_new_index_is_greater_than_last_used_index() throws Exception {
        //given
        MenuIndex oldIndex = new MenuIndex(null, 5, 5);
        MenuIndex newIndex = new MenuIndex(null, 6, 5);

        //when
        List<String> problems = validator.validate(oldIndex, newIndex);

        //then
        assertThat(problems)
                .containsExactly(
                        "Invalid menu index: 6. It must be between 1 and the number of menu in your application having the same parent. The last valid index for parent null is 5");
    }

    @Test
    public void isValid_should_return_no_problems_when_parent_is_not_the_same_and_new_index_is_greater_than_last_used_index_by_one() throws Exception {
        //given
        MenuIndex oldIndex = new MenuIndex(null, 5, 5);
        MenuIndex newIndex = new MenuIndex(2L, 4, 3);

        //when
        List<String> problems = validator.validate(oldIndex, newIndex);

        //then
        assertThat(problems).isEmpty();
    }

    @Test
    public void isValid_should_return_problem_when_parent_is_not_the_same_and_new_index_is_greater_than_last_used_index_by_more_than_one() throws Exception {
        //given
        MenuIndex oldIndex = new MenuIndex(null, 5, 5);
        MenuIndex newIndex = new MenuIndex(2L, 5, 3);

        //when
        List<String> problems = validator.validate(oldIndex, newIndex);

        //then
        assertThat(problems).containsExactly(
                "Invalid menu index: 5. It must be between 1 and the number of menu in your application having the same parent. The last valid index for parent 2 is 4");
    }

    @Test
    public void isValid_should_return_true_when_new_index_is_valid() throws Exception {
        //given
        MenuIndex oldIndex = new MenuIndex(null, 5, 5);
        MenuIndex newIndex = new MenuIndex(null, 4, 5);

        //when
        List<String> problems = validator.validate(oldIndex, newIndex);

        //then
        assertThat(problems).isEmpty();
    }

}
