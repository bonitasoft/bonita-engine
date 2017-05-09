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


/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public interface SProfileMemberBuilderFactory {

    String DISPLAY_NAME_PART3 = "displayNamePart3";

    String DISPLAY_NAME_PART2 = "displayNamePart2";

    String DISPLAY_NAME_PART1 = "displayNamePart1";

    String ROLE_ID = "roleId";

    String GROUP_ID = "groupId";

    String USER_ID = "userId";

    String PROFILE_ID = "profileId";

    String ID = "id";

    SProfileMemberBuilder createNewInstance(long profileId);

    String getIdKey();

}
