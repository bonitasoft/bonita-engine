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

import org.bonitasoft.engine.identity.ContactDataCreator.ContactDataField;

/**
 * @author Matthieu Chaffotte
 */
public class UserCreator implements Serializable {

    private static final long serialVersionUID = -1414989152963184543L;

    public enum UserField {
        NAME, PASSWORD, FIRST_NAME, LAST_NAME, ICON_NAME, ICON_PATH, TITLE, JOB_TITLE, MANAGER_ID, ENABLED;
    }

    private final Map<UserField, Serializable> fields;

    private final Map<ContactDataField, Serializable> persoFields;

    private final Map<ContactDataField, Serializable> proFields;

    public UserCreator(final String name, final String password) {
        fields = new HashMap<UserField, Serializable>(5);
        fields.put(UserField.NAME, name);
        fields.put(UserField.PASSWORD, password);
        persoFields = new HashMap<ContactDataCreator.ContactDataField, Serializable>();
        proFields = new HashMap<ContactDataCreator.ContactDataField, Serializable>();
    }

    public UserCreator setFirstName(final String firstName) {
        fields.put(UserField.FIRST_NAME, firstName);
        return this;
    }

    public UserCreator setLastName(final String lastName) {
        fields.put(UserField.LAST_NAME, lastName);
        return this;
    }

    public UserCreator setIconName(final String iconName) {
        fields.put(UserField.ICON_NAME, iconName);
        return this;
    }

    public UserCreator setIconPath(final String iconPath) {
        fields.put(UserField.ICON_PATH, iconPath);
        return this;
    }

    public UserCreator setTitle(final String title) {
        fields.put(UserField.TITLE, title);
        return this;
    }

    public UserCreator setJobTitle(final String jobTitle) {
        fields.put(UserField.JOB_TITLE, jobTitle);
        return this;
    }

    public UserCreator setManagerUserId(final long managerUserId) {
        fields.put(UserField.MANAGER_ID, managerUserId);
        return this;
    }

    public UserCreator setEnabled(final boolean enabled) {
        fields.put(UserField.ENABLED, enabled);
        return this;
    }

    public Map<UserField, Serializable> getFields() {
        return fields;
    }

    public Map<ContactDataField, Serializable> getPersoFields() {
        return persoFields;
    }

    public Map<ContactDataField, Serializable> getProFields() {
        return proFields;
    }

    public UserCreator setPersonalContactData(final ContactDataCreator creator) {
        if (creator != null && creator.getFields() != null) {
            persoFields.putAll(creator.getFields());
        }
        return this;
    }

    public UserCreator setProfessionalContactData(final ContactDataCreator creator) {
        if (creator != null && creator.getFields() != null) {
            proFields.putAll(creator.getFields());
        }
        return this;
    }

}
