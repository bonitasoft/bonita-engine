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

import org.bonitasoft.engine.api.PlatformCommandAPI;
import org.bonitasoft.engine.api.impl.transaction.dependency.DeleteSDependency;
import org.bonitasoft.engine.api.impl.transaction.platform.AddSPlatformCommandDependency;
import org.bonitasoft.engine.api.impl.transaction.platform.CreateSPlatformCommand;
import org.bonitasoft.engine.api.impl.transaction.platform.DeleteSPlatformCommand;
import org.bonitasoft.engine.api.impl.transaction.platform.GetPlatformCommand;
import org.bonitasoft.engine.api.impl.transaction.platform.GetSPlatformCommand;
import org.bonitasoft.engine.api.impl.transaction.platform.GetSPlatformCommands;
import org.bonitasoft.engine.api.impl.transaction.platform.UpdateSPlatformCommand;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.command.CommandCriterion;
import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.command.CommandUpdater;
import org.bonitasoft.engine.command.DependencyNotFoundException;
import org.bonitasoft.engine.command.PlatformCommand;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandNotFoundException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.platform.command.PlatformCommandService;
import org.bonitasoft.engine.platform.command.SPlatformCommandNotFoundException;
import org.bonitasoft.engine.platform.command.model.SPlatformCommand;
import org.bonitasoft.engine.platform.command.model.SPlatformCommandBuilderFactory;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;

