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
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.bonitasoft.console.common.server.preferences.properties.ConfigurationFilesManager;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.ArchivedDocument;
import org.bonitasoft.engine.bpm.document.ArchivedDocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.DocumentException;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCaseDocumentItem;
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
public class ArchivedCaseDocumentDatastoreTest extends APITestWithMock {

    private ArchivedCaseDocumentDatastore documentDatastore;

    @Mock
    private WebBonitaConstantsUtils constantsValue;

    @Mock
    private APISession engineSession;

    @Mock
    private ProcessAPI processAPI;

    @Mock
    private ArchivedDocument mockedDocument;

    @Mock
    private SearchResult<ArchivedDocument> mockedEngineSearchResults;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        ConfigurationFilesManager.getInstance()
                .setTenantConfigurationFiles(Collections.singletonMap("console-config.properties",
                        "form.attachment.max.size=30".getBytes()));
        initMocks(this);
        when(mockedDocument.getName()).thenReturn("Doc 1");
        when(mockedDocument.getId()).thenReturn(1L);
        documentDatastore = spy(new ArchivedCaseDocumentDatastore(engineSession, processAPI));
    }

    // ---------- GET METHOD TESTS ------------------------------//

    @Test
    public void it_should_call_engine_processAPI_getDocument() throws Exception {
        // Given
        final APIID id = APIID.makeAPIID(1l);

        // When
        documentDatastore.get(id);

        // Then
        verify(processAPI).getArchivedProcessDocument(id.toLong());
    }

    @Test(expected = APIException.class)
    public void it_should_catch_and_throw_APIException_for_not_find_document()
            throws ArchivedDocumentNotFoundException {
        // Given
        final APIID id = APIID.makeAPIID(1l);
        when(processAPI.getArchivedProcessDocument(id.toLong()))
                .thenThrow(new ArchivedDocumentNotFoundException(new Exception()));

        // When
        documentDatastore.get(id);
    }

    @Test
    public void it_should_call_convertEngineToConsole_method() {
        // Given
        final APIID id = APIID.makeAPIID(1l);

        // When
        documentDatastore.get(id);

        // Then
        verify(documentDatastore).convertEngineToConsoleItem(any());
    }

    // ---------- CONVERT ITEM TESTS ------------------------------//

    @Test
    public void it_should_convert_item_return_item() {
        // When
        final ArchivedCaseDocumentItem convertedEngineToConsoleItem = documentDatastore
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

    // ---------- SEARCH TESTS -------------------------------------------------//
    @Test
    public void it_should_call_buildSearchOptionCreator_method() throws SearchException {
        // Given
        when(processAPI.searchArchivedDocuments(any())).thenReturn(mockedEngineSearchResults);
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
        when(processAPI.searchArchivedDocuments(any(SearchOptions.class))).thenReturn(mockedEngineSearchResults);
        final Map<String, String> filters = new HashMap<>();
        filters.put("submittedBy", "1");

        // When
        documentDatastore.searchDocument(0, 10, "hello", filters, "name ASC");

        // Then
        verify(processAPI).searchArchivedDocuments(documentDatastore.searchOptionsCreator.create());
    }

    // -------------DELETE METHOD TESTS ------------------------------------------//
    @Test
    public void it_should_delete_one_document() throws DocumentNotFoundException, DocumentException {
        final List<APIID> docs = new ArrayList<>();
        docs.add(APIID.makeAPIID(mockedDocument.getId()));

        // When
        documentDatastore.delete(docs);

        // Then
        verify(processAPI).deleteContentOfArchivedDocument(1L);
        verify(processAPI, times(1)).deleteContentOfArchivedDocument(any(Long.class));
    }

    @Test
    public void it_should_delete_two_documents() throws DocumentNotFoundException, DocumentException {
        final List<APIID> docs = new ArrayList<>();
        docs.add(APIID.makeAPIID(mockedDocument.getId()));
        docs.add(APIID.makeAPIID(mockedDocument.getId()));

        // When
        documentDatastore.delete(docs);

        // Then
        verify(processAPI, times(2)).deleteContentOfArchivedDocument(1L);
    }

    @Test
    public void it_should_throw_an_exception_when_input_is_null() throws DocumentNotFoundException, DeletionException {
        expectedEx.expect(APIException.class);
        expectedEx.expectMessage("Error while deleting a document. Document id not specified in the request");

        // When
        documentDatastore.delete(null);
    }
}
