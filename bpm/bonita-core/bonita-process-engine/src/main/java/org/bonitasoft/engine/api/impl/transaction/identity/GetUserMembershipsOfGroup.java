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
package org.bonitasoft.engine.api.impl.transaction.identity;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SUserMembership;

/**
 * @author Bole Zhang
 * @author Baptiste Mesta
 */
public class GetUserMembershipsOfGroup implements TransactionContentWithResult<List<SUserMembership>> {

    private final long groupId;

    private final IdentityService identityService;

    private List<SUserMembership> sMembership;

    public GetUserMembershipsOfGroup(final long groupId, final IdentityService identityService) {
        this.groupId = groupId;
        this.identityService = identityService;
    }

    @Override
    public void execute() throws SBonitaException {
        sMembership = identityService.getUserMembershipsOfGroup(groupId);
    }

    @Override
    public List<SUserMembership> getResult() {
        return sMembership;
    }

}
