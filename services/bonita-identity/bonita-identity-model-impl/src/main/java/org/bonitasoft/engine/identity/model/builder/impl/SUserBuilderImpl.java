/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity.model.builder.impl;

import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.SUserBuilder;
import org.bonitasoft.engine.identity.model.impl.SUserImpl;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SUserBuilderImpl implements SUserBuilder {

    private SUserImpl entity;

    static final String ID = "id";

    static final String DELEGEE_USER_NAME = "delegeeUserName";

    static final String MANAGER_USER_ID = "managerUserId";

    static final String JOB_TITLE = "jobTitle";

    static final String TITLE = "title";

    static final String LAST_NAME = "lastName";

    static final String FIRST_NAME = "firstName";

    static final String USER_NAME = "userName";

    static final String PASSWORD = "password";

    static final String LAST_UPDATE = "lastUpdate";

    static final String LAST_CONNECTION = "lastConnection";

    static final String CREATED_BY = "createdBy";

    static final String CREATION_DATE = "creationDate";

    static final String ICON_NAME = "iconName";

    static final String ICON_PATH = "iconPath";

    static final String ENABLED = "enabled";

    @Override
    public SUserBuilder createNewInstance() {
        entity = new SUserImpl();
        return this;
    }

    @Override
    public SUserBuilder createNewInstance(final SUser user) {
        entity = new SUserImpl(user);
        return this;
    }

    @Override
    public SUserBuilder setUserName(final String userName) {
        entity.setUserName(userName);
        return this;
    }

    @Override
    public SUserBuilder setPassword(final String password) {
        entity.setPassword(password);
        return this;
    }

    @Override
    public SUserBuilder setFirstName(final String firstName) {
        entity.setFirstName(firstName);
        return this;
    }

    @Override
    public SUserBuilder setLastName(final String lastName) {
        entity.setLastName(lastName);
        return this;
    }

    @Override
    public SUserBuilder setTitle(final String title) {
        entity.setTitle(title);
        return this;
    }

    @Override
    public SUserBuilder setJobTitle(final String jobTitle) {
        entity.setJobTitle(jobTitle);
        return this;
    }

    @Override
    public SUserBuilder setManagerUserId(final long managerUserId) {
        entity.setManagerUserId(managerUserId);
        return this;
    }

    @Override
    public SUserBuilder setDelegeeUserName(final String delegeeUserName) {
        entity.setDelegeeUserName(delegeeUserName);
        return this;
    }

    @Override
    public SUser done() {
        return entity;
    }

    @Override
    public String getIdKey() {
        return ID;
    }

    @Override
    public String getUserNameKey() {
        return USER_NAME;
    }

    @Override
    public String getPasswordKey() {
        return PASSWORD;
    }

    @Override
    public String getFirstNameKey() {
        return FIRST_NAME;
    }

    @Override
    public String getLastNameKey() {
        return LAST_NAME;
    }

    @Override
    public String getTitleKey() {
        return TITLE;
    }

    @Override
    public String getJobTitleKey() {
        return JOB_TITLE;
    }

    @Override
    public String getManagerUserIdKey() {
        return MANAGER_USER_ID;
    }

    @Override
    public String getDelegeeUserNameKey() {
        return DELEGEE_USER_NAME;
    }

    @Override
    public SUserBuilder setIconName(final String iconName) {
        entity.setIconName(iconName);
        return this;
    }

    @Override
    public SUserBuilder setIconPath(final String iconPath) {
        entity.setIconPath(iconPath);
        return this;
    }

    @Override
    public SUserBuilder setLastUpdate(final long lastUpdate) {
        entity.setLastUpdate(lastUpdate);
        return this;
    }

    @Override
    public SUserBuilder setLastConnection(final Long lastConnection) {
        entity.setLastConnection(lastConnection);
        return this;
    }

    @Override
    public SUserBuilder setCreatedBy(final long createdBy) {
        entity.setCreatedBy(createdBy);
        return this;
    }

    @Override
    public SUserBuilder setCreationDate(final long creationDate) {
        entity.setCreationDate(creationDate);
        return this;
    }

    @Override
    public SUserBuilder setEnabled(final boolean enabled) {
        entity.setEnabled(enabled);
        return this;
    }

    @Override
    public String getIconNameKey() {
        return ICON_NAME;
    }

    @Override
    public String getIconPathKey() {
        return ICON_PATH;
    }

    @Override
    public String getCreatedByKey() {
        return CREATED_BY;
    }

    @Override
    public String getCreationDateKey() {
        return CREATION_DATE;
    }

    @Override
    public String getLastUpdateKey() {
        return LAST_UPDATE;
    }

    @Override
    public String getLastConnectionKey() {
        return LAST_CONNECTION;
    }

    @Override
    public String getEnabledKey() {
        return ENABLED;
    }

}
