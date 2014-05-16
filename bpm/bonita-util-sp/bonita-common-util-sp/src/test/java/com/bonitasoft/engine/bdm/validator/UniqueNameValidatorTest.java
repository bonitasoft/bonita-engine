package com.bonitasoft.engine.bdm.validator;

import static com.bonitasoft.engine.bdm.validator.UniqueNameValidatorTest.FakeNamedElement.aNamedElement;
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
    public void should_validate_a_list_with_no_duplicated_names() throws Exception {
        List<NamedElement> listWithNoDuplicatedNames = asList(aNamedElement("aName"), aNamedElement("anOtherName"), aNamedElement("yetDifferentName"));

        ValidationStatus status = validator.validate(listWithNoDuplicatedNames, "named elements");

        assertThat(status).isOk();
    }
    
    @Test
    public void should_not_validate_a_list_duplicated_names() throws Exception {
        String duplicatedName = "duplicatedName";
        List<NamedElement> listWithNoDuplicatedNames = asList(aNamedElement("notDuplicatedName"), aNamedElement(duplicatedName), aNamedElement(duplicatedName), aNamedElement(duplicatedName));

        ValidationStatus status = validator.validate(listWithNoDuplicatedNames,"named elements");

        assertThat(status).isNotOk();
        assertThat(status.getErrors()).hasSize(1);
    }

    public static class FakeNamedElement implements NamedElement {

        private String name;

        public static NamedElement aNamedElement(String name) {
            return new FakeNamedElement(name);
        }

        public FakeNamedElement(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
