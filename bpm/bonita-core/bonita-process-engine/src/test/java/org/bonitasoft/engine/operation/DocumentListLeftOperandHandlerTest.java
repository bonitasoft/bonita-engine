/*
 * Copyright (C) 2014 BonitaSoft S.A.
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

package org.bonitasoft.engine.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SDocumentListDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SDocumentListDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.persistence.SBonitaReadException;
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

    @InjectMocks
    private DocumentListLeftOperandHandler handler;

    @Before
    public void setUp() throws Exception {

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
        handler.update(createLeftOperand("myDoc"), new HashMap(), 45l, "container");
    }

    @Test
    public void should_update_check_it_is_a_documentValue_list() throws Exception {
        exception.expect(SOperationExecutionException.class);
        exception.expectMessage("Document operation only accepts an expression returning a list of DocumentValue");
        handler.update(createLeftOperand("myDoc"), Arrays.asList(new DocumentValue("theUrl"), new Object()), 45l, "container");

    }

    @Test
    public void should_isDefinedInDefinition_return_false_id_not_in_def() throws Exception {
        //given
        initDefinition("list1", "list2");
        //when then
        assertThat(DocumentListLeftOperandHandler.isListDefinedInDefinition("theList", 45l, processDefinitionService, processInstanceService)).isFalse();
    }

    @Test
    public void should_isDefinedInDefinition_throw_not_found_when_not_existing_instance() throws Exception {
        //given
        initDefinition("list1", "list2");
        doThrow(SProcessInstanceNotFoundException.class).when(processInstanceService).getProcessInstance(45l);
        exception.expect(SObjectNotFoundException.class);
        exception.expectMessage("Unable to find the list theList, nothing in database and the process instance 45 is not found");
        //when
        DocumentListLeftOperandHandler.isListDefinedInDefinition("theList", 45l, processDefinitionService, processInstanceService);
        //then exception
    }

    @Test
    public void should_isDefinedInDefinition_throw_not_found_when_not_existing_definition() throws Exception {
        initDefinition("list1", "list2");
        doThrow(SProcessDefinitionNotFoundException.class).when(processDefinitionService).getProcessDefinition(154l);
        exception.expect(SObjectNotFoundException.class);
        exception.expectMessage("Unable to find the list theList on process instance 45, nothing in database and the process definition is not found");
        //when
        DocumentListLeftOperandHandler.isListDefinedInDefinition("theList", 45l, processDefinitionService, processInstanceService);
    }

    @Test
    public void should_isDefinedInDefinition_throw_read_ex_when_not_existing_instance() throws Exception {
        initDefinition("list1", "list2");
        doThrow(SProcessInstanceReadException.class).when(processInstanceService).getProcessInstance(45l);
        exception.expect(SBonitaReadException.class);
        DocumentListLeftOperandHandler.isListDefinedInDefinition("theList", 45l, processDefinitionService, processInstanceService);
    }

    @Test
    public void should_isDefinedInDefinition_throw_read_ex_when_not_existing_definition() throws Exception {
        initDefinition("list1", "list2");
        doThrow(SProcessDefinitionReadException.class).when(processDefinitionService).getProcessDefinition(154l);
        exception.expect(SBonitaReadException.class);
        DocumentListLeftOperandHandler.isListDefinedInDefinition("theList", 45, processDefinitionService, processInstanceService);
    }

    @Test
    public void should_isDefinedInDefinition_return_true_if_in_def() throws Exception {
        //given
        initDefinition("list1", "list2", "theList");
        //when then
        assertThat(DocumentListLeftOperandHandler.isListDefinedInDefinition("theList", 45l, processDefinitionService, processInstanceService)).isTrue();
    }

    private void initDefinition(String... names) throws SProcessInstanceNotFoundException, SProcessInstanceReadException, SProcessDefinitionNotFoundException,
            SProcessDefinitionReadException {
        doReturn(processInstance).when(processInstanceService).getProcessInstance(45l);
        doReturn(154l).when(processInstance).getProcessDefinitionId();
        doReturn(processDefinition).when(processDefinitionService).getProcessDefinition(154l);
        doReturn(flowElementContainerDefinition).when(processDefinition).getProcessContainer();
        doReturn(createListOfDocumentListDefinition(names)).when(flowElementContainerDefinition).getDocumentListDefinitions();
    }

    private List<SDocumentListDefinition> createListOfDocumentListDefinition(String... names) {
        List<SDocumentListDefinition> list = new ArrayList<SDocumentListDefinition>();
        for (String name : names) {
            list.add(new SDocumentListDefinitionImpl(name));
        }
        return list;
    }
}
