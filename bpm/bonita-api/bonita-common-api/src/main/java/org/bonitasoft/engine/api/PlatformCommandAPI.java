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
import org.bonitasoft.engine.exception.UpdateException;

/**
 * Manipulates a platform command. A command can be registered, unregistered and executed with parameters.<br>
 * These commands are executed in a platform scope, see {@link CommandAPI} for an explanation of how to deploy, execute, ... a command. The only
 * difference between the {@link CommandAPI} and the {@link PlatformCommandAPI} is that a platform command must extend
 * {@code org.bonitasoft.engine.command.PlatformCommand}.
 *
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @see CommandDescriptor
 */
public interface PlatformCommandAPI {

    /**
     * Adds a dependency.
     *
     * @param name
     *        the dependency name.
     * @param jar
     *        the JAR content
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         occurs when the session is not valid
     * @throws AlreadyExistsException
     *         occurs when the dependency name is already used for another dependency
     * @throws CreationException
     *         if a problem occurs when creating the dependency
     */
    void addDependency(String name, byte[] jar) throws AlreadyExistsException, CreationException;

    /**
     * Removes the dependency.
     *
     * @param name
     *        the dependency name.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         if the current platform session is not valid
     * @throws DependencyNotFoundException
     *         if no dependency can be found with the specified name
     * @throws DeletionException
     *         if a problem occurs when deleting the dependency
     */
    void removeDependency(String name) throws DependencyNotFoundException, DeletionException;

    /**
     * Adds a command and its descriptor.
     *
     * @param name
     *        the command name
     * @param description
     *        the command description
     * @param implementation
     *        the implementation class which will be used when executing the command
     * @return the descriptor of the command
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         occurs when the session is not valid
     * @throws AlreadyExistsException
     *         occurs when the command name is already used for another command
     * @throws CreationException
     *         if a problem occurs when registering the command
     */
    CommandDescriptor register(String name, String description, String implementation) throws AlreadyExistsException, CreationException;

    /**
     * Executes a command according to its name and the list of parameters.
     *
     * @param name
     *        the command name
     * @param parameters
     *        the parameters (specific to the command to execute)
     * @return the result of the command execution
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         occurs when the session is not valid
     * @throws CommandNotFoundException
     *         occurs when the name does not refer to any existing command
     * @throws CommandParameterizationException
     *         when command parameters are not correct
     * @throws CommandExecutionException
     *         occurs when an exception is thrown during command execution
     */
    Serializable execute(String name, Map<String, Serializable> parameters) throws CommandNotFoundException, CommandParameterizationException,
            CommandExecutionException;

    /**
     * Deletes a command and its descriptor.
     *
     * @param name
     *        the command name
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         occurs when the session is not valid
     * @throws CommandNotFoundException
     *         occurs when the name does not refer to an existing command
     * @throws DeletionException
     *         occurs when an exception is thrown during command deletion
     */
    void unregister(String name) throws CommandNotFoundException, DeletionException;

    /**
     * Returns the command descriptor.
     *
     * @param name
     *        the command name
     * @return the descriptor of the command
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         occurs when the session is not valid
     * @throws CommandNotFoundException
     *         occurs when the command name does not refer to an existing command.
     * @deprecated As of release 6.2.1, replaced by {@link #getCommand(String)} that does not throw CreationException.
     */
    @Deprecated
    CommandDescriptor get(String name) throws CommandNotFoundException, CreationException;

    /**
     * Returns the command descriptor corresponding to the given command name.
     *
     * @param commandName
     *        the name of the command
     * @return the descriptor of the command
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         occurs when the session is not valid
     * @throws CommandNotFoundException
     *         occurs when the command name does not refer to an existing command.
     * @since 6.2.1
     */
    CommandDescriptor getCommand(String commandName) throws CommandNotFoundException;

    /**
     * Returns the paginated list of command descriptors according to the sort criterion.
     *
     * @param startIndex
     *        the start index
     * @param maxResults
     *        the number of {@link CommandDescriptor} to retrieve
     * @param sort
     *        the sorting criterion
     * @return the paginated list of command descriptors
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         occurs when the session is not valid
     */
    List<CommandDescriptor> getCommands(int startIndex, int maxResults, CommandCriterion sort);

    /**
     * Updates a command according to the update descriptor.
     *
     * @param name
     *        the command name
     * @param updater
     *        the update descriptor
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         if the session is not valid
     * @throws CommandNotFoundException
     *         occurs when the name does not refer to any existing command
     * @throws UpdateException
     *         occurs when an exception is thrown during command update
     */
    void update(String name, CommandUpdater updater) throws CommandNotFoundException, UpdateException;

}
