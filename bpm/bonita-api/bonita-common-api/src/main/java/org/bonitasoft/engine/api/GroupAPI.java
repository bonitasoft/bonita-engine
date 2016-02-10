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
import java.util.Map;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.GroupCreator;
import org.bonitasoft.engine.identity.GroupCriterion;
import org.bonitasoft.engine.identity.GroupNotFoundException;
import org.bonitasoft.engine.identity.GroupUpdater;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

/**
 * GroupAPI forms part of the {@link OrganizationAPI} and gives access to all the Administration operations available on Groups: creation, deletion, updating,
 * search, etc...
 * 
 * @author Zhao Na
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 * @author Hongwen Zang
 * @see Group
 */
public interface GroupAPI {

    /**
     * Creates a group from its name and parent path.
     * <p>
     * If the group is a top level one, the parent path must be null.
     * </p>
     * 
     * @param name
     *            the name of the group
     * @param parentPath
     *            the parent path of the group (null means no parent)
     * @return the created group
     * @throws AlreadyExistsException
     *             If the couple name/parentPath is already taken by an existing group
     * @throws CreationException
     *             If an exception occurs during the group creation
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    Group createGroup(String name, String parentPath) throws AlreadyExistsException, CreationException;

    /**
     * Creates a group.
     * <p>
     * It takes the values of the creator in order to create a group.
     * </p>
     * 
     * @param creator
     *            the group creator
     * @return the created group
     * @throws AlreadyExistsException
     *             If the couple name/parentPath is already taken by an existing group
     * @throws CreationException
     *             If an exception occurs during group creation
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    Group createGroup(GroupCreator creator) throws AlreadyExistsException, CreationException;

    /**
     * Updates the group according to the updater values.
     * 
     * @param groupId
     *            the identifier of the group
     * @param updater
     *            the group updater
     * @return the updated group
     * @throws GroupNotFoundException
     *             If the group identifier does not refer to an existing group
     * @throws UpdateException
     *             If an exception occurs during the group update
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    Group updateGroup(long groupId, GroupUpdater updater) throws GroupNotFoundException, UpdateException;

    /**
     * Deletes the group.
     * 
     * @param groupId
     *            the identifier of the group
     * @throws DeletionException
     *             If an exception occurs during the group deletion
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    void deleteGroup(long groupId) throws DeletionException;

    /**
     * Deletes the groups.
     * 
     * @param groupIds
     *            the list of group identifiers
     * @throws DeletionException
     *             If an exception occurs during the group deletion
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    void deleteGroups(List<Long> groupIds) throws DeletionException;

    /**
     * Retrieves the group.
     * 
     * @param groupId
     *            the group identifier
     * @return the group
     * @throws GroupNotFoundException
     *             If the group identifier does not refer to an existing group
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs during the group retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    Group getGroup(long groupId) throws GroupNotFoundException;

    /**
     * Retrieves the group according to its full path.
     * 
     * @param groupPath
     *            the full path of the group (parentPath/name)
     * @return the group
     * @throws GroupNotFoundException
     *             If the group path does not refer to an existing group
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs during the group retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    Group getGroupByPath(final String groupPath) throws GroupNotFoundException;

    /**
     * Returns the total number of groups.
     * 
     * @return the total number of groups
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs during the count retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    long getNumberOfGroups();

    /**
     * Retrieves the paginated list of groups.
     * <p>
     * It retrieves from the startIndex to the startIndex + maxResults.
     * </p>
     * @param startIndex
     *            the start index
     * @param maxResults
     *            the max number of groups
     * @param criterion
     *            the sorting criterion
     * @return the list of groups
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs during the group retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    List<Group> getGroups(int startIndex, int maxResults, GroupCriterion criterion);

    /**
     * Retrieves the groups.
     * <p>
     * The map contains the couples groupId/Group.
     * If a group does not exists, no exception is thrown and no value is added in the map.
     * </p>
     * 
     * @param groupIds
     *            the identifiers of the groups
     * @return the groups
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs during the group retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    Map<Long, Group> getGroups(List<Long> groupIds);

    /**
     * Searches groups according to the criteria containing in the options.
     * 
     * @param options
     *            the search criteria
     * @return the search result
     * @throws SearchException
     *             If an exception occurs during the group searching
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    SearchResult<Group> searchGroups(SearchOptions options) throws SearchException;

}
