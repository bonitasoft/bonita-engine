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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSubProcessDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowNodeSelectorTest {

    /**
     * 
     */
    private static final long SUB_PROCESS_DEFINITION_ID = 10L;

    @Mock
    private SFlowElementContainerDefinition rootContainer;

    @Mock
    private SFlowElementContainerDefinition subProcessContainer;

    @Mock
    private SProcessDefinition definition;

    @Mock
    private SSubProcessDefinition subProcessDefinition;

    @Before
    public void setUp() {
        doReturn(rootContainer).when(definition).getProcessContainer();
        doReturn(subProcessDefinition).when(rootContainer).getFlowNode(SUB_PROCESS_DEFINITION_ID);
        doReturn(subProcessContainer).when(subProcessDefinition).getSubProcessContainer();

        Set<SFlowNodeDefinition> flowNodes = new HashSet<SFlowNodeDefinition>(Arrays.asList(creatFlowNode("step1"), creatFlowNode("step2"),
                creatFlowNode("step3")));
        doReturn(flowNodes).when(rootContainer).getFlowNodes();
    }

    @Test
    public void getContainer_return_root_container_if_subprocess_id_is_not_set() {
        FlowNodeSelector selector = new FlowNodeSelector(definition, null);
        assertEquals(rootContainer, selector.getContainer());
    }

    @Test
    public void getContainer_return_subprocess_container_if_subprocess_id_is_set() {
        FlowNodeSelector selector = new FlowNodeSelector(definition, null, SUB_PROCESS_DEFINITION_ID);
        assertEquals(subProcessContainer, selector.getContainer());
    }

    private SFlowNodeDefinition creatFlowNode(String name) {
        SFlowNodeDefinition flowNodeDefinition = mock(SFlowNodeDefinition.class);
        doReturn(name).when(flowNodeDefinition).getName();
        return flowNodeDefinition;
    }

    @Test
    public void getStartNodes_return_all_selected_elements() {
        FlowNodeSelector flowNodeSelector = new FlowNodeSelector(definition, new FlowNodeNameFilter(Arrays.asList("step1", "step3")));
        assertEquals("[step1, step3]", stringfy(flowNodeSelector.getFilteredElements()));
    }

    private String stringfy(List<SFlowNodeDefinition> elements) {
        List<String> elementNames = new ArrayList<String>();
        for (SFlowNodeDefinition sFlowNodeDefinition : elements) {
            elementNames.add(sFlowNodeDefinition.getName());
        }
        Collections.sort(elementNames);
        return elementNames.toString();
    }

    @Test
    public void get_process_definition_returns_process_definition_given_in_constructor() {
        FlowNodeSelector selector = new FlowNodeSelector(definition, null);
        assertEquals(definition, selector.getProcessDefinition());
    }

    @Test
    public void get_subProcess_definition_returns_the_id_given_in_constructor() {
        FlowNodeSelector selector = new FlowNodeSelector(null, null, SUB_PROCESS_DEFINITION_ID);
        assertEquals(SUB_PROCESS_DEFINITION_ID, selector.getSubProcessDefinitionId());
    }

}
