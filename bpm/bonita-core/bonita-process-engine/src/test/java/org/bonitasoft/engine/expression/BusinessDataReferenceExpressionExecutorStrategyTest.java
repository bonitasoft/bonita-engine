/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 */

package org.bonitasoft.engine.expression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.business.data.BusinessDataReference;
import org.bonitasoft.engine.business.data.RefBusinessDataRetriever;
import org.bonitasoft.engine.business.data.impl.SimpleBusinessDataReferenceImpl;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.instance.api.exceptions.business.data.SRefBusinessDataInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.business.data.SFlowNodeSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SFlowNodeSimpleRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SProcessSimpleRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.bonitasoft.engine.operation.BusinessDataContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BusinessDataReferenceExpressionExecutorStrategyTest {

    @Mock
    private RefBusinessDataRetriever refBusinessDataRetriever;

    @InjectMocks
    private BusinessDataReferenceExpressionExecutorStrategy businessDataExpressionExecutorStrategy;


    @Test
    public void should_be_a_business_data_expression_kind_strategy() {
        assertThat(businessDataExpressionExecutorStrategy.getExpressionKind()).isEqualTo(ExpressionExecutorStrategy.KIND_BUSINESS_DATA_REFERENCE);
    }

    @Test
    public void should_evaluate_return_a_simple_ref_on_process() throws Exception {
        final String bizDataName = "BizDataName";
        final Map<String, Object> context = createContext(112l, "PROCESS_INSTANCE");
        doReturn(createProcessSimpleDataReference("BizDataName", 112l, "com.objects.MyObject", 150l))
                .when(refBusinessDataRetriever)
                .getRefBusinessDataInstance(
                        any());

        final Object evaluate = businessDataExpressionExecutorStrategy.evaluate(createExpression(bizDataName), context, null, null);

        assertThat(evaluate).isEqualTo(new SimpleBusinessDataReferenceImpl("BizDataName", "com.objects.MyObject", 150l));
    }

    @Test
    public void should_evaluate_return_a_simple_ref_on_activity() throws Exception {
        final String bizDataName = "BizDataName";
        final Map<String, Object> context = createContext(112l, "ACTIVITY_INSTANCE");
        doReturn(createFlowNodeSimpleDataReference("BizDataName", 112l, "com.objects.MyObject", 150l)).when(refBusinessDataRetriever)
                .getRefBusinessDataInstance(
                any());
        final Object evaluate = businessDataExpressionExecutorStrategy.evaluate(createExpression(bizDataName), context, null, null);

        assertThat(evaluate).isEqualTo(new SimpleBusinessDataReferenceImpl("BizDataName", "com.objects.MyObject", 150l));
    }

    @Test(expected = SExpressionEvaluationException.class)
    public void should_evaluate_throw_evaluation_exception_when_not_found() throws Exception {
        final String bizDataName = "BizDataName";
        final Map<String, Object> context = createContext(112l, "ACTIVITY_INSTANCE");
        doThrow(new SRefBusinessDataInstanceNotFoundException(112l, "exception")).when(refBusinessDataRetriever).getRefBusinessDataInstance(
                any(BusinessDataContext.class));
        businessDataExpressionExecutorStrategy.evaluate(createExpression(bizDataName), context, null, null);
    }

    SProcessSimpleRefBusinessDataInstance createProcessSimpleDataReference(final String name, final long processInstanceId, final String type, final long businessDataId) {
        final SProcessSimpleRefBusinessDataInstanceImpl sProcessSimpleRefBusinessDataInstance = new SProcessSimpleRefBusinessDataInstanceImpl();
        sProcessSimpleRefBusinessDataInstance.setName(name);
        sProcessSimpleRefBusinessDataInstance.setProcessInstanceId(processInstanceId);
        sProcessSimpleRefBusinessDataInstance.setDataClassName(type);
        sProcessSimpleRefBusinessDataInstance.setDataId(businessDataId);
        return sProcessSimpleRefBusinessDataInstance;
    }

    SFlowNodeSimpleRefBusinessDataInstance createFlowNodeSimpleDataReference(final String name, final long flowNodeInstanceId, final String type, final long businessDataId) {
        final SFlowNodeSimpleRefBusinessDataInstanceImpl sProcessSimpleRefBusinessDataInstance = new SFlowNodeSimpleRefBusinessDataInstanceImpl();
        sProcessSimpleRefBusinessDataInstance.setName(name);
        sProcessSimpleRefBusinessDataInstance.setFlowNodeInstanceId(flowNodeInstanceId);
        sProcessSimpleRefBusinessDataInstance.setDataClassName(type);
        sProcessSimpleRefBusinessDataInstance.setDataId(businessDataId);
        return sProcessSimpleRefBusinessDataInstance;
    }

    Map<String, Object> createContext(final long id, final String type) {
        final Map<String, Object> context = new HashMap<>();
        context.put(SExpressionContext.CONTAINER_ID_KEY, id);
        context.put(SExpressionContext.CONTAINER_TYPE_KEY, type);
        return context;
    }

    SExpressionImpl createExpression(final String bizDataName) {
        return new SExpressionImpl("theData", bizDataName, ExpressionExecutorStrategy.TYPE_BUSINESS_DATA_REFERENCE, BusinessDataReference.class.getName(), null, null);
    }

}
