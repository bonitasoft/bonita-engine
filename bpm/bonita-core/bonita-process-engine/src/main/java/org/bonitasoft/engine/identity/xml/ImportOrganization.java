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
package org.bonitasoft.engine.identity.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.impl.SCustomUserInfoValueAPI;
import org.bonitasoft.engine.api.impl.SessionInfos;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator;
import org.bonitasoft.engine.identity.ExportedUser;
import org.bonitasoft.engine.identity.GroupCreator;
import org.bonitasoft.engine.identity.GroupCreator.GroupField;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.ImportPolicy;
import org.bonitasoft.engine.identity.OrganizationImportException;
import org.bonitasoft.engine.identity.RoleCreator;
import org.bonitasoft.engine.identity.RoleCreator.RoleField;
import org.bonitasoft.engine.identity.SGroupCreationException;
import org.bonitasoft.engine.identity.SGroupNotFoundException;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.SRoleNotFoundException;
import org.bonitasoft.engine.identity.SUserMembershipCreationException;
import org.bonitasoft.engine.identity.SUserUpdateException;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.model.builder.SUserMembershipBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SUserUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserUpdateBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.xml.Parser;

/**
 * @author Matthieu Chaffotte
 */
public class ImportOrganization implements TransactionContentWithResult<List<String>> {

    private static final String LEGACY_NS = "xmlns:organization=\"http://documentation.bonitasoft.com/organization-xml-schema\"";

    private static final String VERSIONED_NS = "xmlns:organization=\"http://documentation.bonitasoft.com/organization-xml-schema/1.1\"";

    final IdentityService identityService;

    private final Parser parser;

    private final TechnicalLoggerService logger;

    private final String organizationContent;

    private final List<String> warnings;

    private final ImportOrganizationStrategy strategy;

    private final TenantServiceAccessor serviceAccessor;

    private final SCustomUserInfoValueAPI userInfoValueAPI;

    public ImportOrganization(final TenantServiceAccessor serviceAccessor, final String organizationContent, final ImportPolicy policy,
            final SCustomUserInfoValueAPI userInfoValueAPI)
                    throws OrganizationImportException {
        this.serviceAccessor = serviceAccessor;
        this.userInfoValueAPI = userInfoValueAPI;
        identityService = serviceAccessor.getIdentityService();
        this.organizationContent = updateNamespace(organizationContent);
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
                strategy = new ImportOrganizationMergeDuplicatesStrategy(identityService, userInfoValueAPI);
                break;
            default:
                throw new OrganizationImportException("No import strategy found for " + policy);
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
        final InputStream resourceAsStream = this.getClass().getResourceAsStream("/bos-organization.xsd");
        try {
            parser.setSchema(resourceAsStream);
            parser.validate(new StringReader(organizationContent));
            final OrganizationCreator organization = (OrganizationCreator) parser.getObjectFromXML(new StringReader(organizationContent));
            final List<ExportedUser> users = organization.getUsers();
            final List<RoleCreator> roles = organization.getRoleCreators();
            final List<GroupCreator> groups = organization.getGroupCreators();
            final List<UserMembership> memberships = organization.getMemberships();

            // custom user info definitions
            final Map<String, SCustomUserInfoDefinition> customUserInfoDefinitions = importCustomUserInfoDefinitions(organization
                    .getCustomUserInfoDefinitionCreators());

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
        } finally {
            try {
                resourceAsStream.close();
            } catch (final IOException e) {
                throw new SBonitaRuntimeException(e);
            }
        }
    }

    private Map<String, SCustomUserInfoDefinition> importCustomUserInfoDefinitions(final List<CustomUserInfoDefinitionCreator> customUserInfoDefinitionCreators)
            throws SIdentityException, ImportDuplicateInOrganizationException {
        final CustomUserInfoDefinitionImporter importer = new CustomUserInfoDefinitionImporter(serviceAccessor, strategy);
        return importer.importCustomUserInfoDefinitions(customUserInfoDefinitionCreators);
    }

