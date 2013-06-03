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
package org.bonitasoft.engine.identity.model.builder;

import org.bonitasoft.engine.identity.model.SRole;

/**
 * @author Baptiste Mesta
 * @author Bole Zhang
 * @author Bole Zhang
 */
public interface RoleBuilder {

    RoleBuilder setId(final long id);

    RoleBuilder setName(final String name);

    RoleBuilder setDisplayName(final String displayName);

    RoleBuilder setDescription(final String description);

    RoleBuilder setIconName(final String iconName);

    RoleBuilder setIconPath(final String iconPath);

    RoleBuilder setCreatedBy(final long createdBy);

    RoleBuilder setCreationDate(final long creationDate);

    RoleBuilder setLastUpdate(final long lastUpdate);

    RoleBuilder createNewInstance();

    SRole done();

    String getIdKey();

    String getNameKey();

    String getDisplayNameKey();

    String getDescriptionKey();

    String getIconNameKey();

    String getIconPathKey();

    String getCreatedByKey();

    String getCreationDateKey();

    String getLastUpdateKey();

}
