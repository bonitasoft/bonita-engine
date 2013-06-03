/**
 * Copyright (C) 2011 BonitaSoft S.A.
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

import org.bonitasoft.engine.identity.model.builder.UserUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class UserUpdateBuilderImpl implements UserUpdateBuilder {

    private final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public UserUpdateBuilder updateUserName(final String username) {
        descriptor.addField(SUserBuilderImpl.USER_NAME, username);
        return this;
    }

    @Override
    public UserUpdateBuilder updatePassword(final String password) {
        descriptor.addField(SUserBuilderImpl.PASSWORD, password);
        return this;
    }

    @Override
    public UserUpdateBuilder updateFirstName(final String firstName) {
        descriptor.addField(SUserBuilderImpl.FIRST_NAME, firstName);
        return this;
    }

    @Override
    public UserUpdateBuilder updateLastName(final String lastName) {
        descriptor.addField(SUserBuilderImpl.LAST_NAME, lastName);
        return this;
    }

    @Override
    public UserUpdateBuilder updateTitle(final String title) {
        descriptor.addField(SUserBuilderImpl.TITLE, title);
        return this;
    }

    @Override
    public UserUpdateBuilder updateJobTitle(final String jobTitle) {
        descriptor.addField(SUserBuilderImpl.JOB_TITLE, jobTitle);
        return this;
    }

    @Override
    public UserUpdateBuilder updateManagerUserId(final long managerUserId) {
        descriptor.addField(SUserBuilderImpl.MANAGER_USER_ID, managerUserId);
        return this;
    }

    @Override
    public UserUpdateBuilder updateDelegeeUserName(final String delegeeUserName) {
        descriptor.addField(SUserBuilderImpl.DELEGEE_USER_NAME, delegeeUserName);
        return this;
    }

    @Override
    public UserUpdateBuilder updateIconName(final String iconName) {
        descriptor.addField(SUserBuilderImpl.ICON_NAME, iconName);
        return this;
    }

    @Override
    public UserUpdateBuilder updateIconPath(final String iconPath) {
        descriptor.addField(SUserBuilderImpl.ICON_PATH, iconPath);
        return this;
    }

    @Override
    public UserUpdateBuilder updateLastUpdate(final long lastUpdate) {
        descriptor.addField(SUserBuilderImpl.LAST_UPDATE, lastUpdate);
        return this;
    }

    @Override
    public UserUpdateBuilder updateLastConnection(final long lastConnection) {
        descriptor.addField(SUserBuilderImpl.LAST_CONNECTION, lastConnection);
        return this;
    }

    @Override
    public UserUpdateBuilder updateEnabled(boolean enabled) {
        descriptor.addField(SUserBuilderImpl.ENABLED, enabled);
        return this;
    }
}
