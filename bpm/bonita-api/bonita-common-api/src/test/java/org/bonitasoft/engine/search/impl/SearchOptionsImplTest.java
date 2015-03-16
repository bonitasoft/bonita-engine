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
package org.bonitasoft.engine.search.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SearchOptionsImplTest {

    @Test
    public void equals_should_check_the_whole_objects() {
        final SearchOptionsImpl options1 = buildSearchOptions();
        final SearchOptionsImpl options2 = buildSearchOptions();

        assertThat(options1).isEqualTo(options2);
    }

    @Test
    public void hashCode_should_check_the_whole_objects() {
        final SearchOptionsImpl options1 = buildSearchOptions();
        final SearchOptionsImpl options2 = buildSearchOptions();

        assertThat(options1.hashCode()).isEqualTo(options2.hashCode());
    }

    private SearchOptionsImpl buildSearchOptions() {
        final SearchOptionsImpl options = new SearchOptionsImpl(0, 2000);
        options.addFilter("field1", "value");
        options.addFilter("field3", 8);
        options.addFilter("field2", true);
        return options;
    }

}
