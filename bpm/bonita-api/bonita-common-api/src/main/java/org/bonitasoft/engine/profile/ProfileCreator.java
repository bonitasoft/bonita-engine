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
package org.bonitasoft.engine.profile;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Celine Souchet
 */
public class ProfileCreator implements Serializable {

    private static final long serialVersionUID = -1414989152963184543L;

    public enum ProfileField {
        NAME, DESCRIPTION, ICON_PATH;
    }

    private final Map<ProfileField, Serializable> fields;

    public ProfileCreator(final String name) {
        fields = new HashMap<ProfileField, Serializable>(3);
        fields.put(ProfileField.NAME, name);
    }

    public ProfileCreator setDescription(final String description) {
        fields.put(ProfileField.DESCRIPTION, description);
        return this;
    }

    public ProfileCreator setIconPath(final String iconPath) {
        fields.put(ProfileField.ICON_PATH, iconPath);
        return this;
    }

    public Map<ProfileField, Serializable> getFields() {
        return fields;
    }

}
