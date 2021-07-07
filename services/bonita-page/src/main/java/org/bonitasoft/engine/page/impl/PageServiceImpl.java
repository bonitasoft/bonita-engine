/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.ExceptionUtils;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SDeletionException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.PageServiceListener;
import org.bonitasoft.engine.page.SContentType;
import org.bonitasoft.engine.page.SInvalidPageTokenException;
import org.bonitasoft.engine.page.SInvalidPageZipException;
import org.bonitasoft.engine.page.SInvalidPageZipInconsistentException;
import org.bonitasoft.engine.page.SInvalidPageZipMissingAPropertyException;
import org.bonitasoft.engine.page.SInvalidPageZipMissingIndexException;
import org.bonitasoft.engine.page.SInvalidPageZipMissingPropertiesException;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageLogBuilder;
import org.bonitasoft.engine.page.SPageUpdateBuilder;
import org.bonitasoft.engine.page.SPageUpdateBuilderFactory;
import org.bonitasoft.engine.page.SPageWithContent;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadOnlySelectByIdDescriptor;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.profile.ProfileService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
@Slf4j
@Service("pageService")
//must be initialized before ApplicationImporter
@Order(4)
public class PageServiceImpl implements PageService {

    private static final String QUERY_GET_PAGE_BY_NAME = "getPageByName";

    private static final String QUERY_GET_PAGE_BY_NAME_AND_PROCESS_DEFINITION_ID = "getPageByNameAndProcessDefinitionId";

    private static final String QUERY_GET_PAGE_BY_PROCESS_DEFINITION_ID = "getPageByProcessDefinitionId";

    private static final String METHOD_DELETE_PAGE = "deletePage";

    private static final String METHOD_NAME_ADD_PAGE = "addPage";

    private static final String METHOD_UPDATE_PAGE = "updatePage";

    public static final String PAGE_TOKEN_PREFIX = "custompage_";

    public static final String INDEX_GROOVY = "Index.groovy";

    public static final String INDEX_HTML = "index.html";

    public static final String RESOURCES_INDEX_HTML = "resources/index.html";

    private static final String API_EXTENSIONS = "apiExtensions";

    // Refers to a compiled class inside a jar, used by REST API extensions in compiled mode
    private static final String COMPILED_CLASS_NAME = "className";

    // Refers to a Groovy source file, used by REST API extensions in legacy mode (compilation is performed at runtime)
    private static final String GROOVY_CLASS_FILENAME = "classFileName";

    private static final String THEME_CSS = "resources/theme.css";

    private static final String EDITABLE_REMOVABLE_RESOURCES_PATH = "org/bonitasoft/web/page";

    private static final String NON_EDITABLE_NON_REMOVABLE_RESOURCES_PATH = "org/bonitasoft/web/page/final";

    private static final String EDITABLE_NON_REMOVABLE_RESOURCES_PATH = "org/bonitasoft/web/page/editonly";

    private static final String TENANT_STATUS_PAGE_NAME = "custompage_tenantStatusBonita";

    private final ReadPersistenceService persistenceService;

    private final Recorder recorder;

    private final QueriableLoggerService queriableLoggerService;

    private final ProfileService profileService;

    private final ResourcePatternResolver cpResourceResolver = new PathMatchingResourcePatternResolver(
            PageServiceImpl.class.getClassLoader());

    private final SPageContentHelper helper;

    private List<PageServiceListener> pageServiceListeners;

    // Used only in tests
    private boolean initialized = false;

    public PageServiceImpl(final ReadPersistenceService persistenceService, final Recorder recorder,
            final QueriableLoggerService queriableLoggerService,
            final ProfileService profileService) {
        this.persistenceService = persistenceService;
        this.recorder = recorder;
        this.queriableLoggerService = queriableLoggerService;
        this.profileService = profileService;
        helper = new SPageContentHelper();
    }

    @Override
    public SPage addPage(final SPage page, final byte[] content)
            throws SObjectCreationException, SObjectAlreadyExistsException,
            SInvalidPageZipException, SInvalidPageTokenException {
        try {
            checkZipContainsRequiredEntries(unzip(content));
            checkPageNameIsValid(page.getName(), page.isProvided());
            checkPageDisplayNameIsValid(page.getDisplayName());
            return insertPage(page, content);
        } catch (final IOException e) {
            throw new SInvalidPageZipInconsistentException("Error while reading zip file", e);
        }
    }

