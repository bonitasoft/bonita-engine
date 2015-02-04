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
 * represents a helper for updating a {@link Role}. Chaining is possible with this updator to ease the {@link Role} update.
 * <br>
 * For instance, new RoleUpdater("member").setDisplayName("Member").setIconName("userIcon");
 *
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @see Role
 * @since 6.0.0
 */
public class RoleUpdater implements Serializable {

    private static final long serialVersionUID = 728214104237982027L;

    /**
     * represent the available {@link Role} fields
     */
    public enum RoleField {
        NAME, DISPLAY_NAME, DESCRIPTION, ICON_NAME, ICON_PATH;
    }

    private final Map<RoleField, Serializable> fields;

    /**
     * Default Constructor.
     */
    public RoleUpdater() {
        fields = new HashMap<RoleField, Serializable>(5);
    }

    /**
     * @param name
     *        The role's new name to update
     * @return The current {@link RoleUpdater} for chaining purpose
     */
    public RoleUpdater setName(final String name) {
        fields.put(RoleField.NAME, name);
        return this;
    }

    /**
     * @param displayName
     *        The role's display name to update
     * @return The current {@link RoleUpdater} for chaining purpose
     */
    public RoleUpdater setDisplayName(final String displayName) {
        fields.put(RoleField.DISPLAY_NAME, displayName);
        return this;
    }

    /**
     * @param description
     *        The role's description to update
     * @return The current {@link RoleUpdater} for chaining purpose
     */
    public RoleUpdater setDescription(final String description) {
        fields.put(RoleField.DESCRIPTION, description);
        return this;
    }

    /**
     * @param iconName
     *        The role's icon name to update
     * @return The current {@link RoleUpdater} for chaining purpose
     */
    public RoleUpdater setIconName(final String iconName) {
        fields.put(RoleField.ICON_NAME, iconName);
        return this;
    }

    /**
     * @param iconPath
     *        The role's icon path to update
     * @return The current {@link RoleUpdater} for chaining purpose
     */
    public RoleUpdater setIconPath(final String iconPath) {
        fields.put(RoleField.ICON_PATH, iconPath);
        return this;
    }

    /**
     * @return The role's fields to update
     */
    public Map<RoleField, Serializable> getFields() {
        return fields;
    }

}
