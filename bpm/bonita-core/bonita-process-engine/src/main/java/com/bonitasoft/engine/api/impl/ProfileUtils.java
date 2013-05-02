package com.bonitasoft.engine.api.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bonitasoft.engine.api.impl.transaction.identity.GetRole;
import org.bonitasoft.engine.api.impl.transaction.identity.GetSGroup;
import org.bonitasoft.engine.api.impl.transaction.identity.GetSUser;
import org.bonitasoft.engine.api.impl.transaction.profile.GetProfilesWithIds;
import org.bonitasoft.engine.commons.StringUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.platform.InvalidSessionException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileBuilder;
import org.bonitasoft.engine.profile.model.Profile;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchEntityDescriptor;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.impl.SearchOptionsImpl;
import org.bonitasoft.engine.search.profile.SearchProfileMembersForProfile;
import org.bonitasoft.engine.search.profile.SearchProfiles;
import org.bonitasoft.engine.xml.XMLNode;

import com.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import com.bonitasoft.engine.service.TenantServiceAccessor;

public class ProfileUtils {

    public static HashMap<String, Serializable> profileToMap(final Profile profile) {
        final HashMap<String, Serializable> profielMap = new HashMap<String, Serializable>(4);
        profielMap.put(SProfileBuilder.ID, profile.getId());
        profielMap.put(SProfileBuilder.NAME, profile.getName());
        profielMap.put(SProfileBuilder.DESCRIPTION, profile.getDescription());
        profielMap.put(SProfileBuilder.ICON_PATH, profile.getIconPath());
        return profielMap;
    }

    public static HashMap<String, Serializable> sProfileToMap(final SProfile profile) {
        final HashMap<String, Serializable> profielMap = new HashMap<String, Serializable>(4);
        profielMap.put(SProfileBuilder.ID, profile.getId());
        profielMap.put(SProfileBuilder.NAME, profile.getName());
        profielMap.put(SProfileBuilder.DESCRIPTION, profile.getDescription());
        profielMap.put(SProfileBuilder.ICON_PATH, profile.getIconPath());
        return profielMap;
    }

    // Need to define concrete ArrayList here (instead of List) because it needs to be Serializable:
    public static List<HashMap<String, Serializable>> profilesToMap(final List<Profile> profiles) {
        final ArrayList<HashMap<String, Serializable>> profileMaps = new ArrayList<HashMap<String, Serializable>>(profiles.size());
        for (final Profile profile : profiles) {
            profileMaps.add(profileToMap(profile));
        }
        return profileMaps;
    }

    public static List<HashMap<String, Serializable>> sProfilesToMap(final List<SProfile> profiles) {
        final ArrayList<HashMap<String, Serializable>> profileMaps = new ArrayList<HashMap<String, Serializable>>(profiles.size());
        for (final SProfile profile : profiles) {
            profileMaps.add(sProfileToMap(profile));
        }
        return profileMaps;
    }

