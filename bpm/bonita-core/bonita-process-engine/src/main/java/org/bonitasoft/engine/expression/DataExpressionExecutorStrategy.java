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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.SourceVersion;

import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.api.ParentContainerResolver;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
public class DataExpressionExecutorStrategy extends NonEmptyContentExpressionExecutorStrategy {

    public static final String TIME = "time";

    private final DataInstanceService dataService;

    private ParentContainerResolver parentContainerResolver;

    public DataExpressionExecutorStrategy(final DataInstanceService dataService, final ParentContainerResolver parentContainerResolver) {
        this.dataService = dataService;
        this.parentContainerResolver = parentContainerResolver;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionDependencyMissingException, SExpressionEvaluationException {
        return evaluate(Collections.singletonList(expression), context, resolvedExpressions, containerState).get(0);
    }

    @Override
    public void validate(final SExpression expression) throws SInvalidExpressionException {
        // $ can be part of variable name
        super.validate(expression);
        if (!SourceVersion.isIdentifier(expression.getContent())) {
            throw new SInvalidExpressionException(expression.getContent() + " is not a valid data name in expression : " + expression, expression.getName());
        }
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_VARIABLE;
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionDependencyMissingException, SExpressionEvaluationException {
        final int maxExpressionSize = expressions.size();
        final List<String> dataNames = new ArrayList<String>(maxExpressionSize);
        final Map<String, Serializable> results = new HashMap<String, Serializable>(maxExpressionSize);
        for (final SExpression sExpression : expressions) {
            final String dataName = sExpression.getContent();
            if (context.containsKey(dataName)) {
                final Serializable value = (Serializable) context.get(dataName);
                results.put(dataName, value);
            } else {
                dataNames.add(dataName);
            }
        }

        if (dataNames.isEmpty()) {
            return buildExpressionResultSameOrderAsInputList(expressions, results);
        }
        if (context == null || !context.containsKey(CONTAINER_ID_KEY) || !context.containsKey(CONTAINER_TYPE_KEY)) {
            throw new SExpressionDependencyMissingException("The context to evaluate the data '" + dataNames + "' was not set");
        }
        try {
            final long containerId = (Long) context.get(CONTAINER_ID_KEY);
            final String containerType = (String) context.get(CONTAINER_TYPE_KEY);
            final Long time = (Long) context.get(TIME);
            if (time != null) {
                final List<SADataInstance> dataInstances = dataService.getSADataInstances(containerId, containerType, parentContainerResolver, dataNames, time);
                for (final SADataInstance dataInstance : dataInstances) {
                    dataNames.remove(dataInstance.getName());
                    results.put(dataInstance.getName(), dataInstance.getValue());
                }
            }
            final List<SDataInstance> dataInstances = dataService.getDataInstances(dataNames, containerId, containerType, parentContainerResolver);
            for (final SDataInstance dataInstance : dataInstances) {
                dataNames.remove(dataInstance.getName());
                results.put(dataInstance.getName(), dataInstance.getValue());
            }
            if (!dataNames.isEmpty()) {
                throw new SExpressionEvaluationException("Some data were not found " + dataNames, dataNames.get(0));
            }
            return buildExpressionResultSameOrderAsInputList(expressions, results);
        } catch (final SDataInstanceException e) {
            throw new SExpressionEvaluationException(e, null);
        }
    }

    private List<Object> buildExpressionResultSameOrderAsInputList(final List<SExpression> expressions, final Map<String, Serializable> results) {
        final List<Object> list = new ArrayList<Object>(expressions.size());
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
