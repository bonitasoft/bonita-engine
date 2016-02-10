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
package org.bonitasoft.engine.external.identity.mapping;

import java.util.List;

import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMapping;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @since 6.0
 */
public interface ExternalIdentityMappingService {

    /**
     * Create externalIdentityMapping in DB for given externalIdentityMapping
     *
     * @param externalIdentityMapping
     *        an SExternalIdentityMapping object will be stored in DB
     * @return the new created SExternalIdentityMapping object
     * @throws SExternalIdentityMappingAlreadyExistsException
     *         error thrown if externalIdentityMapping already exists in DB
     * @throws SExternalIdentityMappingCreationException
     */
    SExternalIdentityMapping createExternalIdentityMapping(SExternalIdentityMapping externalIdentityMapping)
            throws SExternalIdentityMappingAlreadyExistsException, SExternalIdentityMappingCreationException;

    /**
     * Get externalIdentityMapping by its id
     *
     * @param mappingId
     *        the identifier of externalIdentityMapping
     * @return the externalIdentityMapping have id corresponding to the parameter
     * @throws SExternalIdentityMappingNotFoundException
     *         error thrown if no externalIdentityMapping have an id corresponding to the parameter.
     */
    SExternalIdentityMapping getExternalIdentityMappingById(long mappingId) throws SExternalIdentityMappingNotFoundException;

    /**
     * Get externalIdentityMapping by its id with displayNamePart having user related informations
     *
     * @param mappingId
     *        the identifier of externalIdentityMapping
     * @return the externalIdentityMapping with displayNamePart having userName informations
     * @throws SExternalIdentityMappingNotFoundException
     *         error thrown if no externalIdentityMapping have an id corresponding to the parameter.
     */
    SExternalIdentityMapping getExternalIdentityMappingForUser(long mappingId) throws SExternalIdentityMappingNotFoundException;

    /**
     * Get externalIdentityMapping by its id with displayNamePart having group related informations
     *
     * @param mappingId
     *        the identifier of externalIdentityMapping
     * @return the externalIdentityMapping with displayNamePart having group related informations
     * @throws SExternalIdentityMappingNotFoundException
     *         error thrown if no externalIdentityMapping have an id corresponding to the parameter.
     */
    SExternalIdentityMapping getExternalIdentityMappingForGroup(long mappingId) throws SExternalIdentityMappingNotFoundException;

    /**
     * Get externalIdentityMapping by its id with displayNamePart having role related informations
     *
     * @param mappingId
     *        the identifier of externalIdentityMapping
     * @return the externalIdentityMapping with displayNamePart having role related informations
     * @throws SExternalIdentityMappingNotFoundException
     *         error thrown if no externalIdentityMapping have an id corresponding to the parameter.
     */
    SExternalIdentityMapping getExternalIdentityMappingForRole(long mappingId) throws SExternalIdentityMappingNotFoundException;

    /**
     * Get externalIdentityMapping by its id with displayNamePart having role and group related informations
     *
     * @param mappingId
     *        the identifier of externalIdentityMapping
     * @return the externalIdentityMapping with displayNamePart having role and group related informations
     * @throws SExternalIdentityMappingNotFoundException
     *         error thrown if no externalIdentityMapping have an id corresponding to the parameter.
     */
    SExternalIdentityMapping getExternalIdentityMappingForRoleAndGroup(long mappingId) throws SExternalIdentityMappingNotFoundException;

    /**
     * Delete the id specified externalIdentityMapping
     *
     * @param mappingId
     *        the identifier of externalIdentityMapping
     * @throws SExternalIdentityMappingNotFoundException
     *         error thrown if no externalIdentityMapping have an id corresponding to the parameter.
     * @throws SExternalIdentityMappingDeletionException
     */
    void deleteExternalIdentityMapping(long mappingId) throws SExternalIdentityMappingNotFoundException, SExternalIdentityMappingDeletionException;

    /**
     * Delete the specific externalIdentityMapping
     *
     * @param externalIdentityMapping
     *        the externalIdentityMapping will be deleted
     * @throws SExternalIdentityMappingDeletionException
     */
    void deleteExternalIdentityMapping(SExternalIdentityMapping externalIdentityMapping) throws SExternalIdentityMappingDeletionException;

