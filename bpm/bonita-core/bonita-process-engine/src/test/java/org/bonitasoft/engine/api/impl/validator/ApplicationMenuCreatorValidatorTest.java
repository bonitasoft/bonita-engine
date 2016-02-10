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
package org.bonitasoft.engine.api.impl.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.business.application.ApplicationMenuCreator;
import org.junit.Test;



public class ApplicationMenuCreatorValidatorTest {

    private final ApplicationMenuCreatorValidator validator = new ApplicationMenuCreatorValidator();

    @Test
    public void isValid_should_return_true_if_all_mandatory_fields_are_filled() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator(5L, "main");

        //when
        final boolean valid = validator.isValid(creator);

        //then
        assertThat(valid).isTrue();
    }

    @Test
    public void isValid_should_return_false_if_applicationId_is_null() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator(null, "main");

        //when
        final boolean valid = validator.isValid(creator);

        //then
        assertThat(valid).isFalse();
        assertThat(validator.getProblems()).containsExactly("The applicationId cannot be null");
    }

}
