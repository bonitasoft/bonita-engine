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
package org.bonitasoft.engine.external.identity.mapping.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.external.identity.mapping.ExternalIdentityMappingService;
import org.bonitasoft.engine.external.identity.mapping.SExternalIdentityMappingCreationException;
import org.bonitasoft.engine.external.identity.mapping.SExternalIdentityMappingDeletionException;
import org.bonitasoft.engine.external.identity.mapping.SExternalIdentityMappingNotFoundException;
import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMapping;
import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMappingLogBuilder;
import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMappingLogBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
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
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class ExternalIdentityMappingServiceImpl implements ExternalIdentityMappingService {

    private static final String EXTERNAL_IDENTITY_MAPPING = "EXTERNAL_IDENTITY_MAPPING";

    private final ReadPersistenceService persistenceService;

    private final Recorder recorder;

    private final EventService eventService;

    private final TechnicalLoggerService logger;

    private final QueriableLoggerService queriableLoggerService;

    public ExternalIdentityMappingServiceImpl(final ReadPersistenceService persistenceService, final Recorder recorder, final EventService eventService,
            final TechnicalLoggerService logger,
            final QueriableLoggerService queriableLoggerService) {
        this.persistenceService = persistenceService;
        this.recorder = recorder;
        this.eventService = eventService;
        this.logger = logger;
        this.queriableLoggerService = queriableLoggerService;
    }

    @Override
    public SExternalIdentityMapping createExternalIdentityMapping(final SExternalIdentityMapping externalIdentityMapping)
            throws SExternalIdentityMappingCreationException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "createExternalIdentityMapping"));
        }
        final SExternalIdentityMappingLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Adding a new ExternalIdentityMapping for external id "
                + externalIdentityMapping.getExternalId());
        try {
            final InsertRecord insertRecord = new InsertRecord(externalIdentityMapping);
            SInsertEvent insertEvent = null;
            if (eventService.hasHandlers(EXTERNAL_IDENTITY_MAPPING, EventActionType.CREATED)) {
                insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(EXTERNAL_IDENTITY_MAPPING)
                        .setObject(externalIdentityMapping).done();
            }
            recorder.recordInsert(insertRecord, insertEvent);
            log(externalIdentityMapping.getId(), SQueriableLog.STATUS_OK, logBuilder, "createExternalIdentityMapping");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "createExternalIdentityMapping"));
            }
            return externalIdentityMapping;
        } catch (final SRecorderException re) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "createExternalIdentityMapping", re));
            }
            log(externalIdentityMapping.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createExternalIdentityMapping");
            throw new SExternalIdentityMappingCreationException(re);
        }
    }

    @Override
    public SExternalIdentityMapping getExternalIdentityMappingById(final long mappingId) throws SExternalIdentityMappingNotFoundException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getExternalIdentityMappingById"));
        }
        final SelectByIdDescriptor<SExternalIdentityMapping> selectByIdDescriptor = SelectDescriptorBuilder.getExternalIdentityMappingById(mappingId);
        try {
            final SExternalIdentityMapping mapping = persistenceService.selectById(selectByIdDescriptor);
            if (mapping == null) {
                throw new SExternalIdentityMappingNotFoundException(mappingId + " does not refer to any external identity mapping");
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getExternalIdentityMappingById"));
            }
            return mapping;
        } catch (final SBonitaReadException bre) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getExternalIdentityMappingById", bre));
            }
            throw new SExternalIdentityMappingNotFoundException(bre);
        }
    }

    @Override
    public SExternalIdentityMapping getExternalIdentityMappingForUser(final long mappingId) throws SExternalIdentityMappingNotFoundException {
        return getExternalIdentityMappingById(mappingId, "ForUser", "user");
    }

    @Override
    public SExternalIdentityMapping getExternalIdentityMappingById(final long mappingId, final String suffix, final String messageSuffix)
            throws SExternalIdentityMappingNotFoundException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getExternalIdentityMappingById"));
        }
        final SelectByIdDescriptor<SExternalIdentityMapping> selectByIdDescriptor = SelectDescriptorBuilder.getExternalIdentityMappingById(mappingId, suffix);
        try {
            final SExternalIdentityMapping externalIdentityMapping = persistenceService.selectById(selectByIdDescriptor);
            if (externalIdentityMapping == null) {
                throw new SExternalIdentityMappingNotFoundException(mappingId + " does not refer to any external identity mapping associated to a "
                        + messageSuffix);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getExternalIdentityMappingById"));
            }
            return externalIdentityMapping;
        } catch (final SBonitaReadException bre) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getExternalIdentityMappingById", bre));
            }
            throw new SExternalIdentityMappingNotFoundException(bre);
        }
    }

    @Override
    public SExternalIdentityMapping getExternalIdentityMappingForGroup(final long mappingId) throws SExternalIdentityMappingNotFoundException {
        return getExternalIdentityMappingById(mappingId, "ForGroup", "group");
    }

    @Override
    public SExternalIdentityMapping getExternalIdentityMappingForRole(final long mappingId) throws SExternalIdentityMappingNotFoundException {
        return getExternalIdentityMappingById(mappingId, "ForRole", "role");
    }

    @Override
    public SExternalIdentityMapping getExternalIdentityMappingForRoleAndGroup(final long mappingId) throws SExternalIdentityMappingNotFoundException {
        return getExternalIdentityMappingById(mappingId, "ForRoleAndGroup", "role and group");
    }

    @Override
    public void deleteExternalIdentityMapping(final long mappingId) throws SExternalIdentityMappingNotFoundException, SExternalIdentityMappingDeletionException {
        final SExternalIdentityMapping sExternalIdentityMapping = getExternalIdentityMappingById(mappingId);
        deleteExternalIdentityMapping(sExternalIdentityMapping);
    }

    @Override
    public void deleteExternalIdentityMapping(final SExternalIdentityMapping externalIdentityMapping) throws SExternalIdentityMappingDeletionException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "deleteExternalIdentityMapping"));
        }
        SDeleteEvent deleteEvent = null;
        if (eventService.hasHandlers(EXTERNAL_IDENTITY_MAPPING, EventActionType.DELETED)) {
            deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(EXTERNAL_IDENTITY_MAPPING)
                    .setObject(externalIdentityMapping).done();
        }
        final DeleteRecord record = new DeleteRecord(externalIdentityMapping);
        final SExternalIdentityMappingLogBuilder queriableLog = getQueriableLog(ActionType.DELETED, "deleting external identity mapping");
        try {
            recorder.recordDelete(record, deleteEvent);
            log(externalIdentityMapping.getId(), SQueriableLog.STATUS_OK, queriableLog, "deleteExternalIdentityMapping");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "deleteExternalIdentityMapping"));
            }
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "deleteExternalIdentityMapping", e));
            }
            log(externalIdentityMapping.getId(), SQueriableLog.STATUS_FAIL, queriableLog, "deleteExternalIdentityMapping");
            throw new SExternalIdentityMappingDeletionException("Can't delete process external identity mapping " + externalIdentityMapping, e);
        }
    }

    @Override
    public void deleteAllExternalIdentityMappings() throws SExternalIdentityMappingDeletionException {
        try {
            final DeleteAllRecord record = new DeleteAllRecord(SExternalIdentityMapping.class, null);
            recorder.recordDeleteAll(record);
        } catch (final SRecorderException e) {
            throw new SExternalIdentityMappingDeletionException("Can't delete all process external identity mappings ", e);
        }
    }

    private SExternalIdentityMappingLogBuilder getQueriableLog(final ActionType actionType, final String message) {
        final SExternalIdentityMappingLogBuilder logBuilder = BuilderFactory.get(SExternalIdentityMappingLogBuilderFactory.class).createNewInstance();
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
    public long getNumberOfExternalIdentityMappingsForUser(final String kind, final long userId, final String externalId, final QueryOptions searchOptions,
            final String querySuffix) throws SBonitaReadException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getNumberOfExternalIdentityMappingsForUser"));
        }
        try {
            final Map<String, Object> parameters = new HashMap<String, Object>(2);
            parameters.put("kind", kind);
            parameters.put("userId", userId);
            parameters.put("externalId", externalId);
            final long number = persistenceService.getNumberOfEntities(SExternalIdentityMapping.class, querySuffix, searchOptions, parameters);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE,
                        LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfExternalIdentityMappingsForUser"));
            }
            return number;
        } catch (final SBonitaReadException bre) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE,
                        LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfExternalIdentityMappingsForUser", bre));
            }
            throw new SBonitaReadException(bre);
        }
    }

    @Override
    public List<SExternalIdentityMapping> searchExternalIdentityMappingsForUser(final String kind, final long userId, final String externalId,
            final QueryOptions queryOptions, final String querySuffix) throws SBonitaReadException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "searchExternalIdentityMappingsForUser"));
        }
        final Map<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put("kind", kind);
        parameters.put("userId", userId);
        parameters.put("externalId", externalId);
        final List<SExternalIdentityMapping> listSExternalIdentityMapping = persistenceService.searchEntity(SExternalIdentityMapping.class, querySuffix,
                queryOptions, parameters);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "searchExternalIdentityMappingsForUser"));
        }
        return listSExternalIdentityMapping;
    }

    @Override
    public long getNumberOfExternalIdentityMappings(final String kind, final QueryOptions searchOptions, final String querySuffix)
            throws SBonitaReadException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getNumberOfExternalIdentityMappings"));
        }
        try {
            final Map<String, Object> parameters = new HashMap<String, Object>(1);
            parameters.put("kind", kind);
            final long number = persistenceService.getNumberOfEntities(SExternalIdentityMapping.class, querySuffix, searchOptions, parameters);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfExternalIdentityMappings"));
            }
            return number;
        } catch (final SBonitaReadException bre) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE,
                        LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfExternalIdentityMappings", bre));
            }
            throw new SBonitaReadException(bre);
        }
    }

    @Override
    public List<SExternalIdentityMapping> searchExternalIdentityMappings(final String kind, final QueryOptions queryOptions, final String querySuffix)
            throws SBonitaReadException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "searchExternalIdentityMappings"));
        }
        try {
            final Map<String, Object> parameters = new HashMap<String, Object>(1);
            parameters.put("kind", kind);
            final List<SExternalIdentityMapping> listSExternalIdentityMappings = persistenceService.searchEntity(SExternalIdentityMapping.class, querySuffix,
                    queryOptions, parameters);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "searchExternalIdentityMappings"));
            }
            return listSExternalIdentityMappings;
        } catch (final SBonitaReadException bre) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "searchExternalIdentityMappings", bre));
            }
            throw new SBonitaReadException(bre);
        }
    }

    @Override
    public List<SExternalIdentityMapping> searchExternalIdentityMappings(final String kind, final String externalId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "searchExternalIdentityMappings"));
        }
        try {
            final Map<String, Object> parameters = new HashMap<String, Object>(2);
            parameters.put("kind", kind);
            parameters.put("externalId", externalId);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "searchExternalIdentityMappings"));
            }
            return persistenceService.searchEntity(SExternalIdentityMapping.class, null, queryOptions, parameters);
        } catch (final SBonitaReadException bre) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "searchExternalIdentityMappings", bre));
            }
            throw new SBonitaReadException(bre);
        }
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

}
