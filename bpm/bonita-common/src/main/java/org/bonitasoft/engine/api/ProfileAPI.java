/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.api;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileCriterion;
import org.bonitasoft.engine.profile.ProfileEntry;
import org.bonitasoft.engine.profile.ProfileEntryNotFoundException;
import org.bonitasoft.engine.profile.ProfileEntrySearchDescriptor;
import org.bonitasoft.engine.profile.ProfileMember;
import org.bonitasoft.engine.profile.ProfileMemberCreator;
import org.bonitasoft.engine.profile.ProfileNotFoundException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

/**
 * Profiles are a notion used in Bonita Portal to give and control access to some specific features of the Bonita suite.
 * Profiles are associated to Bonita Identity / Organization notions: users, groups, roles, memberships.
 * <code>ProfileAPI</code> gives access to some of the
 * profile administration: adding / removing members to / from a profile, retrieving / searching for profiles. <br>
 * Full control on profiles is part of <b>Subscription</b> editions of Bonita suite.
 *
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 * @see SearchResult SearchResult for general knowledege on Search mechanism in Bonita.
 */
public interface ProfileAPI {

    /**
     * Retrieves the profile.
     *
     * @param id
     *        The identifier of the profile
     * @return the searched profile
     * @throws ProfileNotFoundException
     *         If the identifier does not refer to an existing profile
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs during the profile retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    Profile getProfile(long id) throws ProfileNotFoundException;

    /**
     * Retrieves the profiles of the user.
     *
     * @param userId
     *        The identifier of the user
     * @return The 1000 first profiles of the user, ordered by name
     * @throws UserNotFoundException
     *         If the user identifier does not refer to an existing user
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs during the profile retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     * @deprecated since 6.3
     * @see ProfileAPI#getProfilesForUser(long, int, int, ProfileCriterion)
     */
    @Deprecated
    List<Profile> getProfilesForUser(long userId) throws UserNotFoundException;

    /**
     * Retrieves the profiles of the user.
     *
     * @param userId
     *        The identifier of the user
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of elements to get per page.
     * @param criterion
     *        The criterion for sorting the items over pages.
     * @return The paginated and ordered profiles of the user
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs during the profile retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.3.2
     */
    List<Profile> getProfilesForUser(long userId, int startIndex, int maxResults, ProfileCriterion criterion);

    /**
     * Retrieves the profiles with portal navigation for the user.
     *
     * @param userId
     *        The identifier of the user
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of elements to get per page.
     * @param criterion
     *        The criterion for sorting the items over pages.
     * @return The paginated and ordered profiles of the user
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs during the profile retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 7.6.1
     * @deprecated since 7.13.0 use getProfilesForUser instead
     */
    @Deprecated(forRemoval = true)
    List<Profile> getProfilesWithNavigationForUser(long userId, int startIndex, int maxResults,
            ProfileCriterion criterion);

    /**
     * Searches for {@link Profile}s with specific search criteria. Use
     * {@link org.bonitasoft.engine.profile.ProfileSearchDescriptor} to
     * know the available filters.
     *
     * @param options
     *        The search criteria
     * @return a {@link SearchResult} containing the list of {@code Profile}s matching the search criteria.
     * @throws SearchException
     *         If an exception occurs during the profile searching
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     * @see Profile
     * @see org.bonitasoft.engine.profile.ProfileSearchDescriptor
     * @see SearchResult
     */
    SearchResult<Profile> searchProfiles(SearchOptions options) throws SearchException;

    /**
     * Retrieves the number of profile members for the profiles. The map contains the couples
     * profileId/numberOfProfileMembers.
     * <p>
     * If a profile does not exist, no exception is thrown and no value is added in the map.
     * </p>
     *
     * @param profileIds
     *        The identifiers of the profiles
     * @return the number of profile members for the profiles
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs during the profile retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    Map<Long, Long> getNumberOfProfileMembers(List<Long> profileIds);

    /**
     * Searches for {@link ProfileMember}s with specific search criteria. Use
     * {@link org.bonitasoft.engine.profile.ProfileMemberSearchDescriptor} to
     * know the available filters.
     *
     * @param memberType
     *        The member type, it can be: user, role, group, roleAndGroup.
     * @param options
     *        The search criteria
     * @return a {@link SearchResult} containing the list of {@code ProfileMember}s matching the search criteria.
     * @throws SearchException
     *         If an exception occurs during the profile searching
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     * @see ProfileMember
     * @see org.bonitasoft.engine.profile.ProfileMemberSearchDescriptor
     * @see SearchResult
     */
    SearchResult<ProfileMember> searchProfileMembers(String memberType, SearchOptions options) throws SearchException;

