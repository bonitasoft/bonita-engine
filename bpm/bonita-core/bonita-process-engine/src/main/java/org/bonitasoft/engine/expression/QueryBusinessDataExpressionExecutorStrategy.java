/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.impl.transaction.expression.bdm.ServerLazyLoader;
import org.bonitasoft.engine.api.impl.transaction.expression.bdm.ServerProxyfier;
import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.NonUniqueResultException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class QueryBusinessDataExpressionExecutorStrategy extends NonEmptyContentExpressionExecutorStrategy {

    private final BusinessDataRepository businessDataRepository;

    public QueryBusinessDataExpressionExecutorStrategy(final BusinessDataRepository businessDataRepository) {
        this.businessDataRepository = businessDataRepository;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException {
        final String queryName = expression.getContent();
        final String returnType = expression.getReturnType();
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        for (final SExpression dependency : expression.getDependencies()) {
            parameters.put(dependency.getName(), (Serializable) resolvedExpressions.get(dependency.getDiscriminant()));
        }
        try {
            if (isNumber(returnType)) {
                Class<? extends Serializable> numberClass;
                try {
                    numberClass = (Class<? extends Serializable>) Class.forName(returnType);
                } catch (ClassNotFoundException e) {
                    throw new SExpressionEvaluationException(e, expression.getName());
                }
                return businessDataRepository.findByNamedQuery(queryName, numberClass, parameters);
            } else if (List.class.getName().equals(returnType)) {
                List<Entity> entities =
                        businessDataRepository.findListByNamedQuery(queryName, Entity.class, parameters,
                                getStartIndexParameter(expression.getDependencies(), resolvedExpressions, expression.getName(), parameters),
                                getMaxResultParameter(expression.getDependencies(), resolvedExpressions, expression.getName(), parameters));
                
                List<Entity> e = new ArrayList<Entity>();
                for (Entity entity : entities) {
                    ServerProxyfier proxyfier = new ServerProxyfier(new ServerLazyLoader(businessDataRepository));
                    Entity proxify = proxyfier.proxify(entity);
                    e.add(proxify);
                }
                
                return e;
            } else {
                Entity findByNamedQuery = businessDataRepository.findByNamedQuery(queryName, Entity.class, parameters);
                ServerProxyfier proxyfier = new ServerProxyfier(new ServerLazyLoader(businessDataRepository));
                return proxyfier.proxify(findByNamedQuery);
            }
        } catch (final NonUniqueResultException nure) {
            throw new SExpressionEvaluationException(nure, expression.getName());
        }
    }

    private int getStartIndexParameter(final List<SExpression> dependencies, final Map<Integer, Object> resolvedExpressions, final String expressionName,
            final Map<String, Serializable> parameters) throws SExpressionEvaluationException {
        for (SExpression dependency : dependencies) {
            if ("startIndex".equals(dependency.getName())) {
                parameters.remove(dependency.getName());
                return (Integer) resolvedExpressions.get(dependency.getDiscriminant());
            }
        }
        throw new SExpressionEvaluationException(
                "Pagination parameter 'startIndex' is mandatory when calling 'find*()' methods returning a List of Business Data", expressionName);
    }

    private int getMaxResultParameter(final List<SExpression> dependencies, final Map<Integer, Object> resolvedExpressions, final String expressionName,
            final Map<String, Serializable> parameters) throws SExpressionEvaluationException {
        for (SExpression dependency : dependencies) {
            if ("maxResults".equals(dependency.getName())) {
                parameters.remove(dependency.getName());
                return (Integer) resolvedExpressions.get(dependency.getDiscriminant());
            }
        }
        throw new SExpressionEvaluationException(
                "Pagination parameter 'maxResults' is mandatory when calling 'find*()' methods returning a List of Business Data", expressionName);
    }

    private boolean isNumber(final String returnType) {
        return Long.class.getName().equals(returnType) || Integer.class.getName().equals(returnType) || Double.class.getName().equals(returnType)
                || Float.class.getName().equals(returnType);
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_QUERY_BUSINESS_DATA;
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException {
        final List<Object> list = new ArrayList<Object>(expressions.size());
        for (final SExpression expression : expressions) {
            list.add(evaluate(expression, context, resolvedExpressions, containerState));
        }
        return list;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return false;
    }

}
