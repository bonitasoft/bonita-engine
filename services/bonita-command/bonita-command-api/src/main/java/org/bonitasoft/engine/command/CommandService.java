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
package org.bonitasoft.engine.command;

import java.util.List;

import org.bonitasoft.engine.command.model.SCommand;
import org.bonitasoft.engine.command.model.SCommandCriterion;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Matthieu Chaffotte
 * @since 6.0
 */
public interface CommandService extends TenantLifecycleService {

    String COMMAND = "COMMAND";

    /**
     * Create command by given command
     * 
     * @param command
     *        command without Id
     * @throws SCommandAlreadyExistsException
     *         Error thrown when relative command already exists
     * @throws SCommandCreationException
     *         Error thrown if has exceptions during the creating command.
     */
    void create(SCommand command) throws SCommandAlreadyExistsException, SCommandCreationException;

    /**
     * Delete command by given command name
     * 
     * @param name
     *        Name of command which will be deleted
     * @throws SCommandNotFoundException
     *         Error thrown if no command have name corresponding to the parameter.
     * @throws SCommandDeletionException
     *         Error thrown if has exception during the deleting command.
     */
    void delete(String name) throws SCommandNotFoundException, SCommandDeletionException;

    /**
     * Delete all commands
     * 
     * @throws SCommandDeletionException
     *         Error thrown if has exception during the deleting command.
     */
    void deleteAll() throws SCommandDeletionException;

    /**
     * Get command by given name
     * 
     * @param name
     *        Name of command
     * @return a command object
     * @throws SCommandNotFoundException
     *         Error thrown if no command have name corresponding to the parameter.
     */
    SCommand get(String name) throws SCommandNotFoundException;

    /**
     * Retrieves a paginated list of commands, The returned list is paginated
     * 
     * @param startIndex
     *        Start index of command record
     * @param maxResults
     *        Number of commands we want to get. Maximum number of commands returned.
     * @param sort
     *        The criterion used to sort the retried commands
     * @return a list of command objects
     * @throws SCommandGettingException
     *         Error thrown if has exception during the command getting.
     */
    List<SCommand> getAllCommands(int startIndex, int maxResults, SCommandCriterion sort) throws SCommandGettingException;

    /**
     * Update the command by its id
     * 
     * @param command
     *        The command will be updated
     * @param updateDescriptor
     *        The description for update command
     * @throws SCommandNotFoundException
     *         Error thrown if no command have name corresponding to the parameter.
     * @throws SCommandUpdateException
     *         Error thrown if has exception during the command updating.
     */
    void update(SCommand command, EntityUpdateDescriptor updateDescriptor) throws SCommandNotFoundException, SCommandUpdateException;

    /**
     * Retrieves a paginated list of commands with System is false
     * 
     * @param startIndex
     *        Start index of command record
     * @param maxResults
     *        Number of commands we want to get. Maximum number of commands returned.
     * @param sCommandCriterion
     *        The criterion used to sort the retried commands
     * @return A list of command objects
     * @throws SCommandGettingException
     *         Error thrown if has exception during the command getting.
     * @since 6.0
     */
    List<SCommand> getUserCommands(int startIndex, int maxResults, SCommandCriterion sCommandCriterion) throws SCommandGettingException;

    /**
     * Get command by given id
     * 
     * @param commandId
     *        identifier of command
     * @return a command object
     * @throws SCommandNotFoundException
     *         Error thrown if no command have name corresponding to the parameter.
     * @author Yanyan Liu
     */
    SCommand get(final long commandId) throws SCommandNotFoundException;

    /**
     * Delete command by given command id
     * 
     * @param commandId
     *        identifier of command which will be deleted
     * @throws SCommandNotFoundException
     *         Error thrown if no command have name corresponding to the parameter.
     * @throws SCommandDeletionException
     *         Error thrown if has exception during the deleting command.
     * @author Yanyan Liu
     */
    void delete(long commandId) throws SCommandNotFoundException, SCommandDeletionException;

    /**
     * Get total number of commands according to the specific criteria
     * 
     * @param options
     *        search criteria
     * @return total number of commands corresponding to the specific criteria
     * @throws SBonitaReadException
     * @author Yanyan Liu
     */
    long getNumberOfCommands(QueryOptions options) throws SBonitaReadException;

    /**
     * Get a list of commands according to the specific criteria
     * 
     * @param options
     *        search criteria
     * @return a list of command objects
     * @throws SBonitaReadException
     * @author Yanyan Liu
     */
    List<SCommand> searchCommands(QueryOptions options) throws SBonitaReadException;

}
