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
package org.bonitasoft.web.rest.server.api.bpm.cases;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import org.bonitasoft.web.rest.model.bpm.cases.CaseDocumentItem;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDocumentDatastore;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

public class APICaseDocumentTest extends APITestWithMock {

    @Spy
    private APICaseDocument apiDocument;

    @Mock
    private CaseDocumentDatastore datastore;

    @Mock
    private CaseDocumentItem documentItemMock;

    @Before
    public void initializeMocks() {
        initMocks(this);
        // apiDocument = spy(new APIDocument());
        doReturn(datastore).when(apiDocument).getCaseDocumentDatastore();
    }

    @Test
    public void it_should_call_the_datastore_get_method() {
        // Given
        final APIID id = APIID.makeAPIID(1L);

        // When
        apiDocument.get(id);

        // Then
        verify(datastore).get(id);
    }

    @Test
    public void it_should_call_the_datastore_add_method() {
        // Given

        // When
        apiDocument.add(documentItemMock);

        // Then
        verify(datastore).add(documentItemMock);
    }

    @Test
    public void it_should_call_the_datastore_update_method() {
        // Given
        final APIID id = APIID.makeAPIID(1L);

        // When
        apiDocument.update(id, null);

        // Then
        verify(datastore).update(id, null);
    }

    @Test
    public void it_should_call_the_datastore_search_method() {
        // When
        apiDocument.search(0, 10, "hello", "documentName ASC", null);

        // Then
        verify(datastore).search(0, 10, "hello", null, "documentName ASC");
    }

}
