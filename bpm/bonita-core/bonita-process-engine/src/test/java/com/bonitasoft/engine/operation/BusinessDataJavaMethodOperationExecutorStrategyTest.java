package com.bonitasoft.engine.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.impl.JavaMethodOperationExecutorStrategy;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataService;

@RunWith(MockitoJUnitRunner.class)
public class BusinessDataJavaMethodOperationExecutorStrategyTest {

    private static final String PARAMETER_TYPE = "org.pojo.Address";

    private static final String METHOD_NAME = "setAddress";

    private static final String DATA_TO_SET = "employee";

    @Mock
    private BusinessDataService businessDataService;

    @Mock
    private JavaMethodOperationExecutorStrategy javaMethodOperationExecutorStrategy;

    @InjectMocks
    @Spy
    private BusinessDataJavaMethodOperationExecutorStrategy strategy;

    @Mock
    private SExpressionContext context;

    @Mock
    private Object valuetoSetObjectWith;

    @Mock
    private SOperation operation;

    @Mock
    private SLeftOperand leftOperand;

    @Mock
    private Entity businessData;

    @Before
    public void before() throws Exception {
        doReturn(leftOperand).when(operation).getLeftOperand();
    }

    @Test
    public void getOperationType() throws Exception {
        //when
        final String operationType = strategy.getOperationType();

        //then
        assertThat(operationType).as("should get opration type").isEqualTo(JavaMethodOperationExecutorStrategy.TYPE_JAVA_METHOD);

    }

    @Test
    public void shouldDelegateToBusinessDataService() throws Exception {
        //given
        final String methodName = METHOD_NAME;
        final String parameterType = PARAMETER_TYPE;
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(DATA_TO_SET, businessData);

        doReturn(SLeftOperand.TYPE_BUSINESS_DATA).when(leftOperand).getType();
        doReturn(DATA_TO_SET).when(leftOperand).getName();
        doReturn(methodName + ":" + parameterType).when(operation).getOperator();
        doReturn(map).when(context).getInputValues();

        //when
        strategy.computeNewValueForLeftOperand(operation, valuetoSetObjectWith, context);

        //then
        verify(businessDataService).callJavaOperation(businessData, valuetoSetObjectWith, methodName, parameterType);
        verifyZeroInteractions(javaMethodOperationExecutorStrategy);

    }

    @Test
    public void shouldNotDelegateToBusinessDataService() throws Exception {
        //given
        doReturn("not business data type").when(leftOperand).getType();
        doReturn(null).when(strategy).computeJavaOperation(operation, valuetoSetObjectWith, context);
        verifyNoMoreInteractions(businessDataService);

        //when
        strategy.computeNewValueForLeftOperand(operation, valuetoSetObjectWith, context);

        //then
        verify(strategy).computeJavaOperation(operation, valuetoSetObjectWith, context);

    }

    @Test
    @Ignore
    public void shouldSetListOnBusinessDataReplaceTheList() throws Exception {
        /*
         * new arrayList(adresses) adresses
         * client.setAdresses(adresses)
         */
        final Object computeNewValueForLeftOperand = strategy.computeNewValueForLeftOperand(operation, valuetoSetObjectWith, context);

        fail("not yet implemented");

    }

}
