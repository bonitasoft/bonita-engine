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
package org.bonitasoft.engine.business.application.importer.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.bonitasoft.engine.business.application.ApplicationMenuCreator;
import org.junit.Test;

public class ApplicationMenuCreatorValidatorTest {

    private final ApplicationMenuCreatorValidator validator = new ApplicationMenuCreatorValidator();

    @Test
    public void isValid_should_return_empty_list_if_all_mandatory_fields_are_filled() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator(5L, "main");

        //when
        assertThat(validator.isValid(creator)).isEmpty();
    }

    @Test
    public void isValid_should_return_errors_if_applicationId_is_null() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator(null, "main");

        //when
        List<String> errors = validator.isValid(creator);

        //then
        assertThat(errors).isNotEmpty();
        assertThat(errors).containsExactly("The applicationId cannot be null");
    }

}
