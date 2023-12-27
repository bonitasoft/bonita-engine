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
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.SLoopActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SLoopActivityInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SLoopActivityInstanceBuilderFactory;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SLoopActivityInstanceBuilderFactoryImpl extends SActivityInstanceBuilderFactoryImpl
        implements SLoopActivityInstanceBuilderFactory {

    @Override
    public SLoopActivityInstanceBuilder createNewOuterTaskInstance(final String name, final long flowNodeDefinitionId,
            final long rootContainerId,
            final long parentContainerId, final long processDefinitionId, final long rootProcessInstanceId,
            final long parentProcessInstanceId) {
        final SLoopActivityInstance activityInstanceImpl = new SLoopActivityInstance(name, flowNodeDefinitionId,
                rootContainerId, parentContainerId, processDefinitionId,
                rootProcessInstanceId);
        activityInstanceImpl.setLogicalGroup(PARENT_PROCESS_INSTANCE_INDEX, parentProcessInstanceId);
        return new SLoopActivityInstanceBuilderImpl(activityInstanceImpl);
    }

}
