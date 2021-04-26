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
package org.bonitasoft.engine.business.application.model.builder;

import org.bonitasoft.engine.business.application.model.AbstractSApplication;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

public class SApplicationUpdateBuilder {

    protected final EntityUpdateDescriptor descriptor;

    public SApplicationUpdateBuilder(final long updaterUserId) {
        descriptor = new EntityUpdateDescriptor();
        descriptor.addField(AbstractSApplication.UPDATED_BY, updaterUserId);
        descriptor.addField(AbstractSApplication.LAST_UPDATE_DATE, System.currentTimeMillis());
    }

    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    public SApplicationUpdateBuilder updateToken(final String token) {
        descriptor.addField(AbstractSApplication.TOKEN, token);
        return this;
    }

    public SApplicationUpdateBuilder updateDisplayName(final String displayName) {
        descriptor.addField(AbstractSApplication.DISPLAY_NAME, displayName);
        return this;
    }

    public SApplicationUpdateBuilder updateVersion(final String version) {
        descriptor.addField(AbstractSApplication.VERSION, version);
        return this;
    }

    public SApplicationUpdateBuilder updateDescription(final String description) {
        descriptor.addField(AbstractSApplication.DESCRIPTION, description);
        return this;
    }

    public SApplicationUpdateBuilder updateIconPath(final String iconPath) {
        descriptor.addField(AbstractSApplication.ICON_PATH, iconPath);
        return this;
    }

    public SApplicationUpdateBuilder updateState(final String state) {
        descriptor.addField(AbstractSApplication.STATE, state);
        return this;
    }

    public SApplicationUpdateBuilder updateProfileId(final Long profileId) {
        descriptor.addField(AbstractSApplication.PROFILE_ID, profileId);
        return this;
    }

    public SApplicationUpdateBuilder updateHomePageId(final Long homePageId) {
        descriptor.addField(AbstractSApplication.HOME_PAGE_ID, homePageId);
        return this;
    }

    public SApplicationUpdateBuilder updateLayoutId(final Long layoutId) {
        descriptor.addField(AbstractSApplication.LAYOUT_ID, layoutId);
        return this;
    }

    public SApplicationUpdateBuilder updateThemeId(final Long themeId) {
        descriptor.addField(AbstractSApplication.THEME_ID, themeId);
        return this;
    }

    public SApplicationUpdateBuilder updateIconMimeType(String mimeType) {
        descriptor.addField(AbstractSApplication.ICON_MIME_TYPE, mimeType);
        return this;
    }

    public SApplicationUpdateBuilder updateIconContent(byte[] content) {
        descriptor.addField(SApplicationWithIcon.ICON_CONTENT, content);
        return this;
    }

}
