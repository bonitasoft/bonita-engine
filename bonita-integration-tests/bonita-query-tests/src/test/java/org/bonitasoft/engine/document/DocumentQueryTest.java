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
package org.bonitasoft.engine.document;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SDocumentMapping;
import org.bonitasoft.engine.core.document.model.SLightDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.archive.SAMappedDocument;
import org.bonitasoft.engine.test.persistence.repository.DocumentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class DocumentQueryTest {

    private static final long PROCESS_INSTANCE_ID = 6666666666666666L;

    @Inject
    private DocumentRepository repository;

    @Test
    public void getLightDocument_should_read_previously_saved_document() {
        // given
        repository.add(
                SLightDocument.builder().id(99L).tenantId(3L).author(11L).hasContent(true).fileName("myFile.pdf")
                        .mimeType("application/pdf").author(22L)
                        .build());

        //when
        final SLightDocument document = repository.getById(SLightDocument.class, 99L, 3L);

        // //then
        assertThat(document.getFileName()).isEqualTo("myFile.pdf");
        assertThat(document.getMimeType()).isEqualTo("application/pdf");
        assertThat(document.getAuthor()).isEqualTo(22L);
    }

    @Test
    public void getDocumentWithContent_should_retrieve_previously_saved_document() {
        // given
        final byte[] binaryDocContent = "someBinaryContent".getBytes();
        repository.add(
                SDocument.builder().id(666L).tenantId(2L).hasContent(true).fileName("myFile.pdf")
                        .mimeType("application/pdf").content(binaryDocContent)
                        .build());

        //when
        final SDocument document = repository.getById(SDocument.class, 666L, 2L);

        // //then
        assertThat(document.getFileName()).isEqualTo("myFile.pdf");
        assertThat(document.getMimeType()).isEqualTo("application/pdf");
        assertThat(document.getContent()).isEqualTo(binaryDocContent);
    }

    @Test
    public void getDocumentMapping_should_retrieve_previously_saved_document_mapping() {
        // given:
        repository.add(SDocumentMapping.builder().id(14L).tenantId(4L)
                .name("myDocMapping").description("doc mapping description").documentId(111L)
                .index(9).processInstanceId(987987987987L).version("2.0")
                .build());

        // when:
        SDocumentMapping documentMapping = repository.getById(SDocumentMapping.class, 14L, 4L);

        // then:
        assertThat(documentMapping.getName()).isEqualTo("myDocMapping");
        assertThat(documentMapping.getDescription()).isEqualTo("doc mapping description");
        assertThat(documentMapping.getIndex()).isEqualTo(9);
        assertThat(documentMapping.getProcessInstanceId()).isEqualTo(987987987987L);
        assertThat(documentMapping.getVersion()).isEqualTo("2.0");
        assertThat(documentMapping.getDocumentId()).isEqualTo(111L);
    }

    @Test
    public void getSMappedDocumentOfProcessWithName_should_retrieve_previously_saved_mapped_document() {
        // given:
        final SLightDocument docContent = SLightDocument.builder()
                .id(666L).tenantId(1L).hasContent(true).fileName("myFile.pdf").mimeType("application/pdf")
                .build();

        repository.add(docContent);
        repository.add(SMappedDocument.builder().id(14L).tenantId(1L)
                .name("myMappedDocument").description("doc desc")
                .index(-1).processInstanceId(444888444488844L).version("1.7")
                .document(docContent)
                .documentId(docContent.getId())
                .build());

        // when:
        SMappedDocument mappedDocument = repository.getSMappedDocumentOfProcessWithName("myMappedDocument",
                444888444488844L);

        // then:
        assertThat(mappedDocument.getName()).isEqualTo("myMappedDocument");
        assertThat(mappedDocument.getDescription()).isEqualTo("doc desc");
        assertThat(mappedDocument.getIndex()).isEqualTo(-1);
        assertThat(mappedDocument.getProcessInstanceId()).isEqualTo(444888444488844L);
        assertThat(mappedDocument.getVersion()).isEqualTo("1.7");
        assertThat(mappedDocument.getDocument()).isEqualTo(docContent);
    }

    @Test
    public void getSAMappedDocumentOfProcessWithName_should_retrieve_previously_saved_archived_mapped_document() {
        // given:
        final SLightDocument docContent = SLightDocument.builder()
                .id(915L).tenantId(1L).hasContent(true).fileName("myFile.txt").mimeType("test/plain")
                .build();

        repository.add(docContent);
        repository.add(SAMappedDocument.builder().id(32L).tenantId(1L)
                .name("archivedMappedDoc").description("doc desc")
                .index(-1).processInstanceId(PROCESS_INSTANCE_ID).version("1.7")
                .document(docContent)
                .documentId(docContent.getId())
                .archiveDate(System.currentTimeMillis())
                .build());

        // when:
        SAMappedDocument archivedMappedDocument = repository.getSAMappedDocumentOfProcessWithName("archivedMappedDoc",
                PROCESS_INSTANCE_ID);

        // then:
        assertThat(archivedMappedDocument.getName()).isEqualTo("archivedMappedDoc");
        assertThat(archivedMappedDocument.getDescription()).isEqualTo("doc desc");
        assertThat(archivedMappedDocument.getIndex()).isEqualTo(-1);
        assertThat(archivedMappedDocument.getProcessInstanceId()).isEqualTo(PROCESS_INSTANCE_ID);
        assertThat(archivedMappedDocument.getVersion()).isEqualTo("1.7");
        assertThat(archivedMappedDocument.getDocument()).isEqualTo(docContent);
    }
}
