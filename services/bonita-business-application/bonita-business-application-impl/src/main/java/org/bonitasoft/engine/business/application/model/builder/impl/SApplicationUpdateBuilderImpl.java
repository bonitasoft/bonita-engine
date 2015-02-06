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
package org.bonitasoft.engine.business.application.model.builder.impl;

import org.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Elias Ricken de Medeiros
 * @author Emmanuel Duchastenier
 */
public class SApplicationUpdateBuilderImpl implements SApplicationUpdateBuilder {

    private final EntityUpdateDescriptor descriptor;

    public SApplicationUpdateBuilderImpl(final long updaterUserId) {
        descriptor = new EntityUpdateDescriptor();
        descriptor.addField(SApplicationFields.UPDATED_BY, updaterUserId);
        descriptor.addField(SApplicationFields.LAST_UPDATE_DATE, System.currentTimeMillis());
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public SApplicationUpdateBuilder updateToken(final String token) {
        descriptor.addField(SApplicationFields.TOKEN, token);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateDisplayName(final String displayName) {
        descriptor.addField(SApplicationFields.DISPLAY_NAME, displayName);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateVersion(final String version) {
        descriptor.addField(SApplicationFields.VERSION, version);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateDescription(final String description) {
        descriptor.addField(SApplicationFields.DESCRIPTION, description);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateIconPath(final String iconPath) {
        descriptor.addField(SApplicationFields.ICON_PATH, iconPath);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateState(final String state) {
        descriptor.addField(SApplicationFields.STATE, state);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateProfileId(final Long profileId) {
        descriptor.addField(SApplicationFields.PROFILE_ID, profileId);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateHomePageId(final Long homePageId) {
        descriptor.addField(SApplicationFields.HOME_PAGE_ID, homePageId);
        return this;
    }

}
