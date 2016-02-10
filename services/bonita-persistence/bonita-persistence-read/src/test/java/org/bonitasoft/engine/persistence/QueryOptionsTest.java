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
package org.bonitasoft.engine.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class QueryOptionsTest {

    @Test
    public void getNextPageShouldPreserveOrderOptions() throws Exception {
        // given:
        final QueryOptions queryOptions = new QueryOptions(0, 10, list(new OrderByOption(PersistentObject.class, "fieldName", OrderByType.ASC)),
                list(new FilterOption(PersistentObject.class, "fieldName")), null);

        // when:
        final QueryOptions nextPage = QueryOptions.getNextPage(queryOptions);

        // then:
        assertThat(nextPage.getOrderByOptions()).isNotNull();
        assertThat(nextPage.getOrderByOptions()).hasSize(1);
    }

    @Test
    public void getNextPageShouldPreserveFilters() throws Exception {
        // given:
        final QueryOptions queryOptions = new QueryOptions(0, 10, list(new OrderByOption(PersistentObject.class, "fieldName", OrderByType.ASC)),
                list(new FilterOption(PersistentObject.class, "fieldName")), null);

        // when:
        final QueryOptions nextPage = QueryOptions.getNextPage(queryOptions);

        // then:
        assertThat(nextPage.getFilters()).isNotNull();
        assertThat(nextPage.getFilters()).hasSize(1);
    }

    @Test
    public void getNextPageShouldPreserveSearchFields() throws Exception {
        // given:
        final QueryOptions queryOptions = new QueryOptions(0, 10, list(new OrderByOption(PersistentObject.class, "fieldName", OrderByType.ASC)),
                list(new FilterOption(PersistentObject.class, "fieldName")), new SearchFields(null, null));

        // when:
        final QueryOptions nextPage = QueryOptions.getNextPage(queryOptions);

        // then:
        assertThat(nextPage.getMultipleFilter()).isNotNull();
    }

    private List list(final Object o) {
        return Arrays.asList(o);
    }
}
