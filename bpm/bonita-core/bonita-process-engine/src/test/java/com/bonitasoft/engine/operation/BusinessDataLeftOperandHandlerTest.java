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
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.Container;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.model.SMultiRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.SSimpleRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.impl.SProcessSimpleRefBusinessDataInstanceImpl;
import com.bonitasoft.engine.core.process.instance.model.impl.SSimpleRefBusinessDataInstanceImpl;
import com.bonitasoft.engine.operation.pojo.Employee;
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

    @Mock
    private EntitiesActionsExecutor actionsExecutor;

    @Mock
    private UpdateDataRefAction action;

    @InjectMocks
    private BusinessDataLeftOperandHandler leftOperandHandler;

    @Test
    public void should_retrieve_retrieve_the_business_data() throws Exception {
        // given:
        final String bizDataName = "myTravel";
        final Travel myTravel = new Travel();

        final BusinessDataLeftOperandHandler spy = spy(leftOperandHandler);
        doReturn(myTravel).when(spy).getBusinessData(anyString(), anyLong(), anyString());
        final Map<String, Serializable> inputValues = new HashMap<String, Serializable>(1);
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

    private SSimpleRefBusinessDataInstance createRefBusinessDataInstance(final Long dataId) {
        final SSimpleRefBusinessDataInstanceImpl sRefBusinessDataInstanceImpl = new SProcessSimpleRefBusinessDataInstanceImpl();
        sRefBusinessDataInstanceImpl.setDataId(dataId);
        return sRefBusinessDataInstanceImpl;
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

    @Test
    public void update_should_execute_UpdateDataRefAction() throws Exception {
        //given
        Address address = new Address(15L);
        String businessDataName = "address";
        long containerId = 1L;
        String containerType = "PROCESS";
        BusinessDataContext businessDataContext = new BusinessDataContext(businessDataName, new Container(containerId, containerType));
        given(actionsExecutor.executeAction(eq(address), Matchers.argThat(new BusinessDataContextMatcher(businessDataContext)), eq(action)))
                .willReturn(address);

        //when
        Object result = leftOperandHandler.update(createLeftOperand(businessDataName), Collections.<String, Object> emptyMap(), address, containerId,
                containerType);

        //then
        assertThat(result).isEqualTo(address);
    }

    @Test(expected = SOperationExecutionException.class)
    public void update_should_throws_operation_exception_when_update_data_ref_action_fails() throws Exception {
        //given
        Address address = new Address(15L);
        String businessDataName = "address";
        long containerId = 1L;
        String containerType = "PROCESS";
        BusinessDataContext businessDataContext = new BusinessDataContext(businessDataName, new Container(containerId, containerType));
        given(actionsExecutor.executeAction(eq(address), Matchers.argThat(new BusinessDataContextMatcher(businessDataContext)), eq(action))).willThrow(
                new SEntityActionExecutionException(""));

        //when
        leftOperandHandler.update(createLeftOperand(businessDataName), Collections.<String, Object> emptyMap(), address, containerId, containerType);

        //then exception
    }

}
