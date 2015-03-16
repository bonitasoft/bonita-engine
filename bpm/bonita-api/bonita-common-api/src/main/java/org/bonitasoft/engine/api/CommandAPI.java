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
 * A command is composed of a jar containing at least one class that implements <code>org.bonitasoft.engine.command.TenantCommand</code>.
 * <code>org.bonitasoft.engine.command.system.CommandWithParameters</code> can be used to handle parameter more easily. The behavior of the command must be
 * defined in the
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
 * {@link CommandAPI#removeDependency(String)} </p>
 * <pre>
 * Code example:<br>
 * 
 * In this example we deploy a command named "myCommandName". The class that implements <code>TenantCommand</code> is <code>org.bonitasoft.engine.command.IntegerCommand</code> and 
 * is contained in the jar we deploy using CommandAPI.addDependency.
 * <br>
 * <br>
 * {@code
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
 * @since 6.0.0
 * @version 6.4.1
 */
public interface CommandAPI {

    /**
     * Add a dependency to the tenant scope.
     *
     * @param name
     *        The name of the dependency.
     * @param jar
     *        The JAR content of the dependency.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws AlreadyExistsException
     *         If a dependency with the same name already exists
     * @throws CreationException
     *         If an other problem occurs
     * @since 6.0
     */
    void addDependency(String name, byte[] jar) throws AlreadyExistsException, CreationException;

    /**
     * Remove a dependency to the tenant scope.
     *
     * @param name
     *        The name of the dependency.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DependencyNotFoundException
     *         If the name does not refer to any existing dependency
     * @throws DeletionException
     *         If an other problem occurs
     * @since 6.0
     */
    void removeDependency(String name) throws DependencyNotFoundException, DeletionException;

    /**
     * Create a new command.
     *
     * @param name
     *        The name of the command
     * @param description
     *        The description of the command
     * @param implementation
     *        The name of the implementation class of the command. It will be used when executing the command. This class is inside the jar of a dependency.
     * @return The descriptor of the newly created command
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws AlreadyExistsException
     *         If a command with the same name already exists
     * @throws CreationException
     *         If an other problem occurs
     * @since 6.0
     */
    CommandDescriptor register(String name, String description, String implementation) throws AlreadyExistsException, CreationException;

    /**
     * Execute a command according to its name and a map of parameters.
     *
     * @param name
     *        The name of the command
     * @param parameters
     *        The parameters of the command
     * @return The result of the command execution.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *         If the name does not refer to any existing command
     * @throws CommandParameterizationException
     *         If a parameter of the command is not correct
     * @throws CommandExecutionException
     *         If an other problem occurs
     * @since 6.0
     */
    Serializable execute(String name, Map<String, Serializable> parameters) throws CommandNotFoundException, CommandParameterizationException,
            CommandExecutionException;

    /**
     * Execute a command according to its name and a map of parameters. During the execution of this method, the command's implementation will have to manage
     * itself its transactions.
     *
     * @param name
     *        The name of the command
     * @param parameters
     *        The parameters of the command
     * @return The result of the command execution.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *         If the name does not refer to any existing command
     * @throws CommandParameterizationException
     *         If a parameter of the command is not correct
     * @throws CommandExecutionException
     *         If an other problem occurs
     * @since 6.2
     */
    Serializable executeWithUserTransactions(String name, Map<String, Serializable> parameters) throws CommandNotFoundException,
            CommandParameterizationException, CommandExecutionException;

    /**
     * Execute a command according to its id and a map of parameters.
     *
     * @param commandId
     *        The identifier of the command
     * @param parameters
     *        The parameters of the command
     * @return The result of the command execution.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *         If the name does not refer to any existing command
     * @throws CommandParameterizationException
     *         If a parameter of the command is not correct
     * @throws CommandExecutionException
     *         If an other problem occurs
     * @since 6.0
     */
    Serializable execute(long commandId, Map<String, Serializable> parameters) throws CommandNotFoundException, CommandParameterizationException,
            CommandExecutionException;

    /**
     * Execute a command according to its id and a map of parameters. During the execution of this method, the command's implementation
     * will have to manage itself its transactions.
     *
     * @param commandId
     *        The identifier of the command
     * @param parameters
     *        The parameters of the command
     * @return The result of the command execution.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *         If the name does not refer to any existing command
     * @throws CommandParameterizationException
     *         If a parameter of the command is not correct
     * @throws CommandExecutionException
     *         If an other problem occurs
     * @since 6.2
     */
    Serializable executeWithUserTransactions(long commandId, Map<String, Serializable> parameters) throws CommandNotFoundException,
            CommandParameterizationException, CommandExecutionException;

    /**
     * Delete a command through its name.
     *
     * @param name
     *        The name of the command
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *         If the name does not refer to any existing command.
     * @throws DeletionException
     *         If an other problem occurs
     * @since 6.0
     */
    void unregister(String name) throws CommandNotFoundException, DeletionException;

    /**
     * Get the descriptor of the command.
     *
     * @param name
     *        The name of the command
     * @return The descriptor of the command
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *         If an other problem occurs
     * @since 6.0
     */
    CommandDescriptor getCommand(String name) throws CommandNotFoundException;

    /**
     * Get the paginated list of the descriptors of the command according to the sort criterion.
     *
     * @param startIndex
     *        The index of the first element to be retrieved (it starts from zero)
     * @param maxResults
     *        The number of {@link CommandDescriptor} to get.
     * @param sort
     *        The sorting criterion of the list.
     * @return The paginated list of descriptors of the command
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.0
     */
    List<CommandDescriptor> getAllCommands(int startIndex, int maxResults, CommandCriterion sort);

    /**
     * Update a command according to the update descriptor.
     *
     * @param name
     *        The name of the command
     * @param updateDescriptor
     *        The update descriptor (containing the fields to update &amp; their new value).
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *         If the name does not refer to any existing command
     * @throws UpdateException
     *         If an other problem occurs.
     * @since 6.0
     */
    void update(String name, CommandUpdater updateDescriptor) throws CommandNotFoundException, UpdateException;

    /**
     * Delete all commands.
     *
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DeletionException
     *         If an other problem occurs.
     * @since 6.0
     */
    void unregisterAll() throws DeletionException;

    /**
     * Get the list of the descriptor of the user commands (no system command).
     *
     * @param startIndex
     *        The index of the first element to be retrieved (it starts from zero)
     * @param maxResults
     *        The number of {@link CommandDescriptor} to get.
     * @param sort
     *        The sorting criterion of the list.
     * @return The paginated list of descriptors of the command
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.0
     */
    List<CommandDescriptor> getUserCommands(final int startIndex, final int maxResults, final CommandCriterion sort);

    /**
     * Get the descriptor of the command by its identifier.
     *
     * @param commandId
     *        The identifier of command
     * @return The descriptor of the command
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *         If the identifier does not refer to any existing command.
     * @since 6.0
     */
    CommandDescriptor get(long commandId) throws CommandNotFoundException;

    /**
     * Update a command according to the update descriptor.
     *
     * @param commandId
     *        The identifier of command to update.
     * @param updateDescriptor
     *        The update descriptor
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *         If the identifier does not refer to any existing command.
     * @throws UpdateException
     *         If an other problem occurs.
     * @since 6.0
     */
    void update(long commandId, CommandUpdater updateDescriptor) throws CommandNotFoundException, UpdateException;

    /**
     * Delete a command through its id.
     *
     * @param commandId
     *        The identifier of command
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommandNotFoundException
     *         If the name does not refer to any existing command.
     * @throws DeletionException
     *         If an other problem occurs.
     * @since 6.0
     */
    void unregister(long commandId) throws CommandNotFoundException, DeletionException;

    /**
     * Search commands corresponding to the criteria.
     *
     * @param searchOptions
     *        The criterion used during the search
     * @return A {@link SearchResult} containing the descriptor of the commands.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws SearchException
     *         If an other problem occurs.
     * @since 6.0
     */
    SearchResult<CommandDescriptor> searchCommands(SearchOptions searchOptions) throws SearchException;

}
