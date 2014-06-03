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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileEntryBuilderFactory;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryDeletionException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryNotFoundException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryUpdateException;
import org.bonitasoft.engine.profile.model.SProfileEntry;
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
import com.bonitasoft.engine.page.impl.exception.SInvalidPageTokenException;
import com.bonitasoft.engine.page.impl.exception.SInvalidPageZipContentException;
import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;

/**
 * @author Baptiste Mesta
 */
public class PageServiceImpl implements PageService {

    public static final String PAGE_PROPERTIES_CONTENT_IS_NOT_VALID = "page.properties content is not valid";

    public static final String PAGE_CONTENT_DOES_NOT_CONTAINS_A_PAGE_PROPERTIES_FILE = "Page content does not contains a page.properties file";

    public static final String PAGE_CONTENT_DOES_NOT_CONTAINS_A_INDEX_GROOVY_OR_INDEX_HTML_FILE = "Page content does not contains a Index.groovy or index.html file";

    public static final String PAGE_CONTENT_IS_NOT_A_VALID_ZIP_FILE = "Page content is not a valid zip file";

    private static final String QUERY_GET_PAGE_CONTENT = "getPageContent";

    private static final String QUERY_GET_PAGE_BY_NAME = "getPageByName";

    private static final String QUERY_GET_PAGE_BY_ID = "getPageById";

    private static final String METHOD_DELETE_PAGE = "deletePage";

    private static final String METHOD_NAME_ADD_PAGE = "addPage";

    private static final String METHOD_UPDATE_PAGE = "updatePage";

    public static final String PAGE_TOKEN_PREFIX = "custompage_";

    private final ReadPersistenceService persistenceService;

    private final Recorder recorder;

    private final EventService eventService;

    private final TechnicalLoggerService logger;

    private final QueriableLoggerService queriableLoggerService;

    private final boolean active;

    private final ProfileService profileService;

    public PageServiceImpl(final ReadPersistenceService persistenceService, final Recorder recorder,
            final EventService eventService, final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService,
            final ProfileService profileService) {
        this(Manager.getInstance(), persistenceService, recorder, eventService, logger, queriableLoggerService, profileService);
    }

    PageServiceImpl(final Manager manager, final ReadPersistenceService persistenceService, final Recorder recorder,
            final EventService eventService, final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService,
            final ProfileService profileService) {
        active = manager.isFeatureActive(Features.CUSTOM_PAGE);
        this.persistenceService = persistenceService;
        this.eventService = eventService;
        this.recorder = recorder;
        this.logger = logger;
        this.queriableLoggerService = queriableLoggerService;
        this.profileService = profileService;
    }

    @Override
    public SPage addPage(final SPage page, final byte[] content) throws SObjectCreationException, SObjectAlreadyExistsException {
        check();
        if (page.getName() == null || page.getName().isEmpty()) {
            throw new SObjectCreationException("Unable to create a page with null or empty name");
        }
        final String message = "Adding a new page with name " + page.getName();
        final SPageLogBuilder logBuilder = getPageLog(ActionType.CREATED, message);
        try {
            final SPageWithContent pageContent = new SPageWithContentImpl(page, content);
            final InsertRecord insertContentRecord = new InsertRecord(pageContent);
            final SInsertEvent insertContentEvent = getInsertEvent(insertContentRecord, PAGE);

            final SPage pageByName = getPageByName(page.getName());
            if (null != pageByName)
            {
                initiateLogBuilder(page.getId(), SQueriableLog.STATUS_FAIL, logBuilder, METHOD_NAME_ADD_PAGE);
                throwAlreadyExistsException(pageByName.getName());
            }

            if (!page.isProvided()) {
                checkContentIsValid(content);
            }

            recorder.recordInsert(insertContentRecord, insertContentEvent);
            page.setId(pageContent.getId());

            initiateLogBuilder(pageContent.getId(), SQueriableLog.STATUS_OK, logBuilder, METHOD_NAME_ADD_PAGE);
            return page;
        } catch (final SRecorderException re) {
            initiateLogBuilder(page.getId(), SQueriableLog.STATUS_FAIL, logBuilder, METHOD_NAME_ADD_PAGE);
            throw new SObjectCreationException(re);
        } catch (final SBonitaReadException bre) {
            initiateLogBuilder(page.getId(), SQueriableLog.STATUS_FAIL, logBuilder, METHOD_NAME_ADD_PAGE);
            throw new SObjectCreationException(bre);
        }
    }

