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
package org.bonitasoft.engine.external.identitymapping;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMapping;
import org.bonitasoft.engine.identity.MemberType;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * Parameter keys: EXTERNAL_ID_KEY: external id provided as is by the external system, USER_ID_KEY: -1 is not needed, ROLE_ID_KEY: -1 is not needed,
 * GROUP_ID_KEY: -1 is not needed, DISCRIMINATOR_ID_KEY: the discriminator to isolate the different functional notions.
 * 
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class AddEntityMemberCommand extends EntityMemberCommand {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        this.serviceAccessor = serviceAccessor;
        String externalId = getStringMandadoryParameter(parameters, EXTERNAL_ID_KEY);
        String kind = getStringMandadoryParameter(parameters, DISCRIMINATOR_ID_KEY);
        final Long userId = getUserIdParameter(parameters);
        final Long groupId = getGroupIdParameter(parameters);
        final Long roleId = getRoleIdParameter(parameters);

        if (userId == null && groupId == null && roleId == null) {
            throw new SCommandParameterizationException("At least one of the following parameters must be set : userId, groupId, roleId");
        }

        final long lUserId = userId != null ? userId : -1;
        final long lGroupId = groupId != null ? groupId : -1;
        final long lRoleId = roleId != null ? roleId : -1;

        try {
            final MemberType memberType = getMemberType(userId, groupId, roleId);
            SExternalIdentityMapping mapp = addExternalIdentityMapping(externalId, lUserId, lRoleId, lGroupId, kind, memberType);
            return toEntityMember(mapp);
        } catch (SCommandExecutionException e) {
            throw e;
        } catch (SBonitaException e) {
            throw new SCommandExecutionException("Error executing command 'AddEntityMemberCommand'", e);
        }
    }

}
