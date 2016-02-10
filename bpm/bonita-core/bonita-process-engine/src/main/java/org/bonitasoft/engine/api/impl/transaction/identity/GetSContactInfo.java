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
import org.bonitasoft.engine.identity.model.SContactInfo;

/**
 * @author Emmanuel Duchastenier
 */
public class GetSContactInfo implements TransactionContentWithResult<SContactInfo> {

    private final IdentityService identityService;

    private final long userId;

    private final boolean personal;

    private SContactInfo sContactInfo;

    public GetSContactInfo(final long userId, final IdentityService identityService, final boolean personal) {
        super();
        this.userId = userId;
        this.identityService = identityService;
        this.personal = personal;
    }

    @Override
    public void execute() throws SBonitaException {
        sContactInfo = identityService.getUserContactInfo(userId, personal);
    }

    @Override
    public SContactInfo getResult() {
        return sContactInfo;
    }

}
