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

import static org.bonitasoft.engine.business.application.importer.validator.ValidationStatusAssert.assertThat;

import org.junit.Test;

public class ValidationStatusTest {

    @Test
    public void should_create_valid_status_with_no_message() throws Exception {
        //given
        ValidationStatus status = new ValidationStatus(true);

        //then
        assertThat(status).isValid().hasMessage(null);
    }

    @Test
    public void should_create_invalid_status_with_no_message() throws Exception {
        //given
        ValidationStatus status = new ValidationStatus(false);

        //then
        assertThat(status).isNotValid().hasMessage(null);
    }

    @Test
    public void should_create_invalid_status_with_message() throws Exception {
        //given
        ValidationStatus status = new ValidationStatus(false, "'content' is a reserved keyword");

        //then
        assertThat(status).isNotValid().hasMessage("'content' is a reserved keyword");
    }

}
