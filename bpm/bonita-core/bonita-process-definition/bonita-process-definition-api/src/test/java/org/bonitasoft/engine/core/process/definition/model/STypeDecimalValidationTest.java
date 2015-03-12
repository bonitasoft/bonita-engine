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
package org.bonitasoft.engine.core.process.definition.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.Test;

public class STypeDecimalValidationTest {

    @Test
    public void float_are_valid() throws Exception {

        boolean validation = SType.DECIMAL.validate(45.2f);

        assertThat(validation).isTrue();
    }

    @Test
    public void Float_are_valid() throws Exception {

        boolean validation = SType.DECIMAL.validate(Float.valueOf(47.65f));

        assertThat(validation).isTrue();
    }

    @Test
    public void double_are_valid() throws Exception {

        boolean validation = SType.DECIMAL.validate(5684.23d);

        assertThat(validation).isTrue();
    }

    @Test
    public void Double_are_valid() throws Exception {

        boolean validation = SType.DECIMAL.validate(Double.valueOf(6548.236d));

        assertThat(validation).isTrue();
    }

    @Test
    public void null_is_valid() throws Exception {

        boolean validation = SType.DECIMAL.validate(null);

        assertThat(validation).isTrue();
    }

    @Test
    public void BigDecimal_are_valid() throws Exception {

        boolean validation = SType.DECIMAL.validate(BigDecimal.valueOf(1235.321d));

        assertThat(validation).isTrue();
    }

    @Test
    public void Integer_are_valid() throws Exception {

        boolean intValidation = SType.DECIMAL.validate(53);

        assertThat(intValidation).isTrue();
    }

    @Test
    public void other_types_are_not_valid() throws Exception {

        boolean stringValidation = SType.DECIMAL.validate("54");
        assertThat(stringValidation).isFalse();

        boolean booleanValidation = SType.DECIMAL.validate(true);
        assertThat(booleanValidation).isFalse();
    }
}
