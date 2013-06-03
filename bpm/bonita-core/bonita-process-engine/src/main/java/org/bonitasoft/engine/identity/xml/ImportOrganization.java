/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.identity.ExportedUser;
import org.bonitasoft.engine.identity.GroupCreator;
import org.bonitasoft.engine.identity.GroupCreator.GroupField;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.ImportPolicy;
import org.bonitasoft.engine.identity.RoleCreator;
import org.bonitasoft.engine.identity.RoleCreator.RoleField;
import org.bonitasoft.engine.identity.OrganizationImportException;
import org.bonitasoft.engine.identity.SGroupCreationException;
import org.bonitasoft.engine.identity.SGroupNotFoundException;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.SRoleNotFoundException;
import org.bonitasoft.engine.identity.SUserCreationException;
import org.bonitasoft.engine.identity.SUserMembershipCreationException;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.SUserUpdateException;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.identity.model.SContactInfo;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.identity.model.builder.UserUpdateBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.xml.Parser;

/**
 * @author Matthieu Chaffotte
 */
public class ImportOrganization implements TransactionContentWithResult<List<String>> {

    final IdentityService identityService;

    final IdentityModelBuilder identityModelBuilder;

    private final Parser parser;

    private final TechnicalLoggerService logger;

    private final String organizationContent;

    private final List<String> warnings;

    private final ImportOrganizationStrategy strategy;

    public ImportOrganization(final TenantServiceAccessor serviceAccessor, final String organizationContent, final ImportPolicy policy)
            throws OrganizationImportException {
        identityService = serviceAccessor.getIdentityService();
        this.organizationContent = organizationContent;
        identityModelBuilder = serviceAccessor.getIdentityModelBuilder();
        parser = serviceAccessor.getParserFactgory().createParser(OrganizationNodeBuilder.BINDINGS);
        logger = serviceAccessor.getTechnicalLoggerService();
        warnings = new ArrayList<String>();
        switch (policy) {
            case FAIL_ON_DUPLICATES:
                strategy = new ImportOrganizationFailOnDuplicatesStrategy();
                break;
            case IGNORE_DUPLICATES:
                strategy = new ImportOrganizationIgnoreDuplicatesStrategy();
                break;
            case MERGE_DUPLICATES:
                strategy = new ImportOrganizationMergeDuplicatesStrategy(identityService, identityModelBuilder);
                break;
            default:
                throw new OrganizationImportException("No import strategy found for " + policy);
        }
    }

    @Override
    public List<String> getResult() {
        return warnings;
    }

    @Override
    public void execute() throws ImportDuplicateInOrganizationException {
        importOrganizationWithDuplicates();
    }

    private void importOrganizationWithDuplicates() throws ImportDuplicateInOrganizationException {
        try {
            parser.setSchema(this.getClass().getResourceAsStream("/bos-organization.xsd"));
            parser.validate(new StringReader(organizationContent));
            final Organization organization = (Organization) parser.getObjectFromXML(new StringReader(organizationContent));
            final List<ExportedUser> users = organization.getUsers();
            final List<RoleCreator> roles = organization.getRoles();
            final List<GroupCreator> groups = organization.getGroups();
            final List<UserMembership> memberships = organization.getMemberships();

            final Map<String, Long> userNameToIdMap = new HashMap<String, Long>(users.size());
            final Map<String, SUser> userNameToSUsers = new HashMap<String, SUser>((int) Math.min(Integer.MAX_VALUE, identityService.getNumberOfUsers()));
            final Map<String, Long> roleNameToIdMap = new HashMap<String, Long>(roles.size());
            final Map<String, Long> groupPathToIdMap = new HashMap<String, Long>(groups.size());

            // Users
            importUsers(users, userNameToIdMap, userNameToSUsers);
            updateManagerId(users, userNameToIdMap, userNameToSUsers);

            // Roles
            importRoles(roles, roleNameToIdMap);

            // Groups
            importGroups(groups, groupPathToIdMap);

            // UserMemberships
            importMemberships(memberships, userNameToIdMap, roleNameToIdMap, groupPathToIdMap);
        } catch (final Exception e) {
            throw new ImportDuplicateInOrganizationException(e);
        }
    }

