package org.bonitasoft.engine.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;

import java.util.HashMap;

import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.exception.SDocumentNotFoundException;
import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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

    @Before
    public void before() throws SSessionNotFoundException {
        when(sessionService.getSession(anyLong())).thenReturn(mock(SSession.class));
    }

    @Test
    public void deleteThrowsAnExceptionNotYetSupported() throws Exception {
        exception.expect(SOperationExecutionException.class);
        exception.expectMessage("Deleting a document is not supported");
        handler.delete(createLeftOperand("myData"), 45l, "container");
    }

    @Test
    public void handlerSupportsBatchUpdate() {
        assertThat(handler.supportBatchUpdate()).isTrue();
    }

    @Test
    public void should_update_check_the_type() throws Exception {
        exception.expect(SOperationExecutionException.class);
        exception.expectMessage("Document operation only accepts an expression returning a DocumentValue and not java.util.HashMap");
        handler.update(new SLeftOperandImpl(), new HashMap(), 45l, "container");
    }

    @Test
    public void should_update_delete_if_type_is_null() throws Exception {
        handler.update(createLeftOperand("myDoc"), null, 45l, "PROCESS_INSTANCE");
        verify(documentService).removeCurrentVersion(45l, "myDoc");
    }

    @Test
    public void should_update_create_doc_if_not_exists() throws Exception {
        //given
        doThrow(new SDocumentNotFoundException("myDoc")).when(documentService).getMappedDocument(45l, "myDoc");
        //when
        handler.update(createLeftOperand("myDoc"), new DocumentValue("content".getBytes(), "plain/text", "file.txt"), 45l, "PROCESS_INSTANCE");
        //then
        verify(documentService).attachDocumentToProcessInstance(any(SDocument.class), eq(45l), eq("myDoc"), isNull(String.class));
    }

    @Test
    public void should_update_update_doc_if_exists() throws Exception {
        //given
        doReturn(mock(SMappedDocument.class)).when(documentService).getMappedDocument(45l, "myDoc");
        //when
        handler.update(createLeftOperand("myDoc"), new DocumentValue("content".getBytes(), "plain/text", "file.txt"), 45l, "PROCESS_INSTANCE");
        //then
        verify(documentService).updateDocumentOfProcessInstance(any(SDocument.class), eq(45l), eq("myDoc"), isNull(String.class));
    }

    public void should_update_find_process_id() throws Exception {
        //given
        FlowNodeInstance flowNodeInstance = mock(FlowNodeInstance.class);
        doReturn(45l).when(flowNodeInstance).getParentProcessInstanceId();
        doReturn(flowNodeInstance).when(activityInstanceService).getFlowNodeInstance(12l);
        //when
        handler.update(createLeftOperand("myDoc"), new DocumentValue("content".getBytes(), "plain/text", "file.txt"), 12l, "notProcess");
        //then
        verify(documentService).updateDocumentOfProcessInstance(any(SDocument.class), eq(45l), eq("myDoc"), isNull(String.class));

    }

}
