package com.bonitasoft.engine.expression;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.bonitasoft.engine.expression.ExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.data.BusinessDataRespository;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;

@RunWith(MockitoJUnitRunner.class)
public class BusinessDataExpressionExecutorStrategyTest {

    @Mock
    private BusinessDataRespository businessDataRepository;

    @Mock
    private RefBusinessDataService refBusinessDataService;

    @InjectMocks
    private BusinessDataExpressionExecutorStrategy businessDataExpressionExecutorStrategy;

    @Test
    public void getExpressionKindShouldReturnBusinessDataExpressionKind() throws Exception {
        assertThat(businessDataExpressionExecutorStrategy.getExpressionKind()).isEqualTo(ExpressionExecutorStrategy.KIND_BUSINESS_DATA);
    }

    @Test(expected = SInvalidExpressionException.class)
    public void validateWithWrongExpressionTypeShouldThrowException() throws Exception {
        fail("NYI");
    }

    @Test
    public void evaluateExistingBDExpressionShouldReturnFoundBusinessData() throws Exception {
        // given:
        String businessDataName = "myLeaveRequest";
        String returnType = "com.bonitasoft.engine.expression.BusinessDataExpressionExecutorStrategyTest.LeaveRequest";
        SExpression expression = buildBusinessDataExpression(businessDataName, returnType);
        // Map<String, Object> map = new HashMap<String, Object>();
        // Map<Integer, Object> resolvedExpressions=new HashMap<String, Object>();

        // when:
        Object object = businessDataExpressionExecutorStrategy.evaluate(expression, null, null);

        // then:
        assertThat(object).isInstanceOf(LeaveRequest.class);
    }

    @Test
    public void mustPutEvaluatedExpressionInContextShouldReturnTrue() throws Exception {
        assertThat(businessDataExpressionExecutorStrategy.mustPutEvaluatedExpressionInContext()).isEqualTo(true);
    }

    private SExpression buildBusinessDataExpression(final String content, final String returnType) {
        final SExpressionImpl eb = new SExpressionImpl();
        eb.setName(content);
        eb.setContent(content);
        eb.setReturnType(returnType);
        eb.setDependencies(null);
        return eb;
    }

    class LeaveRequest {

    }

}
