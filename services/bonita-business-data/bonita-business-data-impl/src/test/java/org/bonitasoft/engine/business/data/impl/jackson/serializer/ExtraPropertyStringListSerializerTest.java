/**
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
 **/

package org.bonitasoft.engine.business.data.impl.jackson.serializer;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

public class ExtraPropertyStringListSerializerTest {

    private ExtraPropertyStringListSerializer serializer = new ExtraPropertyStringListSerializer();

    @Test
    public void should_convert_null_list_to_empty() throws Exception {
        assertThat((List<String>) serializer.convert(null)).isEmpty();
    }

    @Test
    public void should_convert_list_to_string() throws Exception {
        assertThat((List<String>) serializer.convert(asList(1L, 2L))).containsExactly("1", "2");
    }

    @Test
    public void should_convert_list_with_null_elements() throws Exception {
        assertThat((List<String>) serializer.convert(asList(1L, null, 2L))).containsExactly("1", null, "2");
    }

}
