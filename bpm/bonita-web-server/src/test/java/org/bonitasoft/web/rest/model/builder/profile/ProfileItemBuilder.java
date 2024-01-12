/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.model.builder.profile;

import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.impl.ProfileImpl;
import org.bonitasoft.web.rest.model.portal.profile.ProfileItem;

/**
 * @author Colin PUY
 */
public class ProfileItemBuilder {

    protected long id = 1L;

    protected String name = "aName";

    protected String description = "aDescription";

    protected boolean isDefault = false;

    protected String iconPath;

    protected final long createdBy = 0;

    protected final String createdOn = null;

    protected final long updatedBy = 0;

    protected final String updatedOn = null;

    public static ProfileItemBuilder aProfileItem() {
        return new ProfileItemBuilder();
    }

    public ProfileItem build() {
        final ProfileItem item = new ProfileItem();
        item.setId(id);
        item.setName(name);
        item.setDescription(description);
        item.setIsDefault(isDefault);
        item.setIcon(iconPath);
        item.setUpdatedByUserId(updatedBy);
        item.setLastUpdateDate(updatedOn);
        item.setCreatedByUserId(createdBy);
        item.setCreationDate(createdOn);
        return item;
    }

    public Profile toProfile() {
        final ProfileImpl profile = new ProfileImpl(name);
        profile.setDescription(description);
        profile.setDefault(isDefault);
        return profile;
    }

    public ProfileItemBuilder fromEngineItem(final Profile profile) {
        id = profile.getId();
        name = profile.getName();
        description = profile.getDescription();
        isDefault = profile.isDefault();
        return this;
    }

    public ProfileItemBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public ProfileItemBuilder withIcon(final String iconPath) {
        this.iconPath = iconPath;
        return this;
    }

}
