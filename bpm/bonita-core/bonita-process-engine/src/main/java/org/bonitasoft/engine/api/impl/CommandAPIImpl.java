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
package org.bonitasoft.engine.api.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.api.impl.transaction.command.DeleteSCommand;
import org.bonitasoft.engine.api.impl.transaction.command.GetCommands;
import org.bonitasoft.engine.api.impl.transaction.dependency.AddSDependency;
import org.bonitasoft.engine.api.impl.transaction.dependency.DeleteSDependency;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.command.CommandCriterion;
import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.command.CommandService;
import org.bonitasoft.engine.command.CommandUpdater;
import org.bonitasoft.engine.command.CommandUpdater.CommandField;
import org.bonitasoft.engine.command.DependencyNotFoundException;
import org.bonitasoft.engine.command.SCommandDeletionException;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandNotFoundException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.SCommandUpdateException;
import org.bonitasoft.engine.command.TenantCommand;
import org.bonitasoft.engine.command.model.SCommand;
import org.bonitasoft.engine.command.model.SCommandBuilderFactory;
import org.bonitasoft.engine.command.model.SCommandCriterion;
import org.bonitasoft.engine.command.model.SCommandUpdateBuilder;
import org.bonitasoft.engine.command.model.SCommandUpdateBuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyAlreadyExistsException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.search.SearchCommands;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;

/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 * @author Laurent Vaills
 */
public class CommandAPIImpl implements CommandAPI {

    protected static TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addDependency(final String name, final byte[] jar) throws AlreadyExistsException, CreationException {
        // FIXME method in dependency service which get a dependency using its name
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DependencyService dependencyService = tenantAccessor.getDependencyService();
        final AddSDependency addSDependency = new AddSDependency(dependencyService, name, jar, tenantAccessor.getTenantId(), ScopeType.TENANT);
        try {
            addSDependency.execute();
        } catch (final SDependencyAlreadyExistsException e) {
            throw new AlreadyExistsException(e);
        } catch (final SBonitaException sbe) {
            throw new CreationException(sbe);
        }
    }

    @Override
    public void removeDependency(final String name) throws DependencyNotFoundException, DeletionException {
        // FIXME it is maybe too much to delete dependency mappings with the dependency -> better if there are mappings of this dependency throws an exception.
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DependencyService dependencyService = tenantAccessor.getDependencyService();
        final DeleteSDependency deleteSDependency = new DeleteSDependency(dependencyService, name);
        try {
            deleteSDependency.execute();
        } catch (final SDependencyNotFoundException sdnfe) {
            throw new DependencyNotFoundException(sdnfe);
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        }
    }

    @Override
    public CommandDescriptor register(final String name, final String description, final String implementation) throws AlreadyExistsException,
    CreationException {
        CommandDescriptor existingCommandDescriptor = null;
        try {
            existingCommandDescriptor = getCommand(name);
        } catch (final CommandNotFoundException notFoundE) {
            // Nothing to do : no command with that name exists.
        } finally {
            if (existingCommandDescriptor != null) {
                throw new AlreadyExistsException("A command with name \"" + name + "\" already exists");
            }
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final CommandService commandService = tenantAccessor.getCommandService();
        final SCommandBuilderFactory fact = BuilderFactory.get(SCommandBuilderFactory.class);
        final SCommand sCommand = fact.createNewInstance(name, description, implementation).setSystem(false).done();
        try {
            commandService.create(sCommand);
            return ModelConvertor.toCommandDescriptor(sCommand);
        } catch (final SBonitaException sbe) {
            throw new CreationException(sbe);
        }
    }

    private TenantCommand fetchTenantCommand(final SCommandFetcher commandFetcher, final boolean transactionManagedManually) throws SCommandNotFoundException,
    SCommandParameterizationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();

        try {
            final SCommand sCommand;
            if (transactionManagedManually) {
                sCommand = commandFetcher.fetchInTransaction(tenantAccessor.getUserTransactionService(), tenantAccessor.getCommandService());
            } else {
                sCommand = commandFetcher.fetch(tenantAccessor.getCommandService());
            }

            final String tenantCommandClassName = sCommand.getImplementation();
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            return (TenantCommand) contextClassLoader.loadClass(tenantCommandClassName).newInstance();
        } catch (final ClassNotFoundException cnfe) {
            throw new SCommandParameterizationException(cnfe);
        } catch (final InstantiationException ie) {
            throw new SCommandParameterizationException(ie);
        } catch (final IllegalAccessException iae) {
            throw new SCommandParameterizationException(iae);
        }
    }

