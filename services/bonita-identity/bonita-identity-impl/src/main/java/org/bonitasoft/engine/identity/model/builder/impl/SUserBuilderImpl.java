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

    private final SUserImpl entity;

    public SUserBuilderImpl(final SUserImpl entity) {
        super();
        this.entity = entity;
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
    public SUser done() {
        return entity;
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
}
