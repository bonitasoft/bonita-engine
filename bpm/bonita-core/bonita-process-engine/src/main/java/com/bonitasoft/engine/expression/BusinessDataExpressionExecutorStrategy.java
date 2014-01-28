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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.lang.model.SourceVersion;

import org.bonitasoft.engine.expression.NonEmptyContentExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

import com.bonitasoft.engine.business.data.BusinessDataRespository;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;

/**
 * @author Colin Puy
 * @author Emmanuel Duchastenier
 */
public class BusinessDataExpressionExecutorStrategy extends NonEmptyContentExpressionExecutorStrategy {

    private final RefBusinessDataService refBusinessDataService;

    private final BusinessDataRespository businessDataRepository;

    public BusinessDataExpressionExecutorStrategy(final RefBusinessDataService refBusinessDataService, final BusinessDataRespository businessDataRepository) {
        this.refBusinessDataService = refBusinessDataService;
        this.businessDataRepository = businessDataRepository;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> dependencyValues, final Map<Integer, Object> resolvedExpressions)
            throws SExpressionDependencyMissingException, SExpressionEvaluationException {
        // return evaluate(Collections.singletonList(expression), dependencyValues, resolvedExpressions).get(0);
        String bizDataName = expression.getContent();

        return null;
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
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> dependencyValues, final Map<Integer, Object> resolvedExpressions)
            throws SExpressionDependencyMissingException, SExpressionEvaluationException {
        List<Object> objects = new ArrayList<Object>(expressions.size());
        for (SExpression sExpression : expressions) {

        }
        return objects;
    }

    private List<Object> buildExpressionResultSameOrderAsInputList(final List<SExpression> expressions, final Map<String, Serializable> results) {
        final ArrayList<Object> list = new ArrayList<Object>(expressions.size());
        for (final SExpression expression : expressions) {
            list.add(results.get(expression.getContent()));
        }
        return list;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return true;
    }

}
