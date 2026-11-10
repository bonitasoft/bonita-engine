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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.bonitasoft.web.rest.server.QueryParameterUtils.*;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class QueryParameterUtilsTest {

    @Nested
    class ParseFilters {

        @Test
        void should_build_map_from_key_value_pairs() {
            var filters = List.of("id=17", "name='John'",
                    "filter_key_1=value=with=equal=in=value",
                    "filter_key_2=value1,value2,value3",
                    "filter_key_3='value-1','value-2','value-3'");

            assertThat(parseFilters(filters))
                    .hasSize(5)
                    .containsEntry("id", "17")
                    .containsEntry("name", "'John'")
                    .containsEntry("filter_key_1", "value=with=equal=in=value")
                    .containsEntry("filter_key_2", "value1,value2,value3")
                    .containsEntry("filter_key_3", "'value-1','value-2','value-3'");
        }

        @Test
        void should_handle_special_characters_in_values() {
            var filters = List.of("path=/d/d,e");

            assertThat(parseFilters(filters))
                    .hasSize(1)
                    .containsEntry("path", "/d/d,e");
        }

        @Test
        void should_put_null_value_when_filter_has_no_value() {
            var filters = List.of("id=");

            assertThat(parseFilters(filters))
                    .hasSize(1)
                    .containsEntry("id", null);
        }

        @Test
        void should_ignore_filter_with_no_name() {
            assertThat(parseFilters(List.of("="))).isEmpty();
        }

        @Test
        void should_return_null_when_input_is_null() {
            assertThat(parseFilters(null)).isNull();
        }

        @Test
        void should_return_empty_map_for_empty_list() {
            assertThat(parseFilters(List.of())).isEmpty();
        }

        @Test
        void should_put_null_value_when_filter_has_no_equals_sign() {
            var filters = List.of("id");

            assertThat(parseFilters(filters))
                    .hasSize(1)
                    .containsEntry("id", null);
        }

    }

    @Nested
    class ExtractStringFilter {

        @Test
        void should_return_value_when_filter_is_present() {
            var filters = List.of("name=John", "age=30");

            assertThat(extractStringFilter(filters, "name")).isEqualTo("John");
        }

        @Test
        void should_return_null_when_filter_is_not_present() {
            var filters = List.of("other=John");

            assertThat(extractStringFilter(filters, "name")).isNull();
        }

        @Test
        void should_return_null_when_filters_is_null() {
            assertThat(extractStringFilter(null, "name")).isNull();
        }

        @Test
        void should_return_null_when_value_is_empty() {
            var filters = List.of("name=");

            assertThat(extractStringFilter(filters, "name")).isNull();
        }

        @Test
        void should_return_value_containing_equals_sign() {
            var filters = List.of("name=a=b=c");

            assertThat(extractStringFilter(filters, "name")).isEqualTo("a=b=c");
        }

        @Test
        void should_return_first_matching_filter() {
            var filters = List.of("name=first", "name=second");

            assertThat(extractStringFilter(filters, "name")).isEqualTo("first");
        }

        @Test
        void should_return_null_when_filter_has_no_equals_sign() {
            var filters = List.of("name");

            assertThat(extractStringFilter(filters, "name")).isNull();
        }

        @Test
        void should_return_comma_separated_value_as_is() {
            var filters = List.of("name=John,Jane,Bob");

            assertThat(extractStringFilter(filters, "name")).isEqualTo("John,Jane,Bob");
        }
    }

    @Nested
    class ExtractMandatoryStringFilter {

        @Test
        void should_return_value_when_filter_is_present() {
            var filters = List.of("name=John");

            assertThat(extractMandatoryStringFilter(filters, "name")).isEqualTo("John");
        }

        @Test
        void should_throw_when_filter_is_missing() {
            var filters = List.of("other=John");

            assertThatThrownBy(() -> extractMandatoryStringFilter(filters, "name"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("filter name is mandatory");
        }

        @Test
        void should_throw_when_value_is_empty() {
            var filters = List.of("name=");

            assertThatThrownBy(() -> extractMandatoryStringFilter(filters, "name"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("filter name is mandatory");
        }

        @Test
        void should_throw_when_filter_has_no_equals_sign() {
            var filters = List.of("name");

            assertThatThrownBy(() -> extractMandatoryStringFilter(filters, "name"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("filter name is mandatory");
        }

        @Test
        void should_return_comma_separated_value_as_is() {
            var filters = List.of("name=John,Jane,Bob");

            assertThat(extractMandatoryStringFilter(filters, "name")).isEqualTo("John,Jane,Bob");
        }

        @Test
        void should_throw_when_filters_is_null() {
            assertThatThrownBy(() -> extractMandatoryStringFilter(null, "name"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("filter name is mandatory");
        }
    }

    @Nested
    class ExtractLongFilter {

        @Test
        void should_return_value_when_filter_is_a_valid_number() {
            var filters = List.of("id=42");

            assertThat(extractLongFilter(filters, "id")).isEqualTo(42L);
        }

        @Test
        void should_return_null_when_filter_is_not_present() {
            var filters = List.of("other=42");

            assertThat(extractLongFilter(filters, "id")).isNull();
        }

        @Test
        void should_return_null_when_filters_is_null() {
            assertThat(extractLongFilter(null, "id")).isNull();
        }

        @Test
        void should_throw_when_value_is_not_a_number() {
            var filters = List.of("id=abc");

            assertThatThrownBy(() -> extractLongFilter(filters, "id"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("filter id must be a number");
        }

        @Test
        void should_parse_negative_number() {
            var filters = List.of("id=-5");

            assertThat(extractLongFilter(filters, "id")).isEqualTo(-5L);
        }

        @Test
        void should_return_null_when_filter_has_no_equals_sign() {
            var filters = List.of("id");

            assertThat(extractLongFilter(filters, "id")).isNull();
        }

        @Test
        void should_throw_when_value_contains_commas() {
            var filters = List.of("id=1,2,3");

            assertThatThrownBy(() -> extractLongFilter(filters, "id"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("filter id must be a number");
        }
    }

    @Nested
    class ExtractMandatoryLongFilter {

        @Test
        void should_return_value_when_filter_is_a_valid_number() {
            var filters = List.of("id=99");

            assertThat(extractMandatoryLongFilter(filters, "id")).isEqualTo(99L);
        }

        @Test
        void should_throw_when_filter_is_missing() {
            var filters = List.of("other=42");

            assertThatThrownBy(() -> extractMandatoryLongFilter(filters, "id"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("filter id is mandatory");
        }

        @Test
        void should_throw_when_value_is_not_a_number() {
            var filters = List.of("id=abc");

            assertThatThrownBy(() -> extractMandatoryLongFilter(filters, "id"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("filter id must be a number");
        }

        @Test
        void should_throw_when_value_is_empty() {
            var filters = List.of("id=");

            assertThatThrownBy(() -> extractMandatoryLongFilter(filters, "id"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("filter id is mandatory");
        }

        @Test
        void should_throw_when_filter_has_no_equals_sign() {
            var filters = List.of("id");

            assertThatThrownBy(() -> extractMandatoryLongFilter(filters, "id"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("filter id is mandatory");
        }

        @Test
        void should_throw_when_value_contains_commas() {
            var filters = List.of("id=1,2,3");

            assertThatThrownBy(() -> extractMandatoryLongFilter(filters, "id"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("filter id must be a number");
        }

        @Test
        void should_throw_when_filters_is_null() {
            assertThatThrownBy(() -> extractMandatoryLongFilter(null, "id"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("filter id is mandatory");
        }
    }
}
