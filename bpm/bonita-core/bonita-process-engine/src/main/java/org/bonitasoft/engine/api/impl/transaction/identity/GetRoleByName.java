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
package org.bonitasoft.engine.api.impl.transaction.identity;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SRole;

/**
 * @author Lu Kai
 */
public class GetRoleByName implements TransactionContentWithResult<SRole> {

    private final String roleName;

    private final IdentityService identityService;

    private SRole sRole;

    public GetRoleByName(final String roleName, final IdentityService identityService) {
        this.roleName = roleName;
        this.identityService = identityService;
    }

    @Override
    public void execute() throws SBonitaException {
        sRole = identityService.getRoleByName(roleName);
    }

    @Override
    public SRole getResult() {
        return sRole;
    }

}
