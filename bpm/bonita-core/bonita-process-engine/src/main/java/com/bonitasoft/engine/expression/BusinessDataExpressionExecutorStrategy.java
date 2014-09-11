/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.NonEmptyContentExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.persistence.SBonitaReadException;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.model.SMultiRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.SSimpleRefBusinessDataInstance;

/**
 * @author Colin Puy
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class BusinessDataExpressionExecutorStrategy extends NonEmptyContentExpressionExecutorStrategy {

    private final RefBusinessDataService refBusinessDataService;

    private final BusinessDataRepository businessDataRepository;

    private final FlowNodeInstanceService flowNodeInstanceService;

    public BusinessDataExpressionExecutorStrategy(final RefBusinessDataService refBusinessDataService, final BusinessDataRepository businessDataRepository,
            final FlowNodeInstanceService flowNodeInstanceService) {
        this.refBusinessDataService = refBusinessDataService;
        this.businessDataRepository = businessDataRepository;
        this.flowNodeInstanceService = flowNodeInstanceService;
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_BUSINESS_DATA;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionDependencyMissingException, SExpressionEvaluationException {
        final String businessDataName = expression.getContent();
        if (context.containsKey(businessDataName)) {
            return context.get(businessDataName);
        }
        long processInstanceId = -1;
        try {
            final Long containerId = (Long) context.get(SExpressionContext.CONTAINER_ID_KEY);
            final String containerType = (String) context.get(SExpressionContext.CONTAINER_TYPE_KEY);
            if ("PROCESS_INSTANCE".equals(containerType)) {
                processInstanceId = containerId;
            }
            final SRefBusinessDataInstance refBusinessDataInstance = getRefBusinessDataInstance(businessDataName, containerId, containerType);

            final Class<Entity> bizClass = (Class<Entity>) Thread.currentThread().getContextClassLoader().loadClass(refBusinessDataInstance.getDataClassName());
            if (refBusinessDataInstance instanceof SSimpleRefBusinessDataInstance) {
                final SSimpleRefBusinessDataInstance reference = (SSimpleRefBusinessDataInstance) refBusinessDataInstance;
                return businessDataRepository.findById(bizClass, reference.getDataId());
            }
            final SMultiRefBusinessDataInstance reference = (SMultiRefBusinessDataInstance) refBusinessDataInstance;
            return businessDataRepository.findByIds(bizClass, reference.getDataIds());
        } catch (final SBonitaReadException e) {
            throw new SExpressionEvaluationException("Unable to retrieve business data instance with name " + businessDataName, expression.getName());
        } catch (final SBonitaException e) {
            if (processInstanceId != -1) {
                e.setProcessInstanceIdOnContext(processInstanceId);
            }
            throw new SExpressionEvaluationException(e, expression.getName());
        } catch (final ClassNotFoundException e) {
            throw new SExpressionEvaluationException(e, expression.getName());
        }
    }

    protected SRefBusinessDataInstance getRefBusinessDataInstance(final String businessDataName, final long containerId, final String containerType)
            throws SBonitaException {
        if ("PROCESS_INSTANCE".equals(containerType)) {
            final long processInstanceId = flowNodeInstanceService.getProcessInstanceId(containerId, containerType);
            return refBusinessDataService.getRefBusinessDataInstance(businessDataName, processInstanceId);
        }
        try {
            return refBusinessDataService.getFlowNodeRefBusinessDataInstance(businessDataName, containerId);
        } catch (final SBonitaException sbe) {
            final long processInstanceId = flowNodeInstanceService.getProcessInstanceId(containerId, containerType);
            return refBusinessDataService.getRefBusinessDataInstance(businessDataName, processInstanceId);
        }
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException {
        final List<Object> bizDatas = new ArrayList<Object>(expressions.size());
        final List<String> alreadyEvaluatedExpressionContent = new ArrayList<String>();
        for (final SExpression expression : expressions) {
            if (!alreadyEvaluatedExpressionContent.contains(expression.getContent())) {
                bizDatas.add(evaluate(expression, context, resolvedExpressions, containerState));
                alreadyEvaluatedExpressionContent.add(expression.getContent());
            }
        }
        return bizDatas;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return true;
    }

}
