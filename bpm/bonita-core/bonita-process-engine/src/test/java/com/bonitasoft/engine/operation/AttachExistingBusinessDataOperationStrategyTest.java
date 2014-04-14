package com.bonitasoft.engine.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.operation.OperatorType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;

@RunWith(MockitoJUnitRunner.class)
public class AttachExistingBusinessDataOperationStrategyTest {

    @Mock
    private FlowNodeInstanceService flowNodeInstanceService;

    @Mock
    private RefBusinessDataService refBusinessDataService;

    @InjectMocks
    private AttachExistingBusinessDataOperationStrategy attachExistingBusinessDataOperationStrategy;

    @Test(expected = SOperationExecutionException.class)
    public void getValueShouldThrowExceptionIfRightOperandIsNull() throws Exception {
        attachExistingBusinessDataOperationStrategy.getValue(null, null, 0, null, null);
    }

    @Test(expected = SOperationExecutionException.class)
    public void getValueShouldThrowExceptionIfRightOperandIsNotABusinessData() throws Exception {
        attachExistingBusinessDataOperationStrategy.getValue(null, "a string", 0, null, null);
    }

    @Test
    public void getValueShouldReturnPersistenceId() throws Exception {
        Peticion peticion = new Peticion();

        Object value = attachExistingBusinessDataOperationStrategy.getValue(null, peticion, 0, null, null);

        assertThat(value).isEqualTo(peticion.getPersistenceId());
    }

    @Test
    public void updateShouldSetReferenceForNonNullBizDataId() throws Exception {
        AttachExistingBusinessDataOperationStrategy spy = spy(attachExistingBusinessDataOperationStrategy);
        SRefBusinessDataInstance refBiz = mock(SRefBusinessDataInstance.class);
        Long bizDataId = 98744L;
        SLeftOperand sLeftOperand = mock(SLeftOperand.class);
        doReturn(refBiz).when(spy).getRefBusinessDataInstance(anyString(), anyLong(), anyString());

        spy.update(sLeftOperand, bizDataId, 9L, "some container");

        verify(refBusinessDataService).updateRefBusinessDataInstance(refBiz, bizDataId);
    }

    @Test
    public void getOperationTypeShouldBeATTACH_EXISTING_BUSINESS_DATA() throws Exception {
        assertThat(attachExistingBusinessDataOperationStrategy.getOperationType()).isEqualTo(OperatorType.ATTACH_EXISTING_BUSINESS_DATA.name());
    }

    class Peticion implements Entity {

        @Override
        public Long getPersistenceId() {
            return 554477L;
        }

        @Override
        public Long getPersistenceVersion() {
            return null;
        }

    }
}
