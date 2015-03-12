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

import java.math.BigInteger;

import org.junit.Test;

public class STypeIntegerValidationTest {

    @Test
    public void integer_are_valid() throws Exception {

        boolean validation = SType.INTEGER.validate(12);

        assertThat(validation).isTrue();
    }

    @Test
    public void Integer_are_valid() throws Exception {

        boolean validation = SType.INTEGER.validate(Integer.valueOf(12));

        assertThat(validation).isTrue();
    }

    @Test
    public void long_are_valid() throws Exception {

        boolean validation = SType.INTEGER.validate(12l);

        assertThat(validation).isTrue();
    }

    @Test
    public void Long_are_valid() throws Exception {

        boolean validation = SType.INTEGER.validate(Long.valueOf(12));

        assertThat(validation).isTrue();
    }

    @Test
    public void BigInteger_are_valid() throws Exception {

        boolean validation = SType.INTEGER.validate(BigInteger.valueOf(45));

        assertThat(validation).isTrue();
    }

    @Test
    public void short_are_valid() throws Exception {

        boolean validation = SType.INTEGER.validate((short) 65);

        assertThat(validation).isTrue();
    }

    @Test
    public void Short_are_valid() throws Exception {

        boolean validation = SType.INTEGER.validate(Short.valueOf((short) 87));

        assertThat(validation).isTrue();
    }

    @Test
    public void byte_are_valid() throws Exception {

        boolean validation = SType.INTEGER.validate((byte) 8);

        assertThat(validation).isTrue();
    }

    @Test
    public void Byte_are_valid() throws Exception {

        boolean validation = SType.INTEGER.validate(Byte.valueOf((byte) 2));

        assertThat(validation).isTrue();
    }

    @Test
    public void null_is_valid() throws Exception {

        boolean validation = SType.INTEGER.validate(null);

        assertThat(validation).isTrue();
    }

    @Test
    public void other_types_are_not_valid() throws Exception {

        boolean stringValidation = SType.INTEGER.validate("54");
        assertThat(stringValidation).isFalse();

        boolean doubleValidation = SType.INTEGER.validate(53.2d);
        assertThat(doubleValidation).isFalse();

        boolean booleanValidation = SType.INTEGER.validate(true);
        assertThat(booleanValidation).isFalse();
    }

}
