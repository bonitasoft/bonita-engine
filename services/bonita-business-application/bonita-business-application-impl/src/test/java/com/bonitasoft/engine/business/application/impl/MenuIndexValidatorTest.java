/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

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
