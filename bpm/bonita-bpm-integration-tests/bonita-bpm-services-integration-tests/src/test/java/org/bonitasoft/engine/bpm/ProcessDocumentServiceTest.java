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
import org.bonitasoft.engine.core.process.document.api.ProcessDocumentService;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilderFactory;
import org.bonitasoft.engine.core.process.document.model.SProcessDocument;
import org.bonitasoft.engine.core.process.document.model.builder.SProcessDocumentBuilder;
import org.bonitasoft.engine.core.process.document.model.builder.SProcessDocumentBuilderFactory;
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
public class ProcessDocumentServiceTest extends CommonBPMServicesTest {

    private static final int NUMBER_OF_PROCESS_DOCUMENT = 10;

    private static BPMServicesBuilder bpmServicesBuilder;

    private static ProcessDocumentService processDocumentService;

    private static TransactionService transactionService;

    private static String documentNameKey;

    static {
        bpmServicesBuilder = new BPMServicesBuilder();
        processDocumentService = bpmServicesBuilder.getProcessDocumentService();
        transactionService = bpmServicesBuilder.getTransactionService();
        documentNameKey = BuilderFactory.get(SDocumentMappingBuilderFactory.class).getDocumentNameKey();
    }

    @Cover(classes = { ProcessDocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Attach", "ProcessInstance", "Document", "Content" }, jira = "")
    @Test
    public void attachDocumentToProcessInstanceTestWithContent() throws SBonitaException {
        final byte[] documentContent = "this is the content of the document".getBytes();

        transactionService.begin();
        final SProcessDocument document = buildProcessDocumentWithContent(1);
        final SProcessDocument result = processDocumentService.attachDocumentToProcessInstance(document, documentContent);
        transactionService.complete();

        assertEquals(document.getAuthor(), result.getAuthor());
        assertEquals(document.getContentFileName(), result.getContentFileName());
        assertEquals(document.getContentMimeType(), result.getContentMimeType());
        assertEquals(document.getContentSize(), result.getContentSize());
        assertEquals(document.getCreationDate(), result.getCreationDate());
        assertEquals(document.getDiscriminator(), result.getDiscriminator());
        assertEquals(document.getName(), result.getName());
        assertEquals(document.getProcessInstanceId(), result.getProcessInstanceId());

        // Clean up
        removeDocument(result);
    }

    @Cover(classes = { ProcessDocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Attach", "ProcessInstance", "Document" }, jira = "")
    @Test
    public void attachDocumentToProcessInstanceTest() throws SBonitaException {
        transactionService.begin();
        final SProcessDocument document = buildProcessDocument(1);
        final SProcessDocument result = processDocumentService.attachDocumentToProcessInstance(document);
        transactionService.complete();

        assertEquals(document.getAuthor(), result.getAuthor());
        assertEquals(document.getContentFileName(), result.getContentFileName());
        assertEquals(document.getContentMimeType(), result.getContentMimeType());
        assertEquals(document.getContentSize(), result.getContentSize());
        assertEquals(document.getContentStorageId(), result.getContentStorageId());
        assertEquals(document.getCreationDate(), result.getCreationDate());
        assertEquals(document.getDiscriminator(), result.getDiscriminator());
        assertEquals(document.getName(), result.getName());
        assertEquals(document.getProcessInstanceId(), result.getProcessInstanceId());
        assertEquals(document.getURL(), result.getURL());

        // Clean up
        removeDocument(result);
    }

    @Cover(classes = { ProcessDocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "Contents", "Document" }, jira = "")
    @Test
    public void getDocumentContentTest() throws SBonitaException {
        final byte[] documentContent = "this is the content of the document".getBytes();

        final SProcessDocument sProcessDocument = createAndAttachDocumentToProcessInstanceWithContent(documentContent);
        final byte[] storedContents = getDocumentContent(sProcessDocument);
        assertEquals(documentContent.length, storedContents.length);

        // Clean up
        removeDocument(sProcessDocument);
    }

    @Cover(classes = { ProcessDocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "Document", "Id" }, jira = "")
    @Test
    public void getDocumentByIdTest() throws SBonitaException {
        final byte[] documentContent = "this is the content of the document".getBytes();

        final SProcessDocument sProcessDocument = createAndAttachDocumentToProcessInstanceWithContent(documentContent);
        final SProcessDocument result = getDocument(sProcessDocument);
        assertEquals(sProcessDocument.getId(), result.getId());

        // Clean up
        removeDocument(sProcessDocument);
    }

    @Cover(classes = { ProcessDocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "Document", "ProcessId", "Name" }, jira = "")
    @Test
    public void getDocumentByNameAndProcessIdTest() throws SBonitaException {
        final byte[] documentContent = "this is the content of the document".getBytes();

        final SProcessDocument sProcessDocument = createAndAttachDocumentToProcessInstanceWithContent(documentContent);

        transactionService.begin();
        final SProcessDocument result = processDocumentService.getDocument(sProcessDocument.getProcessInstanceId(), sProcessDocument.getName());
        transactionService.complete();

        assertEquals(sProcessDocument.getName(), result.getName());
        assertEquals(sProcessDocument.getProcessInstanceId(), result.getProcessInstanceId());

        // Clean up
        removeDocument(sProcessDocument);
    }

    @Cover(classes = { ProcessDocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "Document", "Number", "ProcessInstance" }, jira = "")
    @Test
    public void getNumberOfDocumentsOfProcessInstanceTest() throws SBonitaException {
        final List<SProcessDocument> list = createAndAttachDocumentToProcessInstances();

        transactionService.begin();
        final long result = processDocumentService.getNumberOfDocumentsOfProcessInstance(list.get(0).getProcessInstanceId());
        transactionService.complete();

        assertEquals(NUMBER_OF_PROCESS_DOCUMENT, result);

        // Clean up
        removeDocuments(list);
    }

    @Cover(classes = { ProcessDocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "Document", "Number", "ProcessInstance" }, jira = "")
    @Test
    public void getDocumentsOfProcessInstanceTest() throws SBonitaException {
        final List<SProcessDocument> list = createAndAttachDocumentToProcessInstances();

        transactionService.begin();
        final List<SProcessDocument> result = processDocumentService.getDocumentsOfProcessInstance(list.get(0).getProcessInstanceId(), 0,
                NUMBER_OF_PROCESS_DOCUMENT + 1, documentNameKey, OrderByType.ASC);
        transactionService.complete();

        assertEquals(NUMBER_OF_PROCESS_DOCUMENT, result.size());
        assertEquals(list.get(0).getId(), result.get(0).getId());
        assertEquals(list.get(0).getName(), result.get(0).getName());
        assertEquals(list.get(1).getId(), result.get(1).getId());
        assertEquals(list.get(1).getName(), result.get(1).getName());
        assertEquals(list.get(2).getId(), result.get(2).getId());
        assertEquals(list.get(2).getName(), result.get(2).getName());
        assertEquals(list.get(3).getId(), result.get(3).getId());
        assertEquals(list.get(3).getName(), result.get(3).getName());
        assertEquals(list.get(4).getId(), result.get(4).getId());
        assertEquals(list.get(4).getName(), result.get(4).getName());
        assertEquals(list.get(5).getId(), result.get(5).getId());
        assertEquals(list.get(5).getName(), result.get(5).getName());
        assertEquals(list.get(6).getId(), result.get(6).getId());
        assertEquals(list.get(6).getName(), result.get(6).getName());
        assertEquals(list.get(7).getId(), result.get(7).getId());
        assertEquals(list.get(7).getName(), result.get(7).getName());
        assertEquals(list.get(8).getId(), result.get(8).getId());
        assertEquals(list.get(8).getName(), result.get(8).getName());
        assertEquals(list.get(9).getId(), result.get(9).getId());
        assertEquals(list.get(9).getName(), result.get(9).getName());

        // Clean up
        removeDocuments(list);
    }

    @Cover(classes = { ProcessDocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Remove", "Document" }, jira = "ENGINE-594")
    @Test
    public void removeDocumentsTest() throws SBonitaException {
        final byte[] documentContent = "this is the content of the document".getBytes();

        final List<SProcessDocument> list = createAndAttachDocumentToProcessInstancesWithContent(documentContent);
        removeDocuments(list);

        for (final SProcessDocument sProcessDocument : list) {
            try {
                transactionService.begin();
                final SProcessDocument result = processDocumentService.getDocument(sProcessDocument.getId());

                assertNull(result);
            } catch (final SDocumentNotFoundException e) {
                transactionService.setRollbackOnly();
            } finally {
                transactionService.complete();
            }
        }
    }

    private SProcessDocument buildProcessDocumentWithContent(final int i) {
        final SProcessDocumentBuilder builder = BuilderFactory.get(SProcessDocumentBuilderFactory.class).createNewInstance();
        builder.setAuthor(i);
        builder.setCreationDate(System.currentTimeMillis());
        builder.setFileName("getContentTest.txt");
        builder.setHasContent(true);
        builder.setName("documentName" + i);
        builder.setContentMimeType("text/plain");
        return builder.done();
    }

    private SProcessDocument buildProcessDocument(final int i) {
        final SProcessDocumentBuilder builder = BuilderFactory.get(SProcessDocumentBuilderFactory.class).createNewInstance();
        builder.setAuthor(i);
        builder.setCreationDate(System.currentTimeMillis());
        builder.setFileName("getContentTest.txt");
        builder.setHasContent(false);
        builder.setName("documentName" + i);
        builder.setContentMimeType("text/plain");
        return builder.done();
    }

    private SProcessDocument createAndAttachDocumentToProcessInstanceWithContent(final byte[] documentContent) throws SBonitaException {
        transactionService.begin();
        final SProcessDocument document = buildProcessDocumentWithContent(1);
        final SProcessDocument doc = processDocumentService.attachDocumentToProcessInstance(document, documentContent);
        transactionService.complete();
        return doc;
    }

    private List<SProcessDocument> createAndAttachDocumentToProcessInstancesWithContent(final byte[] documentContent) throws SBonitaException {
        transactionService.begin();
        final List<SProcessDocument> list = new ArrayList<SProcessDocument>(10);
        for (int i = 0; i < NUMBER_OF_PROCESS_DOCUMENT; i++) {
            final SProcessDocument document = buildProcessDocumentWithContent(i);
            final SProcessDocument doc = processDocumentService.attachDocumentToProcessInstance(document, documentContent);
            list.add(doc);
        }
        transactionService.complete();
        return list;
    }

    private List<SProcessDocument> createAndAttachDocumentToProcessInstances() throws SBonitaException {
        transactionService.begin();
        final List<SProcessDocument> list = new ArrayList<SProcessDocument>(10);
        for (int i = 0; i < NUMBER_OF_PROCESS_DOCUMENT; i++) {
            final SProcessDocument document = buildProcessDocument(i);
            final SProcessDocument doc = processDocumentService.attachDocumentToProcessInstance(document);
            list.add(doc);
        }
        transactionService.complete();
        return list;
    }

    private byte[] getDocumentContent(final SProcessDocument sProcessDocument) throws SBonitaException {
        transactionService.begin();
        final byte[] storedContents = processDocumentService.getDocumentContent(sProcessDocument.getContentStorageId());
        transactionService.complete();
        return storedContents;
    }

    private SProcessDocument getDocument(final SProcessDocument sProcessDocument) throws SBonitaException {
        transactionService.begin();
        final SProcessDocument result = processDocumentService.getDocument(sProcessDocument.getId());
        transactionService.complete();
        return result;
    }

    private void removeDocuments(final List<SProcessDocument> sProcessDocuments) throws SBonitaException {
        transactionService.begin();
        processDocumentService.removeDocuments(sProcessDocuments);
        transactionService.complete();
    }

    private void removeDocument(final SProcessDocument sProcessDocument) throws SBonitaException {
        transactionService.begin();
        processDocumentService.removeDocument(sProcessDocument);
        transactionService.complete();
    }
}
