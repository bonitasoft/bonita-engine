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
package org.bonitasoft.engine.core.expression.control.api;

import java.util.List;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Zhao Na
 * @author Baptiste Mesta
 * @since 6.0
 */
public interface ExpressionResolverService {

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
    Object evaluate(final SExpression expression) throws SExpressionTypeUnknownException,
            SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException;

    /**
     * Evaluate the specific expression with the given expressionContext.
     * 
     * @param expression
     *            the expression will be evaluated
     * @param contextDependency
     *            the expressionContext, it can contain some value informations or evaluated enviorenment for expressions
     * @return the evaluated expression result
     * @throws SExpressionTypeUnknownException
     * @throws SExpressionEvaluationException
     * @throws SExpressionDependencyMissingException
     * @throws SInvalidExpressionException
     */
    Object evaluate(final SExpression expression, final SExpressionContext contextDependency)
            throws SExpressionTypeUnknownException, SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException;

    /**
     * Evaluate the specific expressions with the given expressionContext.
     * 
     * @param expressions
     *            a list of expressions will be evaluated
     * @param contextDependency
     *            the expressionContext, it can contain some value information or evaluated environment for expressions
     * @return the evaluated expression result
     * @throws SExpressionTypeUnknownException
     * @throws SExpressionEvaluationException
     * @throws SExpressionDependencyMissingException
     * @throws SInvalidExpressionException
     */
    List<Object> evaluate(final List<SExpression> expressions, final SExpressionContext contextDependency) throws SExpressionTypeUnknownException,
            SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException;

}
