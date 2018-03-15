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

package org.bonitasoft.engine.business.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.business.data.DummyBusinessDataRefBuilder.buildMultiRefBusinessData;
import static org.bonitasoft.engine.business.data.DummyBusinessDataRefBuilder.buildSimpleRefBusinessData;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.proxy.ServerProxyfier;
import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.operation.pojo.Employee;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BusinessDataRetrieverTest {

    @Mock
    private ServerProxyfier proxyfier;

    @Mock
    private BusinessDataRepository repository;

    @InjectMocks
    private BusinessDataRetriever retriever;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void getSimpleBusinessData_should_return_proxyfied_entity() throws Exception {
        //given
        long id = 11L;
        Employee employee = new Employee(id, 1L, "John", "Doe");
        Employee proxy = new Employee(id, 1L, "John", "Proxy");
        given(repository.findById(Employee.class, id)).willReturn(employee);
        given(proxyfier.proxify(employee)).willReturn(proxy);

        //when
        Entity retrievedEntity = retriever.getSimpleBusinessData(buildSimpleRefBusinessData(id), Employee.class);

        //then
        assertThat(retrievedEntity).isEqualTo(proxy);
    }

    @Test
    public void getSimpleBusinessData_should_return_null_when_ref_business_data_does_not_reference_any_data() throws Exception {
        //when
        Entity retrievedEntity = retriever.getSimpleBusinessData(buildSimpleRefBusinessData((Long) null), Employee.class);

        //then
        assertThat(retrievedEntity).isNull();
        verifyZeroInteractions(repository);
        verifyZeroInteractions(proxyfier);
    }

    @Test
    public void getMultiBusinessData_should_return_list_of_proxyfied_entities() throws Exception {
        //given
        List<Long> ids = Arrays.asList(11L, 12L);
        List<Employee> employees = Arrays.asList(new Employee(ids.get(0), 1L, "John", "Doe"), new Employee(ids.get(1), 1L, "Jack", "Doe"));
        List<Employee> proxies = Arrays.asList(new Employee(ids.get(0), 1L, "John", "Proxy"), new Employee(ids.get(1), 1L, "Jack", "Proxy"));
        given(repository.findByIds(Employee.class, ids)).willReturn(employees);
        given(proxyfier.proxify(employees.get(0))).willReturn(proxies.get(0));
        given(proxyfier.proxify(employees.get(1))).willReturn(proxies.get(1));

        //when
        List<Entity> retrievedEntities = retriever.getMultiBusinessData(buildMultiRefBusinessData(ids), Employee.class);

        //then
        assertThat(retrievedEntities).containsExactly(proxies.get(0), proxies.get(1));
    }

    @Test
    public void getMultiBusinessData_should_return_empty_list_when_multi_ref_data_does_not_reference_any_data() throws Exception {
        //when
        List<Entity> retrievedEntities = retriever.getMultiBusinessData(buildMultiRefBusinessData((List<Long>) null), Employee.class);

        //then
        assertThat(retrievedEntities).isEmpty();
        verifyZeroInteractions(repository);
        verifyZeroInteractions(proxyfier);
    }

    @Test
    public void getMultiBusinessData_should_return_empty_Array_list_when_multi_ref_data_does_not_reference_any_data() throws Exception {

        List<Entity> retrievedEntities = retriever.getMultiBusinessData(buildMultiRefBusinessData(new ArrayList<>()), Employee.class);

        assertThat(retrievedEntities).isEmpty();

        //when
        retrievedEntities.add(new Employee());
        // no exceptions

        verifyZeroInteractions(repository);
        verifyZeroInteractions(proxyfier);
    }

    @Test
    public void getBusinessData_should_use_getSimpleBusinessData_for_SSimpleBusinessDataRef() throws Exception {
        //given
        BusinessDataRetriever retrieverSpy = spy(retriever);
        SSimpleRefBusinessDataInstance dataRef = buildSimpleRefBusinessData("employee", Employee.class.getName());
        Employee employee = new Employee(5L, 1L, "John", "Doe");
        given(retrieverSpy.getSimpleBusinessData(dataRef, Employee.class)).willReturn(employee);

        //when
        Object businessData = retrieverSpy.getBusinessData(dataRef);

        //then
        assertThat(businessData).isEqualTo(employee);
    }

    @Test
    public void getBusinessData_should_use_getMultiBusinessData_for_SMultiBusinessDataRef() throws Exception {
        //given
        BusinessDataRetriever retrieverSpy = spy(retriever);
        SProcessMultiRefBusinessDataInstance dataRef = buildMultiRefBusinessData(Employee.class.getName());
        Employee employee = new Employee(5L, 1L, "John", "Doe");
        given(retrieverSpy.getMultiBusinessData(dataRef, Employee.class)).willReturn(Collections.<Entity>singletonList(employee));

        //when
        List<Entity> businessData = (List<Entity>) retrieverSpy.getBusinessData(dataRef);

        //then
        assertThat(businessData).containsExactly(employee);
    }

    @Test
    public void getBusinessData_should_throw_exception_if_class_is_unknown() throws Exception {
        //given
        String dataName = "employee";
        SSimpleRefBusinessDataInstance dataRef = buildSimpleRefBusinessData(dataName, "org.any.not.exists.Pojo");

        //then
        expectedException.expect(SExpressionEvaluationException.class);
        expectedException.expectMessage("Unable to load class for the business data having reference '" + dataName + "'");

        //when
        retriever.getBusinessData(dataRef);

    }

}
