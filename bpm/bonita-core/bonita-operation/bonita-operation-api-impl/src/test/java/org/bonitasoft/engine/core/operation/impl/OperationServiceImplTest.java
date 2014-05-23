package org.bonitasoft.engine.core.operation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
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

import org.assertj.core.data.MapEntry;
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
    private LeftOperandHandler leftOperandHandler3;

    @Mock
    private LeftOperandHandler leftOperandHandler4;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private OperationExecutorStrategyProvider operationExecutorStrategyProvider;

    @Mock
    private OperationExecutorStrategy operationExecutorStrategy1;

    @Mock
    private OperationExecutorStrategy operationExecutorStrategy2;

    @Mock
    private OperationExecutorStrategy operationExecutorStrategy3;

    private OperationServiceImpl operationServiceImpl;

    private SLeftOperandImpl buildLeftOperand(final String type, final String dataName) {
        final SLeftOperandImpl leftOperand = new SLeftOperandImpl();
        leftOperand.setType(type);
        leftOperand.setName(dataName);
        return leftOperand;
    }

    private SOperation buildOperation(final String type, final String dataName, final SOperatorType operatorType) {
        final SOperationImpl sOperationImpl = new SOperationImpl();
        sOperationImpl.setLeftOperand(buildLeftOperand(type, dataName));
        sOperationImpl.setType(operatorType);
        return sOperationImpl;
    }

    @Before
    public void before() throws SOperationExecutionException {
        doReturn("type1").when(leftOperandHandler1).getType();
        doReturn("type2").when(leftOperandHandler2).getType();
        doReturn("type3").when(leftOperandHandler3).getType();
        doReturn("type4").when(leftOperandHandler4).getType();
        when(leftOperandHandler1.supportBatchUpdate()).thenReturn(true);
        when(leftOperandHandler2.supportBatchUpdate()).thenReturn(true);
        when(leftOperandHandler3.supportBatchUpdate()).thenReturn(true);
        when(leftOperandHandler4.supportBatchUpdate()).thenReturn(false);
        doReturn(SOperatorType.ASSIGNMENT.name()).when(operationExecutorStrategy1).getOperationType();
        doReturn(SOperatorType.XPATH_UPDATE_QUERY.name()).when(operationExecutorStrategy2).getOperationType();
        doReturn(SOperatorType.JAVA_METHOD.name()).when(operationExecutorStrategy2).getOperationType();
        doReturn(operationExecutorStrategy1).when(operationExecutorStrategyProvider).getOperationExecutorStrategy(
                argThat(new MatchOperationType(SOperatorType.ASSIGNMENT)));
        doReturn(operationExecutorStrategy2).when(operationExecutorStrategyProvider).getOperationExecutorStrategy(
                argThat(new MatchOperationType(SOperatorType.XPATH_UPDATE_QUERY)));
        doReturn(operationExecutorStrategy3).when(operationExecutorStrategyProvider).getOperationExecutorStrategy(
                argThat(new MatchOperationType(SOperatorType.JAVA_METHOD)));
        doReturn(Arrays.asList(leftOperandHandler1, leftOperandHandler2, leftOperandHandler3, leftOperandHandler4)).when(leftOperandHandlerProvider)
                .getLeftOperandHandlers();
        operationServiceImpl = new OperationServiceImpl(operationExecutorStrategyProvider, leftOperandHandlerProvider, expressionResolverService, logger);
    }

    @Test
    public void should_UpdateLeftOperands_call_leftOperandHandlers() throws Exception {
        // given
        final Map<String, Object> inputValues = new HashMap<String, Object>();
        inputValues.put("data1", "value1");
        inputValues.put("data2", "value2");
        final SExpressionContext expressionContext = new SExpressionContext(123l, "containerType", inputValues);
        final Map<SLeftOperand, Boolean> updates = new HashMap<SLeftOperand, Boolean>();
        updates.put(buildLeftOperand("type1", "data1"), true);
        updates.put(buildLeftOperand("type2", "data2"), false);

        // when
        operationServiceImpl.updateLeftOperands(updates, 123, "containerType", expressionContext);

        // then
        verify(leftOperandHandler1).update(argThat(new MatchLeftOperandName("data1")), eq("value1"), eq(123l), eq("containerType"));
        verify(leftOperandHandler2).delete(argThat(new MatchLeftOperandName("data2")), eq(123l), eq("containerType"));
    }

    @Test
    public void should_retrieveLeftOperandsAndPutItInExpressionContextIfNotIn_call_leftOperandHandlers() throws Exception {
        // given
        final SOperation op1 = buildOperation("type3", "data1", SOperatorType.JAVA_METHOD);
        final SOperation op2 = buildOperation("type2", "data2", SOperatorType.XPATH_UPDATE_QUERY);
        final SExpressionContext expressionContext = new SExpressionContext(123l, "containerType", new HashMap<String, Object>());
        doReturn("value1").when(leftOperandHandler3).retrieve(argThat(new MatchLeftOperandName("data1")), any(SExpressionContext.class));
        doReturn("value2").when(leftOperandHandler2).retrieve(argThat(new MatchLeftOperandName("data2")), any(SExpressionContext.class));

        // when
        operationServiceImpl.retrieveLeftOperandsAndPutItInExpressionContextIfNotIn(Arrays.asList(op1, op2), 123, "containerType", expressionContext);

        // then
        verify(leftOperandHandler3, times(1)).retrieve(eq(op1.getLeftOperand()), any(SExpressionContext.class));
        verify(leftOperandHandler2, times(1)).retrieve(eq(op2.getLeftOperand()), any(SExpressionContext.class));
        assertThat(expressionContext.getInputValues().get("data1")).isEqualTo("value1");
        assertThat(expressionContext.getInputValues().get("data2")).isEqualTo("value2");
    }

    @Test
    public void should_executeOperator_call_OperationStrategies() throws Exception {
        // given
        final SOperation op1 = buildOperation("type1", "data1", SOperatorType.ASSIGNMENT);
        final SOperation op2 = buildOperation("type2", "data2", SOperatorType.XPATH_UPDATE_QUERY);
        final Map<String, Object> inputValues = new HashMap<String, Object>();
        final SExpressionContext expressionContext = new SExpressionContext(123l, "containerType", inputValues);
        doReturn("value1").when(operationExecutorStrategy1).computeNewValueForLeftOperand(op1, null, expressionContext);
        doReturn("value2").when(operationExecutorStrategy2).computeNewValueForLeftOperand(op2, null, expressionContext);

        // when
        operationServiceImpl.executeOperators(Arrays.asList(op1, op2), expressionContext);

        // then
        assertThat(expressionContext.getInputValues().get("data1")).isEqualTo("value1");
        assertThat(expressionContext.getInputValues().get("data2")).isEqualTo("value2");
    }

    @Test
    public void shouldNotRetrieveLeftOperandOnAssignementOperation() throws Exception {
        // given
        final String myDataName = "myDataName";
        final SOperation op1 = buildOperation("type1", myDataName, SOperatorType.ASSIGNMENT);
        final SExpressionContext expressionContext = new SExpressionContext(123l, "containerType", new HashMap<String, Object>(0));
        // doReturn(null).when(leftOperandHandler1).retrieve(argThat(new MatchLeftOperandName(myDataName)), any(SExpressionContext.class));

        // when
        operationServiceImpl.retrieveLeftOperandsAndPutItInExpressionContextIfNotIn(Arrays.asList(op1), 123, "containerType", expressionContext);

        // then
        verify(leftOperandHandler1, times(0)).retrieve(any(SLeftOperand.class), any(SExpressionContext.class));
    }

    @Test
    public void shouldNotPutInContextWhenLeftOperandRetrievesNullValue() throws Exception {
        // given
        final SOperation op1 = buildOperation("type2", "data1", SOperatorType.XPATH_UPDATE_QUERY);
        final SExpressionContext expressionContext = new SExpressionContext(123l, "containerType", new HashMap<String, Object>());
        doReturn(null).when(leftOperandHandler2).retrieve(argThat(new MatchLeftOperandName("data1")), any(SExpressionContext.class));
        // when
        operationServiceImpl.retrieveLeftOperandsAndPutItInExpressionContextIfNotIn(Arrays.asList(op1), 123, "containerType", expressionContext);

        // then
        assertThat(expressionContext.getInputValues()).doesNotContainKey("data1");
    }

    @Test
    public void should_retrieveLeftOperandsAndPutItInExpressionContextIfNotIn_do_not_override_value_in_map() throws Exception {
        // given
        final SOperation op1 = buildOperation("type2", "data1", SOperatorType.XPATH_UPDATE_QUERY);
        final SExpressionContext expressionContext = new SExpressionContext(123l, "containerType", Collections.<String, Object> singletonMap("data1",
                "originalValue"));
        when(leftOperandHandler2.retrieve(argThat(new MatchLeftOperandName("data1")), any(SExpressionContext.class))).thenReturn("newIgnoredValue");

        // when
        operationServiceImpl.retrieveLeftOperandsAndPutItInExpressionContextIfNotIn(Arrays.asList(op1), 123, "containerType", expressionContext);

        // then
        verify(leftOperandHandler2, times(1)).retrieve(eq(op1.getLeftOperand()), any(SExpressionContext.class));
        assertThat(expressionContext.getInputValues().get("data1")).isEqualTo("originalValue");
    }

    @Test
    public void executeShouldExecuteBatchMode() throws Exception {
        // given
        final SOperation op1 = buildOperation("type1", "data1", SOperatorType.ASSIGNMENT);
        final List<SOperation> operations = new ArrayList<SOperation>();
        operations.add(op1);
        final SExpressionContext expressionContext = new SExpressionContext(123l, "containerType", Collections.<String, Object> singletonMap("data1",
                "givenValue"));
        final OperationServiceImpl spy = spy(operationServiceImpl);
        final InOrder inOrder = inOrder(spy);
        doReturn(true).when(spy).useBatchMode(operations);

        // when
        spy.execute(operations, 123L, "containerType", expressionContext);

        // then
        inOrder.verify(spy).retrieveLeftOperandsAndPutItInExpressionContextIfNotIn(operations, 123L, "containerType", expressionContext);
        inOrder.verify(spy).executeOperators(operations, expressionContext);
        // inOrder.verify(spy).updateLeftOperands(operations, 123L, "containerType", expressionContext);
        verify(spy, never()).executeOperatorsAndUpdateLeftOperand(operations, expressionContext, 123L, "containerType");
    }

    @Test
    public void executeShouldNotExecuteBatchMode() throws Exception {
        final SOperation operation = buildOperation("type1", "data1", SOperatorType.ASSIGNMENT);
        final List<SOperation> operations = new ArrayList<SOperation>();
        operations.add(operation);
        final SExpressionContext expressionContext = new SExpressionContext(123l, "containerType", Collections.<String, Object> singletonMap("data1",
                "givenValue"));
        final OperationServiceImpl spy = spy(operationServiceImpl);
        doReturn(false).when(spy).useBatchMode(operations);

        spy.execute(operations, 123L, "process", expressionContext);

        verify(spy, never()).executeOperators(anyListOf(SOperation.class), any(SExpressionContext.class));
        // verify(spy, never()).updateLeftOperands(anyListOf(SOperation.class), anyLong(), anyString(), any(SExpressionContext.class));
        final InOrder inOrder = inOrder(spy);
        inOrder.verify(spy).retrieveLeftOperandsAndPutItInExpressionContextIfNotIn(operations, 123L, "process", expressionContext);
        inOrder.verify(spy).executeOperatorsAndUpdateLeftOperand(operations, expressionContext, 123L, "process");
    }

    @Test
    public void useBatchModeShouldBeUsedWithLeftOperandsWhichSupportBatch() throws Exception {
        final List<SOperation> operations = new ArrayList<SOperation>();
        operations.add(buildOperation("type1", "data1", SOperatorType.ASSIGNMENT));
        operations.add(buildOperation("type1", "data2", SOperatorType.ASSIGNMENT));

        final boolean useBatchMode = operationServiceImpl.useBatchMode(operations);
        assertThat(useBatchMode).isTrue();
    }

    @Test
    public void useBatchModeShouldBeUsedWithALeftOperandWhichSupportBatchAndAnotherOneNot() throws Exception {
        final List<SOperation> operations = new ArrayList<SOperation>();
        operations.add(buildOperation("type4", "data1", SOperatorType.ASSIGNMENT));
        operations.add(buildOperation("type1", "data2", SOperatorType.ASSIGNMENT));

        final boolean useBatchMode = operationServiceImpl.useBatchMode(operations);
        assertThat(useBatchMode).isTrue();
    }

    @Test
    public void useBatchModeShouldNotBeUsedWithLeftOperandsWhichNotSupportBatch() throws Exception {
        final List<SOperation> operations = new ArrayList<SOperation>();
        operations.add(buildOperation("type4", "data1", SOperatorType.ASSIGNMENT));
        operations.add(buildOperation("type4", "data2", SOperatorType.ASSIGNMENT));

        final boolean useBatchMode = operationServiceImpl.useBatchMode(operations);
        assertThat(useBatchMode).isFalse();
    }

    @Test
    public void useBatchModeShouldBeUsedWithALeftOperandWhichNotSupportBatchButUsedOnTheSameVariable() throws Exception {
        final List<SOperation> operations = new ArrayList<SOperation>();
        operations.add(buildOperation("type4", "data2", SOperatorType.ASSIGNMENT));
        operations.add(buildOperation("type4", "data2", SOperatorType.ASSIGNMENT));

        final boolean useBatchMode = operationServiceImpl.useBatchMode(operations);
        assertThat(useBatchMode).isTrue();
    }

    @Test
    public void useBatchModeShouldNotBeUsedWithALeftOperandWhichNotSupportBatchOnSeveralVariables() throws Exception {
        final List<SOperation> operations = new ArrayList<SOperation>();
        operations.add(buildOperation("type4", "data2", SOperatorType.ASSIGNMENT));
        operations.add(buildOperation("type4", "data2", SOperatorType.ASSIGNMENT));
        operations.add(buildOperation("type4", "data3", SOperatorType.ASSIGNMENT));

        final boolean useBatchMode = operationServiceImpl.useBatchMode(operations);
        assertThat(useBatchMode).isFalse();
    }

    @Test
    public void executeOperatorsShouldReturnASingleUpdateForTheSameDataOfTheSameOperator() throws Exception {
        // given
        final List<SOperation> operations = new ArrayList<SOperation>();
        operations.add(buildOperation("type1", "data1", SOperatorType.JAVA_METHOD));
        operations.add(buildOperation("type1", "data1", SOperatorType.JAVA_METHOD));
        final SExpressionContext expressionContext = new SExpressionContext(123l, "containerType", Collections.<String, Object> singletonMap("data1",
                "givenValue"));
        final OperationServiceImpl spy = spy(operationServiceImpl);

        // when
        final Map<SLeftOperand, Boolean> updates = spy.executeOperators(operations, expressionContext);

        // then
        assertThat(updates).hasSize(1).containsExactly(MapEntry.entry(buildLeftOperand("type1", "data1"), true));
    }

    @Test
    public void executeOperatorsShouldReturnATwoUpdatesForTheDifferentDataOfTheSameOperator() throws Exception {
        // given
        final List<SOperation> operations = new ArrayList<SOperation>();
        operations.add(buildOperation("type1", "data2", SOperatorType.JAVA_METHOD));
        operations.add(buildOperation("type1", "data1", SOperatorType.JAVA_METHOD));
        final SExpressionContext expressionContext = new SExpressionContext(123l, "containerType", Collections.<String, Object> singletonMap("data1",
                "givenValue"));
        final OperationServiceImpl spy = spy(operationServiceImpl);

        // when
        final Map<SLeftOperand, Boolean> updates = spy.executeOperators(operations, expressionContext);

        // then
        assertThat(updates).containsExactly(MapEntry.entry(buildLeftOperand("type1", "data2"), true), MapEntry.entry(buildLeftOperand("type1", "data1"), true));
    }

    @Test
    public void executeOperatorsShouldReturnASingleUpdateForTheSameDataOfDifferentOperators() throws Exception {
        // given
        final List<SOperation> operations = new ArrayList<SOperation>();
        operations.add(buildOperation("type1", "data1", SOperatorType.ASSIGNMENT));
        operations.add(buildOperation("type1", "data1", SOperatorType.JAVA_METHOD));
        final SExpressionContext expressionContext = new SExpressionContext(123l, "containerType", Collections.<String, Object> singletonMap("data1",
                "givenValue"));
        final OperationServiceImpl spy = spy(operationServiceImpl);
        doReturn(true).when(spy).useBatchMode(operations);

        // when
        final Map<SLeftOperand, Boolean> updates = spy.executeOperators(operations, expressionContext);

        // then
        assertThat(updates).hasSize(1).containsExactly(MapEntry.entry(buildLeftOperand("type1", "data1"), true));
    }

    @Test
    public void executeOperatorsShouldReturnASingleDeletionForTheSameDataOfDifferentOperators() throws Exception {
        // given
        final List<SOperation> operations = new ArrayList<SOperation>();
        operations.add(buildOperation("type1", "data1", SOperatorType.ASSIGNMENT));
        operations.add(buildOperation("type1", "data1", SOperatorType.DELETION));
        final SExpressionContext expressionContext = new SExpressionContext(123l, "containerType", Collections.<String, Object> singletonMap("data1",
                "givenValue"));
        final OperationServiceImpl spy = spy(operationServiceImpl);
        doReturn(true).when(spy).useBatchMode(operations);

        // when
        final Map<SLeftOperand, Boolean> updates = spy.executeOperators(operations, expressionContext);

        // then
        assertThat(updates).hasSize(1).containsExactly(MapEntry.entry(buildLeftOperand("type1", "data1"), false));
    }

    @Test
    public void executeOperatorsShouldReturnASingleDeletionForTheSameDataOfSeveralOperators() throws Exception {
        // given
        final List<SOperation> operations = new ArrayList<SOperation>();
        operations.add(buildOperation("type1", "data1", SOperatorType.DELETION));
        operations.add(buildOperation("type1", "data1", SOperatorType.ASSIGNMENT));
        final SExpressionContext expressionContext = new SExpressionContext(123l, "containerType", Collections.<String, Object> singletonMap("data1",
                "givenValue"));
        final OperationServiceImpl spy = spy(operationServiceImpl);
        doReturn(true).when(spy).useBatchMode(operations);

        // when
        final Map<SLeftOperand, Boolean> updates = spy.executeOperators(operations, expressionContext);

        // then
        assertThat(updates).hasSize(1).containsExactly(MapEntry.entry(buildLeftOperand("type1", "data1"), false));
    }

}
