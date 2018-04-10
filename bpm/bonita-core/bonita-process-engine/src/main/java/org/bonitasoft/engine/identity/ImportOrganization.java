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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.impl.SCustomUserInfoValueAPI;
import org.bonitasoft.engine.api.impl.SessionInfos;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.model.builder.SUserMembershipBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SUserUpdateBuilderFactory;
import org.bonitasoft.engine.identity.xml.ExportedCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.xml.ExportedGroup;
import org.bonitasoft.engine.identity.xml.ExportedRole;
import org.bonitasoft.engine.identity.xml.ExportedUser;
import org.bonitasoft.engine.identity.xml.ExportedUserMembership;
import org.bonitasoft.engine.identity.xml.Organization;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 */
public class ImportOrganization implements TransactionContentWithResult<List<String>> {

    private static final String LEGACY_NS = "xmlns:organization=\"http://documentation.bonitasoft.com/organization-xml-schema\"";

    private static final String VERSIONED_NS = "xmlns:organization=\"http://documentation.bonitasoft.com/organization-xml-schema/1.1\"";

    final IdentityService identityService;

    private final TechnicalLoggerService logger;

    private final String organizationContent;

    private final List<String> warnings;

    private final ImportOrganizationStrategy strategy;

    private final TenantServiceAccessor serviceAccessor;

    private final SCustomUserInfoValueAPI userInfoValueAPI;

    public ImportOrganization(final TenantServiceAccessor serviceAccessor, final String organizationContent,
            final ImportPolicy policy, final SCustomUserInfoValueAPI userInfoValueAPI)
            throws OrganizationImportException {
        this.serviceAccessor = serviceAccessor;
        this.userInfoValueAPI = userInfoValueAPI;
        identityService = serviceAccessor.getIdentityService();
        this.organizationContent = updateNamespace(organizationContent);
        logger = serviceAccessor.getTechnicalLoggerService();
        warnings = new ArrayList<>();
        switch (policy) {
            case FAIL_ON_DUPLICATES:
                strategy = new ImportOrganizationFailOnDuplicatesStrategy();
                break;
            case IGNORE_DUPLICATES:
                strategy = new ImportOrganizationIgnoreDuplicatesStrategy();
                break;
            case MERGE_DUPLICATES:
                strategy = new ImportOrganizationMergeDuplicatesStrategy(identityService, userInfoValueAPI, logger);
                break;
            default:
                throw new OrganizationImportException("No import strategy found for " + policy);
        }
    }

    private void excludeGroupsWithInvalidCharactersInName(List<ExportedGroup> groups) throws OrganizationImportException {
        for (ExportedGroup group : groups) {
            String groupName = group.getName();
            if (groupName.contains("/")) {
                groups.remove(group);
                warnings.add("The group name " + groupName + " contains the character '/' which is not supported. The group has not been imported");
            }
        }
    }

    private String updateNamespace(String content) {
        if (content != null) {
            if (content.contains(LEGACY_NS)) {
                content = content.replace(LEGACY_NS, VERSIONED_NS);
            }
        }
        return content;
    }

    @Override
    public List<String> getResult() {
        return warnings;
    }

    @Override
    public void execute() throws SBonitaException {
        try {
            final Organization organization = new OrganizationParser().convert(organizationContent);
            final List<ExportedUser> users = organization.getUsers();
            final List<ExportedRole> roles = organization.getRoles();
            final List<ExportedGroup> groups = organization.getGroups();
            //checking if the Group names contain illegal characters
            excludeGroupsWithInvalidCharactersInName(groups);
            final List<ExportedUserMembership> memberships = organization.getMemberships();
            // custom user info definitions
            final Map<String, SCustomUserInfoDefinition> customUserInfoDefinitions = importCustomUserInfoDefinitions(organization
                    .getCustomUserInfoDefinition());
            // Users
            final Map<String, SUser> userNameToSUsers = importUsers(users, customUserInfoDefinitions);
            updateManagerId(users, userNameToSUsers);
            // Roles
            final Map<String, Long> roleNameToIdMap = importRoles(roles);
            // Groups
            final Map<String, Long> groupPathToIdMap = importGroups(groups);
            // UserMemberships
            importMemberships(memberships, userNameToSUsers, roleNameToIdMap, groupPathToIdMap);
        } catch (final SBonitaException e) {
            throw e;
        } catch (final Exception e) {
            throw new SImportOrganizationException(e);
        }
    }

