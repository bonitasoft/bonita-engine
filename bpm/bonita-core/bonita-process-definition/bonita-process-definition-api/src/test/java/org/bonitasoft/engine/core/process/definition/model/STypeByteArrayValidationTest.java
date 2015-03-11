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

import java.util.ArrayList;

import org.junit.Test;

public class STypeByteArrayValidationTest {

    @Test
    public void byte_array_is_valid() throws Exception {
        final boolean validation = SType.BYTE_ARRAY.validate(new byte[] { 0, 1, 0, 0, 1, 0, 1 });

        assertThat(validation).isTrue();
    }

    @Test
    public void Byte_array_is_invalid() throws Exception {
        final boolean validation = SType.BYTE_ARRAY.validate(new Byte[0]);

        assertThat(validation).isFalse();
    }

    @Test
    public void Byte_list_is_invalid() throws Exception {
        final boolean validation = SType.BYTE_ARRAY.validate(new ArrayList<Byte>());

        assertThat(validation).isFalse();
    }

    @Test
    public void null_is_valid() throws Exception {
        final boolean validation = SType.BYTE_ARRAY.validate(null);

        assertThat(validation).isTrue();
    }

}
