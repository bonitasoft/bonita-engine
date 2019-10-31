/**
 * Copyright (C) 2019 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.expression.impl;

import java.util.Map;

import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

public class GroovyScriptConditionExpressionExecutorStrategy extends GroovyScriptExpressionExecutorCacheStrategy {

    public GroovyScriptConditionExpressionExecutorStrategy(CacheService cacheService,
            ClassLoaderService classLoaderService, TechnicalLoggerService logger) {
        super(cacheService, classLoaderService, logger);
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
