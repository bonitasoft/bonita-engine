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
package org.bonitasoft.web.rest.server.datastore.organization;

import org.bonitasoft.engine.identity.User;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.rest.server.datastore.converter.ItemConverter;
import org.bonitasoft.web.toolkit.client.data.APIID;

public class UserItemConverter extends ItemConverter<UserItem, User> {

    @Override
    public UserItem convert(User user) {
        if (user == null) {
            return null;
        }

        final UserItem result = new UserItem();
        result.setId(APIID.makeAPIID(user.getId()));
        result.setFirstName(user.getFirstName());
        result.setLastName(user.getLastName());
        result.setPassword(null);
        result.setUserName(user.getUserName());
        result.setManagerId(user.getManagerUserId());
        result.setEnabled(user.isEnabled());

        // Add default icon if icon if empty
        if (user.getIconId() != null) {
            result.setIcon(Avatars.PATH + user.getIconId());
        } else {
            result.setIcon(UserItem.DEFAULT_USER_ICON);
        }

        result.setCreationDate(user.getCreationDate());
        result.setCreatedByUserId(user.getCreatedBy());
        result.setLastUpdateDate(user.getLastUpdate());
        result.setLastConnectionDate(user.getLastConnection());
        result.setTitle(user.getTitle());
        result.setJobTitle(user.getJobTitle());

        return result;
    }

}
