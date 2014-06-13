/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.proxy;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.Entity;

/**
 * @author Colin Puy
 */
public class EntityGetterAndSetterFilterTest {

    private EntityGetterAndSetterFilter lazyMethodFilter;

    @Before
    public void initMethodFilter() {
        lazyMethodFilter = new EntityGetterAndSetterFilter();
    }

    @Test
    public void should_return_false_for_a_non_getter_or_setter_method() throws Exception {
        Method method = FilterTestObject.class.getMethod("nonGetterOrSetterMethod");

        boolean handled = lazyMethodFilter.isHandled(method);

        assertThat(handled).isFalse();
    }

    @Test
    public void should_return_true_for_a_setter_that_have_a_subclass_of_entity_as_argument() throws Exception {
        Method method = FilterTestObject.class.getMethod("setEntity", FilterTestObject.class);

        boolean handled = lazyMethodFilter.isHandled(method);

        assertThat(handled).isTrue();
    }

    @Test
    public void should_return_true_for_a_setter_that_have_a_collection_of_subclass_of_entity_as_argument() throws Exception {
        Method method = FilterTestObject.class.getMethod("setEntities", List.class);

        boolean handled = lazyMethodFilter.isHandled(method);

        assertThat(handled).isTrue();
    }

    @Test
    public void should_return_false_for_a_setter_that_have_not_a_subclass_of_entity_as_argument() throws Exception {
        Method method = FilterTestObject.class.getMethod("setLong", Long.class);

        boolean handled = lazyMethodFilter.isHandled(method);

        assertThat(handled).isFalse();
    }

    @Test
    public void should_return_false_for_getter_not_having_an_entity_subclass_return_type() throws Exception {
        Method method = FilterTestObject.class.getMethod("getPersistenceId");

        boolean handled = lazyMethodFilter.isHandled(method);

        assertThat(handled).isFalse();
    }

    @Test
    public void should_return_true_for_getter_having_an_entity_subclass_return_type() throws Exception {
        Method method = FilterTestObject.class.getMethod("getObjectImplementingEntity");

        boolean handled = lazyMethodFilter.isHandled(method);

        assertThat(handled).isTrue();
    }

    @Test
    public void should_return_true_for_getter_having_a_list_of_entity_subclass_return_type() throws Exception {
        Method method = FilterTestObject.class.getMethod("getObjectListImplementingEntity");

        boolean handled = lazyMethodFilter.isHandled(method);

        assertThat(handled).isTrue();
    }

    @SuppressWarnings({ "unused", "serial" })
    private class FilterTestObject implements Entity {

        public FilterTestObject getObjectImplementingEntity() {
            return null;
        }

        public List<FilterTestObject> getObjectListImplementingEntity() {
            return null;
        }

        public FilterTestObject nonGetterOrSetterMethod() {
            return null;
        }

        public void setEntity(FilterTestObject entity) {

        }

        public void setEntities(List<FilterTestObject> entities) {

        }

        public void setLong(Long aLong) {

        }

        @Override
        public Long getPersistenceId() {
            return null;
        }

        @Override
        public Long getPersistenceVersion() {
            return null;
        }
    }
}
