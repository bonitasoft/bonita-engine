/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package com.bonitasoft.engine.expression;

import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilder;

import com.bonitasoft.engine.api.APIAccessor;

/**
 * @author Matthieu Chaffotte
 */
public final class EngineConstantExpressionBuilder {

    public static SExpression getAPIAccessorExpression(final SExpressionBuilder expressionbuilder) throws SInvalidExpressionException {
        final SExpressionBuilder builder = expressionbuilder.createNewInstance();
        builder.setContent("apiAccessor").setExpressionType(ExpressionType.TYPE_ENGINE_CONSTANT.name()).setReturnType(APIAccessor.class.getName());
        return builder.done();
    }

    public static SExpression getEngineExecutionContext(final SExpressionBuilder expressionbuilder) throws SInvalidExpressionException {
        final SExpressionBuilder builder = expressionbuilder.createNewInstance();
        builder.setContent("engineExecutionContext").setExpressionType(ExpressionType.TYPE_ENGINE_CONSTANT.name())
                .setReturnType(EngineExecutionContext.class.getName()).done();
        return builder.done();
    }

}
