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

import org.bonitasoft.engine.profile.builder.SProfileEntryBuilder;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.profile.model.impl.SProfileEntryImpl;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SProfileEntryBuilderImpl implements SProfileEntryBuilder {

    private final SProfileEntryImpl profileEntry;

    public SProfileEntryBuilderImpl(final SProfileEntryImpl profileEntry) {
        super();
        this.profileEntry = profileEntry;
    }

    @Override
    public SProfileEntryBuilder setId(final long id) {
        profileEntry.setId(id);
        return this;
    }

    @Override
    public SProfileEntryBuilder setDescription(final String description) {
        profileEntry.setDescription(description);
        return this;
    }

    @Override
    public SProfileEntryBuilder setParentId(final long parentId) {
        profileEntry.setParentId(parentId);
        return this;
    }

    @Override
    public SProfileEntryBuilder setType(final String type) {
        profileEntry.setType(type);
        return this;
    }

    @Override
    public SProfileEntryBuilder setPage(final String page) {
        profileEntry.setPage(page);
        return this;
    }

    @Override
    public SProfileEntryBuilder setIndex(final long index) {
        profileEntry.setIndex(index);
        return this;
    }

    @Override
    public SProfileEntryBuilder setCustom(final Boolean custom) {
        profileEntry.setCustom(custom);
        return this;
    }

    @Override
    public SProfileEntry done() {
        return profileEntry;
    }

}
