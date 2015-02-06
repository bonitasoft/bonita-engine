/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.expression;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.NonEmptyContentExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryException;

/**
 * @author Emmanuel Duchastenier
 */
public class BusinessObjectDAOExpressionStrategy extends NonEmptyContentExpressionExecutorStrategy {

    private final BusinessDataRepository businessDataRepository;

    public BusinessObjectDAOExpressionStrategy(final BusinessDataRepository businessDataRepository) {
        this.businessDataRepository = businessDataRepository;
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_BUSINESS_OBJECT_DAO;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException {
        final String daoName = expression.getContent();
        if (context.containsKey(daoName)) {
            return context.get(daoName);
        }
        final String daoServerImplementation = getDAOServerImplementationFromInterface(expression.getReturnType());
        try {
            return instantiateDAO(daoServerImplementation);
        } catch (Exception e) {
            throw new SExpressionEvaluationException("Unable to instantiate Business Object Server DAO implementation" + daoServerImplementation, e,
                    expression.getName());
        }
    }

    protected Object instantiateDAO(final String daoClassName) throws Exception {
        Class daoImplClass = Thread.currentThread().getContextClassLoader().loadClass(daoClassName);
        if (daoImplClass != null) {
            Constructor constructor = daoImplClass.getConstructor(BusinessDataRepository.class);
            return constructor.newInstance(businessDataRepository);
        }
        throw new SBusinessDataRepositoryException("Cannot load class " + daoClassName);
    }

    protected String getDAOServerImplementationFromInterface(final String daoClassName) {
        int pointIdx = daoClassName.lastIndexOf('.');
        return daoClassName.substring(0, pointIdx + 1) + "server." + daoClassName.substring(pointIdx + 1) + "Impl";
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException {
        List<Object> daos = new ArrayList<Object>(expressions.size());
        for (SExpression expression : expressions) {
            daos.add(evaluate(expression, context, resolvedExpressions, containerState));
        }
        return daos;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return true;
    }

}
