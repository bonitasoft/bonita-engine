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
package org.bonitasoft.engine.core.migration.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.core.migration.MigrationPlanService;
import org.bonitasoft.engine.core.migration.exceptions.SInvalidMigrationPlanException;
import org.bonitasoft.engine.core.migration.exceptions.SMigrationPlanCreationException;
import org.bonitasoft.engine.core.migration.exceptions.SMigrationPlanDeletionException;
import org.bonitasoft.engine.core.migration.exceptions.SMigrationPlanNotFoundException;
import org.bonitasoft.engine.core.migration.exceptions.SPrepareForMigrationFailedException;
import org.bonitasoft.engine.core.migration.model.SMigrationPlan;
import org.bonitasoft.engine.core.migration.model.SMigrationPlanDescriptor;
import org.bonitasoft.engine.core.migration.model.impl.SMigrationPlanDescriptorImpl;
import org.bonitasoft.engine.core.migration.model.impl.SMigrationPlanDescriptorLogBuilderImpl;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
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
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.xml.ElementBindingsFactory;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.ParserFactory;
import org.bonitasoft.engine.xml.SValidationException;
import org.bonitasoft.engine.xml.SXMLParseException;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class MigrationPlanServiceImpl implements MigrationPlanService {

    protected static final String MIGRATION_PLAN_CACHE_NAME = "MIGRATION_PLAN_CACHE_NAME";

    protected final Parser parser;

    protected final Recorder recorder;

    protected final ReadPersistenceService persistenceService;

    protected final EventService eventService;

    protected final CacheService cacheService;

    protected final ProcessInstanceService processInstanceService;

    protected final QueriableLoggerService queriableLoggerService;

    public MigrationPlanServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceService, final EventService eventService,
            final ParserFactory parserFactory, final CacheService cacheService, final ProcessInstanceService processInstanceService,
            final QueriableLoggerService queriableLoggerService) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.eventService = eventService;
        this.cacheService = cacheService;
        this.processInstanceService = processInstanceService;
        this.queriableLoggerService = queriableLoggerService;
        final ElementBindingsFactory bindingsFactory = new SMigrationPlanElementBindings();
        parser = parserFactory.createParser(bindingsFactory);
        // the schema is in common-api
        final InputStream schemaStream = this.getClass().getResourceAsStream("/org/bonitasoft/engine/bpm/migration/MigrationPlan.xsd");
        try {
            parser.setSchema(schemaStream);
        } catch (final Exception e) {
            throw new BonitaRuntimeException("Unable to configure Migration Plan Service", e);
        } finally {
            try {
                schemaStream.close();
            } catch (final IOException e) {
                throw new BonitaRuntimeException(e);
            }
        }
    }

    @Override
    public SMigrationPlanDescriptor importMigrationPlan(final byte[] content) throws SMigrationPlanCreationException, SInvalidMigrationPlanException {
        final String contentAsString = new String(content);
        StringReader xmlReader = new StringReader(contentAsString);
        final SMigrationPlan objectFromXML;
        final SMigrationPlanDescriptor descriptor;
        try {
            parser.validate(xmlReader);
            xmlReader = new StringReader(contentAsString);
            objectFromXML = (SMigrationPlan) parser.getObjectFromXML(xmlReader);
            descriptor = new SMigrationPlanDescriptorImpl(objectFromXML, content);
        } catch (final SValidationException e) {
            throw new SInvalidMigrationPlanException(e);
        } catch (final IOException e) {
            throw new SMigrationPlanCreationException(e);
        } catch (final SXMLParseException e) {
            throw new SInvalidMigrationPlanException(e);
        }
        final InsertRecord insertRecord = new InsertRecord(descriptor);
        final SMigrationPlanDescriptorLogBuilderImpl logBuilder = getQueriableLog(ActionType.CREATED, ADDED_A_NEW_MIGRATION_PLAN);
        try {
            final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(MIGRATION_PLAN_DESCRIPTOR)
                    .setObject(descriptor)
                    .done();
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(insertRecord.getEntity().getId(), SQueriableLog.STATUS_OK, logBuilder, "importMigrationPlan");
            cacheService.store(MIGRATION_PLAN_CACHE_NAME, String.valueOf(descriptor.getId()), objectFromXML);
            return descriptor;
        } catch (final SRecorderException e) {
            initiateLogBuilder(insertRecord.getEntity().getId(), SQueriableLog.STATUS_FAIL, logBuilder, "importMigrationPlan");
            throw new SMigrationPlanCreationException(e);
        } catch (final SCacheException e) {
            throw new SMigrationPlanCreationException(e);
        }
    }

    private SMigrationPlanDescriptorLogBuilderImpl getQueriableLog(final ActionType actionType, final String message) {
        final SMigrationPlanDescriptorLogBuilderImpl logBuilder = new SMigrationPlanDescriptorLogBuilderImpl();
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
    public byte[] exportMigrationPlan(final long id) throws SBonitaReadException, SMigrationPlanNotFoundException {
        final SMigrationPlanDescriptor selectById = getMigrationPlanDescriptor(id);
        return selectById.getMigrationPlanContent();
    }

    @Override
    public SMigrationPlanDescriptor getMigrationPlanDescriptor(final long id) throws SBonitaReadException, SMigrationPlanNotFoundException {
        SMigrationPlanDescriptor selectById;
        selectById = persistenceService.selectById(new SelectByIdDescriptor<SMigrationPlanDescriptor>("getMigrationPlanDescriptorById",
                SMigrationPlanDescriptor.class, id));
        if (selectById == null) {
            throw new SMigrationPlanNotFoundException(id);
        }
        return selectById;
    }

    @Override
    public List<SMigrationPlanDescriptor> getMigrationPlanDescriptors(final int fromIndex, final int numberOfResults, final String field,
            final OrderByType order) throws SBonitaReadException {
        final Map<String, Object> emptyMap = Collections.emptyMap();
        return persistenceService.selectList(new SelectListDescriptor<SMigrationPlanDescriptor>("getMigrationPlanDescriptors", emptyMap,
                SMigrationPlanDescriptor.class, new QueryOptions(fromIndex, numberOfResults, SMigrationPlanDescriptor.class, field, order)));
    }

    @Override
    public SMigrationPlan getMigrationPlan(final long id) throws SInvalidMigrationPlanException, SBonitaReadException, SMigrationPlanNotFoundException {
        SMigrationPlan migrationPlan = null;
        try {
            migrationPlan = (SMigrationPlan) cacheService.get(MIGRATION_PLAN_CACHE_NAME, String.valueOf(id));
        } catch (final SCacheException e1) {
            // do nothing parse from content in db
        }
        if (migrationPlan != null) {
            return migrationPlan;
        }
        final SMigrationPlanDescriptor selectById = getMigrationPlanDescriptor(id);
        final byte[] content = selectById.getMigrationPlanContent();
        final String contentAsString = new String(content);
        StringReader xmlReader = new StringReader(contentAsString);
        try {
            parser.validate(xmlReader);
            xmlReader = new StringReader(contentAsString);
            final SMigrationPlan objectFromXML = (SMigrationPlan) parser.getObjectFromXML(xmlReader);
            cacheService.store(MIGRATION_PLAN_CACHE_NAME, String.valueOf(id), objectFromXML);
            return objectFromXML;
        } catch (final IOException e) {
            throw new SInvalidMigrationPlanException(e);
        } catch (final SValidationException e) {
            throw new SInvalidMigrationPlanException(e);
        } catch (final SXMLParseException e) {
            throw new SInvalidMigrationPlanException(e);
        } catch (final SCacheException e) {
            throw new SInvalidMigrationPlanException(e);
        }
    }

    @Override
    public void deleteMigrationPlan(final long id) throws SBonitaReadException, SMigrationPlanNotFoundException, SMigrationPlanDeletionException {
        final SMigrationPlanDescriptor descriptor = getMigrationPlanDescriptor(id);
        final DeleteRecord deleteRecord = new DeleteRecord(descriptor);
        final SMigrationPlanDescriptorLogBuilderImpl logBuilder = getQueriableLog(ActionType.DELETED, DELETED_MIGRATION_PLAN);
        final SDeleteEvent deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(MIGRATION_PLAN_DESCRIPTOR)
                .setObject(descriptor)
                .done();
        try {
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(id, SQueriableLog.STATUS_OK, logBuilder, "deleteMigrationPlan");
        } catch (final SRecorderException e) {
            initiateLogBuilder(id, SQueriableLog.STATUS_FAIL, logBuilder, "deleteMigrationPlan");
            throw new SMigrationPlanDeletionException(id, e);
        }
    }

    @Override
    public long getNumberOfMigrationPlan() throws SBonitaReadException {
        final Map<String, Object> emptyMap = Collections.emptyMap();
        return persistenceService.selectOne(new SelectOneDescriptor<Long>("getNumberOfMigrationPlanDescriptors", emptyMap, SMigrationPlanDescriptor.class,
                Long.class));
    }

    @Override
    public void prepareProcessesForMigration(final List<Long> processInstanceIds, final long migrationPlanId) throws SPrepareForMigrationFailedException {
        for (final Long processInstanceId : processInstanceIds) {
            SProcessInstance processInstance;
            try {
                processInstance = processInstanceService.getProcessInstance(processInstanceId);
                processInstanceService.setMigrationPlanId(processInstance, migrationPlanId);
                // TODO check if the process is in ready for migration
            } catch (final SProcessInstanceNotFoundException e) {
                throw new SPrepareForMigrationFailedException(processInstanceId, migrationPlanId, e);
            } catch (final SProcessInstanceReadException e) {
                throw new SPrepareForMigrationFailedException(processInstanceId, migrationPlanId, e);
            } catch (final SProcessInstanceModificationException e) {
                throw new SPrepareForMigrationFailedException(processInstanceId, migrationPlanId, e);
            }
        }
    }

    private void initiateLogBuilder(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String callerMethodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), callerMethodName, log);
        }
    }

}
