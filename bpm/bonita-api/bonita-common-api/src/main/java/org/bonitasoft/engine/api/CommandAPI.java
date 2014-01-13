/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
 ** 
 * @since 6.0
 */
package org.bonitasoft.engine.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.command.CommandCriterion;
import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.command.CommandUpdater;
import org.bonitasoft.engine.command.DependencyNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.InvalidSessionException;

/**
 * Commands in the <b>BonitaBPM Execution Engine</b> are an extension point that allows to add / call behaviour that is not available by default through
 * provided APIs.
 * The CommandAPI gives access to command registration / unregistration to 'deploy' new commands.
 * The commands must be packed in jars and deployed /undeployed in the Engine as dependencies using methods {@link CommandAPI#addDependency(String, byte[])},
 * {@link #removeDependency(String)}
 * 
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 * @see CommandDescriptor
 * @see #register(String, String, String)
 * @see #unregister(long)
 * @see #addDependency(String, byte[])
 * @see #removeDependency(String)
 */
public interface CommandAPI {

    /**
     * Adds a dependency to the tenant scope.
     * 
     * @param name
     *            the dependency name
     * @param jar
     *            the JAR content
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws AlreadyExistsException
     *             if a dependency with the same name already exists
     * @throws CreationException
     *             occurs when any other dependency creation problem occurs
     * @since 6.0
     */
    void addDependency(String name, byte[] jar) throws AlreadyExistsException, CreationException;

    /**
     * Remove a dependency to the tenant scope.
     * 
     * @param name
     *            the dependency name.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DependencyNotFoundException
     *             occurs when the name does not refer to any existing dependency
     * @throws DeletionException
     *             occurs when an exception is thrown during dependency deletion
     * @since 6.0
     */
    void removeDependency(String name) throws DependencyNotFoundException, DeletionException;

    /**
     * Create a new command with its provided name, description, implementation.
     * 
     * @param name
     *            the command name
     * @param description
     *            the descriptor of the command
     * @param implementation
     *            the implementation class which will be uses when executing the command. This class is inside the jar.
     * @return the descriptor of the newly created command
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws AlreadyExistsException
     *             when a command with the same name already exists
     * @throws CreationException
     *             when the command registering cannot be fulfilled
     * @since 6.0
     */
    CommandDescriptor register(String name, String description, String implementation) throws AlreadyExistsException, CreationException;

    /**
     * Execute a command according to its name and a map of parameters.
     * 
     * @param name
     *            the command name
     * @param parameters
     *            the parameters
     * @return the result of the command execution.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *             occurs when the name does not refer to any existing command
     * @throws CommandParameterizationException
     *             when command parameters are not correct
     * @throws CommandExecutionException
     *             occurs when an exception is thrown during command execution
     * @since 6.0
     */
    Serializable execute(String name, Map<String, Serializable> parameters) throws CommandNotFoundException, CommandParameterizationException,
            CommandExecutionException;

    /**
     * Execute a command according to its id and a map of parameters.
     * 
     * @param commandId
     *            the command commandId
     * @param parameters
     *            the parameters
     * @return the result of the command execution.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *             occurs when the name does not refer to any existing command
     * @throws CommandParameterizationException
     *             when command parameters are not correct
     * @throws CommandExecutionException
     *             occurs when an exception is thrown during command execution
     * @since 6.0
     */
    Serializable execute(long commandId, Map<String, Serializable> parameters) throws CommandNotFoundException, CommandParameterizationException,
            CommandExecutionException;

    /**
     * Delete a command through its name.
     * 
     * @param name
     *            the command name
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *             occurs when the name does not refer to any existing command.
     * @throws DeletionException
     *             occurs when an exception is thrown during command deletion
     * @since 6.0
     */
    void unregister(String name) throws CommandNotFoundException, DeletionException;

    /**
     * Returns the command descriptor
     * 
     * @param name
     *            the command name
     * @return the descriptor of the command
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *             occurs when the command name does not refer to any existing command.
     * @since 6.0
     */
    CommandDescriptor getCommand(String name) throws CommandNotFoundException;

    /**
     * Returns the paginated list of command descriptors according to the sort criterion.
     * 
     * @param startIndex
     *            the list start index
     * @param maxResults
     *            the number of {@link CommandDescriptor} to retrieve
     * @param sort
     *            the sorting criterion
     * @return the paginated list of descriptors of the command
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.0
     */
    List<CommandDescriptor> getAllCommands(int startIndex, int maxResults, CommandCriterion sort);

    /**
     * Updates a command according to the update descriptor.
     * 
     * @param name
     *            the command name
     * @param updateDescriptor
     *            the update descriptor
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *             when the name does not refer to any existing command
     * @throws UpdateException
     *             when the update cannot be fulfilled correctly
     * @since 6.0
     */
    void update(String name, CommandUpdater updateDescriptor) throws CommandNotFoundException, UpdateException;

    /**
     * Delete all commands
     * 
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DeletionException
     *             occurs when an exception is thrown during command (unregistering) deletion
     * @since 6.0
     */
    void unregisterAll() throws DeletionException;

    /**
     * Returns the Commands with System is false
     * 
     * @param startIndex
     *            The starting index
     * @param maxResults
     *            The number of {@link CommandDescriptor}
     * @param sort
     *            The sorting criterion
     * @return The list of commands
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.0
     */
    List<CommandDescriptor> getUserCommands(final int startIndex, final int maxResults, final CommandCriterion sort);

    /**
     * Get the command descriptor by its id
     * 
     * @param commandId
     *            identifier of command
     * @return the descriptor of the command
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *             occurs when the command id does not refer to any existing command.
     * @since 6.0
     */
    CommandDescriptor get(long commandId) throws CommandNotFoundException;

    /**
     * Updates a command according to the update descriptor.
     * 
     * @param commandId
     *            identifier of command to indicate which command will be updated
     * @param updater
     *            the update descriptor
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *             occurs when the command id does not refer to any existing command
     * @throws UpdateException
     *             occurs when an exception is thrown during command update
     * @since 6.0
     */
    void update(long commandId, CommandUpdater updater) throws CommandNotFoundException, UpdateException;

    /**
     * Delete a command through its id.
     * 
     * @param commandId
     *            the identifier of command
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *             occurs when the name does not refer to any existing command.
     * @throws DeletionException
     *             occurs when an exception is thrown during command deletion
     * @since 6.0
     */
    void unregister(long commandId) throws CommandNotFoundException, DeletionException;

    /**
     * Search commands
     * 
     * @param searchOptions
     *            The criterion used during the search
     * @return A {@link SearchResult} containing the search result
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws SearchException
     *             when problem occurs during the commands search
     * @since 6.0
     */
    SearchResult<CommandDescriptor> searchCommands(SearchOptions searchOptions) throws SearchException;

}
