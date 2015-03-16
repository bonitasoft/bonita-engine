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
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Lu Kai
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class UpdateUser implements TransactionContentWithResult<SUser> {

    private SUser sUser;

    private final long userId;

    private final IdentityService identityService;

    private final EntityUpdateDescriptor changeDescriptor;

    private final EntityUpdateDescriptor personalDataUpdateDescriptor;

    private final EntityUpdateDescriptor professionalDataUpdateDescriptor;

    public UpdateUser(final IdentityService identityService, final long userId, final EntityUpdateDescriptor changeDescriptor,
            final EntityUpdateDescriptor personalDataUpDescriptor, final EntityUpdateDescriptor professionalDataUpDescriptor) {
        this.userId = userId;
        personalDataUpdateDescriptor = personalDataUpDescriptor;
        professionalDataUpdateDescriptor = professionalDataUpDescriptor;
        sUser = null;
        this.identityService = identityService;
        this.changeDescriptor = changeDescriptor;
    }

    public UpdateUser(final IdentityService identityService, final SUser sUser, final EntityUpdateDescriptor changeDescriptor,
            final EntityUpdateDescriptor personalDataUpDescriptor, final EntityUpdateDescriptor professionalDataUpDescriptor) {
        userId = -1;
        personalDataUpdateDescriptor = personalDataUpDescriptor;
        professionalDataUpdateDescriptor = professionalDataUpDescriptor;
        this.sUser = sUser;
        this.identityService = identityService;
        this.changeDescriptor = changeDescriptor;
    }

    @Override
    public void execute() throws SBonitaException {
        // User change
        if (changeDescriptor != null && !changeDescriptor.getFields().isEmpty()) {
            if (sUser == null) {
                sUser = identityService.getUser(userId);
            }
            identityService.updateUser(sUser, changeDescriptor);
        }

        // Personal data change
        if (personalDataUpdateDescriptor != null && !personalDataUpdateDescriptor.getFields().isEmpty()) {
            SContactInfo persoContactInfo = identityService.getUserContactInfo(userId, true);
            if (persoContactInfo == null) {
                persoContactInfo = BuilderFactory.get(SContactInfoBuilderFactory.class).createNewInstance(userId, true).done();
                identityService.createUserContactInfo(persoContactInfo);
            }
            identityService.updateUserContactInfo(persoContactInfo, personalDataUpdateDescriptor);
        }

        // Professional data change
        if (professionalDataUpdateDescriptor != null && !professionalDataUpdateDescriptor.getFields().isEmpty()) {
            SContactInfo professContactInfo = identityService.getUserContactInfo(userId, false);
            if (professContactInfo == null) {
                professContactInfo = BuilderFactory.get(SContactInfoBuilderFactory.class).createNewInstance(userId, false).done();
                identityService.createUserContactInfo(professContactInfo);
            }
            identityService.updateUserContactInfo(professContactInfo, professionalDataUpdateDescriptor);
        }

        // Refresh user for getResult
        if (sUser == null) {
            sUser = identityService.getUser(userId);
        } else {
            sUser = identityService.getUser(sUser.getId());
        }
    }

    @Override
    public SUser getResult() {
        return sUser;
    }
}
