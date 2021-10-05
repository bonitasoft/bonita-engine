/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.engine.search;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.search.impl.SearchOptionsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Emmanuel Duchastenier
 */
@ExtendWith(MockitoExtension.class)
class AbstractSearchEntityTest {

    @Spy
    private final MySearchEntity mySearchEntity = new MySearchEntity(new SearchOptionsImpl(0, 10));

    @Test
    void should_detect_potential_query_synchronization_issue_between_count_and_search() throws Exception {
        // given
        when(mySearchEntity.executeCount(any())).thenReturn(4L).thenReturn(4L);

        // when
        final String log = tapSystemOut(mySearchEntity::execute);

        // then
        assertThat(log).containsPattern("WARN.*Within the same transaction");
        verify(mySearchEntity).executeSearch(any());
        assertThat(log).doesNotContain("Double checking the same query");
        verify(mySearchEntity, times(2)).executeCount(any());
    }

    @Test
    void should_detect_when_search_retrieves_more_results_than_count() throws Exception {
        // given
        when(mySearchEntity.executeCount(any())).thenReturn(2L).thenReturn(1L);

        // when
        final String log = tapSystemOut(mySearchEntity::execute);

        // then
        verify(mySearchEntity).executeSearch(any());
        verify(mySearchEntity, times(2)).executeCount(any());
        assertThat(log).containsPattern("ERROR.*Double checking the same query");
    }

    @Test
    void should_not_double_check_anything_if_count_and_result_page_match() throws Exception {
        // when
        final String log = tapSystemOut(mySearchEntity::execute);

        // then
        verify(mySearchEntity).executeSearch(any());
        assertThat(log).doesNotContain("Within the same transaction");
        verify(mySearchEntity).executeCount(any()); // only one execution
        assertThat(log).doesNotContain("Double checking the same query");
    }

    static class MySearchEntity extends AbstractSearchEntity<Serializable, PersistentObject> {

        public MySearchEntity(SearchOptions searchOptions) {
            super(null, searchOptions);
        }

        @Override
        public long executeCount(QueryOptions queryOptions) {
            return 1L; // By default, count & search return one result. In some tests, this method is overwritten by mocking
        }

        @Override
        public List<PersistentObject> executeSearch(QueryOptions queryOptions) {
            final ArrayList<PersistentObject> list = new ArrayList<>();
            list.add(new TestEntity());
            return list;
        }

        @Override
        public List<Serializable> convertToClientObjects(List<PersistentObject> serverObjects) {
            return null;
        }

        private static class TestEntity implements PersistentObject {

            @Override
            public long getId() {
                return 0;
            }

            @Override
            public void setId(long id) {
            }

            @Override
            public void setTenantId(long id) {
            }
        }
    }
}
