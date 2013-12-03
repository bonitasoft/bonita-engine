/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.document.api.ProcessDocumentService;
import org.bonitasoft.engine.core.process.document.model.SProcessDocument;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.document.SDocumentNotFoundException;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class DocumentReferenceExpressionExecutorStrategy extends NonEmptyContentExpressionExecutorStrategy {

    private final ProcessDocumentService processDocumentService;

    private final FlowNodeInstanceService flowNodeInstanceService;

    public DocumentReferenceExpressionExecutorStrategy(final ProcessDocumentService processDocumentService,
            final FlowNodeInstanceService flowNodeInstanceService) {
        this.processDocumentService = processDocumentService;
        this.flowNodeInstanceService = flowNodeInstanceService;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> dependencyValues, final Map<Integer, Object> resolvedExpressions)
            throws SExpressionEvaluationException {
        return evaluate(Collections.singletonList(expression), dependencyValues, resolvedExpressions).get(0);
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_DOCUMENT;
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> dependencyValues, final Map<Integer, Object> resolvedExpressions)
            throws SExpressionEvaluationException {
        try {
            final Long containerId = (Long) dependencyValues.get(CONTAINER_ID_KEY);
            final String containerType = (String) dependencyValues.get(CONTAINER_TYPE_KEY);
            if (containerId == null || containerType == null) {
                throw new SExpressionDependencyMissingException("the context to retrieve the document is not set");
            }
            final long processInstanceId;
            if (DataInstanceContainer.PROCESS_INSTANCE.name().equals(containerType)) {
                processInstanceId = containerId;
            } else {
                SFlowNodeInstance flowNodeInstance;
                flowNodeInstance = flowNodeInstanceService.getFlowNodeInstance(containerId);
                processInstanceId = flowNodeInstance.getParentProcessInstanceId();
            }
            final ArrayList<Object> results = new ArrayList<Object>(expressions.size());
            for (final SExpression expression : expressions) {
                try {

                    final SProcessDocument document = processDocumentService.getDocument(processInstanceId, expression.getContent());
                    results.add(ModelConvertor.toDocument(document));
                } catch (final SDocumentNotFoundException e) {
                    results.add(null);
                }
            }
            return results;
        } catch (final SBonitaException e) {
            throw new SExpressionEvaluationException(e);
        }
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return true;
    }

}
