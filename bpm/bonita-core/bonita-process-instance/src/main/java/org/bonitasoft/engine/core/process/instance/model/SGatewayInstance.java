/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.core.process.instance.model;

import javax.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;

/**
 * @author Feng Hui
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("gate")
public class SGatewayInstance extends SFlowNodeInstance {

    @Column
    @Enumerated(EnumType.STRING)
    private SGatewayType gatewayType;
    private String hitBys = "";

    public SGatewayInstance(final String name, final long flowNodeDefinitionId, final long rootContainerId,
            final long parentContainerId,
            final SGatewayType gatewayType, final long logicalGroup1, final long logicalGroup2) {
        super(name, flowNodeDefinitionId, rootContainerId, parentContainerId, logicalGroup1, logicalGroup2);
        this.gatewayType = gatewayType;
    }

    public SGatewayInstance(SGatewayInstance gatewayInstance) {
        super(gatewayInstance.getName(), gatewayInstance.getFlowNodeDefinitionId(),
                gatewayInstance.getRootContainerId(),
                gatewayInstance.getParentContainerId(),
                gatewayInstance.getLogicalGroup(0), gatewayInstance.getLogicalGroup(1));
        setLogicalGroup(2, gatewayInstance.getLogicalGroup(2));
        setLogicalGroup(3, gatewayInstance.getLogicalGroup(3));
        this.gatewayType = gatewayInstance.getGatewayType();
        setStateId(gatewayInstance.getStateId());
    }

    public boolean isFinished() {
        return hitBys != null && hitBys.startsWith(GatewayInstanceService.FINISH);
    }

    public SFlowNodeType getType() {
        return SFlowNodeType.GATEWAY;
    }

    @Override
    public boolean mustExecuteOnAbortOrCancelProcess() {
        //always call execute when abort the gateway because the gateway that merge wait for the flow node in an unstable state
        // this fact is a little strange but a full cleanup of the flownode execution mechanism should be done in order to change that
        return true;
    }

}
