/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.model.SMultiRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.SSimpleRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.impl.SProcessSimpleRefBusinessDataInstanceImpl;
import com.bonitasoft.engine.core.process.instance.model.impl.SSimpleRefBusinessDataInstanceImpl;
import com.bonitasoft.engine.operation.pojo.Employee;
import com.bonitasoft.engine.operation.pojo.InvalidTravel;
import com.bonitasoft.engine.operation.pojo.Travel;

@RunWith(MockitoJUnitRunner.class)
public class BusinessDataLeftOperandHandlerTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Mock
    private FlowNodeInstanceService flowNodeInstanceService;

    @Mock
    private BusinessDataRepository repository;

    @Mock
    private RefBusinessDataService refBusinessDataService;

    @InjectMocks
    private BusinessDataLeftOperandHandler leftOperandHandler;

    @Test
    public void updateShouldSetReferenceForNonNullBizDataId() throws Exception {
        final BusinessDataLeftOperandHandler spy = spy(leftOperandHandler);
        final SSimpleRefBusinessDataInstance refBiz = mock(SSimpleRefBusinessDataInstance.class);
        final Long bizDataId = 98744L;
        doReturn(refBiz).when(spy).getRefBusinessDataInstance(eq("myBizData"), eq(9L), eq("some container"));
        final Peticion bizData = new Peticion(bizDataId);
        doReturn(bizData).when(repository).merge(eq(bizData));

        spy.update(createLeftOperand("myBizData"), Collections.<String,Object>emptyMap(), bizData, 9L, "some container");

        verify(refBusinessDataService).updateRefBusinessDataInstance(refBiz, bizDataId);
    }

    class Peticion implements Entity {

        private static final long serialVersionUID = 1L;

        private final Long id;

        public Peticion(final Long id) {
            this.id = id;
        }

        @Override
        public Long getPersistenceId() {
            return id;
        }

        @Override
        public Long getPersistenceVersion() {
            return null;
        }

    }

    @Test
    public void insertBusinessData() throws SBonitaException {
        final long dataId = 789l;
        final Employee employee = new Employee(dataId, 52L, "firstName", "lastName");
        final long processInstanceId = 76846321l;

        final SLeftOperand leftOperand = mock(SLeftOperand.class);
        final SSimpleRefBusinessDataInstance refBizDataInstance = mock(SSimpleRefBusinessDataInstance.class);
        when(leftOperand.getName()).thenReturn("unused");
        when(flowNodeInstanceService.getProcessInstanceId(processInstanceId, DataInstanceContainer.PROCESS_INSTANCE.name())).thenReturn(processInstanceId);
        when(refBusinessDataService.getRefBusinessDataInstance("unused", processInstanceId)).thenReturn(refBizDataInstance);
        when(refBizDataInstance.getDataId()).thenReturn(null);
        when(repository.merge(employee)).thenReturn(employee);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(final InvocationOnMock invocation) {
                return null;
            }

        }).when(refBusinessDataService).updateRefBusinessDataInstance(refBizDataInstance, dataId);

        leftOperandHandler.update(leftOperand, Collections.<String,Object>emptyMap(), employee, processInstanceId, DataInstanceContainer.PROCESS_INSTANCE.name());

        verify(repository).merge(employee);
        verify(refBusinessDataService).getRefBusinessDataInstance("unused", processInstanceId);
        verify(refBusinessDataService).updateRefBusinessDataInstance(refBizDataInstance, dataId);
    }

    @Test
    public void should_retrieve_retrieve_the_business_data() throws Exception {
        // given:
        final String bizDataName = "myTravel";
        final Travel myTravel = new Travel();

        final BusinessDataLeftOperandHandler spy = spy(leftOperandHandler);
        doReturn(myTravel).when(spy).getBusinessData(anyString(), anyLong(), anyString());
        final Map<String, Object> inputValues = new HashMap<String, Object>(1);
        final SExpressionContext expressionContext = new SExpressionContext(-1L, "unused", inputValues);
        final SLeftOperand leftOperand = createLeftOperand(bizDataName);
        Map<String, Object> contextToSet = new HashMap<String, Object>();
        // when:
        spy.loadLeftOperandInContext(leftOperand, expressionContext, contextToSet);

        // then:
        assertThat(contextToSet).containsOnly(entry("myTravel", myTravel));
    }

    private SLeftOperand createLeftOperand(final String bizDataName) {
        final SLeftOperandImpl leftOperand = new SLeftOperandImpl();
        leftOperand.setName(bizDataName);
        return leftOperand;
    }

    @Test(expected = SOperationExecutionException.class)
    public void shouldUpdate_ThrowAnSOperationExecutionException() throws Exception {
        // given:
        final InvalidTravel myTravel = new InvalidTravel();
        final SSimpleRefBusinessDataInstance refInstance = mock(SSimpleRefBusinessDataInstance.class);
        final BusinessDataLeftOperandHandler spy = spy(leftOperandHandler);
        doReturn(myTravel).when(spy).getBusinessData(anyString(), anyLong(), anyString());
        doReturn(refInstance).when(spy).getRefBusinessDataInstance(anyString(), anyLong(), anyString());
        final SLeftOperand leftOp = mock(SLeftOperand.class);
        when(leftOp.getName()).thenReturn("bizData");

        spy.update(leftOp, Collections.<String,Object>emptyMap(), new Object(), 1L, "");
    }

    @Test
    public void shouldGetOperatorType_Return_BUSINESS_DATA_JAVA_SETTER() {
        assertThat(leftOperandHandler.getType()).isEqualTo(SLeftOperand.TYPE_BUSINESS_DATA);
    }

    @Test
    public void getBusinessDataCreateAnInstanceIfNoReferenceExists() throws Exception {
        final SSimpleRefBusinessDataInstance refInstance = mock(SSimpleRefBusinessDataInstance.class);
        final String bizDataName = "employee";
        final int processInstanceId = 457;
        when(refBusinessDataService.getRefBusinessDataInstance(bizDataName, processInstanceId)).thenReturn(refInstance);
        when(refInstance.getDataId()).thenReturn(null);
        when(refInstance.getDataClassName()).thenReturn(Employee.class.getName());

        final Employee employee = (Employee) leftOperandHandler.getBusinessData(bizDataName, processInstanceId, "PROCESS_INSTANCE");

        assertThat(employee).isNotNull();
        assertThat(employee.getPersistenceId()).isNull();
        assertThat(employee.getFirstName()).isNull();
        assertThat(employee.getLastName()).isNull();
    }

    @Test(expected = SBonitaReadException.class)
    public void getBusinessDataDoesNotCreateAnInstanceIfNoReferenceExistsWhenTheClassNameIsWrong() throws Exception {
        final SSimpleRefBusinessDataInstance refInstance = mock(SSimpleRefBusinessDataInstance.class);
        final String bizDataName = "employee";
        final int processInstanceId = 457;
        when(refBusinessDataService.getRefBusinessDataInstance(bizDataName, processInstanceId)).thenReturn(refInstance);
        when(refInstance.getDataId()).thenReturn(null);
        when(refInstance.getDataClassName()).thenReturn("fr.bonitasoft.engine.Employee");

        leftOperandHandler.getBusinessData(bizDataName, processInstanceId, "PROCESS_INSTANCE");
    }

    @Test(expected = SBonitaReadException.class)
    public void getBusinessDataDoesNotCreateAnInstanceIfNoReferenceExistsWhenTheClassNameIsAnInterface() throws Exception {
        final SSimpleRefBusinessDataInstance refInstance = mock(SSimpleRefBusinessDataInstance.class);
        final String bizDataName = "employee";
        final int processInstanceId = 457;
        when(refBusinessDataService.getRefBusinessDataInstance(bizDataName, processInstanceId)).thenReturn(refInstance);
        when(refInstance.getDataId()).thenReturn(null);
        when(refInstance.getDataClassName()).thenReturn(List.class.getName());

        leftOperandHandler.getBusinessData(bizDataName, processInstanceId, "PROCESS_INSTANCE");
    }

    @Test
    public void should_update_check_business_data_is_not_null() throws Exception {
        expectedException.expect(SOperationExecutionException.class);
        expectedException.expectMessage("Unable to insert/update a null business data");
        // when
        leftOperandHandler.update(createLeftOperand("bizData"), Collections.<String,Object>emptyMap(), null, 1, "cont");
    }

    @Test
    public void should_update_update_business_data() throws Exception {
        // given: business data having id and ref having the same id
        final SLeftOperand leftOperand = createLeftOperand("bizData");
        final Peticion bizData = new Peticion(123456789L);
        final SSimpleRefBusinessDataInstance ref = createRefBusinessDataInstance(123456789L);
        doReturn(ref).when(refBusinessDataService).getRefBusinessDataInstance("bizData", 1);
        doReturn(bizData).when(repository).merge(bizData);

        // when
        leftOperandHandler.update(leftOperand, Collections.<String,Object>emptyMap(), bizData, 1, "PROCESS_INSTANCE");

        // then
        verify(repository).merge(bizData);
        verify(refBusinessDataService, times(0)).updateRefBusinessDataInstance(eq(ref), anyLong());
    }

    private SSimpleRefBusinessDataInstance createRefBusinessDataInstance(final Long dataId) {
        final SSimpleRefBusinessDataInstanceImpl sRefBusinessDataInstanceImpl = new SProcessSimpleRefBusinessDataInstanceImpl();
        sRefBusinessDataInstanceImpl.setDataId(dataId);
        return sRefBusinessDataInstanceImpl;
    }

    @Test
    public void should_update_insert_business_data() throws Exception {
        // given: business data having null id and ref having null id
        final SLeftOperand leftOperand = createLeftOperand("bizData");
        final Peticion bizData = new Peticion(null);
        final Peticion mergedBizData = new Peticion(123456789L);
        final SSimpleRefBusinessDataInstance ref = createRefBusinessDataInstance(null);
        doReturn(ref).when(refBusinessDataService).getRefBusinessDataInstance("bizData", 1);
        doReturn(mergedBizData).when(repository).merge(bizData);

        // when
        leftOperandHandler.update(leftOperand, Collections.<String,Object>emptyMap(), bizData, 1, "PROCESS_INSTANCE");

        // then
        verify(repository, times(1)).merge(bizData);
        verify(refBusinessDataService, times(1)).updateRefBusinessDataInstance(ref, 123456789L);
    }

    @Test
    public void should_update_attach_business_data() throws Exception {
        // given: business data having not null id and ref having null id
        final SLeftOperand leftOperand = createLeftOperand("bizData");
        final Peticion bizData = new Peticion(123456789L);
        final SSimpleRefBusinessDataInstance ref = createRefBusinessDataInstance(null);
        doReturn(ref).when(refBusinessDataService).getRefBusinessDataInstance("bizData", 1);
        doReturn(bizData).when(repository).merge(bizData);

        // when
        leftOperandHandler.update(leftOperand, Collections.<String,Object>emptyMap(), bizData, 1, "PROCESS_INSTANCE");

        // then
        verify(repository, times(1)).merge(bizData);
        verify(refBusinessDataService, times(1)).updateRefBusinessDataInstance(ref, 123456789L);
    }

    @Test
    public void should_update_an_existing_reference_with_a_new_business_data() throws Exception {
        // given: business data having null id and ref having not null id
        final SLeftOperand leftOperand = createLeftOperand("bizData");
        final Peticion bizData = new Peticion(null);
        final Peticion mergedBizData = new Peticion(123456789L);
        final SSimpleRefBusinessDataInstance ref = createRefBusinessDataInstance(123456L);
        doReturn(ref).when(refBusinessDataService).getRefBusinessDataInstance("bizData", 1);
        doReturn(mergedBizData).when(repository).merge(bizData);

        // when
        leftOperandHandler.update(leftOperand, Collections.<String,Object>emptyMap(), bizData, 1, "PROCESS_INSTANCE");

        // then
        verify(repository).merge(bizData);
        verify(refBusinessDataService).updateRefBusinessDataInstance(ref, 123456789L);
    }

    @Test
    public void should_update_an_existing_reference_with_an_updated_business_data() throws Exception {
        // given: business data having null id and ref having not null id
        final SLeftOperand leftOperand = createLeftOperand("bizData");
        final Peticion bizData = new Peticion(123456789L);
        final SSimpleRefBusinessDataInstance ref = createRefBusinessDataInstance(123456L);
        doReturn(ref).when(refBusinessDataService).getRefBusinessDataInstance("bizData", 1);
        doReturn(bizData).when(repository).merge(bizData);

        // when
        leftOperandHandler.update(leftOperand, Collections.<String,Object>emptyMap(), bizData, 1, "PROCESS_INSTANCE");

        // then
        verify(repository).merge(bizData);
        verify(refBusinessDataService).updateRefBusinessDataInstance(ref, 123456789L);
    }

    @Test
    public void should_update_a_new_reference_with_a_new_business_data() throws Exception {
        // given: business data having null id and ref having not null id
        final SLeftOperand leftOperand = createLeftOperand("bizData");
        final Peticion bizData = new Peticion(null);
        final Peticion mergedBizData = new Peticion(123456789L);
        final SSimpleRefBusinessDataInstance ref = createRefBusinessDataInstance(null);
        doReturn(ref).when(refBusinessDataService).getRefBusinessDataInstance("bizData", 1);
        doReturn(mergedBizData).when(repository).merge(bizData);

        // when
        leftOperandHandler.update(leftOperand, Collections.<String,Object>emptyMap(), bizData, 1, "PROCESS_INSTANCE");

        // then
        verify(repository).merge(bizData);
        verify(refBusinessDataService).updateRefBusinessDataInstance(ref, 123456789L);
    }

    @Test
    public void should_update_a_new_reference_with_an_updated_business_data() throws Exception {
        // given: business data having null id and ref having not null id
        final SLeftOperand leftOperand = createLeftOperand("bizData");
        final Peticion bizData = new Peticion(123456789L);
        final SSimpleRefBusinessDataInstance ref = createRefBusinessDataInstance(null);
        doReturn(ref).when(refBusinessDataService).getRefBusinessDataInstance("bizData", 1);
        doReturn(bizData).when(repository).merge(bizData);

        // when
        leftOperandHandler.update(leftOperand, Collections.<String,Object>emptyMap(), bizData, 1, "PROCESS_INSTANCE");

        // then
        verify(repository).merge(bizData);
        verify(refBusinessDataService).updateRefBusinessDataInstance(ref, 123456789L);
    }

    @Test
    public void removeAndDereferenceBusinessData() throws Exception {
        final SLeftOperandImpl leftOperand = new SLeftOperandImpl();
        leftOperand.setName("address");
        final SSimpleRefBusinessDataInstance instance = mock(SSimpleRefBusinessDataInstance.class);
        when(refBusinessDataService.getRefBusinessDataInstance("address", 45)).thenReturn(instance);
        when(instance.getDataClassName()).thenReturn(Address.class.getName());

        leftOperandHandler.delete(leftOperand, 45, "PROCESS_INSTANCE");

        verify(refBusinessDataService).updateRefBusinessDataInstance(instance, null);
        verify(repository).remove(any(Address.class));
    }

    @Test(expected = SOperationExecutionException.class)
    public void deleteThrowsExceptionIfAnInternalExceptionOccurs() throws Exception {
        final SLeftOperandImpl leftOperand = new SLeftOperandImpl();
        leftOperand.setName("address");
        when(flowNodeInstanceService.getProcessInstanceId(45, "PROCESS_INSTANCE")).thenThrow(new SFlowNodeNotFoundException(45));

        leftOperandHandler.delete(leftOperand, 45, "PROCESS_INSTANCE");
    }


    @Test
    public void should_update_attach_multi_business_data() throws Exception {
        final SLeftOperand leftOperand = createLeftOperand("employees");
        final List<Peticion> peticions = new ArrayList<Peticion>();
        final Peticion bizData = new Peticion(123456789L);
        peticions.add(bizData);
        final SMultiRefBusinessDataInstance ref = mock(SMultiRefBusinessDataInstance.class);
        doReturn(ref).when(refBusinessDataService).getRefBusinessDataInstance("employees", 1);
        doReturn(bizData).when(repository).merge(bizData);

        // when
        leftOperandHandler.update(leftOperand, Collections.<String,Object>emptyMap(), peticions, 1, "PROCESS_INSTANCE");

        // then
        verify(repository).merge(bizData);
        verify(refBusinessDataService).updateRefBusinessDataInstance(ref, Arrays.asList(123456789L));
    }

    @Test
    public void removeAndDereferenceAMultiBusinessData() throws Exception {
        final SLeftOperandImpl leftOperand = new SLeftOperandImpl();
        leftOperand.setName("address");
        final SMultiRefBusinessDataInstance ref = mock(SMultiRefBusinessDataInstance.class);
        doReturn(ref).when(refBusinessDataService).getRefBusinessDataInstance("employees", 123l);
        when(refBusinessDataService.getRefBusinessDataInstance("address", 45)).thenReturn(ref);
        when(ref.getDataClassName()).thenReturn(Address.class.getName());
        when(ref.getDataIds()).thenReturn(Arrays.asList(486L));

        leftOperandHandler.delete(leftOperand, 45, "PROCESS_INSTANCE");

        verify(refBusinessDataService).updateRefBusinessDataInstance(ref, new ArrayList<Long>());
        verify(repository).remove(any(Address.class));
    }

    @Test
    public void getMultiBusinessDataReturnTheBusinessData() throws Exception {
        final SMultiRefBusinessDataInstance refInstance = mock(SMultiRefBusinessDataInstance.class);
        final String bizDataName = "employee";
        final int processInstanceId = 457;
        when(refBusinessDataService.getRefBusinessDataInstance(bizDataName, processInstanceId)).thenReturn(refInstance);
        when(refInstance.getDataIds()).thenReturn(Arrays.asList(45l));
        when(refInstance.getDataClassName()).thenReturn(Employee.class.getName());

        leftOperandHandler.getBusinessData(bizDataName, processInstanceId, "PROCESS_INSTANCE");

        verify(repository).findByIds(Employee.class, Arrays.asList(45l));
    }

    @Test
    public void getMultiBusinessDataCreateAnInstanceIfNoReferenceExists() throws Exception {
        final SMultiRefBusinessDataInstance refInstance = mock(SMultiRefBusinessDataInstance.class);
        final String bizDataName = "employee";
        final long processInstanceId = 457;
        when(flowNodeInstanceService.getProcessInstanceId(processInstanceId, "PROCESS_INSTANCE")).thenReturn(processInstanceId);
        when(refBusinessDataService.getRefBusinessDataInstance(bizDataName, processInstanceId)).thenReturn(refInstance);
        when(refInstance.getDataIds()).thenReturn(new ArrayList<Long>());
        when(refInstance.getDataClassName()).thenReturn(Employee.class.getName());

        final List<Employee> employees = (List<Employee>) leftOperandHandler.getBusinessData(bizDataName, processInstanceId, "PROCESS_INSTANCE");
        assertThat(employees).isEmpty();
    }

}
