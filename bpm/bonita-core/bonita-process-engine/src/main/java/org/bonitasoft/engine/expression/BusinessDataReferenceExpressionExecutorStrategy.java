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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Colin Puy
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class BusinessDataReferenceExpressionExecutorStrategy extends CommonBusinessDataExpressionExecutorStrategy {


    public BusinessDataReferenceExpressionExecutorStrategy(final RefBusinessDataService refBusinessDataService,
                                                           final FlowNodeInstanceService flowNodeInstanceService) {
        super(refBusinessDataService, flowNodeInstanceService);
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_BUSINESS_DATA_REFERENCE;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
                           final ContainerState containerState) throws SExpressionEvaluationException {
        final String businessDataName = expression.getContent();
        final Long containerId = (Long) context.get(SExpressionContext.CONTAINER_ID_KEY);
        final String containerType = (String) context.get(SExpressionContext.CONTAINER_TYPE_KEY);
        try {
            final SRefBusinessDataInstance refBusinessDataInstance = getRefBusinessDataInstance(businessDataName, containerId, containerType);
            return ModelConvertor.toBusinessDataReference(refBusinessDataInstance);
        } catch (final SBonitaReadException e) {
            throw new SExpressionEvaluationException(e, "Unable to retrieve business data instance with name " + businessDataName);
        } catch (final SBonitaException e) {
            setProcessInstanceId(containerId, containerType, e);
            throw new SExpressionEvaluationException(e, expression.getName());
        }
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
                                 final ContainerState containerState) throws SExpressionEvaluationException {
        final List<Object> bizData = new ArrayList<Object>(expressions.size());
        for (final SExpression expression : expressions) {
            bizData.add(evaluate(expression, context, resolvedExpressions, containerState));
        }
        return bizData;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return false;
    }

}
