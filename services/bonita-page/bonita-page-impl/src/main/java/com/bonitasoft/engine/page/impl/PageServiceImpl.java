/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
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
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

import com.bonitasoft.engine.page.PageService;
import com.bonitasoft.engine.page.SPage;
import com.bonitasoft.engine.page.SPageContent;
import com.bonitasoft.engine.page.SPageLogBuilder;
import com.bonitasoft.engine.page.SPageUpdateBuilder;
import com.bonitasoft.engine.page.SPageUpdateContentBuilder;
import com.bonitasoft.engine.page.SPageWithContent;
import com.bonitasoft.manager.Manager;

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
        this(Manager.getInstance(), persistenceService, recorder, eventService, logger, queriableLoggerService);
    }

    PageServiceImpl(final Manager manager, final ReadPersistenceService persistenceService, final Recorder recorder,
            final EventService eventService, final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService) {

        // FIXME : uncomment when licence pb is fix onIC
        // if (!manager.isFeatureActive(Features.CUSTOM_PAGES)) {
        // throw new IllegalStateException("The custom pages is not an active feature.");
        // }
        this.persistenceService = persistenceService;
        this.eventService = eventService;
        this.recorder = recorder;
        this.logger = logger;
        this.queriableLoggerService = queriableLoggerService;
    }

    @Override
    public SPage addPage(final SPage page, final byte[] content) throws SObjectCreationException, SObjectAlreadyExistsException {
        if (page.getName() == null || page.getName().isEmpty()) {
            throw new SObjectCreationException("Unable to create a page with null or empty name");
        }
        final String message = "Adding a new page with name " + page.getName();
        final SPageLogBuilder logBuilder = getPageLog(ActionType.CREATED, message);
        try {

            checkContentIsValid(content);

            final SPageWithContent pageContent = new SPageWithContentImpl(page, content);
            final InsertRecord insertContentRecord = new InsertRecord(pageContent);
            final SInsertEvent insertContentEvent = getInsertEvent(insertContentRecord, PAGE);
            recorder.recordInsert(insertContentRecord, insertContentEvent);
            page.setId(pageContent.getId());

            initiateLogBuilder(pageContent.getId(), SQueriableLog.STATUS_OK, logBuilder, "addPage");
            return page;
        } catch (final SRecorderException re) {
            initiateLogBuilder(page.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "addPage");
            throw new SObjectCreationException(re);
        } catch (final SBonitaReadException bre) {
            initiateLogBuilder(page.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "addPage");
            throw new SObjectCreationException(bre);
        }
    }

    SPageLogBuilder getPageLog(final ActionType actionType, final String message) {
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
        } catch (final SBonitaReadException e) {
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

    private SUpdateEvent getUpdateEvent(final Object object, final String type) {
        if (eventService.hasHandlers(type, EventActionType.UPDATED)) {
            return (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(type).setObject(object).done();
        } else {
            return null;
        }
    }

    void initiateLogBuilder(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String methodName) {
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

    @Override
    public SPage updatePage(final long pageId, final EntityUpdateDescriptor entityUpdateDescriptor) throws SObjectModificationException {
        // EntityUpdateDescriptor updateDescriptor;
        final String message = "Update a page with id " + pageId;

        final SPageLogBuilder logBuilder = getPageLog(ActionType.UPDATED, message);
        try {

            final SPage sPage = persistenceService.selectById(new SelectByIdDescriptor<SPage>("getPageById", SPage.class, pageId));
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(sPage,
                    entityUpdateDescriptor);

            final SUpdateEvent updatePageEvent = getUpdateEvent(sPage, PAGE);
            recorder.recordUpdate(updateRecord, updatePageEvent);

            initiateLogBuilder(pageId, SQueriableLog.STATUS_OK, logBuilder, "updatePage");
            return sPage;
        } catch (final SRecorderException re) {
            initiateLogBuilder(pageId, SQueriableLog.STATUS_FAIL, logBuilder, "updatePage");
            throw new SObjectModificationException(re);
        } catch (final SBonitaReadException e) {
            initiateLogBuilder(pageId, SQueriableLog.STATUS_FAIL, logBuilder, "updatePage");
            throw new SObjectModificationException(e);
        }

    }

    @Override
    public void updatePageContent(final long pageId, final EntityUpdateDescriptor entityUpdateDescriptor) throws SObjectModificationException {
        final String message = "Update a page with name " + pageId;
        final SPageLogBuilder logBuilder = getPageLog(ActionType.UPDATED, message);

        try {
            checkPageContentIsValid(entityUpdateDescriptor);
            final SPageContent sPageContent = persistenceService.selectById(new SelectByIdDescriptor<SPageContent>("getPageContent",
                    SPageContent.class, pageId));
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(sPageContent,
                    entityUpdateDescriptor);
            final SUpdateEvent updatePageEvent = getUpdateEvent(sPageContent, PAGE);

            recorder.recordUpdate(updateRecord, updatePageEvent);

            initiateLogBuilder(pageId, SQueriableLog.STATUS_OK, logBuilder, "updatePage");

        } catch (final SRecorderException re) {
            initiateLogBuilder(pageId, SQueriableLog.STATUS_FAIL, logBuilder, "updatePage");
            throw new SObjectModificationException(re);
        } catch (final SBonitaReadException e) {
            initiateLogBuilder(pageId, SQueriableLog.STATUS_FAIL, logBuilder, "updatePage");
            throw new SObjectModificationException(e);
        }

    }

    @Override
    public void start() throws SBonitaException {
        try {
            // check if the provided pages are here or not up to date and import them from class path if needed
            final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("provided-page.properties");
            if (inputStream == null) {
                // no provided page
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, "No provided-page.properties found in the class path, nothing will be imported");
                return;
            }
            final Properties pageProperties = new Properties();
            pageProperties.load(inputStream);

            // provided pages name?
            final SPage pageByName = getPageByName(pageProperties.getProperty("name"));
            if (pageByName == null) {
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Provided page was not imported, importing it.");
                addPage(getProvidedPage(pageProperties), getProvidedPageContent());
                return;
            }
            final byte[] providedPageContent = getProvidedPageContent();
            final byte[] pageContent = getPageContent(pageByName.getId());
            if (pageContent.length != providedPageContent.length) {
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Provided page exists but the content is not up to date, updating it.");
                // think of a better way to check the content are the same or not, it will almost always be the same so....
                updateProvidedPage(pageByName.getId(), pageProperties, providedPageContent);
            } else {
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Provided page exists and is up to date, do nothing");
            }
        } catch (final IOException e) {
            throw new SBonitaReadException("Unable to import the provided page", e);
        }
    }

    /**
     * @param id
     * @param pageProperties
     * @param providedPageContent
     * @throws SObjectModificationException
     */
    private void updateProvidedPage(final long id, final Properties pageProperties, final byte[] providedPageContent) throws SObjectModificationException {
        final SPageUpdateBuilder pageUpdateBuilder = new SPageUpdateBuilderImpl(new EntityUpdateDescriptor());

        final SPageUpdateContentBuilder pageUpdateContentBuilder = new SPageUpdateContentBuilderImpl(new EntityUpdateDescriptor());

        pageUpdateBuilder.updateLastModificationDate(System.currentTimeMillis());
        pageUpdateContentBuilder.updateContent(providedPageContent);

        updatePage(id, pageUpdateBuilder.done());
        updatePageContent(id, pageUpdateContentBuilder.done());

        pageUpdateBuilder.updateLastModificationDate(System.currentTimeMillis());
    }

    /**
     * @param pageProperties
     * @return
     * @throws IOException
     */
    private byte[] getProvidedPageContent() throws IOException {
        return IOUtil.getAllContentFrom(Thread.currentThread().getContextClassLoader().getResourceAsStream("provided-page.zip"));
    }

    /**
     * @param pageProperties
     * @return
     */
    private SPage getProvidedPage(final Properties pageProperties) {
        final long now = System.currentTimeMillis();
        return new SPageImpl(pageProperties.getProperty("name"), pageProperties.getProperty("description"), pageProperties.getProperty("displayName"), now, -1,
                true, now);
    }

    @Override
    public void stop() throws SBonitaException, TimeoutException {
        // nothing to do
    }

    @Override
    public void pause() throws SBonitaException, TimeoutException {
        // nothing to do
    }

    @Override
    public void resume() throws SBonitaException {
        // nothing to do
    }

    protected void checkPageContentIsValid(final EntityUpdateDescriptor entityUpdateDescriptor)
            throws SBonitaReadException {
        if (null == entityUpdateDescriptor || !entityUpdateDescriptor.getFields().containsKey(SPageContentFields.PAGE_CONTENT)) {
            throw new SBonitaReadException("page content error");
        }

        checkContentIsValid((byte[]) entityUpdateDescriptor.getFields().get(SPageContentFields.PAGE_CONTENT));

    }

    protected boolean checkContentIsValid(final byte[] content) throws SBonitaReadException {
        final InputStream resourceAsStream = new ByteArrayInputStream(content);
        final ZipInputStream zin = new ZipInputStream(new BufferedInputStream(resourceAsStream));
        boolean zipIsValid = false;
        ZipEntry entry;

        try {
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.getName().equalsIgnoreCase("index.groovy")) {
                    zipIsValid = true;
                }
                if (entry.getName().equalsIgnoreCase("index.html")) {
                    zipIsValid = true;
                }

            }
            zin.close();
        } catch (final IOException e) {
            zipIsValid = false;
        }

        if (!zipIsValid) {
            throw new SBonitaReadException("page content is not a valid zip file");
        }
        return zipIsValid;
    }
}
