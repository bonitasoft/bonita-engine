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

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSubProcessDefinition;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class FlowNodeSelector {

    
    private final Filter<SFlowNodeDefinition> selector;
    private final SProcessDefinition definition;
    private long subProcessDefinitionId = -1;

    public FlowNodeSelector(SProcessDefinition definition, Filter<SFlowNodeDefinition> filter) {
        this.definition = definition;
        this.selector = filter;
    }

    public FlowNodeSelector(SProcessDefinition definition, Filter<SFlowNodeDefinition> filter, final long subProcessDefinitionId) {
        this(definition, filter);
        this.subProcessDefinitionId  = subProcessDefinitionId;
    }
    
    public List<SFlowNodeDefinition> getFilteredElements() {
        SFlowElementContainerDefinition container = getContainer();
        ArrayList<SFlowNodeDefinition> selectedFlowNodes = new ArrayList<SFlowNodeDefinition>();
        for (SFlowNodeDefinition flowNodeDefinition : container.getFlowNodes()) {
            if(selector.mustSelect(flowNodeDefinition)) {
                selectedFlowNodes.add(flowNodeDefinition);
            }
        }
        return selectedFlowNodes;
    }

    public SFlowElementContainerDefinition getContainer() {
        if(subProcessDefinitionId == -1) {
            return definition.getProcessContainer();
        }
        final SSubProcessDefinition subProcDef = (SSubProcessDefinition) definition.getProcessContainer().getFlowNode(subProcessDefinitionId);
        return subProcDef.getSubProcessContainer();
    }
    
    
    public SProcessDefinition getProcessDefinition() {
        return definition;
    }
    
    
    public long getSubProcessDefinitionId() {
        return subProcessDefinitionId;
    }

    public Filter<SFlowNodeDefinition> getSelector() {
        return selector;
    }
}