    @Override
    public Serializable execute(final String commandName, final Map<String, Serializable> parameters) throws CommandNotFoundException,
    CommandParameterizationException, CommandExecutionException {
        return execute(new SCommandFetcherByName(commandName), parameters);
    }

    @Override
    public Serializable execute(final long commandId, final Map<String, Serializable> parameters) throws CommandNotFoundException,
    CommandParameterizationException, CommandExecutionException {
        return execute(new SCommandFetcherById(commandId), parameters);
    }

    private Serializable execute(final SCommandFetcher commandFetcher, final Map<String, Serializable> parameters) throws CommandNotFoundException,
    CommandParameterizationException, CommandExecutionException {
        return executeCommand(commandFetcher, parameters, false);
    }

    @Override
    @CustomTransactions
    public Serializable executeWithUserTransactions(final String commandName, final Map<String, Serializable> parameters) throws CommandNotFoundException,
    CommandParameterizationException, CommandExecutionException {
        return executeWithUserTransactions(new SCommandFetcherByName(commandName), parameters);
    }

    @Override
    @CustomTransactions
    public Serializable executeWithUserTransactions(final long commandId, final Map<String, Serializable> parameters) throws CommandNotFoundException,
    CommandParameterizationException, CommandExecutionException {
        return executeWithUserTransactions(new SCommandFetcherById(commandId), parameters);
    }

    private Serializable executeWithUserTransactions(final SCommandFetcher commandFetcher, final Map<String, Serializable> parameters)
            throws CommandNotFoundException, CommandParameterizationException, CommandExecutionException {
        return executeCommand(commandFetcher, parameters, true);
    }

    private Serializable executeCommand(final SCommandFetcher commandFetcher, final Map<String, Serializable> parameters,
            final boolean transactionManagedManually) throws CommandNotFoundException, CommandParameterizationException, CommandExecutionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();

