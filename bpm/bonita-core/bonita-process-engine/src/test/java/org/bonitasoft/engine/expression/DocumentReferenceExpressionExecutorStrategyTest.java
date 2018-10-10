/**
 * Copyright (C) 2015-2018 BonitaSoft S.A.
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

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.expression.ContainerState.ACTIVE;
import static org.bonitasoft.engine.service.ModelConvertor.toDocument;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.impl.DocumentImpl;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentReferenceExpressionExecutorStrategyTest {

    public static final long PROCESS_INSTANCE_ID = 1L;
    private static final long PARENT_PROCESS_INSTANCE_ID = 2L;
    private static final long A_LONG_TIME_AGO = 1234L;

    @Mock
    private DocumentService documentService;
    @Mock
    private ActivityInstanceService activityInstanceService;
    @InjectMocks
    private DocumentReferenceExpressionExecutorStrategy strategy;

    @Mock
    private SMappedDocument document;
    @Mock
    private SMappedDocument parentDocument;
    @Mock
    private SMappedDocument archivedDocument;

    @Before
    public void setUp() throws Exception {
        final SFlowNodeInstance flowNodeInstance = mock(SFlowNodeInstance.class);
        doReturn(flowNodeInstance).when(activityInstanceService).getFlowNodeInstance(PROCESS_INSTANCE_ID);
        doReturn(PARENT_PROCESS_INSTANCE_ID).when(flowNodeInstance).getParentProcessInstanceId();
        doReturn(document).when(documentService).getMappedDocument(eq(PROCESS_INSTANCE_ID), nullable(String.class));
        doReturn(parentDocument).when(documentService).getMappedDocument(eq(PARENT_PROCESS_INSTANCE_ID), nullable(String.class));
        doReturn(archivedDocument).when(documentService).getMappedDocument(eq(PROCESS_INSTANCE_ID), nullable(String.class), eq(A_LONG_TIME_AGO));
    }

    @Test(expected = SExpressionDependencyMissingException.class)
    public void evaluate_should_throw_an_exception_when_container_id_is_null() throws Exception {
        strategy.evaluate(Collections.emptyList(), emptyMap(), null, ACTIVE);
    }

    @Test(expected = SExpressionDependencyMissingException.class)
    public void evaluate_should_throw_an_exception_when_container_type_is_null() throws Exception {
        strategy.evaluate(Collections.emptyList(), singletonMap("containerId", PROCESS_INSTANCE_ID), null,
                ACTIVE);
    }

    @Test
    public void evaluate_result_should_contains_process_document_when_container_is_a_process_instance() throws Exception {
        final Map<String, Object> context = new HashMap<>();
        context.put("containerId", PROCESS_INSTANCE_ID);
        context.put("containerType", "PROCESS_INSTANCE");

        final List<Object> result = strategy.evaluate(asList(mock(SExpression.class)), context, null, ACTIVE);

        assertThat(result).hasSize(1).contains(toDocument(document, documentService));
    }

    @Test
    public void evaluate_result_should_contains_parent_process_document_when_container_is_not_a_process_instance() throws Exception {
        final Map<String, Object> context = new HashMap<>();
        context.put("containerId", PROCESS_INSTANCE_ID);
        context.put("containerType", "OTHER");

        final List<Object> result = strategy.evaluate(asList(mock(SExpression.class)), context, null, ACTIVE);

        assertThat(result).hasSize(1).contains(toDocument(parentDocument, documentService));
    }

    @Test
    public void evaluate_result_should_contains_null_when_document_can_not_be_found_for_a_process_instance() throws Exception {
        doThrow(SObjectNotFoundException.class).when(documentService).getMappedDocument(eq(PROCESS_INSTANCE_ID),
                nullable(String.class));
        final Map<String, Object> context = new HashMap<>();
        context.put("containerId", PROCESS_INSTANCE_ID);
        context.put("containerType", "PROCESS_INSTANCE");

        final List<Object> result = strategy.evaluate(asList(mock(SExpression.class)), context, null, ACTIVE);

        assertThat(result).hasSize(1).contains((Document) null);
    }

    @Test
    public void evaluate_result_should_contains_null_when_document_can_not_be_found_for_a_parent_process_instance() throws Exception {
        doThrow(SObjectNotFoundException.class).when(documentService).getMappedDocument(eq(PARENT_PROCESS_INSTANCE_ID),
                nullable(String.class));
        final Map<String, Object> context = new HashMap<>();
        context.put("containerId", PROCESS_INSTANCE_ID);
        context.put("containerType", "OTHER");

        final List<Object> result = strategy.evaluate(asList(mock(SExpression.class)), context, null, ACTIVE);

        assertThat(result).hasSize(1).contains((Document) null);
    }

    @Test
    public void evaluate_result_should_contains_archived_document_when_a_time_is_defined() throws Exception {
        final Map<String, Object> context = new HashMap<>();
        context.put("containerId", PROCESS_INSTANCE_ID);
        context.put("containerType", "PROCESS_INSTANCE");
        context.put("time", A_LONG_TIME_AGO);

        final List<Object> result = strategy.evaluate(singletonList(mock(SExpression.class)), context,
                null, ACTIVE);

        assertThat(result).hasSize(1).contains(toDocument(archivedDocument, documentService));
    }

    @Test
    public void evaluate_should_directly_return_the_document_when_it_is_already_in_the_context() throws Exception {
        //given:
        final Map<String, Object> context = new HashMap<>();
        final DocumentImpl docInContext = new DocumentImpl();
        final String docName = "docPreviouslyPutInContext";
        context.put(docName, docInContext);

        context.put("containerId", PROCESS_INSTANCE_ID);
        context.put("containerType", "PROCESS_INSTANCE");
        context.put("time", A_LONG_TIME_AGO);

        //when:
        final Object result = strategy.evaluate(expressionForDocument(docName), context, null,
                ACTIVE);

        //then:
        assertThat(result).isSameAs(docInContext);
        verifyZeroInteractions(documentService);
    }

    @Test
    public void getProcessInstance_should_query_archives_if_time_is_set() throws Exception {
        final long containerId = 123456L;
        final String containerType = "ACTIVITY_INSTANCE";
        final SAActivityInstance activityInstance = mock(SAActivityInstance.class);
        doReturn(activityInstance).when(activityInstanceService).getMostRecentArchivedActivityInstance(containerId);
        final long processInstanceId = 99998888777L;
        doReturn(processInstanceId).when(activityInstance).getParentProcessInstanceId();

        final long retrievedPIId = strategy.getProcessInstance(containerId, containerType, A_LONG_TIME_AGO);

        verify(activityInstanceService).getMostRecentArchivedActivityInstance(containerId);
        assertThat(retrievedPIId).isEqualTo(processInstanceId);
    }

    // =================================================================================================================
    // UTILS
    // =================================================================================================================

    private static SExpressionImpl expressionForDocument(final String content) {
        final SExpressionImpl expression = new SExpressionImpl();
        expression.setContent(content);
        expression.setReturnType(DocumentImpl.class.getName());
        expression.setExpressionType(ExpressionType.TYPE_DOCUMENT.name());
        return expression;
    }

}