    private Map<String, SCustomUserInfoDefinition> importCustomUserInfoDefinitions(
            final List<ExportedCustomUserInfoDefinition> customUserInfoDefinitionCreators)
            throws SIdentityException, ImportDuplicateInOrganizationException {
        final CustomUserInfoDefinitionImporter importer = new CustomUserInfoDefinitionImporter(serviceAccessor, strategy);
        return importer.importCustomUserInfoDefinitions(customUserInfoDefinitionCreators);
    }

    private void importMemberships(final List<ExportedUserMembership> memberships, final Map<String, SUser> userNameToSUsers,
                                   final Map<String, Long> roleNameToIdMap,
                                   final Map<String, Long> groupPathToIdMap) throws SIdentityException, ImportDuplicateInOrganizationException {
        for (final ExportedUserMembership newMembership : memberships) {
            final Long userId = getUserId(userNameToSUsers, newMembership);
            final Long groupId = getGroupId(groupPathToIdMap, newMembership);
            final Long roleId = getRoleId(roleNameToIdMap, newMembership);
            if (userId != null && groupId != null && roleId != null) {
                try {
                    final SUserMembership sUserMembership = identityService.getUserMembership(userId, groupId, roleId);
                    strategy.foundExistingMembership(sUserMembership);
                } catch (final SIdentityException e) {
                    final Long assignedBy = getAssignedBy(userNameToSUsers, newMembership);
                    addMembership(newMembership, userId, groupId, roleId, assignedBy);
                }
            } else {
                if (logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
                    logger.log(getClass(), TechnicalLogSeverity.WARNING, "The membership " + newMembership
                            + " could not be imported because the user, group or role can't be found\n userId=" +
                            userId + " groupId=" + groupId + " roleId=" + roleId);
                }
            }
        }
    }

    private Long getUserId(final Map<String, SUser> userNameToSUsers, final ExportedUserMembership newMembership) {
        final String username = newMembership.getUserName();
        if (username == null || username.isEmpty()) {
            return -1L;
        }
        final SUser sUser = userNameToSUsers.get(username);
        if (sUser != null) {
            return sUser.getId();
        }
        return null;

    }

    private Long getGroupId(final Map<String, Long> groupPathToIdMap, final ExportedUserMembership newMembership) {
        final String groupParentPath = newMembership.getGroupParentPath();
        final String groupFullPath = (groupParentPath == null ? '/' : groupParentPath + '/') + newMembership.getGroupName();
        return groupPathToIdMap.get(groupFullPath);
    }

    private Long getRoleId(final Map<String, Long> roleNameToIdMap, final ExportedUserMembership newMembership) {
        final String roleName = newMembership.getRoleName();
        if (roleName == null || roleName.isEmpty()) {
            return -1L;
        }
        final Long roleId = roleNameToIdMap.get(roleName);
        if (roleId != null) {
            return roleId;
        }
        return null;
    }

    private Long getAssignedBy(final Map<String, SUser> userNameToSUsers, final ExportedUserMembership newMembership) {
        final String assignedByName = newMembership.getAssignedBy();
        if (assignedByName == null || assignedByName.isEmpty()) {
            return -1L;
        }
        final SUser sUserAssigned = userNameToSUsers.get(assignedByName);
        if (sUserAssigned != null) {
            return sUserAssigned.getId();
        }
        return -1L;
    }

