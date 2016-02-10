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
package org.bonitasoft.engine.profile;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Celine Souchet
 */
public class ProfileMemberCreator implements Serializable {

    private static final long serialVersionUID = -1414989152963184543L;

    public enum ProfileMemberField {
        PROFILE_ID, USER_ID, GROUP_ID, ROLE_ID;
    }

    private final Map<ProfileMemberField, Serializable> fields;

    public ProfileMemberCreator(final long profileId) {
        fields = new HashMap<ProfileMemberField, Serializable>(5);
        fields.put(ProfileMemberField.PROFILE_ID, profileId);
    }

    public ProfileMemberCreator setUserId(final long userId) {
        fields.put(ProfileMemberField.USER_ID, userId);
        return this;
    }

    public ProfileMemberCreator setGroupId(final long groupId) {
        fields.put(ProfileMemberField.GROUP_ID, groupId);
        return this;
    }

    public ProfileMemberCreator setRoleId(final long roleId) {
        fields.put(ProfileMemberField.ROLE_ID, roleId);
        return this;
    }

    public Map<ProfileMemberField, Serializable> getFields() {
        return fields;
    }

}
