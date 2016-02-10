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
import org.bonitasoft.engine.identity.model.SUser;

/**
 * @author Lu Kai
 * @author Matthieu Chaffotte
 */
public class GetSUser implements TransactionContentWithResult<SUser> {

    private final IdentityService identityService;

    private final String userName;

    private final long userId;

    private SUser sUser;

    public GetSUser(final IdentityService identityService, final String userName) {
        this.identityService = identityService;
        this.userName = userName;
        userId = -1;
    }

    public GetSUser(final IdentityService identityService, final long userId) {
        this.identityService = identityService;
        this.userId = userId;
        userName = null;
    }

    @Override
    public void execute() throws SBonitaException {
        if (userName != null) {
            sUser = identityService.getUserByUserName(userName);
        } else {
            sUser = identityService.getUser(userId);
        }
    }

    @Override
    public SUser getResult() {
        return sUser;
    }

}
