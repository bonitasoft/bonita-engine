/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.expression.impl;

import java.util.Map;

import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

public class GroovyScriptConditionExpressionExecutorStrategy extends GroovyScriptExpressionExecutorCacheStrategy {

    public GroovyScriptConditionExpressionExecutorStrategy(CacheService cacheService,
            ClassLoaderService classLoaderService) {
        super(cacheService, classLoaderService);
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_READ_ONLY_CONDITION_SCRIPT_GROOVY;
    }

    @Override
    public Object evaluate(SExpression expression, Map<String, Object> context,
            Map<Integer, Object> resolvedExpressions, ContainerState containerState)
            throws SExpressionEvaluationException {
        Object result = super.evaluate(expression, context, resolvedExpressions, containerState);
        return result instanceof Boolean ? result : result != null;
    }

}
