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
package org.bonitasoft.engine.expression;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

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
