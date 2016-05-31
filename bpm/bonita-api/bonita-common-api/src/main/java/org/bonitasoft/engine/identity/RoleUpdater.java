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
        NAME, DISPLAY_NAME, DESCRIPTION, @Deprecated ICON_NAME, @Deprecated ICON_PATH, ICON_FILENAME, ICON_CONTENT
    }

    private final Map<RoleField, Serializable> fields;

    /**
     * Default Constructor.
     */
    public RoleUpdater() {
        fields = new HashMap<>(5);
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
     * @deprecated since 7.3.0 use #setIcon
     */
    @Deprecated
    public RoleUpdater setIconName(final String iconName) {
        return this;
    }

    /**
     * @param iconPath
     *        The role's icon path to update
     * @return The current {@link RoleUpdater} for chaining purpose
     * @deprecated since 7.3.0 use #setIcon
     */
    @Deprecated
    public RoleUpdater setIconPath(final String iconPath) {
        return this;
    }

    /**
     * set the icon on the role to be created
     *
     * @param filename the filename of the icon
     * @param content the content of the icon
     * @return the role created
     */
    public RoleUpdater setIcon(String filename, byte[] content) {
        fields.put(RoleUpdater.RoleField.ICON_FILENAME, filename);
        fields.put(RoleUpdater.RoleField.ICON_CONTENT, content);
        return this;
    }

    /**
     * @return The role's fields to update
     */
    public Map<RoleField, Serializable> getFields() {
        return fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RoleUpdater that = (RoleUpdater) o;
        return Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields);
    }

    @Override
    public String toString() {
        return "RoleUpdater{" +
                "fields=" + fields +
                '}';
    }
}
