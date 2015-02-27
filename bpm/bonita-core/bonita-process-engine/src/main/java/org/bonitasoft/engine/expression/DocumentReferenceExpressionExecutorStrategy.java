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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class DocumentReferenceExpressionExecutorStrategy extends NonEmptyContentExpressionExecutorStrategy {

    private final DocumentService documentService;

    private final FlowNodeInstanceService flowNodeInstanceService;

    public DocumentReferenceExpressionExecutorStrategy(final DocumentService documentService,
            final FlowNodeInstanceService flowNodeInstanceService) {
        this.documentService = documentService;
        this.flowNodeInstanceService = flowNodeInstanceService;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        return evaluate(Collections.singletonList(expression), context, resolvedExpressions, containerState).get(0);
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_DOCUMENT;
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        final Long containerId = (Long) context.get(CONTAINER_ID_KEY);
        final String containerType = (String) context.get(CONTAINER_TYPE_KEY);
        final Long time = (Long) context.get("time");

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

    private Document getDocument(final long processInstanceId, final SExpression expression, final Long time) throws SBonitaReadException {
        try {
            SMappedDocument document;
            if (time != null) {
                document = documentService.getMappedDocument(processInstanceId, expression.getContent(), time);
            } else {
                document = documentService.getMappedDocument(processInstanceId, expression.getContent());
            }
            return ModelConvertor.toDocument(document, documentService);
        } catch (final SObjectNotFoundException e) {
            return null;
        }
    }

    private long getProcessInstance(final Long containerId, final String containerType) throws SFlowNodeNotFoundException, SFlowNodeReadException,
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