    public static XMLNode getProfileXmlNode(final TenantServiceAccessor tenantAccessor, final List<Long> profileIds) throws InvalidSessionException,
            SearchException {
        final String USER_SUFFIX = "ForUser";
        final String ROLE_SUFFIX = "ForRole";
        final String GROUP_SUFFIX = "ForGroup";
        final String ROLE_AND_GROUP_SUFFIX = "ForRoleAndGroup";

        final List<String> strArr = Arrays.asList(USER_SUFFIX, ROLE_SUFFIX, GROUP_SUFFIX, ROLE_AND_GROUP_SUFFIX);
        final String NS_PREFIX = "profilemappings";
        final String NAME_SPACE = "http://www.bonitasoft.org/ns/profilemapping/6.0";
        final XMLNode profileMappingNode = new XMLNode(NS_PREFIX + ":profileMappings");
        profileMappingNode.addAttribute("xmlns:" + NS_PREFIX, NAME_SPACE);

        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProfileService profileService = tenantAccessor.getProfileService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();

        // FIX ME : Refactor it. Use Pagination
        List<HashMap<String, Serializable>> profileList = null;
        if (profileIds == null) {
            final SearchOptionsBuilder optionsBuilder = new SearchOptionsBuilder(0, Integer.MAX_VALUE);
            optionsBuilder.sort("name", Order.ASC);
            final SearchProfiles searchProfiles = new SearchProfiles(profileService, searchEntitiesDescriptor.getProfileDescriptor(), optionsBuilder.done());

            try {
                transactionExecutor.execute(searchProfiles);
            } catch (final SBonitaException e) {
                throw new SearchException(e);
            }
            // get all profiles
            profileList = searchProfiles.getResult().getResult();
        } else {
            final GetProfilesWithIds getProfiles = new GetProfilesWithIds(profileService, profileIds);
            try {
                transactionExecutor.execute(getProfiles);
            } catch (final SBonitaException e) {
                throw new SearchException(e);
            }
            // get profiles with id sepcified
            profileList = getProfiles.getResult();
        }

        if (profileList != null && profileList.size() > 0) {
            final SearchOptions searchOptions = new SearchOptionsImpl(0, Integer.MAX_VALUE);
            for (final HashMap<String, Serializable> sProfile : profileList) {
                final XMLNode profileNode = new XMLNode("profileMapping");
                profileNode.addAttribute("name", (String) sProfile.get("name"));
                final long profileId = (Long) sProfile.get("id");

                for (final String type : strArr) {
                    SearchEntityDescriptor descriptor = searchEntitiesDescriptor.getProfileMemberUserDescriptor();
                    if (ROLE_SUFFIX.equals(type)) {
                        descriptor = searchEntitiesDescriptor.getProfileMemberUserDescriptor();
                    }
                    if (GROUP_SUFFIX.equals(type)) {
                        descriptor = searchEntitiesDescriptor.getProfileMemberUserDescriptor();
                    }
                    if (ROLE_AND_GROUP_SUFFIX.equals(type)) {
                        descriptor = searchEntitiesDescriptor.getProfileMemberUserDescriptor();
                    }
                    final SearchProfileMembersForProfile searcher = new SearchProfileMembersForProfile(profileId, type, profileService,
                            descriptor, searchOptions);
                    try {
                        transactionExecutor.execute(searcher);
                    } catch (final SBonitaException e) {
                        throw new SearchException(e);
                    }
                    final SearchResult<HashMap<String, Serializable>> profileMembersRes = searcher.getResult();
                    if (profileMembersRes.getCount() > 0) {
                        final List<HashMap<String, Serializable>> profMemberList = profileMembersRes.getResult();
                        if (!type.contains("And")) {
                            final XMLNode elementnode = getProfileElementNode(tenantAccessor, profMemberList,
                                    StringUtil.firstCharToLowerCase(type.substring(3)) + "s");
                            profileNode.addChild(elementnode);
                        } else {
                            final XMLNode elementnode = getProfileElementNode(tenantAccessor, profMemberList, "memberships");
                            profileNode.addChild(elementnode);
                        }
                    }
                }

                profileMappingNode.addChild(profileNode);
            }
        }
        return profileMappingNode;
    }

    private static XMLNode getProfileElementNode(final TenantServiceAccessor tenantAccessor, final List<HashMap<String, Serializable>> profMemberList,
            final String nodeType)
            throws InvalidSessionException, SearchException {
        final String user_node = "users";
        final String role_node = "roles";
        final String group_node = "groups";
        final String membership_node = "memberships";
        final XMLNode elementnode = new XMLNode(nodeType);
        for (final HashMap<String, Serializable> sProfileMember : profMemberList) {
            if (user_node.equals(nodeType)) {
                final XMLNode userNode = new XMLNode("username");
                userNode.setContent(getUsername(tenantAccessor, (Long) sProfileMember.get("userId")));
                elementnode.addChild(userNode);
            }
            if (role_node.equals(nodeType)) {
                final XMLNode roleNode = new XMLNode("rolename");
                roleNode.setContent(getRoleName(tenantAccessor, (Long) sProfileMember.get("roleId")));
                elementnode.addChild(roleNode);
            }
            if (group_node.equals(nodeType)) {
                final XMLNode groupNode = new XMLNode("groupname");
                groupNode.setContent(getGroupName(tenantAccessor, (Long) sProfileMember.get("groupId")));
                elementnode.addChild(groupNode);
            }
            if (membership_node.equals(nodeType)) {
                final XMLNode membernode = new XMLNode("membership");
                elementnode.addChild(membernode);

                final XMLNode roleNode = new XMLNode("rolename");
                roleNode.setContent(getRoleName(tenantAccessor, (Long) sProfileMember.get("roleId")));
                membernode.addChild(roleNode);
                final XMLNode groupNode = new XMLNode("groupname");
                groupNode.setContent(getGroupName(tenantAccessor, (Long) sProfileMember.get("groupId")));
                membernode.addChild(groupNode);
            }
        }
        return elementnode;
    }

    private static String getUsername(final TenantServiceAccessor tenantAccessor, final long userId) throws InvalidSessionException, SearchException {
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final GetSUser getUser = new GetSUser(identityService, userId);
        try {
            transactionExecutor.execute(getUser);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        final SUser user = getUser.getResult();
        return user.getUserName();
    }

    private static String getRoleName(final TenantServiceAccessor tenantAccessor, final long roleId) throws InvalidSessionException, SearchException {
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final GetRole getRole = new GetRole(roleId, identityService);
        try {
            transactionExecutor.execute(getRole);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        final SRole role = getRole.getResult();
        return role.getName();
    }

    private static String getGroupName(final TenantServiceAccessor tenantAccessor, final long groupId) throws InvalidSessionException, SearchException {
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final GetSGroup getGroup = new GetSGroup(groupId, identityService);
        try {
            transactionExecutor.execute(getGroup);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        final SGroup group = getGroup.getResult();
        return group.getPath();
    }

}
