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

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.impl.SProcessInstanceImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SProcessInstanceBuilderImpl implements SProcessInstanceBuilder {

    protected final SProcessInstanceImpl entity;

    public SProcessInstanceBuilderImpl(final SProcessInstanceImpl entity) {
        super();
        this.entity = entity;
    }

    @Override
    public SProcessInstance done() {
        return entity;
    }

    @Override
    public SProcessInstanceBuilder setName(final String name) {
        entity.setName(name);
        return this;
    }

    @Override
    public SProcessInstanceBuilder setDescription(final String description) {
        entity.setDescription(description);
        return this;
    }

    @Override
    public SProcessInstanceBuilder setStartedBy(final long startedBy) {
        entity.setStartedBy(startedBy);
        return this;
    }

    @Override
    public SProcessInstanceBuilder setStartedBySubstitute(final long startedBySubstitute) {
        entity.setStartedBySubstitute(startedBySubstitute);
        return this;
    }

    @Deprecated
    @Override
    public SProcessInstanceBuilder setContainerId(final long id) {
        entity.setContainerId(id);
        return this;
    }

    @Override
    public SProcessInstanceBuilder setCallerId(final long callerId, final SFlowNodeType callerType) {
        entity.setCallerId(callerId);
        entity.setCallerType(callerType);
        return this;
    }

    @Override
    public SProcessInstanceBuilder setMigrationPlanId(final long migrationPlanId) {
        entity.setMigrationPlanId(migrationPlanId);
        return this;
    }

    @Override
    public SProcessInstanceBuilder setRootProcessInstanceId(final long rootProcessInstanceId) {
        entity.setRootProcessInstanceId(rootProcessInstanceId);
        return this;
    }

}
