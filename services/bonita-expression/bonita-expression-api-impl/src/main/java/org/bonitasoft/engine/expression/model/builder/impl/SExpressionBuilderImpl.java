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
package org.bonitasoft.engine.expression.model.builder.impl;

import java.util.List;

import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilder;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
public class SExpressionBuilderImpl implements SExpressionBuilder {

    private final SExpressionImpl expression;

    public SExpressionBuilderImpl(final SExpressionImpl expression) {
        super();
        this.expression = expression;
    }

    @Override
    public SExpression done() throws SInvalidExpressionException {
        if (expression.getReturnType() == null) {
            throw new SInvalidExpressionException("Expression return type must be set.", expression.getName());
        }
        return expression;
    }

    @Override
    public SExpressionBuilder setName(final String name) {
        expression.setName(name);
        return this;
    }

    @Override
    public SExpressionBuilder setContent(final String content) {
        expression.setContent(content);
        return this;
    }

    @Override
    public SExpressionBuilder setExpressionType(final String expressionType) {
        expression.setExpressionType(expressionType);
        return this;
    }

    @Override
    public SExpressionBuilder setReturnType(final String returnType) {
        expression.setReturnType(returnType);
        return this;
    }

    @Override
    public SExpressionBuilder setInterpreter(final String interpreter) {
        expression.setInterpreter(interpreter);
        return this;
    }

    @Override
    public SExpressionBuilder setDependencies(final List<SExpression> dependencies) {
        expression.setDependencies(dependencies);
        return this;
    }

}
