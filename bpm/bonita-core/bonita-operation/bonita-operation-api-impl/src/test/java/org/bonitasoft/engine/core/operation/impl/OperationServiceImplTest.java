package org.bonitasoft.engine.core.operation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.LeftOperandHandler;
import org.bonitasoft.engine.core.operation.LeftOperandHandlerProvider;
import org.bonitasoft.engine.core.operation.OperationExecutorStrategy;
import org.bonitasoft.engine.core.operation.OperationExecutorStrategyProvider;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.SOperatorType;
import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.bonitasoft.engine.core.operation.model.impl.SOperationImpl;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OperationServiceImplTest {

    private final class MatchLeftOperandName extends BaseMatcher<SLeftOperand> {

        private final String name;

        /**
         * @param string
         */
        public MatchLeftOperandName(final String name) {
            this.name = name;
        }

        @Override
        public boolean matches(final Object item) {
            return item instanceof SLeftOperand && name.equals(((SLeftOperand) item).getName());
        }

        @Override
        public void describeTo(final Description description) {
        }
    }

    private final class MatchOperationType extends BaseMatcher<SOperation> {

        private final SOperatorType type;

        /**
         * @param string
         */
        public MatchOperationType(final SOperatorType type) {
            this.type = type;
        }

        @Override
        public boolean matches(final Object item) {
            return item instanceof SOperation && type.equals(((SOperation) item).getType());
        }

        @Override
        public void describeTo(final Description description) {
        }
    }

    @Mock
    private ExpressionResolverService expressionResolverService;

    @Mock
    private LeftOperandHandlerProvider leftOperandHandlerProvider;

    @Mock
    private LeftOperandHandler leftOperandHandler1;

    @Mock
    private LeftOperandHandler leftOperandHandler2;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private OperationExecutorStrategyProvider operationExecutorStrategyProvider;

    @Mock
    private OperationExecutorStrategy operationExecutorStrategy1;

    @Mock
    private OperationExecutorStrategy operationExecutorStrategy2;

    private OperationServiceImpl operationServiceImpl;

    @Before
    public void before() throws SOperationExecutionException {
        doReturn("type1").when(leftOperandHandler1).getType();
        doReturn("type2").when(leftOperandHandler2).getType();
        doReturn(SOperatorType.ASSIGNMENT.name()).when(operationExecutorStrategy1).getOperationType();
        doReturn(SOperatorType.XPATH_UPDATE_QUERY.name()).when(operationExecutorStrategy2).getOperationType();
        doReturn(operationExecutorStrategy1).when(operationExecutorStrategyProvider).getOperationExecutorStrategy(
                argThat(new MatchOperationType(SOperatorType.ASSIGNMENT)));
        doReturn(operationExecutorStrategy2).when(operationExecutorStrategyProvider).getOperationExecutorStrategy(
                argThat(new MatchOperationType(SOperatorType.XPATH_UPDATE_QUERY)));
        doReturn(Arrays.asList(leftOperandHandler1, leftOperandHandler2)).when(leftOperandHandlerProvider).getLeftOperandHandlers();
        operationServiceImpl = new OperationServiceImpl(operationExecutorStrategyProvider, leftOperandHandlerProvider, expressionResolverService, logger);
    }

    @Test
    public void should_UpdateLeftOperands_call_leftOperandHandlers() throws Exception {
        // given
        SOperation op1 = createOperation("type1", "data1", SOperatorType.ASSIGNMENT);
        SOperation op2 = createOperation("type2", "data2", SOperatorType.XPATH_UPDATE_QUERY);
        HashMap<String, Object> inputValues = new HashMap<String, Object>();
        inputValues.put("data1", "value1");
        inputValues.put("data2", "value2");
        SExpressionContext expressionContext = new SExpressionContext(123l, "containerType", inputValues);
        // when
        operationServiceImpl.updateLeftOperands(Arrays.asList(op1, op2), 123, "containerType", expressionContext);

        // then
        verify(leftOperandHandler1, times(1)).update(argThat(new MatchLeftOperandName("data1")), eq("value1"), eq(123l), eq("containerType"));
        verify(leftOperandHandler2, times(1)).update(argThat(new MatchLeftOperandName("data2")), eq("value2"), eq(123l), eq("containerType"));
    }

    @Test
    public void should_retrieveLeftOperandsAndPutItInExpressionContextIfNotIn_call_leftOperandHandlers() throws Exception {
        // given
        SOperation op1 = createOperation("type1", "data1", SOperatorType.ASSIGNMENT);
        SOperation op2 = createOperation("type2", "data2", SOperatorType.XPATH_UPDATE_QUERY);
        SExpressionContext expressionContext = new SExpressionContext(123l, "containerType", new HashMap<String, Object>());
        doReturn("value1").when(leftOperandHandler1).retrieve(argThat(new MatchLeftOperandName("data1")), any(SExpressionContext.class));
        doReturn("value2").when(leftOperandHandler2).retrieve(argThat(new MatchLeftOperandName("data2")), any(SExpressionContext.class));
        // when
        operationServiceImpl.retrieveLeftOperandsAndPutItInExpressionContextIfNotIn(Arrays.asList(op1, op2), 123, "containerType", expressionContext);

        // then
        verify(leftOperandHandler1, times(1)).retrieve(eq(op1.getLeftOperand()), any(SExpressionContext.class));
        verify(leftOperandHandler2, times(1)).retrieve(eq(op2.getLeftOperand()), any(SExpressionContext.class));
        assertThat(expressionContext.getInputValues().get("data1")).isEqualTo("value1");
        assertThat(expressionContext.getInputValues().get("data2")).isEqualTo("value2");
    }

    @Test
    public void should_executeOperator_call_OperationStrategies() throws Exception {
        // given
        SOperation op1 = createOperation("type1", "data1", SOperatorType.ASSIGNMENT);
        SOperation op2 = createOperation("type2", "data2", SOperatorType.XPATH_UPDATE_QUERY);
        HashMap<String, Object> inputValues = new HashMap<String, Object>();
        SExpressionContext expressionContext = new SExpressionContext(123l, "containerType", inputValues);
        doReturn("value1").when(operationExecutorStrategy1).computeNewValueForLeftOperand(op1, null, expressionContext);
        doReturn("value2").when(operationExecutorStrategy2).computeNewValueForLeftOperand(op2, null, expressionContext);

        // when
        operationServiceImpl.executeOperators(Arrays.asList(op1, op2), expressionContext);

        // then
        assertThat(expressionContext.getInputValues().get("data1")).isEqualTo("value1");
        assertThat(expressionContext.getInputValues().get("data2")).isEqualTo("value2");
    }

    @Test
    public void should_retrieveLeftOperandsAndPutItInExpressionContextIfNotIn_work_when_left_op_return_null() throws Exception {
        // given
        SOperation op1 = createOperation("type1", "data1", SOperatorType.ASSIGNMENT);
        SExpressionContext expressionContext = new SExpressionContext(123l, "containerType", new HashMap<String, Object>());
        doReturn(null).when(leftOperandHandler1).retrieve(argThat(new MatchLeftOperandName("data1")), any(SExpressionContext.class));
        // when
        operationServiceImpl.retrieveLeftOperandsAndPutItInExpressionContextIfNotIn(Arrays.asList(op1), 123, "containerType", expressionContext);

        // then
        verify(leftOperandHandler1, times(1)).retrieve(eq(op1.getLeftOperand()), any(SExpressionContext.class));
        assertThat(expressionContext.getInputValues().containsKey("data1")).isEqualTo(false);
    }

    @Test
    public void should_retrieveLeftOperandsAndPutItInExpressionContextIfNotIn_do_not_override_value_in_map() throws Exception {
        // given
        SOperation op1 = createOperation("type1", "data1", SOperatorType.ASSIGNMENT);
        SExpressionContext expressionContext = new SExpressionContext(123l, "containerType", Collections.<String, Object> singletonMap("data1", "givenValue"));
        doReturn("leftOpValue").when(leftOperandHandler1).retrieve(argThat(new MatchLeftOperandName("data1")), any(SExpressionContext.class));
        // when
        operationServiceImpl.retrieveLeftOperandsAndPutItInExpressionContextIfNotIn(Arrays.asList(op1), 123, "containerType", expressionContext);

        // then
        verify(leftOperandHandler1, times(1)).retrieve(eq(op1.getLeftOperand()), any(SExpressionContext.class));
        assertThat(expressionContext.getInputValues().get("data1")).isEqualTo("givenValue");
    }

    @Test
    public void should_execute_call_retrieve_operator_update() throws Exception {
        // given
        SOperation op1 = createOperation("type1", "data1", SOperatorType.ASSIGNMENT);
        SExpressionContext expressionContext = new SExpressionContext(123l, "containerType", Collections.<String, Object> singletonMap("data1", "givenValue"));
        OperationServiceImpl spy = spy(operationServiceImpl);
        InOrder inOrder = inOrder(spy);
        doNothing().when(spy).retrieveLeftOperandsAndPutItInExpressionContextIfNotIn(anyList(), anyLong(), anyString(), any(SExpressionContext.class));
        doNothing().when(spy).executeOperators(anyList(), any(SExpressionContext.class));
        doNothing().when(spy).updateLeftOperands(anyList(), anyLong(), anyString(), any(SExpressionContext.class));
        // when
        spy.execute(op1, 123, "containerType", expressionContext);

        // then
        inOrder.verify(spy).retrieveLeftOperandsAndPutItInExpressionContextIfNotIn(anyList(), anyLong(), anyString(), any(SExpressionContext.class));
        inOrder.verify(spy).executeOperators(anyList(), any(SExpressionContext.class));
        inOrder.verify(spy).updateLeftOperands(anyList(), anyLong(), anyString(), any(SExpressionContext.class));

    }

    /**
     * @param string
     * @param string2
     * @param assignment
     * @return
     */
    private SOperation createOperation(final String type, final String data, final SOperatorType operatorType) {
        SOperationImpl sOperationImpl = new SOperationImpl();
        SLeftOperandImpl leftOperand = new SLeftOperandImpl();
        leftOperand.setType(type);
        leftOperand.setName(data);
        sOperationImpl.setLeftOperand(leftOperand);
        sOperationImpl.setType(operatorType);
        return sOperationImpl;
    }

}
