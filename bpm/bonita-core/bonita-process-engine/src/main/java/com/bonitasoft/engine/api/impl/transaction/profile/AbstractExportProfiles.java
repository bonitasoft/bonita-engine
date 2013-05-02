/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package com.bonitasoft.engine.api.impl.transaction.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SGroupNotFoundException;
import org.bonitasoft.engine.identity.SRoleNotFoundException;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileBuilder;
import org.bonitasoft.engine.profile.builder.SProfileEntryBuilder;
import org.bonitasoft.engine.profile.builder.SProfileMemberBuilder;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.profile.model.SProfileMember;
import org.bonitasoft.engine.xml.XMLNode;
import org.bonitasoft.engine.xml.XMLWriter;

/**
 * @author Celine Souchet
 */
public abstract class AbstractExportProfiles implements TransactionContentWithResult<String> {

    private final static String USER_SUFFIX = "ForUser";

    private final static String ROLE_SUFFIX = "ForRole";

    private final static String GROUP_SUFFIX = "ForGroup";

    private final static String ROLE_AND_GROUP_SUFFIX = "ForRoleAndGroup";

    private final ProfileService profileService;

    private final IdentityService identityService;

    private final XMLWriter writer;

    private String xmlContent;

    public AbstractExportProfiles(final ProfileService profileService, final IdentityService identityService, final XMLWriter writer) {
        super();
        this.profileService = profileService;
        this.identityService = identityService;
        this.writer = writer;
    }

    @Override
    public void execute() throws SBonitaException {
        final XMLNode node = getProfilesXmlNode();
        final byte[] bytes = writer.write(node);
        xmlContent = new String(bytes);
    }

    @Override
    public String getResult() {
        return xmlContent;
    }

    protected abstract XMLNode getProfilesXmlNode() throws SBonitaException;

    protected XMLNode getProfileXmlNode(final SProfile sProfile) throws SBonitaException {
        final XMLNode profileNode = new XMLNode("profile");
        profileNode.addAttribute("name", sProfile.getName());
        profileNode.addChild("description", sProfile.getDescription());
        profileNode.addChild("iconPath", sProfile.getIconPath());

        final XMLNode profileEntriesXmlNode = getProfileEntriesXmlNode(sProfile.getId());
        if (profileEntriesXmlNode != null) {
            profileNode.addChild(profileEntriesXmlNode);
        }

        final XMLNode profileMappingXmlNode = getProfileMappingXmlNode(sProfile.getId());
        if (profileMappingXmlNode != null) {
            profileNode.addChild(profileMappingXmlNode);
        }
        return profileNode;
    }

    protected XMLNode getProfileEntriesXmlNode(final long profileId) throws SBonitaSearchException {
        // parentId 0:folder or no child link
        int index = 0;
        List<SProfileEntry> sParentProfileEntries = searchProfileEntries(index, profileId, 0);
        if (sParentProfileEntries != null && sParentProfileEntries.size() > 0) {
            final XMLNode profileEntriesNode = new XMLNode("profileEntries");
            while (sParentProfileEntries.size() > 0) {
                for (final SProfileEntry sProfileEntry : sParentProfileEntries) {
                    profileEntriesNode.addChild(getParentProfileEntryXmlNode(sProfileEntry, profileId));
                }
                index++;
                sParentProfileEntries = searchProfileEntries(index, profileId, 0);
            }
            return profileEntriesNode;
        }
        return null;
    }

    private XMLNode getParentProfileEntryXmlNode(final SProfileEntry parentEntry, final long profileId) throws SBonitaSearchException {
        final XMLNode parentProfileEntryNode = new XMLNode("parentProfileEntry");
        final String parentProfileEntryName = parentEntry.getName();
        parentProfileEntryNode.addAttribute("name", parentProfileEntryName);
        parentProfileEntryNode.addChild("parentName", "NULL");
        parentProfileEntryNode.addChild("index", parentEntry.getIndex() + "");
        parentProfileEntryNode.addChild("description", parentEntry.getDescription());
        parentProfileEntryNode.addChild("type", parentEntry.getType());
        parentProfileEntryNode.addChild("page", parentEntry.getPage());

        final XMLNode childrenProfileEntryXmlNode = getChildrenProfileEntryXmlNode(profileId, parentEntry.getId(), parentProfileEntryName);
        if (childrenProfileEntryXmlNode != null) {
            parentProfileEntryNode.addChild(childrenProfileEntryXmlNode);
        }
        return parentProfileEntryNode;
    }

