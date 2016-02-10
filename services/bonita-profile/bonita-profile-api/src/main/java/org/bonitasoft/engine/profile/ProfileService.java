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
package org.bonitasoft.engine.profile;

import java.util.List;

import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.exception.profile.SProfileCreationException;
import org.bonitasoft.engine.profile.exception.profile.SProfileDeletionException;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.exception.profile.SProfileUpdateException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryCreationException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryDeletionException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryNotFoundException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryUpdateException;
import org.bonitasoft.engine.profile.exception.profilemember.SProfileMemberCreationException;
import org.bonitasoft.engine.profile.exception.profilemember.SProfileMemberDeletionException;
import org.bonitasoft.engine.profile.exception.profilemember.SProfileMemberNotFoundException;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.profile.model.SProfileMember;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public interface ProfileService {

    String PROFILE = "PROFILE";

    String ENTRY_PROFILE = "ENTRY_PROFILE";

    String PROFILE_MEMBER = "PROFILE_MEMBER";

    /**
     * Get profile by its id
     *
     * @param profileId
     * @return sProfile
     * @throws SProfileNotFoundException
     *         occurs when the identifier does not refer to an existing sProfile
     * @since 6.0
     */
    SProfile getProfile(long profileId) throws SProfileNotFoundException;

    /**
     * Get all profiles by given ids
     *
     * @param profileIds
     *        a list contains many profile id
     * @return
     * @throws SProfileNotFoundException
     *         occurs when the identifier does not refer to an existing sProfile
     * @since 6.0
     */
    List<SProfile> getProfiles(List<Long> profileIds) throws SProfileNotFoundException;

    /**
     * Add a new profile
     *
     * @param profile
     * @return sProfile
     * @throws SProfileCreationException
     *         occurs when an exception is thrown during sProfile creation
     * @since 6.0
     */
    SProfile createProfile(SProfile profile) throws SProfileCreationException;

    /**
     * Update profile by given profile and new content
     *
     * @param profile
     * @param descriptor
     * @return The updated profile
     * @throws SProfileUpdateException
     *         occurs when an exception is thrown during sProfile update
     * @since 6.0
     */
    SProfile updateProfile(SProfile profile, EntityUpdateDescriptor descriptor) throws SProfileUpdateException;

    /**
     * Delete profile by given sProfile
     *
     * @param profile
     * @throws SProfileNotFoundException
     *         occurs when the identifier does not refer to an existing sProfile
     * @throws SProfileDeletionException
     *         occurs when an exception is thrown during sProfile deletion
     * @since 6.0
     */
    void deleteProfile(SProfile profile) throws SProfileNotFoundException, SProfileDeletionException, SProfileEntryDeletionException,
            SProfileMemberDeletionException;

    /**
     * Delete profile by its id
     *
     * @param profileId
     * @throws SProfileNotFoundException
     *         occurs when the identifier does not refer to an existing sProfile
     * @throws SProfileDeletionException
     *         occurs when an exception is thrown during sProfile deletion
     * @since 6.0
     */
    void deleteProfile(long profileId) throws SProfileNotFoundException, SProfileDeletionException, SProfileEntryDeletionException,
            SProfileMemberDeletionException;

    /**
     * Get all profiles of the user by userId
     *
     * @param userId
     * @return A list of sProfile
     * @throws SBonitaReadException
     * @since 6.0
     */
    List<SProfile> searchProfilesOfUser(long userId, int fromIndex, int numberOfElements, String field, OrderByType order) throws SBonitaReadException;

    /**
     * Get profile entry by its id
     *
     * @param profileEntryId
     * @return sProfileEntry
     * @throws SProfileEntryNotFoundException
     *         occurs when the identifier does not refer to an existing sProfileEntry
     * @since 6.0
     */
    SProfileEntry getProfileEntry(long profileEntryId) throws SProfileEntryNotFoundException;

    /**
     * Get all profile entries having the given value for the given int index by profileId
     *
     * @param profileId
     * @param fromIndex
     *        first result to be considered(>=0)
     * @param numberOfProfileEntry
     *        the max number of profileEntry to be returned (>=0)
     * @param field
     * @param order
     * @return all profile entries having the given value for the given int index by profileId
     * @throws SBonitaReadException
     * @since 6.0
     */
    List<SProfileEntry> getEntriesOfProfile(long profileId, int fromIndex, int numberOfProfileEntry, String field, OrderByType order)
            throws SBonitaReadException;

    /**
     * Add new profile entry
     *
     * @param profileEntry
     * @return the new created sProfileEntry
     * @throws SProfileEntryCreationException
     *         occurs when an exception is thrown during sProfileEntry creation
     * @since 6.0
     */
    SProfileEntry createProfileEntry(SProfileEntry profileEntry) throws SProfileEntryCreationException;

    /**
     * Update a profileEntry by given profileEntry and new content
     *
     * @param profileEntry
     * @param descriptor
     * @return The updated profile entry
     * @throws SProfileEntryUpdateException
     *         occurs when an exception is thrown during sProfileEntry update
     * @since 6.0
     */
    SProfileEntry updateProfileEntry(SProfileEntry profileEntry, EntityUpdateDescriptor descriptor) throws SProfileEntryUpdateException;

    /**
     * Delete a profileEntry by given profileEntry
     *
     * @param profileEntry
     * @throws SProfileEntryDeletionException
     *         occurs when an exception is thrown during sProfileEntry deletion
     * @since 6.0
     */
    void deleteProfileEntry(SProfileEntry profileEntry) throws SProfileEntryDeletionException;

    /**
     * Delete a profile entry by its id
     *
     * @param profileEntryId
     * @throws SProfileEntryNotFoundException
     *         occurs when the identifier does not refer to an existing sProfileEntry
     * @throws SProfileEntryDeletionException
     *         occurs when an exception is thrown during sProfileEntry deletion
     * @since 6.0
     */
    void deleteProfileEntry(long profileEntryId) throws SProfileEntryNotFoundException, SProfileEntryDeletionException;

    /**
     * Add a user to exist profile
     *
     * @param profileId
     *        the identifier of profile
     * @param userId
     *        the identifier of user
     * @param firstName
     * @param lastName
     * @param userName
     * @return sProfileMember
     * @throws SProfileMemberCreationException
     * @since 6.0
     */
    SProfileMember addUserToProfile(long profileId, long userId, String firstName, String lastName, String userName)
            throws SProfileMemberCreationException;

    /**
     * Add a group to exist profile
     *
     * @param profileId
     *        the identifier of profile
     * @param groupId
     *        the identifier of group
     * @param groupName
     * @param parentPath
     * @return sProfileMember
     * @throws SProfileMemberCreationException
     *         TODO
     * @since 6.0
     */
    SProfileMember addGroupToProfile(long profileId, long groupId, String groupName, String parentPath) throws SProfileMemberCreationException;

    /**
     * Add a role to exist profile
     *
     * @param profileId
     *        the identifier of profile
     * @param roleId
     *        the identifier of role
     * @param roleName
     * @return sProfileMember
     * @throws SProfileMemberCreationException
     *         TODO
     * @since 6.0
     */
    SProfileMember addRoleToProfile(long profileId, long roleId, String roleName) throws SProfileMemberCreationException;

    /**
     * Add a role and a group to exist profile
     *
     * @param profileId
     *        the identifier of profile
     * @param roleId
     *        the identifier of role
     * @param groupId
     *        the identifier of group
     * @param roleName
     * @param groupName
     * @param groupParentPath
     * @return sProfileMember
     * @throws SProfileMemberCreationException
     *         TODO
     * @since 6.0
     */
    SProfileMember addRoleAndGroupToProfile(long profileId, long roleId, long groupId, String roleName, String groupName, String groupParentPath)
            throws SProfileMemberCreationException;

    /**
     * Get all sProfileMembers with same user having the given value for the given int index
     *
     * @param userId
     *        the identifier of user
     * @param fromIndex
     *        first result to be considered(>=0)
     * @param numberOfProfileEntry
     *        the max number of profileEntry to be returned (>=0)
     * @param field
     * @param order
     * @return sProfileMember
     * @throws SBonitaReadException
     *         occurs when an exception is thrown during sProfileMember creation
     * @since 6.0
     */
    List<SProfileMember> getProfileMembersOfUser(final long userId, int fromIndex, int numberOfElements, String field, OrderByType order)
            throws SBonitaReadException;

    /**
     * Get all sProfileMembers with same group having the given value for the given int index
     *
     * @param groupId
     *        the identifier of group
     * @param fromIndex
     *        first result to be considered(>=0)
     * @param numberOfProfileEntry
     *        the max number of profileEntry to be returned (>=0)
     * @param field
     * @param order
     * @return sProfileMember
     * @throws SBonitaReadException
     *         occurs when an exception is thrown during sProfileMember creation
     * @since 6.0
     */
    List<SProfileMember> getProfileMembersOfGroup(final long groupId, int fromIndex, int numberOfElements, String field, OrderByType order)
            throws SBonitaReadException;

    /**
     * Get all sProfileMembers with same group having the given value for the given int index
     *
     * @param roleId
     *        the identifier of role
     * @param fromIndex
     *        first result to be considered(>=0)
     * @param numberOfProfileEntry
     *        the max number of profileEntry to be returned (>=0)
     * @param field
     * @param order
     * @return sProfileMember
     * @throws SBonitaReadException
     *         occurs when an exception is thrown during sProfileMember creation
     * @since 6.0
     */
    List<SProfileMember> getProfileMembersOfRole(final long roleId, int fromIndex, int numberOfElements, String field, OrderByType order)
            throws SBonitaReadException;

    /**
     * Delete a profile member by its id
     *
     * @param profileMemberId
     * @throws SProfileMemberDeletionException
     *         occurs when an exception is thrown during sProfileMember deletion
     * @throws SProfileMemberNotFoundException
     *         occurs when the identifier does not refer to an existing sProfileMember
     * @since 6.0
     */
    void deleteProfileMember(long profileMemberId) throws SProfileMemberDeletionException, SProfileMemberNotFoundException;

    /**
     * Delete a profile member by given sProfile member
     *
     * @param profileMember
     * @throws SProfileMemberDeletionException
     *         occurs when an exception is thrown during sProfileMember deletion
     * @since 6.0
     */
    void deleteProfileMember(SProfileMember profileMember) throws SProfileMemberDeletionException;

    /**
     * Get the total number of profile members
     *
     * @param querySuffix
     *        the suffix of query string
     * @param countOptions
     *        The criterion used to search profileMembers
     * @return the total number of profile members
     * @throws SBonitaReadException
     * @since 6.0
     */
    long getNumberOfProfileMembers(String querySuffix, final QueryOptions countOptions) throws SBonitaReadException;

    /**
     * Get all sProfileMember by profileId and queryOptions
     *
     * @param querySuffix
     *        the suffix of query string
     * @param countOptions
     *        The criterion used to search profileMembers
     * @return all sProfileMember by profileId and queryOptions
     * @throws SBonitaReadException
     * @since 6.0
     */
    List<SProfileMember> searchProfileMembers(String querySuffix, final QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * Get the total number of sProfileMember by a list contains profileIds
     *
     * @param profileIds
     * @return A list of <profileId, NumberOfMemberForThatProfile>
     * @throws SBonitaReadException
     * @since 6.0
     */
    List<SProfileMember> getProfileMembers(List<Long> profileIds) throws SBonitaReadException;

    /**
     * Get profile by given name
     *
     * @param profileName
     * @return sProfile
     * @throws SProfileNotFoundException
     *         occurs when the identifier does not refer to an existing sProfile
     * @since 6.0
     */
    SProfile getProfileByName(String profileName) throws SProfileNotFoundException;

    /**
     * Get a list of profileMembers by the given profileId
     *
     * @param profileId
     * @return a list of profileMembers
     * @throws SProfileMemberNotFoundException
     */
    List<SProfileMember> getProfileMembers(long profileId, QueryOptions queryOptions) throws SProfileMemberNotFoundException;

    /**
     * Get a list of profileEntries by the given profileId and parentId
     *
     * @param profileId
     * @param parentId
     * @param fromIndex
     * @param numberOfProfileEntries
     * @param field
     * @param order
     * @return a list of profileEntries
     * @throws SBonitaReadException
     */
    List<SProfileEntry> getEntriesOfProfileByParentId(long profileId, long parentId, int fromIndex, int numberOfProfileEntries, String field, OrderByType order)
            throws SBonitaReadException;

    /**
     * Delete all profile members for the connected tenant
     *
     * @throws SProfileMemberDeletionException
     * @since 6.1
     */
    void deleteAllProfileMembers() throws SProfileMemberDeletionException;

    /**
     * @param queryOptions
     * @return
     */
    long getNumberOfProfiles(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * @param queryOptions
     * @return
     */
    List<SProfile> searchProfiles(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * @param queryOptions
     * @return
     * @throws SBonitaReadException
     */
    long getNumberOfProfileEntries(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * @param queryOptions
     * @return
     * @throws SBonitaReadException
     */
    List<SProfileEntry> searchProfileEntries(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * @param profile
     * @throws SProfileMemberDeletionException
     * @since 6.3.1
     */
    void deleteAllProfileMembersOfProfile(SProfile profile) throws SProfileMemberDeletionException;

    /**
     * @param profile
     * @throws SProfileEntryDeletionException
     * @since 6.3.1
     */
    void deleteAllProfileEntriesOfProfile(SProfile profile) throws SProfileEntryDeletionException;

    /**
     * @param profileMemberId
     * @return
     * @throws SProfileMemberNotFoundException
     * @since 6.3.1
     */
    SProfileMember getProfileMemberWithoutDisplayName(final long profileMemberId) throws SProfileMemberNotFoundException;

    /**
     * updates profile metaData fields lastUpdateDate and lastUpdatedBy for a given profile
     *
     * @param profileId
     * @throws SProfileUpdateException
     *         when given profileId is not found
     */
    void updateProfileMetaData(final long profileId) throws SProfileUpdateException;

}
