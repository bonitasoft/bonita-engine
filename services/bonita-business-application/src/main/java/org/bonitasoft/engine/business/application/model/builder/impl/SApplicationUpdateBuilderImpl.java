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
package org.bonitasoft.engine.business.application.model.builder.impl;

import org.bonitasoft.engine.business.application.model.SApplication;
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
        descriptor.addField(SApplication.UPDATED_BY, updaterUserId);
        descriptor.addField(SApplication.LAST_UPDATE_DATE, System.currentTimeMillis());
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public SApplicationUpdateBuilder updateToken(final String token) {
        descriptor.addField(SApplication.TOKEN, token);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateDisplayName(final String displayName) {
        descriptor.addField(SApplication.DISPLAY_NAME, displayName);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateVersion(final String version) {
        descriptor.addField(SApplication.VERSION, version);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateDescription(final String description) {
        descriptor.addField(SApplication.DESCRIPTION, description);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateIconPath(final String iconPath) {
        descriptor.addField(SApplication.ICON_PATH, iconPath);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateState(final String state) {
        descriptor.addField(SApplication.STATE, state);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateProfileId(final Long profileId) {
        descriptor.addField(SApplication.PROFILE_ID, profileId);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateHomePageId(final Long homePageId) {
        descriptor.addField(SApplication.HOME_PAGE_ID, homePageId);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateLayoutId(final Long layoutId) {
        descriptor.addField(SApplication.LAYOUT_ID, layoutId);
        return this;
    }

    @Override
    public SApplicationUpdateBuilder updateThemeId(final Long themeId) {
        descriptor.addField(SApplication.THEME_ID, themeId);
        return this;
    }
}
