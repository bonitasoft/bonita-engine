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
package org.bonitasoft.engine.execution.flowmerger;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SGatewayDefinition;
import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.definition.model.SSubProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SCatchEventDefinition;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SFlowNodeWrapper {

    private SFlowNodeDefinition flowNode;

    public SFlowNodeWrapper(final SFlowNodeDefinition flowNode) {
        this.flowNode = flowNode;
    }

    public boolean isParalleleOrInclusive() {
        if (!isGateway()) {
            return false;
        }
        final SGatewayType gatewayType = ((SGatewayDefinition) flowNode).getGatewayType();
        return SGatewayType.PARALLEL.equals(gatewayType) || SGatewayType.INCLUSIVE.equals(gatewayType);
    }

    
    public boolean isExclusive() {
        if (!isGateway()) {
            return false;
        }
        final SGatewayType gatewayType = ((SGatewayDefinition) flowNode).getGatewayType();
        return SGatewayType.EXCLUSIVE.equals(gatewayType);
    }

    private boolean isGateway() {
        if(isNull()) {
            return false;
        }
        return SFlowNodeType.GATEWAY.equals(flowNode.getType());
    }

    public SFlowNodeDefinition getFlowNode() {
        return flowNode;
    }
    
    
    public boolean isBoundaryEvent() {
        if(isNull()) {
            return false;
        }
        return SFlowNodeType.BOUNDARY_EVENT.equals(flowNode.getType());
    }
    
    public boolean isInterrupting() {
        if(isNull() || !(flowNode instanceof SCatchEventDefinition)) {
            return false;
        }
        return ((SCatchEventDefinition) flowNode).isInterrupting();
    }
    
    public boolean isNull() {
        return flowNode == null;
    }
    
    public boolean hasIncomingTransitions() {
        return flowNode.hasIncomingTransitions();
    }

    public boolean isEventSubProcess() {
        if(isNull()) {
            return false;
        }
        return SFlowNodeType.SUB_PROCESS.equals(flowNode.getType()) && ((SSubProcessDefinition) flowNode).isTriggeredByEvent();
    }

}
