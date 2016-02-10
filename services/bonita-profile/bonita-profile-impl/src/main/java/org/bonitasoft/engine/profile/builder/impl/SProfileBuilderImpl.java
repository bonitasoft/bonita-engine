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

import org.bonitasoft.engine.profile.builder.SProfileBuilder;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.impl.SProfileImpl;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SProfileBuilderImpl implements SProfileBuilder {

    private final SProfileImpl profile;

    public SProfileBuilderImpl(final SProfileImpl profile) {
        super();
        this.profile = profile;
    }

    @Override
    public SProfileBuilder setId(final long id) {
        profile.setId(id);
        return this;
    }

    @Override
    public SProfileBuilder setDefault(final boolean isDefault) {
        profile.setDefault(isDefault);
        return this;
    }

    @Override
    public SProfileBuilder setDescription(final String description) {
        profile.setDescription(description);
        return this;
    }

    @Override
    public SProfileBuilder setCreationDate(final long creationDate) {
        profile.setCreationDate(creationDate);
        return this;
    }

    @Override
    public SProfileBuilder setCreatedBy(final long createdBy) {
        profile.setCreatedBy(createdBy);
        return this;
    }

    @Override
    public SProfileBuilder setLastUpdateDate(final long lastUpdateDate) {
        profile.setLastUpdateDate(lastUpdateDate);
        return this;
    }

    @Override
    public SProfileBuilder setLastUpdatedBy(final long lastUpdatedBy) {
        profile.setLastUpdatedBy(lastUpdatedBy);
        return this;
    }

    @Override
    public SProfile done() {
        return profile;
    }

}
