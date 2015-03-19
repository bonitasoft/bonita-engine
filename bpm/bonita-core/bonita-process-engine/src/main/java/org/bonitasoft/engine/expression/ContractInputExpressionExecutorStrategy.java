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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.contract.data.ContractDataService;
import org.bonitasoft.engine.core.contract.data.SContractDataNotFoundException;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Matthieu Chaffotte
 */
public class ContractInputExpressionExecutorStrategy implements ExpressionExecutorStrategy {

    private final ContractDataService contractDataService;

    public ContractInputExpressionExecutorStrategy(final ContractDataService contractDataService) {
        super();
        this.contractDataService = contractDataService;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState)
            throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        final Long containerId = (Long) context.get(CONTAINER_ID_KEY);
        final String containerType = (String) context.get(CONTAINER_TYPE_KEY);
        try {
            if ("PROCESS_INSTANCE".equals(containerType)) {
                return contractDataService.getProcessDataValue(containerId, expression.getContent());
            }
            else {
                return contractDataService.getUserTaskDataValue(containerId, expression.getContent());
            }
        } catch (final SContractDataNotFoundException scdnfe) {
            throw new SExpressionEvaluationException(scdnfe, expression.getName());
        } catch (final SBonitaReadException sre) {
            throw new SExpressionEvaluationException(sre, expression.getName());
        }
    }

    @Override
    public void validate(final SExpression expression) throws SInvalidExpressionException {
        // No validation because the type validation can only be done after evaluation
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_CONTRACT_INPUT;
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        final List<Object> results = new ArrayList<Object>();
        for (final SExpression expression : expressions) {
            results.add(evaluate(expression, context, resolvedExpressions, containerState));
        }
        return results;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return true;
    }

}