    private Map<String, Long> importGroups(final List<ExportedGroup> groupCreators) throws ImportDuplicateInOrganizationException, SIdentityException {
        final Map<String, Long> groupPathToIdMap = new HashMap<>(groupCreators.size());
        for (final ExportedGroup groupCreator : groupCreators) {
            SGroup sGroup;
            try {
                final String groupPath = getGroupPath(groupCreator);
                sGroup = identityService.getGroupByPath(groupPath);
                strategy.foundExistingGroup(sGroup, groupCreator);
            } catch (final SGroupNotFoundException e) {
                sGroup = addGroup(groupCreator);
            }
            groupPathToIdMap.put(sGroup.getPath(), sGroup.getId());
        }
        return groupPathToIdMap;
    }

    private String getGroupPath(final ExportedGroup exportedGroup) {
        final String name = exportedGroup.getName();
        final String parentPath = exportedGroup.getParentPath();
        if (parentPath == null) {
            return "/" + name;
        }
        return parentPath + "/" + name;
    }

    private Map<String, Long> importRoles(final List<ExportedRole> roleCreators) throws ImportDuplicateInOrganizationException, SIdentityException {
        final Map<String, Long> roleNameToIdMap = new HashMap<>(roleCreators.size());
        for (final ExportedRole roleCreator : roleCreators) {
            SRole sRole;
            try {
                sRole = identityService.getRoleByName(roleCreator.getName());
                strategy.foundExistingRole(sRole, roleCreator);
            } catch (final SRoleNotFoundException e) {
                sRole = addRole(roleCreator);
            }
            roleNameToIdMap.put(sRole.getName(), sRole.getId());
        }
        return roleNameToIdMap;
    }

    private void updateManagerId(final List<ExportedUser> users, final Map<String, SUser> userNameToSUsers) throws SUserUpdateException {
        for (final ExportedUser user : users) {
            final String managerUserName = user.getManagerUserName();
            if (managerUserName != null && managerUserName.trim().length() > 0) {
                SUser manager = userNameToSUsers.get(managerUserName.trim());
                if (manager != null) {
                    identityService.updateUser(userNameToSUsers.get(user.getUserName()),
                            BuilderFactory.get(SUserUpdateBuilderFactory.class).createNewInstance().updateManagerUserId(manager.getId()).done());
                } else {
                    logger.log(this.getClass(), TechnicalLogSeverity.WARNING, "The user " + user.getUserName() + " has a manager with username "
                            + managerUserName + ", but this one does not exist. Please set it manually.");
                }
            }
        }
    }

    private Map<String, SUser> importUsers(final List<ExportedUser> users, final Map<String, SCustomUserInfoDefinition> customUserInfoDefinitions)
            throws SBonitaException {
        final CustomUserInfoValueImporter userInfoValueImporter = new CustomUserInfoValueImporter(userInfoValueAPI, customUserInfoDefinitions);
        final UserImporter userImporter = new UserImporter(serviceAccessor, strategy, SessionInfos.getUserIdFromSession(), userInfoValueImporter);
        return userImporter.importUsers(users);
    }

    private void addMembership(final ExportedUserMembership newMembership, final Long userId, final Long groupId, final Long roleId, final Long assignedBy)
            throws SUserMembershipCreationException {
        final long assignedDateAsLong = getAssignedDate(newMembership);
        final SUserMembership sUserMembership = BuilderFactory.get(SUserMembershipBuilderFactory.class).createNewInstance(userId, groupId, roleId)
                .setAssignedBy(assignedBy).setAssignedDate(assignedDateAsLong).done();
        identityService.createUserMembership(sUserMembership);
    }

    private long getAssignedDate(final ExportedUserMembership newMembership) {
        final Long assignedDate = newMembership.getAssignedDate();
        if (assignedDate != null) {
            return assignedDate;
        }
        return 0;
    }

    private SGroup addGroup(final ExportedGroup exportedGroup) throws SGroupCreationException {
        final SGroup sGroup = ModelConvertor.constructSGroup(exportedGroup);
        identityService.createGroup(sGroup, null, null);
        return sGroup;
    }

    private SRole addRole(final ExportedRole exportedRole) throws SIdentityException {
        final SRole sRole = ModelConvertor.constructSRole(exportedRole);
        identityService.createRole(sRole, null, null);
        return sRole;
    }

}
