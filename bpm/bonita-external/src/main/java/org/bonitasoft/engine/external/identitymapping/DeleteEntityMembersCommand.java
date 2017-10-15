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
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * Delete all entity members related to the provided external ID.
 * Parameter keys: EXTERNAL_ID_KEY: external id provided as is by the external system, DISCRIMINATOR_ID_KEY: the discriminator to isolate the different
 * functional notions.
 * 
 * @author Emmanuel Duchastenier
 */
public class DeleteEntityMembersCommand extends EntityMemberCommand {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        this.serviceAccessor = serviceAccessor;
        String externalId = getStringMandadoryParameter(parameters, EXTERNAL_ID_KEY);
        String kind = getStringMandadoryParameter(parameters, DISCRIMINATOR_ID_KEY);
        try {
            deleteExternalIdentityMappings(externalId, kind);
            // everything went right:
            return Boolean.TRUE;
        } catch (SBonitaException e) {
            throw new SCommandExecutionException("Error executing command 'DeleteEntityMembersCommand'", e);
        }
    }

}
