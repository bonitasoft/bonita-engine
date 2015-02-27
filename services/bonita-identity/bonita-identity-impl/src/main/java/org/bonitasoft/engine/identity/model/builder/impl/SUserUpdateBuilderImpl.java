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
package org.bonitasoft.engine.identity.model.builder.impl;

import org.bonitasoft.engine.identity.model.builder.SUserUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SUserUpdateBuilderImpl implements SUserUpdateBuilder {

    private final EntityUpdateDescriptor descriptor;

    public SUserUpdateBuilderImpl(final EntityUpdateDescriptor descriptor) {
        super();
        this.descriptor = descriptor;
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public SUserUpdateBuilder updateUserName(final String username) {
        descriptor.addField(SUserBuilderFactoryImpl.USER_NAME, username);
        return this;
    }

    @Override
    public SUserUpdateBuilder updatePassword(final String password) {
        descriptor.addField(SUserBuilderFactoryImpl.PASSWORD, password);
        return this;
    }

    @Override
    public SUserUpdateBuilder updateFirstName(final String firstName) {
        descriptor.addField(SUserBuilderFactoryImpl.FIRST_NAME, firstName);
        return this;
    }

    @Override
    public SUserUpdateBuilder updateLastName(final String lastName) {
        descriptor.addField(SUserBuilderFactoryImpl.LAST_NAME, lastName);
        return this;
    }

    @Override
    public SUserUpdateBuilder updateTitle(final String title) {
        descriptor.addField(SUserBuilderFactoryImpl.TITLE, title);
        return this;
    }

    @Override
    public SUserUpdateBuilder updateJobTitle(final String jobTitle) {
        descriptor.addField(SUserBuilderFactoryImpl.JOB_TITLE, jobTitle);
        return this;
    }

    @Override
    public SUserUpdateBuilder updateManagerUserId(final long managerUserId) {
        descriptor.addField(SUserBuilderFactoryImpl.MANAGER_USER_ID, managerUserId);
        return this;
    }

    @Override
    public SUserUpdateBuilder updateIconName(final String iconName) {
        descriptor.addField(SUserBuilderFactoryImpl.ICON_NAME, iconName);
        return this;
    }

    @Override
    public SUserUpdateBuilder updateIconPath(final String iconPath) {
        descriptor.addField(SUserBuilderFactoryImpl.ICON_PATH, iconPath);
        return this;
    }

    @Override
    public SUserUpdateBuilder updateLastUpdate(final long lastUpdate) {
        descriptor.addField(SUserBuilderFactoryImpl.LAST_UPDATE, lastUpdate);
        return this;
    }

    @Override
    public SUserUpdateBuilder updateLastConnection(final long lastConnection) {
        descriptor.addField(SUserBuilderFactoryImpl.LAST_CONNECTION, lastConnection);
        return this;
    }

    @Override
    public SUserUpdateBuilder updateEnabled(final boolean enabled) {
        descriptor.addField(SUserBuilderFactoryImpl.ENABLED, enabled);
        return this;
    }

    public static SUserUpdateBuilder getInstance() {
        return new SUserUpdateBuilderImpl(null);
    }
}
