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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.bonitasoft.engine.bpm.document.ArchivedDocument;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCaseDocumentItem;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ArchivedCaseDocumentItemConverterTest extends APITestWithMock {

    @Test
    public void should_convert_engine_document_into_portal_document() {

        // Given
        final ArchivedCaseDocumentItemConverter documentItemConverter = new ArchivedCaseDocumentItemConverter();
        final ArchivedDocument engineItem = mock(ArchivedDocument.class);
        when(engineItem.getId()).thenReturn(1l);
        when(engineItem.getProcessInstanceId()).thenReturn(1l);
        when(engineItem.getName()).thenReturn("Doc 1");
        when(engineItem.getAuthor()).thenReturn(1l);
        when(engineItem.getContentFileName()).thenReturn("doc.jpg");
        when(engineItem.getCreationDate()).thenReturn(new Date());
        when(engineItem.getContentMimeType()).thenReturn("image");
        when(engineItem.hasContent()).thenReturn(true);
        when(engineItem.getContentStorageId()).thenReturn("1");
        when(engineItem.getUrl()).thenReturn("http://url.com?test=d");
        when(engineItem.getSourceObjectId()).thenReturn(1l);

        // When
        final ArchivedCaseDocumentItem documentItem = documentItemConverter.convert(engineItem);

        // Assert
        assertTrue(documentItem.getId().equals(1l));
        assertTrue(documentItem.getCaseId().equals(1l));
        assertTrue(documentItem.getName().equals("Doc 1"));
        assertTrue(documentItem.getSubmittedBy().equals(1l));
        assertTrue(documentItem.getFileName().equals("doc.jpg"));
        assertTrue(documentItem.getCreationDate().equals(engineItem.getCreationDate()));
        assertTrue(documentItem.getMIMEType().equals("image"));
        assertTrue(documentItem.hasContent());
        assertTrue(documentItem.getStorageId().equals("1"));
        assertTrue(documentItem.getURL().equals("http://url.com?test=d"));
        assertTrue(documentItem.getSourceObjectId().equals(1l));

    }

}
