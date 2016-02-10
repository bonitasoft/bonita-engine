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

import org.bonitasoft.engine.core.process.instance.model.builder.SSubProcessActivityInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SSubProcessActivityInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.impl.SSubProcessActivityInstanceImpl;

/**
 * @author Celine Souchet
 * @author Elias Ricken de Medeiros
 */
public class SSubProcessActivityInstanceBuilderFactoryImpl extends SActivityInstanceBuilderFactoryImpl implements SSubProcessActivityInstanceBuilderFactory {

    @Override
    public SSubProcessActivityInstanceBuilder createNewSubProcessActivityInstance(final String name, final long flowNodeDefinitionId,
            final long rootContainerId, final long parentContainerId, final long processDefinitionId, final long rootProcessInstanceId,
            final long parentProcessInstanceId, final boolean isTriggeredByEvent) {
        final SSubProcessActivityInstanceImpl entity = new SSubProcessActivityInstanceImpl(name, flowNodeDefinitionId, rootContainerId, parentContainerId, processDefinitionId,
                rootProcessInstanceId, isTriggeredByEvent);
        entity.setLogicalGroup(PARENT_PROCESS_INSTANCE_INDEX, parentProcessInstanceId);
        entity.setTokenCount(1);
        return new SSubProcessActivityInstanceBuilderImpl(entity);
    }

    @Override
    public String getTriggeredByEventKey() {
        return "triggeredByEvent";
    }

}
