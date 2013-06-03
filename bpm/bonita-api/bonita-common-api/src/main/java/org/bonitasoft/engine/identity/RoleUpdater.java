/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
public class RoleUpdater implements Serializable {

    private static final long serialVersionUID = 728214104237982027L;

    public enum RoleField {
        NAME, DISPLAY_NAME, DESCRIPTION, ICON_NAME, ICON_PATH;
    }

    private final Map<RoleField, Serializable> fields;

    public RoleUpdater() {
        fields = new HashMap<RoleField, Serializable>(5);
    }

    public RoleUpdater setName(final String name) {
        fields.put(RoleField.NAME, name);
        return this;
    }

    public RoleUpdater setDisplayName(final String displayName) {
        fields.put(RoleField.DISPLAY_NAME, displayName);
        return this;
    }

    public RoleUpdater setDescription(final String description) {
        fields.put(RoleField.DESCRIPTION, description);
        return this;
    }

    public RoleUpdater setIconName(final String iconName) {
        fields.put(RoleField.ICON_NAME, iconName);
        return this;
    }

    public RoleUpdater setIconPath(final String iconPath) {
        fields.put(RoleField.ICON_PATH, iconPath);
        return this;
    }

    public Map<RoleField, Serializable> getFields() {
        return fields;
    }

}
