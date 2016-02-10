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
package org.bonitasoft.engine.identity.xml;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator;
import org.bonitasoft.engine.identity.ExportedUser;
import org.bonitasoft.engine.identity.GroupCreator;
import org.bonitasoft.engine.identity.RoleCreator;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public interface ImportOrganizationStrategy {

    void foundExistingMembership(final SUserMembership existingMembership) throws ImportDuplicateInOrganizationException;

    void foundExistingRole(final SRole existingRole, final RoleCreator newRole) throws ImportDuplicateInOrganizationException, SIdentityException;

    void foundExistingUser(final SUser existingUser, final ExportedUser user) throws SBonitaException;

    void foundExistingGroup(final SGroup existingGroup, final GroupCreator newGroup) throws ImportDuplicateInOrganizationException, SIdentityException;

    void foundExistingCustomUserInfoDefinition(SCustomUserInfoDefinition existingUserInfoDefinition, CustomUserInfoDefinitionCreator newUserInfoDefinition) throws ImportDuplicateInOrganizationException, SIdentityException;

}
