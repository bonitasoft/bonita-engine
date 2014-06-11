/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import java.util.List;

import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileEntry;
import org.bonitasoft.engine.profile.ProfileEntryNotFoundException;
import org.bonitasoft.engine.profile.ProfileNotFoundException;

import com.bonitasoft.engine.profile.ImportPolicy;
import com.bonitasoft.engine.profile.ProfileCreator;
import com.bonitasoft.engine.profile.ProfileEntryCreator;
import com.bonitasoft.engine.profile.ProfileEntryUpdater;
import com.bonitasoft.engine.profile.ProfileUpdater;

/**
 * Profiles are a notion used in Bonita BPM Portal to give and control access to some specific features of the Bonita BPM suite.
 * Profiles are associated to Bonita Identity / Organization notions: users, groups, roles, memberships. <code>ProfileAPI</code> gives FULL access on
 * profile administation: creation / update / removal of profiles, adding / removing members to / from a profile, retrieving / searching for profiles. <br/>
 * Import / export of all profiles is also part of this API. <br/>
 * ProfileAPI also gives access to ProfileEntry management, which are a way to map profiles to features / pages in Bonita BPM Portal *
 * 
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 * @see ProfileEntry
 */
public interface ProfileAPI extends org.bonitasoft.engine.api.ProfileAPI {

    /**
     * Create a new custom profile
     * 
     * @param name
     *            the profile name
     * @param description
     *            the profile description
     * @param iconPath
     *            the profile icon path
     * @return The new created custom profile
     * @throws CreationException
     *             If can't create the new profile
     * @throws AlreadyExistsException
     *             If the profile already exists
     * @deprecated use {@link #createProfile(String name, String description)}
     * @since 6.0
     */
    @Deprecated
    Profile createProfile(String name, String description, String iconPath) throws AlreadyExistsException, CreationException;

    /**
     * Create a new custom profile
     * 
     * @param name
     *            the profile name
     * @param description
     *            the profile description
     * @param iconPath
     *            the profile icon path
     * @return The new created custom profile
     * @throws CreationException
     *             If can't create the new profile
     * @throws AlreadyExistsException
     *             If the profile already exists
     * @since 6.3.1
     */
    Profile createProfile(String name, String description) throws AlreadyExistsException, CreationException;

    /**
     * Create a new custom profile
     * 
     * @param creator
     *            the attributes to initialize
     * @return The new created custom profile
     * @throws CreationException
     *             If can't create the new profile
     * @throws AlreadyExistsException
     *             If the profile already exists
     * @since 6.0
     */
    Profile createProfile(ProfileCreator creator) throws AlreadyExistsException, CreationException;

    /**
     * Delete a specific custom profile
     * 
     * @param id
     *            the identifier of the profile to delete
     * @throws DeletionException
     *             If can't delete the profile
     * @since 6.0
     */
    void deleteProfile(long id) throws DeletionException;

    /**
     * Export all profiles from DataBase to XML file
     * 
     * @return
     *         The content of the xml file containing all profiles
     * @throws ExecutionException
     *             If can't export profiles
     * @since 6.0
     */
    byte[] exportAllProfiles() throws ExecutionException;

    /**
     * Export specific profiles from DataBase to XML file
     * 
     * @param profileIds
     *            profile identifiers to export
     * @return
     *         the content of the xml file containing profiles to export
     * @throws ExecutionException
     *             If can't export profiles
     * @since 6.0
     */
    byte[] exportProfilesWithIdsSpecified(long[] profileIds) throws ExecutionException;

    /**
     * Import profiles from XML file.
     * 
     * @param xmlContent
     *            xml content to import
     * @param policy
     *            import policy to define different way how to import xml content in different case
     * @return
     *         A List<String> is a warning message list in case of non-existing User, Group or Role to map the profile to.
     * @throws ExecutionException
     *             If can't import profiles
     * 
     * @deprecated use {@link #importProfiles(byte[], ImportPolicy)}
     * @since 6.0
     */
    @Deprecated
    List<String> importProfilesUsingSpecifiedPolicy(byte[] xmlContent, ImportPolicy policy) throws ExecutionException;

    /**
     * Update a custom profile.
     * 
     * @param id
     *            the identifier of the profile to update
     * @param updater
     *            the attributes to update
     * @return The updated custom Profile.
     * @throws UpdateException
     *             If can't update profiles
     * @throws ProfileNotFoundException
     *             If the profile to update cannot be found with the given id
     * @throws AlreadyExistsException
     *             If a profile with the new name already exists
     * @since 6.1
     */
    Profile updateProfile(long id, ProfileUpdater updater) throws ProfileNotFoundException, UpdateException, AlreadyExistsException;

    /**
     * Create a new profile entry
     * 
     * @param creator
     *            the attributes to initialize.
     *            The type value must be "folder" or "link". If type = "link", the page is also mandatory.
     *            Index must be an odd long value if the profile is not inserted at last position
     *            To insert at first position, the value of index must be -1
     * @return The created profile entry
     * @throws CreationException
     *             If can't create the new profile entry
     * @since 6.0
     */
    ProfileEntry createProfileEntry(ProfileEntryCreator creator) throws CreationException;

    /**
     * Create a new profile entry
     * 
     * @param name
     *            the profile entry name
     * @param description
     *            the profile entry description
     * @param profileId
     *            the linked profile identifier
     * @param type
     *            the value must be "folder" or "link". If type = "link", the page parameter.
     * @return The created profile entry
     * @throws CreationException
     *             If can't create the new profile entry
     * @since 6.0
     */
    ProfileEntry createProfileEntry(String name, String description, long profileId, String type) throws CreationException;

    /**
     * Create a new profile entry
     * 
     * @param name
     *            the profile entry name
     * @param description
     *            the profile entry description
     * @param profileId
     *            the linked profile id
     * @param type
     *            the value must be "folder" or "link". If type = "link", the page parameter.
     * @param page
     *            token of the linked page
     * @return The created profile entry
     * @throws CreationException
     *             If can't create the new profile entry
     * @since 6.0
     */
    ProfileEntry createProfileEntry(String name, String description, long profileId, String type, String page) throws CreationException;

    /**
     * Delete a specific profile entry
     * 
     * @param id
     *            the identifier of the profile entry to delete
     * @throws DeletionException
     *             If can't delete the profile entry
     * @since 6.0
     */
    void deleteProfileEntry(long id) throws DeletionException;

    /**
     * Update a profile entry.
     * 
     * @param id
     *            the identifier of the profile entry to update
     * @param updater
     *            the attributes to update
     * @return the updated profile entry
     * @throws ProfileEntryNotFoundException
     *             if no <code>ProfileEntry</code> can be found with the provided ID.
     * @throws UpdateException
     *             If can't update the profile entry
     *             Index must be an odd long value if index is updated
     *             To put at first position, the value of index must be -1
     * @since 6.0
     */
    ProfileEntry updateProfileEntry(long id, ProfileEntryUpdater updater) throws ProfileEntryNotFoundException, UpdateException;

    /**
     * Import profiles from XML file.
     * 
     * @param xmlContent
     *            xml content to import
     * @param policy
     *            import policy to define different ways of how to import xml content in different case
     * @return
     *         A list that is the result of the import, there is one element for each profile
     * @throws ExecutionException
     *             If there is an unexpected error during the imports
     * 
     * @since 6.3.1
     */
    List<ImportStatus> importProfiles(final byte[] xmlContent, final ImportPolicy policy) throws ExecutionException;

}
