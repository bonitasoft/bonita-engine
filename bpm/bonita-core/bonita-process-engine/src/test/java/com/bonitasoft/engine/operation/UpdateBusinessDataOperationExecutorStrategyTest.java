package com.bonitasoft.engine.operation;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.bonitasoft.engine.core.operation.model.impl.SOperationImpl;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.data.BusinessDataRespository;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;

@RunWith(MockitoJUnitRunner.class)
public class UpdateBusinessDataOperationExecutorStrategyTest {

    @Mock
    private BusinessDataRespository businessDataRepository;

    @Mock
    private FlowNodeInstanceService flowNodeInstanceService;

    @Mock
    private RefBusinessDataService refBusinessDataService;

    @InjectMocks
    private UpdateBusinessDataOperationExecutorStrategy updateBizDataStrategy;

    @Test
    public void getValueOnBizDataExistingInContextShouldReturnJavaEvaluatedObject() throws Exception {
        final String bizDataName = "travel";
        final int nbDaysToSet = 25;

        UpdateBusinessDataOperationExecutorStrategy spy = spy(updateBizDataStrategy);
        doReturn("setNbDays").when(spy).getOperator(any(SOperation.class));
        doReturn("int").when(spy).getOperatorParameterClassName(any(SOperation.class));
        doReturn(new Travel()).when(spy).getBusinessDataObjectAndPutInContextIfNotAlready(anyLong(), anyString(), any(SExpressionContext.class), anyString());

        SOperationImpl operation = new SOperationImpl();
        SLeftOperandImpl leftOperand = new SLeftOperandImpl();
        leftOperand.setName(bizDataName);
        operation.setLeftOperand(leftOperand);
        SExpressionImpl rightOperand = new SExpressionImpl();
        rightOperand.setReturnType(Integer.class.getName());
        operation.setRightOperand(rightOperand);

        Travel realValue = (Travel) spy.getValue(operation, nbDaysToSet, -1L, "unused", null);

        assertThat(realValue.getNbDays()).isEqualTo(nbDaysToSet);
    }

    @Test
    public void getBusinessObjectShouldPutItInContextIfNotAlready() throws Exception {
        // given:
        String bizDataName = "myTravel";
        Travel myTravel = new Travel();

        UpdateBusinessDataOperationExecutorStrategy spy = spy(updateBizDataStrategy);
        doReturn(myTravel).when(spy).getBusinessData(anyString(), anyLong());
        doReturn(4115L).when(spy).getProcessInstanceId(anyLong(), anyString());
        Map<String, Serializable> inputValues = new HashMap<String, Serializable>(1);
        SExpressionContext expressionContext = new SExpressionContext(-1L, "unused", inputValues);

        // when:
        spy.getBusinessDataObjectAndPutInContextIfNotAlready(1L, "toto", expressionContext, bizDataName);

        // then:
        assertThat(expressionContext.getInputValues().get(bizDataName)).isEqualTo(myTravel);
    }

    public class Travel implements Serializable {

        private static final long serialVersionUID = 1L;

        private int nbDays;

        public int getNbDays() {
            return nbDays;
        }

        public void setNbDays(final int nbDays) {
            this.nbDays = nbDays;
        }
    }

}
