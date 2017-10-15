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

import java.time.LocalDate;

import org.junit.Test;

/**
 * @author Danila Mazour
 */
public class STypeLocalDateTest {
    
    @Test
    public void localDate_are_valid() throws Exception {

        boolean validation = SType.LOCALDATE.validate(LocalDate.now());

        assertThat(validation).isTrue();
    }
    
    @Test
    public void null_is_valid() throws Exception {

        boolean validation = SType.LOCALDATE.validate(null);

        assertThat(validation).isTrue();
    }

    @Test
    public void other_type_are_not_valid() throws Exception {

        boolean integerValidation = SType.LOCALDATE.validate(12);
        assertThat(integerValidation).isFalse();

        boolean stringValidation = SType.LOCALDATE.validate("false");
        assertThat(stringValidation).isFalse();
    }
    
}