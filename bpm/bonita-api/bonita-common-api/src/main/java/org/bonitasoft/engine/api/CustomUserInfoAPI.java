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
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

/**
 * CustomUserInfoAPI forms part of the {@link OrganizationAPI} and gives access to all the Administration operations available on
 * {@link org.bonitasoft.engine.identity.CustomUserInfoDefinition} and {@link org.bonitasoft.engine.identity.CustomUserInfoValue}: creation,
 * deletion and retrieve methods
 *
 * @author Vincent Elcrin
 * @see org.bonitasoft.engine.api.OrganizationAPI
 * @see org.bonitasoft.engine.identity.CustomUserInfoDefinition
 * @see org.bonitasoft.engine.identity.CustomUserInfoValue
 * @since 6.3
 */
public interface CustomUserInfoAPI {

    /**
     * Creates a new {@link org.bonitasoft.engine.identity.CustomUserInfoDefinition} that will be available for all {@link org.bonitasoft.engine.identity.User}
     * s in the organization. In order to set a value for the new created {@code CustomUserInfoDefinition} for a specif
     * {@link org.bonitasoft.engine.identity.User} use the method {@link #setCustomUserInfoValue(long, long, String)}.
     * <p></p>
     * <p>Example:</p>
     * <pre>
     * CustomUserInfoDefinitionCreator creator = new CustomUserInfoDefinitionCreator("Skills", "The user skills");
     * CustomUserInfoDefinition userInfoDef = identityAPI.createCustomUserInfoDefinition(creator);
     * </pre>
     * <p></p>
     *
     * @param creator
     *        A {@link org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator} describing all information about the {@code CustomUserInfoDefinition} to
     *        be created
     * @return The created {@code CustomUserInfoDefinition}
     * @throws AlreadyExistsException
     *         If a {@code CustomUserInfoDefinition} already exists with the same name.
     * @throws CreationException
     *         If an error occurs during the creation
     * @see org.bonitasoft.engine.identity.CustomUserInfoDefinition
     * @see org.bonitasoft.engine.identity.User
     * @see org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator
     * @since 6.3
     */
    CustomUserInfoDefinition createCustomUserInfoDefinition(CustomUserInfoDefinitionCreator creator) throws AlreadyExistsException, CreationException;

    /**
     * Retrieves the list of {@link CustomUserInfoDefinition} according to the given pagination criteria, ordered by name.
     *
     * @param startIndex
     *        The index for the first element to be retrieved (starts from zero)
     * @param maxResult
     *        The maximum number of elements to be retrieved.
     * @return The list of {@code CustomUserInfoDefinition} according to the given pagination criteria, ordered by name.
     * @since 6.3
     * @see org.bonitasoft.engine.identity.CustomUserInfoDefinition
     */

    List<CustomUserInfoDefinition> getCustomUserInfoDefinitions(int startIndex, int maxResult);

    /**
     * Retrieves the number of existing {@link org.bonitasoft.engine.identity.CustomUserInfoDefinition}s.
     *
     * @return The number of existing {@code CustomUserInfoDefinition}s.
     * @since 6.3
     * @see org.bonitasoft.engine.identity.CustomUserInfoDefinition
     */
    long getNumberOfCustomInfoDefinitions();

    /**
     * Deletes the {@link CustomUserInfoDefinition} identified by the given id. All {@link CustomUserInfoValue} related to this {@code CustomUserInfoDefinition}
     * will be deleted as well.
     *
     * @param id
     *        The identifier of the {@code CustomUserInfoDefinition}
     * @throws DeletionException
     *         If an error occurs during deletion
     * @since 6.3
     * @see org.bonitasoft.engine.identity.CustomUserInfoDefinition
     * @see org.bonitasoft.engine.identity.CustomUserInfoValue
     */
    void deleteCustomUserInfoDefinition(long id) throws DeletionException;

