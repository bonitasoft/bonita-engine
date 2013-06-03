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
public class ProfileEntryCreator implements Serializable {

    private static final long serialVersionUID = -1414989152963184543L;

    public enum ProfileEntryField {
        NAME, PROFILE_ID, DESCRIPTION, PARENT_ID, TYPE, PAGE, INDEX;
    }

    private final Map<ProfileEntryField, Serializable> fields;

    public ProfileEntryCreator(final String name, final long profileId) {
        fields = new HashMap<ProfileEntryField, Serializable>(5);
        fields.put(ProfileEntryField.NAME, name);
        fields.put(ProfileEntryField.PROFILE_ID, profileId);
    }

    public ProfileEntryCreator setDescription(final String description) {
        fields.put(ProfileEntryField.DESCRIPTION, description);
        return this;
    }

    public ProfileEntryCreator setParentId(final long parentId) {
        fields.put(ProfileEntryField.PARENT_ID, parentId);
        return this;
    }

    public ProfileEntryCreator setType(final String type) {
        fields.put(ProfileEntryField.TYPE, type);
        return this;
    }

    public ProfileEntryCreator setPage(final String page) {
        fields.put(ProfileEntryField.PAGE, page);
        return this;
    }

    public ProfileEntryCreator setIndex(final long index) {
        fields.put(ProfileEntryField.INDEX, index);
        return this;
    }

    public Map<ProfileEntryField, Serializable> getFields() {
        return fields;
    }

}
