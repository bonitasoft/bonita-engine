/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
 */
package org.bonitasoft.engine.bpm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.document.api.DocumentService;
import org.bonitasoft.engine.core.process.document.mapping.model.SDocumentMapping;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilder;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilderFactory;
import org.bonitasoft.engine.document.SDocumentNotFoundException;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class DocumentContentServiceTest extends CommonBPMServicesTest {

    private static final int NUMBER_OF_PROCESS_DOCUMENT = 10;

    private static BPMServicesBuilder bpmServicesBuilder;

    private static DocumentService documentService;

    private static TransactionService transactionService;

    private static String documentNameKey;

    static {
        bpmServicesBuilder = new BPMServicesBuilder();
        documentService = bpmServicesBuilder.getDocumentService();
        transactionService = bpmServicesBuilder.getTransactionService();
        documentNameKey = BuilderFactory.get(SDocumentMappingBuilderFactory.class).getDocumentNameKey();
    }

    @Cover(classes = { DocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Attach", "ProcessInstance", "Document", "Content" }, jira = "")
    @Test
    public void attachDocumentToProcessInstanceTestWithContent() throws SBonitaException {
        final byte[] documentContent = "this is the content of the document".getBytes();

        transactionService.begin();
        final SDocumentMapping document = buildProcessDocumentWithContent(1);
        final SDocumentMapping result = documentService.attachDocumentToProcessInstance(document, documentContent);
        transactionService.complete();

        assertEquals(document.getDocumentAuthor(), result.getDocumentAuthor());
        assertEquals(document.getDocumentContentFileName(), result.getDocumentContentFileName());
        assertEquals(document.getDocumentContentMimeType(), result.getDocumentContentMimeType());
        assertEquals(document.getDocumentCreationDate(), result.getDocumentCreationDate());
        assertEquals(document.getDiscriminator(), result.getDiscriminator());
        assertEquals(document.getDocumentName(), result.getDocumentName());
        assertEquals(document.getProcessInstanceId(), result.getProcessInstanceId());

        // Clean up
        removeDocument(result);
    }

    @Cover(classes = { DocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Attach", "ProcessInstance", "Document" }, jira = "")
    @Test
    public void attachDocumentToProcessInstanceTest() throws SBonitaException {
        transactionService.begin();
        final SDocumentMapping document = buildProcessDocument(1);
        final SDocumentMapping result = documentService.attachDocumentToProcessInstance(document);
        transactionService.complete();


        assertEquals(document.getDocumentAuthor(), result.getDocumentAuthor());
        assertEquals(document.getDocumentContentFileName(), result.getDocumentContentFileName());
        assertEquals(document.getDocumentContentMimeType(), result.getDocumentContentMimeType());
        assertEquals(document.getDocumentCreationDate(), result.getDocumentCreationDate());
        assertEquals(document.getDiscriminator(), result.getDiscriminator());
        assertEquals(document.getDocumentName(), result.getDocumentName());
        assertEquals(document.getProcessInstanceId(), result.getProcessInstanceId());
        assertEquals(document.getProcessInstanceId(), result.getProcessInstanceId());
        assertEquals(document.getDocumentURL(), result.getDocumentURL());

        // Clean up
        removeDocument(result);
    }

    @Cover(classes = { DocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "Contents", "Document" }, jira = "")
    @Test
    public void getDocumentContentTest() throws SBonitaException {
        final byte[] documentContent = "this is the content of the document".getBytes();

        final SDocumentMapping sProcessDocument = createAndAttachDocumentToProcessInstanceWithContent(documentContent);
        final byte[] storedContents = getDocumentContent(sProcessDocument);
        assertEquals(documentContent.length, storedContents.length);

        // Clean up
        removeDocument(sProcessDocument);
    }

    @Cover(classes = { DocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "Document", "Id" }, jira = "")
    @Test
    public void getDocumentByIdTest() throws SBonitaException {
        final byte[] documentContent = "this is the content of the document".getBytes();

        final SDocumentMapping sProcessDocument = createAndAttachDocumentToProcessInstanceWithContent(documentContent);
        final SDocumentMapping result = getDocument(sProcessDocument);
        assertEquals(sProcessDocument.getId(), result.getId());

        // Clean up
        removeDocument(sProcessDocument);
    }

    @Cover(classes = { DocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "Document", "ProcessId", "Name" }, jira = "")
    @Test
    public void getDocumentByNameAndProcessIdTest() throws SBonitaException {
        final byte[] documentContent = "this is the content of the document".getBytes();

        final SDocumentMapping sProcessDocument = createAndAttachDocumentToProcessInstanceWithContent(documentContent);

        transactionService.begin();
        final SDocumentMapping result = documentService.getDocument(sProcessDocument.getProcessInstanceId(), sProcessDocument.getDocumentName());
        transactionService.complete();

        assertEquals(sProcessDocument.getDocumentName(), result.getDocumentName());
        assertEquals(sProcessDocument.getProcessInstanceId(), result.getProcessInstanceId());

        // Clean up
        removeDocument(sProcessDocument);
    }

    @Cover(classes = { DocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "Document", "Number", "ProcessInstance" }, jira = "")
    @Test
    public void getNumberOfDocumentsOfProcessInstanceTest() throws SBonitaException {
        final List<SDocumentMapping> list = createAndAttachDocumentToProcessInstances();

        transactionService.begin();
        final long result = documentService.getNumberOfDocumentsOfProcessInstance(list.get(0).getProcessInstanceId());
        transactionService.complete();

        assertEquals(NUMBER_OF_PROCESS_DOCUMENT, result);

        // Clean up
        removeDocuments(list);
    }

    @Cover(classes = { DocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "Document", "Number", "ProcessInstance" }, jira = "")
    @Test
    public void getDocumentsOfProcessInstanceTest() throws SBonitaException {
        final List<SDocumentMapping> list = createAndAttachDocumentToProcessInstances();

        transactionService.begin();
        final List<SDocumentMapping> result = documentService.getDocumentsOfProcessInstance(list.get(0).getProcessInstanceId(), 0,
                NUMBER_OF_PROCESS_DOCUMENT + 1, documentNameKey, OrderByType.ASC);
        transactionService.complete();

        assertEquals(NUMBER_OF_PROCESS_DOCUMENT, result.size());
        assertEquals(list.get(0).getId(), result.get(0).getId());
        assertEquals(list.get(0).getDocumentName(), result.get(0).getDocumentName());
        assertEquals(list.get(1).getId(), result.get(1).getId());
        assertEquals(list.get(1).getDocumentName(), result.get(1).getDocumentName());
        assertEquals(list.get(2).getId(), result.get(2).getId());
        assertEquals(list.get(2).getDocumentName(), result.get(2).getDocumentName());
        assertEquals(list.get(3).getId(), result.get(3).getId());
        assertEquals(list.get(3).getDocumentName(), result.get(3).getDocumentName());
        assertEquals(list.get(4).getId(), result.get(4).getId());
        assertEquals(list.get(4).getDocumentName(), result.get(4).getDocumentName());
        assertEquals(list.get(5).getId(), result.get(5).getId());
        assertEquals(list.get(5).getDocumentName(), result.get(5).getDocumentName());
        assertEquals(list.get(6).getId(), result.get(6).getId());
        assertEquals(list.get(6).getDocumentName(), result.get(6).getDocumentName());
        assertEquals(list.get(7).getId(), result.get(7).getId());
        assertEquals(list.get(7).getDocumentName(), result.get(7).getDocumentName());
        assertEquals(list.get(8).getId(), result.get(8).getId());
        assertEquals(list.get(8).getDocumentName(), result.get(8).getDocumentName());
        assertEquals(list.get(9).getId(), result.get(9).getId());
        assertEquals(list.get(9).getDocumentName(), result.get(9).getDocumentName());

        // Clean up
        removeDocuments(list);
    }

    @Cover(classes = { DocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Remove", "Document" }, jira = "ENGINE-594")
    @Test
    public void removeDocumentsTest() throws SBonitaException {
        final byte[] documentContent = "this is the content of the document".getBytes();

        final List<SDocumentMapping> list = createAndAttachDocumentToProcessInstancesWithContent(documentContent);
        removeDocuments(list);

        for (final SDocumentMapping sProcessDocument : list) {
            try {
                transactionService.begin();
                final SDocumentMapping result = documentService.getDocument(sProcessDocument.getId());

                assertNull(result);
            } catch (final SDocumentNotFoundException e) {
                transactionService.setRollbackOnly();
            } finally {
                transactionService.complete();
            }
        }
    }

    private SDocumentMapping buildProcessDocumentWithContent(final int i) {
        final SDocumentMappingBuilder builder = BuilderFactory.get(SDocumentMappingBuilderFactory.class).createNewInstance();
        builder.setDocumentAuthor(i);
        builder.setDocumentCreationDate(System.currentTimeMillis());
        builder.setDocumentContentFileName("getContentTest.txt");
        builder.setHasContent(true);
        builder.setDocumentName("documentName" + i);
        builder.setDocumentContentMimeType("text/plain");
        return builder.done();
    }

    private SDocumentMapping buildProcessDocument(final int i) {
        final SDocumentMappingBuilder builder = BuilderFactory.get(SDocumentMappingBuilderFactory.class).createNewInstance();
        builder.setDocumentAuthor(i);
        builder.setDocumentCreationDate(System.currentTimeMillis());
        builder.setDocumentContentFileName("getContentTest.txt");
        builder.setHasContent(false);
        builder.setDocumentName("documentName" + i);
        builder.setDocumentContentMimeType("text/plain");
        return builder.done();
    }

    private SDocumentMapping createAndAttachDocumentToProcessInstanceWithContent(final byte[] documentContent) throws SBonitaException {
        transactionService.begin();
        final SDocumentMapping document = buildProcessDocumentWithContent(1);
        final SDocumentMapping doc = documentService.attachDocumentToProcessInstance(document, documentContent);
        transactionService.complete();
        return doc;
    }

    private List<SDocumentMapping> createAndAttachDocumentToProcessInstancesWithContent(final byte[] documentContent) throws SBonitaException {
        transactionService.begin();
        final List<SDocumentMapping> list = new ArrayList<SDocumentMapping>(10);
        for (int i = 0; i < NUMBER_OF_PROCESS_DOCUMENT; i++) {
            final SDocumentMapping document = buildProcessDocumentWithContent(i);
            final SDocumentMapping doc = documentService.attachDocumentToProcessInstance(document, documentContent);
            list.add(doc);
        }
        transactionService.complete();
        return list;
    }

    private List<SDocumentMapping> createAndAttachDocumentToProcessInstances() throws SBonitaException {
        transactionService.begin();
        final List<SDocumentMapping> list = new ArrayList<SDocumentMapping>(10);
        for (int i = 0; i < NUMBER_OF_PROCESS_DOCUMENT; i++) {
            final SDocumentMapping document = buildProcessDocument(i);
            final SDocumentMapping doc = documentService.attachDocumentToProcessInstance(document);
            list.add(doc);
        }
        transactionService.complete();
        return list;
    }

    private byte[] getDocumentContent(final SDocumentMapping sProcessDocument) throws SBonitaException {
        transactionService.begin();
        final byte[] storedContents = documentService.getDocumentContent(sProcessDocument.getContentStorageId());
        transactionService.complete();
        return storedContents;
    }

    private SDocumentMapping getDocument(final SDocumentMapping sProcessDocument) throws SBonitaException {
        transactionService.begin();
        final SDocumentMapping result = documentService.getDocument(sProcessDocument.getId());
        transactionService.complete();
        return result;
    }

    private void removeDocuments(final List<SDocumentMapping> sProcessDocuments) throws SBonitaException {
        transactionService.begin();
        documentService.removeDocuments(sProcessDocuments);
        transactionService.complete();
    }

    private void removeDocument(final SDocumentMapping sProcessDocument) throws SBonitaException {
        transactionService.begin();
        documentService.removeDocument(sProcessDocument);
        transactionService.complete();
    }
}
