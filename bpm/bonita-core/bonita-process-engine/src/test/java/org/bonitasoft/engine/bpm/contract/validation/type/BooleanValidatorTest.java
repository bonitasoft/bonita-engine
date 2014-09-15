/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.contract.validation.type;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class BooleanValidatorTest {

    private BooleanValidator validator;

    @Before
    public void setUp() {
        validator = new BooleanValidator();
    }

    @Test
    public void boolean_are_valid() throws Exception {

        boolean validation = validator.validate(true);

        assertThat(validation).isTrue();
    }

    @Test
    public void Boolean_are_valid() throws Exception {

        boolean validation = validator.validate(Boolean.FALSE);

        assertThat(validation).isTrue();
    }
    
    @Test
    public void null_is_valid() throws Exception {

        boolean validation = validator.validate(null);

        assertThat(validation).isTrue();
    }
    
    @Test
    public void other_type_are_not_valid() throws Exception {

        boolean integerValidation = validator.validate(12);
        assertThat(integerValidation).isFalse();
        
        boolean stringValidation = validator.validate("false");
        assertThat(stringValidation).isFalse();
    }
}
