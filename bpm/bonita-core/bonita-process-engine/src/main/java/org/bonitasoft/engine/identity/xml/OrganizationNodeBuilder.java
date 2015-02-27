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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.identity.ExportedCustomUserInfoValue;
import org.bonitasoft.engine.identity.ExportedUser;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.xml.ElementBinding;
import org.bonitasoft.engine.xml.XMLNode;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class OrganizationNodeBuilder {

    static final List<Class<? extends ElementBinding>> BINDINGS = new ArrayList<Class<? extends ElementBinding>>();

    private static final String NAMESPACE = "http://documentation.bonitasoft.com/organization-xml-schema/1.1";

    private static final String NS_PREFIX = "organization";

    static {
        BINDINGS.add(OrganizationBinding.class);
        BINDINGS.add(CustomUserInfoDefinitionBinding.class);
        BINDINGS.add(UserBinding.class);
        BINDINGS.add(PersonalContactDataBinding.class);
        BINDINGS.add(ProfessionalContactDataBinding.class);
        BINDINGS.add(CustomUserInfoValueBinding.class);
        BINDINGS.add(RoleBinding.class);
        BINDINGS.add(GroupBinding.class);
        BINDINGS.add(MembershipBinding.class);
    }


    private OrganizationNodeBuilder() {
    }

    public static XMLNode getDocument(final Organization organization, final Map<Long, String> userNames, final Map<Long, String> groupIdParentPath) {
        final XMLNode document = getRootNode();
        addCustomUserInfoDefinitions(organization.getCustomUserInfoDefinitions(), document);
        addUsers(organization.getUsers(), document);
        addRoles(organization.getRole(), document);
        addGroups(organization.getGroup(), document);
        addMemberships(userNames, groupIdParentPath, organization.getMemberships(), document);
        return document;
    }

    private static void addCustomUserInfoDefinitions(final List<SCustomUserInfoDefinition> customUserInfoDefinitions, final XMLNode document) {
        final XMLNode userInfoDefsNode = new XMLNode(OrganizationMappingConstants.CUSTOM_USER_INFO_DEFINITIONS);
        document.addChild(userInfoDefsNode);
        for (final SCustomUserInfoDefinition userInfoDef : customUserInfoDefinitions) {
            final XMLNode userNode = getCustomUserInfoDefinitionNode(userInfoDef);
            userInfoDefsNode.addChild(userNode);
        }
    }

    private static XMLNode getCustomUserInfoDefinitionNode(final SCustomUserInfoDefinition userInfoDef) {
        final XMLNode userInfDefNode = new XMLNode(OrganizationMappingConstants.CUSTOM_USER_INFO_DEFINITION);
        addCustomUserInfoDefinitionName(userInfoDef, userInfDefNode);
        addCustomUserInfoDefinitionDescription(userInfoDef, userInfDefNode);
        return userInfDefNode;
    }

    private static void addCustomUserInfoDefinitionName(final SCustomUserInfoDefinition userInfoDef, final XMLNode userInfDefNode) {
        if (userInfoDef.getName() != null) {
            final XMLNode node = new XMLNode(OrganizationMappingConstants.NAME);
            node.setContent(userInfoDef.getName());
            userInfDefNode.addChild(node);
        }
    }

    private static void addCustomUserInfoDefinitionDescription(final SCustomUserInfoDefinition userInfoDef, final XMLNode userInfDefNode) {
        if (userInfoDef.getDescription() != null) {
            final XMLNode node = new XMLNode(OrganizationMappingConstants.DESCRIPTION);
            node.setContent(userInfoDef.getDescription());
            userInfDefNode.addChild(node);
        }
    }

    private static void addUsers(final List<ExportedUser> users, final XMLNode document) {
        final XMLNode usersNode = new XMLNode(OrganizationMappingConstants.USERS);
        document.addChild(usersNode);
        for (final ExportedUser user : users) {
            final XMLNode userNode = getUserNode(user);
            usersNode.addChild(userNode);
        }
    }

    private static void addRoles(final List<Role> roles, final XMLNode document) {
        final XMLNode rolesNodes = new XMLNode(OrganizationMappingConstants.ROLES);
        document.addChild(rolesNodes);
        for (final Role role : roles) {
            final XMLNode roleNode = getRoleNode(role);
            rolesNodes.addChild(roleNode);
        }
    }

    private static void addGroups(final List<Group> groups, final XMLNode document) {
        final XMLNode groupsNode = new XMLNode(OrganizationMappingConstants.GROUPS);
        document.addChild(groupsNode);
        for (final Group group : groups) {
            final XMLNode groupNode = getGroupNode(group);
            groupsNode.addChild(groupNode);
        }
    }

    private static void addMemberships(final Map<Long, String> userNames, final Map<Long, String> groupIdParentPath,
            final List<UserMembership> userMemberships, final XMLNode document) {
        final XMLNode membershipsNode = new XMLNode(OrganizationMappingConstants.MEMBERSHIPS);
        document.addChild(membershipsNode);
        for (final UserMembership userMembership : userMemberships) {
            final XMLNode membershipNode = getMembershipNode(userNames, groupIdParentPath, userMembership);
            membershipsNode.addChild(membershipNode);
        }
    }

    private static XMLNode getMembershipNode(final Map<Long, String> userNames, final Map<Long, String> groupIdParentPath, final UserMembership membership) {
        final XMLNode membershipNode = new XMLNode(OrganizationMappingConstants.MEMBERSHIP);
        XMLNode node = new XMLNode(OrganizationMappingConstants.USER_NAME);
        node.setContent(String.valueOf(membership.getUsername()));
        membershipNode.addChild(node);
        node = new XMLNode(OrganizationMappingConstants.ROLE_NAME);
        node.setContent(membership.getRoleName());
        membershipNode.addChild(node);
        node = new XMLNode(OrganizationMappingConstants.GROUP_NAME);
        node.setContent(membership.getGroupName());
        membershipNode.addChild(node);
        node = new XMLNode(OrganizationMappingConstants.GROUP_PARENT_PATH);
        node.setContent(groupIdParentPath.get(membership.getGroupId()));
        membershipNode.addChild(node);
        if (membership.getAssignedBy() > 0) {
            node = new XMLNode(OrganizationMappingConstants.ASSIGNED_BY);
            node.setContent(userNames.get(membership.getAssignedBy()));
            membershipNode.addChild(node);
        }
        if (membership.getAssignedDate() != null && membership.getAssignedDate().getTime() > 0) {
            node = new XMLNode(OrganizationMappingConstants.ASSIGNED_DATE);
            node.setContent(String.valueOf(membership.getAssignedDate().getTime()));
            membershipNode.addChild(node);
        }
        return membershipNode;
    }

    private static XMLNode getRoleNode(final Role role) {
        final XMLNode roleNode = new XMLNode(OrganizationMappingConstants.ROLE);
        if (role.getName() != null) {
            roleNode.addAttribute(OrganizationMappingConstants.NAME, role.getName());
        }
        if (role.getDisplayName() != null) {
            final XMLNode node = new XMLNode(OrganizationMappingConstants.DISPLAY_NAME);
            node.setContent(role.getDisplayName());
            roleNode.addChild(node);
        }
        if (role.getDescription() != null) {
            final XMLNode node = new XMLNode(OrganizationMappingConstants.DESCRIPTION);
            node.setContent(role.getDescription());
            roleNode.addChild(node);
        }
        if (role.getIconName() != null) {
            final XMLNode node = new XMLNode(OrganizationMappingConstants.ICON_NAME);
            node.setContent(role.getIconName());
            roleNode.addChild(node);
        }
        if (role.getIconPath() != null) {
            final XMLNode node = new XMLNode(OrganizationMappingConstants.ICON_PATH);
            node.setContent(role.getIconPath());
            roleNode.addChild(node);
        }
        return roleNode;
    }

    private static XMLNode getGroupNode(final Group group) {
        final XMLNode groupNode = new XMLNode(OrganizationMappingConstants.GROUP);
        if (group.getName() != null) {
            groupNode.addAttribute(OrganizationMappingConstants.NAME, group.getName());
        }
        if (group.getParentPath() != null) {
            groupNode.addAttribute(OrganizationMappingConstants.PARENT_PATH, group.getParentPath());
        }
        if (group.getDisplayName() != null) {
            final XMLNode node = new XMLNode(OrganizationMappingConstants.DISPLAY_NAME);
            node.setContent(group.getDisplayName());
            groupNode.addChild(node);
        }
        if (group.getDescription() != null) {
            final XMLNode node = new XMLNode(OrganizationMappingConstants.DESCRIPTION);
            node.setContent(group.getDescription());
            groupNode.addChild(node);
        }
        if (group.getIconName() != null) {
            final XMLNode node = new XMLNode(OrganizationMappingConstants.ICON_NAME);
            node.setContent(group.getIconName());
            groupNode.addChild(node);
        }
        if (group.getIconPath() != null) {
            final XMLNode node = new XMLNode(OrganizationMappingConstants.ICON_PATH);
            node.setContent(group.getIconPath());
            groupNode.addChild(node);
        }
        return groupNode;
    }

    private static XMLNode getUserNode(final ExportedUser user) {
        final XMLNode userNode = new XMLNode(OrganizationMappingConstants.USER);
        userNode.addAttribute(OrganizationMappingConstants.USER_NAME, user.getUserName());
        addPasswordNode(user, userNode);
        addFirstNameNode(user, userNode);
        addLastNameNode(user, userNode);
        addIconNameNode(user, userNode);
        addIconPathNode(user, userNode);
        addTitleNode(user, userNode);
        addJobTitleNode(user, userNode);
        addEnabledNode(user, userNode);
        addPersonalDataNode(user, userNode);
        addProfessionalDataNode(user, userNode);
        addCustomUserInfoValuesNode(user.getCustomUserInfoValues(), userNode);
        addManagerNode(user, userNode);
        return userNode;
    }

    private static void addCustomUserInfoValuesNode(final List<ExportedCustomUserInfoValue> userInfoValues, final XMLNode userNode) {
        userNode.addChild(CustomUserInfoValueNodeBuilder.buildNode(userInfoValues));
    }

    private static void addPasswordNode(final ExportedUser user, final XMLNode userNode) {
        final XMLNode nodePassword = new XMLNode(OrganizationMappingConstants.PASSWORD);
        nodePassword.addAttribute(OrganizationMappingConstants.PASSWORD_ENCRYPTED, "true");
        nodePassword.setContent(user.getPassword());
        userNode.addChild(nodePassword);
    }

    private static void addFirstNameNode(final ExportedUser user, final XMLNode userNode) {
        if (user.getFirstName() != null) {
            final XMLNode node = new XMLNode(OrganizationMappingConstants.FIRST_NAME);
            node.setContent(user.getFirstName());
            userNode.addChild(node);
        }
    }

    private static void addLastNameNode(final ExportedUser user, final XMLNode userNode) {
        if (user.getLastName() != null) {
            final XMLNode node = new XMLNode(OrganizationMappingConstants.LAST_NAME);
            node.setContent(user.getLastName());
            userNode.addChild(node);
        }
    }

    private static void addIconNameNode(final ExportedUser user, final XMLNode userNode) {
        if (user.getIconName() != null) {
            final XMLNode node = new XMLNode(OrganizationMappingConstants.ICON_NAME);
            node.setContent(user.getIconName());
            userNode.addChild(node);
        }
    }

    private static void addIconPathNode(final ExportedUser user, final XMLNode userNode) {
        if (user.getIconPath() != null) {
            final XMLNode node = new XMLNode(OrganizationMappingConstants.ICON_PATH);
            node.setContent(user.getIconPath());
            userNode.addChild(node);
        }
    }

    private static void addTitleNode(final ExportedUser user, final XMLNode userNode) {
        if (user.getTitle() != null) {
            final XMLNode node = new XMLNode(OrganizationMappingConstants.TITLE);
            node.setContent(user.getTitle());
            userNode.addChild(node);
        }
    }

    private static void addJobTitleNode(final ExportedUser user, final XMLNode userNode) {
        if (user.getJobTitle() != null) {
            final XMLNode node = new XMLNode(OrganizationMappingConstants.JOB_TITLE);
            node.setContent(user.getJobTitle());
            userNode.addChild(node);
        }
    }

    private static void addEnabledNode(final ExportedUser user, final XMLNode userNode) {
        final XMLNode enabledNode = new XMLNode(OrganizationMappingConstants.ENABLED);
        enabledNode.setContent(user.isEnabled() ? "true" : "false");
        userNode.addChild(enabledNode);
    }

    private static void addPersonalDataNode(final ExportedUser user, final XMLNode userNode) {
        final XMLNode personalDataNode = getPersonalDataNode(user);
        userNode.addChild(personalDataNode);
    }

    private static void addProfessionalDataNode(final ExportedUser user, final XMLNode userNode) {
        final XMLNode professionalDataNode = getProfessionalDataNode(user);
        userNode.addChild(professionalDataNode);
    }

    private static void addManagerNode(final ExportedUser user, final XMLNode userNode) {
        if (user.getManagerUserName() != null) {
            final XMLNode node = new XMLNode(OrganizationMappingConstants.MANAGER);
            node.setContent(user.getManagerUserName());
            userNode.addChild(node);
        }
    }

    private static XMLNode getPersonalDataNode(final ExportedUser user) {
        final XMLNode contactDataNode = new XMLNode(OrganizationMappingConstants.PERSONAL_DATA);
        if (user.getPersonalAddress() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.ADDRESS, user.getPersonalAddress());
        }
        if (user.getPersonalBuilding() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.BUILDING, user.getPersonalBuilding());
        }
        if (user.getPersonalCity() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.CITY, user.getPersonalCity());
        }
        if (user.getPersonalCountry() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.COUNTRY, user.getPersonalCountry());
        }
        if (user.getPersonalEmail() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.EMAIL, user.getPersonalEmail());
        }
        if (user.getPersonalFaxNumber() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.FAX_NUMBER, user.getPersonalFaxNumber());
        }
        if (user.getPersonalMobileNumber() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.MOBILE_NUMBER, user.getPersonalMobileNumber());
        }
        if (user.getPersonalPhoneNumber() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.PHONE_NUMBER, user.getPersonalPhoneNumber());
        }
        if (user.getPersonalRoom() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.ROOM, user.getPersonalRoom());
        }
        if (user.getPersonalState() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.STATE, user.getPersonalState());
        }
        if (user.getPersonalWebsite() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.WEBSITE, user.getPersonalWebsite());
        }
        if (user.getPersonalZipCode() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.ZIP_CODE, user.getPersonalZipCode());
        }
        return contactDataNode;
    }

    private static XMLNode getProfessionalDataNode(final ExportedUser user) {
        final XMLNode contactDataNode = new XMLNode(OrganizationMappingConstants.PROFESSIONAL_DATA);
        if (user.getProfessionalAddress() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.ADDRESS, user.getProfessionalAddress());
        }
        if (user.getProfessionalBuilding() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.BUILDING, user.getProfessionalBuilding());
        }
        if (user.getProfessionalCity() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.CITY, user.getProfessionalCity());
        }
        if (user.getProfessionalCountry() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.COUNTRY, user.getProfessionalCountry());
        }
        if (user.getProfessionalEmail() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.EMAIL, user.getProfessionalEmail());
        }
        if (user.getProfessionalFaxNumber() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.FAX_NUMBER, user.getProfessionalFaxNumber());
        }
        if (user.getProfessionalMobileNumber() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.MOBILE_NUMBER, user.getProfessionalMobileNumber());
        }
        if (user.getProfessionalPhoneNumber() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.PHONE_NUMBER, user.getProfessionalPhoneNumber());
        }
        if (user.getProfessionalRoom() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.ROOM, user.getProfessionalRoom());
        }
        if (user.getProfessionalState() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.STATE, user.getProfessionalState());
        }
        if (user.getProfessionalWebsite() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.WEBSITE, user.getProfessionalWebsite());
        }
        if (user.getProfessionalZipCode() != null) {
            contactDataNode.addChild(OrganizationMappingConstants.ZIP_CODE, user.getProfessionalZipCode());
        }
        return contactDataNode;
    }

    private static XMLNode getRootNode() {
        final XMLNode organizationNode = new XMLNode(NS_PREFIX + ":" + OrganizationMappingConstants.IDENTITY_ORGANIZATION);
        organizationNode.addAttribute("xmlns:" + NS_PREFIX, NAMESPACE);
        return organizationNode;
    }

}