    /**
     * Retrieves the list of {@link CustomUserInfo} for the given user, ordered by {@link org.bonitasoft.engine.identity.CustomUserInfoDefinition} name. For
     * {@code CustomUserInfo}s which have {@code CustomUserInfoDefinition} without a related {@link CustomUserInfoValue}, the field value will be null.
     *
     * @param userId
     *        The identifier of the {@link org.bonitasoft.engine.identity.User}
     * @param startIndex
     *        The index of the first element to be retrieved (it starts from zero)
     * @param maxResult
     *        The maximum elements to be retrieved.
     * @return The list of {@link CustomUserInfo} for the given {@code User}, ordered by {@code CustomUserInfoDefinition} name.
     * @see CustomUserInfoDefinition
     * @see CustomUserInfoValue
     * @see org.bonitasoft.engine.identity.CustomUserInfo
     * @see org.bonitasoft.engine.identity.User
     * @since 6.3
     */
    List<CustomUserInfo> getCustomUserInfo(long userId, int startIndex, int maxResult);

    /**
     * Searches {@link org.bonitasoft.engine.identity.CustomUserInfoValue}s according to the criteria contained in the given
     * {@link org.bonitasoft.engine.search.SearchOptions}. In order to know which fields can be used in filters and sorting, please refer to
     * {@link org.bonitasoft.engine.identity.CustomUserInfoValueSearchDescriptor}.
     * <p></p>
     * Example: searches the first 10 {@code CustomUserInfoValue}s having the given {@code CustomUserInfoDefinition} (referenced by its identifier) with the
     * given value. The result is ordered by the related {@code User} identifier:
     * <pre>
     * SearchOptionsBuilder optionsBuilder = new SearchOptionsBuilder(0, 10);
     * optionsBuilder.filter(CustomUserInfoValueSearchDescriptor.DEFINITION_ID, userInfoDefinition.getId());
     * optionsBuilder.filter(CustomUserInfoValueSearchDescriptor.VALUE, value);
     * optionsBuilder.sort(CustomUserInfoValueSearchDescriptor.USER_ID, Order.ASC);
     * SearchResult&lt;CustomUserInfoValue&gt; searchResult = identityAPI.searchCustomUserInfoValues(optionsBuilder.done());
     * </pre>
     *
     * @param options
     *        The {@link org.bonitasoft.engine.search.SearchOptions} containing the search criteria
     * @return The {@link org.bonitasoft.engine.search.SearchResult} containing the number and the list of {@code CustomUserInfoValue}s matching the criteria
     * @since 6.3
     * @see org.bonitasoft.engine.search.SearchOptions
     * @see org.bonitasoft.engine.identity.CustomUserInfoValue
     * @see org.bonitasoft.engine.identity.CustomUserInfoValueSearchDescriptor
     * @see org.bonitasoft.engine.search.SearchResult
     */
    SearchResult<CustomUserInfoValue> searchCustomUserInfoValues(SearchOptions options);

    /**
     * Defines the value of a {@link org.bonitasoft.engine.identity.CustomUserInfoDefinition} for a given {@link org.bonitasoft.engine.identity.User}.
     *
     * @param definitionId
     *        The identifier of the {@code CustomUserInfoDefinition}
     * @param userId
     *        The identifier of the {@code User}
     * @param value
     *        The {@code Custom User Information} value
     * @return a {@link org.bonitasoft.engine.identity.CustomUserInfoValue} representing the value of the given {@code CustomUserInfoDefinition} for the given
     *         {@code User}
     * @throws org.bonitasoft.engine.exception.UpdateException
     *         When an error occurs during the update.
     * @since 6.3
     * @see org.bonitasoft.engine.identity.CustomUserInfoDefinition#getId()
     * @see org.bonitasoft.engine.identity.User#getId()
     * @see org.bonitasoft.engine.identity.CustomUserInfoValue
     */
    CustomUserInfoValue setCustomUserInfoValue(long definitionId, long userId, String value) throws UpdateException;

}
