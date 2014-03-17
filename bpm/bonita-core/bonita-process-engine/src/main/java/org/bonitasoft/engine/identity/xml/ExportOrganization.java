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

    private String organization;

    private static final int NUMBER_PER_PAGE = 20;

    public ExportOrganization(final XMLWriter xmlWriter, final IdentityService identityService) {
        this.xmlWriter = xmlWriter;
        this.identityService = identityService;
    }

    @Override
    public void execute() throws SBonitaException {
        userNames = new HashMap<Long, String>(20);
        final List<ExportedUser> users = getAllUsers();
        final List<Role> roles = getAllRoles();
        final List<SGroup> groups = getAllGroups();
        final Map<Long, String> groupIdParentPath = new HashMap<Long, String>(groups.size());
        for (final SGroup group : groups) {
            groupIdParentPath.put(group.getId(), group.getParentPath());
        }
        final List<SUserMembership> userMemberships = getAllUserMemberships();
        final List<Group> clientGroups = ModelConvertor.toGroups(groups);
        final List<UserMembership> clientUserMemberships = ModelConvertor.toUserMembership(userMemberships, userNames, groupIdParentPath);
        buildXmlContent(users, roles, groupIdParentPath, clientGroups, clientUserMemberships);
    }

    private void buildXmlContent(final List<ExportedUser> users, final List<Role> roles, final Map<Long, String> groupIdParentPath,
            final List<Group> clientGroups, final List<UserMembership> clientUserMemberships) throws SIdentityException {
        final XMLNode document = OrganizationNodeBuilder.getDocument(users, userNames, clientGroups, groupIdParentPath, roles, clientUserMemberships);
        final StringWriter writer = new StringWriter();
        try {
            xmlWriter.write(document, writer);
            writer.close();
        } catch (final IOException e) {
            throw new SIdentityException(e);
        }
        organization = writer.toString();
    }

    @Override
    public String getResult() {
        return organization;
    }

    private List<SUserMembership> getAllUserMemberships() throws SIdentityException {
        final long numberOfUserMemberships = identityService.getNumberOfUserMemberships();
        final List<SUserMembership> sUserMemberships = new ArrayList<SUserMembership>();
        for (int startIndex = 0; startIndex < numberOfUserMemberships; startIndex = startIndex + NUMBER_PER_PAGE) {
            sUserMemberships.addAll(identityService.getUserMemberships(startIndex, NUMBER_PER_PAGE));
        }
        return sUserMemberships;
    }

    private List<SGroup> getAllGroups() throws SIdentityException {
        List<SGroup> groups;
        final long groupNumber = identityService.getNumberOfGroups();
        groups = new ArrayList<SGroup>(getInitialListCapacity(groupNumber));
        for (int startIndex = 0; startIndex < groupNumber; startIndex = startIndex + NUMBER_PER_PAGE) {
            groups.addAll(identityService.getGroups(startIndex, NUMBER_PER_PAGE));
        }
        return groups;
    }

    private List<Role> getAllRoles() throws SIdentityException {
        final long roleNumber = identityService.getNumberOfRoles();
        final List<Role> roles = new ArrayList<Role>(getInitialListCapacity(roleNumber));
        for (int startIndex = 0; startIndex < roleNumber; startIndex = startIndex + NUMBER_PER_PAGE) {
            final List<SRole> sRoles = identityService.getRoles(startIndex, NUMBER_PER_PAGE);
            roles.addAll(ModelConvertor.toRoles(sRoles));
        }
        return roles;
    }

    private List<ExportedUser> getAllUsers() throws SIdentityException {
        final long userNumber = identityService.getNumberOfUsers();
        final List<ExportedUser> users = new ArrayList<ExportedUser>(getInitialListCapacity(userNumber));
        for (int startIndex = 0; startIndex <= userNumber; startIndex = startIndex + NUMBER_PER_PAGE) {
            users.addAll(getNextUsersPage(startIndex, NUMBER_PER_PAGE));
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