    /**
     * Searches for {@link ProfileEntry}s with specific search criteria. Use
     * {@link org.bonitasoft.engine.profile.ProfileEntrySearchDescriptor} to
     * know the available filters.
     *
     * @param options
     *        The search criteria.
     * @return a {@link SearchResult} containing the list of {@code ProfileEntry}s matching the search criteria.
     * @throws SearchException
     *         If an exception occurs during the profile searching
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     * @see ProfileEntry
     * @see ProfileEntrySearchDescriptor
     * @see SearchResult
     * @deprecated since 7.13.0, use {@link org.bonitasoft.engine.business.application.Application} instead
     */
    @Deprecated(since = "7.13.0", forRemoval = true)
    SearchResult<ProfileEntry> searchProfileEntries(SearchOptions options) throws SearchException;

    /**
     * Retrieves the full portal navigation of a profile.
     * <br/>
     * This navigation is what a user having the given profile have in the navigation bar of the portal.
     * <p/>
     * <b>WARNING:</b> this method is <b>Experimental</b>, it might change in later versions
     *
     * @param profileName name of the profile
     * @return the list of profile entries linked to this profile
     * @since 7.8
     * @deprecated since 7.13.0, use {@link org.bonitasoft.engine.business.application.Application} instead
     */
    @Deprecated(since = "7.13.0", forRemoval = true)
    @Experimental
    List<ProfileEntry> getProfileEntries(String profileName) throws ProfileNotFoundException;

    /**
     * Returns a profile entry according to its identifier.
     *
     * @param id
     *        The profile entry identifier
     * @return the searched profile entry
     * @throws ProfileEntryNotFoundException
     *         occurs when the identifier does not refer to an existing profile entry
     * @since 6.0
     */

    /**
     * Retrieves the profile entry.
     *
     * @param id
     *        The identifier of the profile entry
     * @return the searched profile entry
     * @throws ProfileEntryNotFoundException
     *         If the profile entry identifier does not refer to an existing user
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs during the user retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     * @deprecated since 7.13.0, use {@link org.bonitasoft.engine.business.application.Application} instead
     */
    @Deprecated(since = "7.13.0", forRemoval = true)
    ProfileEntry getProfileEntry(long id) throws ProfileEntryNotFoundException;

    /**
     * Creates a profile member.
     *
     * @param profileId
     *        The identifier of the profile
     * @param userId
     *        The identifier of the user
     * @param groupId
     *        The identifier of the group
     * @param roleId
     *        The identifier of the role
     * @return the created profile member
     * @throws AlreadyExistsException
     *         If the tuple profileId/userId/roleId/groupId is already taken by an existing profile member
     * @throws CreationException
     *         If an exception occurs during the profile member creation
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    ProfileMember createProfileMember(Long profileId, Long userId, Long groupId, Long roleId)
            throws CreationException, AlreadyExistsException;

    /**
     * Creates a profile member.
     * <p>
     * It takes the values of the creator in order to create the profile member.
     * </p>
     *
     * @param creator
     *        The profile member to create
     * @return the created profile member
     * @throws AlreadyExistsException
     *         If the tuple profileId/userId/roleId/groupId is already taken by an existing profile member
     * @throws CreationException
     *         If an exception occurs during the profile member creation
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    ProfileMember createProfileMember(ProfileMemberCreator creator) throws CreationException, AlreadyExistsException;

    /**
     * Deletes the profile member.
     *
     * @param id
     *        The identifier of the profile member
     * @throws DeletionException
     *         If an exception occurs during the profile member deletion
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    void deleteProfileMember(Long id) throws DeletionException;

}
