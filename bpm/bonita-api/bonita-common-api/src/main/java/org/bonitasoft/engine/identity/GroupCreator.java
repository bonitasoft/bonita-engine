/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
 * @author Matthieu Chaffotte
 */
public class GroupCreator implements Serializable {

    private static final long serialVersionUID = -1546623947528297571L;

    public enum GroupField {
        NAME, DISPLAY_NAME, DESCRIPTION, ICON_NAME, ICON_PATH, PARENT_PATH;
    }

    private final Map<GroupField, Serializable> fields;

    public GroupCreator(final String name) {
        fields = new HashMap<GroupField, Serializable>(3);
        fields.put(GroupField.NAME, name);
    }

    public void setParentPath(final String parentPath) {
        fields.put(GroupField.PARENT_PATH, parentPath);
    }

    public GroupCreator setDisplayName(final String displayName) {
        fields.put(GroupField.DISPLAY_NAME, displayName);
        return this;
    }

    public GroupCreator setDescription(final String description) {
        fields.put(GroupField.DESCRIPTION, description);
        return this;
    }

    public GroupCreator setIconName(final String iconName) {
        fields.put(GroupField.ICON_NAME, iconName);
        return this;
    }

    public GroupCreator setIconPath(final String iconPath) {
        fields.put(GroupField.ICON_PATH, iconPath);
        return this;
    }

    public Map<GroupField, Serializable> getFields() {
        return fields;
    }

}
