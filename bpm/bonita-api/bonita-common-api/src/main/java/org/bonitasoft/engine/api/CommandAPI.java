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

/**
 * Manipulate tenant commands. A command can be registered, unregistered, and executed with parameters.
 * <p>
 * Commands are used to extend engine behavior, and are classes that are called from this API and executed on the server side. <br>
 * in the execute method of this class.
 * </p>
 * <p>
 * A command is composed of a jar containing at least one class that implements org.bonitasoft.engine.command.TenantCommand.
 * org.bonitasoft.engine.command.system.CommandWithParameters can be used to handle parameter more easily. The behavior of the command must be defined in the
 * execute method of this class.<br>
 * TenantCommand is a class available only in bonita-server.jar. In order to create the jar you will need to have a dependency on that jar.
 * <p>
 * The jar containing the command class must be added to the engine using the {@link CommandAPI#addDependency} method with a name to identify the dependency so
 * that it can be removed later.<br>
 * Then the command must be registered using {@link CommandAPI#register(String, String, String)} with a name to identify it and an implementation that is the
 * fully qualified name of the command class.<br>
 * After registration, the command can be executed using {@link CommandAPI#execute(long, Map)} with the id returned by the register method or
 * {@link CommandAPI#execute(String, Map)} with the name of the command and with a map of parameters required by the command.<br>
 * Finally the command can be removed using both {@link CommandAPI#unregister(long)} or {@link CommandAPI#unregister(String)} and
 * {@link CommandAPI#removeDependency(String)}
 * </p>
 * 
 * <pre>
 * Code example:<br>
 * 
 * In this example we deploy a command named "myCommandName". The class that implements TenantCommand is org.bonitasoft.engine.command.IntegerCommand and 
 * is contained in the jar we deploy using CommandAPI.addDependency.
 *  
 * {@code
 *  
 * byte[] byteArray = /* read the jar containing the command as a byte array * /
 * 
 *  //deploy
 * getCommandAPI().addDependency("myCommandDependency", byteArray);
 * getCommandAPI().register("myCommandName", "Retrieving the integer value", "org.bonitasoft.engine.command.IntegerCommand");
 * 
 *  //execute
 * final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
 * parameters.put("aParamterName", "aParameterValue");
 * parameters.put("anIntParameter", 42);
 * Integer theResultOfTheCommandExecution = (Integer) getCommandAPI().execute("myCommandName", parameters);
 * 
 *  //undeploy
 * getCommandAPI().unregister("myCommandName");
 * getCommandAPI().removeDependency("myCommandDependency");
 * }
 * </pre>
 * 
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 * @author Baptiste Mesta
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
     * @throws org.bonitasoft.engine.session.InvalidSessionException
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
     * @throws org.bonitasoft.engine.session.InvalidSessionException
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
     * @throws org.bonitasoft.engine.session.InvalidSessionException
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
     * @throws org.bonitasoft.engine.session.InvalidSessionException
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
     * Execute a command according to its name and a map of parameters. During the execution of this method, the command's implementation
     * will have to manage itself its transactions.
     * 
     * @param name
     *            the command name
     * @param parameters
     *            the parameters
     * @return the result of the command execution.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *             occurs when the name does not refer to any existing command
     * @throws CommandParameterizationException
     *             when command parameters are not correct
     * @throws CommandExecutionException
     *             occurs when an exception is thrown during command execution
     * @since 6.2
     */
    Serializable executeWithUserTransactions(String name, Map<String, Serializable> parameters) throws CommandNotFoundException,
            CommandParameterizationException,
            CommandExecutionException;

    /**
     * Execute a command according to its id and a map of parameters.
     * 
     * @param commandId
     *            the command commandId
     * @param parameters
     *            the parameters
     * @return the result of the command execution.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
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
     * Execute a command according to its id and a map of parameters. During the execution of this method, the command's implementation
     * will have to manage itself its transactions.
     * 
     * @param commandId
     *            the command commandId
     * @param parameters
     *            the parameters
     * @return the result of the command execution.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *             occurs when the name does not refer to any existing command
     * @throws CommandParameterizationException
     *             when command parameters are not correct
     * @throws CommandExecutionException
     *             occurs when an exception is thrown during command execution
     * @since 6.2
     */
    Serializable executeWithUserTransactions(long commandId, Map<String, Serializable> parameters) throws CommandNotFoundException,
            CommandParameterizationException,
            CommandExecutionException;

    /**
     * Delete a command through its name.
     * 
     * @param name
     *            the command name
     * @throws org.bonitasoft.engine.session.InvalidSessionException
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
     * @throws org.bonitasoft.engine.session.InvalidSessionException
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
     * @throws org.bonitasoft.engine.session.InvalidSessionException
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
     * @throws org.bonitasoft.engine.session.InvalidSessionException
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
     * @throws org.bonitasoft.engine.session.InvalidSessionException
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
     * @throws org.bonitasoft.engine.session.InvalidSessionException
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
     * @throws org.bonitasoft.engine.session.InvalidSessionException
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
     * @param updateDescriptor
     *            the update descriptor
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *             occurs when the command id does not refer to any existing command
     * @throws UpdateException
     *             occurs when an exception is thrown during command update
     * @since 6.0
     */
    void update(long commandId, CommandUpdater updateDescriptor) throws CommandNotFoundException, UpdateException;

    /**
     * Delete a command through its id.
     * 
     * @param commandId
     *            the identifier of command
     * @throws org.bonitasoft.engine.session.InvalidSessionException
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
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws SearchException
     *             when problem occurs during the commands search
     * @since 6.0
     */
    SearchResult<CommandDescriptor> searchCommands(SearchOptions searchOptions) throws SearchException;

}