    /**
     * Delete all external identity mapping for the connected tenant
     *
     * @throws SExternalIdentityMappingDeletionException
     * @since 6.1
     */
    void deleteAllExternalIdentityMappings() throws SExternalIdentityMappingDeletionException;

    /**
     * Get all externalIdentityMappings for specific kind according to the queryOptions. use querySuffix to indicate what the displayNamePart is related to
     *
     * @param kind
     *        the discriminator of the <code>SExternalIdentityMapping</code> to search for.
     * @param queryOptions
     *        a QueryOptions object containing some query conditions
     * @param querySuffix
     *        The query suffix, it can be "USER", "GROUP", "ROLE", "MEMBERSHIP";
     * @return a list of SExternalIdentityMapping objects matching the criteria.
     * @throws SBonitaReadException
     */
    List<SExternalIdentityMapping> searchExternalIdentityMappings(String kind, QueryOptions queryOptions, String querySuffix) throws SBonitaReadException;

    /**
     * Get total number of externalIdentityMappings for specific kind according to the queryOptions.
     *
     * @param kind
     *        the discriminator of the <code>SExternalIdentityMapping</code> to search for.
     * @param searchOptions
     *        a QueryOptions object containing some query conditions
     * @param querySuffix
     *        the query suffix, it can be "USER", "GROUP", "ROLE", "MEMBERSHIP";
     * @return number of externalIdentityMappings matching the criteria.
     * @throws SBonitaReadException
     */
    long getNumberOfExternalIdentityMappings(String kind, QueryOptions searchOptions, String querySuffix) throws SBonitaReadException;

    /**
     * Search all <code>SExternalIdentityMapping</code> objects associated with the specified externalId and kind for the specific user. Use querySuffix to
     * indicate what the displayNamePart is related to
     *
     * @param kind
     *        the discriminator of the <code>SExternalIdentityMapping</code> to search for.
     * @param userId
     *        The identifier of user
     * @param externalId
     *        the external Id identifying the <code>SExternalIdentityMapping</code>s to search.
     * @param queryOptions
     *        a QueryOptions object containing some query conditions
     * @param querySuffix
     *        the query suffix, it can be "USER", "GROUP", "ROLE", "MEMBERSHIP";
     * @return the list of results matching the criteria.
     * @throws SBonitaReadException
     */
    List<SExternalIdentityMapping> searchExternalIdentityMappingsForUser(String kind, long userId, final String externalId, QueryOptions queryOptions,
            String querySuffix) throws SBonitaReadException;

    /**
     * Get total number of <code>SExternalIdentityMapping</code> objects associated with the specified externalId and kind for the specific user. Use
     * querySuffix to
     * indicate what the displayNamePart is related to
     *
     * @param kind
     *        the discriminator of the <code>SExternalIdentityMapping</code> to search for.
     * @param userId
     *        The identifier of user
     * @param externalId
     *        the external Id identifying the <code>SExternalIdentityMapping</code>s to search.
     * @param searchOptions
     *        a QueryOptions object containing some query conditions
     * @param querySuffix
     *        the query suffix, it can be "USER", "GROUP", "ROLE", "MEMBERSHIP";
     * @return the number of externalIdentityMappings matching the criteria.
     * @throws SBonitaReadException
     */
    long getNumberOfExternalIdentityMappingsForUser(String kind, long userId, final String externalId, QueryOptions searchOptions, String querySuffix)
            throws SBonitaReadException;

    /**
     * Search all <code>SExternalIdentityMapping</code> objects associated with the specified externalId and kind.
     *
     * @param kind
     *        the discriminator of the <code>SExternalIdentityMapping</code> to search for.
     * @param externalId
     *        the external Id identifying the <code>SExternalIdentityMapping</code>s to search.
     * @param queryOptions
     *        a QueryOptions object containing some query conditions
     * @return the list of results matching the criteria.
     * @throws SBonitaReadException
     *         in case of search error.
     */
    List<SExternalIdentityMapping> searchExternalIdentityMappings(String kind, String externalId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * @param mappingId
     * @param suffix
     *        the query suffix to specify (ForUser, ForGroup, ...)
     * @param messageSuffix
     * @return
     * @throws SExternalIdentityMappingNotFoundException
     */
    SExternalIdentityMapping getExternalIdentityMappingById(long mappingId, String suffix, String messageSuffix)
            throws SExternalIdentityMappingNotFoundException;

}
