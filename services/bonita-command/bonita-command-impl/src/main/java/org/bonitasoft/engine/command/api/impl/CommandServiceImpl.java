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

import java.util.List;

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
import org.bonitasoft.engine.command.model.SCommandBuilderFactory;
import org.bonitasoft.engine.command.model.SCommandCriterion;
import org.bonitasoft.engine.command.model.SCommandLogBuilder;
import org.bonitasoft.engine.command.model.SCommandLogBuilderFactory;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
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

    private final ReadPersistenceService persistenceService;

    private final Recorder recorder;

    private final EventService eventService;

    private final TechnicalLoggerService logger;

    private final QueriableLoggerService queriableLoggerService;

    public CommandServiceImpl(final ReadPersistenceService persistenceService, final Recorder recorder,
            final EventService eventService, final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService) {
        super();
        this.persistenceService = persistenceService;
        this.recorder = recorder;
        this.eventService = eventService;
        this.logger = logger;
        this.queriableLoggerService = queriableLoggerService;

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
                initiateLogBuilder(command.getId(), SQueriableLog.STATUS_OK, logBuilder, "create");
                if (isTraceEnable) {
                    logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "create"));
                }
            } catch (final SRecorderException re) {
                if (isTraceEnable) {
                    logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "create", re));
                }
                initiateLogBuilder(command.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "create");
                throw new SCommandCreationException(re);
            }
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
            initiateLogBuilder(command.getId(), SQueriableLog.STATUS_OK, logBuilder, "delete");
        } catch (final SRecorderException re) {
            if (isTraceEnable) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "delete", re));
            }
            initiateLogBuilder(command.getId(), SQueriableLog.STATUS_OK, logBuilder, "delete");
            throw new SCommandDeletionException(re);
        }
    }

    @Override
    public void deleteAll() throws SCommandDeletionException {
        final boolean isTraceEnable = logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE);
        if (isTraceEnable) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "deleteAll"));
        }
        List<SCommand> commands;
        do {
            try {
                commands = getAllCommands(0, 100, SCommandCriterion.NAME_ASC);
                for (final SCommand command : commands) {
                    delete(command.getName());
                }
            } catch (final SCommandGettingException scge) {
                throw new SCommandDeletionException(scge);
            } catch (final SCommandNotFoundException scnfe) {
                throw new SCommandDeletionException(scnfe);
            }
        } while (!commands.isEmpty());
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
            initiateLogBuilder(command.getId(), SQueriableLog.STATUS_OK, logBuilder, "update");
        } catch (final SRecorderException re) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "update", re));
            }
            initiateLogBuilder(command.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "update");
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
    public void delete(final long commandId) throws SCommandNotFoundException, SCommandDeletionException {
        final boolean isTraceEnable = logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE);
        if (isTraceEnable) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "delete"));
        }
        final SCommandLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "Deleting command with id " + commandId);
        final SCommand command = this.get(commandId);
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
            initiateLogBuilder(command.getId(), SQueriableLog.STATUS_OK, logBuilder, "delete");
        } catch (final SRecorderException re) {
            if (isTraceEnable) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "delete", re));
            }
            initiateLogBuilder(command.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "delete");
            throw new SCommandDeletionException(re);
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

    private void initiateLogBuilder(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String callerClassName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), callerClassName, log);
        }
    }

}
