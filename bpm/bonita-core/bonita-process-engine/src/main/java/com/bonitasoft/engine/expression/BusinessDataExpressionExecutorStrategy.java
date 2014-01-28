/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package com.bonitasoft.engine.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.lang.model.SourceVersion;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.expression.NonEmptyContentExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.persistence.SBonitaReadException;

import com.bonitasoft.engine.business.data.BusinessDataNotFoundException;
import com.bonitasoft.engine.business.data.BusinessDataRespository;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceNotFoundException;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;

/**
 * @author Colin Puy
 * @author Emmanuel Duchastenier
 */
public class BusinessDataExpressionExecutorStrategy extends NonEmptyContentExpressionExecutorStrategy {

    private final RefBusinessDataService refBusinessDataService;

    private final BusinessDataRespository businessDataRepository;

    private final FlowNodeInstanceService flowNodeInstanceService;

    public BusinessDataExpressionExecutorStrategy(final RefBusinessDataService refBusinessDataService, final BusinessDataRespository businessDataRepository,
            final FlowNodeInstanceService flowsNodeInstanceService) {
        this.refBusinessDataService = refBusinessDataService;
        this.businessDataRepository = businessDataRepository;
        flowNodeInstanceService = flowsNodeInstanceService;
    }

    @Override
    public void validate(final SExpression expression) throws SInvalidExpressionException {
        // $ can be part of variable name
        super.validate(expression);
        if (!SourceVersion.isIdentifier(expression.getContent())) {
            throw new SInvalidExpressionException(expression.getContent() + " is not a valid business data name in expression: " + expression);
        }
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_BUSINESS_DATA;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions)
            throws SExpressionDependencyMissingException, SExpressionEvaluationException {
        String bizDataName = expression.getContent();
        if (context.containsKey(bizDataName)) {
            return context.get(bizDataName);
        }
        try {
            long processInstanceId = getProcessInstanceId((Long) context.get(SExpressionContext.containerIdKey),
                    (String) context.get(SExpressionContext.containerTypeKey));
            SRefBusinessDataInstance refBusinessDataInstance = refBusinessDataService.getRefBusinessDataInstance(bizDataName, processInstanceId);
            Class<?> bizClass = Thread.currentThread().getContextClassLoader().loadClass(refBusinessDataInstance.getDataClassName());
            return businessDataRepository.find(bizClass, refBusinessDataInstance.getDataId());
        } catch (SRefBusinessDataInstanceNotFoundException e) {
            throw new SExpressionEvaluationException("");
        } catch (SBonitaReadException e) {
            throw new SExpressionEvaluationException("");
        } catch (BusinessDataNotFoundException e) {
            throw new SExpressionEvaluationException("");
        } catch (ClassNotFoundException e) {
            throw new SExpressionEvaluationException("");
        }
    }

    /**
     * protected for testing
     */
    protected long getProcessInstanceId(final long containerId, final String containerType) throws SExpressionEvaluationException {
        if (DataInstanceContainer.PROCESS_INSTANCE.name().equals(containerType)) {
            return containerId;
        } else if (DataInstanceContainer.ACTIVITY_INSTANCE.name().equals(containerType)) {
            SFlowNodeInstance flowNodeInstance;
            try {
                flowNodeInstance = flowNodeInstanceService.getFlowNodeInstance(containerId);
                return flowNodeInstance.getParentProcessInstanceId();
            } catch (Exception e) {
                throw new SExpressionEvaluationException("Process instance id not found in context");
            }
        }
        throw new SExpressionEvaluationException("Invalid container type");
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> dependencyValues, final Map<Integer, Object> resolvedExpressions)
            throws SExpressionDependencyMissingException, SExpressionEvaluationException {
        List<Object> objects = new ArrayList<Object>(expressions.size());
        for (SExpression sExpression : expressions) {

        }
        return objects;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return true;
    }

}
