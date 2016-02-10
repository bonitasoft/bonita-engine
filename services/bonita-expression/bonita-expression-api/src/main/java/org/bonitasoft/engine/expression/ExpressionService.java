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

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Zhao Na
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @since 6.0
 */
public interface ExpressionService {

    /**
     * Evaluate the specific expression
     * 
     * @param expression
     *            the expression will be evaluated
     * @return the evaluated expression result
     * @throws SExpressionTypeUnknownException
     * @throws SExpressionEvaluationException
     * @throws SExpressionDependencyMissingException
     * @throws SInvalidExpressionException
     */
    Object evaluate(SExpression expression, Map<Integer, Object> resolvedExpressions, ContainerState containerState) throws SExpressionTypeUnknownException,
            SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException;

    /**
     * Evaluate the specific expression with dependency values
     * 
     * @param expression
     *            the expression will be evaluated
     * @param dependencyValues
     *            the dependency values, it may contain values for expression
     * @return the evaluated expression result
     * @throws SExpressionTypeUnknownException
     * @throws SExpressionEvaluationException
     * @throws SExpressionDependencyMissingException
     * @throws SInvalidExpressionException
     */
    Object evaluate(SExpression expression, Map<String, Object> dependencyValues, Map<Integer, Object> resolvedExpressions, ContainerState containerState)
            throws SExpressionTypeUnknownException, SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException;

    /**
     * Evaluate type specified expressions with dependency values
     * 
     * @param expressionKind
     *            the expression kind to indicate which type of ExpressionExecutorStrategy will be used to evaluate the expressions
     * @param expressions
     *            a list of expressions to be evaluated
     * @param dependencyValues
     *            the dependency values for the expressions, it may contain value informations for expressions
     * @return a list of evaluated expression results
     * @throws SExpressionTypeUnknownException
     * @throws SExpressionEvaluationException
     * @throws SExpressionDependencyMissingException
     * @throws SInvalidExpressionException
     */
    List<Object> evaluate(ExpressionKind expressionKind, List<SExpression> expressions, Map<String, Object> dependencyValues,
            Map<Integer, Object> resolvedExpressions, ContainerState containerState) throws SExpressionTypeUnknownException, SExpressionEvaluationException,
            SExpressionDependencyMissingException, SInvalidExpressionException;

    /**
     * Should we throw exception if the real return type is incompatible with the declared one?
     * 
     * @return true if an exception must be thrown if check fails, false otherwise.
     */
    boolean mustCheckExpressionReturnType();

    /**
     * Should an expression result of a specified {@link ExpressionKind} must be put in the evaluation context?
     * 
     * @param expressionKind
     *            the {@link ExpressionKind}
     */
    boolean mustPutEvaluatedExpressionInContext(ExpressionKind expressionKind);
}
