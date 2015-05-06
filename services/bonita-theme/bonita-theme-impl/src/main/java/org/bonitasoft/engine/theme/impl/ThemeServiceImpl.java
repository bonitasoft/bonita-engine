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
package org.bonitasoft.engine.theme.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
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
import org.bonitasoft.engine.recorder.model.DeleteAllRecord;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.theme.ThemeService;
import org.bonitasoft.engine.theme.builder.SThemeBuilderFactory;
import org.bonitasoft.engine.theme.builder.impl.SThemeLogBuilderImpl;
import org.bonitasoft.engine.theme.exception.SRestoreThemeException;
import org.bonitasoft.engine.theme.exception.SThemeCreationException;
import org.bonitasoft.engine.theme.exception.SThemeDeletionException;
import org.bonitasoft.engine.theme.exception.SThemeNotFoundException;
import org.bonitasoft.engine.theme.exception.SThemeReadException;
import org.bonitasoft.engine.theme.exception.SThemeUpdateException;
import org.bonitasoft.engine.theme.model.STheme;
import org.bonitasoft.engine.theme.model.SThemeType;
import org.bonitasoft.engine.theme.persistence.SelectDescriptorBuilder;

/**
 * @author Celine Souchet
 */
public class ThemeServiceImpl implements ThemeService {

    private final ReadPersistenceService persistenceService;

    private final Recorder recorder;

    private final EventService eventService;

    private final TechnicalLoggerService logger;

    private final QueriableLoggerService queriableLoggerService;

    public ThemeServiceImpl(final ReadPersistenceService persistenceService, final Recorder recorder, final EventService eventService,
            final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService) {
        super();
        this.persistenceService = persistenceService;
        this.recorder = recorder;
        this.eventService = eventService;
        this.logger = logger;
        this.queriableLoggerService = queriableLoggerService;
    }

