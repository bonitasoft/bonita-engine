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

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.document.DocumentsSearchDescriptor;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.document.mapping.DocumentMappingService;
import org.bonitasoft.engine.core.process.document.mapping.model.SDocumentMapping;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilder;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilderFactory;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Test;

/**
 * @author Nicolas Chabanoles
 * @author Celine Souchet
 *         TODO: add tests
 */
public class DocumentMappingServiceTest extends CommonBPMServicesTest {

    private static final int NUMBER_OF_DOCUMENT_MAPPING = 10;

    private static BPMServicesBuilder bpmServicesBuilder;

    private static DocumentMappingService documentMappingService;

    private static TransactionService transactionService;

    static {
        bpmServicesBuilder = new BPMServicesBuilder();
        documentMappingService = bpmServicesBuilder.getDocumentMappingService();
        transactionService = bpmServicesBuilder.getTransactionService();
    }

    @Cover(classes = { DocumentMappingService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Create", "DocumentMapping" }, jira = "")
    @Test
    public void createDocumentMappingTest() throws SBonitaException {
        transactionService.begin();
        final SDocumentMapping document = buildDocumentMapping(1);
        final SDocumentMapping result = documentMappingService.create(document);
        transactionService.complete();

        assertEquals(document.getDocumentAuthor(), result.getDocumentAuthor());
        assertEquals(document.getDocumentContentFileName(), result.getDocumentContentFileName());
        assertEquals(document.getDocumentContentMimeType(), result.getDocumentContentMimeType());
        assertEquals(document.getDocumentCreationDate(), result.getDocumentCreationDate());
        assertEquals(document.getDocumentURL(), result.getDocumentURL());
        assertEquals(document.getContentStorageId(), result.getContentStorageId());
        assertEquals(document.getDiscriminator(), result.getDiscriminator());
        assertEquals(document.getId(), result.getId());
        assertEquals(document.getDocumentName(), result.getDocumentName());
        assertEquals(document.getProcessInstanceId(), result.getProcessInstanceId());
        assertEquals(document.getTenantId(), result.getTenantId());

        // Clean up
        deleteDocumentMapping(result);
    }

    @Cover(classes = { DocumentMappingService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "DocumentMapping", "Id" }, jira = "")
    @Test
    public void getDocumentByIdTest() throws SBonitaException {
        final SDocumentMapping sDocumentMapping = createDocumentMapping();
        final SDocumentMapping result = getDocumentMapping(sDocumentMapping);
        assertEquals(sDocumentMapping.getId(), result.getId());

        // Clean up
        deleteDocumentMapping(result);
    }

    @Cover(classes = { DocumentMappingService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "DocumentMapping", "ProcessId", "Name" }, jira = "")
    @Test
    public void getDocumentByNameAndProcessIdTest() throws SBonitaException {
        final SDocumentMapping sDocumentMapping = createDocumentMapping();

        transactionService.begin();
        final SDocumentMapping result = documentMappingService.get(sDocumentMapping.getProcessInstanceId(), sDocumentMapping.getDocumentName());
        transactionService.complete();

        assertEquals(sDocumentMapping.getId(), result.getId());
        assertEquals(sDocumentMapping.getDocumentName(), result.getDocumentName());
        assertEquals(sDocumentMapping.getProcessInstanceId(), result.getProcessInstanceId());

        // Clean up
        deleteDocumentMapping(result);
    }

    @Cover(classes = { DocumentMappingService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "DocumentMapping", "Number", "ProcessInstance" }, jira = "")
    @Test
    public void getNumberOfDocumentMappingsForProcessInstanceTest() throws SBonitaException {
        final List<SDocumentMapping> list = createDocumentMappings();

        transactionService.begin();
        final long result = documentMappingService.getNumberOfDocumentMappingsForProcessInstance(list.get(0).getProcessInstanceId());
        transactionService.complete();

        assertEquals(NUMBER_OF_DOCUMENT_MAPPING, result);

        // Clean up
        deleteDocumentMappings(list);
    }

    @Cover(classes = { DocumentMappingService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Get", "DocumentMapping", "Number", "ProcessInstance" }, jira = "")
    @Test
    public void getDocumentMappingsForProcessInstanceTest() throws SBonitaException {
        final List<SDocumentMapping> list = createDocumentMappings();

        transactionService.begin();
        final List<SDocumentMapping> result = documentMappingService.getDocumentMappingsForProcessInstance(list.get(0).getProcessInstanceId(), 0,
                NUMBER_OF_DOCUMENT_MAPPING + 1, DocumentsSearchDescriptor.DOCUMENT_NAME, OrderByType.ASC);
        transactionService.complete();

        assertEquals(NUMBER_OF_DOCUMENT_MAPPING, result.size());
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
        deleteDocumentMappings(list);
    }

    @Cover(classes = { DocumentMappingService.class }, concept = BPMNConcept.DOCUMENT, keywords = { "Remove", "DocumentMapping" }, jira = "ENGINE-594")
    @Test
    public void removeDocumentsTest() throws SBonitaException {
        final List<SDocumentMapping> list = createDocumentMappings();
        deleteDocumentMappings(list);

        for (final SDocumentMapping sDocumentMapping : list) {
            final SDocumentMapping result = getDocumentMapping(sDocumentMapping);
            assertEquals(sDocumentMapping.getId(), result.getId());
        }
    }

    private SDocumentMapping buildDocumentMapping(final int i) {
        final SDocumentMappingBuilder builder = BuilderFactory.get(SDocumentMappingBuilderFactory.class).createNewInstance();
        builder.setDocumentAuthor(i);
        builder.setDocumentContentFileName("getContentTest.txt");
        builder.setDocumentContentMimeType("text/plain");
        builder.setDocumentCreationDate(System.currentTimeMillis());
        builder.setDocumentName("documentName" + i);
        builder.setDocumentStorageId(String.valueOf(i));
        builder.setDocumentURL("url");
        builder.setHasContent(true);
        builder.setProcessInstanceId(i);
        return builder.done();
    }

    private SDocumentMapping createDocumentMapping() throws SBonitaException {
        transactionService.begin();
        final SDocumentMapping document = buildDocumentMapping(1);
        final SDocumentMapping doc = documentMappingService.create(document);
        transactionService.complete();
        return doc;
    }

    private List<SDocumentMapping> createDocumentMappings() throws SBonitaException {
        transactionService.begin();
        final List<SDocumentMapping> list = new ArrayList<SDocumentMapping>(10);
        for (int i = 0; i < NUMBER_OF_DOCUMENT_MAPPING; i++) {
            final SDocumentMapping document = buildDocumentMapping(i);
            list.add(documentMappingService.create(document));
        }
        transactionService.complete();
        return list;
    }

    private SDocumentMapping getDocumentMapping(final SDocumentMapping sDocumentMapping) throws SBonitaException {
        transactionService.begin();
        final SDocumentMapping result = documentMappingService.get(sDocumentMapping.getId());
        transactionService.complete();
        return result;
    }

    private void deleteDocumentMappings(final List<SDocumentMapping> sDocumentMappings) throws SBonitaException {
        transactionService.begin();
        for (final SDocumentMapping sDocumentMapping : sDocumentMappings) {
            documentMappingService.delete(sDocumentMapping);
        }
        transactionService.complete();
    }

    private void deleteDocumentMapping(final SDocumentMapping sDocumentMapping) throws SBonitaException {
        transactionService.begin();
        documentMappingService.delete(sDocumentMapping);
        transactionService.complete();
    }
}
