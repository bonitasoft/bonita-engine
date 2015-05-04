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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bonitasoft.engine.bpm.contract.FileInputValue;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.api.impl.DocumentHelper;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.session.SessionService;
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
public class DocumentListLeftOperandHandlerTest {

    private static final long LOGGED_USER_ID = 125;
    public static final long CONTAINER_ID = 45l;
    public static final String CONTAINER_TYPE = "container";
    public static final long PROCESS_INSTANCE_ID = 45l;
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
    @Mock
    private ProcessDefinitionService processDefinitionService;
    @Mock
    private ProcessInstanceService processInstanceService;
    @Mock
    private SProcessInstance processInstance;
    @Mock
    private SProcessDefinition processDefinition;
    @Mock
    private SFlowElementContainerDefinition flowElementContainerDefinition;
    @Mock
    private DocumentHelper documentHelper;

    private DocumentListLeftOperandHandler handler;

    @Before
    public void setUp() throws SFlowNodeReadException, SFlowNodeNotFoundException {
        handler = new DocumentListLeftOperandHandler(activityInstanceService, sessionAccessor, sessionService, documentService, documentHelper);
        doReturn(LOGGED_USER_ID).when(sessionService).getLoggedUserFromSession(sessionAccessor);

        SFlowNodeInstance flowNodeInstance = mock(SFlowNodeInstance.class);
        doReturn(flowNodeInstance).when(activityInstanceService).getFlowNodeInstance(CONTAINER_ID);
        doReturn(PROCESS_INSTANCE_ID).when(flowNodeInstance).getParentProcessInstanceId();
    }

    private SLeftOperandImpl createLeftOperand(final String name) {
        final SLeftOperandImpl sLeftOperand = new SLeftOperandImpl();
        sLeftOperand.setName(name);
        return sLeftOperand;
    }

    @Test
    public void should_update_check_it_is_a_list() throws Exception {
        exception.expect(SOperationExecutionException.class);
        exception.expectMessage("Document operation only accepts an expression returning a list of DocumentValue");
        handler.update(createLeftOperand("myDoc"), Collections.<String, Object>emptyMap(), new HashMap<Object, Object>(), 45l, "container");
    }

    @Test
    public void should_update_setDocumentList() throws Exception {
        doNothing().when(documentHelper).setDocumentList(anyList(), anyString(), anyLong(), anyLong());
        List<DocumentValue> newValue = Arrays.asList(documentValue("doc1"), documentValue("doc2"));
        handler.update(createLeftOperand("myDoc"), Collections.<String, Object>emptyMap(), newValue, CONTAINER_ID, CONTAINER_TYPE);
        verify(documentHelper).setDocumentList(newValue, "myDoc", PROCESS_INSTANCE_ID, LOGGED_USER_ID);
    }

    private DocumentValue documentValue(String name) {
        return new DocumentValue(name);
    }

    @Test
    public void should_toCheckedList_check_null() throws Exception {
        exception.expect(SOperationExecutionException.class);
        exception.expectMessage("Document operation only accepts an expression returning a list of DocumentValue");
        handler.toCheckedList(null);
    }

    @Test
    public void should_toCheckedList_check_not_list() throws Exception {
        exception.expect(SOperationExecutionException.class);
        exception.expectMessage("Document operation only accepts an expression returning a list of DocumentValue");
        handler.toCheckedList(new Object());
    }

    @Test
    public void should_toCheckedList_check_not_all_doc() throws Exception {
        exception.expect(SOperationExecutionException.class);
        exception.expectMessage("Document operation only accepts an expression returning a list of DocumentValue");
        handler.toCheckedList(Arrays.asList(new DocumentValue("theUrl"), new Object()));
    }

    @Test
    public void should_toCheckedList_returns_the_list_if_contains_FileInputValue() throws Exception {
        final List<FileInputValue> inputList = Collections.singletonList(new FileInputValue("report.pdf", "The report content".getBytes()));
        final List<DocumentValue> result = handler.toCheckedList(inputList);
        assertThat(result.get(0).getContent()).isEqualTo("The report content".getBytes());
        assertThat(result.get(0).getFileName()).isEqualTo("report.pdf");
    }

}
