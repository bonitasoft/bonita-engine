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
 */
package org.bonitasoft.engine.core.document.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.contract.FileInputValue;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.bpm.document.impl.DocumentImpl;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.impl.SMappedDocumentImpl;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SDocumentListDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SDocumentListDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DocumentHelperTest {

    public static final long AUTHOR_ID = 12L;
    public static final long PROCESS_INSTANCE_ID = 45L;
    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Mock
    private ProcessDefinitionService processDefinitionService;
    @Mock
    private ProcessInstanceService processInstanceService;
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
        doThrow(SBonitaReadException.class).when(processDefinitionService).getProcessDefinition(154l);
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
            SBonitaReadException {
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
        doReturn(list).when(documentService).getDocumentList("theList", PROCESS_INSTANCE_ID, 0, 100);
        List<SMappedDocument> list2 = createList(50);
        doReturn(list2).when(documentService).getDocumentList("theList", PROCESS_INSTANCE_ID, 100, 100);
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
        documentHelper.deleteDocument("myDoc", PROCESS_INSTANCE_ID);
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
    public void should_createOrUpdate_update_if_found() throws Exception {
        //given
        SMappedDocumentImpl docToUpdate = new SMappedDocumentImpl();
        docToUpdate.setId(145);
        doReturn(docToUpdate).when(documentService).getMappedDocument(PROCESS_INSTANCE_ID, "myDoc");
        //when
        DocumentValue docValue = new DocumentValue("myUrl");
        documentHelper.createOrUpdateDocument(docValue, "myDoc", PROCESS_INSTANCE_ID, AUTHOR_ID, null);
        //then
        verify(documentService).updateDocument(eq(docToUpdate), any(SDocument.class));
    }

    @Test
    public void should_createOrUpdate_create_if_not_found_if_found() throws Exception {
        //given
        doThrow(SObjectNotFoundException.class).when(documentService).getMappedDocument(PROCESS_INSTANCE_ID, "myDoc");
        //when
        DocumentValue docValue = new DocumentValue("myUrl");
        documentHelper.createOrUpdateDocument(docValue, "myDoc", PROCESS_INSTANCE_ID, AUTHOR_ID, null);
        //then
        verify(documentService).attachDocumentToProcessInstance(any(SDocument.class), eq(PROCESS_INSTANCE_ID), eq("myDoc"), nullable(String.class));
    }

    @Test
    public void should_setDocumentList_set_the_list() throws Exception {
        DocumentHelper documentHelperSpy = spy(documentHelper);
        List<SMappedDocument> existingList = Arrays.<SMappedDocument>asList(new SMappedDocumentImpl());
        doReturn(existingList).when(documentHelperSpy).getExistingDocumentList("theList", PROCESS_INSTANCE_ID);

        DocumentValue docValue1 = new DocumentValue("url1");
        DocumentValue docValue2 = new DocumentValue("url2");
        documentHelperSpy.setDocumentList(Arrays.asList(docValue1, docValue2), "theList", PROCESS_INSTANCE_ID, AUTHOR_ID);

        verify(documentHelperSpy).processDocumentOnIndex(docValue1, "theList", PROCESS_INSTANCE_ID, existingList, 0, AUTHOR_ID);
        verify(documentHelperSpy).processDocumentOnIndex(docValue2, "theList", PROCESS_INSTANCE_ID, existingList, 1, AUTHOR_ID);
        verify(documentHelperSpy).removeOthersDocuments(existingList);
    }

    @Test
    public void should_getExistingDocumentList_return_the_list() throws Exception {
        //given
        DocumentHelper documentHelperSpy = spy(documentHelper);
        List<SMappedDocument> existingList = createList(5);
        doReturn(existingList).when(documentHelperSpy).getAllDocumentOfTheList(PROCESS_INSTANCE_ID, "theList");
        //when
        List<SMappedDocument> theList = documentHelperSpy.getExistingDocumentList("theList", PROCESS_INSTANCE_ID);
        //then
        assertThat(theList).isEqualTo(existingList);
    }

    @Test
    public void should_getExistingDocumentList_return_empty_if_in_def() throws Exception {
        //given
        DocumentHelper documentHelperSpy = spy(documentHelper);
        doReturn(Collections.emptyList()).when(documentHelperSpy).getAllDocumentOfTheList(PROCESS_INSTANCE_ID, "theList");
        doReturn(true).when(documentHelperSpy).isListDefinedInDefinition("theList", PROCESS_INSTANCE_ID);
        //when
        List<SMappedDocument> theList = documentHelperSpy.getExistingDocumentList("theList", PROCESS_INSTANCE_ID);
        //then
        assertThat(theList).isEmpty();
    }

    @Test(expected = SObjectNotFoundException.class)
    public void should_getExistingDocumentList_return_throw_exception_if_not_in_def() throws Exception {
        //given
        DocumentHelper documentHelperSpy = spy(documentHelper);
        doReturn(Collections.emptyList()).when(documentHelperSpy).getAllDocumentOfTheList(PROCESS_INSTANCE_ID, "theList");
        doReturn(false).when(documentHelperSpy).isListDefinedInDefinition("theList", PROCESS_INSTANCE_ID);
        //when
        documentHelperSpy.getExistingDocumentList("theList", PROCESS_INSTANCE_ID);
        //then exception
    }

    @Test
    public void should_processDocumentOnIndex_create_new() throws Exception {
        //given
        List<SMappedDocument> list = createList(5);
        DocumentValue documentValue = new DocumentValue("new url");
        //when
        documentHelper.processDocumentOnIndex(documentValue, "theList", PROCESS_INSTANCE_ID, list, 3, AUTHOR_ID);
        //then
        verify(documentService).attachDocumentToProcessInstance(any(SDocument.class), eq(PROCESS_INSTANCE_ID), eq("theList"), nullable(String.class), eq(3));
    }

    @Test
    public void should_processDocumentOnIndex_update_index() throws Exception {
        //given
        DocumentHelper documentHelperSpy = spy(documentHelper);
        List<SMappedDocument> list = createList(5);
        SMappedDocument documentToUpdate = list.get(list.size() - 1);
        DocumentValue documentValue = new DocumentValue(documentToUpdate.getId(), "new url");
        //when
        documentHelperSpy.processDocumentOnIndex(documentValue, "theList", PROCESS_INSTANCE_ID, list, 3, AUTHOR_ID);
        //then
        verify(documentHelperSpy).updateExistingDocument(documentToUpdate, 3, documentValue, AUTHOR_ID);
    }

    @Test(expected = SObjectNotFoundException.class)
    public void should_processDocumentOnIndex_update_index_of_unexisting_doc() throws Exception {
        //given
        DocumentHelper documentHelperSpy = spy(documentHelper);
        List<SMappedDocument> list = createList(5);
        DocumentValue documentValue = new DocumentValue(125l, "new url");
        //when
        documentHelperSpy.processDocumentOnIndex(documentValue, "theList", PROCESS_INSTANCE_ID, list, 3, AUTHOR_ID);
        //then exception
    }

    @Test
    public void should_updateExistingDocument_with_unmodified_content_update_only_index() throws Exception {
        //given
        DocumentValue documentValue = new DocumentValue(125l);
        SMappedDocumentImpl documentToUpdate = new SMappedDocumentImpl();
        documentToUpdate.setIndex(1);
        //when
        documentHelper.updateExistingDocument(documentToUpdate, 2, documentValue, AUTHOR_ID);
        //then
        verify(documentService).updateDocumentIndex(documentToUpdate, 2);
    }

    @Test
    public void should_updateExistingDocument_with_unmodified_content_and_index_do_nothing() throws Exception {
        //given
        DocumentValue documentValue = new DocumentValue(125l);
        SMappedDocumentImpl documentToUpdate = new SMappedDocumentImpl();
        documentToUpdate.setIndex(2);
        //when
        documentHelper.updateExistingDocument(documentToUpdate, 2, documentValue, AUTHOR_ID);
        //then
        verify(documentService, times(0)).updateDocumentIndex(documentToUpdate, 2);
    }

    @Test
    public void should_updateExistingDocument_with_modified_content_update_everything() throws Exception {
        //given
        DocumentValue documentValue = new DocumentValue(125l, "the new url");
        SMappedDocumentImpl documentToUpdate = new SMappedDocumentImpl();
        documentToUpdate.setIndex(1);
        //when
        documentHelper.updateExistingDocument(documentToUpdate, 2, documentValue, AUTHOR_ID);
        //then
        verify(documentService).updateDocumentOfList(eq(documentToUpdate), any(SDocument.class), eq(2));
    }

    @Test
    public void should_getMimeTypeOrGuessIt_return_the_original_mime_type_if_not_null() {
        //given
        final DocumentValue documentValue = new DocumentValue(new byte[]{1, 2}, "myMimeType", "theFile.bin");
        //when
        final String mimeTypeOrGuessIt = documentHelper.getMimeTypeOrGuessIt(documentValue);
        //then
        assertThat(mimeTypeOrGuessIt).isEqualTo("myMimeType");

    }

    @Test
    public void should_getMimeTypeOrGuessIt_guess_plain_text_mime_type() {
        //given
        final DocumentValue documentValue = new DocumentValue("content".getBytes(), null, "theFile.txt");
        //when
        final String mimeTypeOrGuessIt = documentHelper.getMimeTypeOrGuessIt(documentValue);
        //then
        assertThat(mimeTypeOrGuessIt == null || mimeTypeOrGuessIt.equals("text/plain")).as("mimetype should be text/plain or null on mac").isTrue();
    }

    @Test
    public void should_getMimeTypeOrGuessIt_guess_plain_text_mime_type_when_empty() {
        //given
        final DocumentValue documentValue = new DocumentValue("content".getBytes(), "", "theFile.txt");
        //when
        final String mimeTypeOrGuessIt = documentHelper.getMimeTypeOrGuessIt(documentValue);
        //then
        assertThat(mimeTypeOrGuessIt == null || mimeTypeOrGuessIt.equals("text/plain")).as("mimetype should be text/plain or null on mac").isTrue();
    }

    @Test
    public void should_getMimeTypeOrGuessIt_guess_xml_mime_type() {
        //given
        final String xmlFileContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<note>\n" +
                "\t<to>Tove</to>\n" +
                "\t<from>Jani</from>\n" +
                "\t<heading>Reminder</heading>\n" +
                "\t<body>Don't forget me this weekend!</body>\n" +
                "</note>";
        final DocumentValue documentValue = new DocumentValue(xmlFileContent.getBytes(), null, "theFile.xml");
        //when
        final String mimeTypeOrGuessIt = documentHelper.getMimeTypeOrGuessIt(documentValue);
        //then
        assertThat(mimeTypeOrGuessIt).as("mimetype").isIn( //
                null, "application/xml" // Mac/Linux
                , "text/xml" // Windows
        );
    }

    @Test
    public void should_getMimeTypeOrGuessIt_guess_application_octet_stream_if_byte_array() {
        //given
        final DocumentValue documentValue = new DocumentValue(new byte[]{1, 2}, null, "theFile.bin");
        //when
        final String mimeTypeOrGuessIt = documentHelper.getMimeTypeOrGuessIt(documentValue);
        //then
        assertThat(mimeTypeOrGuessIt == null || mimeTypeOrGuessIt.equals("application/octet-stream")).as("mimetype should be application/octet-stream or null on mac").isTrue();
    }

    @Test
    public void should_getMimeTypeOrGuessIt_do_not_fail_with_bad_filename() {
        //given
        final DocumentValue documentValue = new DocumentValue(new byte[]{1, 2}, null, "the\0File.bin");
        //when
        final String mimeTypeOrGuessIt = documentHelper.getMimeTypeOrGuessIt(documentValue);
        //then
        assertThat(mimeTypeOrGuessIt).isNull();
    }

    @Test
    public void should_toCheckedDocumentValue_return_new_DocumentValue_for_FileInput() throws Exception {
        final FileInputValue fileInputValue = new FileInputValue("theFile.txt", "contentType", "It's my file".getBytes());

        final DocumentValue documentValue = documentHelper.toCheckedDocumentValue(fileInputValue);

        assertThat(documentValue).isEqualToIgnoringGivenFields(new DocumentValue(null, "contentType", "theFile.txt"), "content");
        assertThat(documentValue.getContent()).isEqualTo("It's my file".getBytes());
    }

    @Test
    public void should_toCheckedList_check_null() throws Exception {
        exception.expect(SOperationExecutionException.class);
        exception.expectMessage("Document operation only accepts an expression returning a list of DocumentValue");
        documentHelper.toCheckedList(null);
    }

    @Test
    public void should_toCheckedList_check_not_list() throws Exception {
        exception.expect(SOperationExecutionException.class);
        exception.expectMessage("Document operation only accepts an expression returning a list of DocumentValue");
        documentHelper.toCheckedList(new Object());
    }

    @Test
    public void should_toCheckedList_check_not_all_doc() throws Exception {
        exception.expect(SOperationExecutionException.class);
        exception.expectMessage("Document operation only accepts an expression returning a list of DocumentValue");
        documentHelper.toCheckedList(Arrays.asList(new DocumentValue("theUrl"), new Object()));
    }

    @Test
    public void should_toCheckedList_returns_the_list_if_contains_FileInputValue() throws Exception {
        final List<FileInputValue> inputList = Collections.singletonList(new FileInputValue("report.pdf", "contentType", "The report content".getBytes()));
        final List<DocumentValue> result = documentHelper.toCheckedList(inputList);
        assertThat(result.get(0).getContent()).isEqualTo("The report content".getBytes());
        assertThat(result.get(0).getFileName()).isEqualTo("report.pdf");
        assertThat(result.get(0).getMimeType()).isEqualTo("contentType");
    }

    @Test
    public void should_toCheckedList_returns_the_list_if_contains_DocumentImpl() throws Exception {
        DocumentImpl documentWithContent = new DocumentImpl();
        documentWithContent.setHasContent(true);
        documentWithContent.setContentMimeType("application/pdf");
        documentWithContent.setContentStorageId("the storage ID");
        documentWithContent.setFileName("myPdf.pdf");
        final List<DocumentImpl> inputList = Collections.singletonList(documentWithContent);
        doReturn("the pdf content".getBytes()).when(documentService).getDocumentContent("the storage ID");

        final List<DocumentValue> result = documentHelper.toCheckedList(inputList);


        assertThat(result.get(0).getContent()).isEqualTo("the pdf content".getBytes());
        assertThat(result.get(0).getFileName()).isEqualTo("myPdf.pdf");
        assertThat(result.get(0).getMimeType()).isEqualTo("application/pdf");
    }

    @Test
    public void should_toCheckedList_returns_the_list_if_contains_external_DocumentImpl() throws Exception {
        DocumentImpl documentWithContent = new DocumentImpl();
        documentWithContent.setHasContent(false);
        documentWithContent.setUrl("the url");
        final List<DocumentImpl> inputList = Collections.singletonList(documentWithContent);

        final List<DocumentValue> result = documentHelper.toCheckedList(inputList);

        assertThat(result.get(0).getUrl()).isEqualTo("the url");
    }

    @Test
    public void should_toCheckedList_throw_exception_with_document_having_content_not_found() throws Exception {
        DocumentImpl documentWithContent = new DocumentImpl();
        documentWithContent.setHasContent(true);
        documentWithContent.setContentMimeType("application/pdf");
        documentWithContent.setContentStorageId("the storage ID");
        documentWithContent.setFileName("myPdf.pdf");
        final List<DocumentImpl> inputList = Collections.singletonList(documentWithContent);
        doThrow(SObjectNotFoundException.class).when(documentService).getDocumentContent("the storage ID");


        exception.expect(SOperationExecutionException.class);
        exception.expectMessage("Unable to execute set document operation because the content of the document to use is not found");

        documentHelper.toCheckedList(inputList);
    }


}
