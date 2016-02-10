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
package org.bonitasoft.engine.core.process.instance.model.archive.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAActivityInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAActivityInstanceImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public abstract class SAActivityInstanceBuilderImpl extends SAFlowNodeInstanceBuilderImpl implements SAActivityInstanceBuilder {

    protected SAActivityInstanceBuilderImpl(final SAActivityInstanceImpl entity) {
        super(entity);
    }

    @Override
    public SAActivityInstanceBuilder setParentActivityInstanceId(final long parentActivityInstanceId) {
        entity.setLogicalGroup(SAActivityInstanceBuilderFactoryImpl.PROCESS_DEFINITION_INDEX, parentActivityInstanceId);
        return this;
    }

    @Override
    public SAActivityInstanceBuilder setProcessDefinitionId(final long processDefinitionId) {
        entity.setLogicalGroup(SAActivityInstanceBuilderFactoryImpl.PROCESS_DEFINITION_INDEX, processDefinitionId);
        return this;
    }

    @Override
    public SAActivityInstanceBuilder setRootProcessInstanceId(final long processInstanceId) {
        entity.setLogicalGroup(SAActivityInstanceBuilderFactoryImpl.ROOT_PROCESS_INSTANCE_INDEX, processInstanceId);
        return this;
    }

}
