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

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.bonitasoft.engine.commons.EnumToObjectConvertible;
import org.bonitasoft.engine.persistence.search.FilterOperationType;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * @author Baptiste Mesta
 */
public class QueryBuilderTest {

    private static final char LIKE_ESCAPE_CHARACTER = '§';
    public Map<String, String> classAliasMappings = singletonMap(TestObject.class.getName(), "testObj");

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private Session session;
    @Mock
    private Query query;

    @Before
    public void before() {
        when(session.createQuery(anyString())).thenAnswer(a -> {
            Query mock = mock(Query.class);
            doReturn(a.getArgument(0)).when(mock).getQueryString();
            return mock;
        });
    }

    @Test
    public void should_hasChanged_return_false_if_query_has_not_changed() throws Exception {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT TOTO FROM STUFF");
        //when
        //then
        assertThat(queryBuilder.hasChanged()).isFalse();
    }

    private QueryBuilder createQueryBuilder(String baseQuery) {
        return createQueryBuilder(baseQuery, null, OrderByCheckingMode.NONE);
    }

    private HQLQueryBuilder createQueryBuilder(String baseQuery, SelectListDescriptor<TestObject> selectListDescriptor,
            OrderByCheckingMode orderByCheckingMode) {
        doReturn(baseQuery).when(query).getQueryString();
        return new HQLQueryBuilder<>(session, query, new DefaultOrderByBuilder(),
                classAliasMappings,
                LIKE_ESCAPE_CHARACTER, false, orderByCheckingMode, selectListDescriptor);
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
    public void should_generate_query_with_filter() throws Exception {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(Collections.singletonList(new FilterOption(TestObject.class, "theValue", 12)), null,
                false);
        //then
        assertThat(queryBuilder.getQuery())
                .isEqualTo("SELECT testObj.* FROM test_object testObj WHERE (testObj.theValue = 12)");
    }

    @Test
    public void should_generate_query_with_multiple_filters() throws Exception {
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
                "SELECT testObj.* FROM test_object testObj WHERE (testObj.age = 25 AND testObj.lastname = 'John')");
    }

