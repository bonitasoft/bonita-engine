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
 */
public class ExportOrganization implements TransactionContentWithResult<String> {

    private final XMLWriter xmlWriter;

    private final IdentityService identityService;

    private Map<Long, String> userNames;

    private String organization;

    public ExportOrganization(final XMLWriter xmlWriter, final IdentityService identityService) {
        this.xmlWriter = xmlWriter;
        this.identityService = identityService;
    }

    @Override
    public void execute() throws SBonitaException {
        userNames = new HashMap<Long, String>(20);
        final int numberPerPage = 20;
        final List<ExportedUser> users = getAllUsers(numberPerPage);
        userNames = new HashMap<Long, String>();
        for (final ExportedUser user : users) {
            userNames.put(user.getId(), user.getUserName());
        }
        final List<Role> roles = getAllRoles(numberPerPage);
        final List<SGroup> groups = getAllGroups(numberPerPage);
        final Map<Long, String> groupIdParentPath = new HashMap<Long, String>(groups.size());
        for (final SGroup group : groups) {
            groupIdParentPath.put(group.getId(), group.getParentPath());
        }
        final List<SUserMembership> userMemberships = getAllUserMemberships(numberPerPage);
        final List<Group> clientGroups = ModelConvertor.toGroups(groups);
        final List<UserMembership> clientUserMemberships = ModelConvertor.toUserMembership(userMemberships, userNames, groupIdParentPath);
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

    private List<SUserMembership> getAllUserMemberships(final int numberPerPage) throws SIdentityException {
        final long numberOfUserMemberships = identityService.getNumberOfUserMemberships();
        final List<SUserMembership> sUserMemberships = new ArrayList<SUserMembership>();
        for (int i = 0; i < numberOfUserMemberships; i = i + numberPerPage) {
            sUserMemberships.addAll(identityService.getUserMemberships(i, numberPerPage));
        }
        return sUserMemberships;
    }

    private List<SGroup> getAllGroups(final int numberPerPage) throws SIdentityException {
        List<SGroup> groups;
        final long groupNumber = identityService.getNumberOfGroups();
        groups = new ArrayList<SGroup>(Long.valueOf(groupNumber).intValue());// FIXME can't put more than long element here
        for (int i = 0; i < groupNumber; i = i + numberPerPage) {
            groups.addAll(identityService.getGroups(i, numberPerPage));
        }
        return groups;
    }

    private List<Role> getAllRoles(final int numberPerPage) throws SIdentityException {
        final long roleNumber = identityService.getNumberOfRoles();
        final List<Role> roles = new ArrayList<Role>(Long.valueOf(roleNumber).intValue());// FIXME can't put more than long element here
        for (int i = 0; i < roleNumber; i = i + numberPerPage) {
            final List<SRole> sRoles = identityService.getRoles(i, numberPerPage);
            roles.addAll(ModelConvertor.toRoles(sRoles));
        }
        return roles;
    }

    private List<ExportedUser> getAllUsers(final int numberPerPage) throws SIdentityException {
        final long userNumber = identityService.getNumberOfUsers();
        final List<ExportedUser> users = new ArrayList<ExportedUser>(Long.valueOf(userNumber).intValue());// FIXME can't put more than long element here
        for (int i = 0; i <= userNumber; i = i + numberPerPage) {
            final List<SUser> sUsers = identityService.getUsers(i, numberPerPage);
            for (final SUser sUser : sUsers) {
                final SContactInfo persoInfo = identityService.getUserContactInfo(sUser.getId(), true);
                final SContactInfo proInfo = identityService.getUserContactInfo(sUser.getId(), false);
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
                userNames.put(sUser.getId(), sUser.getUserName());
                users.add(ModelConvertor.toExportedUser(sUser, persoInfo, proInfo, managerUserName));
            }
        }
        return users;
    }

}
