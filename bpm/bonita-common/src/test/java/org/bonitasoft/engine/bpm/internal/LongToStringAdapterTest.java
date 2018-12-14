/*
 * Copyright (C) 2016 Bonitasoft S.A.
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
package org.bonitasoft.engine.bpm.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 */
public class LongToStringAdapterTest {

    @Test
    public void marshall_should_add_underscore_when_converting_to_string() throws Exception {
        // given:
        final LongToStringAdapter longToStringAdapter = new LongToStringAdapter();

        // when:
        final String marshal = longToStringAdapter.marshal(154231L);

        // then:
        assertThat(marshal).isEqualTo("_154231");
    }

    @Test
    public void unmarshall_should_remove_underscore_when_converting_to_long() throws Exception {
        // given:
        final LongToStringAdapter longToStringAdapter = new LongToStringAdapter();

        // when:
        final Long myLongId = longToStringAdapter.unmarshal("_998877");

        // then:
        assertThat(myLongId).isEqualTo(998877L);
    }

    @Test
    public void unmarshall_should_not_remove_first_char_if_not_underscore() throws Exception {
        // given:
        final LongToStringAdapter longToStringAdapter = new LongToStringAdapter();

        // when:
        final Long myLongId = longToStringAdapter.unmarshal("118877");

        // then:
        assertThat(myLongId).isEqualTo(118877L);
    }
}
