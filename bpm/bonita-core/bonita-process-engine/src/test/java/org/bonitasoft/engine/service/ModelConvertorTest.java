package org.bonitasoft.engine.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.model.SUser;
import org.junit.Ignore;
import org.junit.Test;

public class ModelConvertorTest {




    @Test
    public void convertDataInstanceIsTransient() {
        final SDataInstance sDataInstance = mock(SDataInstance.class);
        when(sDataInstance.getClassName()).thenReturn(Integer.class.getName());
        when(sDataInstance.isTransientData()).thenReturn(true);

        final DataInstance dataInstance = ModelConvertor.toDataInstance(sDataInstance);
        assertTrue(dataInstance.isTransientData());
    }

    @Test
    public void convertDataInstanceIsNotTransient() {
        final SDataInstance sDataInstance = mock(SDataInstance.class);
        when(sDataInstance.getClassName()).thenReturn(Integer.class.getName());
        when(sDataInstance.isTransientData()).thenReturn(false);

        final DataInstance dataInstance = ModelConvertor.toDataInstance(sDataInstance);
        assertFalse(dataInstance.isTransientData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProcessInstanceState_conversionOnUnknownStateShouldThrowException() {
        ModelConvertor.getProcessInstanceState("un_known_state");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProcessInstanceState_conversionOnNullStateShouldThrowException() {
        ModelConvertor.getProcessInstanceState(null);
    }

    @Test
    public void convertSUserToUserDoesntShowPassword() {
        final SUser sUser = mock(SUser.class);

        final User testUser = ModelConvertor.toUser(sUser);

        assertThat(testUser.getPassword()).isEmpty();
        verify(sUser, never()).getPassword();
    }
    private DocumentService createdMockedDocumentService() {
        DocumentService documentService = mock(DocumentService.class);
        doReturn("url?fileName=document&contentStorageId=123").when(documentService).generateDocumentURL("document","123");
        return documentService;
    }

    private SMappedDocument createMockedDocument() {
        SMappedDocument documentMapping = mock(SMappedDocument.class);
        doReturn("document").when(documentMapping).getFileName();
        doReturn(123l).when(documentMapping).getDocumentId();
        doReturn("whateverUrl").when(documentMapping).getUrl();
        return documentMapping;
    }


    @Test
    public void getDocument_from_process_instance_and_name_should_return_a_document_with_generated_url_when_it_has_content() throws Exception {

        SMappedDocument documentMapping = createMockedDocument();
        DocumentService documentService = createdMockedDocumentService();
        doReturn(true).when(documentMapping).hasContent();

        Document document = ModelConvertor.toDocument(documentMapping, documentService);

        assertEquals("url?fileName=document&contentStorageId=123", document.getUrl());
    }


    @Test
    public void getDocument_from_process_instance_and_name_should_return_a_document_url_when_is_external_url() throws Exception {

        SMappedDocument documentMapping = createMockedDocument();
        DocumentService documentService = createdMockedDocumentService();
        doReturn(false).when(documentMapping).hasContent();

        Document document = ModelConvertor.toDocument(documentMapping, documentService);

        assertEquals("whateverUrl", document.getUrl());
    }

}
