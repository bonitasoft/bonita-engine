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
package org.bonitasoft.engine.api.impl.transaction.expression;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Zhao Na
 * @author Matthieu Chaffotte
 */
public class EvaluateExpressionsInstanceLevelAndArchived extends AbstractEvaluateExpressionsInstance implements
TransactionContentWithResult<Map<String, Serializable>> {

    private final Map<Expression, Map<String, Serializable>> expressions;

    private final long containerId;

    private final long processDefinitionId;

    private final String containerType;

    private final long time;

    private final ExpressionResolverService expressionResolver;

    private final Map<String, Serializable> results = new HashMap<String, Serializable>(0);

    public EvaluateExpressionsInstanceLevelAndArchived(final Map<Expression, Map<String, Serializable>> expressions, final long containerId,
            final String containerType, final long processDefinitionId, final long time, final ExpressionResolverService expressionService,
            final BusinessDataRepository bdrService) {
        super(bdrService);
        this.expressions = expressions;
        this.containerId = containerId;
        expressionResolver = expressionService;
        this.processDefinitionId = processDefinitionId;
        this.containerType = containerType;
        this.time = time;
    }

    @Override
    public void execute() throws SBonitaException {
        // FIXME: call the appropriate method(s) from the right service(s):
        if (expressions != null && !expressions.isEmpty()) {
            // how to deal with containerType and containerId
            final SExpressionContext context = new SExpressionContext();
            context.setContainerId(containerId);
            context.setContainerType(containerType);
            context.setProcessDefinitionId(processDefinitionId);
            context.setTime(time);

            final Set<Expression> exps = expressions.keySet();
            for (final Expression exp : exps) {
                final Map<String, Serializable> partialContext = getPartialContext(expressions, exp);
                context.setSerializableInputValues(partialContext);
                final SExpression sexp = ModelConvertor.constructSExpression(exp);
                final Serializable res = (Serializable) expressionResolver.evaluate(sexp, context);
                results.put(buildName(exp), res);// MAYBE instead of exp.getNAME
            }
        }
    }

    @Override
    public Map<String, Serializable> getResult() {
        return results;
    }
}
