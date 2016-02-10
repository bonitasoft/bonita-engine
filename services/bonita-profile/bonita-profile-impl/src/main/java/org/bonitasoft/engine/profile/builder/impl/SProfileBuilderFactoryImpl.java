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
import org.bonitasoft.engine.profile.builder.SProfileBuilderFactory;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.impl.SProfileImpl;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SProfileBuilderFactoryImpl implements SProfileBuilderFactory {

    @Override
    public SProfileBuilder createNewInstance(final SProfile originalProfile) {
        final SProfileImpl profile = new SProfileImpl(originalProfile);
        return new SProfileBuilderImpl(profile);
    }

    @Override
    public SProfileBuilder createNewInstance(final String name, final boolean isDefault, final long creationDate, final long createdBy,
            final long lastUpdateDate, final long lastUpdatedBy) {
        final SProfileImpl profile = new SProfileImpl();
        profile.setName(name);
        profile.setDefault(isDefault);
        profile.setCreationDate(creationDate);
        profile.setCreatedBy(createdBy);
        profile.setLastUpdateDate(lastUpdateDate);
        profile.setLastUpdatedBy(lastUpdatedBy);
        return new SProfileBuilderImpl(profile);
    }

}
