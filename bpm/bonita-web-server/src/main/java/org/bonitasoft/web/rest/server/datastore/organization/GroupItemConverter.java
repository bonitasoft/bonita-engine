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

import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.web.rest.model.identity.GroupItem;
import org.bonitasoft.web.rest.server.datastore.converter.ItemConverter;

public class GroupItemConverter extends ItemConverter<GroupItem, Group> {

    @Override
    public GroupItem convert(Group group) {
        GroupItem groupItem = new GroupItem();
        groupItem.setCreatedByUserId(group.getCreatedBy());
        groupItem.setCreationDate(group.getCreationDate());
        groupItem.setDescription(group.getDescription());
        groupItem.setDisplayName(group.getDisplayName());
        groupItem.setIcon(group.getIconId() == null ? "" : Avatars.PATH + group.getIconId());
        groupItem.setId(group.getId());
        groupItem.setLastUpdateDate(group.getLastUpdate());
        groupItem.setName(group.getName());
        groupItem.setParentPath(group.getParentPath());
        groupItem.setPath(group.getPath());
        return groupItem;
    }

}
