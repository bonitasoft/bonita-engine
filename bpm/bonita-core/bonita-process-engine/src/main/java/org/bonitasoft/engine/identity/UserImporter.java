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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.identity.model.SContactInfo;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.xml.ExportedUser;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Elias Ricken de Medeiros
 */
public class UserImporter {

    private final IdentityService identityService;

    private final ImportOrganizationStrategy strategy;

    private final long userIdFromSession;

    private final CustomUserInfoValueImporter infoValueImporter;

    public UserImporter(IdentityService identityService, final ImportOrganizationStrategy strategy,
            long userIdFromSession,
            CustomUserInfoValueImporter infoValueImporter) {
        this.strategy = strategy;
        this.userIdFromSession = userIdFromSession;
        this.infoValueImporter = infoValueImporter;
        this.identityService = identityService;
    }

    public Map<String, SUser> importUsers(final List<ExportedUser> usersToImport) throws SBonitaException {
        final Map<String, SUser> userNameToSUsers = new HashMap<>(
                (int) Math.min(Integer.MAX_VALUE, identityService.getNumberOfUsers()));
        for (final ExportedUser userToImport : usersToImport) {
            SUser sUser;
            if (hasUserWithUserName(userToImport.getUserName())) {
                sUser = identityService.getUserByUserName(userToImport.getUserName());
                strategy.foundExistingUser(sUser, userToImport);
            } else {
                sUser = addAllUserInfo(userToImport);
            }
            userNameToSUsers.put(sUser.getUserName(), sUser);
        }
        return userNameToSUsers;
    }

    private boolean hasUserWithUserName(String userName) throws SBonitaReadException {
        final FilterOption filter = new FilterOption(SUser.class, SUser.USER_NAME, userName);
        final QueryOptions queryOptions = new QueryOptions(Collections.singletonList(filter), null);
        final long numberOfUsers = identityService.getNumberOfUsers(queryOptions);
        return numberOfUsers > 0;
    }

    private SUser addAllUserInfo(final ExportedUser userToImport) throws SBonitaException {
        final SUser persistedUser = addUser(userToImport);
        addContactInfo(userToImport, persistedUser);
        infoValueImporter.imporCustomUserInfoValues(userToImport.getCustomUserInfoValues(), persistedUser.getId());
        return persistedUser;
    }

    private void addContactInfo(final ExportedUser userToImport, SUser persistedUser) throws SUserCreationException {
        final SContactInfo persoSContactInfo = ModelConvertor.constructSUserContactInfo(userToImport, true,
                persistedUser.getId());
        identityService.createUserContactInfo(persoSContactInfo);
        final SContactInfo professSContactInfo = ModelConvertor.constructSUserContactInfo(userToImport, false,
                persistedUser.getId());
        identityService.createUserContactInfo(professSContactInfo);
    }

    private SUser addUser(final ExportedUser user) throws SUserCreationException {
        SUser sUser;
        if (user.isPasswordEncrypted()) {
            sUser = identityService.createUserWithoutEncryptingPassword(constructSUser(user));
        } else {
            sUser = identityService.createUser(constructSUser(user));
        }
        return sUser;
    }

    private SUser constructSUser(final ExportedUser exportedUser) {
        final SUser.SUserBuilder userBuilder = SUser.builder();
        final long now = System.currentTimeMillis();
        userBuilder.creationDate(now);
        userBuilder.lastUpdate(now);

        userBuilder.userName(exportedUser.getUserName());
        userBuilder.password(exportedUser.getPassword());
        userBuilder.firstName(exportedUser.getFirstName());
        userBuilder.lastName(exportedUser.getLastName());
        userBuilder.jobTitle(exportedUser.getJobTitle());
        userBuilder.title(exportedUser.getTitle());
        userBuilder.createdBy(userIdFromSession);
        userBuilder.enabled(exportedUser.isEnabled());
        return userBuilder.build();
    }
}
