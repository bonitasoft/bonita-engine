/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.expression;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.expression.ExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.data.BusinessDataRespository;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;

@RunWith(MockitoJUnitRunner.class)
public class BusinessDataExpressionExecutorStrategyTest {

    @Mock
    private BusinessDataRespository businessDataRepository;

    @Mock
    private RefBusinessDataService refBusinessDataService;

    @Mock
    private FlowNodeInstanceService flowNodeInstanceService;

    @InjectMocks
    private BusinessDataExpressionExecutorStrategy businessDataExpressionExecutorStrategy;

    private SFlowNodeInstance createAflowNodeInstanceInRepository() throws Exception {
        SFlowNodeInstance flowNode = mock(SFlowNodeInstance.class);
        when(flowNode.getParentProcessInstanceId()).thenReturn(1234L);
        when(flowNode.getId()).thenReturn(456L);
        when(flowNodeInstanceService.getFlowNodeInstance(flowNode.getId())).thenReturn(flowNode);
        return flowNode;
    }

    private SimpleBizData createAbizDataInRepository() throws Exception {
        return createAbizDataInRepository(98L);
    }

    private SimpleBizData createAbizDataInRepository(final long bizDataId) throws Exception {
        SimpleBizData bizData = new SimpleBizData(bizDataId);
        when(businessDataRepository.find(SimpleBizData.class, bizData.getId())).thenReturn(bizData);
        return bizData;
    }

    private SRefBusinessDataInstance createARefBizDataInRepository(final SimpleBizData bizData, final long processInstanceId) throws Exception {
        return createARefBizDataInRepository(bizData, "bizDataName", processInstanceId);
    }

    private SRefBusinessDataInstance createARefBizDataInRepository(final SimpleBizData bizData, final String bizDataName, final long processInstanceId)
            throws Exception {
        SRefBusinessDataInstance refBizData = mock(SRefBusinessDataInstance.class);
        when(refBizData.getDataClassName()).thenReturn(bizData.getClass().getName());
        when(refBizData.getName()).thenReturn(bizDataName);
        when(refBizData.getProcessInstanceId()).thenReturn(processInstanceId);
        when(refBizData.getDataId()).thenReturn(bizData.getId());
        when(refBusinessDataService.getRefBusinessDataInstance(refBizData.getName(), processInstanceId)).thenReturn(refBizData);
        return refBizData;
    }

    private HashMap<String, Object> buildBusinessDataExpressionContext(final long containerId, final DataInstanceContainer containerType) {
        HashMap<String, Object> context = new HashMap<String, Object>();
        context.put(SExpressionContext.containerIdKey, containerId);
        context.put(SExpressionContext.containerTypeKey, containerType.name());
        return context;
    }

    private SExpressionImpl buildBusinessDataExpression(final String content) {
        SExpressionImpl expression = new SExpressionImpl();
        expression.setContent(content);
        expression.setReturnType("com.bonitasoft.engine.expression.BusinessDataExpressionExecutorStrategyTest.LeaveRequest");
        expression.setExpressionType(ExpressionType.TYPE_BUSINESS_DATA.name());
        return expression;
    }

    @Test
    public void should_be_a_business_data_expression_kind_strategy() throws Exception {
        assertThat(businessDataExpressionExecutorStrategy.getExpressionKind()).isEqualTo(ExpressionExecutorStrategy.KIND_BUSINESS_DATA);
    }

    @Test
    public void evaluate_on_a_process_instance_should_return_biz_data_instance_corresponding_to_data_name_and_processInstance_id() throws Exception {
        SimpleBizData expectedBizData = createAbizDataInRepository();
        long proccessInstanceId = 1L;
        SRefBusinessDataInstance refBizData = createARefBizDataInRepository(expectedBizData, proccessInstanceId);
        HashMap<String, Object> context = buildBusinessDataExpressionContext(proccessInstanceId, DataInstanceContainer.PROCESS_INSTANCE);
        SExpressionImpl buildBusinessDataExpression = buildBusinessDataExpression(refBizData.getName());

        Object fetchedBizData = businessDataExpressionExecutorStrategy.evaluate(buildBusinessDataExpression, context, null);

        assertThat(fetchedBizData).isEqualTo(expectedBizData);
    }

