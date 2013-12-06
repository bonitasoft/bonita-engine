/**
 * Copyright (C) 2013 BonitaSoft S.A.
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

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.impl.transaction.command.CreateSCommand;
import org.bonitasoft.engine.api.impl.transaction.command.DeleteAllCommands;
import org.bonitasoft.engine.api.impl.transaction.command.DeleteSCommand;
import org.bonitasoft.engine.api.impl.transaction.command.GetCommands;
import org.bonitasoft.engine.api.impl.transaction.command.GetSCommand;
import org.bonitasoft.engine.api.impl.transaction.command.GetSCommands;
import org.bonitasoft.engine.api.impl.transaction.command.GetTenantCommand;
import org.bonitasoft.engine.api.impl.transaction.command.UpdateSCommand;
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
import org.bonitasoft.engine.command.DependencyNotFoundException;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandNotFoundException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.TenantCommand;
import org.bonitasoft.engine.command.model.SCommand;
import org.bonitasoft.engine.command.model.SCommandBuilderFactory;
import org.bonitasoft.engine.command.model.SCommandUpdateBuilder;
import org.bonitasoft.engine.command.model.SCommandUpdateBuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyAlreadyExistsException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.search.SearchCommands;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
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
        final AddSDependency addSDependency = new AddSDependency(dependencyService, name, jar, tenantAccessor.getTenantId(),
                "tenant");
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
            final CreateSCommand createCommand = new CreateSCommand(commandService, sCommand);
            createCommand.execute();
            return ModelConvertor.toCommandDescriptor(sCommand);
        } catch (final SBonitaException sbe) {
            throw new CreationException(sbe);
        }
    }

    @Override
    public Serializable execute(final String name, final Map<String, Serializable> parameters) throws CommandNotFoundException,
            CommandParameterizationException, CommandExecutionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final CommandService commandService = tenantAccessor.getCommandService();
        try {
            final GetTenantCommand getTenantCommand = new GetTenantCommand(commandService, name);
            getTenantCommand.execute();
            final TenantCommand command = getTenantCommand.getResult();
            return command.execute(parameters, tenantAccessor);
        } catch (final SCommandNotFoundException scnfe) {
            throw new CommandNotFoundException(scnfe);
        } catch (final SCommandExecutionException scee) {
            throw new CommandExecutionException(scee);
        } catch (final SCommandParameterizationException scpe) {
            throw new CommandParameterizationException(scpe);
        } catch (final SBonitaException sbe) {
            throw new CommandExecutionException(sbe);
        }
    }

    @Override
    public Serializable execute(final long commandId, final Map<String, Serializable> parameters) throws CommandNotFoundException,
            CommandParameterizationException, CommandExecutionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final CommandService commandService = tenantAccessor.getCommandService();
        try {
            final GetTenantCommand getTenantCommand = new GetTenantCommand(commandService, commandId);
            getTenantCommand.execute();
            final TenantCommand command = getTenantCommand.getResult();
            return command.execute(parameters, tenantAccessor);
        } catch (final SCommandNotFoundException scnfe) {
            throw new CommandNotFoundException(scnfe);
        } catch (final SCommandExecutionException scee) {
            throw new CommandExecutionException(scee);
        } catch (final SCommandParameterizationException scpe) {
            throw new CommandParameterizationException(scpe);
        } catch (final SBonitaException sbe) {
            throw new CommandExecutionException(sbe);
        }
    }

    @Override
    public void unregister(final String name) throws CommandNotFoundException, DeletionException {
        if (name == null) {
            // FIXME: throw IllegalArgumentException instead, and make bonita interceptor catch all exceptions and wrap it into BonitaRuntimeException:
            throw new DeletionException("Command name can not be null!");
        }
        try {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final CommandService commandService = tenantAccessor.getCommandService();
            final DeleteSCommand deleteCommand = new DeleteSCommand(commandService, name);
            deleteCommand.execute();
        } catch (final SCommandNotFoundException scnfe) {
            throw new CommandNotFoundException(scnfe);
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        }
    }

    @Override
    public CommandDescriptor getCommand(final String commandName) throws CommandNotFoundException {
        final CommandService commandService = getTenantAccessor().getCommandService();
        try {
            final GetSCommand getComandByName = new GetSCommand(commandService, commandName);
            getComandByName.execute();
            final SCommand sCommand = getComandByName.getResult();
            return ModelConvertor.toCommandDescriptor(sCommand);
        } catch (final SBonitaException e) {
            throw new CommandNotFoundException(e);
        }
    }

    @Override
    public List<CommandDescriptor> getAllCommands(final int startIndex, final int maxResults, final CommandCriterion sort) {
        final CommandService commandService = getTenantAccessor().getCommandService();
        try {
            final GetSCommands getCommands = new GetSCommands(commandService, startIndex, maxResults, sort);
            getCommands.execute();
            return ModelConvertor.toCommandDescriptors(getCommands.getResult());
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public void update(final String commandName, final CommandUpdater updateDescriptor) throws UpdateException {
        if (updateDescriptor == null || updateDescriptor.getFields().isEmpty()) {
            throw new UpdateException("The update descriptor does not contain field updates");
        }
        final CommandService commandService = getTenantAccessor().getCommandService();
        final SCommandUpdateBuilderFactory fact = BuilderFactory.get(SCommandUpdateBuilderFactory.class);
        final SCommandUpdateBuilder commandUpdateBuilder = fact.createNewInstance();
        try {
            final UpdateSCommand updateCommand = new UpdateSCommand(commandService, commandUpdateBuilder, commandName, updateDescriptor);
            updateCommand.execute();
        } catch (final SCommandNotFoundException scnfe) {
            throw new UpdateException(scnfe);
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public void unregisterAll() throws DeletionException {
        try {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final CommandService commandService = tenantAccessor.getCommandService();
            final DeleteAllCommands deleteCommand = new DeleteAllCommands(commandService);
            deleteCommand.execute();
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        }
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
    public CommandDescriptor get(final long commandId) throws CommandNotFoundException {
        final CommandService commandService = getTenantAccessor().getCommandService();
        try {
            final GetSCommand getComandById = new GetSCommand(commandService, commandId);
            getComandById.execute();
            final SCommand sCommand = getComandById.getResult();
            return ModelConvertor.toCommandDescriptor(sCommand);
        } catch (final SBonitaException e) {
            throw new CommandNotFoundException(e);
        }
    }

    @Override
    public void update(final long commandId, final CommandUpdater updater) throws UpdateException {
        if (updater == null || updater.getFields().isEmpty()) {
            throw new UpdateException("The update descriptor does not contain field updates");
        }
        final CommandService commandService = getTenantAccessor().getCommandService();
        final SCommandUpdateBuilderFactory fact = BuilderFactory.get(SCommandUpdateBuilderFactory.class);
        final SCommandUpdateBuilder commandUpdateBuilder = fact.createNewInstance();
        try {
            final UpdateSCommand updateCommand = new UpdateSCommand(commandService, commandUpdateBuilder, commandId, updater);
            updateCommand.execute();
        } catch (final SCommandNotFoundException scnfe) {
            throw new UpdateException(scnfe);
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public void unregister(final long commandId) throws CommandNotFoundException, DeletionException {
        try {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final CommandService commandService = tenantAccessor.getCommandService();
            final DeleteSCommand deleteCommand = new DeleteSCommand(commandService, commandId);
            deleteCommand.execute();
        } catch (final SCommandNotFoundException scnfe) {
            throw new CommandNotFoundException(scnfe);
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        }
    }

    @Override
    public SearchResult<CommandDescriptor> searchCommands(final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final CommandService commandService = tenantAccessor.getCommandService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchCommands searchCommands = new SearchCommands(commandService, searchEntitiesDescriptor.getCommandDescriptor(), searchOptions);
        try {
            searchCommands.execute();
            return searchCommands.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

}
