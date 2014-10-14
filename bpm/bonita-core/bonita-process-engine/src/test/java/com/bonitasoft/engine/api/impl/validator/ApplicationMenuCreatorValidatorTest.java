package com.bonitasoft.engine.api.impl.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.bonitasoft.engine.business.application.ApplicationMenuCreator;



public class ApplicationMenuCreatorValidatorTest {

    private final ApplicationMenuCreatorValidator validator = new ApplicationMenuCreatorValidator();

    @Test
    public void isValid_should_return_true_if_all_mandatory_fields_are_filled() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator(5L, "main", 1);

        //when
        final boolean valid = validator.isValid(creator);

        //then
        assertThat(valid).isTrue();
    }

    @Test
    public void isValid_should_return_false_if_applicationId_is_null() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator(null, "main", 1);

        //when
        final boolean valid = validator.isValid(creator);

        //then
        assertThat(valid).isFalse();
        assertThat(validator.getProblems()).containsExactly("The applicationId cannot be null");
    }

}
