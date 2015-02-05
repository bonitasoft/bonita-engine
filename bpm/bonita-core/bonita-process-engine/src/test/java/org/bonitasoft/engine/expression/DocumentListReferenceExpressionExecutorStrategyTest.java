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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SDocumentListDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SDocumentListDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DocumentListReferenceExpressionExecutorStrategyTest {

    @Mock
    private DocumentService documentService;
    @Mock
    private FlowNodeInstanceService flowNodeInstanceService;
    @Mock
    private ProcessInstanceService processInstanceService;
    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Mock
    private SProcessInstance processInstance;
    @Mock
    private SProcessDefinition processDefinition;
    @Mock
    private SFlowElementContainerDefinition flowElementContainerDefinition;

    @InjectMocks
    private DocumentListReferenceExpressionExecutorStrategy documentListReferenceExpressionExecutorStrategy;

    @Before
    public void setUp() {
    }

    @Test
    public void should_getDocument_return_empty() throws Exception {
        //given
        doReturn(Collections.emptyList()).when(documentService).getDocumentList("theList", 45l, 0, 100);
        initDefinition("theList");

        //when
        final List<Document> theList = documentListReferenceExpressionExecutorStrategy.getDocumentList(45l, "theList", null);

        assertThat(theList).isEmpty();

    }

    @Test
    public void should_getDocument_return_null() throws Exception {
        //given
        doReturn(Collections.emptyList()).when(documentService).getDocumentList("theList", 45l, 0, 100);
        initDefinition("notTheList");

        //when
        final List<Document> theList = documentListReferenceExpressionExecutorStrategy.getDocumentList(45l, "theList", null);

        assertThat(theList).isNull();
    }

    @Test
    public void should_getDocument_return_theList() throws Exception {
        //given
        final List<SMappedDocument> toBeReturned = Collections.singletonList(mock(SMappedDocument.class));
        doReturn(toBeReturned).when(documentService).getDocumentList("theList", 45l, 0, 100);

        //when
        final List<Document> theList = documentListReferenceExpressionExecutorStrategy.getDocumentList(45l, "theList", null);

        assertThat(theList).hasSize(1);
    }

    private void initDefinition(final String... names) throws SProcessInstanceNotFoundException, SProcessInstanceReadException,
            SProcessDefinitionNotFoundException,
            SProcessDefinitionReadException {
        doReturn(processInstance).when(processInstanceService).getProcessInstance(45l);
        doReturn(154l).when(processInstance).getProcessDefinitionId();
        doReturn(processDefinition).when(processDefinitionService).getProcessDefinition(154l);
        doReturn(flowElementContainerDefinition).when(processDefinition).getProcessContainer();
        doReturn(createListOfDocumentListDefinition(names)).when(flowElementContainerDefinition).getDocumentListDefinitions();
    }

    private List<SDocumentListDefinition> createListOfDocumentListDefinition(final String... names) {
        final List<SDocumentListDefinition> list = new ArrayList<SDocumentListDefinition>();
        for (final String name : names) {
            list.add(new SDocumentListDefinitionImpl(name));
        }
        return list;
    }
}
