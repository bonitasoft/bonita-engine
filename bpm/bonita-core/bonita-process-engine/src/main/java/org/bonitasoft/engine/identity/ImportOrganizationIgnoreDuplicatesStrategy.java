/**
 * Copyright (C) 2016 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.identity;

import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.xml.ExportedCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.xml.ExportedGroup;
import org.bonitasoft.engine.identity.xml.ExportedRole;
import org.bonitasoft.engine.identity.xml.ExportedUser;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class ImportOrganizationIgnoreDuplicatesStrategy implements ImportOrganizationStrategy {

    public ImportOrganizationIgnoreDuplicatesStrategy() {
    }

    @Override
    public void foundExistingGroup(final SGroup existingGroup, final ExportedGroup newGroup) {
    }

    @Override
    public void foundExistingUser(final SUser existingUser, final ExportedUser user) {
    }

    @Override
    public void foundExistingRole(final SRole existingRole, final ExportedRole newRole) {
    }

    @Override
    public void foundExistingMembership(final SUserMembership existingMembership) {
    }

    @Override
    public void foundExistingCustomUserInfoDefinition(final SCustomUserInfoDefinition existingUserInfoDefinition,
                                                      final ExportedCustomUserInfoDefinition newUserInfoDefinition) {
    }

}
