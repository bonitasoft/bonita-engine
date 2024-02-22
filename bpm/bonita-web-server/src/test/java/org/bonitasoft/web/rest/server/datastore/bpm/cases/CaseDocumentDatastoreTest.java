/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.web.rest.server.datastore.bpm.cases;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.io.FileContent;
import org.bonitasoft.engine.io.TemporaryFileNotFoundException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.cases.CaseDocumentItem;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CaseDocumentDatastoreTest extends APITestWithMock {

    private CaseDocumentDatastore documentDatastore;

    @Mock
    private APISession engineSession;

    @Mock
    private ProcessAPI processAPI;

    @Mock
    private Document mockedDocument;

    @Mock
    private BonitaHomeFolderAccessor tenantFolder;

    @Mock
    private SearchResult<Document> mockedEngineSearchResults;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private final CaseDocumentItem mockedDocumentItem = new CaseDocumentItem();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(mockedDocument.getName()).thenReturn("Doc 1");
        when(mockedDocument.getId()).thenReturn(1L);
        documentDatastore = spy(new CaseDocumentDatastore(engineSession, processAPI, tenantFolder));
    }

    // ---------- GET METHOD TESTS ------------------------------//

    @Test
    public void it_should_call_engine_processAPI_getDocument() throws Exception {
        // Given
        final APIID id = APIID.makeAPIID(1L);

        // When
        documentDatastore.get(id);

        // Then
        verify(processAPI).getDocument(id.toLong());
    }

    @Test(expected = APIException.class)
    public void it_should_catch_and_throw_APIException_for_not_find_document() throws Exception {
        // Given
        final APIID id = APIID.makeAPIID(1L);
        when(processAPI.getDocument(id.toLong()))
                .thenThrow(new DocumentNotFoundException("not found", new Exception()));

        // When
        documentDatastore.get(id);
    }

    @Test
    public void it_should_call_convertEngineToConsole_method() {
        // Given
        final APIID id = APIID.makeAPIID(1L);

        // When
        documentDatastore.get(id);

        // Then
        verify(documentDatastore).convertEngineToConsoleItem(any());
    }

    // ---------- CONVERT ITEM TESTS ------------------------------//

    @Test
    public void it_should_convert_item_return_item() {
        // When
        final CaseDocumentItem convertedEngineToConsoleItem = documentDatastore
                .convertEngineToConsoleItem(mockedDocument);
        // Then
        assertTrue(convertedEngineToConsoleItem != null);
    }

    @Test
    public void it_should_not_convert_null_item_return_null() {
        // When
        final CaseDocumentItem convertedEngineToConsoleItem = documentDatastore.convertEngineToConsoleItem(null);
        // Then
        assertTrue(convertedEngineToConsoleItem == null);
    }

    // ---------- buildDocumentValueFromUploadPath TESTS ------------------------------//
    @Test
    public void it_should_create_documentvalue_with_given_filename() throws Exception {
        String uploadKey = "3544697";
        when(tenantFolder.retrieveUploadedTempContent(uploadKey))
                .thenReturn(new FileContent("doc.jpg", InputStream.nullInputStream(), "img/jpg"));

        // When
        final DocumentValue documentValue = documentDatastore.buildDocumentValueFromUploadPath(uploadKey, 1,
                "fileName");
        // Then
        assertTrue(documentValue.getFileName().equals("fileName"));
    }

    @Test
    public void it_should_create_documentvalue_with_name_of_the_uploaded_file() throws Exception {
        String uploadKey = "46645";
        when(tenantFolder.retrieveUploadedTempContent(uploadKey))
                .thenReturn(new FileContent("doc.jpg", InputStream.nullInputStream(), "img/jpg"));

        // When
        final DocumentValue documentValue = documentDatastore.buildDocumentValueFromUploadPath(uploadKey, 1, "");
        // Then
        assertTrue(documentValue.getFileName().equals("doc.jpg"));
    }

    // ---------- ADD METHOD TESTS ------------------------------//

    @Test
    public void it_should_add_a_document_calling_addDocument_with_upload_Path() throws Exception {
        // Given
        String uploadKey = "1456";
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_CASE_ID, 1L);
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_NAME, "doc 1");
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_UPLOAD_PATH, uploadKey);

        when(tenantFolder.retrieveUploadedTempContent(uploadKey))
                .thenReturn(new FileContent("doc.jpg", InputStream.nullInputStream(), "img/jpg"));

        // When
        documentDatastore.add(mockedDocumentItem);

        // Then
        verify(documentDatastore).buildDocumentValueFromUploadPath(uploadKey, -1, null);
        verify(processAPI).addDocument(eq(1L), eq("doc 1"), eq(""), any(DocumentValue.class));
    }

    @Test
    public void it_should_add_a_document_calling_addDocument_with_upload_Path_and_fileName() throws Exception {
        // Given
        String uploadKey = "58768";
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_CASE_ID, 1L);
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_NAME, "doc 1");
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_CONTENT_FILENAME, "doc_file_name.jpg");
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_UPLOAD_PATH, uploadKey);

        when(tenantFolder.retrieveUploadedTempContent(uploadKey))
                .thenReturn(new FileContent("doc.jpg", InputStream.nullInputStream(), "img/jpg"));

        // When
        documentDatastore.add(mockedDocumentItem);

        // Then
        verify(documentDatastore).buildDocumentValueFromUploadPath(uploadKey, -1, "doc_file_name.jpg");
        verify(processAPI).addDocument(eq(1L), eq("doc 1"), eq(""), any(DocumentValue.class));
    }

    @Test
    public void it_should_add_a_document_calling_addDocument_with_upload_Path_with_index_and_description()
            throws Exception {
        // Given
        String uploadKey = "546374";
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_CASE_ID, 1L);
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_NAME, "doc 1");
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_UPLOAD_PATH, uploadKey);
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_DESCRIPTION, "This is a description");
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_INDEX, "2");

        when(tenantFolder.retrieveUploadedTempContent(uploadKey))
                .thenReturn(new FileContent("doc.jpg", InputStream.nullInputStream(), "img/jpg"));

        // When
        documentDatastore.add(mockedDocumentItem);

        // Then
        verify(documentDatastore).buildDocumentValueFromUploadPath(uploadKey, 2, null);
        verify(processAPI).addDocument(eq(1L), eq("doc 1"), eq("This is a description"), any(DocumentValue.class));
    }

    @Test(expected = APIException.class)
    public void it_should_not_add_a_document_calling_addDocument_with_invalid_upload_Path() throws Exception {
        // Given
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_CASE_ID, 1L);
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_NAME, "doc 1");
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_UPLOAD_PATH, "unexisting.document");

        when(tenantFolder.retrieveUploadedTempContent("unexisting.document"))
                .thenThrow(TemporaryFileNotFoundException.class);

        try {
            // When
            documentDatastore.add(mockedDocumentItem);
        } finally {
            // Then
            verify(documentDatastore).buildDocumentValueFromUploadPath("unexisting.document", -1, null);
            verify(processAPI, times(0)).addDocument(eq(1L), eq("doc 1"), eq(""), any(DocumentValue.class));
        }
    }

    @Test
    public void it_should_add_a_document_calling_addDocument_with_external_Url() throws Exception {
        // Given
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_CASE_ID, 1L);
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_NAME, "doc 1");
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_URL, "http://images/doc.jpg");

        // When
        documentDatastore.add(mockedDocumentItem);

        // Then
        verify(documentDatastore).buildDocumentValueFromUrl("http://images/doc.jpg", -1);
        verify(processAPI).addDocument(eq(1L), eq("doc 1"), eq(""), any(DocumentValue.class));
    }

    @Test(expected = APIException.class)
    public void it_throws_an_exception_adding_a_document_with_invalid_inputs() {
        // Given
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_CASE_ID, -1);
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_NAME, "");
        // byte[] fileContent = DocumentUtil.getArrayByteFromFile(new File(docUrl));
        // When
        documentDatastore.add(mockedDocumentItem);

    }

    @Test(expected = APIException.class)
    public void it_throws_an_exception_adding_a_document_with_missing_inputs() {
        // Given
        mockedDocumentItem.setAttribute(CaseDocumentItem.ATTRIBUTE_NAME, "");
        // byte[] fileContent = DocumentUtil.getArrayByteFromFile(new File(docUrl));
        // When
        documentDatastore.add(mockedDocumentItem);

    }

    // ---------- UPDATE METHOD TESTS ------------------------------//

    @Test
    public void it_should_update_a_document_calling_updateDocument_with_upload_Path() throws Exception {
        // Given
        String uploadKey = "5464187";
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(CaseDocumentItem.ATTRIBUTE_UPLOAD_PATH, uploadKey);
        when(tenantFolder.retrieveUploadedTempContent(uploadKey))
                .thenReturn(new FileContent("doc.jpg", InputStream.nullInputStream(), "img/jpg"));

        // When
        documentDatastore.update(APIID.makeAPIID(1L), attributes);

        // Then
        verify(documentDatastore).buildDocumentValueFromUploadPath(uploadKey, -1, null);
        verify(processAPI).updateDocument(eq(1L), any(DocumentValue.class));
    }

    @Test
    public void it_should_update_a_document_calling_updateDocument_with_upload_Path_and_fileName() throws Exception {
        // Given
        String uploadKey = "357898";
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(CaseDocumentItem.ATTRIBUTE_UPLOAD_PATH, uploadKey);
        attributes.put(CaseDocumentItem.ATTRIBUTE_CONTENT_FILENAME, "doc_file_name.jpg");

        when(tenantFolder.retrieveUploadedTempContent(uploadKey))
                .thenReturn(new FileContent("doc.jpg", InputStream.nullInputStream(), "img/jpg"));

        // When
        documentDatastore.update(APIID.makeAPIID(1L), attributes);

        // Then
        verify(documentDatastore).buildDocumentValueFromUploadPath(uploadKey, -1, "doc_file_name.jpg");
        verify(processAPI).updateDocument(eq(1L), any(DocumentValue.class));
    }

    @Test
    public void it_should_update_a_document_calling_updateDocument_with_external_Url() throws Exception {
        // Given
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(CaseDocumentItem.ATTRIBUTE_URL, "http://images/doc.jpg");

        // When
        documentDatastore.update(APIID.makeAPIID(1L), attributes);

        // Then
        verify(documentDatastore).buildDocumentValueFromUrl("http://images/doc.jpg", -1);
        verify(processAPI).updateDocument(eq(1L), any(DocumentValue.class));
    }

    @Test(expected = APIException.class)
    public void it_should_not_update_document_and_throws_exception_for_missing_uploadPath() {
        // Given
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(CaseDocumentItem.ATTRIBUTE_NAME, "Doc 1");
        final APIID id = APIID.makeAPIID(1L);

        // When
        documentDatastore.update(id, attributes);
    }

    @Test(expected = APIException.class)
    public void it_should_not_update_document_and_throws_exception_for_invalid_uploadPath() throws Exception {
        // Given
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(CaseDocumentItem.ATTRIBUTE_NAME, "Doc 1");
        attributes.put(CaseDocumentItem.ATTRIBUTE_UPLOAD_PATH, "unexisting.document");
        final APIID id = APIID.makeAPIID(1L);
        doThrow(FileNotFoundException.class).when(documentDatastore)
                .buildDocumentValueFromUploadPath("unexisting.document", -1, null);

        try {
            // When
            documentDatastore.update(id, attributes);
        } finally {
            // Then
            verify(documentDatastore).buildDocumentValueFromUploadPath("unexisting.document", -1, null);
            verify(processAPI, times(0)).updateDocument(eq(1L), any(DocumentValue.class));
        }
    }

    // ---------- SEARCH TESTS -------------------------------------------------//
    @Test
    public void it_should_call_buildSearchOptionCreator_method() throws SearchException {
        // Given
        when(processAPI.searchDocuments(any(SearchOptions.class))).thenReturn(mockedEngineSearchResults);
        final Map<String, String> filters = new HashMap<>();
        filters.put("submittedBy", "1");

        // When
        documentDatastore.searchDocument(0, 10, "hello", filters, "name ASC");

        // Then
        verify(documentDatastore).buildSearchOptionCreator(0, 10, "hello", filters, "name ASC");
    }

    @Test
    public void it_should_call_processAPI_searchDocuments_method() throws SearchException {
        // Given
        when(processAPI.searchDocuments(any(SearchOptions.class))).thenReturn(mockedEngineSearchResults);
        final Map<String, String> filters = new HashMap<>();
        filters.put("submittedBy", "1");

        // When
        documentDatastore.searchDocument(0, 10, "hello", filters, "name ASC");

        // Then
        verify(processAPI).searchDocuments(documentDatastore.searchOptionsCreator.create());
    }

    // -------------DELETE METHOD TESTS ------------------------------------------//
    @Test
    public void it_should_delete_one_document() throws DocumentNotFoundException, DeletionException {
        final List<APIID> docs = new ArrayList<>();
        docs.add(APIID.makeAPIID(mockedDocument.getId()));

        // When
        documentDatastore.delete(docs);

        // Then
        verify(processAPI).removeDocument(1L);
        verify(processAPI, times(1)).removeDocument(any(Long.class));
    }

    @Test
    public void it_should_delete_two_documents() throws DocumentNotFoundException, DeletionException {
        final List<APIID> docs = new ArrayList<>();
        docs.add(APIID.makeAPIID(mockedDocument.getId()));
        docs.add(APIID.makeAPIID(mockedDocument.getId()));

        // When
        documentDatastore.delete(docs);

        // Then
        verify(processAPI, times(2)).removeDocument(1L);
    }

    @Test
    public void it_should_throw_an_exception_when_input_is_null() {
        expectedEx.expect(APIException.class);
        expectedEx.expectMessage("Error while deleting a document. Document id not specified in the request");

        // When
        documentDatastore.delete(null);
    }

    @Test
    public void it_should_throw_an_exception_when_document_is_not_found()
            throws DocumentNotFoundException, DeletionException {
        expectedEx.expect(APIException.class);
        expectedEx.expectMessage("Error while deleting a document. Document not found");
        // When
        when(processAPI.removeDocument(3L)).thenThrow(DocumentNotFoundException.class);

        final List<APIID> docs = new ArrayList<>();
        docs.add(APIID.makeAPIID(3L));

        // When
        documentDatastore.delete(docs);
    }

}
