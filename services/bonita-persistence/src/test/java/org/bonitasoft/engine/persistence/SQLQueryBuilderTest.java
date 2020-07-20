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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
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
    private NativeQuery mockedQuery;
    @Mock
    private Query query;
    private static final char LIKE_ESCAPE_CHARACTER = '§';
    private Map<String, String> classAliasMappings = singletonMap(TestObject.class.getName(), "testObj");

    private SQLQueryBuilder createQueryBuilder(String baseQuery) {
        doReturn(baseQuery).when(query).getQueryString();
        return new SQLQueryBuilder(null, query, new DefaultOrderByBuilder(),
                classAliasMappings,
                LIKE_ESCAPE_CHARACTER, false, OrderByCheckingMode.NONE, null);
    }

    @Test
    public void should_generate_query_with_boolean_parameter() throws Exception {
        //given
        String baseQuery = "SELECT testObj.* FROM test_object testObj WHERE testObj.enabled = :trueValue";
        doReturn(baseQuery).when(mockedQuery).getQueryString();
        SQLQueryBuilder queryBuilder = createQueryBuilder(baseQuery);
        //when
        queryBuilder.addConstantsAsParameters(mockedQuery);
        //then
        verify(mockedQuery).setParameter("trueValue", true);
    }
}
