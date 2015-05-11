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
package org.bonitasoft.engine.page.impl;

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
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SContentType;
import org.bonitasoft.engine.page.SInvalidPageTokenException;
import org.bonitasoft.engine.page.SInvalidPageZipException;
import org.bonitasoft.engine.page.SInvalidPageZipInconsistentException;
import org.bonitasoft.engine.page.SInvalidPageZipMissingAPropertyException;
import org.bonitasoft.engine.page.SInvalidPageZipMissingIndexException;
import org.bonitasoft.engine.page.SInvalidPageZipMissingPropertiesException;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageBuilderFactory;
import org.bonitasoft.engine.page.SPageContent;
import org.bonitasoft.engine.page.SPageLogBuilder;
import org.bonitasoft.engine.page.SPageUpdateBuilder;
import org.bonitasoft.engine.page.SPageUpdateBuilderFactory;
import org.bonitasoft.engine.page.SPageUpdateContentBuilder;
import org.bonitasoft.engine.page.SPageUpdateContentBuilderFactory;
import org.bonitasoft.engine.page.SPageWithContent;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Baptiste Mesta
 */
public class PageServiceImpl implements PageService {

    public static final String PAGE_CONTENT_DOES_NOT_CONTAINS_A_INDEX_GROOVY_OR_INDEX_HTML_FILE = "Page content does not contains a Index.groovy or index.html file";

    private static final String QUERY_GET_PAGE_CONTENT = "getPageContent";

    private static final String QUERY_GET_PAGE_BY_NAME = "getPageByName";

    private static final String QUERY_GET_PAGE_BY_NAME_AND_PROCESS_DEFINITION_ID = "getPageByNameAndProcessDefinitionId";

    private static final String QUERY_GET_PAGE_BY_PROCESS_DEFINITION_ID = "getPageByProcessDefinitionId";

    private static final String QUERY_GET_PAGE_BY_ID = "getPageById";

    private static final String METHOD_DELETE_PAGE = "deletePage";

    private static final String METHOD_NAME_ADD_PAGE = "addPage";

    private static final String METHOD_UPDATE_PAGE = "updatePage";

    public static final String PAGE_TOKEN_PREFIX = "custompage_";

    public static final String INDEX_GROOVY = "Index.groovy";

    public static final String INDEX_HTML = "index.html";

    public static final String RESOURCES_INDEX_HTML = "resources/index.html";

    private final ReadPersistenceService persistenceService;

    private final Recorder recorder;

    private final EventService eventService;

    private final TechnicalLoggerService logger;

    private final QueriableLoggerService queriableLoggerService;

    private final ProfileService profileService;

    public PageServiceImpl(final ReadPersistenceService persistenceService, final Recorder recorder,
            final EventService eventService, final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService,
            final ProfileService profileService) {
        this.persistenceService = persistenceService;
        this.eventService = eventService;
        this.recorder = recorder;
        this.logger = logger;
        this.queriableLoggerService = queriableLoggerService;
        this.profileService = profileService;
    }

    @Override
    public SPage addPage(final SPage page, final byte[] content) throws SObjectCreationException, SObjectAlreadyExistsException,
            SInvalidPageZipException, SInvalidPageTokenException {
        Map<String, byte[]> zipContent;
        try {
            zipContent = IOUtil.unzip(content);
            checkZipContainsRequiredEntries(zipContent);
            checkPageNameIsValid(page.getName(), page.isProvided());
            checkPageDisplayNameIsValid(page.getDisplayName());
            return insertPage(page, content);
        } catch (final IOException e) {
            throw new SInvalidPageZipInconsistentException("Error while reading zip file", e);
        }
    }

    @Override
    public SPage addPage(final byte[] content, final String contentName, final long userId) throws SObjectCreationException, SObjectAlreadyExistsException,
            SInvalidPageZipException,
            SInvalidPageTokenException {
        return addPage(content, contentName, userId, false/* provided */);
    }

    @Override
    public SPage getPageByNameAndProcessDefinitionId(String name, long processDefinitionId) throws SBonitaReadException {
        Map<String, Object> inputParameters = new HashMap<>();
        inputParameters.put("pageName", name);
        inputParameters.put("processDefinitionId", processDefinitionId);
        return persistenceService.selectOne(new SelectOneDescriptor<SPage>(QUERY_GET_PAGE_BY_NAME_AND_PROCESS_DEFINITION_ID, inputParameters, SPage.class));
    }

