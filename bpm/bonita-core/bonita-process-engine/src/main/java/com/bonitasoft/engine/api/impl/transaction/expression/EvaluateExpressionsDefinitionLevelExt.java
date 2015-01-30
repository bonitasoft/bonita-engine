/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
