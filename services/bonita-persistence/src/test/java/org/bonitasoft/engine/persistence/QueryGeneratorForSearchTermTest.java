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
import static org.assertj.core.api.Assertions.entry;

import java.util.stream.Stream;

import org.junit.Test;

public class QueryGeneratorForSearchTermTest {

    @Test
    public void should_getQueryFilters_append_OR_clause_when_wordSearch_is_enabled() {
        QueryGeneratorForSearchTerm generator = new QueryGeneratorForSearchTerm('$');

        QueryGeneratorForSearchTerm.QueryGeneratedSearchTerms query = generator
                .generate(Stream.of("field1").collect(toSet()), singletonList("termA"), true);

        assertThat(query.getSearch()).isEqualTo("field1 LIKE :s1 ESCAPE '$' OR field1 LIKE :s2 ESCAPE '$'");
        assertThat(query.getParameters()).containsOnly(
                entry("s1", "termA%"),
                entry("s2", "% termA%"));
    }

    @Test
    public void should_getQueryFilters_append_OR_clause_when_wordSearch_is_not_enabled() {
        QueryGeneratorForSearchTerm generator = new QueryGeneratorForSearchTerm('%');

        QueryGeneratorForSearchTerm.QueryGeneratedSearchTerms query = generator.generate(
                Stream.of("field1", "field2").collect(toSet()), asList("termA", "termB"),
                false);

        assertThat(query.getSearch()).isEqualTo(
                "field1 LIKE :s1 ESCAPE '%' OR field1 LIKE :s2 ESCAPE '%' OR field2 LIKE :s3 ESCAPE '%' OR field2 LIKE :s4 ESCAPE '%'");
        assertThat(query.getParameters()).containsOnly(
                entry("s1", "termA%"),
                entry("s2", "termB%"),
                entry("s3", "termA%"),
                entry("s4", "termB%"));
    }

    @Test
    public void should_escape_special_chars() {
        QueryGeneratorForSearchTerm generator = new QueryGeneratorForSearchTerm('@');

        QueryGeneratorForSearchTerm.QueryGeneratedSearchTerms query = generator
                .generate(Stream.of("field1").collect(toSet()), singletonList("100%"), false);

        assertThat(query.getSearch()).isEqualTo("field1 LIKE :s1 ESCAPE '@'");
        assertThat(query.getParameters()).containsOnly(
                entry("s1", "100@%%"));
    }
}
