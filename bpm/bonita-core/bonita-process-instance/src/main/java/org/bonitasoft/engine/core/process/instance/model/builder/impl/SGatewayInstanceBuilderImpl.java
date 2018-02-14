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
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SGatewayInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.impl.SGatewayInstanceImpl;

/**
 * @author Feng Hui
 * @author Celine Souchet
 */
public class SGatewayInstanceBuilderImpl extends SFlowNodeInstanceBuilderImpl implements SGatewayInstanceBuilder {

    public SGatewayInstanceBuilderImpl(final SGatewayInstanceImpl entity) {
        super(entity);
    }

    @Override
    public SGatewayInstanceBuilder setStateId(final int stateId) {
        entity.setStateId(stateId);
        return this;
    }

    @Override
    public SGatewayInstanceBuilder setGatewayType(final SGatewayType gatewayType) {
        ((SGatewayInstanceImpl) entity).setGatewayType(gatewayType);
        return this;
    }

    @Override
    public SGatewayInstanceBuilder setHitBys(final String hitBys) {
        ((SGatewayInstanceImpl) entity).setHitBys(hitBys);
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
    public SGatewayInstance done() {
        return (SGatewayInstance) entity;
    }

    @Override
    public SGatewayInstanceBuilder setParentActivityInstanceId(final long parentActivityInstanceId) {
        entity.setLogicalGroup(SGatewayInstanceBuilderFactoryImpl.PARENT_ACTIVITY_INSTANCE_INDEX, parentActivityInstanceId);
        return this;
    }

    @Override
    public SGatewayInstanceBuilder setProcessInstanceId(final long processInstanceId) {
        entity.setLogicalGroup(SGatewayInstanceBuilderFactoryImpl.ROOT_PROCESS_INSTANCE_INDEX, processInstanceId);
        return this;
    }

}
