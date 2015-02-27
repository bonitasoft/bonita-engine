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
 * represents a helper for updating a {@link User}. Chaining is possible with this updator to ease the {@link User} update.
 * <br>
 * For instance, new UserUpdater.setUsername("john.doe").setFirstname("John").setLastname("Doe");
 *
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @see User
 * @since 6.0.0
 */
public class UserUpdater implements Serializable {

    private static final long serialVersionUID = 4565052647320534796L;

    /**
     * represent the available {@link User} fields
     */
    public enum UserField {
        USER_NAME, PASSWORD, FIRST_NAME, LAST_NAME, ICON_NAME, ICON_PATH, TITLE, JOB_TITLE, MANAGER_ID, ENABLED;
    }

    private final Map<UserField, Serializable> fields;

    private ContactDataUpdater persoContactUpdater;

    private ContactDataUpdater proContactUpdater;

    /**
     * Default Constructor.
     */
    public UserUpdater() {
        fields = new HashMap<UserField, Serializable>(5);
    }

    /**
     * @param name the user's username to update
     * @return the current {@link UserUpdater} for chaining purpose
     */
    public UserUpdater setUserName(final String name) {
        fields.put(UserField.USER_NAME, name);
        return this;
    }

    /**
     * @param password the user's password to update
     * @return the current {@link UserUpdater} for chaining purpose
     */
    public UserUpdater setPassword(final String password) {
        fields.put(UserField.PASSWORD, password);
        return this;
    }

    /**
     * @param firstName the user's firstname to update
     * @return the current {@link UserUpdater} for chaining purpose
     */
    public UserUpdater setFirstName(final String firstName) {
        fields.put(UserField.FIRST_NAME, firstName);
        return this;
    }

    /**
     * @param lastName the user's lastname to update
     * @return the current {@link UserUpdater} for chaining purpose
     */
    public UserUpdater setLastName(final String lastName) {
        fields.put(UserField.LAST_NAME, lastName);
        return this;
    }

    /**
     * @param iconName the user's icon name to update
     * @return the current {@link UserUpdater} for chaining purpose
     */
    public UserUpdater setIconName(final String iconName) {
        fields.put(UserField.ICON_NAME, iconName);
        return this;
    }

    /**
     * @param managerId the user's manager id to update
     * @return the current {@link UserUpdater} for chaining purpose
     */
    public UserUpdater setManagerId(final long managerId) {
        fields.put(UserField.MANAGER_ID, managerId);
        return this;
    }

    /**
     * @param iconPath the user's icon path to update
     * @return the current {@link UserUpdater} for chaining purpose
     */
    public UserUpdater setIconPath(final String iconPath) {
        fields.put(UserField.ICON_PATH, iconPath);
        return this;
    }

    /**
     * @param title the user's title to update
     * @return the current {@link UserUpdater} for chaining purpose
     */
    public UserUpdater setTitle(final String title) {
        fields.put(UserField.TITLE, title);
        return this;
    }

    /**
     * @param jobTitle the user's job title to update
     * @return the current {@link UserUpdater} for chaining purpose
     */
    public UserUpdater setJobTitle(final String jobTitle) {
        fields.put(UserField.JOB_TITLE, jobTitle);
        return this;
    }

    /**
     * @param enabled allow to know if the current user is enabled inside organization
     * @return the current {@link UserUpdater} for chaining purpose
     */
    public UserUpdater setEnabled(final boolean enabled) {
        fields.put(UserField.ENABLED, enabled);
        return this;
    }

    /**
     * @return the current user information to udpate
     */
    public Map<UserField, Serializable> getFields() {
        return fields;
    }

    /**
     * @param persoContactUpdater the user's personal contact information to update
     * @return the current {@link UserUpdater} for chaining purpose
     */
    public UserUpdater setPersonalContactData(final ContactDataUpdater persoContactUpdater) {
        this.persoContactUpdater = persoContactUpdater;
        return this;
    }

    /**
     * @param proContactUpdater the user's professional contact information to update
     * @return the current {@link UserUpdater} for chaining purpose
     */
    public UserUpdater setProfessionalContactData(final ContactDataUpdater proContactUpdater) {
        this.proContactUpdater = proContactUpdater;
        return this;
    }

    /**
     * @return the user's personal contact updater object
     */
    public ContactDataUpdater getPersoContactUpdater() {
        return persoContactUpdater;
    }

    /**
     * @return the professional contact updater object
     */
    public ContactDataUpdater getProContactUpdater() {
        return proContactUpdater;
    }

    /**
     * Has this updater at least one field to update (directly or in its personal / professional contact data)?
     *
     * @return true if there is at least one field to update
     */
    public boolean hasFields() {
        return !getFields().isEmpty() || getPersoContactUpdater() != null && getPersoContactUpdater().hasFields()
                || getProContactUpdater() != null && getProContactUpdater().hasFields();
    }
}
