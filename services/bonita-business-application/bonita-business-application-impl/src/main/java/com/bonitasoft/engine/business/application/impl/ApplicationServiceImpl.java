/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package com.bonitasoft.engine.business.application.impl;

import java.util.Collections;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
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
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.SApplication;
import com.bonitasoft.engine.business.application.SApplicationLogBuilder;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationServiceImpl implements ApplicationService {

    private final Recorder recorder;

    private final ReadPersistenceService persistenceService;

    private final QueriableLoggerService queriableLoggerService;

    public ApplicationServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceService, final QueriableLoggerService queriableLoggerService) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.queriableLoggerService = queriableLoggerService;
    }

    @Override
    public SApplication createApplication(final SApplication application) throws SObjectCreationException, SObjectAlreadyExistsException {
        final SApplicationLogBuilder logBuilder = getApplicationLog(ActionType.CREATED, "Creating application named");
        final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(ApplicationService.APPLICATION)
                .setObject(application).done();
        try {
            if (hasApplicationWithName(application.getName())) {
                throw new SObjectAlreadyExistsException("An application already exists with name '" + application.getName() + "'.");
            }
            recorder.recordInsert(new InsertRecord(application), insertEvent);
            log(application.getId(), SQueriableLog.STATUS_OK, logBuilder, "createApplication");
        } catch (final SRecorderException e) {
            throwCreationException(application, logBuilder, e);
        } catch (final SBonitaReadException e) {
            throwCreationException(application, logBuilder, e);
        }
        return application;
    }

    private void throwCreationException(final SApplication application, final SApplicationLogBuilder logBuilder, final Exception e)
            throws SObjectCreationException {
        log(application.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createApplication");
        throw new SObjectCreationException(e);
    }

    private void throwModificationException(final long applicationId, final SApplicationLogBuilder logBuilder, final String methodName, final Exception e)
            throws SObjectModificationException {
        log(applicationId, SQueriableLog.STATUS_FAIL, logBuilder, methodName);
        throw new SObjectModificationException(e);
    }

    public boolean hasApplicationWithName(final String name) throws SBonitaReadException {
        final SApplication application = persistenceService.selectOne(new SelectOneDescriptor<SApplication>("getApplicationByName", Collections
                .<String, Object> singletonMap("name",
                        name), SApplication.class));
        return application != null;
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    private SApplicationLogBuilder getApplicationLog(final ActionType actionType, final String message) {
        final SApplicationLogBuilder logBuilder = new SApplicationLogBuilderImpl();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        return logBuilder;
    }

    void log(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String methodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), methodName, log);
        }
    }

    @Override
    public SApplication getApplication(final long applicationId) throws SBonitaReadException, SObjectNotFoundException {
        final SApplication application = persistenceService
                .selectById(new SelectByIdDescriptor<SApplication>("getApplicationById", SApplication.class, applicationId));
        if (application == null) {
            throw new SObjectNotFoundException("No application found with id '" + applicationId + "'.");
        }
        return application;
    }

    @Override
    public void deleteApplication(final long applicationId) throws SObjectModificationException, SObjectNotFoundException {
        final String methodName = "deleteApplication";
        final SApplicationLogBuilder logBuilder = getApplicationLog(ActionType.CREATED, "Creating application named");
        try {
            final SApplication application = getApplication(applicationId);
            final SDeleteEvent event = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(ApplicationService.APPLICATION)
                    .setObject(application).done();
            recorder.recordDelete(new DeleteRecord(application), event);
            log(application.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SBonitaReadException e) {
            throwModificationException(applicationId, logBuilder, methodName, e);
        } catch (final SRecorderException e) {
            throwModificationException(applicationId, logBuilder, methodName, e);
        }

    }

}