    private XMLNode getChildrenProfileEntryXmlNode(final long profileId, final long parentId, final String parentProfileEntryName)
            throws SBonitaSearchException {
        int index = 0;
        List<SProfileEntry> sChildrenProfileEntries = searchProfileEntries(index, profileId, parentId);
        if (sChildrenProfileEntries != null && sChildrenProfileEntries.size() > 0) {
            final XMLNode childrenEntriesNode = new XMLNode("childrenEntries");
            while (sChildrenProfileEntries.size() > 0) {
                for (final SProfileEntry sChildProfileEntry : sChildrenProfileEntries) {
                    childrenEntriesNode.addChild(getChildProfileEntryXmlNode(sChildProfileEntry, parentProfileEntryName));
                }
                index++;
                sChildrenProfileEntries = searchProfileEntries(index, profileId, parentId);
            }
            return childrenEntriesNode;
        }
        return null;
    }

    private XMLNode getChildProfileEntryXmlNode(final SProfileEntry childProfileEntry, final String parentProfileEntryName) {
        final XMLNode childProfileEntryNode = new XMLNode("profileEntry");
        childProfileEntryNode.addAttribute("name", childProfileEntry.getName());
        childProfileEntryNode.addChild("parentName", parentProfileEntryName);
        childProfileEntryNode.addChild("index", childProfileEntry.getIndex() + "");
        childProfileEntryNode.addChild("description", childProfileEntry.getDescription());
        childProfileEntryNode.addChild("type", childProfileEntry.getType());
        childProfileEntryNode.addChild("page", childProfileEntry.getPage());
        return childProfileEntryNode;
    }

    protected XMLNode getProfileMappingXmlNode(final long profileId) throws SBonitaException {
        final XMLNode profileMappingNode = new XMLNode("profileMapping");

        final XMLNode usersXmlNode = getUsersXmlNode(profileId);
        if (usersXmlNode != null) {
            profileMappingNode.addChild(usersXmlNode);
        }

        final XMLNode rolesXmlNode = getRolesXmlNode(profileId);
        if (rolesXmlNode != null) {
            profileMappingNode.addChild(rolesXmlNode);
        }

        final XMLNode groupsXmlNode = getGroupsXmlNode(profileId);
        if (groupsXmlNode != null) {
            profileMappingNode.addChild(groupsXmlNode);
        }

        final XMLNode membershipsXmlNode = getMembershipsXmlNode(profileId);
        if (membershipsXmlNode != null) {
            profileMappingNode.addChild(membershipsXmlNode);
        }

        return profileMappingNode;
    }

    private XMLNode getUsersXmlNode(final long profileId) throws SBonitaException {
        int index = 0;
        List<SProfileMember> sProfileMembers = searchProfileMembers(index, profileId, USER_SUFFIX);
        if (sProfileMembers != null && sProfileMembers.size() > 0) {
            final XMLNode usersNode = new XMLNode("users");

            while (sProfileMembers.size() > 0) {
                for (final SProfileMember sProfileMember : sProfileMembers) {
                    usersNode.addChild("user", getUsername(sProfileMember.getUserId()));
                }
                index++;
                sProfileMembers = searchProfileMembers(index, profileId, USER_SUFFIX);
            }
            return usersNode;
        }
        return null;
    }

    private XMLNode getRolesXmlNode(final long profileId) throws SBonitaException {
        int index = 0;
        List<SProfileMember> sProfileMembers = searchProfileMembers(index, profileId, ROLE_SUFFIX);
        if (sProfileMembers != null && sProfileMembers.size() > 0) {
            final XMLNode rolesNode = new XMLNode("roles");

            while (sProfileMembers.size() > 0) {
                for (final SProfileMember sProfileMember : sProfileMembers) {
                    rolesNode.addChild("role", getRoleName(sProfileMember.getRoleId()));
                }
                index++;
                sProfileMembers = searchProfileMembers(index, profileId, ROLE_SUFFIX);
            }
            return rolesNode;
        }
        return null;
    }

