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
package org.bonitasoft.engine.platform.command.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.platform.command.PlatformCommandService;
import org.bonitasoft.engine.platform.command.SPlatformCommandAlreadyExistsException;
import org.bonitasoft.engine.platform.command.SPlatformCommandCreationException;
import org.bonitasoft.engine.platform.command.SPlatformCommandDeletionException;
import org.bonitasoft.engine.platform.command.SPlatformCommandGettingException;
import org.bonitasoft.engine.platform.command.SPlatformCommandNotFoundException;
import org.bonitasoft.engine.platform.command.SPlatformCommandUpdateException;
import org.bonitasoft.engine.platform.command.model.SPlatformCommand;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;

/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 */
@Slf4j
public class PlatformCommandServiceImpl implements PlatformCommandService {

    private final PersistenceService platformPersistenceService;

    public PlatformCommandServiceImpl(final PersistenceService platformPersistenceService) {
        super();
        this.platformPersistenceService = platformPersistenceService;
    }

    @Override
    public void create(final SPlatformCommand platformCommand)
            throws SPlatformCommandAlreadyExistsException, SPlatformCommandCreationException,
            SPlatformCommandGettingException {
        NullCheckingUtil.checkArgsNotNull(platformCommand);
        SPlatformCommand existedPlatformCommand = null;
        try {
            existedPlatformCommand = getPlatformCommand(platformCommand.getName());
        } catch (final SPlatformCommandNotFoundException ignored) {
        } finally {
            if (existedPlatformCommand != null) {
                throw new SPlatformCommandAlreadyExistsException("platformCommand already existed");
            }
        }
        try {
            platformPersistenceService.insert(platformCommand);
        } catch (final SPersistenceException pe) {
            throw new SPlatformCommandCreationException(pe);
        }
    }

    public void deletePlatformCommand(final SPlatformCommand sPlatformCommand)
            throws SPlatformCommandDeletionException {
        try {
            platformPersistenceService.delete(sPlatformCommand);
        } catch (final SPersistenceException pe) {
            throw new SPlatformCommandDeletionException(pe);
        }
    }

    @Override
    public void delete(final String platformCommandName)
            throws SPlatformCommandNotFoundException, SPlatformCommandDeletionException,
            SPlatformCommandGettingException {
        final SPlatformCommand sPlatformCommand = getPlatformCommand(platformCommandName);
        deletePlatformCommand(sPlatformCommand);
    }

    @Override
    public void deleteAll() throws SPlatformCommandDeletionException {
        final QueryOptions queryOptions = new QueryOptions(0, 100, SPlatformCommand.class, "id", OrderByType.ASC);
        List<SPlatformCommand> sPlatformCommands = null;
        do {
            try {
                sPlatformCommands = getPlatformCommands(queryOptions);
            } catch (final SPlatformCommandGettingException e) {
                throw new SPlatformCommandDeletionException(e);
            }
            for (final SPlatformCommand sPlatformCommand : sPlatformCommands) {
                deletePlatformCommand(sPlatformCommand);
            }
        } while (sPlatformCommands.size() == queryOptions.getNumberOfResults());
    }

    @Override
    public List<SPlatformCommand> getPlatformCommands(final QueryOptions queryOptions)
            throws SPlatformCommandGettingException {
        final Map<String, Object> parameters = Collections.emptyMap();
        try {
            return platformPersistenceService
                    .selectList(new SelectListDescriptor<SPlatformCommand>(
                            "getPlatformCommands", parameters, SPlatformCommand.class, queryOptions));
        } catch (final SBonitaReadException bre) {
            throw new SPlatformCommandGettingException(bre);
        }
    }

    @Override
    public SPlatformCommand getPlatformCommand(final String platformCommandName)
            throws SPlatformCommandNotFoundException, SPlatformCommandGettingException {
        final Map<String, Object> parameters = Collections.singletonMap("name", platformCommandName);
        try {
            final SPlatformCommand sPlatformCommand = platformPersistenceService
                    .selectOne(new SelectOneDescriptor<SPlatformCommand>(
                            "getPlatformCommandByName", parameters, SPlatformCommand.class));
            if (sPlatformCommand == null) {
                throw new SPlatformCommandNotFoundException(
                        "No platformCommand exists using name: " + platformCommandName);
            }
            return sPlatformCommand;
        } catch (final SBonitaReadException bre) {
            throw new SPlatformCommandGettingException(bre);
        }
    }

    @Override
    public void update(final SPlatformCommand platformCommand, final EntityUpdateDescriptor updateDescriptor)
            throws SPlatformCommandUpdateException {
        final UpdateDescriptor desc = new UpdateDescriptor(platformCommand);
        desc.addFields(updateDescriptor.getFields());
        try {
            platformPersistenceService.update(desc);
        } catch (final SPersistenceException pe) {
            throw new SPlatformCommandUpdateException(pe);
        }
    }
}
