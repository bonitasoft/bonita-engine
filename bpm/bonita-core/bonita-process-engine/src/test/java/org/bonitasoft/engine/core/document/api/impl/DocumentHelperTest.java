/*
 *
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.bonitasoft.engine.core.document.api.impl;

import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.impl.SMappedDocumentImpl;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SDocumentListDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SDocumentListDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DocumentHelperTest {

    public static final long AUTHOR_ID = 12l;
    public static final long PROCESS_INSTANCE_ID = 45l;
    @Mock
    private ProcessDefinitionService processDefinitionService;
    @Mock
    private ProcessInstanceService processInstanceService;
    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Mock
    private SProcessInstance processInstance;
    @Mock
    private SProcessDefinition processDefinition;
    @Mock
    private SFlowElementContainerDefinition flowElementContainerDefinition;
    @Mock
    private DocumentService documentService;
    @InjectMocks
    private DocumentHelper documentHelper;

    @Test
    public void should_isDefinedInDefinition_return_false_id_not_in_def() throws Exception {
        //given
        initDefinition("list1", "list2");
        //when then
        assertThat(documentHelper.isListDefinedInDefinition("theList", PROCESS_INSTANCE_ID)).isFalse();
    }

    @Test
    public void should_isDefinedInDefinition_throw_not_found_when_not_existing_instance() throws Exception {
        //given
        initDefinition("list1", "list2");
        doThrow(SProcessInstanceNotFoundException.class).when(processInstanceService).getProcessInstance(PROCESS_INSTANCE_ID);
        exception.expect(org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException.class);
        exception.expectMessage("Unable to find the list theList, nothing in database and the process instance 45 is not found");
        //when
        documentHelper.isListDefinedInDefinition("theList", PROCESS_INSTANCE_ID);
        //then exception
    }

    @Test
    public void should_isDefinedInDefinition_throw_not_found_when_not_existing_definition() throws Exception {
        initDefinition("list1", "list2");
        doThrow(SProcessDefinitionNotFoundException.class).when(processDefinitionService).getProcessDefinition(154l);
        exception.expect(org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException.class);
        exception.expectMessage("Unable to find the list theList on process instance 45, nothing in database and the process definition is not found");
        //when
        documentHelper.isListDefinedInDefinition("theList", PROCESS_INSTANCE_ID);
    }

    @Test
    public void should_isDefinedInDefinition_throw_read_ex_when_not_existing_instance() throws Exception {
        initDefinition("list1", "list2");
        doThrow(SProcessInstanceReadException.class).when(processInstanceService).getProcessInstance(PROCESS_INSTANCE_ID);
        exception.expect(SBonitaReadException.class);
        documentHelper.isListDefinedInDefinition("theList", PROCESS_INSTANCE_ID);
    }

    @Test
    public void should_isDefinedInDefinition_throw_read_ex_when_not_existing_definition() throws Exception {
        initDefinition("list1", "list2");
        doThrow(SProcessDefinitionReadException.class).when(processDefinitionService).getProcessDefinition(154l);
        exception.expect(SBonitaReadException.class);
        documentHelper.isListDefinedInDefinition("theList", 45);
    }

    @Test
    public void should_isDefinedInDefinition_return_true_if_in_def() throws Exception {
        //given
        initDefinition("list1", "list2", "theList");
        //when then
        assertThat(documentHelper.isListDefinedInDefinition("theList", PROCESS_INSTANCE_ID)).isTrue();
    }

    private void initDefinition(String... names) throws SProcessInstanceNotFoundException, SProcessInstanceReadException, SProcessDefinitionNotFoundException,
            SProcessDefinitionReadException {
        doReturn(processInstance).when(processInstanceService).getProcessInstance(PROCESS_INSTANCE_ID);
        doReturn(154l).when(processInstance).getProcessDefinitionId();
        doReturn(processDefinition).when(processDefinitionService).getProcessDefinition(154l);
        doReturn(flowElementContainerDefinition).when(processDefinition).getProcessContainer();
        doReturn(createListOfDocumentListDefinition(names)).when(flowElementContainerDefinition).getDocumentListDefinitions();
    }

    private List<SDocumentListDefinition> createListOfDocumentListDefinition(String... names) {
        List<SDocumentListDefinition> list = new ArrayList<SDocumentListDefinition>();
        for (String name : names) {
            list.add(new SDocumentListDefinitionImpl(name));
        }
        return list;
    }


    @Test
    public void should_getAllDocumentOfTheList_return_all_the_elements() throws Exception {
        //given
        List<SMappedDocument> list = createList(100);
        doReturn(list).when(documentService).getDocumentList("theList",PROCESS_INSTANCE_ID,0,100);
        List<SMappedDocument> list2 = createList(50);
        doReturn(list2).when(documentService).getDocumentList("theList",PROCESS_INSTANCE_ID,100,100);
        //when
        List<SMappedDocument> theList = documentHelper.getAllDocumentOfTheList(PROCESS_INSTANCE_ID, "theList");

        //then
        ArrayList<SMappedDocument> expected = new ArrayList<SMappedDocument>(list);
        expected.addAll(list2);
        assertThat(theList).hasSize(150);
        assertThat(theList).isEqualTo(expected);
    }

    private List<SMappedDocument> createList(int size) {
        List<SMappedDocument> sMappedDocuments = new ArrayList<SMappedDocument>(size);
        for (int i = 0; i < size; i++) {
            SMappedDocumentImpl sMappedDocument = new SMappedDocumentImpl();
            sMappedDocument.setId(size + (i * 1000));
            sMappedDocuments.add(sMappedDocument);
        }
        return sMappedDocuments;
    }

    @Test
    public void should_createDocumentObject_have_all_fields() {
        DocumentValue documentValue = new DocumentValue("plop".getBytes(), "mime", "filename");
        SDocument documentObject = documentHelper.createDocumentObject(documentValue, AUTHOR_ID);
        assertThat(documentObject.getContent()).isEqualTo("plop".getBytes());
        assertThat(documentObject.getMimeType()).isEqualTo("mime");
        assertThat(documentObject.getFileName()).isEqualTo("filename");
        assertThat(documentObject.hasContent()).isTrue();
        assertThat(documentObject.getAuthor()).isEqualTo(AUTHOR_ID);
    }

    @Test
    public void should_delete_document_removeCurrentVersion() throws SObjectModificationException, SObjectNotFoundException {
        //when
        documentHelper.deleteDocument("myDoc",PROCESS_INSTANCE_ID);
        //then
        verify(documentService).removeCurrentVersion(PROCESS_INSTANCE_ID, "myDoc");
    }

    @Test
    public void should_delete_document_be_quiet_when_not_found() throws SObjectModificationException, SObjectNotFoundException {
        //given
        doThrow(SObjectNotFoundException.class).when(documentService).removeCurrentVersion(PROCESS_INSTANCE_ID, "myDoc");
        //when
        documentHelper.deleteDocument("myDoc", PROCESS_INSTANCE_ID);
        //no exception
    }

    @Test(expected = SObjectModificationException.class)
    public void should_delete_document_throw_SObjectModificationException() throws SObjectModificationException, SObjectNotFoundException {
        //given
        doThrow(SObjectModificationException.class).when(documentService).removeCurrentVersion(PROCESS_INSTANCE_ID, "myDoc");
        //when
        documentHelper.deleteDocument("myDoc", PROCESS_INSTANCE_ID);
    }

    @Test
    public void should_createOrUpdate_update_if_found() throws SObjectModificationException, SObjectNotFoundException,  SObjectCreationException, SSessionNotFoundException, SBonitaReadException {
        //given
        SMappedDocumentImpl docToUpdate = new SMappedDocumentImpl();
        doReturn(docToUpdate).when(documentService).getMappedDocument(PROCESS_INSTANCE_ID,"myDoc");
        //when
        DocumentValue docValue = new DocumentValue("myUrl");
        documentHelper.createOrUpdateDocument(docValue, "myDoc", PROCESS_INSTANCE_ID, AUTHOR_ID);
        //then
        verify(documentService).updateDocument(docToUpdate,documentHelper.createDocumentObject(docValue,AUTHOR_ID));
    }

    @Test
    public void should_createOrUpdate_create_if_not_found_if_found() throws SObjectModificationException, SObjectNotFoundException, SObjectCreationException, SSessionNotFoundException, SBonitaReadException {
        //given
        doThrow(SObjectNotFoundException.class).when(documentService).getMappedDocument(PROCESS_INSTANCE_ID, "myDoc");
        //when
        DocumentValue docValue = new DocumentValue("myUrl");
        documentHelper.createOrUpdateDocument(docValue, "myDoc", PROCESS_INSTANCE_ID, AUTHOR_ID);
        //then
        verify(documentService).attachDocumentToProcessInstance(any(SDocument.class), eq(PROCESS_INSTANCE_ID), eq("myDoc"),anyString());
    }

}