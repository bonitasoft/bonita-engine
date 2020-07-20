/**
 * Copyright (C) 2019 Bonitasoft S.A.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.bonitasoft.engine.commons.EnumToObjectConvertible;
import org.bonitasoft.engine.persistence.search.FilterOperationType;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class QueryBuilderTest {

    private static final char LIKE_ESCAPE_CHARACTER = '§';
    public Map<String, String> classAliasMappings = singletonMap(TestObject.class.getName(), "testObj");

    @Test
    public void should_hasChanged_return_false_if_query_has_not_changed() {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT TOTO FROM STUFF");
        //when
        //then
        assertThat(queryBuilder.hasChanged()).isFalse();
    }

    private QueryBuilder createQueryBuilder(String baseQuery) {
        return new HQLQueryBuilder(baseQuery, new DefaultOrderByBuilder(), classAliasMappings,
                LIKE_ESCAPE_CHARACTER);
    }

    @Test
    public void should_hasChanged_return_true_if_query_has_changed() throws Exception {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT TOTO FROM STUFF");
        //when
        queryBuilder.appendOrderByClause(
                Collections.singletonList(new OrderByOption(TestObject.class, "theValue", OrderByType.ASC)),
                TestObject.class);
        //then
        assertThat(queryBuilder.hasChanged()).isTrue();
    }

    @Test
    public void should_generate_query_with_order_by() throws Exception {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendOrderByClause(
                Collections.singletonList(new OrderByOption(TestObject.class, "theValue", OrderByType.ASC)),
                TestObject.class);
        //then
        assertThat(queryBuilder.getQuery())
                .isEqualTo("SELECT testObj.* FROM test_object testObj ORDER BY testObj.theValue ASC,testObj.id ASC");
    }

    @Test
    public void should_generate_query_with_multiple_order_by() throws Exception {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendOrderByClause(Arrays.asList(new OrderByOption(TestObject.class, "theValue", OrderByType.ASC),
                new OrderByOption(TestObject.class, "id", OrderByType.ASC),
                new OrderByOption(TestObject.class, "lastName", OrderByType.DESC_NULLS_LAST)),
                TestObject.class);
        //then
        assertThat(queryBuilder.getQuery())
                .isEqualTo(
                        "SELECT testObj.* FROM test_object testObj ORDER BY testObj.theValue ASC,testObj.id ASC,testObj.lastName DESC NULLS LAST");

    }

    @Test
    public void should_generate_query_with_filter() {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(Collections.singletonList(new FilterOption(TestObject.class, "theValue", 12)), null,
                false);
        //then
        assertThat(queryBuilder.getQuery())
                .isEqualTo("SELECT testObj.* FROM test_object testObj WHERE (testObj.theValue = :p1)");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo(12);
    }

    @Test
    public void should_generate_query_with_multiple_filters() {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(
                Arrays.asList(new FilterOption(TestObject.class, "age", 25),
                        new FilterOption(TestObject.class, "lastname", "John")),
                null,
                false);
        //then
        assertThat(queryBuilder.getQuery()).isEqualTo(
                "SELECT testObj.* FROM test_object testObj WHERE (testObj.age = :p1 AND testObj.lastname = :p2)");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo(25);
        assertThat(queryBuilder.getQueryParameters().get("p2")).isEqualTo("John");
    }

    @Test
    public void should_generate_query_with_filter_on_query_containing_filters_already() {
        //given
        QueryBuilder queryBuilder = createQueryBuilder(
                "SELECT testObj.* FROM test_object testObj WHERE testObj.enabled = true");
        //when
        queryBuilder.appendFilters(Collections.singletonList(new FilterOption(TestObject.class, "theValue", 12)), null,
                false);
        //then
        assertThat(queryBuilder.getQuery()).isEqualTo(
                "SELECT testObj.* FROM test_object testObj WHERE testObj.enabled = true AND (testObj.theValue = :p1)");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo(12);
    }

    @Test
    public void should_generate_query_with_filter_and_order_clause() throws Exception {
        //given
        QueryBuilder queryBuilder = createQueryBuilder(
                "SELECT testObj.* FROM test_object testObj WHERE testObj.enabled = true");
        //when
        queryBuilder.appendFilters(Collections.singletonList(new FilterOption(TestObject.class, "theValue", 12)), null,
                false);
        queryBuilder.appendOrderByClause(
                Collections.singletonList(new OrderByOption(TestObject.class, "theValue", OrderByType.ASC)),
                TestObject.class);
        //then
        assertThat(queryBuilder.getQuery()).isEqualTo(
                "SELECT testObj.* FROM test_object testObj WHERE testObj.enabled = true AND (testObj.theValue = :p1) ORDER BY testObj.theValue ASC,testObj.id ASC");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo(12);
    }

    @Test
    public void should_generate_query_with_search_term() {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(Collections.emptyList(),
                new SearchFields(Collections.singletonList("toto"),
                        Collections.singletonMap(TestObject.class,
                                aSet("field1", "field2"))),
                false);
        //then
        assertThat(queryBuilder.getQuery()).matches(
                "SELECT testObj\\.\\* FROM test_object testObj WHERE \\(testObj.field1 LIKE :p1 ESCAPE '§' OR testObj.field2 LIKE :p2 ESCAPE '§'\\)");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo("toto%");
        assertThat(queryBuilder.getQueryParameters().get("p2")).isEqualTo("toto%");
    }

    @Test
    public void should_generate_query_with_multiple_search_terms() {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(Collections.emptyList(),
                new SearchFields(Arrays.asList("toto", "tata"),
                        Collections.singletonMap(TestObject.class,
                                aSet("field1", "field2"))),
                false);
        //then
        assertThat(queryBuilder.getQuery()).isEqualTo(
                "SELECT testObj.* FROM test_object testObj WHERE (testObj.field1 LIKE :p1 ESCAPE '§' " +
                        "OR testObj.field1 LIKE :p2 ESCAPE '§' " +
                        "OR testObj.field2 LIKE :p3 ESCAPE '§' " +
                        "OR testObj.field2 LIKE :p4 ESCAPE '§')");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo("toto%");
        assertThat(queryBuilder.getQueryParameters().get("p2")).isEqualTo("tata%");
        assertThat(queryBuilder.getQueryParameters().get("p3")).isEqualTo("toto%");
        assertThat(queryBuilder.getQueryParameters().get("p4")).isEqualTo("tata%");
    }

    @Test
    public void should_generate_query_with_search_term_with_word_search() {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(Collections.emptyList(),
                new SearchFields(Collections.singletonList("toto"),
                        Collections.singletonMap(TestObject.class,
                                aSet("field1", "field2"))),
                true);
        //then
        assertThat(queryBuilder.getQuery()).isEqualTo(
                "SELECT testObj.* FROM test_object testObj WHERE " +
                        "(testObj.field1 LIKE :p1 ESCAPE '§' OR testObj.field1 LIKE :p2 ESCAPE '§' " +
                        "OR testObj.field2 LIKE :p3 ESCAPE '§' OR testObj.field2 LIKE :p4 ESCAPE '§')");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo("toto%");
        assertThat(queryBuilder.getQueryParameters().get("p2")).isEqualTo("% toto%");
        assertThat(queryBuilder.getQueryParameters().get("p3")).isEqualTo("toto%");
        assertThat(queryBuilder.getQueryParameters().get("p4")).isEqualTo("% toto%");
    }

    private Set<String> aSet(String... fields) {
        return new TreeSet<>(Arrays.asList(fields));
    }

    @Test
    public void should_generate_query_with_search_term_and_filters() {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(Collections.singletonList(new FilterOption(TestObject.class, "field1", "tata")),
                new SearchFields(Collections.singletonList("toto"),
                        Collections.singletonMap(TestObject.class, aSet("field1", "field2"))),
                false);
        //then
        assertThat(queryBuilder.getQuery()).isEqualTo(
                "SELECT testObj.* FROM test_object testObj WHERE (testObj.field1 = :p1) AND (testObj.field2 LIKE :p2 ESCAPE '§')");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo("tata");
        assertThat(queryBuilder.getQueryParameters().get("p2")).isEqualTo("toto%");
    }

    @Test
    public void should_escape_special_chars_with_escape_character_in_search_terms() {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(Collections.emptyList(),
                new SearchFields(Collections.singletonList("the'value%with_special:_§§"),
                        Collections.singletonMap(TestObject.class, aSet("field1"))),
                false);
        //then
        assertThat(queryBuilder.getQuery())
                .isEqualTo(
                        "SELECT testObj.* FROM test_object testObj WHERE (testObj.field1 LIKE :p1 ESCAPE '§')");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo("the'value§%with§_special:§_§§§§%");
    }

    @Test
    public void should_generate_query_with_greater_or_equals_filter() {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(
                Collections.singletonList(
                        new FilterOption(TestObject.class, "age", 25, FilterOperationType.GREATER_OR_EQUALS)),
                null,
                false);
        //then
        assertThat(queryBuilder.getQuery())
                .isEqualTo("SELECT testObj.* FROM test_object testObj WHERE (testObj.age >= :p1)");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo(25);
    }

    @Test
    public void should_generate_query_with_greater_filter() throws Exception {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(
                Collections.singletonList(new FilterOption(TestObject.class, "age", 25, FilterOperationType.GREATER)),
                null,
                false);
        //then
        assertThat(queryBuilder.getQuery())
                .isEqualTo("SELECT testObj.* FROM test_object testObj WHERE (testObj.age > :p1)");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo(25);
    }

    @Test
    public void should_generate_query_with_less_filter() {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(
                Collections.singletonList(new FilterOption(TestObject.class, "age", 25, FilterOperationType.LESS)),
                null,
                false);
        //then
        assertThat(queryBuilder.getQuery())
                .isEqualTo("SELECT testObj.* FROM test_object testObj WHERE (testObj.age < :p1)");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo(25);
    }

    private QueryBuilder createBaseQueryBuilder() {
        return createQueryBuilder("SELECT testObj.* FROM test_object testObj");
    }

    @Test
    public void should_getQueryFilters_append_OR_clause_when_wordSearch_is_enabled() {
        final StringBuilder stringBuilder = new StringBuilder();
        final QueryBuilder queryBuilder = createBaseQueryBuilder();
        queryBuilder.buildLikeClauseForOneFieldOneTerm(stringBuilder, "myField", "foo", true);

        assertThat(stringBuilder.toString())
                .as("query should contains like to check if the field start with foo and if the field contains a word starting by foo")
                .isEqualTo("myField LIKE :p1 ESCAPE '§' OR myField LIKE :p2 ESCAPE '§'");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo("foo%");
        assertThat(queryBuilder.getQueryParameters().get("p2")).isEqualTo("% foo%");
    }

    @Test
    public void should_getQueryFilters_append_OR_clause_when_wordSearch_is_not_enabled() {
        final StringBuilder stringBuilder = new StringBuilder();
        final QueryBuilder queryBuilder = createBaseQueryBuilder();
        queryBuilder.buildLikeClauseForOneFieldOneTerm(stringBuilder, "myField", "foo", false);

        assertThat(stringBuilder.toString()).isEqualTo("myField LIKE :p1 ESCAPE '§'");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo("foo%");
    }

    @Test
    public void should_generate_query_with_less_or_equals_filter() {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(
                Collections.singletonList(
                        new FilterOption(TestObject.class, "age", 25, FilterOperationType.LESS_OR_EQUALS)),
                null,
                false);
        //then
        assertThat(queryBuilder.getQuery())
                .isEqualTo("SELECT testObj.* FROM test_object testObj WHERE (testObj.age <= :p1)");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo(25);
    }

    @Test
    public void should_generate_query_with_different_filter() {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(
                Collections.singletonList(new FilterOption(TestObject.class, "age", 25, FilterOperationType.DIFFERENT)),
                null,
                false);
        //then
        assertThat(queryBuilder.getQuery())
                .isEqualTo("SELECT testObj.* FROM test_object testObj WHERE (testObj.age != :p1)");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo(25);
    }

    @Test
    public void should_generate_query_with_between_filter() {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(Collections.singletonList(new FilterOption(TestObject.class, "age", 25, 27)), null,
                false);
        //then
        assertThat(queryBuilder.getQuery()).isEqualTo(
                "SELECT testObj.* FROM test_object testObj WHERE ((:p1 <= testObj.age AND testObj.age <= :p2))");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo(25);
        assertThat(queryBuilder.getQueryParameters().get("p2")).isEqualTo(27);
    }

    @Test
    public void should_generate_query_with_in_filter() {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        FilterOption age = new FilterOption(TestObject.class, "age");
        final List<Integer> inValues = Arrays.asList(25, 26, 27);
        age.setIn(inValues);
        age.setFilterOperationType(FilterOperationType.IN);
        queryBuilder.appendFilters(Collections.singletonList(age), null,
                false);
        //then
        assertThat(queryBuilder.getQuery())
                .isEqualTo("SELECT testObj.* FROM test_object testObj WHERE (testObj.age IN (:p1))");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo(inValues);
    }

    @Test
    public void should_generate_query_with_parenthesis_filter() {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(Arrays.asList(new FilterOption(TestObject.class, "age", 12),
                new FilterOption(FilterOperationType.AND),
                new FilterOption(FilterOperationType.L_PARENTHESIS),
                new FilterOption(TestObject.class, "lastname", "john"),
                new FilterOption(FilterOperationType.OR),
                new FilterOption(TestObject.class, "lastname", "jack"),
                new FilterOption(FilterOperationType.R_PARENTHESIS)), null,
                false);
        //then
        assertThat(queryBuilder.getQuery())
                .isEqualTo(
                        "SELECT testObj.* FROM test_object testObj WHERE (testObj.age = :p1 AND  (testObj.lastname = :p2 OR testObj.lastname = :p3 ))");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo(12);
        assertThat(queryBuilder.getQueryParameters().get("p2")).isEqualTo("john");
        assertThat(queryBuilder.getQueryParameters().get("p3")).isEqualTo("jack");
    }

    @Test
    public void should_generate_query_with_like_filter() {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(
                Collections.singletonList(
                        new FilterOption(TestObject.class, "lastname", "jack", FilterOperationType.LIKE)),
                null,
                false);
        //then
        assertThat(queryBuilder.getQuery())
                .isEqualTo("SELECT testObj.* FROM test_object testObj WHERE (testObj.lastname LIKE :p1 ESCAPE '§')");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo("%jack%");
    }

    @Test
    public void should_generate_query_with_equals_filter_and_null_value() {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(
                Collections.singletonList(
                        new FilterOption(TestObject.class, "lastname", null, FilterOperationType.EQUALS)),
                null,
                false);
        //then
        assertThat(queryBuilder.getQuery())
                .isEqualTo("SELECT testObj.* FROM test_object testObj WHERE (testObj.lastname IS NULL)");
    }

    @Test
    public void should_generate_query_with_filter_having_convertible_value() {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(
                Collections.singletonList(
                        new FilterOption(TestObject.class, "lastname", TEST_ENUM.TEST1, FilterOperationType.EQUALS)),
                null,
                false);
        //then
        assertThat(queryBuilder.getQuery())
                .isEqualTo("SELECT testObj.* FROM test_object testObj WHERE (testObj.lastname = :p1)");
        assertThat(queryBuilder.getQueryParameters().get("p1")).isEqualTo(TEST_ENUM.TEST1);
    }

    @Test(expected = SBonitaReadException.class)
    public void should_throw_exception_if_class_is_not_mapped_in_filters() throws Exception {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendOrderByClause(
                Collections.singletonList(new OrderByOption(PersistentObject.class, "theValue", OrderByType.ASC)),
                PersistentObject.class);
        queryBuilder.getQuery();
    }

    private enum TEST_ENUM implements EnumToObjectConvertible {
        TEST1;

        @Override
        public int fromEnum() {
            return ordinal();
        }
    }

}
