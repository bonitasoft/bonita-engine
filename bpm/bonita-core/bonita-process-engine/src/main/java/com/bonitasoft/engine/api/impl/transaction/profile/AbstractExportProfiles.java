/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileBuilderFactory;
import org.bonitasoft.engine.profile.builder.SProfileEntryBuilderFactory;
import org.bonitasoft.engine.profile.builder.SProfileMemberBuilderFactory;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.profile.model.SProfileMember;
import org.bonitasoft.engine.xml.XMLNode;
import org.bonitasoft.engine.xml.XMLWriter;

/**
 * @author Celine Souchet
 */
public abstract class AbstractExportProfiles implements TransactionContentWithResult<String>, IProfileNameSpace
{

    private static final String GROUP_PATH_SEPARATOR = "/";

    private static final int NUMBER_OF_RESULTS = 100;

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
        profileNode.addAttribute("isDefault", String.valueOf(sProfile.isDefault()));
        profileNode.addChild("description", sProfile.getDescription());

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

    protected XMLNode getProfileEntriesXmlNode(final long profileId) throws SBonitaReadException {
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

    private XMLNode getParentProfileEntryXmlNode(final SProfileEntry parentEntry, final long profileId) throws SBonitaReadException {
        final XMLNode parentProfileEntryNode = new XMLNode("parentProfileEntry");
        final String parentProfileEntryName = parentEntry.getName();
        parentProfileEntryNode.addAttribute("name", parentProfileEntryName);
        parentProfileEntryNode.addAttribute("isCustom", parentEntry.isCustom());
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
            throws SBonitaReadException {
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
        childProfileEntryNode.addAttribute("isCustom", childProfileEntry.isCustom());
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

        final XMLNode groupsXmlNode = getGroupsXmlNode(profileId);
        if (groupsXmlNode != null) {
            profileMappingNode.addChild(groupsXmlNode);
        }

        final XMLNode membershipsXmlNode = getMembershipsXmlNode(profileId);
        if (membershipsXmlNode != null) {
            profileMappingNode.addChild(membershipsXmlNode);
        }

        final XMLNode rolesXmlNode = getRolesXmlNode(profileId);
        if (rolesXmlNode != null) {
            profileMappingNode.addChild(rolesXmlNode);
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
                    groupsNode.addChild("group", getGroupUniquePath(sProfileMember.getGroupId()));
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
                    memberShipNode.addChild("group", getGroupUniquePath(sProfileMember.getGroupId()));
                    memberShipNode.addChild("role", getRoleName(sProfileMember.getRoleId()));
                    memberShipsNode.addChild(memberShipNode);
                }
                index++;
                sProfileMembers = searchProfileMembers(index, profileId, ROLE_AND_GROUP_SUFFIX);
            }
            return memberShipsNode;
        }
        return null;
    }

    protected List<SProfile> searchProfiles(final int fromIndex) throws SBonitaReadException {
        final QueryOptions queryOptions = new QueryOptions(fromIndex * NUMBER_OF_RESULTS, NUMBER_OF_RESULTS, Collections.singletonList(new OrderByOption(
                SProfile.class, SProfileBuilderFactory.NAME, OrderByType.ASC)), Collections.<FilterOption> emptyList(), null);
        return profileService.searchProfiles(queryOptions);
    }

    protected List<SProfileEntry> searchProfileEntries(final int fromIndex, final long profileId, final long parentId) throws SBonitaReadException {
        final List<OrderByOption> orderByOptions = Collections
                .singletonList(new OrderByOption(SProfileEntry.class, SProfileEntryBuilderFactory.INDEX, OrderByType.ASC));
        final List<FilterOption> filters = new ArrayList<FilterOption>();
        filters.add(new FilterOption(SProfileEntry.class, SProfileEntryBuilderFactory.PROFILE_ID, profileId));
        filters.add(new FilterOption(SProfileEntry.class, SProfileEntryBuilderFactory.PARENT_ID, parentId));
        final QueryOptions queryOptions = new QueryOptions(fromIndex * NUMBER_OF_RESULTS, NUMBER_OF_RESULTS, orderByOptions, filters, null);
        return profileService.searchProfileEntries(queryOptions);
    }

    private List<SProfileMember> searchProfileMembers(final int fromIndex, final long profileId, final String querySuffix) throws SBonitaReadException {
        final QueryOptions queryOptions = new QueryOptions(fromIndex * NUMBER_OF_RESULTS, NUMBER_OF_RESULTS, Collections.singletonList(new OrderByOption(
                SProfileMember.class, SProfileMemberBuilderFactory.ID, OrderByType.ASC)), Collections.singletonList(new FilterOption(SProfileMember.class,
                SProfileEntryBuilderFactory.PROFILE_ID, profileId)), null);
        return profileService.searchProfileMembers(querySuffix, queryOptions);
    }

    private String getUsername(final long userId) throws SUserNotFoundException {
        final SUser user = identityService.getUser(userId);
        return user.getUserName();
    }

    private String getRoleName(final long roleId) throws SRoleNotFoundException {
        final SRole role = identityService.getRole(roleId);
        return role.getName();
    }

    /**
     * 
     * @param groupId
     * @return unique full path of the group
     *         ex: /acme/finance where /acme is parent group our finance
     * @throws SGroupNotFoundException
     */
    private String getGroupUniquePath(final long groupId) throws SGroupNotFoundException {
        final SGroup group = identityService.getGroup(groupId);
        final StringBuilder builder = new StringBuilder();
        if (group.getParentPath() == null) {
            builder.append(GROUP_PATH_SEPARATOR);
            builder.append(group.getName());

        } else {
            builder.append(group.getParentPath());
            builder.append(GROUP_PATH_SEPARATOR);
            builder.append(group.getName());
        }
        final String uniquePath = builder.toString();
        return uniquePath;
    }

    protected ProfileService getProfileService() {
        return profileService;
    }
}
