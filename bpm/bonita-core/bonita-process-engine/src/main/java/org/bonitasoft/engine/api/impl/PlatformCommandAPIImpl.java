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
import org.bonitasoft.engine.api.impl.transaction.platform.DeleteSPlatformCommand;
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
import org.bonitasoft.engine.command.SCommandNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.platform.command.PlatformCommandService;
import org.bonitasoft.engine.platform.command.SPlatformCommandGettingException;
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
    public void addDependency(final String name, final byte[] jar) throws CreationException {
        final PlatformServiceAccessor platformAccessor = getPlatformServiceAccessor();
        final DependencyService dependencyService = platformAccessor.getDependencyService();
        final ClassLoaderService classLoaderService = platformAccessor.getClassLoaderService();
        try {
            ScopeType type = ScopeType.valueOf(classLoaderService.getGlobalClassLoaderType());
            long id = classLoaderService.getGlobalClassLoaderId();
            dependencyService.createMappedDependency(name, jar, name, id, type);
            dependencyService.refreshClassLoaderAfterUpdate(type, id);
        } catch (SDependencyException e) {
            throw new CreationException(e);
        }
    }

    @Override
    public void removeDependency(final String name) throws DependencyNotFoundException, DeletionException {
        final PlatformServiceAccessor platformAccessor = getPlatformServiceAccessor();
        final DependencyService dependencyService = platformAccessor.getDependencyService();
        ClassLoaderService classLoaderService = platformAccessor.getClassLoaderService();

        try {
            dependencyService.deleteDependency(name);
            dependencyService.refreshClassLoaderAfterUpdate(ScopeType.valueOf(classLoaderService.getGlobalClassLoaderType()), classLoaderService.getGlobalClassLoaderId());
        } catch (final SDependencyNotFoundException e) {
            throw new DependencyNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    @Override
    public CommandDescriptor register(final String name, final String description, final String implementation) throws CreationException {
        final PlatformServiceAccessor platformAccessor = getPlatformServiceAccessor();
        final PlatformCommandService platformCommandService = platformAccessor.getPlatformCommandService();
        try {
            platformCommandService.getPlatformCommand(name);
            throw new AlreadyExistsException("A command with name \"" + name + "\" already exists");
        } catch (SPlatformCommandNotFoundException ignored) {
        } catch (SPlatformCommandGettingException e) {
            throw new CreationException("Unable to create the platform command", e);
        }
        final SPlatformCommand sPlatformCommand = BuilderFactory.get(SPlatformCommandBuilderFactory.class).createNewInstance(name, description, implementation)
                .done();
        try {
            platformCommandService.create(sPlatformCommand);
            return ModelConvertor.toCommandDescriptor(sPlatformCommand);
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }
    }

    @Override
    public Serializable execute(final String platformCommandName, final Map<String, Serializable> parameters) throws CommandNotFoundException,
            CommandParameterizationException, CommandExecutionException {
        final PlatformServiceAccessor platformAccessor = getPlatformServiceAccessor();

        final PlatformCommandService platformCommandService = platformAccessor.getPlatformCommandService();
        try {
            SPlatformCommand sPlatformCommand = platformCommandService.getPlatformCommand(platformCommandName);
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            PlatformCommand command = (PlatformCommand) contextClassLoader.loadClass(sPlatformCommand.getImplementation()).newInstance();
            return command.execute(parameters, platformAccessor);
        } catch (final SPlatformCommandNotFoundException e) {
            throw new CommandNotFoundException(e);
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new CommandParameterizationException(e);
        } catch (final SBonitaException e) {
            throw new CommandExecutionException(e);
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
    public CommandDescriptor getCommand(final String platformCommandName) throws CommandNotFoundException {
        final PlatformServiceAccessor platformAccessor = getPlatformServiceAccessor();

        final PlatformCommandService platformCommandService = platformAccessor.getPlatformCommandService();
        try {
            return ModelConvertor.toCommandDescriptor(platformCommandService.getPlatformCommand(platformCommandName));
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
