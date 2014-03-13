/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page.impl;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
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

import com.bonitasoft.engine.page.PageService;
import com.bonitasoft.engine.page.SPage;
import com.bonitasoft.engine.page.SPageContent;
import com.bonitasoft.engine.page.SPageLogBuilder;
import com.bonitasoft.engine.page.SPageWithContent;

/**
 * @author Matthieu Chaffotte
 */
public class PageServiceImpl implements PageService {

    private final ReadPersistenceService persistenceService;

    private final Recorder recorder;

    private final EventService eventService;

    private final TechnicalLoggerService logger;

    private final QueriableLoggerService queriableLoggerService;

    public PageServiceImpl(final ReadPersistenceService persistenceService, final Recorder recorder,
            final EventService eventService, final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService) {
        this.persistenceService = persistenceService;
        this.eventService = eventService;
        this.recorder = recorder;
        this.logger = logger;
        this.queriableLoggerService = queriableLoggerService;
    }

    @Override
    public SPage addPage(final SPage page, final byte[] content) throws SObjectCreationException, SObjectAlreadyExistsException {
        final String message = "Adding a new page with name " + page.getName();
        final SPageLogBuilder logBuilder = getPageLog(ActionType.CREATED, message);
        try {
            SPageWithContent pageContent = new SPageWithContentImpl(page, content);
            final InsertRecord insertContentRecord = new InsertRecord(pageContent);
            final SInsertEvent insertContentEvent = getInsertEvent(insertContentRecord, PAGE);
            recorder.recordInsert(insertContentRecord, insertContentEvent);
            page.setId(pageContent.getId());

            initiateLogBuilder(pageContent.getId(), SQueriableLog.STATUS_OK, logBuilder, "addPage");
            return page;
        } catch (final SRecorderException re) {
            initiateLogBuilder(page.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "addPage");
            throw new SObjectCreationException(re);
        }
    }

    private SPageLogBuilder getPageLog(final ActionType actionType, final String message) {
        final SPageLogBuilder logBuilder = new SPageLogBuilderImpl();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    @Override
    public SPage getPage(final long pageId) throws SBonitaReadException, SObjectNotFoundException {
        final SPage page = persistenceService.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId));
        if (page == null) {
            throw new SObjectNotFoundException("Page with id " + pageId + " not found");
        }
        return page;
    }

    @Override
    public SPage getPageByName(final String pageName) throws SBonitaReadException {
        final SPage page = persistenceService.selectOne(new SelectOneDescriptor<SPage>("getPageByName", Collections.singletonMap("pageName",
                (Object) pageName), SPage.class));
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getPageByName"));
        }
        return page;
    }

    @Override
    public long getNumberOfPages(final QueryOptions options) throws SBonitaReadException {
        final long number = persistenceService.getNumberOfEntities(SPage.class, options, null);
        return number;
    }

    @Override
    public List<SPage> searchPages(final QueryOptions options) throws SBonitaSearchException {
        try {
            return persistenceService.searchEntity(SPage.class, options, null);
        } catch (SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public void deletePage(final long pageId) throws SObjectModificationException, SObjectNotFoundException {
        try {
            final SPage page = getPage(pageId);
            deletePage(page);
        } catch (final SBonitaReadException sbe) {
            new SObjectModificationException(sbe);
        }
    }

    private void deletePage(final SPage page) throws SObjectModificationException {
        final SPageLogBuilder logBuilder = getPageLog(ActionType.DELETED, "Deleting page named: " + page.getName());
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(page);
            final SDeleteEvent deleteEvent = getDeleteEvent(page, PAGE);
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(page.getId(), SQueriableLog.STATUS_OK, logBuilder, "deletePage");
        } catch (final SRecorderException re) {
            initiateLogBuilder(page.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deletePage");
            throw new SObjectModificationException(re);
        }
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    private SInsertEvent getInsertEvent(final Object object, final String type) {
        if (eventService.hasHandlers(type, EventActionType.CREATED)) {
            return (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(type).setObject(object).done();
        } else {
            return null;
        }
    }

    private SDeleteEvent getDeleteEvent(final Object object, final String type) {
        if (eventService.hasHandlers(type, EventActionType.DELETED)) {
            return (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(type).setObject(object).done();
        } else {
            return null;
        }
    }

    private void initiateLogBuilder(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String methodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), methodName, log);
        }
    }

    @Override
    public byte[] getPageContent(final long pageId) throws SBonitaReadException, SObjectNotFoundException {
        final SPageContent pageContent = persistenceService.selectById(new SelectByIdDescriptor<SPageContent>("getPageContent",
                SPageContent.class, pageId));
        if (pageContent == null) {
            throw new SObjectNotFoundException("Page with id " + pageId + " not found");
        }
        return pageContent.getContent();
    }

}
