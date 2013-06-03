/**
 * Copyright (C) 20112 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SGatewayInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.impl.SFlowNodeInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SGatewayInstanceImpl;

/**
 * @author Feng Hui
 * @author Celine Souchet
 */
public class SGatewayInstanceBuilderImpl extends SFlowNodeInstanceBuilderImpl implements SGatewayInstanceBuilder {

    private static final String ID_KEY = "id";

    private static final String NAME_KEY = "name";

    private static final String STATE_ID_KEY = "stateId";

    private static final String GATEWAY_TYPE_KEY = "gatewayType";

    private static final String HITBYS = "hitBys";

    private SGatewayInstanceImpl entity;

    @Override
    public SGatewayInstanceBuilder createNewInstance(final String name, final long flowNodeDefinitionId, final long rootContainerId,
            final long parentContainerId, final SGatewayType gatewayType, final long processDefinitionId, final long rootProcessInstanceId,
            final long parentProcessInstanceId) {
        entity = new SGatewayInstanceImpl(name, flowNodeDefinitionId, rootContainerId, parentContainerId, gatewayType, processDefinitionId,
                rootProcessInstanceId);
        entity.setLogicalGroup(PARENT_PROCESS_INSTANCE_INDEX, parentProcessInstanceId);
        return this;
    }

    @Override
    public SGatewayInstanceBuilder setStateId(final int stateId) {
        entity.setStateId(stateId);
        return this;
    }

    @Override
    public SGatewayInstanceBuilder setGatewayType(final SGatewayType gatewayType) {
        entity.setGatewayType(gatewayType);
        return this;
    }

    @Override
    public SGatewayInstanceBuilder setHitBys(final String hitBys) {
        entity.setHitBys(hitBys);
        return this;
    }

    @Override
    public SGatewayInstanceBuilder setParentContainerId(final long containerId) {
        entity.setParentContainerId(containerId);
        return this;
    }

    @Override
    public SGatewayInstanceBuilder setRootContainerId(final long containerId) {
        entity.setRootContainerId(containerId);
        return this;
    }

    @Override
    public SGatewayInstanceBuilder setProcessDefinitionId(final long processDefinitionId) {
        entity.setLogicalGroup(PROCESS_DEFINITION_INDEX, processDefinitionId);
        return this;
    }

    @Override
    public SGatewayInstanceBuilder setProcessInstanceId(final long processInstanceId) {
        entity.setLogicalGroup(ROOT_PROCESS_INSTANCE_INDEX, processInstanceId);
        return this;
    }

    @Override
    public SGatewayInstance done() {
        return entity;
    }

    @Override
    public String getIdKey() {
        return ID_KEY;
    }

    @Override
    public String getNameKey() {
        return NAME_KEY;
    }

    @Override
    public String getStateIdKey() {
        return STATE_ID_KEY;
    }

    @Override
    public String getGatewayTypeKey() {
        return GATEWAY_TYPE_KEY;
    }

    @Override
    public String getHitBysKey() {
        return HITBYS;
    }

    @Override
    public SGatewayInstanceBuilder setParentActivityInstanceId(final long logicalGroup3) {
        entity.setLogicalGroup(2, logicalGroup3);
        return this;
    }

    @Override
    protected SFlowNodeInstanceImpl getEntity() {
        return entity;
    }

}
