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
package org.bonitasoft.engine.profile.builder;

import org.bonitasoft.engine.profile.model.SProfile;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface SProfileBuilderFactory {

    String PROFILE_IDS = "profileIds";

    String ICON_PATH = "iconPath";

    String DESCRIPTION = "description";

    String NAME = "name";

    String ID = "id";

    String IS_DEFAULT = "isDefault";

    String CREATION_DATE = "creationDate";

    String CREATED_BY = "createdBy";

    String LAST_UPDATE_DATE = "lastUpdateDate";

    String LAST_UPDATED_BY = "lastUpdatedBy";

    SProfileBuilder createNewInstance(SProfile profile);

    SProfileBuilder createNewInstance(String name, boolean isDefault, long creationDate, long createdBy, long lastUpdateDate, long lastUpdatedBy);

}
