package com.bonitasoft.engine.bdm.dao.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

import com.bonitasoft.engine.bdm.proxy.model.Address;
import com.bonitasoft.engine.bdm.proxy.model.Employee;

public class EntityGetterTest {

    @Test(expected = IllegalArgumentException.class)
    public void can_only_be_applicable_to_getters() throws Exception {
        Method setter = Employee.class.getMethod("setManager", Employee.class);
        new EntityGetter(setter);
    }

    @Test
    public void should_be_able_to_retrieve_source_entity_name() throws Exception {
        Method methodOfEmployee = Employee.class.getMethod("getManager");

        String sourceEntityName = new EntityGetter(methodOfEmployee).getSourceEntityName();

        assertThat(sourceEntityName).isEqualTo(Employee.class.getSimpleName());
    }

    @Test
    public void should_be_able_to_retrieve_target_entity_class() throws Exception {
        Method methodReturningAdress = Employee.class.getMethod("getAddress");

        Class<?> targetEntityClass = new EntityGetter(methodReturningAdress).getTargetEntityClass();

        assertThat(targetEntityClass).isEqualTo(Address.class);
    }

    @Test
    public void should_be_able_to_retrieve_target_entity_class_even_if_method_return_a_list() throws Exception {
        Method methodReturningListOfAdresses = Employee.class.getMethod("getAddresses");

        Class<?> targetEntityClass = new EntityGetter(methodReturningListOfAdresses).getTargetEntityClass();

        assertThat(targetEntityClass).isEqualTo(Address.class);
    }

    @Test
    public void should_be_able_to_retrieve_capitalized_field_name() throws Exception {
        Method getAdresses = Employee.class.getMethod("getAddresses");

        String sourceEntityName = new EntityGetter(getAdresses).getCapitalizedFieldName();

        assertThat(sourceEntityName).isEqualTo("Addresses");
    }

    @Test
    public void should_return_entity_class_name_if_getter_return_a_unique_object() throws Exception {
        Method uniqueObjectGetter = Employee.class.getMethod("getAddress");

        String classname = new EntityGetter(uniqueObjectGetter).getReturnTypeClassName();

        assertThat(classname).isEqualTo(Address.class.getName());
    }

    @Test
    public void should_return_list_class_name_if_getter_return_multiple_objects() throws Exception {
        Method multipleObjectGetter = Employee.class.getMethod("getAddresses");

        String classname = new EntityGetter(multipleObjectGetter).getReturnTypeClassName();

        assertThat(classname).isEqualTo(List.class.getName());
    }

    @Test
    public void should_be_able_to_get_associated_named_query() throws Exception {
        Method getManager = Employee.class.getMethod("getManager");

        String namedQuery = new EntityGetter(getManager).getAssociatedNamedQuery();

        assertThat(namedQuery).isEqualTo("Employee.findManagerByEmployeePersistenceId");
    }
    
    @Test
    public void should_be_able_to_determine_if_getter_return_a_list() throws Exception {
        Method multipleObjectGetter = Employee.class.getMethod("getAddresses");

        boolean isList = new EntityGetter(multipleObjectGetter).returnsList();

        assertThat(isList).isTrue();
    }
    
    @Test
    public void should_be_able_to_determine_if_getter_do_not_return_a_list() throws Exception {
        Method multipleObjectGetter = Employee.class.getMethod("getManager");

        boolean isList = new EntityGetter(multipleObjectGetter).returnsList();

        assertThat(isList).isFalse();
    }
}
