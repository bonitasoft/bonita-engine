/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.rest.server.QueryParameterUtils.parseFilters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class QueryParameterUtilsTest {

    @Test
    void parseFilterShouldBuildExpectedMap() {
        // given:
        final List<String> filters = Arrays.asList("toto=17", "titi='EN_ECHEC'", "task=task=with=equal=in=name");

        // when:
        final Map<String, String> parseFilters = parseFilters(filters);

        // then:
        assertThat(parseFilters).hasSize(3);
        assertThat(parseFilters.get("toto")).isEqualTo("17");
        assertThat(parseFilters.get("titi")).isEqualTo("'EN_ECHEC'");
        assertThat(parseFilters.get("task")).isEqualTo("task=with=equal=in=name");
    }

    @Test
    void parseFilterWithSpecialCharactersShouldBuildExpectedMap() {
        // given:
        final List<String> filters = Arrays.asList("a=b", "c=/d/d,e");

        // when:
        final Map<String, String> parseFilters = parseFilters(filters);

        // then:
        assertThat(parseFilters).hasSize(2);
        assertThat(parseFilters.get("a")).isEqualTo("b");
        assertThat(parseFilters.get("c")).isEqualTo("/d/d,e");
    }

    @Test
    void parseFilterShouldBuildMapEvenIfNoValueForParam() {
        // given:
        final List<String> filters = new ArrayList<>(2);
        filters.add("nomatchingvalue=");

        // when:
        final Map<String, String> parseFilters = parseFilters(filters);

        // then:
        assertThat(parseFilters).hasSize(1);
        assertThat(parseFilters.get("nomatchingvalue")).isNull();
    }

    @Test
    public void parseFilterShouldNotFailIfParameterHasNoName() {
        assertThat(parseFilters(List.of("="))).isEmpty();
    }

    @Test
    public void parseFilterShouldReturnNullIfListIsNull() {
        assertThat(parseFilters(null)).isNull();
    }

}
