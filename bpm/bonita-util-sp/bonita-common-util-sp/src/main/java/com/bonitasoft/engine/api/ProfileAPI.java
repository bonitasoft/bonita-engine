/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.bonitasoft.engine.bpm.model.ProfileEntryUpdateDescriptor;
import com.bonitasoft.engine.bpm.model.ProfileUpdateDescriptor;
import com.bonitasoft.engine.exception.profile.ProfileCreationException;
import com.bonitasoft.engine.exception.profile.ProfileDeletionException;
import com.bonitasoft.engine.exception.profile.ProfileEntryCreationException;
import com.bonitasoft.engine.exception.profile.ProfileEntryDeletionException;
import com.bonitasoft.engine.exception.profile.ProfileEntryUpdateException;
import com.bonitasoft.engine.exception.profile.ProfileExportException;
import com.bonitasoft.engine.exception.profile.ProfileImportException;
import com.bonitasoft.engine.exception.profile.ProfileUpdateException;
import com.bonitasoft.engine.profile.ImportPolicy;

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
     * @return The created profile
     *         the new created profile
     * @throws InvalidSessionException
     *             occurs when the session is not valid
     * @throws ProfileCreationException
     *             errors thrown if can't create the new profile
     * @since 6.0
     */
    Map<String, Serializable> createProfile(String name, String description, String iconPath) throws ProfileCreationException;

    /**
     * Delete a specific profile
     * 
     * @param id
     *            profile identifier to delete
     * @throws InvalidSessionException
     *             occurs when the session is not valid
     * @throws ProfileDeletionException
     *             errors thrown if can't delete the profile
     * @since 6.0
     */
    void deleteProfile(long id) throws ProfileDeletionException;

    /**
     * Export all profiles from DataBase to XML file
     * 
     * @return
     *         the content of the xml file containing all profiles
     * @throws InvalidSessionException
     *             occurs when the session is not valid
     * @throws ProfileExportException
     *             errors thrown if can't export profiles
     * @since 6.0
     */
    byte[] exportAllProfiles() throws ProfileExportException;

    /**
     * Export specific profiles from DataBase to XML file
     * 
     * @param profileIds
     *            profile identifiers to export
     * @return
     *         the content of the xml file containing profiles to export
     * @throws InvalidSessionException
     *             occurs when the session is not valid
     * @throws ProfileExportException
     *             errors thrown if can't export profiles
     * @since 6.0
     */
    byte[] exportProfilesWithIdsSpecified(long[] profileIds) throws ProfileExportException;

    /**
     * Import profiles from XML file.
     * 
     * @param xmlContent
     *            xml content to import
     * @param policy
     *            import policy to define different way how to import xml content in different case
     * @return
     *         a List<String> is a warning message list in case of non-existing User, Group or Role to map the profile to.
     * @throws InvalidSessionException
     *             occurs when the session is not valid
     * @throws ProfileImportException
     *             errors thrown if can't import profiles
     * @since 6.0
     */
    List<String> importProfilesUsingSpecifiedPolicy(byte[] xmlContent, ImportPolicy policy) throws ProfileImportException;

    /**
     * Update a profile.
     * 
     * @param id
     *            the profile id to update
     * @param updateDescriptor
     *            including new value of all attributes adaptable
     * @return a Map<String, Serializable> represent the updated Profile.
     *         The String key represent the attribute name, and the Serializable value represent the attribute value.
     * @throws InvalidSessionException
     *             occurs when the session is not valid
     * @throws ProfileUpdateException
     *             errors thrown if can't update profiles
     * @since 6.0
     */
    Map<String, Serializable> updateProfile(long id, ProfileUpdateDescriptor updateDescriptor) throws ProfileUpdateException;

    /**
     * Create a new profile entry
     * 
     * @param name
     *            the profile entry name
     * @param description
     *            the profile entry description
     * @param parentId
     *            The direct parent element Id
     * @param profileId
     *            the linked profile id
     * @param index
     *            the position of the profile entry in list of entry of the parent.
     * @param type
     *            the value must be "folder" or "link". If type = "link", the following parameter is also mandatory:
     * @param page
     *            token of the linked page
     * @return The created profile entry
     * @throws InvalidSessionException
     *             occurs when the session is not valid
     * @throws ProfileEntryCreationException
     *             errors thrown if can't create the new profile entry
     * @since 6.0
     */
    Map<String, Serializable> createProfileEntry(String name, String description, Long parentId, long profileId, Long index, String type, String page)
            throws ProfileEntryCreationException;

    /**
     * Delete a specific profile entry
     * 
     * @param profileEntryId
     *            profile entry identifier to delete
     * @throws InvalidSessionException
     *             occurs when the session is not valid
     * @throws ProfileEntryDeletionException
     *             errors thrown if can't delete the profile entry
     * @since 6.0
     */
    void deleteProfileEntry(long profileEntryId) throws ProfileEntryDeletionException;

    /**
     * Update a profile entry.
     * 
     * @param id
     *            the profile entry id to update
     * @param updateDescriptor
     *            including new value of all attributes adaptable
     * @return the updated profile entry
     * @throws InvalidSessionException
     *             occurs when the session is not valid
     * @throws ProfileEntryUpdateException
     *             errors thrown if can't update the profile entry
     * @since 6.0
     */
    Map<String, Serializable> updateProfileEntry(long id, ProfileEntryUpdateDescriptor updateDescriptor) throws ProfileEntryUpdateException;

}
