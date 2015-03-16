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
package org.bonitasoft.engine.execution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SGatewayDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSubProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SUserTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SBoundaryEventDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 */
@RunWith(MockitoJUnitRunner.class)
public class AdvancedStartProcessValidatorTest {

    private static final String PROCESS_VERSION = "1.0";

    private static final String PROCESS_NAME = "My process";

    private static final String GATEWAY_NAME = "gateway";

    private static final String SUBPROCESS_NAME = "subprocess";

    private static final String BOUNDARY_NAME = "boundary";

    private static final String USERTASK_NAME = "usertask";

    private final long PROCESS_DEFINITION_ID = 1234L;

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Mock
    private SProcessDefinition procDef;

    @Mock
    private SFlowElementContainerDefinition container;

    @Mock
    private SGatewayDefinition gateway;

    @Mock
    private SSubProcessDefinition subProcess;

    @Mock
    private SBoundaryEventDefinition boundary;

    @Mock
    private SUserTaskDefinition userTask;

    private AdvancedStartProcessValidator starterValidator;

    @Before
    public void setUp() throws Exception {
        starterValidator = new AdvancedStartProcessValidator(processDefinitionService, PROCESS_DEFINITION_ID);
        Set<SFlowNodeDefinition> flowNodes = getFlowNodes();
        doReturn(procDef).when(processDefinitionService).getProcessDefinition(PROCESS_DEFINITION_ID);
        doReturn(container).when(procDef).getProcessContainer();
        doReturn(PROCESS_NAME).when(procDef).getName();
        doReturn(PROCESS_VERSION).when(procDef).getVersion();
        doReturn(flowNodes).when(container).getFlowNodes();

        doReturn(SFlowNodeType.GATEWAY).when(gateway).getType();
        doReturn(SFlowNodeType.SUB_PROCESS).when(subProcess).getType();
        doReturn(SFlowNodeType.BOUNDARY_EVENT).when(boundary).getType();
        doReturn(SFlowNodeType.USER_TASK).when(userTask).getType();

        doReturn(GATEWAY_NAME).when(gateway).getName();
        doReturn(SUBPROCESS_NAME).when(subProcess).getName();
        doReturn(BOUNDARY_NAME).when(boundary).getName();
        doReturn(USERTASK_NAME).when(userTask).getName();
    }

    private Set<SFlowNodeDefinition> getFlowNodes() {
        Set<SFlowNodeDefinition> flowNodes = new HashSet<SFlowNodeDefinition>(4);
        flowNodes.add(gateway);
        flowNodes.add(subProcess);
        flowNodes.add(boundary);
        flowNodes.add(userTask);
        return flowNodes;
    }

    @Test
    public void validate_return_problem_if_one_of_chosen_flownode_is_a_boundary() throws Exception {
        List<String> problems = starterValidator.validate(Collections.singletonList(BOUNDARY_NAME));
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains(BOUNDARY_NAME));
    }

    @Test
    public void validate_return_problem_if_one_of_chosen_flownode_is_a_subprocess() throws Exception {
        List<String> problems = starterValidator.validate(Collections.singletonList(SUBPROCESS_NAME));
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains(SUBPROCESS_NAME));
    }

    @Test
    public void validate_return_problem_if_one_of_chosen_flownode_is_a_gateway() throws Exception {
        List<String> problems = starterValidator.validate(Collections.singletonList(GATEWAY_NAME));
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains(GATEWAY_NAME));
    }

    @Test
    public void validate_return_empty_list_if_is_valid() throws Exception {
        List<String> problems = starterValidator.validate(Collections.singletonList(USERTASK_NAME));
        assertEquals(0, problems.size());
    }

    @Test
    public void validate_return_problem_if_one_element_is_not_found() throws Exception {
        List<String> problems = starterValidator.validate(Collections.singletonList("unknowntask"));
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("unknowntask"));
    }

    @Test
    public void validate_return_problem_if_the_flowNode_name_list_is_empty() throws Exception {
        List<String> problems = starterValidator.validate(Collections.<String>emptyList());
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("empty"));
    }

}
