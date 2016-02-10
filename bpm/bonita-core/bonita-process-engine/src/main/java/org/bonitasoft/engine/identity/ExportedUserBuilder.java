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


/**
 * Import / export version of the client User model
 * 
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class ExportedUserBuilder {

    private final ExportedUserImpl user;

    public ExportedUserBuilder(final ExportedUserImpl user) {
        super();
        this.user = user;
    }

    public ExportedUserBuilder setPasswordEncrypted(final boolean passwordEncrypted) {
        user.setPasswordEncrypted(passwordEncrypted);
        return this;
    }

    public ExportedUserBuilder setPassword(final String password) {
        user.setPassword(password);
        return this;
    }

    public ExportedUserBuilder setFirstName(final String firstName) {
        user.setFirstName(firstName);
        return this;
    }

    public ExportedUserBuilder setLastName(final String lastName) {
        user.setLastName(lastName);
        return this;
    }

    public ExportedUserBuilder setUsername(final String userName) {
        user.setUserName(userName);
        return this;
    }

    public ExportedUserBuilder setIconName(final String iconName) {
        user.setIconName(iconName);
        return this;
    }

    public ExportedUserBuilder setIconPath(final String iconPath) {
        user.setIconPath(iconPath);
        return this;
    }

    public ExportedUserBuilder setTitle(final String title) {
        user.setTitle(title);
        return this;
    }

    public ExportedUserBuilder setJobTitle(final String jobTitle) {
        user.setJobTitle(jobTitle);
        return this;
    }

    public ExportedUserBuilder setCreatedBy(final long createdBy) {
        user.setCreatedBy(createdBy);
        return this;
    }

    public ExportedUserBuilder setPersonalData(final ContactData personalData) {
        user.setPersonalData(personalData);
        return this;
    }

    public ExportedUserBuilder setProfessionalData(final ContactData professionalData) {
        user.setProfessionalData(professionalData);
        return this;
    }

    public ExportedUserBuilder setManagerUserId(final long managerUserId) {
        user.setManagerUserId(managerUserId);
        return this;
    }

    public ExportedUserBuilder setManagerUserName(final String managerUserName) {
        user.setManagerUserName(managerUserName);
        return this;
    }

    public ExportedUserBuilder setEnabled(final boolean enabled) {
        user.setEnabled(enabled);
        return this;
    }
    
    public ExportedUserBuilder addCustomUserInfoValue(ExportedCustomUserInfoValue userInfo) {
        user.addCustomUserInfoValues(userInfo);
        return this;
    }

    public ExportedUser done() {
        return user;
    }

}
