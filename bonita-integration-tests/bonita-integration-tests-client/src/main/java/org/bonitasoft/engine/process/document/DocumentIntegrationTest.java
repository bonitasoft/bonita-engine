/**
 * Copyright (C) 2012, 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.process.document;

import static org.bonitasoft.engine.matchers.ListElementMatcher.nameAre;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.document.ArchivedDocument;
import org.bonitasoft.engine.bpm.document.ArchivedDocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.ArchivedDocumentsSearchDescriptor;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentCriterion;
import org.bonitasoft.engine.bpm.document.DocumentException;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.bpm.document.DocumentsSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.CallActivityInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.impl.AutomaticTaskDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.CallActivityBuilder;
import org.bonitasoft.engine.bpm.process.impl.DocumentBuilder;
import org.bonitasoft.engine.bpm.process.impl.DocumentDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Nicolas Chabanoles
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class DocumentIntegrationTest extends CommonAPITest {

    private User user;

    private int processVersion = 0;

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        user = createUser(USERNAME, PASSWORD);
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
    }

    @After
    public void afterTest() throws BonitaException {
        deleteUser(user);
        logoutOnTenant();
    }

    @Test
    public void attachADocumentToProcessInstanceTest() throws BonitaException {
        final ProcessInstance pi = deployAndEnableWithActorAndStartIt(user);
        Document attachment;
        try {
            final String documentName = "newDocument";
            final Document doc = buildReferenceToExternalDocument();
            attachment = getProcessAPI().attachDocument(pi.getId(), documentName, doc.getContentFileName(), doc.getContentMimeType(), doc.getUrl());
            final Document attachedDoc = getProcessAPI().getDocument(attachment.getId());
            assertIsSameDocument(attachment, attachedDoc);
            assertFalse(attachedDoc.hasContent());
        } finally {
            disableAndDeleteProcess(pi.getProcessDefinitionId());
        }
    }

    @Test
    public void attachADocumentAndItsContentToProcessInstanceTest() throws BonitaException {
        final ProcessInstance pi = deployAndEnableWithActorAndStartIt(user);
        Document attachment;
        try {
            final String documentName = "newDocument";
            final Document doc = BuildTestUtil.buildDocument(documentName);
            final byte[] documentContent = BuildTestUtil.generateContent(doc);
            attachment = getProcessAPI().attachDocument(pi.getId(), documentName, doc.getContentFileName(), doc.getContentMimeType(), documentContent);
            final Document attachedDoc = getProcessAPI().getDocument(attachment.getId());
            assertIsSameDocument(attachment, attachedDoc);
            final byte[] attachedContent = getProcessAPI().getDocumentContent(attachedDoc.getContentStorageId());
            assertTrue(Arrays.equals(documentContent, attachedContent));
            assertTrue(attachedDoc.hasContent());
        } finally {
            disableAndDeleteProcess(pi.getProcessDefinitionId());
        }
    }

    private void assertIsSameDocument(final Document attachment, final Document attachedDoc) {
        assertEquals("IDs are not the same!", attachment.getId(), attachedDoc.getId());
        assertEquals("Process instances IDs are not the same!", attachment.getProcessInstanceId(), attachedDoc.getProcessInstanceId());
        assertEquals("Names are not the same!", attachment.getName(), attachedDoc.getName());
        assertEquals("Authors are not the same!", attachment.getAuthor(), attachedDoc.getAuthor());
        assertEquals("Creation dates are not the same!", attachment.getCreationDate(), attachedDoc.getCreationDate());
        assertEquals("Has content flags are not the same!", attachment.hasContent(), attachedDoc.hasContent());
        assertEquals("File names are not the same!", attachment.getContentFileName(), attachedDoc.getContentFileName());
        assertEquals("Mime types are not the same!", attachment.getContentMimeType(), attachedDoc.getContentMimeType());
        assertEquals("Content storage IDs are not the same!", attachment.getContentStorageId(), attachedDoc.getContentStorageId());
        assertEquals("URL are not the same!", attachment.getUrl(), attachedDoc.getUrl());
    }

    private void assertIsSameDocument(final Document attachedDoc, final long processInstanceId, final String name, final long author, final boolean hasContent,
            final String fileName, final String mimeType, final String url) {
        assertEquals("Process instances IDs are not the same!", processInstanceId, attachedDoc.getProcessInstanceId());
        assertEquals("Names are not the same!", name, attachedDoc.getName());
        assertEquals("Authors are not the same!", author, attachedDoc.getAuthor());
        assertEquals("Has content flags are not the same!", hasContent, attachedDoc.hasContent());
        assertEquals("File names are not the same!", fileName, attachedDoc.getContentFileName());
        assertEquals("Mime types are not the same!", mimeType, attachedDoc.getContentMimeType());
        assertEquals("URL are not the same!", url, attachedDoc.getUrl());
    }

    @Test
    public void createProcessWithUrlDocument() throws BonitaException {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("createProcessWithUrlDocument", "1.0");
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        final String docName = "myRtfDocument";
        final String url = "http://intranet.bonitasoft.com/private/docStorage/anyValue";
        final DocumentDefinitionBuilder documentDefinition = designProcessDefinition.addDocumentDefinition(docName);
        documentDefinition.addUrl(url);
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(designProcessDefinition.getProcess()).done();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchive, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        try {
            final SearchOptionsBuilder sob = new SearchOptionsBuilder(0, 10).filter(DocumentsSearchDescriptor.PROCESSINSTANCE_ID, processInstance.getId());
            final SearchResult<Document> docs = getProcessAPI().searchDocuments(sob.done());
            final Document actualDoc = docs.getResult().get(0);
            assertEquals(docName, actualDoc.getName());
            assertEquals(url, actualDoc.getUrl());
            assertTrue("Document Content filename should NOT be valuated", null == actualDoc.getContentFileName());
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Test
    public void attachAnExternalDocumentReferenceToProcessInstanceTest() throws BonitaException {
        final ProcessInstance pi = deployAndEnableWithActorAndStartIt(user);
        Document attachment;
        try {
            final Document doc = buildReferenceToExternalDocument();
            final String documentName = "newAttachment";
            attachment = getProcessAPI().attachDocument(pi.getId(), documentName, doc.getContentFileName(), doc.getContentMimeType(), doc.getUrl());
            final Document attachedDoc = getProcessAPI().getDocument(attachment.getId());
            assertIsSameDocument(attachment, attachedDoc);

        } finally {
            disableAndDeleteProcess(pi.getProcessDefinitionId());
        }
    }

    private Document buildReferenceToExternalDocument() {
        final String now = String.valueOf(System.currentTimeMillis());
        final String documentName = now + "-series";
        final DocumentBuilder builder = new DocumentBuilder().createNewInstance(documentName, true);
        builder.setURL("http://tinyurl.com/7n77prz");
        return builder.done();
    }

    @Test
    public void attachAnExternalDocumentReferenceToProcessInstanceAsNewVersionTest() throws BonitaException {
        final ProcessInstance processInstance = deployAndEnableWithActorAndStartIt(user);
        buildAndAttachDocument(processInstance);
        Document attachment;
        try {
            final String documentName = getAttachmentDocumentName(processInstance);
            final Document doc = buildReferenceToExternalDocument();
            attachment = getProcessAPI().attachNewDocumentVersion(processInstance.getId(), documentName, doc.getContentFileName(), doc.getContentMimeType(),
                    doc.getUrl());
            final Document attachedDoc = getProcessAPI().getDocument(attachment.getId());
            assertIsSameDocument(attachment, attachedDoc);
        } finally {
            disableAndDeleteProcess(processInstance.getProcessDefinitionId());
        }
    }

    @Test
    public void attachADocumentAndItsContentToProcessInstanceAsNewVersionTest() throws BonitaException {
        final ProcessInstance processInstance = deployAndEnableWithActorAndStartIt(user);
        buildAndAttachDocument(processInstance);

        try {
            final String documentName = getAttachmentDocumentName(processInstance);
            final Document doc = BuildTestUtil.buildDocument(documentName);
            final Document attachment = getProcessAPI().attachNewDocumentVersion(processInstance.getId(), documentName, doc.getContentFileName(),
                    doc.getContentMimeType(),
                    documentName.getBytes());
            final Document attachedDoc = getProcessAPI().getDocument(attachment.getId());
            assertIsSameDocument(attachment, attachedDoc);

        } finally {
            disableAndDeleteProcess(processInstance.getProcessDefinitionId());
        }
    }

    @Test
    public void getDocumentContentTest() throws Exception {
        final ProcessInstance processInstance = deployAndEnableWithActorAndStartIt(user);
        buildAndAttachDocument(processInstance);
        try {
            final Document attachment = getAttachmentWithoutItsContent(processInstance);
            final byte[] docContent = getProcessAPI().getDocumentContent(attachment.getContentStorageId());
            assertNotNull(docContent);
            assertTrue(docContent.length > 0);

        } finally {
            disableAndDeleteProcess(processInstance.getProcessDefinitionId());
        }
    }

    @Test
    public void getLastDocumentTest() throws Exception {
        final ProcessInstance processInstance = deployAndEnableWithActorAndStartIt(user);
        buildAndAttachDocument(processInstance);

        Document attachment;
        Document lastVersion;
        try {
            final String documentName = getAttachmentDocumentName(processInstance);
            attachment = getAttachmentWithoutItsContent(processInstance);
            lastVersion = getProcessAPI().getLastDocument(processInstance.getId(), documentName);
            assertIsSameDocument(attachment, lastVersion);

            final Document doc = buildReferenceToExternalDocument();
            final Document newVersion = getProcessAPI().attachNewDocumentVersion(processInstance.getId(), documentName, doc.getContentFileName(),
                    doc.getContentMimeType(),
                    doc.getUrl());
            lastVersion = getProcessAPI().getLastDocument(processInstance.getId(), documentName);
            assertIsSameDocument(newVersion, lastVersion);

        } finally {
            disableAndDeleteProcess(processInstance.getProcessDefinitionId());
        }
    }

    @Test
    public void getDocumentOnProcessWithDocumentInDefinitionUsingBarResource() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MyProcessWithDocumentsInBar", "1.0");
        builder.addUserTask("step1", ACTOR_NAME);
        builder.addActor(ACTOR_NAME);
        builder.addDocumentDefinition("myDoc").addContentFileName("myPdfModifiedName.pdf").addDescription("a cool pdf document").addMimeType("application/pdf")
                .addFile("myPdf.pdf");
        final byte[] pdfContent = new byte[] { 5, 0, 1, 4, 6, 5, 2, 3, 1, 5, 6, 8, 4, 6, 6, 3, 2, 4, 5 };
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(builder.getProcess())
                .addDocumentResource(new BarResource("myPdf.pdf", pdfContent)).done();
        final ProcessInstance processInstance = deployAndEnableProcessWithActorAndStartIt(businessArchive, user);
        try {
            final Document attachment = getAttachmentWithoutItsContent(processInstance);
            assertIsSameDocument(attachment, processInstance.getId(), "myDoc", user.getId(), true, "myPdfModifiedName.pdf", "application/pdf",
                    attachment.getUrl());
            final byte[] docContent = getProcessAPI().getDocumentContent(attachment.getContentStorageId());
            assertTrue(Arrays.equals(pdfContent, docContent));
        } finally {
            waitForUserTask("step1", processInstance);
            disableAndDeleteProcess(processInstance.getProcessDefinitionId());
        }
    }

    @Test
    public void getDocumentUsingExpression() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MyProcessWithDocumentsInBar", "1.0");
        final AutomaticTaskDefinitionBuilder automaticTask = builder.addAutomaticTask("setDataTask");
        automaticTask.addOperation(new LeftOperandBuilder().createNewInstance("myDocRef").done(), OperatorType.ASSIGNMENT, "=", null,
                new ExpressionBuilder().createDocumentReferenceExpression("myDoc"));
        automaticTask.addOperation(
                new LeftOperandBuilder().createNewInstance("docFileName").done(),
                OperatorType.ASSIGNMENT,
                "=",
                null,
                new ExpressionBuilder().createGroovyScriptExpression("myScript", "myDoc.getFileName()", String.class.getName(),
                        new ExpressionBuilder().createDocumentReferenceExpression("myDoc")));
        builder.addUserTask("step1", ACTOR_NAME);
        builder.addActor(ACTOR_NAME);
        builder.addData("myDocRef", Document.class.getName(), null);
        builder.addData("docFileName", String.class.getName(), null);
        builder.addDocumentDefinition("myDoc").addContentFileName("myPdfModifiedName.pdf").addDescription("a cool pdf document").addMimeType("application/pdf")
                .addFile("myPdf.pdf");
        builder.addTransition("setDataTask", "step1");
        final byte[] pdfContent = new byte[] { 5, 0, 1, 4, 6, 5, 2, 3, 1, 5, 6, 8, 4, 6, 6, 3, 2, 4, 5 };
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(builder.getProcess())
                .addDocumentResource(new BarResource("myPdf.pdf", pdfContent)).done();
        final ProcessInstance pi = deployAndEnableProcessWithActorAndStartIt(businessArchive, user);
        try {
            final long step1 = waitForUserTask("step1", pi).getId();
            final DataInstance activityDataInstance = getProcessAPI().getActivityDataInstance("myDocRef", step1);
            final Document docRef = (Document) activityDataInstance.getValue();

            final Document attachment = getAttachmentWithoutItsContent(pi);
            assertEquals(attachment, docRef);
            assertEquals("myPdfModifiedName.pdf", getProcessAPI().getActivityDataInstance("docFileName", step1).getValue());
        } finally {
            disableAndDeleteProcess(pi.getProcessDefinitionId());
        }
    }

    @Test
    public void evaluateExpressionOnCompletedProcessInstance_should_be_able_to_retrieve_document_for_an_archived_process_instance() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MyProcessWithDocumentsInBar", "1.0");
        builder.addStartEvent("start");
        builder.addAutomaticTask("auto");
        builder.addEndEvent("end");

        builder.addDocumentDefinition("document").addContentFileName("document.content").addFile("document.content");
        final BusinessArchiveBuilder archive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(builder.getProcess());
        archive.addDocumentResource(new BarResource("document.content", "content".getBytes()));
        final ProcessInstance processInstance = getProcessAPI().startProcess(deployAndEnableProcess(archive.done()).getId());
        waitForProcessToFinish(processInstance.getId());

        final Map<Expression, Map<String, Serializable>> expressions = Collections.<Expression, Map<String, Serializable>> singletonMap(
                new ExpressionBuilder().createDocumentReferenceExpression("document"), Collections.<String, Serializable> emptyMap());
        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionOnCompletedProcessInstance(processInstance.getId(), expressions);

        assertEquals("document.content", ((Document) result.get("document")).getContentFileName());
        disableAndDeleteProcess(processInstance.getProcessDefinitionId());
    }

    @Test
    public void getDocumentOnProcessWithDocumentInDefinitionUsingUrl() throws Exception {
        final String url = "http://plop.org/file.pdf";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MyProcessWithExternalDocuments", "1.0");
        builder.addUserTask("step1", ACTOR_NAME);
        builder.addActor(ACTOR_NAME);
        builder.addDocumentDefinition("myDoc").addContentFileName("file.pdf").addDescription("a cool pdf document").addMimeType("application/pdf").addUrl(url);
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(builder.getProcess()).done();
        final ProcessInstance processInstance = deployAndEnableProcessWithActorAndStartIt(businessArchive, user);
        try {
            final Document attachment = getAttachmentWithoutItsContent(processInstance);
            assertIsSameDocument(attachment, processInstance.getId(), "myDoc", user.getId(), false, "file.pdf", "application/pdf", url);
        } finally {
            // Clean up
            waitForUserTask("step1", processInstance);
            disableAndDeleteProcess(processInstance.getProcessDefinitionId());
        }
    }

    @Test
    public void getDocumentAtProcessInstantiation() throws Exception {
        final ProcessInstance processInstance = deployAndEnableWithActorAndStartIt(user);
        buildAndAttachDocument(processInstance);
        try {
            final Document beforeUpdate = getAttachmentWithoutItsContent(processInstance);
            final Document doc = BuildTestUtil.buildDocument(beforeUpdate.getName());
            getProcessAPI().attachNewDocumentVersion(processInstance.getId(), beforeUpdate.getName(), doc.getContentFileName(), doc.getContentMimeType(),
                    "contentOfTheDoc".getBytes());
            final Document afterUpdate = getAttachmentWithoutItsContent(processInstance);
            assertNotSame(beforeUpdate, afterUpdate);
            final Document documentAtProcessInstantiation = getProcessAPI().getDocumentAtProcessInstantiation(processInstance.getId(), afterUpdate.getName());
            assertEquals(beforeUpdate, documentAtProcessInstantiation);
        } finally {
            disableAndDeleteProcess(processInstance.getProcessDefinitionId());
        }
    }

    @Test
    public void getDocumentAtActivityInstanceCompletion() throws Exception {
        final ProcessInstance processInstance = deployAndEnableWithActorAndStartIt(user);
        buildAndAttachDocument(processInstance);
        try {
            final Document beforeUpdate = getAttachmentWithoutItsContent(processInstance);
            final Document doc = BuildTestUtil.buildDocument(beforeUpdate.getName());
            getProcessAPI().attachNewDocumentVersion(processInstance.getId(), beforeUpdate.getName(), doc.getContentFileName(), doc.getContentMimeType(),
                    "contentOfTheDoc".getBytes());
            final Document afterUpdate = getAttachmentWithoutItsContent(processInstance);
            assertNotSame(beforeUpdate, afterUpdate);

            final HumanTaskInstance step1 = waitForUserTaskAndExecuteIt("step1", processInstance, user);
            waitForArchivedActivity(step1.getId(), TestStates.getNormalFinalState());

            final Document doc2 = BuildTestUtil.buildDocument(beforeUpdate.getName());
            getProcessAPI().attachNewDocumentVersion(processInstance.getId(), beforeUpdate.getName(), doc2.getContentFileName(), doc2.getContentMimeType(),
                    "contentOfTheDoc".getBytes());
            final Document documentAtActivityInstanciation = getProcessAPI().getDocumentAtActivityInstanceCompletion(step1.getId(),
                    beforeUpdate.getName());
            assertEquals(afterUpdate, documentAtActivityInstanciation);
        } finally {
            disableAndDeleteProcess(processInstance.getProcessDefinitionId());
        }

    }

    @Test
    public void getNumberOfDocument() throws Exception {
        final ProcessInstance processInstance = deployAndEnableWithActorAndStartIt(user);
        buildAndAttachDocument(processInstance);

        try {
            final long initialNbOfDocument = getProcessAPI().getNumberOfDocuments(processInstance.getId());
            final String documentName = "anotherDocumentReference";
            final Document doc = buildReferenceToExternalDocument();

            getProcessAPI().attachDocument(processInstance.getId(), documentName, doc.getContentFileName(), doc.getContentMimeType(), doc.getUrl());
            final long currentNbOfDocument = getProcessAPI().getNumberOfDocuments(processInstance.getId());
            assertEquals("Invalid number of attachments!", initialNbOfDocument + 1, currentNbOfDocument);

        } finally {
            disableAndDeleteProcess(processInstance.getProcessDefinitionId());
        }
    }

    @Test
    public void getNumberOfDocumentAfterAddingDocumentValue() throws Exception {
        final ProcessInstance processInstance = deployAndEnableWithActorAndStartIt(user);
        buildAndAttachDocument(processInstance);

        try {
            final long initialNbOfDocument = getProcessAPI().getNumberOfDocuments(processInstance.getId());
            final String documentName = "anotherDocumentValue";
            final Document doc = BuildTestUtil.buildDocument(documentName);

            getProcessAPI().attachDocument(processInstance.getId(), documentName, doc.getContentFileName(), doc.getContentMimeType(), documentName.getBytes());
            final long currentNbOfDocument = getProcessAPI().getNumberOfDocuments(processInstance.getId());
            assertEquals("Invalid number of attachments!", initialNbOfDocument + 1, currentNbOfDocument);

        } finally {
            disableAndDeleteProcess(processInstance.getProcessDefinitionId());
        }
    }

    @Test
    public void searchDocuments() throws Exception {
        // add a new document, search it.
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        final ProcessInstance processInstance = deployAndEnableWithActorAndStartIt(user);
        buildAndAttachDocument(processInstance);
        searchOptionsBuilder.filter(DocumentsSearchDescriptor.PROCESSINSTANCE_ID, processInstance.getId());
        searchOptionsBuilder.sort(DocumentsSearchDescriptor.DOCUMENT_NAME, Order.ASC);
        final SearchResult<Document> documentSearch = getProcessAPI().searchDocuments(searchOptionsBuilder.done());
        assertEquals(1, documentSearch.getCount());
        assertEquals(processInstance.getId(), documentSearch.getResult().get(0).getProcessInstanceId());
        assertEquals(user.getId(), documentSearch.getResult().get(0).getAuthor());

        disableAndDeleteProcess(processInstance.getProcessDefinitionId());
    }

    @Cover(classes = { SearchOptionsBuilder.class, ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "SearchDocuments", "Apostrophe" }, jira = "ENGINE-366, ENGINE-594")
    @Test
    public void searchDocumentsWithApostrophe() throws Exception {
        searchDocumentsWithApostrophe("'documentName", "fileName");
        searchDocumentsWithApostrophe("documentName", "'fileName");
    }

    private void searchDocumentsWithApostrophe(final String documentName, final String fileName) throws Exception {
        final ProcessInstance processInstance = deployAndEnableWithActorAndStartIt(user);
        buildAndAttachDocument(processInstance, documentName, fileName);

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.sort(DocumentsSearchDescriptor.DOCUMENT_NAME, Order.ASC);
        searchOptionsBuilder.searchTerm("'");
        final SearchResult<Document> documentSearch = getProcessAPI().searchDocuments(searchOptionsBuilder.done());

        assertEquals(1, documentSearch.getCount());
        assertEquals(processInstance.getId(), documentSearch.getResult().get(0).getProcessInstanceId());
        assertEquals(user.getId(), documentSearch.getResult().get(0).getAuthor());
        disableAndDeleteProcess(processInstance.getProcessDefinitionId());
    }

    @Test
    public void documentsAreDeletedWhenProcessIsDeleted() throws Exception {
        final ProcessInstance processInstance = deployAndEnableWithActorAndStartIt(user);
        buildAndAttachDocument(processInstance, "test", "test.txt");
        disableAndDeleteProcess(processInstance.getProcessDefinitionId());

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.sort(DocumentsSearchDescriptor.DOCUMENT_NAME, Order.ASC);
        final SearchResult<Document> documentSearch = getProcessAPI().searchDocuments(searchOptionsBuilder.done());

        assertEquals(0, documentSearch.getCount());
    }

    @Test
    public void searchArchivedDocuments() throws Exception {
        // first time search, no document in archive table.
        final ProcessInstance processInstance = deployAndEnableWithActorAndStartIt(user);
        buildAndAttachDocument(processInstance);
        SearchOptionsBuilder searchOptionsBuilder;
        searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(ArchivedDocumentsSearchDescriptor.PROCESSINSTANCE_ID, processInstance.getId());
        SearchResult<ArchivedDocument> documentSearch = getProcessAPI().searchArchivedDocuments(searchOptionsBuilder.done());
        assertEquals(0, documentSearch.getCount());

        try {
            final Document beforeUpdate = getAttachmentWithoutItsContent(processInstance);
            final Document doc = BuildTestUtil.buildDocument(beforeUpdate.getName());
            getProcessAPI().attachNewDocumentVersion(processInstance.getId(), beforeUpdate.getName(), doc.getContentFileName(), doc.getContentMimeType(),
                    "contentOfTheDoc".getBytes());
            final Document afterUpdate = getAttachmentWithoutItsContent(processInstance);
            getProcessAPI().getDocumentAtProcessInstantiation(processInstance.getId(), afterUpdate.getName());

            // search again. exist 1 document in archive table.
            searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
            searchOptionsBuilder.filter(ArchivedDocumentsSearchDescriptor.PROCESSINSTANCE_ID, processInstance.getId());
            searchOptionsBuilder.sort(ArchivedDocumentsSearchDescriptor.DOCUMENT_NAME, Order.ASC);
            documentSearch = getProcessAPI().searchArchivedDocuments(searchOptionsBuilder.done());
            assertEquals(1, documentSearch.getCount());
            assertEquals(processInstance.getId(), documentSearch.getResult().get(0).getProcessInstanceId());
            assertEquals(user.getId(), documentSearch.getResult().get(0).getDocumentAuthor());

            // search with term:
            searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
            searchOptionsBuilder.searchTerm(afterUpdate.getName());
            documentSearch = getProcessAPI().searchArchivedDocuments(searchOptionsBuilder.done());
            assertEquals(1, documentSearch.getCount());
            assertEquals(afterUpdate.getName(), documentSearch.getResult().get(0).getName());

        } finally {
            disableAndDeleteProcess(processInstance.getProcessDefinitionId());
        }
    }

    @Cover(classes = { SearchOptionsBuilder.class, ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "SearchArchivedDocuments", "Apostrophe" }, jira = "ENGINE-366")
    @Test
    public void searchArchivedDocumentsWithApostropheInTheDocumentName() throws Exception {
        final ProcessInstance processInstance = deployAndEnableWithActorAndStartIt(user);
        buildAndAttachDocument(processInstance, "a'", "a");
        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        SearchResult<ArchivedDocument> documentSearch = getProcessAPI().searchArchivedDocuments(searchOptionsBuilder.done());
        assertEquals(0, documentSearch.getCount());

        try {
            final Document beforeUpdate = getAttachmentWithoutItsContent(processInstance);
            final Document doc = BuildTestUtil.buildDocument(beforeUpdate.getName());
            getProcessAPI().attachNewDocumentVersion(processInstance.getId(), beforeUpdate.getName(), doc.getContentFileName(), doc.getContentMimeType(),
                    "contentOfTheDoc".getBytes());
            final Document afterUpdate = getAttachmentWithoutItsContent(processInstance);
            getProcessAPI().getDocumentAtProcessInstantiation(processInstance.getId(), afterUpdate.getName());

            // search with term:
            searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
            searchOptionsBuilder.searchTerm("a'");
            documentSearch = getProcessAPI().searchArchivedDocuments(searchOptionsBuilder.done());
            assertEquals(1, documentSearch.getCount());
            assertEquals(afterUpdate.getName(), documentSearch.getResult().get(0).getName());

        } finally {
            disableAndDeleteProcess(processInstance.getProcessDefinitionId());
        }
    }

    @Cover(classes = { SearchOptionsBuilder.class, ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "SearchArchivedDocuments", "Apostrophe" }, jira = "ENGINE-366")
    @Test
    public void searchArchivedDocumentsWithApostropheInTheFileName() throws Exception {
        final ProcessInstance processInstance = deployAndEnableWithActorAndStartIt(user);
        buildAndAttachDocument(processInstance, "b", "b'");
        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        SearchResult<ArchivedDocument> documentSearch = getProcessAPI().searchArchivedDocuments(searchOptionsBuilder.done());
        assertEquals(0, documentSearch.getCount());

        try {
            final Document beforeUpdate = getAttachmentWithoutItsContent(processInstance);
            final Document doc = BuildTestUtil.buildDocument(beforeUpdate.getName());
            getProcessAPI().attachNewDocumentVersion(processInstance.getId(), beforeUpdate.getName(), doc.getContentFileName(), doc.getContentMimeType(),
                    "contentOfTheDoc".getBytes());
            final Document afterUpdate = getAttachmentWithoutItsContent(processInstance);
            getProcessAPI().getDocumentAtProcessInstantiation(processInstance.getId(), afterUpdate.getName());

            // search with term:
            searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
            searchOptionsBuilder.searchTerm("b'");
            documentSearch = getProcessAPI().searchArchivedDocuments(searchOptionsBuilder.done());
            assertEquals(1, documentSearch.getCount());
            assertEquals(afterUpdate.getName(), documentSearch.getResult().get(0).getName());

        } finally {
            disableAndDeleteProcess(processInstance.getProcessDefinitionId());
        }
    }

    @Test
    public void getArchivedVersionOfDocuments() throws BonitaException {
        // add new document
        final ProcessInstance processInstance = deployAndEnableWithActorAndStartIt(user);
        buildAndAttachDocument(processInstance);
        // search archive document. result is 0.
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(ArchivedDocumentsSearchDescriptor.PROCESSINSTANCE_ID, processInstance.getId());
        final SearchResult<ArchivedDocument> documentSearch = getProcessAPI().searchArchivedDocuments(searchOptionsBuilder.done());
        assertEquals(0, documentSearch.getCount());

        // archive document
        try {
            final Document beforeUpdate = getAttachmentWithoutItsContent(processInstance);
            final Document doc = BuildTestUtil.buildDocument(beforeUpdate.getName());
            getProcessAPI().attachNewDocumentVersion(processInstance.getId(), beforeUpdate.getName(), doc.getContentFileName(), doc.getContentMimeType(),
                    "contentOfTheDoc".getBytes());
            getAttachmentWithoutItsContent(processInstance);

            // get archived document
            final ArchivedDocument archivedDocument = getProcessAPI().getArchivedVersionOfProcessDocument(beforeUpdate.getId());
            assertNotNull(archivedDocument.getArchiveDate());
            assertEquals(beforeUpdate.getId(), archivedDocument.getSourceObjectId());
            assertEquals(processInstance.getId(), archivedDocument.getProcessInstanceId());
        } finally {
            disableAndDeleteProcess(processInstance.getProcessDefinitionId());
        }
    }

    @Test(expected = ArchivedDocumentNotFoundException.class)
    public void getArchivedDocumentNotFound() throws BonitaException {
        getProcessAPI().getArchivedProcessDocument(123456789l);
    }

    @Test
    public void getArchivedDocument() throws BonitaException {
        // add new document
        final ProcessInstance processInstance = deployAndEnableWithActorAndStartIt(user);
        buildAndAttachDocument(processInstance);
        // search archive document. result is 0.
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(ArchivedDocumentsSearchDescriptor.PROCESSINSTANCE_ID, processInstance.getId());
        final SearchResult<ArchivedDocument> documentSearch = getProcessAPI().searchArchivedDocuments(searchOptionsBuilder.done());
        assertEquals(0, documentSearch.getCount());

        // archive document
        try {
            final Document beforeUpdate = getAttachmentWithoutItsContent(processInstance);
            final Document doc = BuildTestUtil.buildDocument(beforeUpdate.getName());
            getProcessAPI().attachNewDocumentVersion(processInstance.getId(), beforeUpdate.getName(), doc.getContentFileName(), doc.getContentMimeType(),
                    "contentOfTheDoc".getBytes());
            getAttachmentWithoutItsContent(processInstance);
            final ArchivedDocument archivedDocument = getProcessAPI().getArchivedVersionOfProcessDocument(beforeUpdate.getId());
            assertEquals(archivedDocument, getProcessAPI().getArchivedProcessDocument(archivedDocument.getId()));
        } finally {
            disableAndDeleteProcess(processInstance.getProcessDefinitionId());
        }
    }

    @Test
    public void countAttachmentWithSomeAttachments() throws BonitaException {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        final long initialNbOfDocument = getProcessAPI().countAttachments(searchOptionsBuilder.done());
        final ProcessInstance processInstance = deployAndEnableWithActorAndStartIt(user);
        buildAndAttachDocument(processInstance);
        final long numberOfAttachments = getProcessAPI().countAttachments(searchOptionsBuilder.done());
        assertEquals(1 + initialNbOfDocument, numberOfAttachments);
        disableAndDeleteProcess(processInstance.getProcessDefinitionId());
    }

    @Cover(classes = DocumentValue.class, concept = BPMNConcept.DOCUMENT, jira = "ENGINE-631", keywords = { "document", "operation", "update" }, story = "update an existing document using operation")
    @Test
    public void updateExistingDocumentWithOperation() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("procWithStringIndexes", "1.0");
        designProcessDefinition.addActor(ACTOR_NAME).addDescription("The doc'");
        designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        final Expression groovyThatCreateDocumentContent = new ExpressionBuilder().createGroovyScriptExpression("script",
                "return new org.bonitasoft.engine.bpm.document.DocumentValue(\"updated Content\".getBytes(), \"plain/text\", \"updatedContent.txt\");",
                DocumentValue.class.getName());
        designProcessDefinition.addAutomaticTask("step2").addOperation(new OperationBuilder().createSetDocument("textFile", groovyThatCreateDocumentContent));
        designProcessDefinition.addUserTask("step3", ACTOR_NAME);
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.addTransition("step2", "step3");
        designProcessDefinition.addDocumentDefinition("textFile").addContentFileName("myUnmodifiedTextFile.pdf").addDescription("a cool text document")
                .addMimeType("plain/text").addFile("myUnmodifiedTextFile.txt");
        final byte[] textContent = "Unmodified content".getBytes();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(designProcessDefinition.getProcess()).addDocumentResource(new BarResource("myUnmodifiedTextFile.txt", textContent))
                .done();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchive, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // before update
        final HumanTaskInstance humanTaskInstance = waitForUserTask("step1", processInstance);
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(DocumentsSearchDescriptor.PROCESSINSTANCE_ID, processInstance.getId());
        final SearchOptions searchOptions = searchOptionsBuilder.done();
        SearchResult<Document> searchDocuments = getProcessAPI().searchDocuments(searchOptions);
        assertEquals(1, searchDocuments.getCount());
        final Document initialDocument = searchDocuments.getResult().iterator().next();
        final byte[] documentContent = getProcessAPI().getDocumentContent(initialDocument.getContentStorageId());
        assertEquals("Unmodified content", new String(documentContent));

        // update
        assignAndExecuteStep(humanTaskInstance, user.getId());
        waitForUserTask("step3", processInstance);

        // after update
        searchDocuments = getProcessAPI().searchDocuments(searchOptions);
        assertEquals(1, searchDocuments.getCount());
        final Document newDocument = searchDocuments.getResult().iterator().next();
        assertEquals("textFile", newDocument.getName());
        assertEquals("updatedContent.txt", newDocument.getContentFileName());
        assertEquals("plain/text", newDocument.getContentMimeType());
        final byte[] newDocumentContent = getProcessAPI().getDocumentContent(newDocument.getContentStorageId());
        assertEquals("updated Content", new String(newDocumentContent));

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = DocumentValue.class, concept = BPMNConcept.DOCUMENT, jira = "ENGINE-978", keywords = { "document", "operation", "update" }, story = "update an existing document using operation")
    @Test
    public void updateExistingDocumentWithNullOperation() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("procWithStringIndexes", "1.0");
        designProcessDefinition.addActor(ACTOR_NAME).addDescription("The doc'");
        designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        final Expression groovyThatReturnNull = new ExpressionBuilder().createGroovyScriptExpression("script", "return null;", DocumentValue.class.getName());
        designProcessDefinition.addAutomaticTask("step2").addOperation(new OperationBuilder().createSetDocument("textFile", groovyThatReturnNull));
        designProcessDefinition.addUserTask("step3", ACTOR_NAME);
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.addTransition("step2", "step3");
        designProcessDefinition.addDocumentDefinition("textFile").addContentFileName("myUnmodifiedTextFile.pdf").addDescription("a cool text document")
                .addMimeType("plain/text").addFile("myUnmodifiedTextFile.txt");
        final byte[] textContent = "Unmodified content".getBytes();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(designProcessDefinition.getProcess()).addDocumentResource(new BarResource("myUnmodifiedTextFile.txt", textContent))
                .done();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchive, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // before update
        final HumanTaskInstance humanTaskInstance = waitForUserTask("step1", processInstance);
        final Document initialDocument = getProcessAPI().getLastDocument(processInstance.getId(), "textFile");
        final byte[] documentContent = getProcessAPI().getDocumentContent(initialDocument.getContentStorageId());
        assertEquals("Unmodified content", new String(documentContent));

        // update
        assignAndExecuteStep(humanTaskInstance, user.getId());
        waitForUserTask("step3", processInstance);

        // after update
        assertEquals("textFile", getProcessAPI().getArchivedVersionOfProcessDocument(initialDocument.getId()).getName());
        try {
            getProcessAPI().getLastDocument(processInstance.getId(), "textFile");
            fail();
        } catch (final DocumentNotFoundException e) {
            // ok
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Cover(classes = DocumentValue.class, concept = BPMNConcept.DOCUMENT, jira = "ENGINE-631", keywords = { "document", "operation", "update" }, story = "update an existing document url using operation")
    @Test
    public void updateExistingDocumentUrlWithOperation() throws Exception {

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("procWithStringIndexes", "1.0");
        designProcessDefinition.addActor(ACTOR_NAME).addDescription("The doc'");
        designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        designProcessDefinition.addAutomaticTask("step2").addOperation(
                new OperationBuilder().createSetDocument("textFile", getDocumentValueExpressionWithUrl("http://www.example.com/new_url.txt")));
        designProcessDefinition.addUserTask("step3", ACTOR_NAME);
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.addTransition("step2", "step3");
        designProcessDefinition.addDocumentDefinition("textFile").addContentFileName("myUnmodifiedTextFile.pdf").addDescription("a cool text document")
                .addMimeType("plain/text").addUrl("http://www.example.com/original_url.txt");
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(designProcessDefinition.getProcess()).done();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchive, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // before update
        final HumanTaskInstance humanTaskInstance = waitForUserTask("step1", processInstance);
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(DocumentsSearchDescriptor.PROCESSINSTANCE_ID, processInstance.getId());
        final SearchOptions searchOptions = searchOptionsBuilder.done();
        SearchResult<Document> searchDocuments = getProcessAPI().searchDocuments(searchOptions);
        assertEquals(1, searchDocuments.getCount());
        final Document initialDocument = searchDocuments.getResult().iterator().next();
        assertEquals("http://www.example.com/original_url.txt", initialDocument.getUrl());

        // update
        assignAndExecuteStep(humanTaskInstance, user.getId());
        waitForUserTask("step3", processInstance);

        // after update
        searchDocuments = getProcessAPI().searchDocuments(searchOptions);
        assertEquals(1, searchDocuments.getCount());
        final Document newDocument = searchDocuments.getResult().iterator().next();
        assertEquals("http://www.example.com/new_url.txt", newDocument.getUrl());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = DocumentValue.class, concept = BPMNConcept.DOCUMENT, jira = "ENGINE-631", keywords = { "document", "operation", "update" }, story = "create a document document using operation")
    @Test
    public void createDocumentWithOperation() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("procWithStringIndexes", "1.0");
        designProcessDefinition.addData("documentValue", DocumentValue.class.getName(), null);
        designProcessDefinition.addActor(ACTOR_NAME).addDescription("The doc'");
        designProcessDefinition.addAutomaticTask("step0").addOperation(
                new OperationBuilder().createSetDataOperation("documentValue", new ExpressionBuilder().createDocumentReferenceExpression("textFile")));
        designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        final Expression groovyThatCreateDocumentContent = new ExpressionBuilder().createGroovyScriptExpression("script",
                "return new org.bonitasoft.engine.bpm.document.DocumentValue(\"updated Content\".getBytes(), \"plain/text\", \"updatedContent.txt\");",
                DocumentValue.class.getName());
        // designProcessDefinition.addAutomaticTask("step2").addOperation(
        // new OperationBuilder().createNewInstance().setRightOperand(groovyThatCreateDocumentContent).setType(OperatorType.DOCUMENT_CREATE_UPDATE)
        // .setLeftOperand("textFile", false).done());
        designProcessDefinition.addAutomaticTask("step2").addOperation(new OperationBuilder().createSetDocument("textFile", groovyThatCreateDocumentContent));
        designProcessDefinition.addUserTask("step3", ACTOR_NAME);
        designProcessDefinition.addTransition("step0", "step1");
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.addTransition("step2", "step3");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // before update
        final HumanTaskInstance humanTaskInstance = waitForUserTask("step1", processInstance);
        // document value expression should return null when document don't exists
        assertNull(getProcessAPI().getProcessDataInstance("documentValue", processInstance.getId()).getValue());
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(DocumentsSearchDescriptor.PROCESSINSTANCE_ID, processInstance.getId());
        final SearchOptions searchOptions = searchOptionsBuilder.done();
        SearchResult<Document> searchDocuments = getProcessAPI().searchDocuments(searchOptions);
        assertEquals(0, searchDocuments.getCount());

        // update
        assignAndExecuteStep(humanTaskInstance, user.getId());
        waitForUserTask("step3", processInstance);

        // after update
        searchDocuments = getProcessAPI().searchDocuments(searchOptions);
        assertEquals(1, searchDocuments.getCount());
        final Document newDocument = searchDocuments.getResult().iterator().next();
        assertEquals("textFile", newDocument.getName());
        assertEquals("updatedContent.txt", newDocument.getContentFileName());
        assertEquals("plain/text", newDocument.getContentMimeType());
        final byte[] newDocumentContent = getProcessAPI().getDocumentContent(newDocument.getContentStorageId());
        assertEquals("updated Content", new String(newDocumentContent));
        disableAndDeleteProcess(processDefinition);

    }

    @Test
    public void startProcessAndSetDocumentValueWithOperations() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("LivingDay", "1.0");
        final String ACTOR_NAME = "documentalist";
        String docRefName = "invoiceReference";
        designProcessDefinition.addData(docRefName, DocumentValue.class.getName(), null);
        String docName = "invoiceLetter";
        designProcessDefinition.addData(docName, DocumentValue.class.getName(), null);
        designProcessDefinition.addActor(ACTOR_NAME);
        // ...
        designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), ACTOR_NAME, user);

        String docUrl = "http://internal.intranet.org/resources/myDoc.pdf";
        Operation docRefOperation = new OperationBuilder().createSetDocument(docRefName,
                new ExpressionBuilder().createInputExpression("documentReference", DocumentValue.class.getName()));

        Operation docContentOperation = new OperationBuilder().createSetDocument(docName,
                new ExpressionBuilder().createInputExpression("documentValue", DocumentValue.class.getName()));

        Map<String, Serializable> expressionContext = new HashMap<String, Serializable>(2);
        String documentFileName = "updatedContent.txt";
        expressionContext.put("documentValue", new DocumentValue("Binary content of the document".getBytes(), "plain/text", documentFileName));
        expressionContext.put("documentReference", new DocumentValue(docUrl));
        ProcessInstance myCase = getProcessAPI()
                .startProcess(processDefinition.getId(), Arrays.asList(docContentOperation, docRefOperation), expressionContext);
        waitForUserTask("step1", myCase);
        SearchResult<Document> searchDocuments = getProcessAPI().searchDocuments(
                new SearchOptionsBuilder(0, 5).filter(DocumentsSearchDescriptor.DOCUMENT_NAME, docRefName).done());
        assertEquals(docUrl, searchDocuments.getResult().get(0).getUrl());

        searchDocuments = getProcessAPI().searchDocuments(new SearchOptionsBuilder(0, 5).filter(DocumentsSearchDescriptor.DOCUMENT_NAME, docName).done());
        assertEquals(documentFileName, searchDocuments.getResult().get(0).getContentFileName());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = DocumentValue.class, concept = BPMNConcept.DOCUMENT, jira = "ENGINE-975", keywords = { "document", "operation", "create", "URL" }, story = "create a document using operation and URL")
    @Test
    public void createDocumentWithOperationUsingURL() throws Exception {
        // deploy and instantiate process
        final String url = "http://www.example.com/new_url.txt";
        final ProcessDefinition processDefinition = deployProcessWithURLDocumentCreateOperation("textFile", url);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // before update
        final HumanTaskInstance humanTaskInstance = waitForUserTask("step1", processInstance);
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(DocumentsSearchDescriptor.PROCESSINSTANCE_ID, processInstance.getId());
        final SearchOptions searchOptions = searchOptionsBuilder.done();
        SearchResult<Document> searchDocuments = getProcessAPI().searchDocuments(searchOptions);
        assertEquals(0, searchDocuments.getCount());

        // update
        assignAndExecuteStep(humanTaskInstance, user.getId());
        waitForUserTask("step3", processInstance);

        // after update
        searchDocuments = getProcessAPI().searchDocuments(searchOptions);
        assertEquals(1, searchDocuments.getCount());
        final Document newDocument = searchDocuments.getResult().iterator().next();
        assertEquals("textFile", newDocument.getName());
        assertEquals(url, newDocument.getUrl());

        // cleaup
        disableAndDeleteProcess(processDefinition);

    }

    private ProcessDefinition deployProcessWithURLDocumentCreateOperation(final String documentName, final String url) throws BonitaException {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("simpleProcess", "1.0");
        designProcessDefinition.addActor(ACTOR_NAME).addDescription("The doctor");
        designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        designProcessDefinition.addAutomaticTask("step2").addOperation(
                new OperationBuilder().createSetDocument(documentName, getDocumentValueExpressionWithUrl(url)));
        designProcessDefinition.addUserTask("step3", ACTOR_NAME);
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.addTransition("step2", "step3");
        return deployAndEnableProcessWithActor(designProcessDefinition.done(), ACTOR_NAME, user);
    }

    @Cover(classes = Document.class, concept = BPMNConcept.DOCUMENT, jira = "ENGINE-652", keywords = { "document", "sort" }, story = "get last version of document, sorted")
    @Test
    public void getLastVersionOfDocumentsOfAProcess() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("procWithStringIndexes", "1.0");
        designProcessDefinition.addActor(ACTOR_NAME).addDescription("The doc'");
        UserTaskDefinitionBuilder userTaskBuilder = designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        userTaskBuilder.addOperation(new OperationBuilder().createSetDocument("textFile2",
                getDocumentValueExpressionWithUrl("http://www.example.com/new_url.txt")));
        userTaskBuilder = designProcessDefinition.addUserTask("step2", ACTOR_NAME);
        userTaskBuilder.addOperation(new OperationBuilder().createSetDocument("textFile4",
                getDocumentValueExpressionWithUrl("http://www.example.com/new_url.txt")));
        designProcessDefinition.addDocumentDefinition("textFile2").addContentFileName("myFile3.pdf").addDescription("a cool text document")
                .addMimeType("application/atom+xml").addUrl("http://www.example.com/original_url5.txt");
        designProcessDefinition.addDocumentDefinition("textFile1").addContentFileName("myFile1.pdf").addDescription("a cool text document")
                .addMimeType("plain/csv").addUrl("http://www.example.com/original_url3.txt");
        designProcessDefinition.addDocumentDefinition("textFile3").addContentFileName("myFile4.pdf").addDescription("a cool text document")
                .addMimeType("plain/text").addUrl("http://www.example.com/original_url2.txt");
        designProcessDefinition.addDocumentDefinition("textFile4").addContentFileName("myFile2.pdf").addDescription("a cool text document")
                .addMimeType("application/pdf").addUrl("http://www.example.com/original_url4.txt");
        designProcessDefinition.addDocumentDefinition("textFile5").addContentFileName("myFile5.pdf").addDescription("a cool text document")
                .addMimeType("plain/xml").addUrl("http://www.example.com/original_url1.txt");
        designProcessDefinition.addUserTask("step3", ACTOR_NAME);
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.addTransition("step2", "step3");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        HumanTaskInstance humanTaskInstance = waitForUserTask("step1", processInstance);
        check(processInstance, 1, 2, 3, 4, 5, DocumentCriterion.NAME_ASC);
        check(processInstance, 5, 4, 3, 2, 1, DocumentCriterion.NAME_DESC);
        check(processInstance, 1, 4, 2, 3, 5, DocumentCriterion.FILENAME_ASC);
        check(processInstance, 5, 3, 2, 4, 1, DocumentCriterion.FILENAME_DESC);
        check(processInstance, 2, 4, 1, 3, 5, DocumentCriterion.MIMETYPE_ASC);
        check(processInstance, 5, 3, 1, 4, 2, DocumentCriterion.MIMETYPE_DESC);
        check(processInstance, 5, 3, 1, 4, 2, DocumentCriterion.URL_ASC);
        check(processInstance, 2, 4, 1, 3, 5, DocumentCriterion.URL_DESC);

        final User john = createUser("john", "bpm");
        logoutOnTenant();
        loginOnDefaultTenantWith("john", "bpm");
        assignAndExecuteStep(humanTaskInstance, john.getId());
        humanTaskInstance = waitForUserTask("step2", processInstance);

        // user id of john > matti
        check(processInstance, 1, 3, 4, 5, 2, DocumentCriterion.AUTHOR_ASC);
        check(processInstance, 2, 1, 3, 4, 5, DocumentCriterion.AUTHOR_DESC);

        // Assign and execute step2
        assignAndExecuteStep(humanTaskInstance, john.getId());
        waitForUserTask("step3", processInstance);

        // special check because date can be too close depending on systems
        final List<Document> dateAsc = getProcessAPI().getLastVersionOfDocuments(processInstance.getId(), 0, 10, DocumentCriterion.CREATION_DATE_ASC);
        assertEquals("textFile2", dateAsc.get(3).getName());
        assertEquals("textFile4", dateAsc.get(4).getName());
        final List<Document> dateDesc = getProcessAPI().getLastVersionOfDocuments(processInstance.getId(), 0, 10, DocumentCriterion.CREATION_DATE_DESC);
        assertEquals("textFile2", dateDesc.get(1).getName());
        assertEquals("textFile4", dateDesc.get(0).getName());

        disableAndDeleteProcess(processDefinition);
        deleteUser(john);
    }

    @Cover(classes = Document.class, concept = BPMNConcept.DOCUMENT, jira = "ENGINE-929", keywords = { "document", "name" }, story = "Start a process with a long name (number of characters > 255)")
    @Test(expected = InvalidProcessDefinitionException.class)
    public void startProcessWithLongSizeDocumentName() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithDocumentWithLongName", "1.0");
        processBuilder.addActor(ACTOR_NAME).addUserTask("step1", ACTOR_NAME);

        // Build fileName with 256 characters
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            builder.append("aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeee"); // + 50 characters
        }
        builder.append("12.pdf");
        final String fileName = builder.toString();
        assertEquals(256, fileName.length());

        // Build document
        processBuilder.addDocumentDefinition("doc").addFile("myPdf.pdf").addContentFileName(fileName).addMimeType("application/octet-stream");

        processBuilder.getProcess();
    }

    @Cover(classes = Document.class, concept = BPMNConcept.DOCUMENT, jira = "ENGINE-929", keywords = { "document", "name" }, story = "Start a process with file name with maximum number of characters authorized.")
    @Test
    public void startProcessWithMaxSizeDocumentName() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithDocumentWithLongName", "1.0");
        processBuilder.addActor(ACTOR_NAME).addUserTask("step1", ACTOR_NAME);

        // Build fileName with 255 characters
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            builder.append("aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeee"); // + 50 characters
        }
        builder.append("1.pdf");
        final String fileName = builder.toString();
        assertEquals(255, fileName.length());

        // Build document
        final byte[] pdfContent = "Some document content".getBytes();
        processBuilder.addDocumentDefinition("doc").addFile("myPdf.pdf").addContentFileName(fileName).addMimeType("application/octet-stream");
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processBuilder.getProcess())
                .addDocumentResource(new BarResource("myPdf.pdf", pdfContent)).done();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchive, ACTOR_NAME, user);
        getProcessAPI().startProcess(processDefinition.getId());

        disableAndDeleteProcess(processDefinition.getId());
    }

    @Cover(classes = Document.class, concept = BPMNConcept.DOCUMENT, jira = "ENGINE-929", keywords = { "document", "url" }, story = "Start a process with a long url (number of characters > 255)")
    @Test(expected = InvalidProcessDefinitionException.class)
    public void startProcessWithLongSizeDocumentURL() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithDocumentWithLongName", "1.0");
        processBuilder.addActor(ACTOR_NAME).addUserTask("step1", ACTOR_NAME);

        // Build URL with 256 characters
        final StringBuilder builder = new StringBuilder("http://intranet.bonitasoft.com/private/docStorage/");
        for (int i = 0; i < 4; i++) {
            builder.append("aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeee"); // + 50 characters
        }
        builder.append("123456");
        final String url = builder.toString();
        assertEquals(256, url.length());

        // Build document
        final String docName = "myRtfDocument";
        final DocumentDefinitionBuilder documentDefinition = processBuilder.addDocumentDefinition(docName);
        documentDefinition.addUrl(url);

        processBuilder.getProcess();
    }

    @Cover(classes = Document.class, concept = BPMNConcept.DOCUMENT, jira = "ENGINE-929", keywords = { "document", "url" }, story = "Start a process with url with maximum number of characters authorized.")
    @Test
    public void startProcessWithMaxSizeDocumentURL() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithDocumentWithLongName", "1.0");
        processBuilder.addActor(ACTOR_NAME).addUserTask("step1", ACTOR_NAME);

        // Build URL with 255 characters
        final StringBuilder builder = new StringBuilder("http://intranet.bonitasoft.com/private/docStorage/");
        for (int i = 0; i < 4; i++) {
            builder.append("aaaaaaaaaabbbbbbbbbbccccccccccddddddddddeeeeeeeeee"); // + 50 characters
        }
        builder.append("12345");
        final String url = builder.toString();
        assertEquals(255, url.length());

        // Build document
        final String docName = "myRtfDocument";
        final DocumentDefinitionBuilder documentDefinition = processBuilder.addDocumentDefinition(docName);
        documentDefinition.addUrl(url);
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processBuilder.getProcess())
                .done();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchive, ACTOR_NAME, user);
        getProcessAPI().startProcess(processDefinition.getId());

        disableAndDeleteProcess(processDefinition.getId());

    }

    private Expression getDocumentValueExpressionWithUrl(final String url) throws InvalidExpressionException {
        return new ExpressionBuilder().createGroovyScriptExpression("script", "return new org.bonitasoft.engine.bpm.document.DocumentValue(\"" + url + "\");",
                DocumentValue.class.getName());
    }

    private void check(final ProcessInstance processInstance, final int one, final int two, final int three, final int four, final int five,
            final DocumentCriterion documentCriterion) throws ProcessInstanceNotFoundException, DocumentException {
        final List<Document> lastVersionOfDocuments = getProcessAPI().getLastVersionOfDocuments(processInstance.getId(), 0, 10, documentCriterion);
        assertThat("the order was not respected for " + documentCriterion, lastVersionOfDocuments,
                nameAre("textFile" + one, "textFile" + two, "textFile" + three, "textFile" + four, "textFile" + five));
    }

    @Cover(jira = "ENGINE-1898", classes = { Document.class, CallActivityInstance.class }, concept = BPMNConcept.DOCUMENT, keywords = { "document",
            "call activity", "multi instance" })
    @Test
    public void getMIDocumentOnProcess() throws Exception {
        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
        final ProcessDefinitionBuilder miBuilder = new ProcessDefinitionBuilder().createNewInstance("MI", "0.8");
        miBuilder.addData("urls", List.class.getName(),
                expressionBuilder.createGroovyScriptExpression("urls", "[\"http://someurl\", \"http://someurl1\", \"http://someurl2\"]", List.class.getName()));
        final CallActivityBuilder callActivityBuilder = miBuilder.addCallActivity("mi", expressionBuilder.createConstantStringExpression("DocSubProcess"),
                expressionBuilder.createConstantStringExpression("0.4"));
        callActivityBuilder.addShortTextData("url", null);
        callActivityBuilder
                .addDataInputOperation(
                        new OperationBuilder().createSetDataOperation("url", expressionBuilder.createDataExpression("url", String.class.getName())))
                .addMultiInstance(false, "urls")
                .addDataInputItemRef("url")
                .addCompletionCondition(
                        expressionBuilder.createGroovyScriptExpression("urls", "numberOfCompletedInstances == urls.size();", Boolean.class.getName(),
                                expressionBuilder.createDataExpression("urls", List.class.getName())));

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("DocSubProcess", "0.4");
        builder.addDocumentDefinition("caseDocument").addUrl("toto");
        builder.addShortTextData("url", null);
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step1", ACTOR_NAME).addOperation(
                new OperationBuilder().createSetDocument("caseDocument", expressionBuilder.createGroovyScriptExpression("addDocVersion",
                        "import org.bonitasoft.engine.bpm.document.DocumentValue;return new DocumentValue(url);", DocumentValue.class.getName(),
                        expressionBuilder.createDataExpression("url", String.class.getName()))));
        builder.addUserTask("step2", ACTOR_NAME).addTransition("step1", "step2");
        final ProcessDefinition docDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
        final ProcessDefinition miDefinition = deployAndEnableProcess(miBuilder.done());
        getProcessAPI().startProcess(miDefinition.getId());
        waitForUserTaskAndExecuteIt("step1", user);
        waitForUserTaskAndExecuteIt("step1", user);
        waitForUserTaskAndExecuteIt("step1", user);

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.sort(DocumentsSearchDescriptor.DOCUMENT_URL, Order.ASC);
        final SearchResult<Document> searchDocuments = getProcessAPI().searchDocuments(searchOptionsBuilder.done());
        assertEquals(3, searchDocuments.getCount());
        final List<Document> documents = searchDocuments.getResult();
        final List<String> urls = new ArrayList<String>();
        urls.add(documents.get(0).getUrl());
        urls.add(documents.get(1).getUrl());
        urls.add(documents.get(2).getUrl());

        assertEquals(Arrays.asList("http://someurl", "http://someurl1", "http://someurl2"), urls);

        waitForUserTask("step2");
        waitForUserTask("step2");
        waitForUserTask("step2");
        disableAndDeleteProcess(miDefinition);
        disableAndDeleteProcess(docDefinition);
    }

    @Cover(jira = "BS-507", classes = { Document.class, CallActivityInstance.class }, concept = BPMNConcept.DOCUMENT, keywords = { "document reference",
            "call activity" })
    @Test
    public void getDocumentOnACallActivityOfAProcess() throws Exception {
        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();

        final ProcessDefinitionBuilder spBuilder = new ProcessDefinitionBuilder().createNewInstance("SubProcess", "0.8");
        spBuilder.addActor(ACTOR_NAME);
        spBuilder.addUserTask("step1", ACTOR_NAME);
        spBuilder.addDocumentDefinition("document1").addMimeType("application/octet-stream").addContentFileName("file").addFile("file");
        final byte[] pdfContent = new byte[] { 5, 0, 1, 4, 6, 5, 2, 3, 1, 5, 6, 8, 4, 6, 6, 3, 2, 4, 5 };
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(spBuilder.getProcess())
                .addDocumentResource(new BarResource("file", pdfContent)).done();
        final ProcessDefinition docDefinition = deployAndEnableProcessWithActor(businessArchive, ACTOR_NAME, user);

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("CAProcess", "0.4");
        builder.addActor(ACTOR_NAME);
        builder.addCallActivity("ca", expressionBuilder.createConstantStringExpression("SubProcess"), expressionBuilder.createConstantStringExpression("0.8"));
        final ProcessDefinition caDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);

        getProcessAPI().startProcess(caDefinition.getId());
        final HumanTaskInstance taskInstance = waitForUserTask("step1");

        final Expression expression = expressionBuilder.createDocumentReferenceExpression("document1");
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(5);
        expressions.put(expression, new HashMap<String, Serializable>());

        final Map<String, Serializable> docsMap = getProcessAPI().evaluateExpressionsOnActivityInstance(taskInstance.getId(), expressions);

        assertNotNull(docsMap);
        final String fileName = ((Document) docsMap.get(expression.getName())).getContentFileName();
        assertEquals("file", fileName);

        disableAndDeleteProcess(caDefinition);
        disableAndDeleteProcess(docDefinition);
    }

    public ProcessInstance deployAndEnableWithActorAndStartIt(final User user) throws BonitaException {
        return deployAndEnableProcessWithActorAndStartIt(getNormalBar(), user);
    }

    public BusinessArchive getNormalBar() throws InvalidProcessDefinitionException, InvalidBusinessArchiveFormatException {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process",
                String.valueOf(processVersion++), Arrays.asList("step1", "step2"), Arrays.asList(true, true), ACTOR_NAME, false);
        return new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
    }
}
