/*
 * Copyright (C) 2017 Bonitasoft S.A.
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
 */

package org.bonitasoft.engine.core.process.definition.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Test;

/**
 * @author Danila Mazour
 */
public class STypeLocalDateTimeTest {
    
    @Test
    public void localDateTime_are_valid() throws Exception {

        boolean validation = SType.LOCALDATETIME.validate(LocalDateTime.now());

        assertThat(validation).isTrue();
    }
    
    @Test
    public void null_is_valid() throws Exception {

        boolean validation = SType.LOCALDATETIME.validate(null);

        assertThat(validation).isTrue();
    }

    @Test
    public void other_type_are_not_valid() throws Exception {

        boolean integerValidation = SType.LOCALDATETIME.validate(12);
        assertThat(integerValidation).isFalse();

        boolean stringValidation = SType.LOCALDATETIME.validate("false");
        assertThat(stringValidation).isFalse();
    }
}