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
import org.bonitasoft.engine.core.process.instance.model.builder.SGatewayInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SGatewayInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.impl.SGatewayInstanceImpl;

/**
 * @author Feng Hui
 * @author Celine Souchet
 */
public class SGatewayInstanceBuilderFactoryImpl extends SFlowNodeInstanceBuilderFactoryImpl implements SGatewayInstanceBuilderFactory {

    private static final String ID_KEY = "id";

    private static final String NAME_KEY = "name";

    private static final String STATE_ID_KEY = "stateId";

    private static final String GATEWAY_TYPE_KEY = "gatewayType";

    private static final String HITBYS = "hitBys";

    @Override
    public SGatewayInstanceBuilder createNewInstance(final String name, final long flowNodeDefinitionId, final long rootContainerId,
            final long parentContainerId, final SGatewayType gatewayType, final long processDefinitionId, final long rootProcessInstanceId,
            final long parentProcessInstanceId) {
        final SGatewayInstanceImpl entity = new SGatewayInstanceImpl(name, flowNodeDefinitionId, rootContainerId, parentContainerId, gatewayType, processDefinitionId,
                rootProcessInstanceId);
        entity.setLogicalGroup(PARENT_PROCESS_INSTANCE_INDEX, parentProcessInstanceId);
        return new SGatewayInstanceBuilderImpl(entity);
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

}
