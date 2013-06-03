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
 * @author Celine Souchet
 */
public class UserUpdater implements Serializable {

    private static final long serialVersionUID = 4565052647320534796L;

    public enum UserField {
        USER_NAME, PASSWORD, FIRST_NAME, LAST_NAME, ICON_NAME, ICON_PATH, TITLE, JOB_TITLE, MANAGER_ID, ENABLED;
    }

    private final Map<UserField, Serializable> fields;

    private ContactDataUpdater persoContactUpdater;

    private ContactDataUpdater proContactUpdater;

    public UserUpdater() {
        fields = new HashMap<UserField, Serializable>(5);
    }

    public UserUpdater setUserName(final String name) {
        fields.put(UserField.USER_NAME, name);
        return this;
    }

    public UserUpdater setPassword(final String password) {
        fields.put(UserField.PASSWORD, password);
        return this;
    }

    public UserUpdater setFirstName(final String firstName) {
        fields.put(UserField.FIRST_NAME, firstName);
        return this;
    }

    public UserUpdater setLastName(final String lastName) {
        fields.put(UserField.LAST_NAME, lastName);
        return this;
    }

    public UserUpdater setIconName(final String iconName) {
        fields.put(UserField.ICON_NAME, iconName);
        return this;
    }

    public UserUpdater setManagerId(final long managerId) {
        fields.put(UserField.MANAGER_ID, managerId);
        return this;
    }

    public UserUpdater setIconPath(final String iconPath) {
        fields.put(UserField.ICON_PATH, iconPath);
        return this;
    }

    public UserUpdater setTitle(final String title) {
        fields.put(UserField.TITLE, title);
        return this;
    }

    public UserUpdater setJobTitle(final String jobTitle) {
        fields.put(UserField.JOB_TITLE, jobTitle);
        return this;
    }

    public UserUpdater setEnabled(final boolean enabled) {
        fields.put(UserField.ENABLED, enabled);
        return this;
    }

    public Map<UserField, Serializable> getFields() {
        return fields;
    }

    public UserUpdater setPersonalContactData(final ContactDataUpdater persoContactUpdater) {
        this.persoContactUpdater = persoContactUpdater;
        return this;
    }

    public UserUpdater setProfessionalContactData(final ContactDataUpdater proContactUpdater) {
        this.proContactUpdater = proContactUpdater;
        return this;
    }

    public ContactDataUpdater getPersoContactUpdater() {
        return persoContactUpdater;
    }

    public ContactDataUpdater getProContactUpdater() {
        return proContactUpdater;
    }

    /**
     * Has this updater at least one field to update (directly or in its personal / professional contact data)?
     * 
     * @return true if there is at least one field to update
     */
    public boolean hasFields() {
        return !getFields().isEmpty() || (getPersoContactUpdater() != null && getPersoContactUpdater().hasFields())
                || (getProContactUpdater() != null && getProContactUpdater().hasFields());
    }
}
