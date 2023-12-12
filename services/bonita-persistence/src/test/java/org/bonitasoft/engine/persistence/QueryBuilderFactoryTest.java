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

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

public class QueryBuilderFactoryTest {

    static class ParentDummyPersistentObject implements PersistentObject {

        private static final long serialVersionUID = 1L;

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public void setId(final long id) {

        }

        @Override
        public void setTenantId(final long id) {

        }

    }

    class Child1DummyPersistentObject extends ParentDummyPersistentObject {

        private static final long serialVersionUID = 1L;

    }

    class Child2DummyPersistentObject extends ParentDummyPersistentObject {

        private static final long serialVersionUID = 1L;

    }

    class DummyPersistentObject2 implements PersistentObject {

        private static final long serialVersionUID = 1L;

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public void setId(final long id) {

        }

        @Override
        public void setTenantId(final long id) {

        }

    }

    @Test
    public void should_word_search_returns_false_when_entity_class_is_null() throws Exception {
        final Set<String> wordSearchExclusionMappings = Collections.<String> emptySet();
        final Class<? extends PersistentObject> entityClass = null;
        final boolean expectedResult = false;

        executeIsWordSearchEnabled(wordSearchExclusionMappings, entityClass, expectedResult);

    }

    @Test
    public void should_word_search_returns_true_when_feature_is_enabled_and_exclusion_is_empty() throws Exception {
        final Set<String> wordSearchExclusionMappings = Collections.<String> emptySet();
        final Class<? extends PersistentObject> entityClass = ParentDummyPersistentObject.class;
        final boolean expectedResult = true;

        executeIsWordSearchEnabled(wordSearchExclusionMappings, entityClass, expectedResult);
    }

    @Test
    public void should_word_search_returns_false_when_feature_is_enabled_and_entity_class_is_excluded()
            throws Exception {
        final Set<String> wordSearchExclusionMappings = Collections
                .singleton(ParentDummyPersistentObject.class.getName());
        final Class<? extends PersistentObject> entityClass = ParentDummyPersistentObject.class;
        final boolean expectedResult = false;

        executeIsWordSearchEnabled(wordSearchExclusionMappings, entityClass, expectedResult);
    }

    @Test
    public void should_word_search_returns_true_when_feature_is_enabled_and_entity_class_is_not_excluded()
            throws Exception {
        final Set<String> wordSearchExclusionMappings = Collections.singleton(DummyPersistentObject2.class.getName());
        final Class<? extends PersistentObject> entityClass = ParentDummyPersistentObject.class;
        final boolean expectedResult = true;

        executeIsWordSearchEnabled(wordSearchExclusionMappings, entityClass, expectedResult);
    }

    @Test
    public void should_word_search_returns_false_when_feature_is_enabled_and_parent_entity_class_is_excluded()
            throws Exception {
        final Set<String> wordSearchExclusionMappings = Collections
                .singleton(ParentDummyPersistentObject.class.getName());
        final Class<? extends PersistentObject> entityClass = Child1DummyPersistentObject.class;
        final boolean expectedResult = false;

        executeIsWordSearchEnabled(wordSearchExclusionMappings, entityClass, expectedResult);
    }

    @Test
    public void should_word_search_returns_true_when_feature_is_enabled_and_child_entity_class_is_excluded()
            throws Exception {
        final Set<String> wordSearchExclusionMappings = Collections
                .singleton(Child1DummyPersistentObject.class.getName());
        final Class<? extends PersistentObject> entityClass = ParentDummyPersistentObject.class;
        final boolean expectedResult = true;

        executeIsWordSearchEnabled(wordSearchExclusionMappings, entityClass, expectedResult);
    }

    @Test
    public void should_word_search_returns_true_when_feature_is_enabled_and_brother_entity_class_is_excluded()
            throws Exception {
        final Set<String> wordSearchExclusionMappings = Collections
                .singleton(Child1DummyPersistentObject.class.getName());
        final Class<? extends PersistentObject> entityClass = Child2DummyPersistentObject.class;
        final boolean expectedResult = true;

        executeIsWordSearchEnabled(wordSearchExclusionMappings, entityClass, expectedResult);
    }

    private void executeIsWordSearchEnabled(
            final Set<String> wordSearchExclusionMappings,
            final Class<? extends PersistentObject> entityClass, final boolean expectedResult)
            throws Exception {
        QueryBuilderFactory queryBuilderFactory = new QueryBuilderFactory(OrderByCheckingMode.NONE, emptyMap(), '%',
                wordSearchExclusionMappings);

        assertThat(queryBuilderFactory.isWordSearchEnabled(entityClass)).isEqualTo(expectedResult);
    }

}