    private void importMemberships(final List<UserMembership> memberships, final Map<String, Long> userNameToIdMap, final Map<String, Long> roleNameToIdMap,
            final Map<String, Long> groupPathToIdMap) throws SIdentityException, ImportDuplicateInOrganizationException {
        for (final UserMembership newMembership : memberships) {
            SUserMembership existingMembership = null;
            final List<SUserMembership> oldUserMemberships = identityService.getUserMemberships(0, (int) identityService.getNumberOfUserMemberships());
            for (final SUserMembership oldMembership : oldUserMemberships) {
                if (oldMembership.getUsername().equals(newMembership.getUsername()) && oldMembership.getRoleName().equals(newMembership.getRoleName())
                        && oldMembership.getGroupName().equals(newMembership.getGroupName())) {
                    existingMembership = oldMembership;
                    break;
                }

            }
            if (existingMembership != null) {
                strategy.foundExistingMembership(existingMembership);
            } else {
                addMembership(newMembership, userNameToIdMap, roleNameToIdMap, groupPathToIdMap);
            }
        }
    }

    private void importGroups(final List<GroupCreator> groups, final Map<String, Long> groupPathToIdMap) throws ImportDuplicateInOrganizationException,
            SIdentityException {
        for (final GroupCreator group : groups) {
            SGroup existingGroup;
            try {
                final String groupPath = getGroupPath(group);
                existingGroup = identityService.getGroupByPath(groupPath);
                strategy.foundExistingGroup(existingGroup, group);
            } catch (final SGroupNotFoundException e) {
                addGroup(group, groupPathToIdMap);
            }
        }
    }

    private String getGroupPath(final GroupCreator creator) {
        final Map<GroupField, Serializable> fields = creator.getFields();
        final String name = (String) fields.get(GroupField.NAME);
        final String parentPath = (String) fields.get(GroupField.PARENT_PATH);
        if (parentPath == null) {
            return "/" + name;
        } else {
            return parentPath + "/" + name;
        }
    }

    private void importRoles(final List<RoleCreator> roles, final Map<String, Long> roleNameToIdMap) throws ImportDuplicateInOrganizationException,
            SIdentityException {
        for (final RoleCreator role : roles) {
            SRole existingRole;
            try {
                existingRole = identityService.getRoleByName((String) role.getFields().get(RoleField.NAME));
            } catch (final SRoleNotFoundException e) {
                existingRole = null;
            }
            if (existingRole != null) {
                strategy.foundExistingRole(existingRole, role);
            } else {
                addRole(role, roleNameToIdMap);
            }
        }
    }

    private void updateManagerId(final List<ExportedUser> users, final Map<String, Long> userNameToIdMap, final Map<String, SUser> userNameToSUsers)
            throws SUserUpdateException {
        for (final ExportedUser user : users) {
            final String managerUserName = user.getManagerUserName();
            if (managerUserName != null && managerUserName.trim().length() > 0 && userNameToIdMap.get(managerUserName) != null) {
                final SUser sUser = userNameToSUsers.get(user.getUserName());
                final Long managerId = userNameToIdMap.get(managerUserName);
                if (managerId != null) {
                    final UserUpdateBuilder userUpdateBuilder = identityModelBuilder.getUserUpdateBuilder();
                    identityService.updateUser(sUser, userUpdateBuilder.updateManagerUserId(managerId).done());
                } else {
                    if (logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
                        logger.log(this.getClass(), TechnicalLogSeverity.WARNING, "The user " + user.getUserName() + " has a manager with username "
                                + managerUserName + " but this one does not exist, Please set it manually");
                    }
                }
            }
        }
    }

