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
package org.bonitasoft.engine.command.api.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.bonitasoft.engine.command.model.SCommandBuilder;
import org.bonitasoft.engine.command.model.SCommandBuilderFactory;
import org.bonitasoft.engine.command.model.SCommandBuilderFactoryImpl;
import org.bonitasoft.engine.command.model.SCommandCriterion;
import org.bonitasoft.engine.command.model.SCommandLogBuilder;
import org.bonitasoft.engine.command.model.SCommandLogBuilderFactory;
import org.bonitasoft.engine.command.model.SCommandUpdateBuilderImpl;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
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
public class CommandServiceImpl implements CommandService {

    public static final int FETCH_SIZE = 1000;
    private final ReadPersistenceService persistenceService;

    private final Recorder recorder;

    private final EventService eventService;

    private final TechnicalLoggerService logger;

    private final QueriableLoggerService queriableLoggerService;
    private final CommandProvider defaultCommandProvider;
    private final int fetchSize;

    public CommandServiceImpl(final ReadPersistenceService persistenceService, final Recorder recorder,
            final EventService eventService, final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService,
            CommandProvider defaultCommandProvider) {
        this(persistenceService, recorder, eventService, logger, queriableLoggerService, defaultCommandProvider, FETCH_SIZE);
    }

    public CommandServiceImpl(final ReadPersistenceService persistenceService, final Recorder recorder,
            final EventService eventService, final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService,
            CommandProvider defaultCommandProvider, int fetchSize) {
        super();
        this.persistenceService = persistenceService;
        this.recorder = recorder;
        this.eventService = eventService;
        this.logger = logger;
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
        final boolean isTraceEnable = logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE);
        if (isTraceEnable) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "create"));
        }
        try {
            this.get(command.getName());
            throw new SCommandAlreadyExistsException("Command '" + command.getName() + "' already exists");
        } catch (final SCommandNotFoundException scmfe) {
            final SCommandLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Creating a new command with name " + command.getName());
            final InsertRecord insertRecord = new InsertRecord(command);

            SInsertEvent insertEvent = null;
            if (eventService.hasHandlers(COMMAND, EventActionType.CREATED)) {
                insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(COMMAND).setObject(command).done();
            }
            try {
                recorder.recordInsert(insertRecord, insertEvent);
                log(command.getId(), SQueriableLog.STATUS_OK, logBuilder, "create");
                if (isTraceEnable) {
                    logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "create"));
                }
            } catch (final SRecorderException re) {
                if (isTraceEnable) {
                    logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "create", re));
                }
                log(command.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "create");
                throw new SCommandCreationException(re);
            }
        }
    }

    @Override
    public void delete(final long commandId) throws SCommandNotFoundException, SCommandDeletionException {
        final boolean isTraceEnable = logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE);
        if (isTraceEnable) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "delete"));
        }
        final SCommandLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "Deleting command with id " + commandId);
        final SCommand command = this.get(commandId);
        delete(command, logBuilder);
    }

    protected void delete(final SCommand command, final SCommandLogBuilder logBuilder) throws SCommandDeletionException {
        final boolean isTraceEnable = logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE);
        final DeleteRecord deleteRecord = new DeleteRecord(command);
        SDeleteEvent deleteEvent = null;
        if (eventService.hasHandlers(COMMAND, EventActionType.DELETED)) {
            deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(COMMAND).setObject(command).done();
        }
        try {
            recorder.recordDelete(deleteRecord, deleteEvent);
            if (isTraceEnable) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "delete"));
            }
            log(command.getId(), SQueriableLog.STATUS_OK, logBuilder, "delete");
        } catch (final SRecorderException re) {
            if (isTraceEnable) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "delete", re));
            }
            log(command.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "delete");
            throw new SCommandDeletionException(re);
        }
    }

    @Override
    public void delete(final String commandName) throws SCommandNotFoundException, SCommandDeletionException {
        final SCommandLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "Deleting command with name " + commandName);
        final boolean isTraceEnable = logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE);
        if (isTraceEnable) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "delete"));
        }
        final SCommand command = this.get(commandName);
        delete(command, logBuilder);
    }

    @Override
    public void deleteAll() throws SCommandDeletionException {
        final boolean isTraceEnable = logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE);
        if (isTraceEnable) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "deleteAll"));
        }
        List<SCommand> commands;
        try {
            do {
                commands = getAllCommands(0, 1000, SCommandCriterion.NAME_ASC);
                for (final SCommand command : commands) {
                    final SCommandLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "Deleting command with name " + command.getName());
                    delete(command, logBuilder);
                }
            } while (!commands.isEmpty());
        } catch (final SCommandGettingException scge) {
            throw new SCommandDeletionException(scge);
        }
        if (isTraceEnable) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "deleteAll"));
        }
    }

    @Override
    public SCommand get(final String commandName) throws SCommandNotFoundException {
        final boolean trace = logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE);
        try {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "get"));
            }
            final SelectOneDescriptor<SCommand> descriptor = SelectDescriptorBuilder.getCommandByName(commandName);
            final SCommand scommand = persistenceService.selectOne(descriptor);
            if (scommand == null) {
                throw new SCommandNotFoundException("command '" + commandName + "' does not exist");
            }
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "get"));
            }
            return scommand;
        } catch (final SBonitaReadException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "get", e));
            }
            throw new SCommandNotFoundException("Cannot get command: " + commandName, e);
        }
    }

    @Override
    public List<SCommand> getAllCommands(final int startIndex, final int maxResults, final SCommandCriterion sCommandCriterion) throws SCommandGettingException {
        final boolean trace = logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE);
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getAllCommands"));
        }
        String field = null;
        OrderByType orderByType = null;
        switch (sCommandCriterion) {
            case NAME_ASC:
                orderByType = OrderByType.ASC;
                field = "name";
                break;
            case NAME_DESC:
                orderByType = OrderByType.DESC;
                field = "name";
                break;
            default:
                throw new IllegalStateException();
        }
        try {
            final SelectListDescriptor<SCommand> descriptor = SelectDescriptorBuilder.getCommands(field, orderByType, startIndex, maxResults);
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getAllCommands"));
            }
            return persistenceService.selectList(descriptor);
        } catch (final SBonitaReadException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getAllCommands", e));
            }
            throw new SCommandGettingException("can't get the commands", e);
        }
    }

    @Override
    public void update(final SCommand command, final EntityUpdateDescriptor updateDescriptor) throws SCommandUpdateException {
        final boolean trace = logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE);
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "update"));
        }
        final SCommandLogBuilder logBuilder = getQueriableLog(ActionType.UPDATED, "Updating command with name " + command.getName());

        final SCommandBuilderFactory fact = BuilderFactory.get(SCommandBuilderFactory.class);
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(command, updateDescriptor);
        SUpdateEvent updateEvent = null;
        if (eventService.hasHandlers(COMMAND, EventActionType.UPDATED)) {
            final SCommand oldCommand = fact.createNewInstance(command).done();
            updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(COMMAND).setObject(command).done();
            updateEvent.setOldObject(oldCommand);
        }
        try {
            recorder.recordUpdate(updateRecord, updateEvent);
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "update"));
            }
            log(command.getId(), SQueriableLog.STATUS_OK, logBuilder, "update");
        } catch (final SRecorderException re) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "update", re));
            }
            log(command.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "update");
            throw new SCommandUpdateException(re);
        }
    }

    @Override
    public List<SCommand> getUserCommands(final int startIndex, final int maxResults, final SCommandCriterion sCommandCriterion)
            throws SCommandGettingException {
        final boolean trace = logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE);
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getUserCommands"));
        }
        String field = null;
        OrderByType orderByType = null;
        switch (sCommandCriterion) {
            case NAME_ASC:
                orderByType = OrderByType.ASC;
                field = "name";
                break;
            case NAME_DESC:
                orderByType = OrderByType.DESC;
                field = "name";
                break;
            default:
                throw new IllegalStateException();
        }
        try {
            final SelectListDescriptor<SCommand> descriptor = SelectDescriptorBuilder.getUserCommands(field, orderByType, startIndex, maxResults);
            final List<SCommand> selectList = persistenceService.selectList(descriptor);
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getUserCommands"));
            }
            return selectList;
        } catch (final SBonitaReadException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getUserCommands", e));
            }
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
        logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getNumberOfCommands"));
        try {
            final long number = persistenceService.getNumberOfEntities(SCommand.class, options, null);
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfCommands"));
            return number;
        } catch (final SBonitaReadException bre) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfCommands", bre));
            throw new SBonitaReadException(bre);
        }
    }

    @Override
    public List<SCommand> searchCommands(final QueryOptions options) throws SBonitaReadException {
        final boolean trace = logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE);
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "searchCommands"));
        }
        try {
            final List<SCommand> commands = persistenceService.searchEntity(SCommand.class, options, null);
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "searchCommands"));
            }
            return commands;
        } catch (final SBonitaReadException bre) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "searchCommands", bre));
            }
            throw new SBonitaReadException(bre);
        }
    }

    private void log(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String callerClassName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), callerClassName, log);
        }
    }

    @Override
    public void start() throws SBonitaException {
        Map<String, CommandDeployment> commandDeployments = toCommandDeploymentMap(defaultCommandProvider.getDefaultCommands());
        Map<String, SCommand> availableSystemCommands = getAllAvailableSystemCommands();
        createAndUpdateCommands(commandDeployments, availableSystemCommands);
        deleteSystemCommandsNotPresentInDeployments(commandDeployments, availableSystemCommands);
    }

    private void createAndUpdateCommands(final Map<String, CommandDeployment> commandDeployements,
            final Map<String, SCommand> availableSystemCommands) throws SBonitaException {
        boolean infoEnabled = logger.isLoggable(this.getClass(), TechnicalLogSeverity.INFO);
        for (String commandName : commandDeployements.keySet()) {
            if (!availableSystemCommands.keySet().contains(commandName)) {
                createMissingCommand(commandDeployements.get(commandName), infoEnabled);
            } else {
                SCommand sCommand = availableSystemCommands.get(commandName);
                updateExistingCommandIfNecessary(sCommand, commandDeployements.get(commandName), infoEnabled);
            }
        }
    }

    private void updateExistingCommandIfNecessary(final SCommand command, final CommandDeployment commandDeployment, final boolean infoEnabled)
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
            if (infoEnabled) {
                logger.log(getClass(), TechnicalLogSeverity.INFO, "Updating system command. Before update: " + command + ". After update: " + commandDeployment);
            }
            update(command, updateDescriptor);
        }
    }

    private void createMissingCommand(final CommandDeployment commandDeployment, final boolean infoEnabled) throws SCommandAlreadyExistsException,
            SCommandCreationException {
        if (infoEnabled) {
            logger.log(this.getClass(), TechnicalLogSeverity.INFO, "Creating missing system command: " + commandDeployment);
        }
        SCommandBuilder commandBuilder = new SCommandBuilderFactoryImpl().createNewInstance(commandDeployment.getName(),
                commandDeployment.getDescription(), commandDeployment.getImplementation());
        commandBuilder.setSystem(true);
        create(commandBuilder.done());
    }

    private void deleteSystemCommandsNotPresentInDeployments(final Map<String, CommandDeployment> commandDeployments,
            final Map<String, SCommand> availableSystemCommands) throws SCommandDeletionException {
        boolean infoEnabled = logger.isLoggable(getClass(), TechnicalLogSeverity.INFO);
        for (String commandName : availableSystemCommands.keySet()) {
            if (!commandDeployments.keySet().contains(commandName)) {
                final SCommandLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "Deleting command with name " + commandName);
                SCommand command = availableSystemCommands.get(commandName);
                if (infoEnabled) {
                    logger.log(getClass(), TechnicalLogSeverity.INFO, "The following system command is not used any more and will be deleted: " + command);
                }
                delete(command, logBuilder);
            }
        }
    }

    private Map<String, SCommand> getAllAvailableSystemCommands() throws SBonitaReadException {
        Map<String, SCommand> commands = new HashMap<String, SCommand>();
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
        SCommandBuilderFactoryImpl factory = new SCommandBuilderFactoryImpl();
        QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults, Collections.singletonList(new OrderByOption(SCommand.class, factory.getIdKey(),
                OrderByType.ASC)), Collections.singletonList(new FilterOption(SCommand.class, factory.getSystemKey(), true)), null);
        return searchCommands(queryOptions);
    }

    private Map<String, SCommand> toCommandMap(List<SCommand> commands) {
        Map<String, SCommand> map = new HashMap<String, SCommand>(commands.size());
        for (SCommand command : commands) {
            map.put(command.getName(), command);
        }
        return map;
    }

    private Map<String, CommandDeployment> toCommandDeploymentMap(List<CommandDeployment> commandDeployments) {
        Map<String, CommandDeployment> map = new HashMap<String, CommandDeployment>(commandDeployments.size());
        for (CommandDeployment deployment : commandDeployments) {
            map.put(deployment.getName(), deployment);
        }
        return map;
    }

    @Override
    public void stop() throws SBonitaException {
        // nothing to do
    }

    @Override
    public void pause() throws SBonitaException {
        // nothing to do
    }

    @Override
    public void resume() throws SBonitaException {
        // nothing to do
    }
}