    private XMLNode getGroupsXmlNode(final long profileId) throws SBonitaException {
        int index = 0;
        List<SProfileMember> sProfileMembers = searchProfileMembers(index, profileId, GROUP_SUFFIX);
        if (sProfileMembers != null && sProfileMembers.size() > 0) {
            final XMLNode groupsNode = new XMLNode("groups");

            while (sProfileMembers.size() > 0) {
                for (final SProfileMember sProfileMember : sProfileMembers) {
                    groupsNode.addChild("group", getGroupName(sProfileMember.getGroupId()));
                }
                index++;
                sProfileMembers = searchProfileMembers(index, profileId, GROUP_SUFFIX);
            }
            return groupsNode;
        }
        return null;
    }

    private XMLNode getMembershipsXmlNode(final long profileId) throws SBonitaException {
        int index = 0;
        List<SProfileMember> sProfileMembers = searchProfileMembers(index, profileId, ROLE_AND_GROUP_SUFFIX);
        if (sProfileMembers != null && sProfileMembers.size() > 0) {
            final XMLNode memberShipsNode = new XMLNode("memberships");

            while (sProfileMembers.size() > 0) {
                for (final SProfileMember sProfileMember : sProfileMembers) {
                    final XMLNode memberShipNode = new XMLNode("membership");
                    memberShipNode.addChild("role", getRoleName(sProfileMember.getRoleId()));
                    memberShipNode.addChild("group", getGroupName(sProfileMember.getGroupId()));
                    memberShipsNode.addChild(memberShipNode);
                }
                index++;
                sProfileMembers = searchProfileMembers(index, profileId, ROLE_AND_GROUP_SUFFIX);
            }
            return memberShipsNode;
        }
        return null;
    }

    protected List<SProfile> searchProfiles(final int index) throws SBonitaSearchException {
        final QueryOptions queryOptions = new QueryOptions(index, 100, Collections.singletonList(new OrderByOption(SProfile.class,
                SProfileBuilder.NAME, OrderByType.ASC)), Collections.<FilterOption> emptyList(), null);
        return profileService.searchProfiles(queryOptions);
    }

    protected List<SProfileEntry> searchProfileEntries(final int fromIndex, final long profileId, final long parentId) throws SBonitaSearchException {
        final List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SProfileEntry.class,
                SProfileEntryBuilder.INDEX, OrderByType.ASC));
        final List<FilterOption> filters = new ArrayList<FilterOption>();
        filters.add(new FilterOption(SProfileEntry.class, SProfileEntryBuilder.PROFILE_ID, profileId));
        filters.add(new FilterOption(SProfileEntry.class, SProfileEntryBuilder.PARENT_ID, parentId));
        final QueryOptions queryOptions = new QueryOptions(fromIndex, 100, orderByOptions, filters, null);
        return profileService.searchProfileEntries(queryOptions);
    }

    private List<SProfileMember> searchProfileMembers(final int index, final long profileId, final String querySuffix) throws SBonitaSearchException {
        final QueryOptions queryOptions = new QueryOptions(index, 100, Collections.singletonList(new OrderByOption(SProfileMember.class,
                SProfileMemberBuilder.ID, OrderByType.ASC)), Collections.<FilterOption> emptyList(), null);
        return profileService.searchProfileMembers(profileId, querySuffix, queryOptions);
    }

    private String getUsername(final long userId) throws SUserNotFoundException {
        final SUser user = identityService.getUser(userId);
        return user.getUserName();
    }

    private String getRoleName(final long roleId) throws SRoleNotFoundException {
        final SRole role = identityService.getRole(roleId);
        return role.getName();
    }

    private String getGroupName(final long groupId) throws SGroupNotFoundException {
        final SGroup group = identityService.getGroup(groupId);
        return group.getName();
    }

    protected ProfileService getProfileService() {
        return profileService;
    }
}
