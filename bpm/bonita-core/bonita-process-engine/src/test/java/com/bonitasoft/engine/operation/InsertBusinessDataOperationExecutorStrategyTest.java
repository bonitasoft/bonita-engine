package com.bonitasoft.engine.operation;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.data.BusinessDataRespository;

@RunWith(MockitoJUnitRunner.class)
public class InsertBusinessDataOperationExecutorStrategyTest {

    @Mock
    private BusinessDataRespository respository;

    @InjectMocks
    private InsertBusinessDataOperationExecutorStrategy strategy;

    @Test
    public void returnTheGivenValue() throws SBonitaException {
        final Employee employee = new Employee("firstName", "lastName");

        final Object value = strategy.getValue(null, employee, -56, null, null);
        assertThat(value).isEqualTo(employee);
    }

    @Test(expected = SOperationExecutionException.class)
    public void throwExceptionWhenGivingANullValue() throws SBonitaException {
        strategy.getValue(null, null, -56, null, null);
    }

    @Test
    public void insertBusinessData() throws SBonitaException {
        final Employee employee = new Employee("firstName", "lastName");

        final SLeftOperand leftOperand = mock(SLeftOperand.class);
        when(leftOperand.getName()).thenReturn("unused");

        strategy.update(leftOperand, employee, -45, "any");

        verify(respository).persist(employee);
    }

}