    // @VisibleForTesting
    Map<String, byte[]> unzip(byte[] content) throws IOException {
        return IOUtil.unzip(content);
    }

    @Override
    public SPage addPage(final byte[] content, final String contentName, final long userId)
            throws SObjectCreationException, SObjectAlreadyExistsException, SInvalidPageZipException,
            SInvalidPageTokenException {
        return addPage(content, contentName, userId, false, false);
    }

    @Override
    public SPage getPageByNameAndProcessDefinitionId(final String name, final long processDefinitionId)
            throws SBonitaReadException {
        final Map<String, Object> inputParameters = new HashMap<>();
        inputParameters.put("pageName", name);
        inputParameters.put("processDefinitionId", processDefinitionId);
        return persistenceService.selectOne(new SelectOneDescriptor<>(QUERY_GET_PAGE_BY_NAME_AND_PROCESS_DEFINITION_ID,
                inputParameters, SPage.class));
    }

    @Override
    public List<SPage> getPageByProcessDefinitionId(final long processDefinitionId, final int fromIndex,
            final int numberOfResults)
            throws SBonitaReadException {
        final Map<String, Object> inputParameters = new HashMap<>();
        inputParameters.put("processDefinitionId", processDefinitionId);
        final OrderByOption orderByOption = new OrderByOption(SPage.class, SPageFields.PAGE_NAME, OrderByType.ASC);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfResults,
                Collections.singletonList(orderByOption));
        return persistenceService.selectList(
                new SelectListDescriptor<>(QUERY_GET_PAGE_BY_PROCESS_DEFINITION_ID, inputParameters, SPage.class,
                        queryOptions));
    }

    private SPage addPage(final byte[] content, final String contentName, final long userId, final boolean provided,
            boolean hidden) throws SInvalidPageZipException,
            SInvalidPageTokenException, SObjectAlreadyExistsException, SObjectCreationException {
        final Properties pageProperties = readPageZip(content, provided);
        final SPage page = buildPage(pageProperties.getProperty(PageService.PROPERTIES_NAME),
                pageProperties.getProperty(PageService.PROPERTIES_DISPLAY_NAME),
                pageProperties.getProperty(PageService.PROPERTIES_DESCRIPTION), contentName, userId, provided, hidden,
                true, true,
                pageProperties.getProperty(PROPERTIES_CONTENT_TYPE, SContentType.PAGE));
        return insertPage(page, content);
    }

    @Override
    public Properties readPageZip(final byte[] content)
            throws SInvalidPageZipMissingIndexException, SInvalidPageZipMissingAPropertyException,
            SInvalidPageZipInconsistentException, SInvalidPageZipMissingPropertiesException,
            SInvalidPageTokenException {
        return readPageZip(content, false);
    }

    Properties readPageZip(final byte[] content, final boolean provided)
            throws SInvalidPageZipMissingIndexException, SInvalidPageZipMissingAPropertyException,
            SInvalidPageZipInconsistentException, SInvalidPageZipMissingPropertiesException,
            SInvalidPageTokenException {
        final Properties pageProperties;
        if (content == null) {
            throw new SInvalidPageZipInconsistentException("Content can't be null");
        }
        try {
            final Map<String, byte[]> zipContent = unzip(content);
            pageProperties = helper.loadPageProperties(zipContent);
            if (isAnAPIExtension(pageProperties)) {
                checkApiControllerExists(zipContent, pageProperties);
            } else {
                checkZipContainsRequiredEntries(zipContent);
            }
            checkPageNameIsValid(pageProperties.getProperty(PageService.PROPERTIES_NAME), provided);
            checkPageDisplayNameIsValid(pageProperties.getProperty(PageService.PROPERTIES_DISPLAY_NAME));
        } catch (final IOException e) {
            throw new SInvalidPageZipInconsistentException("Error while reading zip file", e);
        }
        return pageProperties;
    }

    /**
     * For each declared API, the following verifications are performed:
     * - If a property 'className' is defined, then we assume that the required jar file is present in the lib folder
     * and no
     * further verification is performed.
     * - If the property 'className' isn't defined, then we look for the property 'classFileName' (legacy mode). The
     * property
     * points to a Groovy source file, which will be compiled at runtime. We verify that this Groovy file exists in the
     * archive.
     */
    private void checkApiControllerExists(Map<String, byte[]> zipContent, Properties pageProperties)
            throws SInvalidPageZipInconsistentException, SInvalidPageZipMissingAPropertyException {
        final Set<String> entrySet = zipContent.keySet();
        final String declaredApis = pageProperties.getProperty(API_EXTENSIONS);
        if (declaredApis == null || declaredApis.isEmpty()) {
            throw new SInvalidPageZipMissingAPropertyException(API_EXTENSIONS);
        }
        final String[] apis = declaredApis.split(",");
        for (final String api : apis) {
            String className = pageProperties.getProperty(api.trim() + "." + COMPILED_CLASS_NAME);
            if (className == null) {
                final String classFileName = pageProperties.getProperty(api.trim() + "." + GROOVY_CLASS_FILENAME);
                if (classFileName == null || classFileName.isEmpty()) {
                    throw new SInvalidPageZipMissingAPropertyException(api.trim() + "." + GROOVY_CLASS_FILENAME);
                }
                if (!entrySet.contains(classFileName.trim())) {
                    throw new SInvalidPageZipInconsistentException(
                            String.format("RestAPIController %s has not been found in archive.", classFileName.trim()));
                }
            }
        }
    }

    private boolean isAnAPIExtension(Properties pageProperties) {
        return Objects.equals(SContentType.API_EXTENSION, pageProperties.get(PageService.PROPERTIES_CONTENT_TYPE));
    }

    SPage insertPage(final SPage page, final byte[] content)
            throws SObjectAlreadyExistsException, SObjectCreationException {
        final SPageLogBuilder logBuilder = getPageLog(ActionType.CREATED,
                "Adding a new page with name " + page.getName());
        try {
            final SPageWithContent pageContent = new SPageWithContent(page, content);
            final SPage pageByName = checkIfPageAlreadyExists(page);
            if (null != pageByName) {
                initiateLogBuilder(page.getId(), SQueriableLog.STATUS_FAIL, logBuilder, METHOD_NAME_ADD_PAGE);
                throwAlreadyExistsException(pageByName.getName());
            }
            recorder.recordInsert(new InsertRecord(pageContent), PAGE);
            page.setId(pageContent.getId());
            notifyPageInsert(page, content);
            return page;
        } catch (final SObjectCreationException ce) {
            throw ce;
        } catch (SRecorderException | SBonitaReadException re) {
            throw new SObjectCreationException(re);
        }
    }

    private void notifyPageInsert(final SPage page, final byte[] content) throws SObjectCreationException {
        for (final PageServiceListener pageServiceListener : pageServiceListeners) {
            pageServiceListener.pageInserted(page, content);
        }

    }

    private SPage checkIfPageAlreadyExists(final SPage page) throws SBonitaReadException {
        SPage existingPage;
        if (page.getProcessDefinitionId() > 0) {
            existingPage = getPageByNameAndProcessDefinitionId(page.getName(), page.getProcessDefinitionId());
        } else {
            existingPage = getPageByName(page.getName());
        }
        return existingPage;
    }

    private void checkPageDisplayNameIsValid(final String displayName) throws SInvalidPageZipMissingPropertiesException,
            SInvalidPageZipMissingAPropertyException {
        if (displayName == null || displayName.length() == 0) {
            throw new SInvalidPageZipMissingAPropertyException(PageService.PROPERTIES_DISPLAY_NAME);
        }
    }

    private void checkPageNameIsValid(final String name, final boolean provided) throws SInvalidPageTokenException {
        if (name == null || name.isEmpty() || !provided && !name.matches(PAGE_TOKEN_PREFIX + "\\p{Alnum}+")) {
            throw new SInvalidPageTokenException(
                    "Page name is not valid, it must contains only alpha numeric characters and start with "
                            + PAGE_TOKEN_PREFIX);
        }
    }

    void checkZipContainsRequiredEntries(final Map<String, byte[]> zipContent)
            throws SInvalidPageZipMissingIndexException {
        final Set<String> entrySet = zipContent.keySet();
        for (final String entry : entrySet) {
            if (INDEX_GROOVY.equals(entry)
                    || INDEX_HTML.equalsIgnoreCase(entry)
                    || RESOURCES_INDEX_HTML.equalsIgnoreCase(entry)
                    || THEME_CSS.equalsIgnoreCase(entry)) {
                return;
            }
        }
        throw new SInvalidPageZipMissingIndexException();
    }

    private SPage buildPage(final String name, final String displayName, final String description,
            final String contentName, final long creatorUserId,
            final boolean provided, boolean hidden, boolean removable, boolean editable, final String contentType) {
        long currentTime = System.currentTimeMillis();
        return SPage.builder().name(name).description(description).displayName(displayName)
                .installationDate(currentTime).installedBy(creatorUserId).provided(provided)
                .lastModificationDate(currentTime).lastUpdatedBy(creatorUserId)
                .hidden(hidden)
                .removable(removable)
                .editable(editable)
                .contentName(contentName)
                .contentType(contentType)
                .build();
    }

    SPageLogBuilder getPageLog(final ActionType actionType, final String message) {
        final SPageLogBuilder logBuilder = new SPageLogBuilderImpl();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    @Override
    public SPage getPage(final long pageId) throws SBonitaReadException, SObjectNotFoundException {
        final SPage page = persistenceService.selectById(new SelectByIdDescriptor<>(SPage.class, pageId));
        if (page == null) {
            throw new SObjectNotFoundException("Page with id " + pageId + " not found");
        }
        return page;
    }

    @Override
    public SPage getPageByName(final String pageName) throws SBonitaReadException {
        return persistenceService
                .selectOne(new SelectOneDescriptor<SPage>(QUERY_GET_PAGE_BY_NAME, Collections.singletonMap("pageName",
                        pageName), SPage.class));
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
            deletePageIfRemovable(page);
        } catch (final SBonitaReadException sbe) {
            throw new SObjectModificationException(sbe);
        }
    }

    private void deletePageIfRemovable(final SPage sPage) throws SObjectModificationException {
        if (!sPage.isRemovable()) {
            throw new SObjectModificationException(
                    "The page " + sPage.getName() + " cannot be deleted because it is set as non-removable");
        } else {
            deletePage(sPage);
        }
    }

    private void deletePage(final SPage sPage) throws SObjectModificationException {
        final SPageLogBuilder logBuilder = getPageLog(ActionType.DELETED, "Deleting page named: " + sPage.getName());
        try {
            deleteProfileEntry(sPage);
            for (final PageServiceListener pageServiceListener : pageServiceListeners) {
                pageServiceListener.pageDeleted(sPage);
            }
            recorder.recordDelete(new DeleteRecord(sPage), PAGE);
            initiateLogBuilder(sPage.getId(), SQueriableLog.STATUS_OK, logBuilder, METHOD_DELETE_PAGE);
        } catch (SRecorderException | SBonitaReadException | SProfileEntryNotFoundException
                | SProfileEntryDeletionException | SDeletionException re) {
            initiateLogBuilder(sPage.getId(), SQueriableLog.STATUS_FAIL, logBuilder, METHOD_DELETE_PAGE);
            throw new SObjectModificationException(re);
        }
    }

    private void deleteProfileEntry(final SPage sPage)
            throws SBonitaReadException, SProfileEntryNotFoundException, SProfileEntryDeletionException {
        final List<OrderByOption> orderByOptions = Collections
                .singletonList(new OrderByOption(SProfileEntry.class, SProfileEntry.INDEX, OrderByType.ASC));
        final List<FilterOption> filters = new ArrayList<>();
        filters.add(new FilterOption(SProfileEntry.class, SProfileEntry.PAGE, sPage.getName()));
        filters.add(new FilterOption(SProfileEntry.class, SProfileEntry.CUSTOM, new Boolean(true)));

        final QueryOptions queryOptions = new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS, orderByOptions,
                filters, null);

        final List<SProfileEntry> searchProfileEntries = profileService.searchProfileEntries(queryOptions);
        for (final SProfileEntry sProfileEntry : searchProfileEntries) {
            profileService.deleteProfileEntry(sProfileEntry.getId());
            if (sProfileEntry.getParentId() > 0) {
                deleteParentIfNoMoreChildren(sProfileEntry);
            }
        }
    }

    private void deleteParentIfNoMoreChildren(final SProfileEntry sProfileEntry)
            throws SBonitaReadException, SProfileEntryNotFoundException,
            SProfileEntryDeletionException {
        final List<OrderByOption> orderByOptions = Collections
                .singletonList(new OrderByOption(SProfileEntry.class, SProfileEntry.INDEX, OrderByType.ASC));
        final List<FilterOption> filters = new ArrayList<>();
        filters.add(new FilterOption(SProfileEntry.class, SProfileEntry.PROFILE_ID, sProfileEntry.getProfileId()));
        filters.add(new FilterOption(SProfileEntry.class, SProfileEntry.PARENT_ID, sProfileEntry.getParentId()));

        final QueryOptions queryOptions = new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS, orderByOptions,
                filters, null);

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

    void initiateLogBuilder(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder,
            final String methodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.build();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), methodName, log);
        }
    }

    @Override
    public byte[] getPageContent(final long pageId) throws SBonitaReadException, SObjectNotFoundException {
        final SPageWithContent page = persistenceService
                .selectById(new ReadOnlySelectByIdDescriptor<>(SPageWithContent.class, pageId));
        if (page == null) {
            throw new SObjectNotFoundException("Page with id " + pageId + " not found");
        }
        final byte[] content = page.getContent();
        try {
            final Map<String, byte[]> contentAsMap = unzip(content);
            final byte[] bytes = contentAsMap.get("page.properties");
            final Properties pageProperties = new Properties();
            if (bytes != null) {
                pageProperties.load(new ByteArrayInputStream(bytes));
            }
            pageProperties.put(PROPERTIES_NAME, page.getName());
            pageProperties.put(PROPERTIES_DISPLAY_NAME, page.getDisplayName());
            if (page.getDescription() != null) {
                pageProperties.put(PROPERTIES_DESCRIPTION, page.getDescription());
            }
            contentAsMap.put("page.properties",
                    IOUtil.getPropertyAsString(pageProperties, "The name must start with 'custompage_'"));

            return IOUtil.zip(contentAsMap);
        } catch (final IOException e) {
            throw new SBonitaReadException("the page is not a valid zip file", e);
        }

    }

    @Override
    public SPage updatePage(final long pageId, final EntityUpdateDescriptor entityUpdateDescriptor)
            throws SObjectModificationException,
            SObjectAlreadyExistsException, SInvalidPageTokenException {
        final SPageLogBuilder logBuilder = getPageLog(ActionType.UPDATED, "Update a page with id " + pageId);
        final String logMethodName = METHOD_UPDATE_PAGE;
        try {

            final SPage sPage = persistenceService.selectById(new SelectByIdDescriptor<>(SPage.class, pageId));
            if (!sPage.isEditable()) {
                throw new SObjectModificationException(
                        "The page " + sPage.getName() + " cannot be modified because it is set as not modifiable");
            }
            checkPageDuplicate(sPage, entityUpdateDescriptor, logBuilder, logMethodName);
            final String oldPageName = sPage.getName();
            recorder.recordUpdate(UpdateRecord.buildSetFields(sPage, entityUpdateDescriptor), PAGE);
            updatePageNameInProfileEntry(entityUpdateDescriptor, oldPageName);

            initiateLogBuilder(pageId, SQueriableLog.STATUS_OK, logBuilder, logMethodName);
            return sPage;
        } catch (SRecorderException | SBonitaReadException | SProfileEntryUpdateException e) {
            initiateLogBuilder(pageId, SQueriableLog.STATUS_FAIL, logBuilder, logMethodName);
            throw new SObjectModificationException(e);
        }

    }

    protected void updatePageNameInProfileEntry(final EntityUpdateDescriptor entityUpdateDescriptor,
            final String oldPageName)
            throws SInvalidPageTokenException,
            SBonitaReadException, SProfileEntryUpdateException {
        if (entityUpdateDescriptor.getFields().containsKey(SPageFields.PAGE_NAME)) {
            // page name has changed
            final String newPageName = entityUpdateDescriptor.getFields().get(SPageFields.PAGE_NAME).toString();
            checkPageNameIsValid(newPageName, false);
            updateProfileEntry(oldPageName, newPageName);
        }
    }

    protected void checkPageDuplicate(final SPage sPage, final EntityUpdateDescriptor entityUpdateDescriptor,
            final SPageLogBuilder logBuilder,
            final String logMethodName)
            throws SBonitaReadException, SObjectAlreadyExistsException {
        if (entityUpdateDescriptor.getFields().containsKey(SPageFields.PAGE_NAME)
                || entityUpdateDescriptor.getFields().containsKey(SPageFields.PAGE_PROCESS_DEFINITION_ID)) {
            String sPageName = sPage.getName();
            long sPageProcessDefinitionId = sPage.getProcessDefinitionId();
            if (entityUpdateDescriptor.getFields().containsKey(SPageFields.PAGE_NAME)) {
                sPageName = entityUpdateDescriptor.getFields().get(SPageFields.PAGE_NAME).toString();
            }
            if (entityUpdateDescriptor.getFields().containsKey(SPageFields.PAGE_PROCESS_DEFINITION_ID)) {
                sPageProcessDefinitionId = Long.parseLong(
                        entityUpdateDescriptor.getFields().get(SPageFields.PAGE_PROCESS_DEFINITION_ID).toString());
            }

            final SPage page;
            if (sPageProcessDefinitionId > 0) {
                page = getPageByNameAndProcessDefinitionId(sPageName, sPageProcessDefinitionId);
            } else {
                page = getPageByName(sPageName);
            }
            if (null != page && page.getId() != sPage.getId()) {
                initiateLogBuilder(sPage.getId(), SQueriableLog.STATUS_FAIL, logBuilder, logMethodName);
                throwAlreadyExistsException(page.getName());
            }
        }
    }

    private void updateProfileEntry(final String oldPageName, final String newPageName)
            throws SBonitaReadException, SProfileEntryUpdateException {
        if (newPageName.equals(oldPageName)) {
            return;
        }
        final List<FilterOption> filters = new ArrayList<>();
        filters.add(new FilterOption(SProfileEntry.class, SProfileEntry.PAGE, oldPageName));
        final QueryOptions queryOptions = new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS, Collections
                .singletonList(new OrderByOption(SProfileEntry.class, SProfileEntry.INDEX, OrderByType.ASC)), filters,
                null);
        final List<SProfileEntry> searchProfileEntries = profileService.searchProfileEntries(queryOptions);
        for (final SProfileEntry sProfileEntry : searchProfileEntries) {
            final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
            entityUpdateDescriptor.addField(SProfileEntry.NAME, sProfileEntry.getName());
            entityUpdateDescriptor.addField(SProfileEntry.PAGE, newPageName);
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
    public void updatePageContent(final long pageId, final byte[] content, final String contentName)
            throws SObjectModificationException,
            SInvalidPageZipException, SInvalidPageTokenException, SObjectAlreadyExistsException {
        final SPageLogBuilder logBuilder = getPageLog(ActionType.UPDATED, "Update a page with name " + pageId);
        final Properties pageProperties = readPageZip(content, false);
        final SPageWithContent sPageContent;
        try {
            sPageContent = persistenceService.selectById(new SelectByIdDescriptor<>(
                    SPageWithContent.class, pageId));
            EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
            entityUpdateDescriptor.addField("content", content);
            recorder.recordUpdate(UpdateRecord.buildSetFields(sPageContent,
                    entityUpdateDescriptor), PAGE);

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
        pageBuilder.updateContentType(pageProperties.getProperty(PROPERTIES_CONTENT_TYPE, SContentType.PAGE));
        if (sPageContent.isProvided()) {
            //update the md5 sum of the page only if it is provided
            pageBuilder.updatePageHash(DigestUtils.md5DigestAsHex(content));
        }
        final SPage sPage = updatePage(pageId, pageBuilder.done());
        for (final PageServiceListener pageServiceListener : pageServiceListeners) {
            pageServiceListener.pageUpdated(sPage, content);
        }
    }

    @Override
    public void init() throws SBonitaException {
        try {
            importProvidedNonRemovableNonEditablePagesFromClasspath();
            importProvidedNonRemovableEditablePagesFromClasspath();
            importProvidedPagesFromClasspath();
        } catch (IOException e) {
            log.error("Cannot load provided pages at startup. Root cause: {}", ExceptionUtils.printRootCauseOnly(e));
            log.debug("Full stack : ", e);
        }
        initialized = true;
    }

    private void importProvidedNonRemovableNonEditablePagesFromClasspath() throws IOException {
        importProvidedPagesFromResourcePattern(false, false, NON_EDITABLE_NON_REMOVABLE_RESOURCES_PATH);
    }

    private void importProvidedNonRemovableEditablePagesFromClasspath() throws IOException {
        importProvidedPagesFromResourcePattern(false, true, EDITABLE_NON_REMOVABLE_RESOURCES_PATH);
    }

    private void importProvidedPagesFromClasspath() throws IOException {
        importProvidedPagesFromResourcePattern(true, true, EDITABLE_REMOVABLE_RESOURCES_PATH);
    }

    private void importProvidedPagesFromResourcePattern(boolean removable, boolean editable, String resourcesPath)
            throws IOException {
        Resource[] resources = cpResourceResolver
                .getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/" + resourcesPath + "/*.zip");
        for (Resource resource : resources) {
            if (resource.exists() && resource.isReadable() && resource.contentLength() > 0) {
                importProvidedPagesFromResource(resource, removable, editable);
            } else {
                log.warn("A resource {} could not be read when loading default pages", resource.getDescription());
            }
        }
    }

    private void importProvidedPagesFromResource(Resource resource, boolean removable, boolean editable) {
        String resourceName = resource.getFilename();
        log.debug("Found provided page '{}' in classpath", resourceName);
        try (InputStream resourceAsStream = resource.getInputStream()) {
            final byte[] content = org.apache.commons.io.IOUtils.toByteArray(resourceAsStream);
            final Properties pageProperties = readPageZip(content, true);
            importProvidedPage(resourceName, content, pageProperties, removable, editable);
        } catch (IOException | SBonitaException e) {
            log.error("Unable to import the page {} because: {}", resourceName,
                    ExceptionUtils.printLightWeightStacktrace(e));
            log.debug("Stacktrace of the import issue is:", e);
        }
    }

    private void importProvidedPage(String pageZipName, final byte[] providedPageContent,
            final Properties pageProperties, boolean removable, boolean editable) throws SBonitaException {
        final SPage pageByName = getPageByName(pageProperties.getProperty(PROPERTIES_NAME));
        if (pageByName == null) {
            log.debug("Provided page {} does not exist yet, importing it.", pageZipName);
            createProvidedPage(pageZipName, providedPageContent, pageProperties, removable, editable);
        } else {
            if (!pageByName.isEditable()) {
                String md5Sum = DigestUtils.md5DigestAsHex(providedPageContent);
                if (Objects.equals(pageByName.getPageHash(), md5Sum)) {
                    log.debug("Provided page exists and is up to date, nothing to do");
                } else {
                    log.info("Provided page {} exists but the content is not up to date, updating it.", pageZipName);
                    updatePageContent(pageByName.getId(), providedPageContent, pageZipName);
                }
            } else {
                log.debug("Provided page exists, and will not be updated");
            }
        }
    }

    protected void createProvidedPage(String pageZipName, final byte[] providedPageContent,
            final Properties pageProperties, boolean removable, boolean editable) throws SObjectAlreadyExistsException,
            SObjectCreationException {
        boolean hidden = pageProperties.getProperty("name").equals(TENANT_STATUS_PAGE_NAME);
        final SPage page = buildPage(pageProperties.getProperty(PageService.PROPERTIES_NAME),
                pageProperties.getProperty(PageService.PROPERTIES_DISPLAY_NAME),
                pageProperties.getProperty(PageService.PROPERTIES_DESCRIPTION), pageZipName, -1, true,
                hidden, removable, editable,
                pageProperties.getProperty(PageService.PROPERTIES_CONTENT_TYPE, SContentType.PAGE));
        page.setPageHash(DigestUtils.md5DigestAsHex(providedPageContent));
        insertPage(page, providedPageContent);
    }

    @Override
    public void stop() throws SBonitaException {
        // nothing to do
    }

    @Override
    public void start() throws SBonitaException {
        // nohing to do
    }

    @Override
    public void pause() throws SBonitaException {
        // nothing to do
    }

    @Override
    public void resume() {
        // nothing to do
    }

    @Override
    public boolean initialized() {
        // Used only in tests
        return initialized;
    }

    public List<PageServiceListener> getPageServiceListeners() {
        return pageServiceListeners;
    }

    @Autowired
    public void setPageServiceListeners(final List<PageServiceListener> pageServiceListeners) {
        this.pageServiceListeners = pageServiceListeners;
    }

}