    @Override
    public List<SPage> getPageByProcessDefinitionId(long processDefinitionId, int fromIndex, int numberOfResults) throws SBonitaReadException {
        Map<String, Object> inputParameters = new HashMap<>();
        inputParameters.put("processDefinitionId", processDefinitionId);
        final OrderByOption orderByOption = new OrderByOption(SPage.class, SPageFields.PAGE_NAME, OrderByType.ASC);
        QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfResults, Collections.singletonList(orderByOption));
        return persistenceService.selectList(new SelectListDescriptor<SPage>(QUERY_GET_PAGE_BY_PROCESS_DEFINITION_ID, inputParameters, SPage.class,
                queryOptions));
    }

    private SPage addPage(final byte[] content, final String contentName, final long userId, final boolean provided) throws SInvalidPageZipException,
            SInvalidPageTokenException, SObjectAlreadyExistsException, SObjectCreationException {
        final Properties pageProperties = readPageZip(content, provided);
        final SPage page = createPage(pageProperties.getProperty(PageService.PROPERTIES_NAME), pageProperties.getProperty(PageService.PROPERTIES_DISPLAY_NAME),
                pageProperties.getProperty(PageService.PROPERTIES_DESCRIPTION), contentName, userId, provided, SContentType.PAGE);
        return insertPage(page, content);
    }

    @Override
    public Properties readPageZip(final byte[] content) throws SInvalidPageZipMissingIndexException, SInvalidPageZipMissingAPropertyException,
            SInvalidPageZipInconsistentException, SInvalidPageZipMissingPropertiesException, SInvalidPageTokenException {
        return readPageZip(content, false);
    }

    Properties readPageZip(final byte[] content, final boolean provided) throws SInvalidPageZipMissingIndexException, SInvalidPageZipMissingAPropertyException,
            SInvalidPageZipInconsistentException, SInvalidPageZipMissingPropertiesException, SInvalidPageTokenException {
        final Properties pageProperties;
        if (content == null) {
            throw new SInvalidPageZipInconsistentException("Content can't be null");
        }
        try {
            final Map<String, byte[]> zipContent = IOUtil.unzip(content);
            checkZipContainsRequiredEntries(zipContent);
            pageProperties = loadPageProperties(zipContent);
            checkPageNameIsValid(pageProperties.getProperty(PageService.PROPERTIES_NAME), provided);
            checkPageDisplayNameIsValid(pageProperties.getProperty(PageService.PROPERTIES_DISPLAY_NAME));
        } catch (final IOException e) {
            throw new SInvalidPageZipInconsistentException("Error while reading zip file", e);
        }
        return pageProperties;
    }

    SPage insertPage(final SPage page, final byte[] content) throws SObjectAlreadyExistsException, SObjectCreationException {
        final SPageLogBuilder logBuilder = getPageLog(ActionType.CREATED, "Adding a new page with name " + page.getName());
        try {
            final SPageWithContent pageContent = new SPageWithContentImpl(page, content);
            final InsertRecord insertContentRecord = new InsertRecord(pageContent);
            final SInsertEvent insertContentEvent = getInsertEvent(insertContentRecord, PAGE);

            final SPage pageByName = checkIfPageAlreadyExists(page);
            if (null != pageByName) {
                initiateLogBuilder(page.getId(), SQueriableLog.STATUS_FAIL, logBuilder, METHOD_NAME_ADD_PAGE);
                throwAlreadyExistsException(pageByName.getName());
            }
            recorder.recordInsert(insertContentRecord, insertContentEvent);
            page.setId(pageContent.getId());

            return page;
        } catch (SRecorderException | SBonitaReadException re) {
            throw new SObjectCreationException(re);
        }
    }

    private SPage checkIfPageAlreadyExists(SPage page) throws SBonitaReadException {
        SPage existingPage;
        if (page.getProcessDefinitionId() != null) {
            existingPage = getPageByNameAndProcessDefinitionId(page.getName(), page.getProcessDefinitionId());
        } else {
            existingPage = getPageByName(page.getName());
        }
        return existingPage;
    }

    /**
     * @param displayName
     * @throws SInvalidPageTokenException
     */
    private void checkPageDisplayNameIsValid(final String displayName) throws SInvalidPageZipMissingPropertiesException,
            SInvalidPageZipMissingAPropertyException {
        if (displayName == null
                || displayName.length() == 0) {
            throw new SInvalidPageZipMissingAPropertyException(PageService.PROPERTIES_DISPLAY_NAME);
        }
    }

    private Properties loadPageProperties(final Map<String, byte[]> zipContent) throws SInvalidPageZipMissingPropertiesException, IOException {
        final byte[] pagePropertiesContent = zipContent.get(PageService.PROPERTIES_FILE_NAME);
        if (pagePropertiesContent == null) {
            throw new SInvalidPageZipMissingPropertiesException();
        }

        final Properties pageProperties = new Properties();
        pageProperties.load(new StringReader(new String(pagePropertiesContent, "UTF-8")));
        return pageProperties;
    }

    /**
     * @param name
     * @param provided
     * @throws SInvalidPageTokenException
     */
    private void checkPageNameIsValid(final String name, final boolean provided) throws SInvalidPageTokenException {
        if (name == null || name.isEmpty() || !provided && !name.matches(PAGE_TOKEN_PREFIX + "\\p{Alnum}+")) {
            throw new SInvalidPageTokenException("Page name is not valid, it must contains only alpha numeric characters and start with " + PAGE_TOKEN_PREFIX);
        }
    }

    void checkZipContainsRequiredEntries(final Map<String, byte[]> zipContent) throws SInvalidPageZipMissingIndexException {
        final Set<String> entrySet = zipContent.keySet();
        for (final String entry : entrySet) {
            if (INDEX_GROOVY.equals(entry) || INDEX_HTML.equalsIgnoreCase(entry) || RESOURCES_INDEX_HTML.equalsIgnoreCase(entry)) {
                return;
            }
        }
        throw new SInvalidPageZipMissingIndexException();
    }

    /**
     * @param name
     * @param displayName
     * @param description
     * @param provided
     * @param contentType
     * @return
     */
    private SPage createPage(final String name, final String displayName, final String description, final String contentName, final long creatorUserId,
            final boolean provided, String contentType) {
        return BuilderFactory.get(SPageBuilderFactory.class).createNewInstance(name, description, displayName,
                System.currentTimeMillis(), creatorUserId, provided, contentName).setContentType(contentType).done();
    }

    SPageLogBuilder getPageLog(final ActionType actionType, final String message) {
        final SPageLogBuilder logBuilder = new SPageLogBuilderImpl();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    @Override
    public SPage getPage(final long pageId) throws SBonitaReadException, SObjectNotFoundException {
        final SPage page = persistenceService.selectById(new SelectByIdDescriptor<SPage>(QUERY_GET_PAGE_BY_ID, SPage.class, pageId));
        if (page == null) {
            throw new SObjectNotFoundException("Page with id " + pageId + " not found");
        }
        return page;
    }

    @Override
    public SPage getPageByName(final String pageName) throws SBonitaReadException {
        return persistenceService.selectOne(new SelectOneDescriptor<SPage>(QUERY_GET_PAGE_BY_NAME, Collections.singletonMap("pageName",
                (Object) pageName), SPage.class));
    }

    @Override
    public long getNumberOfPages(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SPage.class, options, null);
    }

    @Override
    public List<SPage> searchPages(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.searchEntity(SPage.class, options, null);
    }

    @Override
    public void deletePage(final long pageId) throws SObjectModificationException, SObjectNotFoundException {
        try {
            final SPage page = getPage(pageId);
            deletePage(page);
        } catch (final SBonitaReadException sbe) {
            throw new SObjectModificationException(sbe);
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
        } catch (SRecorderException | SBonitaReadException | SProfileEntryNotFoundException | SProfileEntryDeletionException re) {
            initiateLogBuilder(sPage.getId(), SQueriableLog.STATUS_FAIL, logBuilder, METHOD_DELETE_PAGE);
            throw new SObjectModificationException(re);
        }
    }

    private void deleteProfileEntry(final SPage sPage) throws SBonitaReadException, SProfileEntryNotFoundException, SProfileEntryDeletionException {
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

    private void deleteParentIfNoMoreChildren(final SProfileEntry sProfileEntry) throws SBonitaReadException, SProfileEntryNotFoundException,
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
        }
        return null;
    }

    private SDeleteEvent getDeleteEvent(final Object object, final String type) {
        if (eventService.hasHandlers(type, EventActionType.DELETED)) {
            return (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(type).setObject(object).done();
        }
        return null;
    }

    private SUpdateEvent getUpdateEvent(final Object object, final String type) {
        if (eventService.hasHandlers(type, EventActionType.UPDATED)) {
            return (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(type).setObject(object).done();
        }
        return null;
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
        final SPage page = getPage(pageId);
        final SPageContent pageContent = persistenceService.selectById(new SelectByIdDescriptor<SPageContent>(QUERY_GET_PAGE_CONTENT,
                SPageContent.class, pageId));
        if (pageContent == null) {
            throw new SObjectNotFoundException("Page with id " + pageId + " not found");
        }
        final byte[] content = pageContent.getContent();
        try {
            final Map<String, byte[]> contentAsMap = IOUtil.unzip(content);
            byte[] bytes = contentAsMap.get("page.properties");
            final Properties pageProperties = new Properties();
            if (bytes != null) {
                pageProperties.load(new ByteArrayInputStream(bytes));
            }
            pageProperties.put(PROPERTIES_NAME, page.getName());
            pageProperties.put(PROPERTIES_DISPLAY_NAME, page.getDisplayName());
            pageProperties.put(PROPERTIES_DESCRIPTION, page.getDescription());
            contentAsMap.put("page.properties", IOUtil.getPropertyAsString(pageProperties, "The name must start with 'custompage_'"));

            return IOUtil.zip(contentAsMap);
        } catch (final IOException e) {
            throw new SBonitaReadException("the page is not a valid zip file", e);
        }

    }

    @Override
    public SPage updatePage(final long pageId, final EntityUpdateDescriptor entityUpdateDescriptor) throws SObjectModificationException,
            SObjectAlreadyExistsException, SInvalidPageTokenException {
        final SPageLogBuilder logBuilder = getPageLog(ActionType.UPDATED, "Update a page with id " + pageId);
        final String logMethodName = METHOD_UPDATE_PAGE;
        try {
            checkPageDuplicate(pageId, entityUpdateDescriptor, logBuilder, logMethodName);

            final SPage sPage = persistenceService.selectById(new SelectByIdDescriptor<SPage>(QUERY_GET_PAGE_BY_ID, SPage.class, pageId));
            final String oldPageName = sPage.getName();
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(sPage,
                    entityUpdateDescriptor);

            final SUpdateEvent updatePageEvent = getUpdateEvent(sPage, PAGE);
            recorder.recordUpdate(updateRecord, updatePageEvent);
            updatePageNameInProfileEntry(entityUpdateDescriptor, oldPageName);

            initiateLogBuilder(pageId, SQueriableLog.STATUS_OK, logBuilder, logMethodName);
            return sPage;
        } catch (SRecorderException | SBonitaReadException | SProfileEntryUpdateException e) {
            initiateLogBuilder(pageId, SQueriableLog.STATUS_FAIL, logBuilder, logMethodName);
            throw new SObjectModificationException(e);
        }

    }

    protected void updatePageNameInProfileEntry(EntityUpdateDescriptor entityUpdateDescriptor, String oldPageName) throws SInvalidPageTokenException,
            SBonitaReadException, SProfileEntryUpdateException {
        if (entityUpdateDescriptor.getFields().containsKey(SPageFields.PAGE_NAME)) {
            // page name has changed
            final String newPageName = entityUpdateDescriptor.getFields().get(SPageFields.PAGE_NAME).toString();
            checkPageNameIsValid(newPageName, false);
            updateProfileEntry(oldPageName, newPageName);
        }
    }

    protected void checkPageDuplicate(long pageId, EntityUpdateDescriptor entityUpdateDescriptor, SPageLogBuilder logBuilder, String logMethodName)
            throws SBonitaReadException, SObjectAlreadyExistsException {
        if (entityUpdateDescriptor.getFields().containsKey(SPageFields.PAGE_NAME)) {
            final SPage pageByName = getPageByName(entityUpdateDescriptor.getFields().get(SPageFields.PAGE_NAME).toString());
            if (null != pageByName && pageByName.getId() != pageId) {
                initiateLogBuilder(pageId, SQueriableLog.STATUS_FAIL, logBuilder, logMethodName);
                throwAlreadyExistsException(pageByName.getName());
            }
        }
    }

    private void updateProfileEntry(final String oldPageName, final String newPageName) throws SBonitaReadException, SProfileEntryUpdateException {
        if (newPageName.equals(oldPageName)) {
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
    public void updatePageContent(final long pageId, final byte[] content, final String contentName) throws SObjectModificationException,
            SInvalidPageZipException, SInvalidPageTokenException, SObjectAlreadyExistsException {
        final SPageLogBuilder logBuilder = getPageLog(ActionType.UPDATED, "Update a page with name " + pageId);
        final Properties pageProperties = readPageZip(content, false);
        try {

            final SPageContent sPageContent = persistenceService.selectById(new SelectByIdDescriptor<SPageContent>(QUERY_GET_PAGE_CONTENT,
                    SPageContent.class, pageId));
            final SPageUpdateContentBuilder builder = BuilderFactory.get(SPageUpdateContentBuilderFactory.class)
                    .createNewInstance(new EntityUpdateDescriptor());
            builder.updateContent(content);
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(sPageContent,
                    builder.done());
            final SUpdateEvent updatePageEvent = getUpdateEvent(sPageContent, PAGE);

            recorder.recordUpdate(updateRecord, updatePageEvent);

            initiateLogBuilder(pageId, SQueriableLog.STATUS_OK, logBuilder, METHOD_UPDATE_PAGE);

        } catch (SRecorderException | SBonitaReadException re) {
            initiateLogBuilder(pageId, SQueriableLog.STATUS_FAIL, logBuilder, METHOD_UPDATE_PAGE);
            throw new SObjectModificationException(re);
        }
        final SPageUpdateBuilder pageBuilder = BuilderFactory.get(SPageUpdateBuilderFactory.class)
                .createNewInstance(new EntityUpdateDescriptor());
        pageBuilder.updateContentName(contentName);
        pageBuilder.updateDescription(pageProperties.getProperty(PROPERTIES_DESCRIPTION));
        pageBuilder.updateDisplayName(pageProperties.getProperty(PROPERTIES_DISPLAY_NAME));
        pageBuilder.updateName(pageProperties.getProperty(PROPERTIES_NAME));
        updatePage(pageId, pageBuilder.done());

    }

    @Override
    public void start() throws SBonitaException {
        importProvidedPage("bonita-html-page-example.zip");
        importProvidedPage("bonita-groovy-page-example.zip");
        importProvidedPage("bonita-home-page.zip");
        importProvidedPage("step-autogenerated-form.zip");
        importProvidedPage("process-autogenerated-form.zip");
        importProvidedPage("case-autogenerated-overview.zip");
    }

    private void importProvidedPage(final String zipName) throws SBonitaReadException, SObjectCreationException, SObjectAlreadyExistsException,
            SObjectNotFoundException,
            SObjectModificationException, SInvalidPageZipException, SInvalidPageTokenException {
        try {
            // check if the provided pages are here or not up to date and import them from class path if needed
            final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(zipName);
            if (inputStream == null) {
                // no provided page
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, "No provided-" + zipName + " found in the class path, nothing will be imported");
                return;
            }
            final byte[] providedPageContent = IOUtil.getAllContentFrom(inputStream);
            final Properties pageProperties = readPageZip(providedPageContent, true);

            final SPage pageByName = getPageByName(pageProperties.getProperty(PROPERTIES_NAME));
            if (pageByName == null) {
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Provided page was not imported, importing it.");
                insertPage(createPage(pageProperties.getProperty(PageService.PROPERTIES_NAME), pageProperties.getProperty(PageService.PROPERTIES_DISPLAY_NAME),
                        pageProperties.getProperty(PageService.PROPERTIES_DESCRIPTION), zipName, -1, true, SContentType.PAGE), providedPageContent);
                return;
            }
            final byte[] pageContent = getPageContent(pageByName.getId());
            if (pageContent.length != providedPageContent.length) {
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Provided page exists but the content is not up to date, updating it.");
                // think of a better way to check the content are the same or not, it will almost always be the same so....
                updatePageContent(pageByName.getId(), providedPageContent, zipName);
            } else {
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Provided page exists and is up to date, do nothing");
            }
        } catch (final IOException e) {
            logger.log(getClass(), TechnicalLogSeverity.WARNING, "Provided page " + zipName + "can't be imported");
        }
    }

    @Override
    public void stop() {
        // nothing to do
    }

    @Override
    public void pause() {
        // nothing to do
    }

    @Override
    public void resume() {
        // nothing to do
    }

}
