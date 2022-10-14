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
package org.bonitasoft.web.rest.model.portal.profile;

import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasUniqueId;

/**
 * @author Julien Mege
 * @author Séverin Moussel
 */
public class ProfileMemberItem extends AbstractMemberItem implements ItemHasUniqueId {

    public ProfileMemberItem() {
        super();
    }

    public ProfileMemberItem(final IItem item) {
        super(item);
    }

    public static final String ATTRIBUTE_PROFILE_ID = "profile_id";

    // FIXME : delete when id in engine is deleted
    @Override
    public void setId(final String id) {
        this.setAttribute(ATTRIBUTE_ID, id);
    }

    // FIXME : delete when id in engine is deleted
    @Override
    public void setId(final Long id) {
        this.setAttribute(ATTRIBUTE_ID, id);
    }

    public APIID getProfileId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_PROFILE_ID);
    }

    public void setProfileId(final String id) {
        setAttribute(ATTRIBUTE_PROFILE_ID, id);
    }

    public void setProfileId(final APIID id) {
        setAttribute(ATTRIBUTE_PROFILE_ID, id);
    }

    public void setProfileId(final Long id) {
        setAttribute(ATTRIBUTE_PROFILE_ID, id);
    }

    // Deploys

    public ProfileItem getProfile() {
        return new ProfileItem(getDeploy(ATTRIBUTE_PROFILE_ID));
    }

    @Override
    public ItemDefinition<ProfileMemberItem> getItemDefinition() {
        return ProfileMemberDefinition.get();
    }

}