    @Override
    public STheme createTheme(final STheme theme) throws SThemeCreationException {
        final String methodName = "createTheme";
        logBeforeMethod(methodName);
        final SThemeLogBuilderImpl logBuilder = getSThemeLog(ActionType.CREATED, "Adding a new theme");
        final InsertRecord insertRecord = new InsertRecord(theme);
        SInsertEvent insertEvent = null;
        if (eventService.hasHandlers(THEME, EventActionType.CREATED)) {
            insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(THEME).setObject(theme).done();
        }
        try {
            recorder.recordInsert(insertRecord, insertEvent);
            log(theme.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
            logAfterMethod(methodName);
            return theme;
        } catch (final SRecorderException re) {
            logOnExceptionMethod(methodName, re);
            log(theme.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SThemeCreationException(re);
        }
    }

    @Override
    public void deleteTheme(final long id) throws SThemeNotFoundException, SThemeDeletionException {
        final String methodName = "deleteTheme";
        logBeforeMethod(methodName);
        STheme theme;
        try {
            theme = getTheme(id);
        } catch (final SThemeReadException e) {
            throw new SThemeDeletionException(e);
        }
        deleteTheme(theme);
        logAfterMethod(methodName);
    }

    @Override
    public void deleteTheme(final STheme theme) throws SThemeDeletionException {
        final String methodName = "deleteTheme";
        logBeforeMethod(methodName);
        final SThemeLogBuilderImpl logBuilder = getSThemeLog(ActionType.DELETED, "Deleting theme");
        final DeleteRecord deleteRecord = new DeleteRecord(theme);
        SDeleteEvent deleteEvent = null;
        if (eventService.hasHandlers(THEME, EventActionType.DELETED)) {
            deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(THEME).setObject(theme).done();
        }
        try {
            recorder.recordDelete(deleteRecord, deleteEvent);
            log(theme.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
            logAfterMethod(methodName);
        } catch (final SRecorderException re) {
            logOnExceptionMethod(methodName, re);
            log(theme.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SThemeDeletionException(re);
        }
    }

    @Override
    public void restoreDefaultTheme(final SThemeType type) throws SRestoreThemeException {
        try {
            final FilterOption defaultFilter = new FilterOption(STheme.class, SThemeBuilderFactory.IS_DEFAULT, false);
            final FilterOption typeFilter = new FilterOption(STheme.class, SThemeBuilderFactory.TYPE, type.name());
            final List<FilterOption> filterOptions = new ArrayList<FilterOption>();
            filterOptions.add(defaultFilter);
            filterOptions.add(typeFilter);
            final DeleteAllRecord record = new DeleteAllRecord(STheme.class, filterOptions);
            recorder.recordDeleteAll(record);
        } catch (final SRecorderException e) {
            throw new SRestoreThemeException("Can't delete custom themes for type = " + type, e);
        }
    }

    @Override
    public STheme getTheme(final SThemeType type, final boolean isDefault) throws SThemeNotFoundException, SThemeReadException {
        final SelectOneDescriptor<STheme> selectDescriptor = SelectDescriptorBuilder.getTheme(type, isDefault);
        try {
            final STheme theme = persistenceService.selectOne(selectDescriptor);
            if (theme == null) {
                throw new SThemeNotFoundException(type, isDefault);
            }
            return theme;
        } catch (final SBonitaReadException e) {
            throw new SThemeReadException(e);
        }
    }

    @Override
    public STheme getTheme(final long id) throws SThemeNotFoundException, SThemeReadException {
        final String methodName = "getTheme";
        logBeforeMethod(methodName);
        try {
            final SelectByIdDescriptor<STheme> descriptor = SelectDescriptorBuilder.getElementById(STheme.class, "Theme", id);
            final STheme theme = persistenceService.selectById(descriptor);
            if (theme == null) {
                throw new SThemeNotFoundException(id);
            }
            logAfterMethod(methodName);
            return theme;
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod(methodName, e);
            throw new SThemeReadException(e);
        }
    }

    @Override
    public STheme getLastModifiedTheme(final SThemeType type) throws SThemeNotFoundException, SThemeReadException {
        final SelectOneDescriptor<STheme> selectDescriptor = SelectDescriptorBuilder.getLastModifiedTheme(type);
        try {
            final STheme theme = persistenceService.selectOne(selectDescriptor);
            if (theme == null) {
                throw new SThemeNotFoundException(type);
            }
            return theme;
        } catch (final SBonitaReadException e) {
            throw new SThemeReadException(e);
        }
    }

    @Override
    public STheme updateTheme(final STheme sTheme, final EntityUpdateDescriptor descriptor) throws SThemeUpdateException {
        final String methodName = "updateTheme";
        logBeforeMethod(methodName);
        NullCheckingUtil.checkArgsNotNull(sTheme);
        final SThemeLogBuilderImpl logBuilder = getSThemeLog(ActionType.UPDATED, "Updating theme");
        final STheme oldUser = BuilderFactory.get(SThemeBuilderFactory.class).createNewInstance(sTheme).done();
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(sTheme, descriptor);
        SUpdateEvent updateEvent = null;
        if (eventService.hasHandlers(THEME, EventActionType.UPDATED)) {
            updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(THEME).setObject(sTheme).done();
            updateEvent.setOldObject(oldUser);
        }
        try {
            recorder.recordUpdate(updateRecord, updateEvent);
            log(sTheme.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
            logAfterMethod(methodName);
        } catch (final SRecorderException re) {
            logOnExceptionMethod(methodName, re);
            log(sTheme.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SThemeUpdateException(re);
        }
        return sTheme;
    }

    @Override
    public long getNumberOfThemes(final QueryOptions queryOptions) throws SBonitaReadException {
        try {
            final Map<String, Object> parameters = Collections.emptyMap();
            return persistenceService.getNumberOfEntities(STheme.class, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaReadException(e);
        }
    }

    @Override
    public List<STheme> searchThemes(final QueryOptions queryOptions) throws SBonitaReadException {
        try {
            final Map<String, Object> parameters = Collections.emptyMap();
            return persistenceService.searchEntity(STheme.class, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaReadException(e);
        }
    }

    private void logBeforeMethod(final String methodName) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), methodName));
        }
    }

    private void logAfterMethod(final String methodName) {
        final Class<? extends ThemeServiceImpl> thisClass = this.getClass();
        if (logger.isLoggable(thisClass, TechnicalLogSeverity.TRACE)) {
            logger.log(thisClass, TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(thisClass, methodName));
        }
    }

    private void logOnExceptionMethod(final String methodName, final SBonitaException e) {
        final Class<? extends ThemeServiceImpl> thisClass = this.getClass();
        if (logger.isLoggable(thisClass, TechnicalLogSeverity.TRACE)) {
            logger.log(thisClass, TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(thisClass, methodName, e));
        }
    }

    private SThemeLogBuilderImpl getSThemeLog(final ActionType actionType, final String message) {
        final SThemeLogBuilderImpl logBuilder = new SThemeLogBuilderImpl();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private void log(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String callerMethodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), callerMethodName, log);
        }
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

	@Override
	public void start() throws SBonitaException {
		try {
			new ThemeServiceStartupHelper(this).createDefaultThemes();
		} catch (IOException e) {
			throw new SBonitaRuntimeException("Failed to start theme service due to: "+ e.getMessage(), e);
		}
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
