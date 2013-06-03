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

import org.bonitasoft.engine.identity.model.SGroup;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public interface GroupBuilder {

    GroupBuilder createNewInstance();

    GroupBuilder setId(final long id);

    GroupBuilder setName(final String name);

    GroupBuilder setDisplayName(final String displayName);

    GroupBuilder setDescription(final String description);

    GroupBuilder setParentPath(final String parentPath);

    GroupBuilder setIconName(final String iconName);

    GroupBuilder setIconPath(final String iconPath);

    GroupBuilder setCreatedBy(final long createdBy);

    GroupBuilder setCreationDate(final long creationDate);

    GroupBuilder setLastUpdate(final long lastUpdate);

    SGroup done();

    String getIdKey();

    String getNameKey();

    String getDisplayNameKey();

    String getDescriptionKey();

    String getIconNameKey();

    String getIconPathKey();

    String getCreatedByKey();

    String getCreationDateKey();

    String getLastUpdateKey();

    String getParentPathKey();

}
