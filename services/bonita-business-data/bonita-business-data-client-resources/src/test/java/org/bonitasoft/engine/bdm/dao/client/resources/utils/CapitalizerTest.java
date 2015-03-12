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
package org.bonitasoft.engine.bdm.dao.client.resources.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.bdm.dao.client.resources.utils.Capitalizer;
import org.junit.Test;

public class CapitalizerTest {

    @Test
    public void should_capitalize_a_string() {

        String capitalized = Capitalizer.capitalize("uncapitalized");

        assertThat(capitalized).isEqualTo("Uncapitalized");
    }

    @Test
    public void should_do_nothing_for_a_null_string() {
        String capitalized = Capitalizer.capitalize(null);

        assertThat(capitalized).isNull();
    }

    @Test
    public void should_do_nothing_for_an_empty_string() {
        String capitalized = Capitalizer.capitalize("");

        assertThat(capitalized).isEmpty();
    }
}
