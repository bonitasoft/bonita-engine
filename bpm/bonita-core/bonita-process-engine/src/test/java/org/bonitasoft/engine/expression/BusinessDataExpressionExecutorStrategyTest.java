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
 **/
package org.bonitasoft.engine.expression;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.BusinessDataRetriever;
import org.bonitasoft.engine.business.data.RefBusinessDataRetriever;
import org.bonitasoft.engine.business.data.proxy.ServerProxyfier;
import org.bonitasoft.engine.commons.Container;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SExceptionContext;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.instance.api.exceptions.business.data.SRefBusinessDataInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.bonitasoft.engine.operation.BusinessDataContext;
import org.bonitasoft.engine.operation.BusinessDataContextMatcher;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BusinessDataExpressionExecutorStrategyTest {

    @Mock
    ServerProxyfier proxyfier;

    @Mock
    private BusinessDataRetriever businessDataRetriever;

    @Mock
    private RefBusinessDataRetriever refBusinessDataRetriever;

    @InjectMocks
    private BusinessDataExpressionExecutorStrategy businessDataExpressionExecutorStrategy;

    private SimpleBizData createAbizDataInRepository() throws Exception {
        return createAbizDataInRepository(98L);
    }

    private SimpleBizData createAbizDataInRepository(final long bizDataId) throws Exception {
        return new SimpleBizData(bizDataId);
    }

    private SRefBusinessDataInstance createARefBizDataInRepository(final SimpleBizData bizData, final long processInstanceId) throws Exception {
        return createARefBizDataInRepository(bizData, "bizDataName", processInstanceId);
    }

    private SRefBusinessDataInstance createARefBizDataInRepository(final SimpleBizData bizData, final String bizDataName, final long processInstanceId)
            throws Exception {
        final SProcessSimpleRefBusinessDataInstance refBizData = mock(SProcessSimpleRefBusinessDataInstance.class);
        when(refBizData.getDataClassName()).thenReturn(bizData.getClass().getName());
        when(refBizData.getName()).thenReturn(bizDataName);
        when(refBizData.getProcessInstanceId()).thenReturn(processInstanceId);
        when(refBizData.getDataId()).thenReturn(bizData.getId());
        when(
                refBusinessDataRetriever.getRefBusinessDataInstance(argThat(new BusinessDataContextMatcher(new BusinessDataContext(refBizData.getName(),
                        new Container(processInstanceId, DataInstanceContainer.PROCESS_INSTANCE.name())))))).thenReturn(refBizData);
        given(businessDataRetriever.getBusinessData(refBizData)).willReturn(bizData);
        return refBizData;
    }

    private SRefBusinessDataInstance createAMultiRefBizDataInRepository(final SimpleBizData bizData, final String bizDataName, final long processInstanceId)
            throws Exception {
        final SProcessMultiRefBusinessDataInstance refBizData = mock(SProcessMultiRefBusinessDataInstance.class);
        when(refBizData.getDataClassName()).thenReturn(bizData.getClass().getName());
        when(refBizData.getName()).thenReturn(bizDataName);
        when(refBizData.getProcessInstanceId()).thenReturn(processInstanceId);
        when(refBizData.getDataIds()).thenReturn(Arrays.asList(bizData.getId()));
        when(refBusinessDataRetriever.getRefBusinessDataInstance(argThat(new BusinessDataContextMatcher(new BusinessDataContext(refBizData.getName(),
                new Container(processInstanceId, DataInstanceContainer.PROCESS_INSTANCE.name())))))).thenReturn(refBizData);
        given(businessDataRetriever.getBusinessData(refBizData)).willReturn(Arrays.asList(bizData));
        return refBizData;
    }

    private HashMap<String, Object> buildBusinessDataExpressionContext(final long containerId, final DataInstanceContainer containerType) {
        final HashMap<String, Object> context = new HashMap<>();
        context.put(SExpressionContext.CONTAINER_ID_KEY, containerId);
        context.put(SExpressionContext.CONTAINER_TYPE_KEY, containerType.name());
        return context;
    }

    private SExpressionImpl buildBusinessDataExpression(final String content) {
        final SExpressionImpl expression = new SExpressionImpl();
        expression.setContent(content);
        expression.setReturnType("com.bonitasoft.engine.expression.BusinessDataExpressionExecutorStrategyTest.LeaveRequest");
        expression.setExpressionType(ExpressionType.TYPE_BUSINESS_DATA.name());
        return expression;
    }

    @Test
    public void should_be_a_business_data_expression_kind_strategy() {
        assertThat(businessDataExpressionExecutorStrategy.getExpressionKind()).isEqualTo(ExpressionExecutorStrategy.KIND_BUSINESS_DATA);
    }

    @Test
    public void evaluate_on_a_process_instance_should_return_biz_data_instance_corresponding_to_data_name_and_processInstance_id() throws Exception {
        final SimpleBizData expectedBizData = createAbizDataInRepository();
        final long processInstanceId = 1L;
        final SRefBusinessDataInstance refBizData = createARefBizDataInRepository(expectedBizData, processInstanceId);
        final HashMap<String, Object> context = buildBusinessDataExpressionContext(processInstanceId, DataInstanceContainer.PROCESS_INSTANCE);
        final SExpressionImpl buildBusinessDataExpression = buildBusinessDataExpression(refBizData.getName());

        final Entity fetchedBizData = (Entity) businessDataExpressionExecutorStrategy.evaluate(buildBusinessDataExpression, context, null,
                ContainerState.ACTIVE);

        assertThat(unProxyfyIdNeeded(fetchedBizData)).isEqualTo(expectedBizData);
    }

    @Test(expected = SExpressionEvaluationException.class)
    public void evaluate_on_a_process_instance_should_throw_an_exception_when_a_read_exception_occurs() throws Exception {
        final SimpleBizData expectedBizData = createAbizDataInRepository();
        final long processInstanceId = 1L;
        final SRefBusinessDataInstance refBizData = createARefBizDataInRepository(expectedBizData, processInstanceId);
        final HashMap<String, Object> context = buildBusinessDataExpressionContext(processInstanceId, DataInstanceContainer.PROCESS_INSTANCE);
        final SExpressionImpl buildBusinessDataExpression = buildBusinessDataExpression(refBizData.getName());

        when(
                refBusinessDataRetriever.getRefBusinessDataInstance(argThat(new BusinessDataContextMatcher(new BusinessDataContext(refBizData.getName(),
                        new Container(processInstanceId, DataInstanceContainer.PROCESS_INSTANCE.name())))))).thenThrow(
                new SBonitaReadException("internal error"));

        final Object fetchedBizData = businessDataExpressionExecutorStrategy.evaluate(buildBusinessDataExpression, context, null, ContainerState.ACTIVE);

        assertThat(fetchedBizData).isEqualTo(expectedBizData);
    }

    @Test
    public void evaluate_on_a_process_instance_should_return_list_of_biz_data_instance_corresponding_to_data_name_and_processInstance() throws Exception {
        final SimpleBizData bizData = new SimpleBizData(457L);
        final long proccessInstanceId = 1L;
        final SRefBusinessDataInstance refBizData = createAMultiRefBizDataInRepository(bizData, "bizData", proccessInstanceId);
        final HashMap<String, Object> context = buildBusinessDataExpressionContext(proccessInstanceId, DataInstanceContainer.PROCESS_INSTANCE);
        final SExpressionImpl buildBusinessDataExpression = buildBusinessDataExpression(refBizData.getName());

        @SuppressWarnings("unchecked")
        final List<Entity> fetchedBizDataList = (List<Entity>) businessDataExpressionExecutorStrategy.evaluate(buildBusinessDataExpression, context, null,
                ContainerState.ACTIVE);

        assertThat(unProxyfyIdNeeded(fetchedBizDataList.get(0))).isEqualTo(bizData);
    }

    @Test
    public void failingEvaluateShouldPutProcessInstanceIdInExceptionContext() throws Exception {
        final SimpleBizData expectedBizData = createAbizDataInRepository();
        final long proccessInstanceId = 1564L;
        final SRefBusinessDataInstance refBizData = createARefBizDataInRepository(expectedBizData, proccessInstanceId);
        final HashMap<String, Object> context = buildBusinessDataExpressionContext(proccessInstanceId, DataInstanceContainer.PROCESS_INSTANCE);
        final SExpressionImpl buildBusinessDataExpression = buildBusinessDataExpression(refBizData.getName());
        doThrow(new SRefBusinessDataInstanceNotFoundException(444L, "toto")).when(refBusinessDataRetriever).getRefBusinessDataInstance(
                any(BusinessDataContext.class));

        try {
            businessDataExpressionExecutorStrategy.evaluate(buildBusinessDataExpression, context, null, ContainerState.ACTIVE);
            fail("should throw Exception");
        } catch (final SBonitaException e) {
            assertThat(((SBonitaException) e.getCause()).getContext().get(SExceptionContext.PROCESS_INSTANCE_ID)).isEqualTo(proccessInstanceId);
        }
    }

    private SimpleBizData unProxyfyIdNeeded(final Entity fetchedBizData) {
        final SimpleBizData unProxyfyIfNeeded;
        unProxyfyIfNeeded = (SimpleBizData) ServerProxyfier.unProxyfyIfNeeded(fetchedBizData);
        return unProxyfyIfNeeded;
    }

    @Test
    public void evaluate_should_first_check_if_expression_already_evaluated_and_available_in_context() throws Exception {
        final HashMap<String, Object> context = new HashMap<>();
        final SimpleBizData expectedBizData = new SimpleBizData(12L);
        final String bizDataName = "businessDataName";
        context.put(bizDataName, expectedBizData);
        final SExpressionImpl buildBusinessDataExpression = buildBusinessDataExpression(bizDataName);

        final Object fetchedBizData = businessDataExpressionExecutorStrategy.evaluate(buildBusinessDataExpression, context, null, ContainerState.ACTIVE);

        assertThat(fetchedBizData).isEqualTo(expectedBizData);
        verifyZeroInteractions(businessDataRetriever);
        verifyZeroInteractions(refBusinessDataRetriever);
    }

    @Test
    public void evaluate_should_resolve_multiple_expressions() throws Exception {
        final SimpleBizData firstBizData = createAbizDataInRepository(1L);
        final SimpleBizData secondBizData = createAbizDataInRepository(2L);
        final long processInstanceId = 6L;
        final SRefBusinessDataInstance firstRefBizData = createARefBizDataInRepository(firstBizData, "dataOne", processInstanceId);
        final SRefBusinessDataInstance secondRefBizData = createARefBizDataInRepository(secondBizData, "dataTwo", processInstanceId);
        final HashMap<String, Object> context = buildBusinessDataExpressionContext(processInstanceId, DataInstanceContainer.PROCESS_INSTANCE);
        final SExpression firstbuildBusinessDataExpression = buildBusinessDataExpression(firstRefBizData.getName());
        final SExpression secondbuildBusinessDataExpression = buildBusinessDataExpression(secondRefBizData.getName());

        final List<Object> fetchedBizDatas = businessDataExpressionExecutorStrategy.evaluate(
                Arrays.asList(firstbuildBusinessDataExpression, secondbuildBusinessDataExpression), context, null, ContainerState.ACTIVE);

        assertThat(fetchedBizDatas).contains(firstBizData, secondBizData);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void evaluate_should_not_resolve_expression_with_same_content_as_already_resolved_one() throws Exception {
        final SExpression firstbuildBusinessDataExpression = buildBusinessDataExpression("sameName");
        final SExpression secondbuildBusinessDataExpression = buildBusinessDataExpression("sameName");
        final BusinessDataExpressionExecutorStrategy strategy = spy(new BusinessDataExpressionExecutorStrategy(refBusinessDataRetriever, businessDataRetriever));
        final ContainerState active = ContainerState.ACTIVE;
        doReturn(new Object()).when(strategy).evaluate(any(SExpression.class), anyMap(), anyMap(), eq(active));

        strategy.evaluate(asList(firstbuildBusinessDataExpression, secondbuildBusinessDataExpression), null, null, active);

        verify(strategy, times(1)).evaluate(any(SExpression.class), anyMap(), anyMap(), eq(active));
    }

    @Test
    public void evaluation_result_should_be_pushed_in_context() {
        assertThat(businessDataExpressionExecutorStrategy.mustPutEvaluatedExpressionInContext()).isEqualTo(true);
    }

}
