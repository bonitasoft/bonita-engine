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

import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.builder.SHumanTaskInstanceBuilder;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public abstract class SHumanTaskInstanceBuilderImpl extends SActivityInstanceBuilderImpl
        implements SHumanTaskInstanceBuilder {

    protected SHumanTaskInstanceBuilderImpl(final SHumanTaskInstance entity) {
        super(entity);
    }

    @Override
    public SHumanTaskInstanceBuilder setAssigneeId(final long assigneeId) {
        ((SHumanTaskInstance) this.entity).setAssigneeId(assigneeId);
        ((SHumanTaskInstance) this.entity).setClaimedDate(System.currentTimeMillis());
        return this;
    }

    @Override
    public SHumanTaskInstanceBuilder setPriority(final STaskPriority priority) {
        ((SHumanTaskInstance) this.entity).setPriority(priority);
        return this;
    }

    @Override
    public SHumanTaskInstanceBuilder setExpectedEndDate(final Long expectedEndDate) {
        ((SHumanTaskInstance) this.entity).setExpectedEndDate(expectedEndDate);
        return this;
    }

    @Override
    public SHumanTaskInstanceBuilder setDisplayDescription(final String displayDescription) {
        this.entity.setDisplayDescription(displayDescription);
        return this;
    }

    @Override
    public SHumanTaskInstanceBuilder setDisplayName(final String displayName) {
        this.entity.setDisplayName(displayName);
        return this;
    }

}
