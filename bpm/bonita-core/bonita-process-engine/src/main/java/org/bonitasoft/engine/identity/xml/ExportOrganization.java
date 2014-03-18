/**
 * Copyright (C) 2009-2013 BonitaSoft S.A.
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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.identity.ExportedUser;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.identity.model.SContactInfo;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.xml.XMLNode;
import org.bonitasoft.engine.xml.XMLWriter;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class ExportOrganization implements TransactionContentWithResult<String> {

    private final XMLWriter xmlWriter;

    private final IdentityService identityService;

    private Map<Long, String> userNames;

    private String xmlOrganization;

    private final int maxResults;

    public ExportOrganization(final XMLWriter xmlWriter, final IdentityService identityService, int maxResults) {
        this.xmlWriter = xmlWriter;
        this.identityService = identityService;
        this.maxResults = maxResults;
    }

    @Override
    public void execute() throws SBonitaException {
        userNames = new HashMap<Long, String>(20);
        final List<ExportedUser> users = getAllUsers();
        
        //improvement: user server object to avoid useless conversion;
        final List<Role> roles = getAllRoles();
        
        final List<SGroup> groups = getAllGroups();
        final Map<Long, String> groupIdParentPath = new HashMap<Long, String>(groups.size());
        for (final SGroup group : groups) {
            groupIdParentPath.put(group.getId(), group.getParentPath());
        }
        final List<SUserMembership> userMemberships = getAllUserMemberships();

        //improvement: user server object to avoid useless conversion;
        final List<Group> clientGroups = ModelConvertor.toGroups(groups);

        //improvement: user server object to avoid useless conversion;
        final List<UserMembership> clientUserMemberships = ModelConvertor.toUserMembership(userMemberships, userNames, groupIdParentPath);
        List<SCustomUserInfoDefinition> customUserInfoDefinitions = getAllCustomUserInfoDefinitions();
        Organization organization = new Organization(users, roles, clientGroups, clientUserMemberships, customUserInfoDefinitions);
        buildXmlContent(organization, groupIdParentPath);
    }

    private void buildXmlContent(Organization organization, final Map<Long, String> groupIdParentPath) throws SIdentityException {
        final XMLNode document = OrganizationNodeBuilder.getDocument(organization, userNames, groupIdParentPath);
        final StringWriter writer = new StringWriter();
        try {
            xmlWriter.write(document, writer);
            writer.close();
        } catch (final IOException e) {
            throw new SIdentityException(e);
        }
        xmlOrganization = writer.toString();
    }

    @Override
    public String getResult() {
        return xmlOrganization;
    }
    
    protected List<SCustomUserInfoDefinition> getAllCustomUserInfoDefinitions() throws SIdentityException {
        List<SCustomUserInfoDefinition> allCustomUserInfoDefinitions = new ArrayList<SCustomUserInfoDefinition>(5);
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
        final List<SUserMembership> sUserMemberships = new ArrayList<SUserMembership>();
        for (int startIndex = 0; startIndex < numberOfUserMemberships; startIndex = startIndex + maxResults) {
            sUserMemberships.addAll(identityService.getUserMemberships(startIndex, maxResults));
        }
        return sUserMemberships;
    }

    private List<SGroup> getAllGroups() throws SIdentityException {
        List<SGroup> groups;
        final long groupNumber = identityService.getNumberOfGroups();
        groups = new ArrayList<SGroup>(getInitialListCapacity(groupNumber));
        for (int startIndex = 0; startIndex < groupNumber; startIndex = startIndex + maxResults) {
            groups.addAll(identityService.getGroups(startIndex, maxResults));
        }
        return groups;
    }

    private List<Role> getAllRoles() throws SIdentityException {
        final long roleNumber = identityService.getNumberOfRoles();
        final List<Role> roles = new ArrayList<Role>(getInitialListCapacity(roleNumber));
        for (int startIndex = 0; startIndex < roleNumber; startIndex = startIndex + maxResults) {
            final List<SRole> sRoles = identityService.getRoles(startIndex, maxResults);
            roles.addAll(ModelConvertor.toRoles(sRoles));
        }
        return roles;
    }

    private List<ExportedUser> getAllUsers() throws SIdentityException {
        final long userNumber = identityService.getNumberOfUsers();
        final List<ExportedUser> users = new ArrayList<ExportedUser>(getInitialListCapacity(userNumber));
        for (int startIndex = 0; startIndex <= userNumber; startIndex = startIndex + maxResults) {
            users.addAll(getNextUsersPage(startIndex, maxResults));
        }
        return users;
    }

    private int getInitialListCapacity(final long elementsToRetrive) {
        return Integer.MAX_VALUE >= elementsToRetrive ? Long.valueOf(elementsToRetrive).intValue() : Integer.MAX_VALUE;
    }

    private List<ExportedUser> getNextUsersPage(int startIndex, final int numberPerPage) throws SIdentityException, SUserNotFoundException {
        List<ExportedUser> currentUsersPage = new ArrayList<ExportedUser>(numberPerPage);
        final List<SUser> sUsers = identityService.getUsers(startIndex, numberPerPage);
        for (final SUser sUser : sUsers) {
            final SContactInfo persoInfo = identityService.getUserContactInfo(sUser.getId(), true);
            final SContactInfo proInfo = identityService.getUserContactInfo(sUser.getId(), false);
            String managerUserName = getManagerUsername(sUser);
            userNames.put(sUser.getId(), sUser.getUserName());
            currentUsersPage.add(ModelConvertor.toExportedUser(sUser, persoInfo, proInfo, managerUserName));
        }
        return currentUsersPage;
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
