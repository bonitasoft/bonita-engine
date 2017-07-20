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
package org.bonitasoft.engine.platform.command;

import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.platform.command.model.SPlatformCommand;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Zhang Bole
 * @author Emmanuel Duchastenier
 */
public interface PlatformCommandService {

    /**
     * Create a sPlatformCommand
     * 
     * @param command
     * @throws SPlatformCommandAlreadyExistsException
     *             occurs when the sPlatformCommand has already been taken
     * @throws SPlatformCommandCreationException
     *             occurs when an exception is thrown during sPlatformCommand creation
     * @throws SPlatformCommandGettingException
     *             occurs when an exception is thrown during getting sPlatformCommand
     * @since 6.0
     */
    void create(SPlatformCommand command) throws SPlatformCommandAlreadyExistsException, SPlatformCommandCreationException, SPlatformCommandGettingException;

    /**
     * Delete a sPlatformCommand from its name
     * 
     * @param name
     *            the platform name
     * @throws SPlatformCommandNotFoundException
     *             occurs when the identifier does not refer to an existing sPlatformCommand
     * @throws SPlatformCommandDeletionException
     *             occurs when an exception is thrown during sPlatformCommand creation
     * @throws SPlatformCommandGettingException
     *             occurs when an exception is thrown during getting sPlatformCommand
     * @since 6.0
     */
    void delete(String name) throws SPlatformCommandNotFoundException, SPlatformCommandDeletionException, SPlatformCommandGettingException;

    /**
     * Delete all sPlatformCommands
     * 
     * @throws SPlatformCommandDeletionException
     *             occurs when an exception is thrown during sPlatformCommand creation
     * @since 6.0
     */
    void deleteAll() throws SPlatformCommandDeletionException;

    /**
     * Get sPlatformCommand by its name
     * 
     * @param name
     * @return an entity of sPlatformCommand
     * @throws SPlatformCommandNotFoundException
     *             occurs when the identifier does not refer to an existing sPlatformCommand
     * @throws SPlatformCommandGettingException
     *             occurs when an exception is thrown during getting sPlatformCommand
     * @since 6.0
     */
    SPlatformCommand getPlatformCommand(String name) throws SPlatformCommandNotFoundException, SPlatformCommandGettingException;

    /**
     * Get the sPlatformCommand having the given value for the given int index
     * 
     * @param queryOptions
     *            criteria
     * @return a list of sPlatformCommand
     * @throws SPlatformCommandGettingException
     *             occurs when an exception is thrown during getting sPlatformCommand
     * @since 6.0
     */
    List<SPlatformCommand> getPlatformCommands(QueryOptions queryOptions) throws SPlatformCommandGettingException;

    /**
     * Update a sPlatformCommand with given sPlatformCommand and new content.
     * 
     * @param command
     * @param updateDescriptor
     * @throws SPlatformCommandNotFoundException
     *             occurs when the identifier does not refer to an existing sPlatformCommand
     * @throws SPlatformCommandUpdateException
     *             occurs when an exception is thrown during sPlatformCommand update
     * @since 6.0
     */
    void update(SPlatformCommand command, EntityUpdateDescriptor updateDescriptor) throws SPlatformCommandNotFoundException, SPlatformCommandUpdateException;

}
