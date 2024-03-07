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
package org.bonitasoft.web.rest.model.builder.organisation;

import org.bonitasoft.web.rest.model.identity.GroupItem;

/**
 * @author Colin PUY
 */
public class GroupItemBuilder {

    private final String name = "Group1";
    private final String displayName = "Group";
    private final String description = "Text for describe the group";
    private final String parentPath = "";
    private final String parentGroupId = "";

    public static GroupItemBuilder aGroup() {
        return new GroupItemBuilder();
    }

    public GroupItem build() {
        GroupItem group = new GroupItem();
        group.setName(name);
        group.setDisplayName(displayName);
        group.setDescription(description);
        group.setParentPath(parentPath);
        group.setParentGroupId(parentGroupId);
        return group;
    }
}
