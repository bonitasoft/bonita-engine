/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.persistence;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Arrays;
import java.util.Collections;

import org.bonitasoft.engine.persistence.search.FilterOperationType;
import org.junit.Test;

public class QueryGeneratorForFiltersTest {

    @Test
    public void should_generate_query_with_one_boolean_filter() throws Exception {
        QueryGeneratorForFilters generator = new QueryGeneratorForFilters(
                singletonMap(TestObject.class.getName(), "testObject"), '%');

        QueryGeneratorForFilters.QueryGeneratedFilters whereClause = generator.generate(Collections.singletonList(
                new FilterOption(TestObject.class, "enabled", true, FilterOperationType.EQUALS)));

        assertThat(whereClause.getFilters()).isEqualTo("testObject.enabled = :f1");
        assertThat(whereClause.getParameters()).containsOnly(entry("f1", true));
        assertThat(whereClause.getSpecificFilters()).containsOnly("testObject.enabled");
    }

    @Test
    public void should_generate_query_with_multiple_filters() throws Exception {
        QueryGeneratorForFilters generator = new QueryGeneratorForFilters(
                singletonMap(TestObject.class.getName(), "testObject"), '%');

        QueryGeneratorForFilters.QueryGeneratedFilters whereClause = generator.generate(Arrays.asList(
                new FilterOption(TestObject.class, "enabled", true, FilterOperationType.EQUALS),
                new FilterOption(TestObject.class, "name", "john", FilterOperationType.EQUALS)));

        assertThat(whereClause.getFilters()).isEqualTo("testObject.enabled = :f1 AND testObject.name = :f2");
        assertThat(whereClause.getParameters()).containsOnly(entry("f1", true), entry("f2", "john"));
        assertThat(whereClause.getSpecificFilters()).containsOnly("testObject.enabled", "testObject.name");
    }

}
