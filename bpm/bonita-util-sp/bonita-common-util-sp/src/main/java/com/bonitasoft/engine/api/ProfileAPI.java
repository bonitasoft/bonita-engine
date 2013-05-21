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

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.exception.profile.ProfileEntryNotFoundException;
import org.bonitasoft.engine.exception.profile.ProfileNotFoundException;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileCreator;
import org.bonitasoft.engine.profile.ProfileEntry;
import org.bonitasoft.engine.profile.ProfileEntryCreator;

import com.bonitasoft.engine.profile.ImportPolicy;
import com.bonitasoft.engine.profile.ProfileEntryUpdater;
import com.bonitasoft.engine.profile.ProfileUpdater;

/**
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public interface ProfileAPI extends org.bonitasoft.engine.api.ProfileAPI {

    /**
     * Create a new profile
     * 
     * @param name
     *            the profile name
     * @param description
     *            the profile description
     * @param iconPath
     *            the profile icon path
     * @return The new created profile
     * @throws CreationException
     *             errors thrown if can't create the new profile
     * @since 6.0
     */
    Profile createProfile(String name, String description, String iconPath) throws AlreadyExistsException, CreationException;

    /**
     * Create a new profile
     * 
     * @param creator
     *            fields to initialize
     * @return The new created profile
     * @throws CreationException
     *             errors thrown if can't create the new profile
     * @since 6.0
     */
    Profile createProfile(ProfileCreator creator) throws AlreadyExistsException, CreationException;

    /**
     * Delete a specific profile
     * 
     * @param id
     *            profile identifier to delete
     * @throws DeletionException
     *             errors thrown if can't delete the profile
     * @since 6.0
     */
    void deleteProfile(long id) throws DeletionException;

    /**
     * Export all profiles from DataBase to XML file
     * 
     * @return
     *         the content of the xml file containing all profiles
     * @throws ExecutionException
     *             errors thrown if can't export profiles
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
     *             errors thrown if can't export profiles
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
     *         a List<String> is a warning message list in case of non-existing User, Group or Role to map the profile to.
     * @throws ExecutionException
     *             errors thrown if can't import profiles
     * @since 6.0
     */
    List<String> importProfilesUsingSpecifiedPolicy(byte[] xmlContent, ImportPolicy policy) throws ExecutionException;

    /**
     * Update a profile.
     * 
     * @param id
     *            the profile id to update
     * @param updater
     *            including new value of all attributes adaptable
     * @return the updated Profile.
     * @throws UpdateException
     *             errors thrown if can't update profiles
     * @since 6.0
     */
    Profile updateProfile(long id, ProfileUpdater updater) throws ProfileNotFoundException, UpdateException;

    /**
     * Create a new profile entry
     * 
     * @param creator
     *            fields to initialize.
     *            The type value must be "folder" or "link". If type = "link", the page is also mandatory.
     * @return The created profile entry
     * @throws CreationException
     *             errors thrown if can't create the new profile entry
     * @since 6.0
     */
    ProfileEntry createProfileEntry(ProfileEntryCreator creator) throws AlreadyExistsException, CreationException;

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
     * @return The created profile entry
     * @throws CreationException
     *             errors thrown if can't create the new profile entry
     * @since 6.0
     */
    ProfileEntry createProfileEntry(String name, String description, long profileId, String type) throws AlreadyExistsException, CreationException;

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
     *             errors thrown if can't create the new profile entry
     * @since 6.0
     */
    ProfileEntry createProfileEntry(String name, String description, long profileId, String type, String page) throws AlreadyExistsException, CreationException;

    /**
     * Delete a specific profile entry
     * 
     * @param id
     *            profile entry identifier to delete
     * @throws DeletionException
     *             errors thrown if can't delete the profile entry
     * @since 6.0
     */
    void deleteProfileEntry(long id) throws DeletionException;

    /**
     * Update a profile entry.
     * 
     * @param id
     *            the profile entry id to update
     * @param updater
     *            including new value of all attributes adaptable
     * @return the updated profile entry
     * @throws UpdateException
     *             errors thrown if can't update the profile entry
     * @since 6.0
     */
    ProfileEntry updateProfileEntry(long id, ProfileEntryUpdater updater) throws ProfileEntryNotFoundException, UpdateException;

}
