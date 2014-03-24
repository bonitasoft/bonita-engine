/**
 * Copyright (C) 2012-2014 BonitaSoft S.A.
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

import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.document.api.ProcessDocumentService;
import org.bonitasoft.engine.core.process.document.model.SProcessDocument;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
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
 * @author Celine Souchet
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
            throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        return evaluate(Collections.singletonList(expression), dependencyValues, resolvedExpressions).get(0);
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_DOCUMENT;
    }

    @SuppressWarnings("unused")
    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> dependencyValues, final Map<Integer, Object> resolvedExpressions)
            throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        final Long containerId = (Long) dependencyValues.get(CONTAINER_ID_KEY);
        final String containerType = (String) dependencyValues.get(CONTAINER_TYPE_KEY);
        final Long time = (Long) dependencyValues.get("time");

        try {
            final long processInstanceId = getProcessInstance(containerId, containerType);
            final ArrayList<Object> results = new ArrayList<Object>(expressions.size());
            for (final SExpression expression : expressions) {
                results.add(getDocument(processInstanceId, expression, time));
            }
            return results;
        } catch (final SExpressionDependencyMissingException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SExpressionEvaluationException(e, null);
        }
    }

    private Document getDocument(long processInstanceId, SExpression expression, Long time) {
        try {
            SProcessDocument document;
            if (time != null) {
                document = processDocumentService.getDocument(processInstanceId, expression.getContent(), time);
            } else {
                document = processDocumentService.getDocument(processInstanceId, expression.getContent());
            }
            return ModelConvertor.toDocument(document);
        } catch (final SDocumentNotFoundException e) {
            return null;
        }
    }

    private long getProcessInstance(Long containerId, String containerType) throws SFlowNodeNotFoundException, SFlowNodeReadException,
            SExpressionDependencyMissingException {
        if (containerId == null || containerType == null) {
            throw new SExpressionDependencyMissingException("The context to retrieve the document is not set.");
        }
        if (DataInstanceContainer.PROCESS_INSTANCE.name().equals(containerType)) {
            return containerId;
        }
        return flowNodeInstanceService.getFlowNodeInstance(containerId).getParentProcessInstanceId();
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return true;
    }

}
