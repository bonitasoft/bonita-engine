/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Zhao Na
 */
public class EvaluateExpressionsDefinitionLevel extends AbstractEvaluateExpressionsInstance implements TransactionContentWithResult<Map<String, Serializable>> {

    private final Map<Expression, Map<String, Serializable>> expressionsAndTheirPartialContext;

    private final long processDefinitionId;

    private final ExpressionResolverService expressionResolver;

    private final ProcessDefinitionService processDefinitionService;

    private final Map<String, Serializable> results = new HashMap<String, Serializable>(0);

    public EvaluateExpressionsDefinitionLevel(final Map<Expression, Map<String, Serializable>> expressions, final long processDefinitionId,
            final ExpressionResolverService expressionResolverService,
            final ProcessDefinitionService processDefinitionService) {
        expressionsAndTheirPartialContext = expressions;
        this.processDefinitionId = processDefinitionId;
        expressionResolver = expressionResolverService;
        this.processDefinitionService = processDefinitionService;
    }

    @Override
    public void execute() throws SBonitaException {
        // FIXME: call the appropriate method(s) from the right service(s):
        if (expressionsAndTheirPartialContext != null && !expressionsAndTheirPartialContext.isEmpty()) {
            // how to deal with containerType and containerId
            final SExpressionContext context = new SExpressionContext();
            if (processDefinitionId != 0) {
                final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
                final Set<Expression> exps = expressionsAndTheirPartialContext.keySet();
                for (Expression exp : exps) {
                    Map<String, Serializable> inputValues = expressionsAndTheirPartialContext.get(exp);
                    if (inputValues == null) {
                        inputValues = new HashMap<String, Serializable>();
                    }
                    inputValues.put(SExpressionContext.processDefinitionKey, processDefinition);
                    context.setProcessDefinitionId(processDefinitionId);
                    context.setSerializableInputValues(inputValues);
                    final SExpression sexp = ServerModelConvertor.convertExpression(exp);
                    // maybe the context's not enough to delivery those parameters like ap<String, Serializable>.
                    final Serializable res = (Serializable) expressionResolver.evaluate(sexp, context);// evaluate(sexp, context);
                    results.put(buildName(exp), res);
                }
            }
        }

    }

    @Override
    public Map<String, Serializable> getResult() {
        return results;
    }
}
