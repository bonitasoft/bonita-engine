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
package org.bonitasoft.engine.profile.builder.impl;

import org.bonitasoft.engine.profile.builder.SProfileBuilderFactory;
import org.bonitasoft.engine.profile.builder.SProfileUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Celine Souchet
 */
public class SProfileUpdateBuilderImpl implements SProfileUpdateBuilder {

    protected final EntityUpdateDescriptor descriptor;

    public SProfileUpdateBuilderImpl(final EntityUpdateDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public SProfileUpdateBuilder setName(final String name) {
        descriptor.addField(SProfileBuilderFactory.NAME, name);
        return this;
    }

    @Override
    public SProfileUpdateBuilder setDescription(final String description) {
        descriptor.addField(SProfileBuilderFactory.DESCRIPTION, description);
        return this;
    }

    @Override
    public SProfileUpdateBuilder setIconPath(final String iconPath) {
        descriptor.addField(SProfileBuilderFactory.ICON_PATH, iconPath);
        return this;
    }

    @Override
    public SProfileUpdateBuilder setLastUpdateDate(final long lastUpdateDate) {
        descriptor.addField(SProfileBuilderFactory.LAST_UPDATE_DATE, lastUpdateDate);
        return this;
    }

    @Override
    public SProfileUpdateBuilder setLastUpdatedBy(final long lastUpdatedBy) {
        descriptor.addField(SProfileBuilderFactory.LAST_UPDATED_BY, lastUpdatedBy);
        return this;
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

}
