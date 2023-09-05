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
package org.bonitasoft.engine.command.api.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.command.CommandService;
import org.bonitasoft.engine.command.SCommandAlreadyExistsException;
import org.bonitasoft.engine.command.SCommandCreationException;
import org.bonitasoft.engine.command.SCommandDeletionException;
import org.bonitasoft.engine.command.SCommandGettingException;
import org.bonitasoft.engine.command.SCommandNotFoundException;
import org.bonitasoft.engine.command.SCommandUpdateException;
import org.bonitasoft.engine.command.api.record.SelectDescriptorBuilder;
import org.bonitasoft.engine.command.model.SCommand;
import org.bonitasoft.engine.command.model.SCommandCriterion;
import org.bonitasoft.engine.command.model.SCommandLogBuilder;
import org.bonitasoft.engine.command.model.SCommandLogBuilderFactory;
import org.bonitasoft.engine.command.model.SCommandUpdateBuilderImpl;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 * @author Hongwen Zang
 * @author Celine Souchet
 */
@Slf4j
public class CommandServiceImpl implements CommandService {

    public static final int FETCH_SIZE = 1000;
    private final ReadPersistenceService persistenceService;

    private final Recorder recorder;

    private final EventService eventService;

    private final QueriableLoggerService queriableLoggerService;
    private final CommandProvider defaultCommandProvider;
    private final int fetchSize;

    public CommandServiceImpl(final ReadPersistenceService persistenceService, final Recorder recorder,
            final EventService eventService, final QueriableLoggerService queriableLoggerService,
            CommandProvider defaultCommandProvider) {
        this(persistenceService, recorder, eventService, queriableLoggerService, defaultCommandProvider,
                FETCH_SIZE);
    }

    public CommandServiceImpl(final ReadPersistenceService persistenceService, final Recorder recorder,
            final EventService eventService, final QueriableLoggerService queriableLoggerService,
            CommandProvider defaultCommandProvider, int fetchSize) {
        super();
        this.persistenceService = persistenceService;
        this.recorder = recorder;
        this.eventService = eventService;
        this.queriableLoggerService = queriableLoggerService;
        this.defaultCommandProvider = defaultCommandProvider;
        this.fetchSize = fetchSize;
    }

