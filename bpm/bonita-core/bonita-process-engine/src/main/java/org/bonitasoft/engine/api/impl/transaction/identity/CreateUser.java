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

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SContactInfo;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.SContactInfoBuilderFactory;

/**
 * @author Lu Kai
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class CreateUser implements TransactionContentWithResult<SUser> {

    private final SUser sUser;

    private final IdentityService identityService;

    private final SContactInfo sPersonalData;

    private final SContactInfo sProfessionalData;

    private SUser user;

    public CreateUser(final SUser sUser, final SContactInfo sPersonalData, final SContactInfo sProfessionalData, final IdentityService identityService) {
        this.sUser = sUser;
        this.sPersonalData = sPersonalData;
        this.sProfessionalData = sProfessionalData;
        this.identityService = identityService;
    }

    @Override
    public void execute() throws SBonitaException {
        user = identityService.createUser(sUser);
        if (sPersonalData != null) {
            identityService.createUserContactInfo(BuilderFactory.get(SContactInfoBuilderFactory.class).createNewInstance(sPersonalData).setUserId(user.getId()).done());
        }
        if (sProfessionalData != null) {
            identityService.createUserContactInfo(BuilderFactory.get(SContactInfoBuilderFactory.class).createNewInstance(sProfessionalData).setUserId(user.getId()).done());
        }
    }

    @Override
    public SUser getResult() {
        return user;
    }

}
