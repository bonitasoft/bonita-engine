/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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

import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.builder.RoleBuilder;
import org.bonitasoft.engine.identity.model.impl.SRoleImpl;

/**
 * @author Baptiste Mesta
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 */
public class RoleBuilderImpl implements RoleBuilder {

    private SRoleImpl role;

    static final String ID = "id";

    static final String NAME = "name";

    static final String DESCRIPTION = "description";

    static final String DISPLAY_NAME = "displayName";

    static final String ICON_NAME = "iconName";

    static final String ICON_PATH = "iconPath";

    static final String CREATED_BY = "createdBy";

    static final String CREATION_DATE = "creationDate";

    static final String LAST_UPDATE = "lastUpdate";

    @Override
    public RoleBuilder setName(final String name) {
        role.setName(name);
        return this;
    }

    @Override
    public RoleBuilder setDisplayName(final String displayName) {
        role.setDisplayName(displayName);
        return this;
    }

    @Override
    public RoleBuilder setDescription(final String description) {
        role.setDescription(description);
        return this;
    }

    @Override
    public RoleBuilder setIconName(final String iconName) {
        role.setIconName(iconName);
        return this;
    }

    @Override
    public RoleBuilder setIconPath(final String iconPath) {
        role.setIconPath(iconPath);
        return this;
    }

    @Override
    public RoleBuilder setCreatedBy(final long createdBy) {
        role.setCreatedBy(createdBy);
        return this;
    }

    @Override
    public RoleBuilder setCreationDate(final long creationDate) {
        role.setCreationDate(creationDate);
        return this;
    }

    @Override
    public RoleBuilder setLastUpdate(final long lastUpdate) {
        role.setLastUpdate(lastUpdate);
        return this;
    }

    @Override
    public RoleBuilder setId(final long id) {
        role.setId(id);
        return this;
    }

    @Override
    public RoleBuilder createNewInstance() {
        role = new SRoleImpl();
        return this;
    }

    @Override
    public SRole done() {
        return role;
    }

    @Override
    public String getIdKey() {
        return ID;
    }

    @Override
    public String getNameKey() {
        return NAME;
    }

    @Override
    public String getDisplayNameKey() {
        return DISPLAY_NAME;
    }

    @Override
    public String getDescriptionKey() {
        return DESCRIPTION;
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

}
