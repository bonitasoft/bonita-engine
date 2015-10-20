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
package org.bonitasoft.engine.business.data.proxy;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.Test;

public class EntityGetterTest {

    @Test(expected = IllegalArgumentException.class)
    public void can_only_be_applicable_to_getters() throws Exception {
        //given
        Method setter = Employee.class.getMethod("setLastName", String.class);

        //when then
        new EntityGetter(setter);
    }

    @Test
    public void should_be_able_to_retrieve_source_entity_name() throws Exception {
        //given
        Method methodOfEmployee = Employee.class.getMethod("getAddress");

        //when
        String sourceEntityName = new EntityGetter(methodOfEmployee).getSourceEntityName();

        //then
        assertThat(sourceEntityName).isEqualTo(Employee.class.getSimpleName());
    }

    @Test
    public void should_be_able_to_retrieve_target_entity_class_even_if_method_return_a_list() throws Exception {
        //given
        Method methodReturningListOfAdresses = Employee.class.getMethod("getAddresses");

        //when
        final EntityGetter entityGetter = new EntityGetter(methodReturningListOfAdresses);

        //then
        assertThat(entityGetter.getTargetEntityClass()).isEqualTo(Address.class);
        assertThat(entityGetter.getReturnTypeClassName()).isEqualTo("org.bonitasoft.engine.business.data.proxy.Address");
        assertThat(entityGetter.returnsList()).isTrue();
    }

    @Test
    public void should_be_able_to_retrieve_capitalized_field_name() throws Exception {
        //given
        Method getAdresses = Employee.class.getMethod("getAddresses");

        //when
        String sourceEntityName = new EntityGetter(getAdresses).getCapitalizedFieldName();

        //then
        assertThat(sourceEntityName).isEqualTo("Addresses");
    }

    @Test
    public void should_return_entity_class_name_if_getter_return_a_unique_object() throws Exception {
        //given
        Method uniqueObjectGetter = Employee.class.getMethod("getAddress");

        //when
        final EntityGetter entityGetter = new EntityGetter(uniqueObjectGetter);

        //then
        assertThat(entityGetter.getReturnTypeClassName()).isEqualTo(Address.class.getName());
        assertThat(entityGetter.returnsList()).isFalse();

    }

    @Test
    public void should_be_able_to_get_associated_named_query() throws Exception {
        //given
        Method getManager = Employee.class.getMethod("getAddresses");

        //when
        String namedQuery = new EntityGetter(getManager).getAssociatedNamedQuery();

        //then
        assertThat(namedQuery).isEqualTo("Address.findAddressesByEmployeePersistenceId");
    }

    @Test
    public void should_be_able_to_determine_if_getter_return_a_list() throws Exception {
        //given
        Method multipleObjectGetter = Employee.class.getMethod("getAddresses");

        //when
        final EntityGetter entityGetter = new EntityGetter(multipleObjectGetter);

        //then
        assertThat(entityGetter.returnsList()).isTrue();
        assertThat(entityGetter.getReturnTypeClassName()).isEqualTo(entityGetter.getTargetEntityClass().getName());
    }

    @Test
    public void should_be_able_to_determine_if_getter_do_not_return_a_list() throws Exception {
        //given
        Method multipleObjectGetter = Employee.class.getMethod("getLastName");

        //when
        final EntityGetter entityGetter = new EntityGetter(multipleObjectGetter);

        //then
        assertThat(entityGetter.returnsList()).isFalse();
        assertThat(entityGetter.getReturnTypeClassName()).isEqualTo(entityGetter.getTargetEntityClass().getName());

    }
}
