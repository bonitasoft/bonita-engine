/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
 **/
package org.bonitasoft.engine.bpm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilder;
import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilderFactory;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Nicolas Chabanoles
 * @author Celine Souchet
 */
public class DocumentServiceTest extends CommonBPMServicesTest {

    private static final int NUMBER_OF_DOCUMENT = 10;

    private static DocumentService documentService;

    private static TransactionService transactionService;

    private static long processInstanceId = 123l;

    private static String documentNameKey;

    @Before
    public void before(){
        documentService = getTenantAccessor().getDocumentService();
        transactionService = getTransactionService();
        documentNameKey = BuilderFactory.get(SDocumentBuilderFactory.class).getNameKey();
    }

    @Cover(classes = { DocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Create", "DocumentMapping" }, jira = "")
    @Test
    public void attachDocumentToProcessInstanceWithContentTest() throws SBonitaException {
        transactionService.begin();
        final SDocument document = buildProcessDocumentWithContent(1, "theContent".getBytes());
        final SMappedDocument result = documentService.attachDocumentToProcessInstance(document, processInstanceId, "documentName", "the description");
        transactionService.complete();

        assertEquals(document.getAuthor(), result.getAuthor());
        assertEquals(document.getFileName(), result.getFileName());
        assertEquals(document.getMimeType(), result.getMimeType());
        assertEquals(document.getCreationDate(), result.getCreationDate());
        assertEquals(document.getUrl(), result.getUrl());
        assertEquals(document.getId(), result.getDocumentId());
        assertEquals("documentName", result.getName());
        assertEquals("the description", result.getDescription());
        assertEquals(processInstanceId, result.getProcessInstanceId());

        // Clean up
        delete(result);
    }

    @Cover(classes = { DocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Attach", "ProcessInstance", "Document" }, jira = "")
    @Test
    public void attachDocumentToProcessInstanceWithUrlTest() throws SBonitaException {
        transactionService.begin();
        final SDocument document = buildProcessDocumentWithUrl(1);
        final SMappedDocument result = documentService.attachDocumentToProcessInstance(document, processInstanceId, "documentName", "the description");
        transactionService.complete();

        assertEquals(document.getAuthor(), result.getAuthor());
        assertEquals(document.getFileName(), result.getFileName());
        assertEquals(document.getMimeType(), result.getMimeType());
        assertEquals(document.getCreationDate(), result.getCreationDate());
        assertEquals("documentName", result.getName());
        assertEquals(processInstanceId, result.getProcessInstanceId());
        assertEquals(document.getUrl(), result.getUrl());
        assertEquals(-1, result.getIndex());

        // Clean up
        delete(result);
    }

    @Cover(classes = { DocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Attach", "ProcessInstance", "Document" }, jira = "")
    @Test
    public void attachDocumentToProcessInList() throws SBonitaException {
        transactionService.begin();
        final SDocument document0 = buildProcessDocumentWithUrl(1);
        final SDocument document1 = buildProcessDocumentWithUrl(1);
        final SMappedDocument result0 = documentService.attachDocumentToProcessInstance(document0, processInstanceId, "documentName", "the description", 0);
        final SMappedDocument result1 = documentService.attachDocumentToProcessInstance(document1, processInstanceId, "documentName", "the description", 1);
        transactionService.complete();

        assertEquals(document0.getAuthor(), result0.getAuthor());
        assertEquals(document0.getFileName(), result0.getFileName());
        assertEquals(document0.getMimeType(), result0.getMimeType());
        assertEquals(document0.getCreationDate(), result0.getCreationDate());
        assertEquals("documentName", result0.getName());
        assertEquals(processInstanceId, result0.getProcessInstanceId());
        assertEquals(document0.getUrl(), result0.getUrl());
        assertEquals(0, result0.getIndex());
        assertEquals(1, result1.getIndex());

        // Clean up
        delete(result0);
        delete(result1);
    }

    @Cover(classes = { DocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "DocumentMapping", "Id" }, jira = "")
    @Test
    public void getDocumentByIdTest() throws SBonitaException {
        final SMappedDocument sDocument = createDocumentMapping();
        final SMappedDocument result = getDocumentMapping(sDocument);
        assertEquals(sDocument.getId(), result.getId());

        // Clean up
        delete(result);
    }

    @Cover(classes = { DocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "DocumentMapping", "ProcessId", "Name" }, jira = "")
    @Test
    public void getDocumentByNameAndProcessIdTest() throws SBonitaException {
        final SMappedDocument sDocument = createDocumentMapping();

        transactionService.begin();
        final SMappedDocument result = documentService.getMappedDocument(sDocument.getProcessInstanceId(), sDocument.getName());
        transactionService.complete();

        assertEquals(sDocument.getId(), result.getId());
        assertEquals(sDocument.getName(), result.getName());
        assertEquals(sDocument.getProcessInstanceId(), result.getProcessInstanceId());

        // Clean up
        delete(result);
    }

    @Cover(classes = { DocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "DocumentMapping", "Number", "ProcessInstance" }, jira = "")
    @Test
    public void getNumberOfDocumentMappingsForProcessInstanceTest() throws SBonitaException {
        final List<SMappedDocument> list = createDocumentMappings();

        transactionService.begin();
        final long result = documentService.getNumberOfDocumentsOfProcessInstance(processInstanceId);
        transactionService.complete();

        assertEquals(NUMBER_OF_DOCUMENT, result);

        // Clean up
        delete(list);
    }

    @Cover(classes = { DocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "DocumentMapping", "Number", "ProcessInstance" }, jira = "")
    @Test
    public void getDocumentMappingsForProcessInstanceTest() throws SBonitaException {
        final List<SMappedDocument> list = createDocumentMappings();

        transactionService.begin();
        final List<SMappedDocument> result = documentService.getDocumentsOfProcessInstance(list.get(0).getProcessInstanceId(), 0,
                NUMBER_OF_DOCUMENT + 1, "name", OrderByType.ASC);
        transactionService.complete();

        assertEquals(NUMBER_OF_DOCUMENT, result.size());
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
        delete(list);
    }

    private SDocument buildProcessDocumentWithContent(final int i, final byte[] documentContent) {
        final SDocumentBuilder builder = BuilderFactory.get(SDocumentBuilderFactory.class).createNewInstance("getContentTest.txt", "text/plain", i);
        builder.setHasContent(true);
        builder.setContent(documentContent);
        return builder.done();
    }

    private SDocument buildProcessDocumentWithUrl(final int i) {
        final SDocumentBuilder builder = BuilderFactory.get(SDocumentBuilderFactory.class).createNewInstance("getContentTest.txt", "text/plain", i);
        builder.setHasContent(false);
        builder.setURL("theUrl");
        return builder.done();
    }

    private SMappedDocument createDocumentMapping() throws SBonitaException {
        transactionService.begin();
        final SDocument document = buildProcessDocumentWithContent(1, "content".getBytes());
        final SMappedDocument doc = documentService.attachDocumentToProcessInstance(document, processInstanceId, "documentName", "the description");
        transactionService.complete();
        return doc;
    }

    private List<SMappedDocument> createDocumentMappings() throws SBonitaException {
        transactionService.begin();
        final List<SMappedDocument> list = new ArrayList<SMappedDocument>(10);
        for (int i = 0; i < NUMBER_OF_DOCUMENT; i++) {
            final SDocument document = buildProcessDocumentWithContent(i, "content".getBytes());
            list.add(documentService.attachDocumentToProcessInstance(document, processInstanceId, "documentName", "the description"));
        }
        transactionService.complete();
        return list;
    }

    private SMappedDocument getDocumentMapping(final SMappedDocument sDocument) throws SBonitaException {
        transactionService.begin();
        final SMappedDocument result = documentService.getMappedDocument(sDocument.getId());
        transactionService.complete();
        return result;
    }

    private void delete(final List<SMappedDocument> sDocuments) throws SBonitaException {
        transactionService.begin();
        for (final SMappedDocument sDocument : sDocuments) {
            documentService.removeDocument(sDocument);
        }
        transactionService.complete();
    }

    private void delete(final SMappedDocument mappedDocument) throws SBonitaException {
        transactionService.begin();
        documentService.removeDocument(mappedDocument);
        documentService.deleteDocument(documentService.getDocument(mappedDocument.getDocumentId()));
        transactionService.complete();
    }

    @Cover(classes = { DocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "Contents", "Document" }, jira = "")
    @Test
    public void getDocumentContentTest() throws SBonitaException {
        final byte[] documentContent = "this is the content of the document".getBytes();

        final SMappedDocument sProcessDocument = createAndAttachDocumentToProcessInstanceWithContent(documentContent);
        final byte[] storedContents = getDocumentContent(sProcessDocument);
        assertEquals(documentContent.length, storedContents.length);

        // Clean up
        delete(sProcessDocument);
    }

    @Cover(classes = { DocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "Document", "Number", "ProcessInstance" }, jira = "")
    @Test
    public void getNumberOfDocumentsOfProcessInstanceTest() throws SBonitaException {
        final List<SMappedDocument> list = createAndAttachDocumentToProcessInstances();

        transactionService.begin();
        final long result = documentService.getNumberOfDocumentsOfProcessInstance(list.get(0).getProcessInstanceId());
        transactionService.complete();

        assertEquals(NUMBER_OF_DOCUMENT, result);

        // Clean up
        delete(list);
    }

    @Cover(classes = { DocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "Document", "Number", "ProcessInstance" }, jira = "")
    @Test
    public void getDocumentsOfProcessInstanceTest() throws SBonitaException {
        final List<SMappedDocument> list = createAndAttachDocumentToProcessInstances();

        transactionService.begin();
        final List<SMappedDocument> result = documentService.getDocumentsOfProcessInstance(list.get(0).getProcessInstanceId(), 0,
                NUMBER_OF_DOCUMENT + 1, documentNameKey, OrderByType.ASC);
        transactionService.complete();

        assertEquals(NUMBER_OF_DOCUMENT, result.size());
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
        delete(list);
    }

    @Cover(classes = { DocumentService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Remove", "Document" }, jira = "ENGINE-594")
    @Test
    public void removeDocumentsTest() throws SBonitaException {
        final byte[] documentContent = "this is the content of the document".getBytes();

        final List<SMappedDocument> list = createAndAttachDocumentToProcessInstancesWithContent(documentContent);
        delete(list);

        for (final SMappedDocument sProcessDocument : list) {
            try {
                transactionService.begin();
                final SMappedDocument result = documentService.getMappedDocument(sProcessDocument.getId());

                assertNull(result);
            } catch (final SObjectNotFoundException e) {
                transactionService.setRollbackOnly();
            } finally {
                transactionService.complete();
            }
        }
    }

    private SMappedDocument createAndAttachDocumentToProcessInstanceWithContent(final byte[] documentContent) throws SBonitaException {
        transactionService.begin();
        final SDocument document = buildProcessDocumentWithContent(1, documentContent);
        final SMappedDocument doc = documentService.attachDocumentToProcessInstance(document, processInstanceId, "documentName", "the description");
        transactionService.complete();
        return doc;
    }

    private List<SMappedDocument> createAndAttachDocumentToProcessInstancesWithContent(final byte[] documentContent) throws SBonitaException {
        transactionService.begin();
        final List<SMappedDocument> list = new ArrayList<SMappedDocument>(10);
        for (int i = 0; i < NUMBER_OF_DOCUMENT; i++) {
            final SDocument document = buildProcessDocumentWithContent(i, documentContent);
            final SMappedDocument doc = documentService.attachDocumentToProcessInstance(document, processInstanceId, "documentName", "the description");
            list.add(doc);
        }
        transactionService.complete();
        return list;
    }

    private List<SMappedDocument> createAndAttachDocumentToProcessInstances() throws SBonitaException {
        transactionService.begin();
        final List<SMappedDocument> list = new ArrayList<SMappedDocument>(10);
        for (int i = 0; i < NUMBER_OF_DOCUMENT; i++) {
            final SDocument document = buildProcessDocumentWithUrl(i);
            final SMappedDocument doc = documentService.attachDocumentToProcessInstance(document, processInstanceId, "documentName", "the description");
            list.add(doc);
        }
        transactionService.complete();
        return list;
    }

    private byte[] getDocumentContent(final SMappedDocument sProcessDocument) throws SBonitaException {
        transactionService.begin();
        final byte[] storedContents = documentService.getDocumentContent(String.valueOf(sProcessDocument.getDocumentId()));
        transactionService.complete();
        return storedContents;
    }

}
