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
package org.bonitasoft.engine.core.process.instance.model.impl;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;

/**
 * @author Feng Hui
 * @author Celine Souchet
 */
public class SGatewayInstanceImpl extends SFlowNodeInstanceImpl implements SGatewayInstance {

    private static final long serialVersionUID = 1683115469514401404L;

    private SGatewayType gatewayType;

    private String hitBys = "";

    public SGatewayInstanceImpl() {
        super();
    }

    public SGatewayInstanceImpl(final String name, final long flowNodeDefinitionId, final long rootContainerId, final long parentContainerId,
            final SGatewayType gatewayType, final long logicalGroup1, final long logicalGroup2) {
        super(name, flowNodeDefinitionId, rootContainerId, parentContainerId, logicalGroup1, logicalGroup2);
        this.gatewayType = gatewayType;
    }

    public SGatewayInstanceImpl(SGatewayInstance gatewayInstance) {
        super(gatewayInstance.getName(), gatewayInstance.getFlowNodeDefinitionId(), gatewayInstance.getRootContainerId(), gatewayInstance.getParentContainerId(),
                gatewayInstance.getLogicalGroup(0), gatewayInstance.getLogicalGroup(1));
        setLogicalGroup(2, gatewayInstance.getLogicalGroup(2));
        setLogicalGroup(3, gatewayInstance.getLogicalGroup(3));
        this.gatewayType = gatewayInstance.getGatewayType();
        setStateId(gatewayInstance.getStateId());
    }

    @Override
    public SGatewayType getGatewayType() {
        return gatewayType;
    }

    @Override
    public String getHitBys() {
        return hitBys;
    }

    public void setGatewayType(final SGatewayType gatewayType) {
        this.gatewayType = gatewayType;
    }

    public void setHitBys(final String hitBys) {
        this.hitBys = hitBys;
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.GATEWAY;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (gatewayType == null ? 0 : gatewayType.hashCode());
        result = prime * result + (hitBys == null ? 0 : hitBys.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SGatewayInstanceImpl other = (SGatewayInstanceImpl) obj;
        if (gatewayType != other.gatewayType) {
            return false;
        }
        if (hitBys == null) {
            if (other.hitBys != null) {
                return false;
            }
        } else if (!hitBys.equals(other.hitBys)) {
            return false;
        }
        return true;
    }

}
