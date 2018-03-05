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

package org.bonitasoft.engine.persistence;

import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.persistence.search.FilterOperationType;
import org.bonitasoft.engine.services.Vendor;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class SQLQueryBuilderTest {

    @Mock
    private Session session;
    @Mock
    private SQLQuery mockedQuery;
    private static final char LIKE_ESCAPE_CHARACTER = '§';
    private Map<String, String> classAliasMappings = singletonMap(TestObject.class.getName(), "testObj");
    private Map<String, Class<? extends PersistentObject>> interfaceToClassMapping = Collections
            .singletonMap(TestObject.class.getName(), TestObject.class);

    private QueryBuilder createQueryBuilder(String baseQuery, Vendor vendor) {
        return new SQLQueryBuilder(baseQuery, vendor, TestObject.class, new DefaultOrderByBuilder(), classAliasMappings, interfaceToClassMapping,
                LIKE_ESCAPE_CHARACTER);
    }

    @Test
    public void should_generate_query_with_boolean_parameter() throws Exception {
        //given
        doReturn(mockedQuery).when(session).createSQLQuery(anyString());
        String baseQuery = "SELECT testObj.* FROM test_object testObj WHERE testObj.enabled = :trueValue";
        doReturn(baseQuery).when(mockedQuery).getQueryString();
        //when
        QueryBuilder queryBuilder = createQueryBuilder(baseQuery, Vendor.POSTGRES);
        //then
        queryBuilder.buildQuery(session);
        verify(session).createSQLQuery("SELECT testObj.* FROM test_object testObj WHERE testObj.enabled = :trueValue");
        verify(mockedQuery).setParameter("trueValue", true);
    }

    @Test
    public void should_generate_query_with_boolean_parameter_replaced_by_int_on_oracle() throws Exception {
        //given
        doReturn(mockedQuery).when(session).createSQLQuery(anyString());
        String baseQuery = "SELECT testObj.* FROM test_object testObj WHERE testObj.enabled = :trueValue";
        doReturn(baseQuery).when(mockedQuery).getQueryString();
        //when
        QueryBuilder queryBuilder = createQueryBuilder(baseQuery, Vendor.ORACLE);
        queryBuilder.buildQuery(session);
        //then
        verify(session).createSQLQuery("SELECT testObj.* FROM test_object testObj WHERE testObj.enabled = :trueValue");
        verify(mockedQuery).setParameter("trueValue", 1);
    }

    @Test
    public void should_generate_query_with_boolean_parameter_replaced_by_int_on_sqlserver() throws Exception {
        //given
        doReturn(mockedQuery).when(session).createSQLQuery(anyString());
        String baseQuery = "SELECT testObj.* FROM test_object testObj WHERE testObj.enabled = :trueValue";
        doReturn(baseQuery).when(mockedQuery).getQueryString();
        //when
        QueryBuilder queryBuilder = createQueryBuilder(baseQuery, Vendor.SQLSERVER);
        queryBuilder.buildQuery(session);
        //then
        verify(session).createSQLQuery("SELECT testObj.* FROM test_object testObj WHERE testObj.enabled = :trueValue");
        verify(mockedQuery).setParameter("trueValue", 1);
    }

    @Test
    public void should_not_replace_parameters_of_type_boolean_on_postgres() throws Exception {
        //given
        doReturn(mockedQuery).when(session).createSQLQuery(anyString());
        String baseQuery = "SELECT testObj.* FROM test_object testObj";
        doReturn(baseQuery).when(mockedQuery).getQueryString();
        //when
        QueryBuilder queryBuilder = createQueryBuilder(baseQuery, Vendor.POSTGRES);
        queryBuilder.appendFilters(Collections.singletonList(new FilterOption(TestObject.class, "enabled", true, FilterOperationType.EQUALS)), null,
                false);
        queryBuilder.buildQuery(session);
        //then
        verify(session).createSQLQuery("SELECT testObj.* FROM test_object testObj WHERE (testObj.enabled = true)");
    }

    @Test
    public void should_replace_parameters_of_type_boolean_on_oracle() throws Exception {
        //given
        doReturn(mockedQuery).when(session).createSQLQuery(anyString());
        String baseQuery = "SELECT testObj.* FROM test_object testObj";
        doReturn(baseQuery).when(mockedQuery).getQueryString();
        //when
        QueryBuilder queryBuilder = createQueryBuilder(baseQuery, Vendor.ORACLE);
        queryBuilder.appendFilters(Collections.singletonList(new FilterOption(TestObject.class, "enabled", true, FilterOperationType.EQUALS)), null,
                false);
        queryBuilder.buildQuery(session);
        //then
        verify(session).createSQLQuery("SELECT testObj.* FROM test_object testObj WHERE (testObj.enabled = 1)");
    }
}