    @Test
    public void should_generate_query_with_filter_on_query_containing_filters_already() throws Exception {
        //given
        QueryBuilder queryBuilder = createQueryBuilder(
                "SELECT testObj.* FROM test_object testObj WHERE testObj.enabled = true");
        //when
        queryBuilder.appendFilters(Collections.singletonList(new FilterOption(TestObject.class, "theValue", 12)), null,
                false);
        //then
        assertThat(queryBuilder.getQuery()).isEqualTo(
                "SELECT testObj.* FROM test_object testObj WHERE testObj.enabled = true AND (testObj.theValue = 12)");
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
                "SELECT testObj.* FROM test_object testObj WHERE testObj.enabled = true AND (testObj.theValue = 12) ORDER BY testObj.theValue ASC,testObj.id ASC");
    }

    @Test
    public void should_generate_query_with_search_term() throws Exception {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(Collections.<FilterOption> emptyList(),
                new SearchFields(Collections.singletonList("toto"),
                        Collections.<Class<? extends PersistentObject>, Set<String>> singletonMap(TestObject.class,
                                aSet("field1", "field2"))),
                false);
        //then
        assertThat(queryBuilder.getQuery()).matches(
                "SELECT testObj\\.\\* FROM test_object testObj WHERE \\(testObj.field(1|2) LIKE 'toto%' ESCAPE '§' OR testObj.field(1|2) LIKE 'toto%' ESCAPE '§'\\)");
    }

    @Test
    public void should_generate_query_with_multiple_search_terms() throws Exception {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(Collections.<FilterOption> emptyList(),
                new SearchFields(Arrays.asList("toto", "tata"),
                        Collections.<Class<? extends PersistentObject>, Set<String>> singletonMap(TestObject.class,
                                aSet("field1", "field2"))),
                false);
        //then
        assertThat(queryBuilder.getQuery()).matches(
                "SELECT testObj\\.\\* FROM test_object testObj WHERE \\(testObj.field(1|2) LIKE 'toto%' ESCAPE '§' " +
                        "OR testObj.field(1|2) LIKE 'tata%' ESCAPE '§' " +
                        "OR testObj.field(1|2) LIKE 'toto%' ESCAPE '§' " +
                        "OR testObj.field(1|2) LIKE 'tata%' ESCAPE '§'\\)");
    }

    @Test
    public void should_generate_query_with_search_term_with_word_search() throws Exception {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(Collections.<FilterOption> emptyList(),
                new SearchFields(Collections.singletonList("toto"),
                        Collections.<Class<? extends PersistentObject>, Set<String>> singletonMap(TestObject.class,
                                aSet("field1", "field2"))),
                true);
        //then
        assertThat(queryBuilder.getQuery()).matches(
                "SELECT testObj\\.\\* FROM test_object testObj WHERE \\(testObj.field(1|2) LIKE 'toto%' ESCAPE '§' " +
                        "OR testObj.field(1|2) LIKE '% toto%' ESCAPE '§' " +
                        "OR testObj.field(1|2) LIKE 'toto%' ESCAPE '§' " +
                        "OR testObj.field(1|2) LIKE '% toto%' ESCAPE '§'\\)");
    }

    private Set<String> aSet(String... fields) {
        return new TreeSet<>(Arrays.asList(fields));
    }

    @Test
    public void should_generate_query_with_search_term_and_filters() throws Exception {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(Collections.singletonList(new FilterOption(TestObject.class, "field1", "tata")),
                new SearchFields(Collections.singletonList("toto"),
                        Collections.singletonMap(TestObject.class, aSet("field1", "field2"))),
                false);
        //then
        assertThat(queryBuilder.getQuery()).isEqualTo(
                "SELECT testObj.* FROM test_object testObj WHERE (testObj.field1 = 'tata') AND (testObj.field2 LIKE 'toto%' ESCAPE '§')");
    }

    @Test
    public void should_escape_special_chars_with_escape_character_in_filters() throws Exception {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(
                Collections.singletonList(new FilterOption(TestObject.class, "theValue", "the'value%with_special:_§§")),
                null, false);
        //then
        assertThat(queryBuilder.getQuery()).isEqualTo(
                "SELECT testObj.* FROM test_object testObj WHERE (testObj.theValue = 'the''value%with_special:_§§')");
    }

    @Test
    public void should_escape_special_chars_with_escape_character_in_search_terms() throws Exception {
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
                        "SELECT testObj.* FROM test_object testObj WHERE (testObj.field1 LIKE 'the''value§%with§_special:§_§§§§%' ESCAPE '§')");
    }

    @Test
    public void should_generate_query_with_greater_or_equals_filter() throws Exception {
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
                .isEqualTo("SELECT testObj.* FROM test_object testObj WHERE (testObj.age >= 25)");
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
                .isEqualTo("SELECT testObj.* FROM test_object testObj WHERE (testObj.age > 25)");
    }

    @Test
    public void should_generate_query_with_less_filter() throws Exception {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(
                Collections.singletonList(new FilterOption(TestObject.class, "age", 25, FilterOperationType.LESS)),
                null,
                false);
        //then
        assertThat(queryBuilder.getQuery())
                .isEqualTo("SELECT testObj.* FROM test_object testObj WHERE (testObj.age < 25)");
    }

    private QueryBuilder createBaseQueryBuilder() {
        return createQueryBuilder("SELECT testObj.* FROM test_object testObj");
    }

    @Test
    public void should_generate_query_with_less_or_equals_filter() throws Exception {
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
                .isEqualTo("SELECT testObj.* FROM test_object testObj WHERE (testObj.age <= 25)");
    }

    @Test
    public void should_generate_query_with_different_filter() throws Exception {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(
                Collections.singletonList(new FilterOption(TestObject.class, "age", 25, FilterOperationType.DIFFERENT)),
                null,
                false);
        //then
        assertThat(queryBuilder.getQuery())
                .isEqualTo("SELECT testObj.* FROM test_object testObj WHERE (testObj.age != 25)");
    }

    @Test
    public void should_generate_query_with_between_filter() throws Exception {
        //given
        QueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj");
        //when
        queryBuilder.appendFilters(Collections.singletonList(new FilterOption(TestObject.class, "age", 25, 27)), null,
                false);
        //then
        assertThat(queryBuilder.getQuery()).isEqualTo(
                "SELECT testObj.* FROM test_object testObj WHERE ((25 <= testObj.age AND testObj.age <= 27))");
    }

    @Test
    public void should_generate_query_with_parenthesis_filter() throws Exception {
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
                        "SELECT testObj.* FROM test_object testObj WHERE (testObj.age = 12 AND  (testObj.lastname = 'john' OR testObj.lastname = 'jack' ))");
    }

    @Test
    public void should_generate_query_with_like_filter() throws Exception {
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
                .isEqualTo("SELECT testObj.* FROM test_object testObj WHERE (testObj.lastname LIKE '%jack%')");
    }

    @Test
    public void should_generate_query_with_equals_filter_and_null_value() throws Exception {
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
    public void should_generate_query_with_filter_having_convertible_value() throws Exception {
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
                .isEqualTo("SELECT testObj.* FROM test_object testObj WHERE (testObj.lastname = 0)");
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
        public Object fromEnum() {
            return ordinal();
        }
    }

    @Test
    public final void should_log_nothing_when_no_ORDER_BY_clause_in_query_and_no_checking_mode() throws Exception {

        HQLQueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj",
                descriptorWithoutOrderBy(), OrderByCheckingMode.NONE);

        queryBuilder.build();

        assertThat(systemOutRule.getLog()).isEmpty();
    }

    @Test
    public final void should_log_nothing_when_no_ORDER_BY_clause_in_query_and_checking_mode_is_NONE()
            throws Exception {
        HQLQueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj",
                descriptorWithoutOrderBy(), OrderByCheckingMode.NONE);

        queryBuilder.build();

        assertThat(systemOutRule.getLog()).isEmpty();
    }

    @Test
    public final void should_log_when_no_ORDER_BY_clause_in_query_and_checking_mode_is_WARNING() throws Exception {
        HQLQueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj",
                descriptorWithoutOrderBy(), OrderByCheckingMode.WARNING);

        queryBuilder.build();

        assertThat(systemOutRule.getLog())
                .contains("Query 'SELECT testObj.* FROM test_object testObj' does not contain 'ORDER BY' clause");
    }

    @Test
    public final void should_log_nothing_when_ORDER_BY_clause_in_query_and_checking_mode_is_STRICT()
            throws Exception {
        HQLQueryBuilder queryBuilder = createQueryBuilder("SELECT testObj.* FROM test_object testObj",
                descriptorWithOrderBy(), OrderByCheckingMode.NONE);

        queryBuilder.build();

        assertThat(systemOutRule.getLog()).isEmpty();
    }

    private SelectListDescriptor<TestObject> descriptorWithOrderBy() {
        return new SelectListDescriptor<>("someQuery", emptyMap(), TestObject.class, new QueryOptions(0, 100,
                singletonList(new OrderByOption(TestObject.class, "someField", OrderByType.ASC))));
    }

    private SelectListDescriptor<TestObject> descriptorWithoutOrderBy() {
        return new SelectListDescriptor<>("someQuery", emptyMap(), TestObject.class, new QueryOptions(0, 100));
    }

    @Test
    public void should_escapeString_escape_quote() {
        new QueryGeneratorForFilters(emptyMap(), false, '%');
        // 1) escape ' character by adding another ' character
        final String s = QueryBuilder.escapeString("toto'toto");

        assertThat(s).isEqualTo("toto''toto");
    }

    @Test
    public void should_escapeString_do_not_escape_like_wildcard() {
        new QueryGeneratorForFilters(emptyMap(), false, '%');
        // 1) escape ' character by adding another ' character
        final String s = QueryBuilder.escapeString("%to'to%t_oto%");

        assertThat(s).isEqualTo("%to''to%t_oto%");
    }

    @Test
    public void should_detect_WHERE_when_in_root_query() {
        assertThat(QueryBuilder.hasWHEREInRootQuery("" +
                "Select * \n" +
                "from user_\n" +
                "WHERE\n" +
                "something\n" +
                "ORDER BY name\n")).isTrue();
    }

    @Test
    public void should_detect_WHERE_when_in_root_query_when_lowercase() {
        assertThat(QueryBuilder.hasWHEREInRootQuery("" +
                "Select * \n" +
                "from user_\n" +
                "where\n" +
                "something\n" +
                "ORDER BY name\n")).isTrue();
    }

    @Test
    public void should_not_detect_WHERE_when_only_in_sub_query() {
        assertThat(QueryBuilder.hasWHEREInRootQuery("" +
                "Select * \n" +
                "from (\n" +
                "   Select * \n" +
                "   from user_\n" +
                "   WHERE\n" +
                "   something)\n" +
                ")\n" +
                "ORDER BY name")).isFalse();
    }

    @Test
    public void should_not_detect_WHERE_when_not_present() {
        assertThat(QueryBuilder.hasWHEREInRootQuery("" +
                "Select * \n" +
                "from user_\n" +
                "ORDER BY name\n")).isFalse();
    }

    @Test
    public void should_detect_WHERE_when_select_has_parenthesis() {
        assertThat(QueryBuilder.hasWHEREInRootQuery("" +
                "Select count(*) \n" +
                "from user_\n" +
                "WHERE (something)\n" +
                "ORDER BY name\n")).isTrue();
    }

    @Test
    public void should_not_detect_WHERE_with_subqueries() {
        assertThat(QueryBuilder.hasWHEREInRootQuery("" +
                "Select * \n" +
                "from (\n" +
                "   Select * \n" +
                "   from user_\n" +
                "   WHERE\n" +
                "   something\n" +
                ") user_\n" +
                "WHERE \n (somethings)")).isTrue();
    }
}
