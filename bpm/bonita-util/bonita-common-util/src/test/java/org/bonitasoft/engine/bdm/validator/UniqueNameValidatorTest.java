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
package org.bonitasoft.engine.bdm.validator;

import static org.bonitasoft.engine.bdm.validator.assertion.ValidationStatusAssert.assertThat;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.bonitasoft.engine.bdm.model.NamedElement;

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
