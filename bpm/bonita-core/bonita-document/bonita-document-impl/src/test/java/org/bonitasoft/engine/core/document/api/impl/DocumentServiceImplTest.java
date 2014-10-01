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
package org.bonitasoft.engine.core.document.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.impl.SMappedDocumentImpl;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Vincent Elcrin
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceImplTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    @Mock
    private Recorder recorder;
    @Mock
    private ReadPersistenceService persistenceService;
    @Mock
    private SDocumentDownloadURLProvider urlProvider;
    @Mock
    private EventService eventService;
    @Mock
    private TechnicalLoggerService technicalLogger;
    @Mock
    private ArchiveService archiveService;

    private DocumentServiceImpl documentService;

    @Before
    public void setUp() throws Exception {
        documentService = spy(new DocumentServiceImpl(recorder, persistenceService, urlProvider, eventService, technicalLogger, archiveService));
    }

    @Test
    public void generateDocumentURL_should_call_urlProvider() {
        doReturn("generated").when(urlProvider).generateURL("name", "docId");
        assertEquals("generated", documentService.generateDocumentURL("name", "docId"));
    }

    @Test
    public void should_getDocumentList_return_the_list() throws Exception {
        //given
        List<SMappedDocumentImpl> documentList = Arrays.asList(new SMappedDocumentImpl(), new SMappedDocumentImpl());
        doReturn(documentList).when(persistenceService).selectList(any(SelectListDescriptor.class));
        //when
        List<SMappedDocument> theList = documentService.getDocumentList("theList", 45l,0,100);
        //then
        assertThat(theList).isEqualTo(documentList);
    }

    @Test
    public void should_getDocumentList_return_the_list_with_more_than_100_elements() throws Exception {
        //given
        List<SMappedDocument> documentList1 = constructList(100);
        List<SMappedDocument> documentList2 = constructList(50);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(documentList1, documentList2);
        //when
        List<SMappedDocument> theList = documentService.getDocumentList("theList", 45l,0,100);
        //then
        documentList1.addAll(documentList2);
        assertThat(theList).isEqualTo(documentList1);
    }

    @Test
    public void should_getDocumentList_return_the_list_with_100_elements() throws Exception {
        //given
        List<SMappedDocument> documentList1 = constructList(100);
        List<SMappedDocument> documentList2 = constructList(0);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(documentList1, documentList2);
        //when
        List<SMappedDocument> theList = documentService.getDocumentList("theList", 45l,0,100);
        //then
        assertThat(theList).isEqualTo(documentList1);
    }

    private List<SMappedDocument> constructList(int size) {
        ArrayList<SMappedDocument> list = new ArrayList<SMappedDocument>();
        for (int i = 0; i < size; i++) {
            list.add(new SMappedDocumentImpl());
        }
        return list;
    }

    @Test
    public void should_getDocumentList_return_empty_list() throws Exception {
        //given
        doReturn(Collections.emptyList()).when(persistenceService).selectList(any(SelectListDescriptor.class));
        //when
        List<SMappedDocument> theList = documentService.getDocumentList("theList", 45l,0,100);
        //then
        assertThat(theList).isEmpty();
    }

}
