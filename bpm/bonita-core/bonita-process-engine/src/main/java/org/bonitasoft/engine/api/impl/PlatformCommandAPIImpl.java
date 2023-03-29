/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.GLOBAL;
import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.GLOBAL_ID;
import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.GLOBAL_TYPE;
import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.PlatformCommandAPI;
import org.bonitasoft.engine.api.impl.transaction.platform.DeleteSPlatformCommand;
import org.bonitasoft.engine.api.impl.transaction.platform.GetSPlatformCommands;
import org.bonitasoft.engine.api.impl.transaction.platform.UpdateSPlatformCommand;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.command.*;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.platform.command.PlatformCommandService;
import org.bonitasoft.engine.platform.command.SPlatformCommandGettingException;
import org.bonitasoft.engine.platform.command.SPlatformCommandNotFoundException;
import org.bonitasoft.engine.platform.command.model.SPlatformCommand;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;

/**
 * @author Matthieu Chaffotte
 * @author Zhang Bole
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class PlatformCommandAPIImpl implements PlatformCommandAPI {

    private static ServiceAccessor getServiceAccessor() throws RetrieveException {
        try {
            return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (final Exception e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public void addDependency(final String name, final byte[] jar) throws CreationException {
        final ServiceAccessor serviceAccessor = getServiceAccessor();
        final DependencyService dependencyService = serviceAccessor.getPlatformDependencyService();
        final ClassLoaderService classLoaderService = serviceAccessor.getClassLoaderService();
        try {
            dependencyService.createMappedDependency(name, jar, name, GLOBAL_ID, GLOBAL_TYPE);
            classLoaderService.refreshClassLoaderAfterUpdate(GLOBAL);
        } catch (SDependencyException | SClassLoaderException e) {
            throw new CreationException(e);
        }
    }

    @Override
    public void removeDependency(final String name) throws DependencyNotFoundException, DeletionException {
        final ServiceAccessor serviceAccessor = getServiceAccessor();
        final DependencyService dependencyService = serviceAccessor.getPlatformDependencyService();
        ClassLoaderService classLoaderService = serviceAccessor.getClassLoaderService();

        try {
            dependencyService.deleteDependency(name);
            classLoaderService.refreshClassLoaderAfterUpdate(
                    identifier(GLOBAL_TYPE, GLOBAL_ID));
        } catch (final SDependencyNotFoundException e) {
            throw new DependencyNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    @Override
    public CommandDescriptor register(final String name, final String description, final String implementation)
            throws CreationException {
        final ServiceAccessor serviceAccessor = getServiceAccessor();
        final PlatformCommandService platformCommandService = serviceAccessor.getPlatformCommandService();
        try {
            platformCommandService.getPlatformCommand(name);
            throw new AlreadyExistsException("A command with name \"" + name + "\" already exists");
        } catch (SPlatformCommandNotFoundException ignored) {
        } catch (SPlatformCommandGettingException e) {
            throw new CreationException("Unable to create the platform command", e);
        }
        final SPlatformCommand sPlatformCommand = new SPlatformCommand(name, description, implementation);
        try {
            platformCommandService.create(sPlatformCommand);
            return ModelConvertor.toCommandDescriptor(sPlatformCommand);
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }
    }

    @Override
    public Serializable execute(final String platformCommandName, final Map<String, Serializable> parameters)
            throws CommandNotFoundException,
            CommandParameterizationException, CommandExecutionException {
        final ServiceAccessor serviceAccessor = getServiceAccessor();

        final PlatformCommandService platformCommandService = serviceAccessor.getPlatformCommandService();
        try {
            SPlatformCommand sPlatformCommand = platformCommandService.getPlatformCommand(platformCommandName);
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            RuntimeCommand command = (RuntimeCommand) contextClassLoader
                    .loadClass(sPlatformCommand.getImplementation()).newInstance();
            return command.execute(parameters, serviceAccessor);
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
            final ServiceAccessor serviceAccessor = getServiceAccessor();
            final PlatformCommandService platformCommandService = serviceAccessor.getPlatformCommandService();

            final DeleteSPlatformCommand deletePlatformCommand = new DeleteSPlatformCommand(platformCommandService,
                    platformCommandName);
            deletePlatformCommand.execute();
        } catch (final SCommandNotFoundException scnfe) {
            throw new CommandNotFoundException(scnfe);
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        }
    }

    @Override
    public CommandDescriptor getCommand(final String platformCommandName) throws CommandNotFoundException {
        final ServiceAccessor serviceAccessor = getServiceAccessor();

        final PlatformCommandService platformCommandService = serviceAccessor.getPlatformCommandService();
        try {
            return ModelConvertor.toCommandDescriptor(platformCommandService.getPlatformCommand(platformCommandName));
        } catch (final SBonitaException e) {
            throw new CommandNotFoundException(e);
        }
    }

    @Override
    public List<CommandDescriptor> getCommands(final int startIndex, final int maxResults,
            final CommandCriterion sort) {
        final ServiceAccessor serviceAccessor = getServiceAccessor();
        final PlatformCommandService platformCommandService = serviceAccessor.getPlatformCommandService();

        try {
            final GetSPlatformCommands getPlatformCommands = new GetSPlatformCommands(platformCommandService,
                    startIndex, maxResults, sort);
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
        final ServiceAccessor serviceAccessor = getServiceAccessor();
        final PlatformCommandService platformCommandService = serviceAccessor.getPlatformCommandService();

        try {
            final UpdateSPlatformCommand updatePlatformCommand = new UpdateSPlatformCommand(platformCommandService,
                    platformCommandName, updater);
            updatePlatformCommand.execute();
        } catch (final SCommandNotFoundException scnfe) {
            throw new UpdateException(scnfe);
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

}
