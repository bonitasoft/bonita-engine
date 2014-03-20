/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.identity.ExportedUser;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SUserCreationException;
import org.bonitasoft.engine.identity.model.SContactInfo;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.SUserBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserBuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Elias Ricken de Medeiros
 */
public class UserImporter {

    private IdentityService identityService;

    private ImportOrganizationStrategy strategy;

    private long userIdFromSession;

    private CustomUserInfoValueImporter infoValueImporter;

    public UserImporter(TenantServiceAccessor serviceAccessor, final ImportOrganizationStrategy strategy, long userIdFromSession,
            CustomUserInfoValueImporter infoValueImporter) {
        this.strategy = strategy;
        this.userIdFromSession = userIdFromSession;
        this.infoValueImporter = infoValueImporter;
        identityService = serviceAccessor.getIdentityService();
    }

    public Map<String, SUser> importUsers(final List<ExportedUser> usersToImport) throws SBonitaException {
        final Map<String, SUser> userNameToSUsers = new HashMap<String, SUser>((int) Math.min(Integer.MAX_VALUE, identityService.getNumberOfUsers()));
        for (final ExportedUser userToImport : usersToImport) {
            SUser sUser = null;
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

    private boolean hasUserWithUserName(String userName) throws SBonitaSearchException {
        SUserBuilderFactory keyProvider = BuilderFactory.get(SUserBuilderFactory.class);
        FilterOption filter = new FilterOption(SUser.class, keyProvider.getUserNameKey(), userName);
        QueryOptions queryOptions = new QueryOptions(Collections.singletonList(filter), null);
        long numberOfUsers = identityService.getNumberOfUsers(queryOptions);
        return numberOfUsers > 0;
    }

    private SUser addAllUserInfo(final ExportedUser userToImport) throws SBonitaException {
        SUser persistedUser = addUser(userToImport);
        addContactInfo(userToImport, persistedUser);
        infoValueImporter.imporCustomUserInfoValues(userToImport.getCustomUserInfoValues(), persistedUser.getId());
        return persistedUser;
    }

    private void addContactInfo(final ExportedUser userToImport, SUser persistedUser) throws SUserCreationException {
        final SContactInfo persoSContactInfo = ModelConvertor.constructSUserContactInfo(userToImport, true, persistedUser.getId());
        identityService.createUserContactInfo(persoSContactInfo);
        final SContactInfo professSContactInfo = ModelConvertor.constructSUserContactInfo(userToImport, false, persistedUser.getId());
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

    private SUser constructSUser(final ExportedUser newUser) {
        final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance();
        final long now = System.currentTimeMillis();
        userBuilder.setCreationDate(now);
        userBuilder.setLastUpdate(now);
        userBuilder.setLastConnection(null);

        userBuilder.setUserName(newUser.getUserName());
        userBuilder.setPassword(newUser.getPassword());
        userBuilder.setFirstName(newUser.getFirstName());
        userBuilder.setLastName(newUser.getLastName());
        userBuilder.setIconName(newUser.getIconName());
        userBuilder.setIconPath(newUser.getIconPath());
        userBuilder.setJobTitle(newUser.getJobTitle());
        userBuilder.setTitle(newUser.getTitle());
        userBuilder.setCreatedBy(newUser.getCreatedBy() == 0 ? userIdFromSession : newUser.getCreatedBy());
        userBuilder.setManagerUserId(newUser.getManagerUserId());
        userBuilder.setEnabled(newUser.isEnabled());
        return userBuilder.done();
    }

}
