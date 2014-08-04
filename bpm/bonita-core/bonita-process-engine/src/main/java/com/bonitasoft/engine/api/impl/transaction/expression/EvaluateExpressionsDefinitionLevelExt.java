/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package com.bonitasoft.engine.api.impl.transaction.expression;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.api.impl.transaction.expression.EvaluateExpressionsDefinitionLevel;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.expression.Expression;

import com.bonitasoft.engine.business.data.BusinessDataRepository;

/**
 * @author Romain Bioteau
 *
 */
public class EvaluateExpressionsDefinitionLevelExt extends EvaluateExpressionsDefinitionLevel {

    private final EntityMerger entityMerger;

    public EvaluateExpressionsDefinitionLevelExt(final Map<Expression, Map<String, Serializable>> expressions, final long processDefinitionId,
            final ExpressionResolverService expressionResolverService, final ProcessDefinitionService processDefinitionService,
            final BusinessDataRepository bdrService) {
        super(expressions, processDefinitionId, expressionResolverService, processDefinitionService);
        entityMerger = new EntityMerger(bdrService);
    }

    @Override
    protected Map<String, Serializable> getPartialContext(final Map<Expression, Map<String, Serializable>> expressions, final Expression exp) {
        final Map<String, Serializable> partialContext = super.getPartialContext(expressions, exp);
        if (partialContext == null || partialContext.isEmpty()) {
            return partialContext;
        }

        final Map<String, Serializable> result = new HashMap<String, Serializable>();
        for (final Entry<String, Serializable> entry : partialContext.entrySet()) {
            result.put(entry.getKey(), entityMerger.merge(entry.getValue()));
        }

        return result;
    }

}
