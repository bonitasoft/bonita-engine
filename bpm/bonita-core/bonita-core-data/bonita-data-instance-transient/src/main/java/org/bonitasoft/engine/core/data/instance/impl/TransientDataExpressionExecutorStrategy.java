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
package org.bonitasoft.engine.core.data.instance.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.data.instance.TransientDataService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceNotFoundException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.NonEmptyContentExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Baptiste Mesta
 */
public class TransientDataExpressionExecutorStrategy extends NonEmptyContentExpressionExecutorStrategy {

    private final TransientDataService transientDataService;

    public TransientDataExpressionExecutorStrategy(final TransientDataService transientDataService) {
        this.transientDataService = transientDataService;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> dependencyValues, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        return evaluate(Arrays.asList(expression), dependencyValues, resolvedExpressions, containerState).get(0);
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_TRANSIENT_VARIABLE;
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> dependencyValues,
            final Map<Integer, Object> resolvedExpressions, final ContainerState containerState) throws SExpressionEvaluationException,
            SExpressionDependencyMissingException {
        long containerId;
        String containerType;
        final int maxExpressionSize = expressions.size();
        final ArrayList<String> dataNames = new ArrayList<String>(maxExpressionSize);
        final HashMap<String, Serializable> results = new HashMap<String, Serializable>(maxExpressionSize);
        for (final SExpression sExpression : expressions) {
            final String dataName = sExpression.getContent();
            if (dependencyValues.containsKey(dataName)) {
                final Serializable value = (Serializable) dependencyValues.get(dataName);
                results.put(dataName, value);
            } else {
                dataNames.add(dataName);
            }
        }

        if (dataNames.isEmpty()) {
            return buildExpressionResultSameOrderAsInputList(expressions, results);
        }
        if (dependencyValues != null && dependencyValues.containsKey(CONTAINER_ID_KEY) && dependencyValues.containsKey(CONTAINER_TYPE_KEY)) {
            String currentData = null;
            try {
                containerId = (Long) dependencyValues.get(CONTAINER_ID_KEY);
                containerType = (String) dependencyValues.get(CONTAINER_TYPE_KEY);
                // TODO archived transient data?
                // final Long time;
                // if ((time = (Long) dependencyValues.get(TIME)) != null) {
                // final List<SADataInstance> dataInstances = transientDataService.getSADataInstances(containerId, containerType, dataNames, time);
                // for (final SADataInstance dataInstance : dataInstances) {
                // dataNames.remove(dataInstance.getName());
                // results.put(dataInstance.getName(), dataInstance.getValue());
                // }
                // }
                for (final String name : dataNames) {
                    currentData = name;
                    SDataInstance dataInstance;
                    try {
                        dataInstance = transientDataService.getDataInstance(name, containerId, containerType);
                    } catch (final SDataInstanceNotFoundException e) {
                        dataInstance = handleDataNotFound(name, containerId, containerType, e);
                    }
                    results.put(dataInstance.getName(), dataInstance.getValue());
                }
                return buildExpressionResultSameOrderAsInputList(expressions, results);
            } catch (final SBonitaReadException e) {
                throw new SExpressionEvaluationException("Can't read transient data", e, currentData);
            } catch (final SDataInstanceException e) {
                throw new SExpressionEvaluationException("Can't read transient data", e, currentData);
            }
        }
        throw new SExpressionDependencyMissingException("The context to evaluate the data '" + dataNames + "' was not set");
    }

    protected SDataInstance handleDataNotFound(final String name, final long containerId, final String containerType, final SDataInstanceNotFoundException e)
            throws SDataInstanceNotFoundException, SBonitaReadException, SDataInstanceException {
        throw e;
    }

    private List<Object> buildExpressionResultSameOrderAsInputList(final List<SExpression> expressions, final Map<String, Serializable> results) {
        final ArrayList<Object> list = new ArrayList<Object>(expressions.size());
        for (final SExpression expression : expressions) {
            list.add(results.get(expression.getContent()));
        }
        return list;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return true;
    }

}
