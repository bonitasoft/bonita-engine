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
 * represents a helper for creating a {@link Role}. Chaining is possible with this creator to ease the {@link Role} creation.
 * <br>
 * For instance, new RoleCreator("member").setDisplayName("Member").setIconName("userIcon");
 *
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @see Role
 * @since 6.0.0
 */
public class RoleCreator implements Serializable {

    private static final long serialVersionUID = -1414989152963184543L;

    /**
     * represents the available {@link Role} field
     */
    public enum RoleField {
        NAME, DISPLAY_NAME, DESCRIPTION, ICON_NAME, ICON_PATH;
    }

    private final Map<RoleField, Serializable> fields;

    /**
     * create a new creator instance with a given role name
     *
     * @param name
     *        The name of the role to create
     */
    public RoleCreator(final String name) {
        fields = new HashMap<RoleField, Serializable>(5);
        fields.put(RoleField.NAME, name);
    }

    /**
     * @param displayName
     *        The role's display name to create
     * @return The current {@link RoleCreator}
     */
    public RoleCreator setDisplayName(final String displayName) {
        fields.put(RoleField.DISPLAY_NAME, displayName);
        return this;
    }

    /**
     * @param description
     *        The role's description to create
     * @return The current {@link RoleCreator}
     */
    public RoleCreator setDescription(final String description) {
        fields.put(RoleField.DESCRIPTION, description);
        return this;
    }

    /**
     * @param iconName
     *        The role's icon name to create
     * @return The current {@link RoleCreator}
     */
    public RoleCreator setIconName(final String iconName) {
        fields.put(RoleField.ICON_NAME, iconName);
        return this;
    }

    /**
     * @param iconPath
     *        The role's icon path to create
     * @return The current {@link RoleCreator}
     */
    public RoleCreator setIconPath(final String iconPath) {
        fields.put(RoleField.ICON_PATH, iconPath);
        return this;
    }

    /**
     * @return The current role's information to create
     */
    public Map<RoleField, Serializable> getFields() {
        return fields;
    }

}
