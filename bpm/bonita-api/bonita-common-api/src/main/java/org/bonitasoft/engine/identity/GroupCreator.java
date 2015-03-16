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
package org.bonitasoft.engine.identity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * represent a helper for creating a {@link Group}
 *
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @see Group
 * @since 6.0.0
 */
public class GroupCreator implements Serializable {

    private static final long serialVersionUID = -1546623947528297571L;

    /**
     * represents the available {@link Group} field
     */
    public enum GroupField {
        NAME, DISPLAY_NAME, DESCRIPTION, ICON_NAME, ICON_PATH, PARENT_PATH;
    }

    private final Map<GroupField, Serializable> fields;

    /**
     * creates a new {@link GroupCreator} with a group name to create
     *
     * @param name
     *        The name of the group that will be created
     */
    public GroupCreator(final String name) {
        fields = new HashMap<GroupField, Serializable>(3);
        fields.put(GroupField.NAME, name);
    }

    /**
     * @param parentPath
     *        The group's parent path to create
     */
    public void setParentPath(final String parentPath) {
        fields.put(GroupField.PARENT_PATH, parentPath);
    }

    /**
     * @param displayName
     *        The group's display to create
     */
    public GroupCreator setDisplayName(final String displayName) {
        fields.put(GroupField.DISPLAY_NAME, displayName);
        return this;
    }

    /**
     * @param description
     *        The group's description to create
     */
    public GroupCreator setDescription(final String description) {
        fields.put(GroupField.DESCRIPTION, description);
        return this;
    }

    /**
     * @param iconName
     *        The group's icon name to create
     */
    public GroupCreator setIconName(final String iconName) {
        fields.put(GroupField.ICON_NAME, iconName);
        return this;
    }

    /**
     * @param iconPath
     *        The group's icon file path to create
     */
    public GroupCreator setIconPath(final String iconPath) {
        fields.put(GroupField.ICON_PATH, iconPath);
        return this;
    }

    /**
     * @return The information associated with the group to create
     */
    public Map<GroupField, Serializable> getFields() {
        return fields;
    }

}
