/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator;

import static com.bonitasoft.engine.bdm.validator.assertion.ValidationStatusAssert.assertThat;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.model.NamedElement;

public class UniqueNameValidatorTest {

    private UniqueNameValidator validator;

    @Before
    public void initValidator() {
        validator = new UniqueNameValidator();
    }

    @Test
    public void should_validate_a_list_with_no_duplicated_names() {
        List<NamedElement> listWithNoDuplicatedNames = asList(aNamedElement("aName"), aNamedElement("anOtherName"), aNamedElement("yetDifferentName"));

        ValidationStatus status = validator.validate(listWithNoDuplicatedNames, "named elements");

        assertThat(status).isOk();
    }

    @Test
    public void should_not_validate_a_list_duplicated_names() {
        String duplicatedName = "duplicatedName";
        List<NamedElement> listWithNoDuplicatedNames = asList(aNamedElement("notDuplicatedName"), aNamedElement(duplicatedName), aNamedElement(duplicatedName),
                aNamedElement(duplicatedName));

        ValidationStatus status = validator.validate(listWithNoDuplicatedNames, "named elements");

        assertThat(status).isNotOk();
        assertThat(status.getErrors()).hasSize(1);
    }

    public NamedElement aNamedElement(String name) {
        return new FakeNamedElement(name);
    }

    public static class FakeNamedElement implements NamedElement {

        private final String name;

        public FakeNamedElement(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