    private void importUsers(final List<ExportedUser> users, final Map<String, Long> userNameToIdMap, final Map<String, SUser> userNameToSUsers)
            throws ImportDuplicateInOrganizationException, SIdentityException {
        for (final ExportedUser user : users) {
            SUser existingUser;
            try {
                existingUser = identityService.getUserByUserName(user.getUserName());
            } catch (final SUserNotFoundException e) {
                existingUser = null;
            }
            SUser newUser;
            if (existingUser != null) {
                newUser = existingUser;
                strategy.foundExistingUser(existingUser, user);
            } else {
                newUser = addUser(user);
            }
            userNameToIdMap.put(user.getUserName(), newUser.getId());
            userNameToSUsers.put(user.getUserName(), newUser);
        }
    }

    private void addMembership(final UserMembership newMembership, final Map<String, Long> userNameToIdMap, final Map<String, Long> roleNameToIdMap,
            final Map<String, Long> groupPathToIdMap) throws SUserMembershipCreationException {
        final String username = newMembership.getUsername();
        final String roleName = newMembership.getRoleName();
        final String groupParentPath = newMembership.getGroupParentPath();
        final String groupFullPath = (groupParentPath == null ? '/' : groupParentPath + '/') + newMembership.getGroupName();

        final String assignedByName = newMembership.getAssignedByName();
        final long assignedBy;
        if (assignedByName == null || assignedByName.isEmpty()) {
            assignedBy = -1;
        } else {
            assignedBy = userNameToIdMap.get(assignedByName);
        }
        final Date assignedDate = newMembership.getAssignedDate();
        final long assignedDateAsLong;
        if (assignedDate != null) {
            assignedDateAsLong = assignedDate.getTime();
        } else {
            assignedDateAsLong = 0;
        }
        final Long userId = userNameToIdMap.get(username);
        final Long groupId = groupPathToIdMap.get(groupFullPath);
        final Long roldeId = roleNameToIdMap.get(roleName);
        if (userId != null && groupId != null && roldeId != null) {
            final SUserMembership sUserMembership = identityModelBuilder.getUserMembershipBuilder().createNewInstance(userId, groupId, roldeId)
                    .setAssignedBy(assignedBy).setAssignedDate(assignedDateAsLong).done();
            identityService.createUserMembership(sUserMembership);
        } else {
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
                logger.log(getClass(), TechnicalLogSeverity.WARNING, "The membership " + newMembership
                        + " because the user group or role can't be found\n userId=" + userId + " groupId=" + groupId + " roleId=" + roldeId);
            }
        }
    }

    private void addGroup(final GroupCreator creator, final Map<String, Long> groupPathToIdMap) throws SGroupCreationException {
        final SGroup sGroup = ModelConvertor.constructSGroup(creator, identityModelBuilder);
        identityService.createGroup(sGroup);
        groupPathToIdMap.put(sGroup.getPath(), sGroup.getId());
    }

    private void addRole(final RoleCreator creator, final Map<String, Long> roleNameToIdMap) throws SIdentityException {
        final SRole sRole = ModelConvertor.constructSRole(creator, identityModelBuilder);
        identityService.createRole(sRole);
        roleNameToIdMap.put(sRole.getName(), sRole.getId());
    }

    private SUser addUser(final ExportedUser user) throws SUserCreationException, SUserUpdateException {
        SUser sUser;
        if (user.isPasswordEncrypted()) {
            sUser = identityService.createUserWithoutEncryptingPassword(ModelConvertor.constructSUser(user, identityModelBuilder));
        } else {
            sUser = identityService.createUser(ModelConvertor.constructSUser(user, identityModelBuilder));
        }
        final SContactInfo persoSContactInfo = ModelConvertor.constructSUserContactInfo(user, true, identityModelBuilder, sUser.getId());
        identityService.createUserContactInfo(persoSContactInfo);
        final SContactInfo professSContactInfo = ModelConvertor.constructSUserContactInfo(user, false, identityModelBuilder, sUser.getId());
        identityService.createUserContactInfo(professSContactInfo);
        return sUser;
    }

}