/**
 * @author Matthieu Chaffotte
 * @author Zhang Bole
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class PlatformCommandAPIImpl implements PlatformCommandAPI {

    private static PlatformServiceAccessor getPlatformServiceAccessor() throws RetrieveException {
        try {
            return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (final Exception e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public void addDependency(final String name, final byte[] jar) throws AlreadyExistsException, CreationException {
        final PlatformServiceAccessor platformAccessor = getPlatformServiceAccessor();
        final DependencyService dependencyService = platformAccessor.getDependencyService();
        final ClassLoaderService classLoaderService = platformAccessor.getClassLoaderService();
        final long artifactId = classLoaderService.getGlobalClassLoaderId();
        final ScopeType artifactType = ScopeType.valueOf(classLoaderService.getGlobalClassLoaderType());
        final AddSPlatformCommandDependency addSDependency = new AddSPlatformCommandDependency(dependencyService, name, jar, artifactId, artifactType);
        try {
            addSDependency.execute();
        } catch (final SBonitaException sbe) {
            throw new CreationException(sbe);
        }
    }

    @Override
    public void removeDependency(final String name) throws DependencyNotFoundException, DeletionException {
        final PlatformServiceAccessor platformAccessor = getPlatformServiceAccessor();
        final DependencyService dependencyService = platformAccessor.getDependencyService();

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
            existingCommandDescriptor = get(name);
        } catch (final CommandNotFoundException unfe) {
        } finally {
            if (existingCommandDescriptor != null) {
                throw new AlreadyExistsException("A command with name \"" + name + "\" already exists");
            }
        }
        final PlatformServiceAccessor platformAccessor = getPlatformServiceAccessor();
        final PlatformCommandService platformCommandService = platformAccessor.getPlatformCommandService();

        final SPlatformCommand sPlatformCommand = BuilderFactory.get(SPlatformCommandBuilderFactory.class).createNewInstance(name, description, implementation)
                .done();
        try {
            final CreateSPlatformCommand createPlatformCommand = new CreateSPlatformCommand(platformCommandService, sPlatformCommand);
            createPlatformCommand.execute();
            return ModelConvertor.toCommandDescriptor(sPlatformCommand);
        } catch (final SBonitaException sbe) {
            throw new CreationException(sbe);
        }
    }

    @Override
    public Serializable execute(final String platformCommandName, final Map<String, Serializable> parameters) throws CommandNotFoundException,
            CommandParameterizationException, CommandExecutionException {
        final PlatformServiceAccessor platformAccessor = getPlatformServiceAccessor();

        final PlatformCommandService platformCommandService = platformAccessor.getPlatformCommandService();
        try {
            final GetSPlatformCommand getPlatformCmdTx = new GetSPlatformCommand(platformCommandService, platformCommandName);
            getPlatformCmdTx.execute();
            final SPlatformCommand sPlatformCommand = getPlatformCmdTx.getResult();
            final GetPlatformCommand getPlatformCommand = new GetPlatformCommand(sPlatformCommand.getImplementation());
            getPlatformCommand.execute();
            final PlatformCommand command = getPlatformCommand.getResult();
            return command.execute(parameters, platformAccessor);
        } catch (final SPlatformCommandNotFoundException scnfe) {
            throw new CommandNotFoundException(scnfe);
        } catch (final SCommandParameterizationException scpe) {
            throw new CommandParameterizationException(scpe);
        } catch (final SCommandExecutionException scee) {
            throw new CommandExecutionException(scee);
        } catch (final SBonitaException sbe) {
            throw new CommandExecutionException(sbe);
        }
    }

    @Override
    public void unregister(final String platformCommandName) throws CommandNotFoundException, DeletionException {
        if (platformCommandName == null) {
            throw new DeletionException("Command name can not be null!");
        }
        try {
            final PlatformServiceAccessor platformAccessor = getPlatformServiceAccessor();
            final PlatformCommandService platformCommandService = platformAccessor.getPlatformCommandService();

            final DeleteSPlatformCommand deletePlatformCommand = new DeleteSPlatformCommand(platformCommandService, platformCommandName);
            deletePlatformCommand.execute();
        } catch (final SCommandNotFoundException scnfe) {
            throw new CommandNotFoundException(scnfe);
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        }
    }

    @Override
    public CommandDescriptor get(final String platformCommandName) throws CommandNotFoundException {
        return getCommand(platformCommandName);
    }

    @Override
    public CommandDescriptor getCommand(final String platformCommandName) throws CommandNotFoundException {
        final PlatformServiceAccessor platformAccessor = getPlatformServiceAccessor();

        final PlatformCommandService platformCommandService = platformAccessor.getPlatformCommandService();
        try {
            final GetSPlatformCommand getPlatformComandByName = new GetSPlatformCommand(platformCommandService, platformCommandName);
            getPlatformComandByName.execute();
            final SPlatformCommand sPlatformCommand = getPlatformComandByName.getResult();
            return ModelConvertor.toCommandDescriptor(sPlatformCommand);
        } catch (final SBonitaException e) {
            throw new CommandNotFoundException(e);
        }
    }

    @Override
    public List<CommandDescriptor> getCommands(final int startIndex, final int maxResults, final CommandCriterion sort) {
        final PlatformServiceAccessor platformAccessor = getPlatformServiceAccessor();
        final PlatformCommandService platformCommandService = platformAccessor.getPlatformCommandService();

        try {
            final GetSPlatformCommands getPlatformCommands = new GetSPlatformCommands(platformCommandService, startIndex, maxResults, sort);
            getPlatformCommands.execute();
            return ModelConvertor.toPlatformCommandDescriptors(getPlatformCommands.getResult());
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public void update(final String platformCommandName, final CommandUpdater updater) throws UpdateException {
        if (updater == null || updater.getFields().isEmpty()) {
            throw new UpdateException("The update descriptor does not contain field updates");
        }
        final PlatformServiceAccessor platformAccessor = getPlatformServiceAccessor();
        final PlatformCommandService platformCommandService = platformAccessor.getPlatformCommandService();

        try {
            final UpdateSPlatformCommand updatePlatformCommand = new UpdateSPlatformCommand(platformCommandService, platformCommandName, updater);
            updatePlatformCommand.execute();
        } catch (final SCommandNotFoundException scnfe) {
            throw new UpdateException(scnfe);
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

}
