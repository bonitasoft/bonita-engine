/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.document.api.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.bonitasoft.engine.core.process.document.mapping.DocumentMappingService;
import org.bonitasoft.engine.core.process.document.mapping.model.SDocumentMapping;
import org.bonitasoft.engine.core.process.document.mapping.model.archive.SADocumentMapping;
import org.bonitasoft.engine.document.DocumentContentService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentContentServiceImplTest {

    @Mock
    private DocumentContentService documentContentService;

    @Mock
    private DocumentMappingService documentServiceMapping;

    private final SDocumentDownloadURLProvider urlProvider = new SDocumentDownloadURLProviderImpl("url");

    @Mock
    private SDocumentMapping documentMapping;

    @Mock
    private SADocumentMapping archivedDocumentMapping;

    private DocumentServiceImpl processDocumentService;

    @Before
    public void setUp() throws Exception {
        processDocumentService = new DocumentServiceImpl(documentContentService, documentServiceMapping, urlProvider);
        doReturn(documentMapping).when(documentServiceMapping).get(1L);
        doReturn(documentMapping).when(documentServiceMapping).get(1L, "document");
        doReturn(archivedDocumentMapping).when(documentServiceMapping).get(1L, "document", 1L);

        doReturn("document").when(documentMapping).getDocumentContentFileName();
        doReturn("123").when(documentMapping).getContentStorageId();
        doReturn("whateverurl").when(documentMapping).getDocumentURL();

        doReturn("document").when(archivedDocumentMapping).getDocumentContentFileName();
        doReturn("123").when(archivedDocumentMapping).getContentStorageId();
        doReturn("whateverurl").when(archivedDocumentMapping).getDocumentURL();
    }

    @Test
    public void getDocument_from_id_should_return_a_document_with_generated_url_when_it_has_content() throws Exception {
        doReturn(true).when(documentMapping).documentHasContent();

        SDocumentMapping document = processDocumentService.getDocument(1L);

        assertEquals("url?fileName=document&contentStorageId=123", document.getDocumentURL());
    }

    @Test
    public void getDocument_from_id_should_return_a_document_url_when_is_external_url() throws Exception {
        doReturn(false).when(documentMapping).documentHasContent();
        doReturn("whateverurl").when(documentMapping).getDocumentURL();

        SDocumentMapping document = processDocumentService.getDocument(1L);

        assertEquals("whateverurl", document.getDocumentURL());
    }

    @Test
    public void getDocument_from_process_instance_and_name_should_return_a_document_with_generated_url_when_it_has_content() throws Exception {
        doReturn(true).when(documentMapping).documentHasContent();

        SDocumentMapping document = processDocumentService.getDocument(1L, "document");

        assertEquals("url?fileName=document&contentStorageId=123", document.getDocumentURL());
    }

    @Test
    public void getDocument_from_process_instance_and_name_should_return_a_document_url_when_is_external_url() throws Exception {
        doReturn(false).when(documentMapping).documentHasContent();

        SDocumentMapping document = processDocumentService.getDocument(1L, "document");

        assertEquals("whateverurl", document.getDocumentURL());
    }

    @Test
    public void getDocument_from_process_instance_and_name_for_a_given_time_should_return_a_document_with_generated_url_when_it_has_content() throws Exception {
        doReturn(true).when(archivedDocumentMapping).documentHasContent();

        SDocumentMapping document = processDocumentService.getDocument(1L, "document", 1L);

        assertEquals("url?fileName=document&contentStorageId=123", document.getDocumentURL());
    }

    @Test
    public void getDocument_from_process_instance_and_name_for_a_given_time_should_return_a_document_url_when_is_external_url() throws Exception {
        doReturn(false).when(documentMapping).documentHasContent();

        SDocumentMapping document = processDocumentService.getDocument(1L, "document", 1L);

        assertEquals("whateverurl", document.getDocumentURL());
    }
}