    @Test
    public void evaluate_on_a_task_instance_should_return_biz_data_instance_corresponding_to_data_name_and_processInstance_id() throws Exception {
        SimpleBizData expectedBizData = createAbizDataInRepository();
        SFlowNodeInstance flowNode = createAflowNodeInstanceInRepository();
        SRefBusinessDataInstance refBizData = createARefBizDataInRepository(expectedBizData, flowNode.getParentProcessInstanceId());
        HashMap<String, Object> context = buildBusinessDataExpressionContext(flowNode.getId(), DataInstanceContainer.ACTIVITY_INSTANCE);
        SExpressionImpl buildBusinessDataExpression = buildBusinessDataExpression(refBizData.getName());

        Object fetchedBizData = businessDataExpressionExecutorStrategy.evaluate(buildBusinessDataExpression, context, null);

        assertThat(fetchedBizData).isEqualTo(expectedBizData);
    }

    @Test
    public void evaluate_should_first_check_if_expression_already_evaluated_and_available_in_context() throws Exception {
        HashMap<String, Object> context = new HashMap<String, Object>();
        SimpleBizData expectedBizData = new SimpleBizData(12L);
        String bizDataName = "businessDataName";
        context.put(bizDataName, expectedBizData);
        SExpressionImpl buildBusinessDataExpression = buildBusinessDataExpression(bizDataName);

        Object fetchedBizData = businessDataExpressionExecutorStrategy.evaluate(buildBusinessDataExpression, context, null);

        assertThat(fetchedBizData).isEqualTo(expectedBizData);
        verifyZeroInteractions(businessDataRepository);
        verifyZeroInteractions(refBusinessDataService);
        verifyZeroInteractions(flowNodeInstanceService);
    }

    @Test
    public void evaluate_should_resolve_multiple_expressions() throws Exception {
        SimpleBizData firstBizData = createAbizDataInRepository(1L);
        SimpleBizData secondBizData = createAbizDataInRepository(2L);
        long aProcessInstanceId = 6L;
        SRefBusinessDataInstance firstRefBizData = createARefBizDataInRepository(firstBizData, "dataOne", aProcessInstanceId);
        SRefBusinessDataInstance secondRefBizData = createARefBizDataInRepository(secondBizData, "dataTwo", aProcessInstanceId);
        HashMap<String, Object> context = buildBusinessDataExpressionContext(aProcessInstanceId, DataInstanceContainer.PROCESS_INSTANCE);
        SExpression firstbuildBusinessDataExpression = buildBusinessDataExpression(firstRefBizData.getName());
        SExpression secondbuildBusinessDataExpression = buildBusinessDataExpression(secondRefBizData.getName());

        List<Object> fetchedBizDatas = businessDataExpressionExecutorStrategy.evaluate(
                Arrays.asList(firstbuildBusinessDataExpression, secondbuildBusinessDataExpression), context, null);

        assertThat(fetchedBizDatas).contains(firstBizData, secondBizData);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void evaluate_should_not_resolve_expression_with_same_content_as_already_resolved_one() throws Exception {
        SExpression firstbuildBusinessDataExpression = buildBusinessDataExpression("sameName");
        SExpression secondbuildBusinessDataExpression = buildBusinessDataExpression("sameName");
        BusinessDataExpressionExecutorStrategy strategy = spy(new BusinessDataExpressionExecutorStrategy(refBusinessDataService, businessDataRepository,
                flowNodeInstanceService));
        doReturn(new Object()).when(strategy).evaluate(any(SExpression.class), anyMap(), anyMap());

        strategy.evaluate(asList(firstbuildBusinessDataExpression, secondbuildBusinessDataExpression), null, null);

        verify(strategy, times(1)).evaluate(any(SExpression.class), anyMap(), anyMap());
    }

    @Test
    public void evaluation_result_should_be_pushed_in_context() throws Exception {
        assertThat(businessDataExpressionExecutorStrategy.mustPutEvaluatedExpressionInContext()).isEqualTo(true);
    }

    @Test
    public void getProcessInstanceId_should_return_container_id_if_containerType_is_process() throws Exception {
        long expectedContainerId = 1L;

        long processInstanceId = businessDataExpressionExecutorStrategy
                .getProcessInstanceId(expectedContainerId, DataInstanceContainer.PROCESS_INSTANCE.name());

        assertThat(processInstanceId).isEqualTo(expectedContainerId);
    }

    @Test
    public void getProcessInstanceId_should_return_taskInstance_processId_if_containerType_is_activity() throws Exception {
        SFlowNodeInstance flowNode = createAflowNodeInstanceInRepository();

        long processInstanceId = businessDataExpressionExecutorStrategy.getProcessInstanceId(flowNode.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name());

        assertThat(processInstanceId).isEqualTo(flowNode.getParentProcessInstanceId());
    }

    @Test(expected = SExpressionEvaluationException.class)
    public void getProcessInstanceId_should_throw_an_exception_for_a_not_supported_container_type() throws Exception {
        businessDataExpressionExecutorStrategy.getProcessInstanceId(1L, DataInstanceContainer.MESSAGE_INSTANCE.name());
    }
}
