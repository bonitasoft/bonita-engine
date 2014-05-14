/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.api;

import java.util.List;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.CustomUserInfo;
import org.bonitasoft.engine.identity.CustomUserInfoDefinition;
import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator;
import org.bonitasoft.engine.identity.CustomUserInfoValue;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

/**
 * CustomUserInfoAPI forms part of the {@link OrganizationAPI} and gives access to all the Administration operations available on custom user info: creation,
 * deletion and retrieve methods
 *
 * @author Vincent Elcrin
 */
public interface CustomUserInfoAPI {

    /**
     * Create a new CustomUserInfoDefinition that will be available for all {@link User}s in the organization.
     *
     * @param creator
     *            describes all information for the new object
     * @return the created CustomUserInfoDefinition
     * @throws AlreadyExistsException
     *             if a CustomUserInfoDefinition already exists with the same name.
     * @throws CreationException
     *             if an error occurs during the creation
     * @since 6.3
     */
    CustomUserInfoDefinition createCustomUserInfoDefinition(CustomUserInfoDefinitionCreator creator) throws AlreadyExistsException, CreationException;

    /**
     * Retrieves the list of {@link CustomUserInfoDefinition} according to the given pagination criteria, ordered by name.
     *
     * @param startIndex
     *            the index for the first element to be retrieved (starts from zero)
     * @param maxResult
     *            the maximum number of elements to be retrieved.
     * @return the list of {@link CustomUserInfoDefinition} according to the given pagination criteria, ordered by name.
     * @since 6.3
     */
    List<CustomUserInfoDefinition> getCustomUserInfoDefinitions(int startIndex, int maxResult);

    /**
     * Count the number of existing definitions.
     *
     * @return the number of existing definitions.
     * @since 6.3
     */
    long getNumberOfCustomInfoDefinitions();

    /**
     * Delete the {@link CustomUserInfoDefinition} related to the given id. All {@link CustomUserInfoValue} related to this {@link CustomUserInfoDefinition}
     * will be deleted as well.
     *
     * @param id
     *            the identifier of the {@link org.bonitasoft.engine.identity.CustomUserInfoDefinition}
     * @return
     * @throws DeletionException
     *             if an error occurs during deletion
     * @since 6.3
     */
    void deleteCustomUserInfoDefinition(long id) throws DeletionException;

    /**
     * Retrieve the list of {@link CustomUserInfo} for the given user, ordered by custom user info definition name. For {@link CustomUserInfo}s which have
     * {@link CustomUserInfoDefinition} without a related {@link CustomUserInfoValue}, the field value will be null.
     *
     * @param userId
     *            the user identifier
     * @param startIndex
     *            the index of the first result to be retrieved (it starts from zero)
     * @param maxResult
     *            the maximum elements to be retrieved.
     * @return The list of {@link CustomUserInfo} for the given user, ordered by custom user info definition name.
     * @see CustomUserInfoDefinition
     * @see CustomUserInfoValue
     * @since 6.3
     */
    List<CustomUserInfo> getCustomUserInfo(long userId, int startIndex, int maxResult);

    /**
     * Searches custom user info values according to the criteria containing in the options.
     *
     * @param options
     *            The search criteria
     * @return The search result
     * @since 6.3
     */
    SearchResult<CustomUserInfoValue> searchCustomUserInfoValues(SearchOptions options);

    /**
     * Set value of a custom user info described by definitionId/userId.
     *
     * @param definitionId
     *            the {@link CustomUserInfoDefinition} identifier
     * @param userId
     *            the {@link User} identifier
     * @param value
     *            the new custom user info value
     * @return custom user info value
     * @since 6.3
     */
    CustomUserInfoValue setCustomUserInfoValue(long definitionId, long userId, String value) throws UpdateException;

}
