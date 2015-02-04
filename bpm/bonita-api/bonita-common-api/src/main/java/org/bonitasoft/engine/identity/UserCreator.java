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

import org.bonitasoft.engine.identity.ContactDataCreator.ContactDataField;

/**
 * represents a helper for creating {@link User}. Chaining is possible with this creator to ease the {@link User} creation.
 * <br>
 * For instance, new UserCreator("john.doe", "password").setFirstname("John").setLastname("Doe");
 *
 * @author Matthieu Chaffotte
 * @see User
 * @since 6.0.0
 */
public class UserCreator implements Serializable {

    private static final long serialVersionUID = -1414989152963184543L;

    /**
     * represents the available {@link User} field
     */
    public enum UserField {
        NAME, PASSWORD, FIRST_NAME, LAST_NAME, ICON_NAME, ICON_PATH, TITLE, JOB_TITLE, MANAGER_ID, ENABLED;
    }

    private final Map<UserField, Serializable> fields;

    private final Map<ContactDataField, Serializable> persoFields;

    private final Map<ContactDataField, Serializable> proFields;

    /**
     * create a new creator instance with a given user name and password
     *
     * @param name the name of the user to create
     * @param password the password of the user to create
     */
    public UserCreator(final String name, final String password) {
        fields = new HashMap<UserField, Serializable>(5);
        fields.put(UserField.NAME, name);
        fields.put(UserField.PASSWORD, password);
        persoFields = new HashMap<ContactDataCreator.ContactDataField, Serializable>();
        proFields = new HashMap<ContactDataCreator.ContactDataField, Serializable>();
    }

    /**
     * @param firstName the user's firstname to create
     * @return the current {@link UserCreator}
     */
    public UserCreator setFirstName(final String firstName) {
        fields.put(UserField.FIRST_NAME, firstName);
        return this;
    }

    /**
     * @param lastName the user's lastName to create
     * @return the current {@link UserCreator}
     */
    public UserCreator setLastName(final String lastName) {
        fields.put(UserField.LAST_NAME, lastName);
        return this;
    }

    /**
     * @param iconName the user's icon name to create
     * @return the current {@link UserCreator}
     */
    public UserCreator setIconName(final String iconName) {
        fields.put(UserField.ICON_NAME, iconName);
        return this;
    }

    /**
     * @param iconPath the user's icon path to create
     * @return the current {@link UserCreator}
     */
    public UserCreator setIconPath(final String iconPath) {
        fields.put(UserField.ICON_PATH, iconPath);
        return this;
    }

    /**
     * @param title the user's title to create
     * @return the current {@link UserCreator}
     */
    public UserCreator setTitle(final String title) {
        fields.put(UserField.TITLE, title);
        return this;
    }

    /**
     * @param jobTitle the user's jobTitle to create
     * @return the current {@link UserCreator}
     */
    public UserCreator setJobTitle(final String jobTitle) {
        fields.put(UserField.JOB_TITLE, jobTitle);
        return this;
    }

    /**
     * @param managerUserId the user's manager id to create
     * @return the current {@link UserCreator}
     */
    public UserCreator setManagerUserId(final long managerUserId) {
        fields.put(UserField.MANAGER_ID, managerUserId);
        return this;
    }

    /**
     * @param enabled enabled Boolean set to true if the user is enabled inside the organization
     * @return the current {@link UserCreator}
     */
    public UserCreator setEnabled(final boolean enabled) {
        fields.put(UserField.ENABLED, enabled);
        return this;
    }

    /**
     * @return the current user information to create
     */
    public Map<UserField, Serializable> getFields() {
        return fields;
    }

    /**
     * @return the current user personal information to create
     */
    public Map<ContactDataField, Serializable> getPersoFields() {
        return persoFields;
    }

    /**
     * @return the current user professional information to create
     */
    public Map<ContactDataField, Serializable> getProFields() {
        return proFields;
    }

    /**
     * @param creator the user's personal contact information to create
     * @return the current {@link UserCreator}
     */
    public UserCreator setPersonalContactData(final ContactDataCreator creator) {
        if (creator != null && creator.getFields() != null) {
            persoFields.putAll(creator.getFields());
        }
        return this;
    }

    /**
     * @param creator the user's professional contact information to create
     * @return the current {@link UserCreator}
     */
    public UserCreator setProfessionalContactData(final ContactDataCreator creator) {
        if (creator != null && creator.getFields() != null) {
            proFields.putAll(creator.getFields());
        }
        return this;
    }

}
