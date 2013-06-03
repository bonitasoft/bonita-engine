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
package org.bonitasoft.engine.identity.model.builder;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public interface UserUpdateBuilder extends UpdateBuilder {

    UserUpdateBuilder updateUserName(final String userName);

    UserUpdateBuilder updatePassword(final String password);

    UserUpdateBuilder updateFirstName(final String firstName);

    UserUpdateBuilder updateLastName(final String lastName);

    UserUpdateBuilder updateTitle(final String title);

    UserUpdateBuilder updateJobTitle(final String jobTitle);

    UserUpdateBuilder updateManagerUserId(final long managerUserId);

    UserUpdateBuilder updateDelegeeUserName(final String delegeeUserName);

    UserUpdateBuilder updateIconName(final String iconName);

    UserUpdateBuilder updateIconPath(final String iconPath);

    UserUpdateBuilder updateLastUpdate(final long lastUpdate);

    UserUpdateBuilder updateLastConnection(final long lastConnection);

    UserUpdateBuilder updateEnabled(final boolean enabled);

}
