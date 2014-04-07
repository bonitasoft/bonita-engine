/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.expression.NonEmptyContentExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.NonUniqueResultException;

/**
 * @author Matthieu Chaffotte
 */
public class QueryBusinessDataExpressionExecutorStrategy extends NonEmptyContentExpressionExecutorStrategy {

    private final BusinessDataRepository businessDataRepository;

    public QueryBusinessDataExpressionExecutorStrategy(final BusinessDataRepository businessDataRepository) {
        this.businessDataRepository = businessDataRepository;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions)
            throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        final String queryName = expression.getContent();
        final String returnType = expression.getReturnType();
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        for (final SExpression dependency : expression.getDependencies()) {
            parameters.put(dependency.getName(), (Serializable) resolvedExpressions.get(dependency.getDiscriminant()));
        }
        try {
            if (Long.class.getName().equals(returnType)) {
                return businessDataRepository.findByNamedQuery(queryName, Long.class, parameters);
            } else if (List.class.getName().equals(returnType)) {
                return businessDataRepository.findListByNamedQuery(queryName, Entity.class, parameters);
            } else {
                return businessDataRepository.findByNamedQuery(queryName, Entity.class, parameters);
            }
        } catch (final NonUniqueResultException nure) {
            throw new SExpressionEvaluationException(nure, expression.getName());
        }
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_QUERY_BUSINESS_DATA;
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions)
            throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        final List<Object> list = new ArrayList<Object>(expressions.size());
        for (final SExpression expression : expressions) {
            list.add(evaluate(expression, context, resolvedExpressions));
        }
        return list;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return false;
    }

}
