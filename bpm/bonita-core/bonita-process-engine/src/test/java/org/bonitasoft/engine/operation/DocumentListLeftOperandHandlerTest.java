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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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

    @InjectMocks
    private DocumentListLeftOperandHandler handler;

    @Before
    public void setUp() {

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
    public void should_toCheckedList_returns_the_list_if_ok() throws Exception {
        final List<DocumentValue> inputList = Arrays.asList(new DocumentValue("theUrl"));
        final List<DocumentValue> result = handler.toCheckedList(inputList);
        assertThat(result).isEqualTo(inputList);
    }

}
