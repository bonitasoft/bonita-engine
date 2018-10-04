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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.expression.ContainerState.ACTIVE;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.impl.DocumentImpl;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SDocumentListDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SDocumentListDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DocumentListReferenceExpressionExecutorStrategyTest {

    @Mock
    private DocumentService documentService;
    @Mock
    private ActivityInstanceService activityInstanceService;
    @Mock
    private ProcessInstanceService processInstanceService;
    @Mock
    private ProcessDefinitionService processDefinitionService;

    @InjectMocks
    private DocumentListReferenceExpressionExecutorStrategy strategy;

    @Test
    public void evaluate_result_should_retrieve_documents_list_when_it_is_not_already_in_the_context() throws Exception {
        //given:
        final Map<String, Object> context = new HashMap<>();
        context.put("containerId", 123L);
        context.put("containerType", "PROCESS_INSTANCE");

        final DocumentListReferenceExpressionExecutorStrategy spiedStrategy = spy(strategy);
        final List<DocumentImpl> expectedDocumentList = singletonList(new DocumentImpl());
        doReturn(expectedDocumentList).when(spiedStrategy).getDocumentList(anyLong(), anyString(), eq(null));

        //when:
        final List<Object> result = spiedStrategy.evaluate(asList(expressionForDocumentList()), context, null, ACTIVE);

        //then:
        assertThat(result).hasSize(1).containsOnly(expectedDocumentList);
        verifyZeroInteractions(activityInstanceService);
    }

    @Test
    public void evaluate_should_directly_return_the_documents_list_when_it_is_already_in_the_context() throws Exception {
        //given:
        final Map<String, Object> context = new HashMap<>();
        final List<DocumentImpl> documentListInContext = singletonList(new DocumentImpl());
        final String docListName = "docListPreviouslyPutInContext";
        context.put(docListName, documentListInContext);

        context.put("containerId", 365498L);
        context.put("containerType", "PROCESS_INSTANCE");

        //when:
        final Object result = strategy.evaluate(expressionForDocumentList(docListName), context, null, ACTIVE);

        //then:
        assertThat(result).isSameAs(documentListInContext);
        verifyZeroInteractions(documentService);
    }

    // =================================================================================================================
    // implementation detail methods tests
    // =================================================================================================================

    @Test
    public void should_getDocument_return_empty() throws Exception {
        //given
        doReturn(emptyList()).when(documentService).getDocumentList("theList", 45l, 0, 100);
        initDefinition("theList");

        //when
        final List<Document> theList = strategy.getDocumentList(45l, "theList", null);

        assertThat(theList).isEmpty();
    }

    @Test
    public void should_getDocument_return_null() throws Exception {
        //given
        doReturn(emptyList()).when(documentService).getDocumentList("theList", 45l, 0, 100);
        initDefinition("notTheList");

        //when
        final List<Document> theList = strategy.getDocumentList(45l, "theList", null);

        assertThat(theList).isNull();
    }

    @Test
    public void should_getDocument_return_theList() throws Exception {
        //given
        doReturn(singletonList(mock(SMappedDocument.class))).when(documentService).getDocumentList("theList", 45l, 0,
                100);

        //when
        final List<Document> theList = strategy.getDocumentList(45l, "theList", null);

        assertThat(theList).hasSize(1);
    }

    // =================================================================================================================
    // UTILS
    // =================================================================================================================

    private void initDefinition(final String... names) throws SProcessInstanceNotFoundException, SProcessInstanceReadException,
            SProcessDefinitionNotFoundException,
            SBonitaReadException {
        final SProcessInstance processInstance = mock(SProcessInstance.class);
        doReturn(154l).when(processInstance).getProcessDefinitionId();
        doReturn(processInstance).when(processInstanceService).getProcessInstance(45l);

        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        final SFlowElementContainerDefinition flowElementContainerDefinition = mock(SFlowElementContainerDefinition.class);
        doReturn(flowElementContainerDefinition).when(processDefinition).getProcessContainer();
        doReturn(createListOfDocumentListDefinition(names)).when(flowElementContainerDefinition).getDocumentListDefinitions();
        doReturn(processDefinition).when(processDefinitionService).getProcessDefinition(154l);
    }

    private static List<SDocumentListDefinition> createListOfDocumentListDefinition(final String... names) {
        final List<SDocumentListDefinition> list = new ArrayList<>();
        for (final String name : names) {
            list.add(new SDocumentListDefinitionImpl(name));
        }
        return list;
    }

    private static SExpressionImpl expressionForDocumentList(final String content) {
        final SExpressionImpl expression = new SExpressionImpl();
        expression.setContent(content);
        expression.setReturnType(List.class.getName());
        expression.setExpressionType(ExpressionType.TYPE_DOCUMENT_LIST.name());
        return expression;
    }

    private static SExpressionImpl expressionForDocumentList() {
        return expressionForDocumentList("a_name_i_dont_care_about");
    }

}