    private void importMemberships(final List<UserMembership> memberships, final Map<String, SUser> userNameToSUsers, final Map<String, Long> roleNameToIdMap,
            final Map<String, Long> groupPathToIdMap) throws SIdentityException, ImportDuplicateInOrganizationException {
        for (final UserMembership newMembership : memberships) {
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
                            + " coud not be imported because the user, group or role can't be found\n userId=" + userId + " groupId=" + groupId + " roleId="
                            + roleId);
                }
            }
        }
    }

    private Long getUserId(final Map<String, SUser> userNameToSUsers, final UserMembership newMembership) {
        final String username = newMembership.getUsername();
        if (username == null || username.isEmpty()) {
            return -1L;
        }
        final SUser sUser = userNameToSUsers.get(username);
        if (sUser != null) {
            return sUser.getId();
        }
        return null;

    }

    private Long getGroupId(final Map<String, Long> groupPathToIdMap, final UserMembership newMembership) {
        final String groupParentPath = newMembership.getGroupParentPath();
        final String groupFullPath = (groupParentPath == null ? '/' : groupParentPath + '/') + newMembership.getGroupName();
        return groupPathToIdMap.get(groupFullPath);
    }

    private Long getRoleId(final Map<String, Long> roleNameToIdMap, final UserMembership newMembership) {
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

    private Long getAssignedBy(final Map<String, SUser> userNameToSUsers, final UserMembership newMembership) {
        final String assignedByName = newMembership.getAssignedByName();
        if (assignedByName == null || assignedByName.isEmpty()) {
            return -1L;
        }
        final SUser sUserAssigned = userNameToSUsers.get(assignedByName);
        if (sUserAssigned != null) {
            return sUserAssigned.getId();
        }
        return -1L;
    }

    private Map<String, Long> importGroups(final List<GroupCreator> groupCreators) throws ImportDuplicateInOrganizationException, SIdentityException {
        final Map<String, Long> groupPathToIdMap = new HashMap<String, Long>(groupCreators.size());
        for (final GroupCreator groupCreator : groupCreators) {
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

    private String getGroupPath(final GroupCreator creator) {
        final Map<GroupField, Serializable> fields = creator.getFields();
        final String name = (String) fields.get(GroupField.NAME);
        final String parentPath = (String) fields.get(GroupField.PARENT_PATH);
        if (parentPath == null) {
            return "/" + name;
        }
        return parentPath + "/" + name;
    }

    private Map<String, Long> importRoles(final List<RoleCreator> roleCreators) throws ImportDuplicateInOrganizationException, SIdentityException {
        final Map<String, Long> roleNameToIdMap = new HashMap<String, Long>(roleCreators.size());
        for (final RoleCreator roleCreator : roleCreators) {
            SRole sRole;
            try {
                sRole = identityService.getRoleByName((String) roleCreator.getFields().get(RoleField.NAME));
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
            if (managerUserName != null && managerUserName.trim().length() > 0 && userNameToSUsers.get(managerUserName) != null) {
                final SUser sUser = userNameToSUsers.get(user.getUserName());
                final Long managerId = userNameToSUsers.get(managerUserName).getId();
                if (managerId != null) {
                    final SUserUpdateBuilder userUpdateBuilder = BuilderFactory.get(SUserUpdateBuilderFactory.class).createNewInstance();
                    identityService.updateUser(sUser, userUpdateBuilder.updateManagerUserId(managerId).done());
                } else {
                    if (logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
                        logger.log(this.getClass(), TechnicalLogSeverity.WARNING, "The user " + user.getUserName() + " has a manager with username "
                                + managerUserName + ", but this one does not exist. Please set it manually.");
                    }
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

    private void addMembership(final UserMembership newMembership, final Long userId, final Long groupId, final Long roleId, final Long assignedBy)
            throws SUserMembershipCreationException {
        final long assignedDateAsLong = getAssignedDate(newMembership);
        final SUserMembership sUserMembership = BuilderFactory.get(SUserMembershipBuilderFactory.class).createNewInstance(userId, groupId, roleId)
                .setAssignedBy(assignedBy).setAssignedDate(assignedDateAsLong).done();
        identityService.createUserMembership(sUserMembership);
    }

    private long getAssignedDate(final UserMembership newMembership) {
        final Date assignedDate = newMembership.getAssignedDate();
        if (assignedDate != null) {
            return assignedDate.getTime();
        }
        return 0;
    }

    private SGroup addGroup(final GroupCreator creator) throws SGroupCreationException {
        final SGroup sGroup = ModelConvertor.constructSGroup(creator);
        identityService.createGroup(sGroup);
        return sGroup;
    }

    private SRole addRole(final RoleCreator creator) throws SIdentityException {
        final SRole sRole = ModelConvertor.constructSRole(creator);
        identityService.createRole(sRole);
        return sRole;
    }

}
