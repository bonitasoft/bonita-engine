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
package org.bonitasoft.engine.operation;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;

import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DocumentLeftOperandHandlerTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    private DocumentService documentService;

    @Mock
    private ActivityInstanceService activityInstanceService;

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private DocumentLeftOperandHandler handler;

    private SLeftOperandImpl createLeftOperand(final String name) {
        final SLeftOperandImpl sLeftOperand = new SLeftOperandImpl();
        sLeftOperand.setName(name);
        return sLeftOperand;
    }

    @Test
    public void deleteThrowsAnExceptionNotYetSupported() throws Exception {
        exception.expect(SOperationExecutionException.class);
        exception.expectMessage("Deleting a document is not supported");
        handler.delete(createLeftOperand("myData"), 45l, "container");
    }

    @Test
    public void should_update_check_the_type() throws Exception {
        exception.expect(SOperationExecutionException.class);
        exception.expectMessage("Document operation only accepts an expression returning a DocumentValue and not java.util.HashMap");
        handler.update(new SLeftOperandImpl(), Collections.<String, Object>emptyMap(), new HashMap<Object, Object>(), 45l, "container");
    }

    @Test
    public void should_update_delete_if_type_is_null() throws Exception {
        handler.update(createLeftOperand("myDoc"), Collections.<String, Object>emptyMap(), null, 45l, "PROCESS_INSTANCE");
        verify(documentService).removeCurrentVersion(45l, "myDoc");
    }

    @Test
    public void should_update_create_doc_if_not_exists() throws Exception {
        //given
        doThrow(new SObjectNotFoundException("myDoc")).when(documentService).getMappedDocument(45l, "myDoc");
        //when
        handler.update(createLeftOperand("myDoc"), Collections.<String,Object>emptyMap(), new DocumentValue("content".getBytes(), "plain/text", "file.txt"), 45l, "PROCESS_INSTANCE");
        //then
        verify(documentService).attachDocumentToProcessInstance(any(SDocument.class), eq(45l), eq("myDoc"), isNull(String.class));
    }

    @Test
    public void should_update_update_doc_if_exists() throws Exception {
        //given
        final SMappedDocument sMappedDocument = mock(SMappedDocument.class);
        doReturn(sMappedDocument).when(documentService).getMappedDocument(45l, "myDoc");
        //when
        handler.update(createLeftOperand("myDoc"), Collections.<String,Object>emptyMap(), new DocumentValue("content".getBytes(), "plain/text", "file.txt"), 45l, "PROCESS_INSTANCE");
        //then
        verify(documentService).updateDocument(eq(sMappedDocument), any(SDocument.class));
    }

    public void should_update_find_process_id() throws Exception {
        //given
        final FlowNodeInstance flowNodeInstance = mock(FlowNodeInstance.class);
        doReturn(45l).when(flowNodeInstance).getParentProcessInstanceId();
        doReturn(flowNodeInstance).when(activityInstanceService).getFlowNodeInstance(12l);
        final SMappedDocument sMappedDocument = mock(SMappedDocument.class);
        doReturn(sMappedDocument).when(documentService).getMappedDocument(45l, "myDoc");
        //when
        handler.update(createLeftOperand("myDoc"), Collections.<String,Object>emptyMap(), new DocumentValue("content".getBytes(), "plain/text", "file.txt"), 12l, "notProcess");
        //then
        verify(documentService).updateDocument(eq(sMappedDocument), any(SDocument.class));

    }

    @Test
    public void should_not_update_if_has_change_is_false() throws Exception {
        //when
        handler.update(createLeftOperand("myDoc"), Collections.<String,Object>emptyMap(), new DocumentValue(123l), 45l, "PROCESS_INSTANCE");
        //then
        verify(documentService, times(0)).updateDocument(any(SMappedDocument.class), any(SDocument.class));
    }

}
