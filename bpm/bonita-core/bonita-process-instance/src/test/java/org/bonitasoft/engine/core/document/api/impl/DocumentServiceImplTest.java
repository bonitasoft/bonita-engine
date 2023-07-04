/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.document.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.*;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.document.model.AbstractSMappedDocument;
import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.archive.SAMappedDocument;
import org.bonitasoft.engine.core.document.model.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.persistence.*;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Vincent Elcrin
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceImplTest {

    @Mock
    private Recorder recorder;
    @Mock
    private ReadPersistenceService persistenceService;
    @Mock
    private ReadPersistenceService definitiveArchiveReadPersistenceService;
    @Mock
    private SDocumentDownloadURLProvider urlProvider;
    @Mock
    private ArchiveService archiveService;

    private DocumentServiceImpl documentService;

    @Before
    public void setUp() {
        when(archiveService.getDefinitiveArchiveReadPersistenceService())
                .thenReturn(definitiveArchiveReadPersistenceService);
        documentService = spy(new DocumentServiceImpl(recorder, persistenceService, urlProvider, archiveService));
    }

    @Test
    public void generateDocumentURL_should_call_urlProvider() {
        doReturn("generated").when(urlProvider).generateURL("name", "docId");
        assertEquals("generated", documentService.generateDocumentURL("name", "docId"));
    }

    @Test
    public void should_getDocumentList_return_the_list() throws Exception {
        //given
        final List<SMappedDocument> documentList = Arrays.asList(new SMappedDocument(), new SMappedDocument());
        doReturn(documentList).when(persistenceService)
                .selectList(ArgumentMatchers.<SelectListDescriptor<SMappedDocument>> any());
        //when
        final List<SMappedDocument> theList = documentService.getDocumentList("theList", 45L, 0, 100);
        //then
        assertThat(theList).isEqualTo(documentList);
    }

    @Test
    public void should_getDocumentList_return_the_list_with_more_than_100_elements() throws Exception {
        //given
        final List<SMappedDocument> documentList1 = constructList(100);
        final List<SMappedDocument> documentList2 = constructList(50);
        when(persistenceService.selectList(ArgumentMatchers.<SelectListDescriptor<SMappedDocument>> any()))
                .thenReturn(documentList1).thenReturn(documentList2);
        //when
        final List<SMappedDocument> theList = documentService.getDocumentList("theList", 45L, 0, 100);
        //then
        documentList1.addAll(documentList2);
        assertThat(theList).isEqualTo(documentList1);
    }

    @Test
    public void should_getDocumentList_return_the_list_with_100_elements() throws Exception {
        //given
        final List<SMappedDocument> documentList1 = constructList(100);
        final List<SMappedDocument> documentList2 = constructList(0);
        when(persistenceService.selectList(ArgumentMatchers.<SelectListDescriptor<SMappedDocument>> any()))
                .thenReturn(documentList1).thenReturn(documentList2);
        //when
        final List<SMappedDocument> theList = documentService.getDocumentList("theList", 45L, 0, 100);
        //then
        assertThat(theList).isEqualTo(documentList1);
    }

    @Test
    public void should_call_deletion_with_right_arguments() throws Exception {
        //given
        Long sourceObjectId = 5L;
        final SAMappedDocument mappedDocument = new SAMappedDocument(1L, sourceObjectId);
        byte[] docContent = "theContent".getBytes();
        final SDocument document = new SDocument(docContent);
        ArgumentCaptor<UpdateRecord> argumentCaptor = ArgumentCaptor.forClass(UpdateRecord.class);
        when(definitiveArchiveReadPersistenceService.selectById(any()))
                .thenReturn(mappedDocument);
        when(persistenceService.selectById(any()))
                .thenReturn(document);

        //when
        documentService.deleteContentOfArchivedDocument(sourceObjectId);
        //then
        verify(recorder).recordUpdate(argumentCaptor.capture(), any());
        UpdateRecord record = argumentCaptor.getValue();
        assertThat(record.getFields()).containsOnlyKeys("content", "hasContent");
    }

    private List<SMappedDocument> constructList(final int size) {
        final ArrayList<SMappedDocument> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new SMappedDocument());
        }
        return list;
    }

    @Test
    public void should_getDocumentList_return_empty_list() throws Exception {
        //given
        doReturn(Collections.emptyList()).when(persistenceService)
                .selectList(ArgumentMatchers.<SelectListDescriptor<SMappedDocument>> any());
        //when
        final List<SMappedDocument> theList = documentService.getDocumentList("theList", 45L, 0, 100);
        //then
        assertThat(theList).isEmpty();
    }

    @Test
    public void should_getDocumentList_at_give_time_get_archived() throws Exception {
        //given
        final List<SMappedDocument> sMappedDocuments = Arrays.asList(new SMappedDocument(), new SMappedDocument());
        final List<SAMappedDocument> saMappedDocuments = Arrays.asList(new SAMappedDocument(), new SAMappedDocument());
        doReturn(saMappedDocuments).when(persistenceService).selectList(
                SelectDescriptorBuilder.getArchivedDocumentList("theList", 45L,
                        new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS), 123456789L));
        doReturn(sMappedDocuments).when(persistenceService)
                .selectList(
                        SelectDescriptorBuilder.getDocumentListCreatedBefore("theList", 45L,
                                new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS),
                                123456789L));
        //when
        final List<AbstractSMappedDocument> theList = documentService.getDocumentList("theList", 45L, 123456789L);
        //then
        final ArrayList<AbstractSMappedDocument> expected = new ArrayList<>(saMappedDocuments);
        expected.addAll(sMappedDocuments);
        assertThat(theList).isEqualTo(expected);
    }

    @Test(expected = SObjectNotFoundException.class)
    public void should_get_throw_not_found_exceptions() throws Exception {
        //when
        documentService.getDocument(123456L);
        //then exception
    }

}
