/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
