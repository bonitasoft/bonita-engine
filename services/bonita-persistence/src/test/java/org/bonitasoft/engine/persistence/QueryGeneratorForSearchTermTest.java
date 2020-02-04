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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.Test;

public class QueryGeneratorForSearchTermTest {

    @Test
    public void should_getQueryFilters_append_OR_clause_when_wordSearch_is_enabled() {
        QueryGeneratorForSearchTerm generator = new QueryGeneratorForSearchTerm('$');

        String query = generator.generate(Stream.of("field1").collect(toSet()), singletonList("termA"), true);

        assertThat(query).isEqualTo("field1 LIKE 'termA%' ESCAPE '$' OR field1 LIKE '% termA%' ESCAPE '$'");
    }

    @Test
    public void should_getQueryFilters_append_OR_clause_when_wordSearch_is_not_enabled() {
        QueryGeneratorForSearchTerm generator = new QueryGeneratorForSearchTerm('%');

        String query = generator.generate(Stream.of("field1", "field2").collect(toSet()), asList("termA", "termB"),
                false);

        assertThat(query).isEqualTo(
                "field1 LIKE 'termA%' ESCAPE '%' OR field1 LIKE 'termB%' ESCAPE '%' OR field2 LIKE 'termA%' ESCAPE '%' OR field2 LIKE 'termB%' ESCAPE '%'");
    }

    @Test
    public void should_escape_special_chars() {
        QueryGeneratorForSearchTerm generator = new QueryGeneratorForSearchTerm('@');

        String query = generator.generate(Stream.of("field1").collect(toSet()), singletonList("100%"), false);

        assertThat(query).isEqualTo("field1 LIKE '100@%%' ESCAPE '@'");
    }
}