    private SCommandLogBuilder getQueriableLog(final ActionType actionType, final String message) {
        final SCommandLogBuilder logBuilder = BuilderFactory.get(SCommandLogBuilderFactory.class).createNewInstance();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    @Override
    public void create(final SCommand command) throws SCommandAlreadyExistsException, SCommandCreationException {
        try {
            this.get(command.getName());
            throw new SCommandAlreadyExistsException("Command '" + command.getName() + "' already exists");
        } catch (final SCommandNotFoundException e) {
            final SCommandLogBuilder logBuilder = getQueriableLog(ActionType.CREATED,
                    "Creating a new command with name " + command.getName());
            try {
                recorder.recordInsert(new InsertRecord(command), COMMAND);
                log(command.getId(), SQueriableLog.STATUS_OK, logBuilder, "create");
            } catch (final SRecorderException re) {
                log(command.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "create");
                throw new SCommandCreationException(re);
            }
        }
    }

    @Override
    public void delete(final long commandId) throws SCommandNotFoundException, SCommandDeletionException {
        final SCommandLogBuilder logBuilder = getQueriableLog(ActionType.DELETED,
                "Deleting command with id " + commandId);
        final SCommand command = this.get(commandId);
        delete(command, logBuilder);
    }

    protected void delete(final SCommand command, final SCommandLogBuilder logBuilder)
            throws SCommandDeletionException {
        try {
            recorder.recordDelete(new DeleteRecord(command), COMMAND);
            log(command.getId(), SQueriableLog.STATUS_OK, logBuilder, "delete");
        } catch (final SRecorderException re) {
            log(command.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "delete");
            throw new SCommandDeletionException(re);
        }
    }

    @Override
    public void delete(final String commandName) throws SCommandNotFoundException, SCommandDeletionException {
        final SCommandLogBuilder logBuilder = getQueriableLog(ActionType.DELETED,
                "Deleting command with name " + commandName);
        final SCommand command = this.get(commandName);
        delete(command, logBuilder);
    }

    @Override
    public void deleteAll() throws SCommandDeletionException {
        List<SCommand> commands;
        try {
            do {
                commands = getAllCommands(0, 1000, SCommandCriterion.NAME_ASC);
                for (final SCommand command : commands) {
                    final SCommandLogBuilder logBuilder = getQueriableLog(ActionType.DELETED,
                            "Deleting command with name " + command.getName());
                    delete(command, logBuilder);
                }
            } while (!commands.isEmpty());
        } catch (final SCommandGettingException scge) {
            throw new SCommandDeletionException(scge);
        }
    }

    @Override
    public SCommand get(final String commandName) throws SCommandNotFoundException {
        try {
            final SelectOneDescriptor<SCommand> descriptor = SelectDescriptorBuilder.getCommandByName(commandName);
            final SCommand scommand = persistenceService.selectOne(descriptor);
            if (scommand == null) {
                throw new SCommandNotFoundException("command '" + commandName + "' does not exist");
            }
            return scommand;
        } catch (final SBonitaReadException e) {
            throw new SCommandNotFoundException("Cannot get command: " + commandName, e);
        }
    }

    @Override
    public List<SCommand> getAllCommands(final int startIndex, final int maxResults,
            final SCommandCriterion sCommandCriterion) throws SCommandGettingException {
        OrderByType orderByType;
        switch (sCommandCriterion) {
            case NAME_ASC:
                orderByType = OrderByType.ASC;
                break;
            case NAME_DESC:
                orderByType = OrderByType.DESC;
                break;
            default:
                throw new IllegalStateException();
        }
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getCommands("name", orderByType,
                    startIndex, maxResults));
        } catch (final SBonitaReadException e) {
            throw new SCommandGettingException("can't get the commands", e);
        }
    }

    @Override
    public void update(final SCommand command, final EntityUpdateDescriptor updateDescriptor)
            throws SCommandUpdateException {
        final SCommandLogBuilder logBuilder = getQueriableLog(ActionType.UPDATED,
                "Updating command with name " + command.getName());
        try {
            recorder.recordUpdate(UpdateRecord.buildSetFields(command, updateDescriptor), COMMAND);
            log(command.getId(), SQueriableLog.STATUS_OK, logBuilder, "update");
        } catch (final SRecorderException re) {
            log(command.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "update");
            throw new SCommandUpdateException(re);
        }
    }

    @Override
    public List<SCommand> getUserCommands(final int startIndex, final int maxResults,
            final SCommandCriterion sCommandCriterion)
            throws SCommandGettingException {
        OrderByType orderByType;
        switch (sCommandCriterion) {
            case NAME_ASC:
                orderByType = OrderByType.ASC;
                break;
            case NAME_DESC:
                orderByType = OrderByType.DESC;
                break;
            default:
                throw new IllegalStateException();
        }
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getUserCommands("name",
                    orderByType, startIndex, maxResults));
        } catch (final SBonitaReadException e) {
            throw new SCommandGettingException("can't get the commands", e);
        }
    }

    @Override
    public SCommand get(final long commandId) throws SCommandNotFoundException {
        final SelectByIdDescriptor<SCommand> selectByIdDescriptor = SelectDescriptorBuilder.getCommandById(commandId);
        try {
            final SCommand command = persistenceService.selectById(selectByIdDescriptor);
            if (command == null) {
                throw new SCommandNotFoundException(commandId + " does not refer to any command");
            }
            return command;
        } catch (final SBonitaReadException bre) {
            throw new SCommandNotFoundException(bre);
        }
    }

    @Override
    public long getNumberOfCommands(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SCommand.class, options, null);
    }

    @Override
    public List<SCommand> searchCommands(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.searchEntity(SCommand.class, options, null);
    }

    private void log(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder,
            final String callerClassName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.build();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), callerClassName, log);
        }
    }

    @Override
    public void start() throws SBonitaException {
        Map<String, CommandDeployment> commandDeployments = toCommandDeploymentMap(
                defaultCommandProvider.getDefaultCommands());
        Map<String, SCommand> availableSystemCommands = getAllAvailableSystemCommands();
        createAndUpdateCommands(commandDeployments, availableSystemCommands);
        deleteSystemCommandsNotPresentInDeployments(commandDeployments, availableSystemCommands);
    }

    private void createAndUpdateCommands(final Map<String, CommandDeployment> commandDeployements,
            final Map<String, SCommand> availableSystemCommands) throws SBonitaException {
        for (String commandName : commandDeployements.keySet()) {
            if (!availableSystemCommands.containsKey(commandName)) {
                createMissingCommand(commandDeployements.get(commandName));
            } else {
                SCommand sCommand = availableSystemCommands.get(commandName);
                updateExistingCommandIfNecessary(sCommand, commandDeployements.get(commandName));
            }
        }
    }

    private void updateExistingCommandIfNecessary(final SCommand command, final CommandDeployment commandDeployment)
            throws SCommandUpdateException {
        SCommandUpdateBuilderImpl updateBuilder = new SCommandUpdateBuilderImpl();
        if (!command.getDescription().equals(commandDeployment.getDescription())) {
            updateBuilder.updateDescription(commandDeployment.getDescription());
        }
        if (!command.getImplementation().equals(commandDeployment.getImplementation())) {
            updateBuilder.updateImplementation(commandDeployment.getImplementation());
        }
        EntityUpdateDescriptor updateDescriptor = updateBuilder.done();
        if (!updateDescriptor.getFields().isEmpty()) {
            if (log.isInfoEnabled()) {
                log.info("Updating system command. Before update: {}. After update: {}", command, commandDeployment);
            }
            update(command, updateDescriptor);
        }
    }

    private void createMissingCommand(final CommandDeployment commandDeployment)
            throws SCommandAlreadyExistsException,
            SCommandCreationException {
        log.debug("Creating missing system command: {}", commandDeployment);

        create(SCommand.builder()
                .name(commandDeployment.getName())
                .description(commandDeployment.getDescription())
                .implementation(commandDeployment.getImplementation())
                .isSystem(true).build());
    }

    private void deleteSystemCommandsNotPresentInDeployments(final Map<String, CommandDeployment> commandDeployments,
            final Map<String, SCommand> availableSystemCommands) throws SCommandDeletionException {

        for (String commandName : availableSystemCommands.keySet()) {
            if (!commandDeployments.containsKey(commandName)) {
                final SCommandLogBuilder logBuilder = getQueriableLog(ActionType.DELETED,
                        "Deleting command with name " + commandName);
                SCommand command = availableSystemCommands.get(commandName);
                if (log.isInfoEnabled()) {
                    log.info("The following system command is not used any more and will be deleted: {}", command);
                }
                delete(command, logBuilder);
            }
        }
    }

    private Map<String, SCommand> getAllAvailableSystemCommands() throws SBonitaReadException {
        Map<String, SCommand> commands = new HashMap<>();
        List<SCommand> currentPage;
        int fromIndex = 0;
        do {
            currentPage = getSystemCommands(fromIndex, fetchSize);
            commands.putAll(toCommandMap(getSystemCommands(fromIndex, fetchSize)));
            fromIndex += fetchSize;
        } while (currentPage.size() == fetchSize);
        return commands;
    }

    private List<SCommand> getSystemCommands(int fromIndex, int maxResults) throws SBonitaReadException {
        QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults,
                Collections.singletonList(new OrderByOption(SCommand.class, SCommand.ID,
                        OrderByType.ASC)),
                Collections.singletonList(new FilterOption(SCommand.class, SCommand.SYSTEM, true)), null);
        return searchCommands(queryOptions);
    }

    private Map<String, SCommand> toCommandMap(List<SCommand> commands) {
        Map<String, SCommand> map = new HashMap<>(commands.size());
        for (SCommand command : commands) {
            map.put(command.getName(), command);
        }
        return map;
    }

    private Map<String, CommandDeployment> toCommandDeploymentMap(List<CommandDeployment> commandDeployments) {
        Map<String, CommandDeployment> map = new HashMap<>(commandDeployments.size());
        for (CommandDeployment deployment : commandDeployments) {
            map.put(deployment.getName(), deployment);
        }
        return map;
    }

}