        try {
            final TenantCommand tenantCommand = fetchTenantCommand(commandFetcher, transactionManagedManually);
            return tenantCommand.execute(parameters, tenantAccessor);
        } catch (final SCommandExecutionException scee) {
            throw new CommandExecutionException(scee);
        } catch (final SCommandParameterizationException scpe) {
            throw new CommandParameterizationException(scpe);
        } catch (final SCommandNotFoundException e) {
            throw new CommandNotFoundException(e);
        }
    }

    @Override
    public void unregister(final long commandId) throws CommandNotFoundException, DeletionException {
        final CommandService commandService = getTenantAccessor().getCommandService();
        final DeleteSCommand deleteCommand = new DeleteSCommand(commandService, commandId);
        unregister(deleteCommand);
    }

    @Override
    public void unregister(final String name) throws CommandNotFoundException, DeletionException {
        if (name == null) {
            // FIXME: throw IllegalArgumentException instead, and make bonita interceptor catch all exceptions and wrap it into BonitaRuntimeException:
            throw new DeletionException("Command name can not be null!");
        }
        final CommandService commandService = getTenantAccessor().getCommandService();
        final DeleteSCommand deleteCommand = new DeleteSCommand(commandService, name);
        unregister(deleteCommand);
    }

    private void unregister(final DeleteSCommand deleteCommand) throws CommandNotFoundException, DeletionException {
        try {
            deleteCommand.execute();
        } catch (final SCommandNotFoundException scnfe) {
            throw new CommandNotFoundException(scnfe);
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        }
    }

    @Override
    public void unregisterAll() throws DeletionException {
        final CommandService commandService = getTenantAccessor().getCommandService();
        try {
            commandService.deleteAll();
        } catch (final SCommandDeletionException sde) {
            throw new DeletionException(sde);
        }
    }

    @Override
    public CommandDescriptor get(final long commandId) throws CommandNotFoundException {
        return getCommand(new SCommandFetcherById(commandId));
    }

    @Override
    public CommandDescriptor getCommand(final String commandName) throws CommandNotFoundException {
        return getCommand(new SCommandFetcherByName(commandName));
    }

    private CommandDescriptor getCommand(final SCommandFetcher commandFetcher) throws CommandNotFoundException {
        final CommandService commandService = getTenantAccessor().getCommandService();
        try {
            final SCommand sCommand = commandFetcher.fetch(commandService);
            return ModelConvertor.toCommandDescriptor(sCommand);
        } catch (final SBonitaException e) {
            throw new CommandNotFoundException(e);
        }
    }

    @Override
    public List<CommandDescriptor> getAllCommands(final int startIndex, final int maxResults, final CommandCriterion sort) {
        SCommandCriterion sCommandCriterion = null;
        if (CommandCriterion.NAME_ASC.equals(sort)) {
            sCommandCriterion = SCommandCriterion.NAME_ASC;
        } else {
            sCommandCriterion = SCommandCriterion.NAME_DESC;
        }

        final CommandService commandService = getTenantAccessor().getCommandService();
        try {
            final List<SCommand> commands = commandService.getAllCommands(startIndex, maxResults, sCommandCriterion);
            return ModelConvertor.toCommandDescriptors(commands);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public void update(final long commandId, final CommandUpdater updateDescriptor) throws UpdateException {
        update(new SCommandFetcherById(commandId), updateDescriptor);
    }

    @Override
    public void update(final String commandName, final CommandUpdater updateDescriptor) throws UpdateException {
        update(new SCommandFetcherByName(commandName), updateDescriptor);
    }

    private void update(final SCommandFetcher commandFetcher, final CommandUpdater updateDescriptor) throws UpdateException {
        if (updateDescriptor == null || updateDescriptor.getFields().isEmpty()) {
            throw new UpdateException("The update descriptor does not contain field updates");
        }

        final SCommandUpdateBuilderFactory fact = BuilderFactory.get(SCommandUpdateBuilderFactory.class);
        final SCommandUpdateBuilder commandUpdateBuilder = fact.createNewInstance();
        final CommandService commandService = getTenantAccessor().getCommandService();
        try {
            final EntityUpdateDescriptor changeDescriptor = getCommandUpdateDescriptor(updateDescriptor, commandUpdateBuilder);
            final SCommand sCommand = commandFetcher.fetch(commandService);
            commandService.update(sCommand, changeDescriptor);
        } catch (final SCommandNotFoundException scnfe) {
            throw new UpdateException(scnfe);
        } catch (final SCommandUpdateException e) {
            throw new UpdateException(e);
        }
    }

    private EntityUpdateDescriptor getCommandUpdateDescriptor(final CommandUpdater updateDescriptor, final SCommandUpdateBuilder commandUpdateBuilder) {
        final Map<CommandField, Serializable> fields = updateDescriptor.getFields();
        for (final Entry<CommandField, Serializable> field : fields.entrySet()) {
            final String value = (String) field.getValue();
            switch (field.getKey()) {
                case NAME:
                    commandUpdateBuilder.updateName(value);
                    break;
                case DESCRIPTION:
                    commandUpdateBuilder.updateDescription(value);
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
        return commandUpdateBuilder.done();
    }

    @Override
    public List<CommandDescriptor> getUserCommands(final int startIndex, final int maxResults, final CommandCriterion sort) {
        final CommandService commandService = getTenantAccessor().getCommandService();
        try {
            final GetCommands getCommands = new GetCommands(commandService, startIndex, maxResults, sort);
            getCommands.execute();
            return ModelConvertor.toCommandDescriptors(getCommands.getResult());
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public SearchResult<CommandDescriptor> searchCommands(final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final CommandService commandService = tenantAccessor.getCommandService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchCommands searchCommands = new SearchCommands(commandService, searchEntitiesDescriptor.getSearchCommandDescriptor(), searchOptions);
        try {
            searchCommands.execute();
            return searchCommands.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

    // Utility classes to factorize how we fetch a TenantCommand
    private abstract static class SCommandFetcher {

        abstract SCommand fetch(final CommandService commandService) throws SCommandNotFoundException;

        SCommand fetchInTransaction(final UserTransactionService userTransactionService, final CommandService commandService) throws SCommandNotFoundException {
            try {
                return userTransactionService.executeInTransaction(new Callable<SCommand>() {

                    @Override
                    public SCommand call() throws Exception {
                        return fetch(commandService);
                    }
                });
            } catch (final Exception e) {
                throw new SCommandNotFoundException(e);
            }

        }
    }

    private static class SCommandFetcherByName extends SCommandFetcher {

        private final String commandName;

        public SCommandFetcherByName(final String commandName) {
            this.commandName = commandName;
        }

        @Override
        SCommand fetch(final CommandService commandService) throws SCommandNotFoundException {
            return commandService.get(commandName);
        }
    }

    private static class SCommandFetcherById extends SCommandFetcher {

        private final long commandId;

        public SCommandFetcherById(final long commandId) {
            this.commandId = commandId;
        }

        @Override
        SCommand fetch(final CommandService commandService) throws SCommandNotFoundException {
            return commandService.get(commandId);
        }
    }

}
