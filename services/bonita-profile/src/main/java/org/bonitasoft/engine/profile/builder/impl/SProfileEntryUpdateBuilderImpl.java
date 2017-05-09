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

import org.bonitasoft.engine.profile.builder.SProfileEntryBuilderFactory;
import org.bonitasoft.engine.profile.builder.SProfileEntryUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Celine Souchet
 */
public class SProfileEntryUpdateBuilderImpl implements SProfileEntryUpdateBuilder {

    protected final EntityUpdateDescriptor descriptor;

    public SProfileEntryUpdateBuilderImpl() {
        descriptor = new EntityUpdateDescriptor();
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public SProfileEntryUpdateBuilder setName(final String name) {
        descriptor.addField(SProfileEntryBuilderFactory.NAME, name);
        return this;
    }

    @Override
    public SProfileEntryUpdateBuilder setDescription(final String description) {
        descriptor.addField(SProfileEntryBuilderFactory.DESCRIPTION, description);
        return this;
    }

    @Override
    public SProfileEntryUpdateBuilder setParentId(final long parentId) {
        descriptor.addField(SProfileEntryBuilderFactory.PARENT_ID, parentId);
        return this;
    }

    @Override
    public SProfileEntryUpdateBuilder setProfileId(final long profileId) {
        descriptor.addField(SProfileEntryBuilderFactory.PROFILE_ID, profileId);
        return this;
    }

    @Override
    public SProfileEntryUpdateBuilder setType(final String type) {
        descriptor.addField(SProfileEntryBuilderFactory.TYPE, type);
        return this;
    }

    @Override
    public SProfileEntryUpdateBuilder setPage(final String page) {
        descriptor.addField(SProfileEntryBuilderFactory.PAGE, page);
        return this;
    }

    @Override
    public SProfileEntryUpdateBuilder setIndex(final long index) {
        descriptor.addField(SProfileEntryBuilderFactory.INDEX, index);
        return this;
    }

    @Override
    public SProfileEntryUpdateBuilder setCustom(final boolean custom) {
        descriptor.addField(SProfileEntryBuilderFactory.CUSTOM, custom);
        return this;
    }

}
