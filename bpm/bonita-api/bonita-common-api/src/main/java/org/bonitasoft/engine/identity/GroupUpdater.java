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
 * represent a helper fpr updating a {@link Group}
 *
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @see Group
 * @since 6.0.0
 */
public class GroupUpdater implements Serializable {

    private static final long serialVersionUID = 728214104237982027L;

    /**
     * represents the available {@link Group} fields
     */
    public enum GroupField {
        NAME, DISPLAY_NAME, DESCRIPTION, ICON_NAME, ICON_PATH, PARENT_PATH;
    }

    private final Map<GroupField, Serializable> fields;

    /**
     * Default Constructor.
     */
    public GroupUpdater() {
        fields = new HashMap<GroupField, Serializable>(3);
    }

    /**
     * @param name
     *        The group's name to update
     */
    public void updateName(final String name) {
        fields.put(GroupField.NAME, name);
    }

    /**
     * @param displayName
     *        The group's display name to update
     */
    public void updateDisplayName(final String displayName) {
        fields.put(GroupField.DISPLAY_NAME, displayName);
    }

    /**
     * @param description
     *        The group's description to update
     */
    public void updateDescription(final String description) {
        fields.put(GroupField.DESCRIPTION, description);
    }

    /**
     * @param iconName
     *        The group's icon name to update
     */
    public void updateIconName(final String iconName) {
        fields.put(GroupField.ICON_NAME, iconName);
    }

    /**
     * @param iconPath
     *        The group's icon path to update
     */
    public void updateIconPath(final String iconPath) {
        fields.put(GroupField.ICON_PATH, iconPath);
    }

    /**
     * @param parentPath
     *        The group's parent path to update
     */
    public void updateParentPath(final String parentPath) {
        fields.put(GroupField.PARENT_PATH, parentPath);
    }

    /**
     * @return The group's fields to update
     */
    public Map<GroupField, Serializable> getFields() {
        return fields;
    }

}
