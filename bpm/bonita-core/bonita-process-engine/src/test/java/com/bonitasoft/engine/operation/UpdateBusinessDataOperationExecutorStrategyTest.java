package com.bonitasoft.engine.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.bonitasoft.engine.core.operation.model.impl.SOperationImpl;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.bonitasoft.engine.operation.OperatorType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;

@RunWith(MockitoJUnitRunner.class)
public class UpdateBusinessDataOperationExecutorStrategyTest {

	@Mock
	private BusinessDataRepository businessDataRepository;

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
		Map<String, Serializable> inputValues = new HashMap<String, Serializable>(1);
		SExpressionContext expressionContext = new SExpressionContext(-1L, "unused", inputValues);

		// when:
		spy.getBusinessDataObjectAndPutInContextIfNotAlready(1L, "toto", expressionContext, bizDataName);

		// then:
		assertThat(expressionContext.getInputValues().get(bizDataName)).isEqualTo(myTravel);
	}

	@Test(expected=SOperationExecutionException.class)
	public void shouldUpdate_ThrowAnSOperationExecutionException() throws Exception {
		// given:
		InvalidTravel myTravel = new InvalidTravel();

		UpdateBusinessDataOperationExecutorStrategy spy = spy(updateBizDataStrategy);
		when(businessDataRepository.merge(any())).thenReturn(myTravel);
		doReturn(myTravel).when(spy).getBusinessData(anyString(), anyLong());
		SLeftOperand leftOp = mock(SLeftOperand.class);
		when(leftOp.getName()).thenReturn("bizData");
		updateBizDataStrategy.update(leftOp, new Object(), 1L, "");

	}

	public class InvalidTravel implements Serializable {

		private static final long serialVersionUID = 1L;

		private int nbDays;

		public int getNbDays() {
			return nbDays;
		}

		public void setNbDays(final int nbDays) {
			this.nbDays = nbDays;
		}
	}

	public class Travel implements Serializable,Entity {

		private static final long serialVersionUID = 1L;

		private int nbDays;

		public int getNbDays() {
			return nbDays;
		}

		public void setNbDays(final int nbDays) {
			this.nbDays = nbDays;
		}

		@Override
		public Long getPersistenceId() {
			return 1L;
		}

		@Override
		public Long getPersistenceVersion() {
			return 1L;
		}
	}

	@Test
	public void shouldGetOperatorType_Return_BUSINESS_DATA_JAVA_SETTER() throws Exception {
		assertThat(updateBizDataStrategy.getOperationType()).isEqualTo(OperatorType.BUSINESS_DATA_JAVA_SETTER.name());
	}

}