    /**
     * 
     */
    private final void check() {
        if (!active) {
            throw new IllegalStateException("The custom pages is not an active feature.");
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
        check();
        final SPage page = persistenceService.selectById(new SelectByIdDescriptor<SPage>(QUERY_GET_PAGE_BY_ID, SPage.class, pageId));
        if (page == null) {
            throw new SObjectNotFoundException("Page with id " + pageId + " not found");
        }
        return page;
    }

    @Override
    public SPage getPageByName(final String pageName) throws SBonitaReadException {
        if (!active) {
            return null;
        }
        final SPage page = persistenceService.selectOne(new SelectOneDescriptor<SPage>(QUERY_GET_PAGE_BY_NAME, Collections.singletonMap("pageName",
                (Object) pageName), SPage.class));
        return page;
    }

    @Override
    public long getNumberOfPages(final QueryOptions options) throws SBonitaReadException {
        check();
        final long number = persistenceService.getNumberOfEntities(SPage.class, options, null);
        return number;
    }

    @Override
    public List<SPage> searchPages(final QueryOptions options) throws SBonitaSearchException {
        check();
        try {
            return persistenceService.searchEntity(SPage.class, options, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public void deletePage(final long pageId) throws SObjectModificationException, SObjectNotFoundException {
        check();
        try {
            final SPage page = getPage(pageId);
            deletePage(page);
        } catch (final SBonitaReadException sbe) {
            new SObjectModificationException(sbe);
        }
    }

    private void deletePage(final SPage sPage) throws SObjectModificationException {
        final SPageLogBuilder logBuilder = getPageLog(ActionType.DELETED, "Deleting page named: " + sPage.getName());
        try {
            deleteProfileEntry(sPage);

            final DeleteRecord deleteRecord = new DeleteRecord(sPage);
            final SDeleteEvent deleteEvent = getDeleteEvent(sPage, PAGE);
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(sPage.getId(), SQueriableLog.STATUS_OK, logBuilder, METHOD_DELETE_PAGE);
        } catch (final SRecorderException re) {
            initiateLogBuilder(sPage.getId(), SQueriableLog.STATUS_FAIL, logBuilder, METHOD_DELETE_PAGE);
            throw new SObjectModificationException(re);
        } catch (final SBonitaSearchException e) {
            initiateLogBuilder(sPage.getId(), SQueriableLog.STATUS_FAIL, logBuilder, METHOD_DELETE_PAGE);
            throw new SObjectModificationException(e);
        } catch (final SProfileEntryNotFoundException e) {
            initiateLogBuilder(sPage.getId(), SQueriableLog.STATUS_FAIL, logBuilder, METHOD_DELETE_PAGE);
            throw new SObjectModificationException(e);
        } catch (final SProfileEntryDeletionException e) {
            initiateLogBuilder(sPage.getId(), SQueriableLog.STATUS_FAIL, logBuilder, METHOD_DELETE_PAGE);
            throw new SObjectModificationException(e);
        }
    }

    private void deleteProfileEntry(final SPage sPage) throws SBonitaSearchException, SProfileEntryNotFoundException, SProfileEntryDeletionException {
        final List<OrderByOption> orderByOptions = Collections
                .singletonList(new OrderByOption(SProfileEntry.class, SProfileEntryBuilderFactory.INDEX, OrderByType.ASC));
        final List<FilterOption> filters = new ArrayList<FilterOption>();
        filters.add(new FilterOption(SProfileEntry.class, SProfileEntryBuilderFactory.PAGE, sPage.getName()));
        filters.add(new FilterOption(SProfileEntry.class, SProfileEntryBuilderFactory.CUSTOM, new Boolean(true)));

        final QueryOptions queryOptions = new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS, orderByOptions, filters, null);

        final List<SProfileEntry> searchProfileEntries = profileService.searchProfileEntries(queryOptions);
        for (final SProfileEntry sProfileEntry : searchProfileEntries) {
            profileService.deleteProfileEntry(sProfileEntry.getId());
            if (sProfileEntry.getParentId() > 0) {
                deleteParentIfNoMoreChildren(sProfileEntry);
            }
        }
    }

    private void deleteParentIfNoMoreChildren(final SProfileEntry sProfileEntry) throws SBonitaSearchException, SProfileEntryNotFoundException,
            SProfileEntryDeletionException {
        final List<OrderByOption> orderByOptions = Collections
                .singletonList(new OrderByOption(SProfileEntry.class, SProfileEntryBuilderFactory.INDEX, OrderByType.ASC));
        final List<FilterOption> filters = new ArrayList<FilterOption>();
        filters.add(new FilterOption(SProfileEntry.class, SProfileEntryBuilderFactory.PROFILE_ID, sProfileEntry.getProfileId()));
        filters.add(new FilterOption(SProfileEntry.class, SProfileEntryBuilderFactory.PARENT_ID, sProfileEntry.getParentId()));

        final QueryOptions queryOptions = new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS, orderByOptions, filters, null);

        final List<SProfileEntry> searchProfileEntries = profileService.searchProfileEntries(queryOptions);
        if (null == searchProfileEntries || searchProfileEntries.isEmpty()) {
            // no more children
            profileService.deleteProfileEntry(sProfileEntry.getParentId());
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
            return (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(type).setObject(object).done();
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
        check();
        final SPage page = getPage(pageId);
        final SPageContent pageContent = persistenceService.selectById(new SelectByIdDescriptor<SPageContent>(QUERY_GET_PAGE_CONTENT,
                SPageContent.class, pageId));
        if (pageContent == null) {
            throw new SObjectNotFoundException("Page with id " + pageId + " not found");
        }
        final byte[] content = pageContent.getContent();
        try {
            final Map<String, byte[]> contentAsMap = IOUtil.unzip(content);
            final Properties pageProperties = new Properties();
            pageProperties.put(PROPERTIES_NAME, page.getName());
            pageProperties.put(PROPERTIES_DISPLAY_NAME, page.getDisplayName());
            pageProperties.put(PROPERTIES_DESCRIPTION, page.getDescription());
            contentAsMap.put("page.properties", IOUtil.getPropertyAsString(pageProperties));

            return IOUtil.zip(contentAsMap);
        } catch (final IOException e) {
            throw new SBonitaReadException("the page is not a valid zip file", e);
        }

    }

    @Override
    public SPage updatePage(final long pageId, final EntityUpdateDescriptor entityUpdateDescriptor) throws SObjectModificationException,
            SObjectAlreadyExistsException {
        check();
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Update a page with id ");
        stringBuilder.append(pageId);
        final SPageLogBuilder logBuilder = getPageLog(ActionType.UPDATED, stringBuilder.toString());
        final String logMethodName = METHOD_UPDATE_PAGE;
        try {
            if (entityUpdateDescriptor.getFields().containsKey(SPageFields.PAGE_NAME))
            {
                final SPage pageByName = getPageByName(entityUpdateDescriptor.getFields().get(SPageFields.PAGE_NAME).toString());
                if (null != pageByName && pageByName.getId() != pageId)
                {
                    initiateLogBuilder(pageId, SQueriableLog.STATUS_FAIL, logBuilder, logMethodName);
                    throwAlreadyExistsException(pageByName.getName());
                }
            }

            final SPage sPage = persistenceService.selectById(new SelectByIdDescriptor<SPage>(QUERY_GET_PAGE_BY_ID, SPage.class, pageId));
            final String oldPageName = sPage.getName();
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(sPage,
                    entityUpdateDescriptor);

            final SUpdateEvent updatePageEvent = getUpdateEvent(sPage, PAGE);
            recorder.recordUpdate(updateRecord, updatePageEvent);
            if (entityUpdateDescriptor.getFields().containsKey(SPageFields.PAGE_NAME))
            {
                // page name has changed
                final String newPageName = entityUpdateDescriptor.getFields().get(SPageFields.PAGE_NAME).toString();
                updateProfileEntry(oldPageName, newPageName);
            }

            initiateLogBuilder(pageId, SQueriableLog.STATUS_OK, logBuilder, logMethodName);
            return sPage;
        } catch (final SRecorderException re) {
            initiateLogBuilder(pageId, SQueriableLog.STATUS_FAIL, logBuilder, logMethodName);
            throw new SObjectModificationException(re);
        } catch (final SBonitaReadException e) {
            initiateLogBuilder(pageId, SQueriableLog.STATUS_FAIL, logBuilder, logMethodName);
            throw new SObjectModificationException(e);
        } catch (final SBonitaSearchException e) {
            initiateLogBuilder(pageId, SQueriableLog.STATUS_FAIL, logBuilder, logMethodName);
            throw new SObjectModificationException(e);
        } catch (final SProfileEntryUpdateException e) {
            initiateLogBuilder(pageId, SQueriableLog.STATUS_FAIL, logBuilder, logMethodName);
            throw new SObjectModificationException(e);
        }

    }

    private void updateProfileEntry(final String oldPageName, final String newPageName) throws SBonitaSearchException, SProfileEntryUpdateException {
        if (newPageName.equals(oldPageName))
        {
            return;
        }
        final List<FilterOption> filters = new ArrayList<FilterOption>();
        filters.add(new FilterOption(SProfileEntry.class, SProfileEntryBuilderFactory.PAGE, oldPageName));
        final QueryOptions queryOptions = new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS, Collections
                .singletonList(new OrderByOption(SProfileEntry.class, SProfileEntryBuilderFactory.INDEX, OrderByType.ASC)), filters, null);
        final List<SProfileEntry> searchProfileEntries = profileService.searchProfileEntries(queryOptions);
        for (final SProfileEntry sProfileEntry : searchProfileEntries) {
            final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
            entityUpdateDescriptor.addField(SProfileEntryBuilderFactory.NAME, sProfileEntry.getName());
            entityUpdateDescriptor.addField(SProfileEntryBuilderFactory.PAGE, newPageName);
            profileService.updateProfileEntry(sProfileEntry, entityUpdateDescriptor);
        }

    }

    private void throwAlreadyExistsException(final String pageName) throws SObjectAlreadyExistsException {
        final StringBuilder stringBuilderException = new StringBuilder();
        stringBuilderException.append("page with name ");
        stringBuilderException.append(pageName);
        throw new SObjectAlreadyExistsException(stringBuilderException.toString());
    }

    @Override
    public void updatePageContent(final long pageId, final EntityUpdateDescriptor entityUpdateDescriptor) throws SObjectModificationException {
        check();
        final String message = "Update a page with name " + pageId;
        final SPageLogBuilder logBuilder = getPageLog(ActionType.UPDATED, message);

        try {
            checkPageContentIsValid(entityUpdateDescriptor);
            final SPageContent sPageContent = persistenceService.selectById(new SelectByIdDescriptor<SPageContent>(QUERY_GET_PAGE_CONTENT,
                    SPageContent.class, pageId));
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(sPageContent,
                    entityUpdateDescriptor);
            final SUpdateEvent updatePageEvent = getUpdateEvent(sPageContent, PAGE);

            recorder.recordUpdate(updateRecord, updatePageEvent);

            initiateLogBuilder(pageId, SQueriableLog.STATUS_OK, logBuilder, METHOD_UPDATE_PAGE);

        } catch (final SRecorderException re) {
            initiateLogBuilder(pageId, SQueriableLog.STATUS_FAIL, logBuilder, METHOD_UPDATE_PAGE);
            throw new SObjectModificationException(re);
        } catch (final SBonitaReadException e) {
            initiateLogBuilder(pageId, SQueriableLog.STATUS_FAIL, logBuilder, METHOD_UPDATE_PAGE);
            throw new SObjectModificationException(e);
        }

    }

    @Override
    public void start() throws SBonitaException {
        if (active) {
            importProvidedPage("bonita-html-page-example.zip");
            importProvidedPage("bonita-groovy-page-example.zip");
        }
    }

    private void importProvidedPage(final String zipName) throws SBonitaReadException, SObjectCreationException, SObjectAlreadyExistsException,
            SObjectNotFoundException,
            SObjectModificationException {
        try {
            // check if the provided pages are here or not up to date and import them from class path if needed
            final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(zipName);
            if (inputStream == null) {
                // no provided page
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, "No provided-" + zipName + " found in the class path, nothing will be imported");
                return;
            }
            final byte[] providedPageContent = IOUtil.getAllContentFrom(inputStream);

            final byte[] zipEntryContent = IOUtil.getZipEntryContent("page.properties", new ByteArrayInputStream(providedPageContent));
            final Properties pageProperties = new Properties();
            pageProperties.load(new ByteArrayInputStream(zipEntryContent));

            // provided pages name?
            final SPage pageByName = getPageByName(pageProperties.getProperty(PROPERTIES_NAME));
            if (pageByName == null) {
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Provided page was not imported, importing it.");
                addPage(getProvidedPage(pageProperties, zipName), providedPageContent);
                return;
            }
            final byte[] pageContent = getPageContent(pageByName.getId());
            if (pageContent.length != providedPageContent.length) {
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Provided page exists but the content is not up to date, updating it.");
                // think of a better way to check the content are the same or not, it will almost always be the same so....
                updateProvidedPage(pageByName.getId(), pageProperties, providedPageContent);
            } else {
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Provided page exists and is up to date, do nothing");
            }
        } catch (final IOException e) {
            logger.log(getClass(), TechnicalLogSeverity.WARNING, "Provided page " + zipName + "can't be imported");
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

        try {
            updatePage(id, pageUpdateBuilder.done());
        } catch (final SObjectAlreadyExistsException e) {
            // throw only when updating with existing name
            throw new SObjectModificationException(e);
        }
        updatePageContent(id, pageUpdateContentBuilder.done());

        pageUpdateBuilder.updateLastModificationDate(System.currentTimeMillis());
    }

    /**
     * @param pageProperties
     * @return
     */
    private SPage getProvidedPage(final Properties pageProperties, final String zipName) {
        final long now = System.currentTimeMillis();
        return new SPageImpl(pageProperties.getProperty(PROPERTIES_NAME), pageProperties.getProperty(PROPERTIES_DESCRIPTION),
                pageProperties.getProperty(PROPERTIES_DISPLAY_NAME), now, -1,
                true, now, -1, zipName);
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

    protected void checkPageContentIsValid(final EntityUpdateDescriptor entityUpdateDescriptor) throws SInvalidPageZipContentException,
            SInvalidPageTokenException
    {
        if (null == entityUpdateDescriptor) {
            throw new SInvalidPageZipContentException("page content error");
        }
        final Map<String, Object> fields = entityUpdateDescriptor.getFields();
        if (!fields.containsKey(SPageContentFields.PAGE_CONTENT)) {
            throw new SInvalidPageZipContentException("page content error");
        }
        checkContentIsValid((byte[]) fields.get(SPageContentFields.PAGE_CONTENT));

    }

    protected boolean checkContentIsValid(final byte[] content) throws SInvalidPageZipContentException, SInvalidPageTokenException {
        final InputStream resourceAsStream = new ByteArrayInputStream(content);
        final ZipInputStream zin = new ZipInputStream(new BufferedInputStream(resourceAsStream));
        boolean contentIsValid = false;
        boolean zipIsValid = false;
        boolean zipContainsIndex = false;
        boolean zipContainsPageProperties = false;
        boolean pagePropertiesContentIsValid = false;
        boolean pageNameIsValid = false;
        boolean pageDisplayNameIsValid = false;

        ZipEntry entry;
        try {
            while ((entry = zin.getNextEntry()) != null) {
                zipIsValid = true;
                if (entry.getName().equals("Index.groovy")) {
                    zipContainsIndex = true;
                }
                if (entry.getName().equalsIgnoreCase("index.html")) {
                    zipContainsIndex = true;
                }
                if (entry.getName().equals("page.properties")) {
                    zipContainsPageProperties = true;
                    final byte[] zipEntryContent = IOUtil.getZipEntryContent(PageService.PROPERTIES_FILE_NAME, content);
                    final Properties pageProperties = new Properties();
                    pageProperties.load(new ByteArrayInputStream(zipEntryContent));
                    pagePropertiesContentIsValid = pageProperties.containsKey(PageService.PROPERTIES_NAME)
                            && pageProperties.containsKey(PageService.PROPERTIES_DISPLAY_NAME)
                            && pageProperties.containsKey(PageService.PROPERTIES_DESCRIPTION);
                    if (pagePropertiesContentIsValid) {
                        pageNameIsValid = isPageTokenValid(pageProperties.getProperty(PageService.PROPERTIES_NAME));
                        pageDisplayNameIsValid = pageProperties.getProperty(PageService.PROPERTIES_DISPLAY_NAME) != null
                                && pageProperties.getProperty(PageService.PROPERTIES_DISPLAY_NAME).length() > 0;
                    }
                    pagePropertiesContentIsValid = pagePropertiesContentIsValid && pageDisplayNameIsValid;
                }
            }
            zin.close();
            if (!zipIsValid) {
                throw new SInvalidPageZipContentException(PAGE_CONTENT_IS_NOT_A_VALID_ZIP_FILE);
            }
            if (!zipContainsIndex) {
                throw new SInvalidPageZipContentException(PAGE_CONTENT_DOES_NOT_CONTAINS_A_INDEX_GROOVY_OR_INDEX_HTML_FILE);
            }
            if (!zipContainsPageProperties) {
                throw new SInvalidPageZipContentException(PAGE_CONTENT_DOES_NOT_CONTAINS_A_PAGE_PROPERTIES_FILE);
            }
            if (!pagePropertiesContentIsValid) {
                throw new SInvalidPageZipContentException(PAGE_PROPERTIES_CONTENT_IS_NOT_VALID);
            }
            if (!pageNameIsValid) {
                throw new SInvalidPageTokenException(PAGE_PROPERTIES_CONTENT_IS_NOT_VALID);
            }
        } catch (final IOException e) {
            throw new SInvalidPageZipContentException(PAGE_CONTENT_IS_NOT_A_VALID_ZIP_FILE);
        }

        contentIsValid = zipIsValid && zipContainsIndex && zipContainsPageProperties && pagePropertiesContentIsValid && pageNameIsValid
                && pageDisplayNameIsValid;
        return contentIsValid;
    }

    protected boolean isPageTokenValid(final String urlToken) {
        return urlToken.matches(PAGE_TOKEN_PREFIX + "\\p{Alnum}+");
    }

}
