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
import java.util.Objects;

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
        NAME, DISPLAY_NAME, DESCRIPTION, @Deprecated ICON_NAME, @Deprecated ICON_PATH, PARENT_PATH, ICON_FILENAME, ICON_CONTENT
    }

    private final Map<GroupField, Serializable> fields;

    /**
     * Default Constructor.
     */
    public GroupUpdater() {
        fields = new HashMap<>(3);
    }

    /**
     * @param name
     *        The group's name to update
     */
    public GroupUpdater updateName(final String name) {
        fields.put(GroupField.NAME, name);
        return this;
    }

    /**
     * @param displayName
     *        The group's display name to update
     */
    public GroupUpdater updateDisplayName(final String displayName) {
        fields.put(GroupField.DISPLAY_NAME, displayName);
        return this;
    }

    /**
     * @param description
     *        The group's description to update
     */
    public GroupUpdater updateDescription(final String description) {
        fields.put(GroupField.DESCRIPTION, description);
        return this;
    }

    /**
     * @param iconName
     *        The group's icon name to update
     * @deprecated since 7.3.0 use #updateIcon
     */
    @Deprecated
    public GroupUpdater updateIconName(final String iconName) {
        return this;
    }

    /**
     * @param iconPath
     *        The group's icon path to update
     * @deprecated since 7.3.0 use #updateIcon
     */
    @Deprecated
    public GroupUpdater updateIconPath(final String iconPath) {
        return this;
    }

    public GroupUpdater updateIcon(String filename, byte[] content) {
        fields.put(GroupField.ICON_FILENAME, filename);
        fields.put(GroupField.ICON_CONTENT, content);
        return this;
    }

    /**
     * @param parentPath
     *        The group's parent path to update
     */
    public GroupUpdater updateParentPath(final String parentPath) {
        fields.put(GroupField.PARENT_PATH, parentPath);
        return this;
    }

    /**
     * @return The group's fields to update
     */
    public Map<GroupField, Serializable> getFields() {
        return fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GroupUpdater that = (GroupUpdater) o;
        return Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields);
    }

    @Override
    public String toString() {
        return "GroupUpdater{" +
                "fields=" + fields +
                '}';
    }
}
