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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.identity.model.SContactInfo;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoValueBuilderFactory;
import org.bonitasoft.engine.identity.xml.ExportedCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.xml.ExportedCustomUserInfoValue;
import org.bonitasoft.engine.identity.xml.ExportedGroup;
import org.bonitasoft.engine.identity.xml.ExportedRole;
import org.bonitasoft.engine.identity.xml.ExportedUser;
import org.bonitasoft.engine.identity.xml.ExportedUserMembership;
import org.bonitasoft.engine.identity.xml.Organization;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class ExportOrganization implements TransactionContentWithResult<String> {

    private final IdentityService identityService;

    private Map<Long, String> userNames;

    private String xmlOrganization;

    private final int maxResults;

    public ExportOrganization(final IdentityService identityService, final int maxResults) {
        this.identityService = identityService;
        this.maxResults = maxResults;
    }

    @Override
    public void execute() throws SBonitaException {
        userNames = new HashMap<>(20);
        final List<SCustomUserInfoDefinition> customUserInfoDefinitions = getAllCustomUserInfoDefinitions();
        final List<ExportedUser> users = getAllUsers(getUserInfoDefinitionNames(customUserInfoDefinitions));

        // improvement: user server object to avoid useless conversion;
        final List<ExportedRole> roles = getAllRoles();

        final List<SGroup> groups = getAllGroups();
        final Map<Long, String> groupIdParentPath = new HashMap<>(groups.size());
        for (final SGroup group : groups) {
            groupIdParentPath.put(group.getId(), group.getParentPath());
        }
        final List<SUserMembership> userMemberships = getAllUserMemberships();

        // improvement: user server object to avoid useless conversion;
        final List<ExportedGroup> clientGroups = ModelConvertor.toExportedGroups(groups);

        // improvement: user server object to avoid useless conversion;
        final List<ExportedUserMembership> clientUserMemberships = ModelConvertor.toExportedUserMembership(userMemberships, userNames, groupIdParentPath);
        List<ExportedCustomUserInfoDefinition> customUserInfoDefinitionCreators = ModelConvertor.toExportedCustomUserInfoDefinition(customUserInfoDefinitions);
        final Organization organization = new Organization(users, roles, clientGroups, clientUserMemberships, customUserInfoDefinitionCreators);
        try {
            xmlOrganization = new OrganizationParser().convert(organization);
        } catch (JAXBException e) {
            throw new SBonitaReadException(e);
        }
    }

    private static Map<Long, String> getUserInfoDefinitionNames(final List<SCustomUserInfoDefinition> userInfoDefinitions) {
        final Map<Long, String> names = new HashMap<>(userInfoDefinitions.size());
        for (final SCustomUserInfoDefinition userInfoDefinition : userInfoDefinitions) {
            names.put(userInfoDefinition.getId(), userInfoDefinition.getName());
        }
        return names;
    }

    @Override
    public String getResult() {
        return xmlOrganization;
    }

    protected List<SCustomUserInfoDefinition> getAllCustomUserInfoDefinitions() throws SIdentityException {
        final List<SCustomUserInfoDefinition> allCustomUserInfoDefinitions = new ArrayList<>(5);
        List<SCustomUserInfoDefinition> currentPage = null;
        int startIndex = 0;
        do {
            currentPage = identityService.getCustomUserInfoDefinitions(startIndex, maxResults);
            allCustomUserInfoDefinitions.addAll(currentPage);
            startIndex += maxResults;
        } while (currentPage.size() == maxResults);
        return allCustomUserInfoDefinitions;
    }

    private List<SUserMembership> getAllUserMemberships() throws SIdentityException {
        final long numberOfUserMemberships = identityService.getNumberOfUserMemberships();
        final List<SUserMembership> sUserMemberships = new ArrayList<>();
        for (int startIndex = 0; startIndex < numberOfUserMemberships; startIndex = startIndex + maxResults) {
            sUserMemberships.addAll(identityService.getUserMemberships(startIndex, maxResults));
        }
        return sUserMemberships;
    }

    private List<SGroup> getAllGroups() throws SIdentityException {
        List<SGroup> groups;
        final long groupNumber = identityService.getNumberOfGroups();
        groups = new ArrayList<>();
        for (int startIndex = 0; startIndex < groupNumber; startIndex = startIndex + maxResults) {
            groups.addAll(identityService.getGroups(startIndex, maxResults));
        }
        return groups;
    }

    private List<ExportedRole> getAllRoles() throws SIdentityException {
        final long roleNumber = identityService.getNumberOfRoles();
        final List<ExportedRole> roles = new ArrayList<>();
        for (int startIndex = 0; startIndex < roleNumber; startIndex = startIndex + maxResults) {
            final List<SRole> sRoles = identityService.getRoles(startIndex, maxResults);
            roles.addAll(ModelConvertor.toExportedRoles(sRoles));
        }
        return roles;
    }

    private List<ExportedUser> getAllUsers(final Map<Long, String> userInfoDefinitionNames) throws SBonitaException {
        final long userNumber = identityService.getNumberOfUsers();
        final List<ExportedUser> users = new ArrayList<>();
        for (int startIndex = 0; startIndex <= userNumber; startIndex = startIndex + maxResults) {
            users.addAll(getNextUsersPage(startIndex, maxResults, userInfoDefinitionNames));
        }
        return users;
    }

    private List<ExportedUser> getNextUsersPage(final int startIndex, final int numberPerPage, final Map<Long, String> userInfoDefinitionNames)
            throws SBonitaException {
        final List<ExportedUser> currentUsersPage = new ArrayList<>(numberPerPage);
        final List<SUser> sUsers = identityService.getUsers(startIndex, numberPerPage);
        for (final SUser sUser : sUsers) {
            userNames.put(sUser.getId(), sUser.getUserName());
            currentUsersPage.add(toExportedUser(sUser, userInfoDefinitionNames));
        }
        return currentUsersPage;
    }

    protected ExportedUser toExportedUser(final SUser sUser, final Map<Long, String> userInfoDefinitionNames) throws SBonitaException {
        final String managerUserName = getManagerUsername(sUser);

        final ExportedUserBuilder clientUserbuilder = ExportedUserBuilderFactory.createNewInstance(sUser.getUserName(), sUser.getPassword());
        // Do not export dates and id
        clientUserbuilder.setPasswordEncrypted(true);
        clientUserbuilder.setFirstName(sUser.getFirstName());
        clientUserbuilder.setLastName(sUser.getLastName());
        clientUserbuilder.setTitle(sUser.getTitle());
        clientUserbuilder.setJobTitle(sUser.getJobTitle());
        clientUserbuilder.setEnabled(sUser.isEnabled());

        setManagerInfo(managerUserName, clientUserbuilder);
        setPersonalContactInfo(sUser, clientUserbuilder);
        setProfessionalContactInfo(sUser, clientUserbuilder);
        addCustomUserInfoValues(sUser.getId(), clientUserbuilder, userInfoDefinitionNames);
        return clientUserbuilder.done();
    }

    protected void addCustomUserInfoValues(final long userId, final ExportedUserBuilder clientUserbuilder, final Map<Long, String> userInfoDefinitionNames)
            throws SBonitaReadException {
        final List<SCustomUserInfoValue> userInfoValues = getAllCustomUserInfoForUser(userId);
        for (final SCustomUserInfoValue infoValue : userInfoValues) {
            final String definitionName = userInfoDefinitionNames.get(infoValue.getDefinitionId());
            clientUserbuilder.addCustomUserInfoValue(new ExportedCustomUserInfoValue(definitionName, infoValue.getValue()));
        }
    }

    protected List<SCustomUserInfoValue> getAllCustomUserInfoForUser(final long userId) throws SBonitaReadException {
        final List<SCustomUserInfoValue> allValues = new ArrayList<>(5);
        int fromIndex = 0;
        List<SCustomUserInfoValue> currentPage;
        do {
            final QueryOptions options = getQueryOptions(userId, fromIndex);
            currentPage = identityService.searchCustomUserInfoValue(options);
            allValues.addAll(currentPage);
            fromIndex += maxResults;
        } while (currentPage.size() == maxResults);
        return allValues;
    }

    private QueryOptions getQueryOptions(final long userId, final int fromIndex) {
        final SCustomUserInfoValueBuilderFactory keyProvider = BuilderFactory.get(SCustomUserInfoValueBuilderFactory.class);
        final OrderByOption orderByOption = new OrderByOption(SCustomUserInfoValue.class, keyProvider.getIdKey(), OrderByType.ASC);
        final FilterOption filterOption = new FilterOption(SCustomUserInfoValue.class, keyProvider.getUserIdKey(), userId);
        return new QueryOptions(fromIndex, maxResults, Collections.singletonList(orderByOption), Collections.singletonList(filterOption), null);
    }

    private void setPersonalContactInfo(final SUser sUser, final ExportedUserBuilder clientUserbuilder) throws SIdentityException {
        final SContactInfo persoInfo = identityService.getUserContactInfo(sUser.getId(), true);
        if (persoInfo != null) {
            clientUserbuilder.setPersonalData(ModelConvertor.toUserContactData(persoInfo));
        }
    }

    private void setProfessionalContactInfo(final SUser sUser, final ExportedUserBuilder clientUserbuilder) throws SIdentityException {
        final SContactInfo proInfo = identityService.getUserContactInfo(sUser.getId(), false);
        if (proInfo != null) {
            clientUserbuilder.setProfessionalData(ModelConvertor.toUserContactData(proInfo));
        }
    }

    private void setManagerInfo(final String managerUserName, final ExportedUserBuilder clientUserbuilder) {
        clientUserbuilder.setManagerUserName(managerUserName);
    }

    private String getManagerUsername(final SUser sUser) throws SUserNotFoundException {
        final long managerUserId = sUser.getManagerUserId();
        String managerUserName = null;
        if (managerUserId > 0) {
            managerUserName = userNames.get(managerUserId);
            if (managerUserName == null) {
                final SUser manager = identityService.getUser(sUser.getManagerUserId());
                userNames.put(manager.getId(), manager.getUserName());
                managerUserName = manager.getUserName();
            }
        }
        return managerUserName;
    }

}
