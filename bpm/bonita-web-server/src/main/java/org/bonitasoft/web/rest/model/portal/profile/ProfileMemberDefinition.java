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

import static org.bonitasoft.web.rest.model.portal.profile.ProfileMemberItem.ATTRIBUTE_PROFILE_ID;

import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * @author Julien Mege
 * @author Séverin Moussel
 */
public class ProfileMemberDefinition extends AbstractMemberDefinition<ProfileMemberItem> {

    /**
     * Singleton
     */
    public static ProfileMemberDefinition get() {
        return (ProfileMemberDefinition) Definitions.get(TOKEN);
    }

    public static final String TOKEN = "memberProfile";

    /**
     * the URL of UserProfileAssociation resource
     */
    protected static final String API_URL = "../API/portal/profileMember";

    @Override
    public String defineToken() {
        return TOKEN;
    }

    @Override
    protected void definePrimaryKeys() {
        // FIXME : delete when id in engine is deleted
        setPrimaryKeys(ProfileMemberItem.ATTRIBUTE_ID);

        // FIXME : uncomment when id in engine is deleted
        // setPrimaryKeys(
        // ATTRIBUTE_PROFILE_ID,
        // ATTRIBUTE_USER_ID,
        // ATTRIBUTE_ROLE_ID,
        // ATTRIBUTE_GROUP_ID);
    }

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    @Override
    protected void defineAttributes() {
        createAttribute(ATTRIBUTE_PROFILE_ID, ItemAttribute.TYPE.ITEM_ID).isMandatory();
        super.defineAttributes();
    }

    @Override
    protected void defineDeploys() {
        declareDeployable(ATTRIBUTE_PROFILE_ID, ProfileDefinition.get());
        super.defineDeploys();
    }

    @Override
    public ProfileMemberItem _createItem() {
        return new ProfileMemberItem();
    }
}
