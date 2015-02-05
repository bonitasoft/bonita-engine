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
package org.bonitasoft.engine.core.process.definition.model.impl;

import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.GatewayDefinition;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SGatewayDefinition;
import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;

/**
 * @author Feng Hui
 * @author Zhao Na
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SGatewayDefinitionImpl extends SFlowNodeDefinitionImpl implements SGatewayDefinition {

    private static final long serialVersionUID = -5765195373419051716L;

    private final SGatewayType gatewayType;

    public SGatewayDefinitionImpl(final GatewayDefinition gatewayDefinition,
            final Map<String, STransitionDefinition> transitionsMap) {
        super(gatewayDefinition, transitionsMap);
        final GatewayType type = gatewayDefinition.getGatewayType();
        gatewayType = SGatewayType.valueOf(type.toString());
    }

    public SGatewayDefinitionImpl(final long id, final String name, final SGatewayType gatewayType) {
        super(id, name);
        this.gatewayType = gatewayType;
    }

    @Override
    public SGatewayType getGatewayType() {
        return gatewayType;
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.GATEWAY;
    }

    @Override
    public boolean isParalleleOrInclusive() {
        final SGatewayType gatewayType = ((SGatewayDefinition) this).getGatewayType();
        return SGatewayType.PARALLEL.equals(gatewayType) || SGatewayType.INCLUSIVE.equals(gatewayType);
    }

    @Override
    public boolean isExclusive() {
        final SGatewayType gatewayType = ((SGatewayDefinition) this).getGatewayType();
        return SGatewayType.EXCLUSIVE.equals(gatewayType);
    }

}
