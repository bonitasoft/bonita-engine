package com.bonitasoft.engine.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.bonitasoft.engine.business.data.BusinessDataRespository;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;

@RunWith(MockitoJUnitRunner.class)
public class InsertBusinessDataOperationExecutorStrategyTest {

    @Mock
    private BusinessDataRespository respository;

    @Mock
    private RefBusinessDataService service;

    @InjectMocks
    private InsertBusinessDataOperationExecutorStrategy strategy;

    @Test
    public void returnTheGivenValue() throws SBonitaException {
        final Employee employee = new Employee(48578l, "firstName", "lastName");

        final Object value = strategy.getValue(null, employee, -56, null, null);
        assertThat(value).isEqualTo(employee);
    }

    @Test(expected = SOperationExecutionException.class)
    public void throwExceptionWhenGivingANullValue() throws SBonitaException {
        strategy.getValue(null, null, -56, null, null);
    }

    @Test
    public void insertBusinessData() throws SBonitaException {
        final long dataId = 789l;
        final Employee employee = new Employee(dataId, "firstName", "lastName");
        final long processInstanceId = 76846321l;

        final SLeftOperand leftOperand = mock(SLeftOperand.class);
        final SRefBusinessDataInstance instance = mock(SRefBusinessDataInstance.class);
        when(leftOperand.getName()).thenReturn("unused");
        when(service.getRefBusinessDataInstance("unused", processInstanceId)).thenReturn(instance);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                return null;
            }

        }).when(service).updateRefBusinessDataInstance(instance, dataId);

        strategy.update(leftOperand, employee, processInstanceId, "any");

        verify(respository).persist(employee);
        verify(service).getRefBusinessDataInstance("unused", processInstanceId);
        verify(service).updateRefBusinessDataInstance(instance, dataId);
    }
}
