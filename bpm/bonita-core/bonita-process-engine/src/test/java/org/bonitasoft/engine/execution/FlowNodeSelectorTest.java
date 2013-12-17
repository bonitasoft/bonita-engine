/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;



/**
 * @author Elias Ricken de Medeiros
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowNodeSelectorTest {
    
    private SFlowNodeDefinition creatFlowNode(String name) {
        SFlowNodeDefinition flowNodeDefinition = mock(SFlowNodeDefinition.class);
        doReturn(name).when(flowNodeDefinition).getName();
        return flowNodeDefinition;
    }
    
    @Test
    public void getStartNodes_return_all_selected_elements() throws Exception {
        List<SFlowNodeDefinition> flowNodes = Arrays.asList(creatFlowNode("step1"), creatFlowNode("step2"), creatFlowNode("step3"));
        FlowNodeSelector flowNodeSelector = new FlowNodeSelector(flowNodes, new FlowNodeNameFilter(Arrays.asList("step1", "step3")));
        assertEquals("[step1, step3]", stringfy(flowNodeSelector.getFilteredElements()));
    }

    private String stringfy(List<SFlowNodeDefinition> elements) {
        List<String> elementNames = new ArrayList<String>();
        for (SFlowNodeDefinition sFlowNodeDefinition : elements) {
            elementNames.add(sFlowNodeDefinition.getName());
        }
        return elementNames.toString();
    }
    
    
            
}
