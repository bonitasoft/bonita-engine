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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.authorization.PermissionService;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.ExceptionUtils;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SDeletionException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.page.AbstractSPage;
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
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadOnlySelectByIdDescriptor;
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
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
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

    private final ReadSessionAccessor sessionAccessor;
    private final SessionService sessionService;
    private final PermissionService permissionService;

    private final ResourcePatternResolver cpResourceResolver = new PathMatchingResourcePatternResolver(
            PageServiceImpl.class.getClassLoader());

    private final SPageContentHelper helper;

    private List<PageServiceListener> pageServiceListeners;

    // Used only in tests
    private boolean initialized = false;

    public PageServiceImpl(final ReadPersistenceService persistenceService, final Recorder recorder,
            final QueriableLoggerService queriableLoggerService, ReadSessionAccessor sessionAccessor,
            SessionService sessionService, PermissionService permissionService) {
        this.persistenceService = persistenceService;
        this.recorder = recorder;
        this.queriableLoggerService = queriableLoggerService;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.permissionService = permissionService;
        helper = new SPageContentHelper();
    }

    @Override
    public SPage addPage(final SPage page, final byte[] content)
            throws SObjectCreationException, SObjectAlreadyExistsException,
            SInvalidPageZipException, SInvalidPageTokenException {
        try {
            final Map<String, byte[]> zipContent = unzip(content);
            checkZipContainsRequiredEntries(zipContent);
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
        SPage sPage = buildPage(content, contentName, userId, false, true, true);
        return insertPage(sPage, content);
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

    private SPage buildPage(final byte[] content, final String contentName, final long userId, final boolean provided,
            boolean removable, boolean editable) throws SInvalidPageZipException,
            SInvalidPageTokenException {
        final Properties pageProperties = readPageZip(content, provided);
        long currentTime = System.currentTimeMillis();
        return SPage.builder().name(pageProperties.getProperty(PageService.PROPERTIES_NAME))
                .description(pageProperties.getProperty(PageService.PROPERTIES_DESCRIPTION))
                .displayName(pageProperties.getProperty(PageService.PROPERTIES_DISPLAY_NAME))
                .installationDate(currentTime).installedBy(userId).provided(provided)
                .lastModificationDate(currentTime).lastUpdatedBy(userId)
                .removable(removable).editable(editable)
                .contentName(contentName)
                .contentType(pageProperties.getProperty(PROPERTIES_CONTENT_TYPE, SContentType.PAGE)).build();

    }

    private void addPermissionsFromPageProperties(String pageName, Properties pageProperties) {
        permissionService.addPermissions(pageName, pageProperties);
    }

    private void removePermissionsDeclaredInPageProperties(Properties pageProperties) {
        permissionService.removePermissions(pageProperties);
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
            pageProperties = getPreviousPageProperties(content);
            final Map<String, byte[]> zipContent = unzip(content);
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
            addPermissionsFromPageProperties(page.getName(), getPreviousPageProperties(content));
            page.setId(pageContent.getId());
            notifyPageInsert(page, content);
            return page;
        } catch (SRecorderException | SBonitaReadException | IOException
                | SInvalidPageZipMissingPropertiesException re) {
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

    private void checkPageDisplayNameIsValid(final String displayName) throws SInvalidPageZipMissingAPropertyException {
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
                .selectOne(new SelectOneDescriptor<>(QUERY_GET_PAGE_BY_NAME, Collections.singletonMap("pageName",
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
                    "The page '" + sPage.getName() + "' cannot be deleted because it is non-removable");
        } else {
            deletePage(sPage);
        }
    }

    private void deletePage(final SPage sPage) throws SObjectModificationException {
        final SPageLogBuilder logBuilder = getPageLog(ActionType.DELETED, "Deleting page named: " + sPage.getName());
        try {
            // Need to read previous version permissions from page properties before deleting the page:
            Properties pageProperties = getPreviousPageProperties(sPage);

            for (final PageServiceListener pageServiceListener : pageServiceListeners) {
                pageServiceListener.pageDeleted(sPage);
            }
            recorder.recordDelete(new DeleteRecord(sPage), PAGE);
            removePermissionsDeclaredInPageProperties(pageProperties);
            initiateLogBuilder(sPage.getId(), SQueriableLog.STATUS_OK, logBuilder,
                    METHOD_DELETE_PAGE);
        } catch (SRecorderException | SBonitaReadException | SDeletionException | SObjectNotFoundException
                | SInvalidPageZipMissingPropertiesException | IOException re) {
            initiateLogBuilder(sPage.getId(), SQueriableLog.STATUS_FAIL, logBuilder, METHOD_DELETE_PAGE);
            throw new SObjectModificationException(re);
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
            final byte[] bytes = contentAsMap.get(PROPERTIES_FILE_NAME);
            final Properties pageProperties = new Properties();
            if (bytes != null) {
                pageProperties.load(new ByteArrayInputStream(bytes));
            }
            pageProperties.put(PROPERTIES_NAME, page.getName());
            pageProperties.put(PROPERTIES_DISPLAY_NAME, page.getDisplayName());
            if (page.getDescription() != null) {
                pageProperties.put(PROPERTIES_DESCRIPTION, page.getDescription());
            }
            contentAsMap.put(PROPERTIES_FILE_NAME,
                    IOUtil.getPropertyAsString(pageProperties, "The name must start with 'custompage_'"));

            return IOUtil.zip(contentAsMap);
        } catch (final IOException e) {
            throw new SBonitaReadException("the page is not a valid zip file", e);
        }
    }

    @Override
    public SPage updatePage(final long pageId, final EntityUpdateDescriptor entityUpdateDescriptor)
            throws SObjectModificationException, SObjectAlreadyExistsException, SInvalidPageTokenException {
        try {

            final SPage sPage = persistenceService.selectById(new SelectByIdDescriptor<>(SPage.class, pageId));
            updatePage(entityUpdateDescriptor, sPage);
            return sPage;
        } catch (SBonitaReadException e) {
            throw new SObjectModificationException(e);
        }
    }

    AbstractSPage updatePage(EntityUpdateDescriptor entityUpdateDescriptor, AbstractSPage sPage)
            throws SObjectModificationException, SObjectAlreadyExistsException {
        long pageId = sPage.getId();
        final SPageLogBuilder logBuilder = getPageLog(ActionType.UPDATED, "Update a page with id " + pageId);
        final String logMethodName = METHOD_UPDATE_PAGE;
        try {
            if (!sPage.isEditable() && !isSystemSession()) { // Only non-editable pages can be updated (by the system)
                throw new SObjectModificationException(
                        "The page '" + sPage.getName() + "' cannot be modified because it is not modifiable");
            }
            if (entityUpdateDescriptor.getFields().containsKey(SPageFields.PAGE_PROCESS_DEFINITION_ID)) {
                checkPageDuplicateForProcessDefinition(sPage, logBuilder, Long.parseLong(
                        entityUpdateDescriptor.getFields().get(SPageFields.PAGE_PROCESS_DEFINITION_ID).toString()));
            }
            recorder.recordUpdate(UpdateRecord.buildSetFields(sPage, entityUpdateDescriptor), PAGE);
            initiateLogBuilder(pageId, SQueriableLog.STATUS_OK, logBuilder, logMethodName);

            return sPage;
        } catch (SRecorderException | SBonitaReadException e) {
            initiateLogBuilder(pageId, SQueriableLog.STATUS_FAIL, logBuilder, logMethodName);
            throw new SObjectModificationException(e);
        }
    }

    // @VisibleForTesting
    protected boolean isSystemSession() {
        return sessionService.getLoggedUserFromSession(sessionAccessor) == SessionService.SYSTEM_ID;
    }

    protected void checkPageDuplicateForProcessDefinition(
            final AbstractSPage sPage, final SPageLogBuilder logBuilder, long sPageProcessDefinitionId)
            throws SBonitaReadException, SObjectAlreadyExistsException {

        String sPageName = sPage.getName();
        if (sPageProcessDefinitionId > 0) {
            SPage page = getPageByNameAndProcessDefinitionId(sPageName, sPageProcessDefinitionId);

            if (page != null && page.getId() != sPage.getId()) {
                initiateLogBuilder(sPage.getId(), SQueriableLog.STATUS_FAIL, logBuilder, METHOD_UPDATE_PAGE);
                throwAlreadyExistsException(page.getName());
            }
        }
    }

    private void throwAlreadyExistsException(final String pageName) throws SObjectAlreadyExistsException {
        throw new SObjectAlreadyExistsException("page with name " + pageName);
    }

    @Override
    public void updatePageContent(final long pageId, final byte[] content, final String contentName)
            throws SObjectModificationException,
            SInvalidPageZipException, SInvalidPageTokenException, SObjectAlreadyExistsException {
        updatePageContent(pageId, content, contentName, null);
    }

    @Override
    public void updatePageContent(long pageId, byte[] content, String contentName, SPageUpdateBuilder pageUpdateBuilder)
            throws SObjectModificationException, SInvalidPageZipException, SInvalidPageTokenException,
            SObjectAlreadyExistsException {

        final SPageLogBuilder logBuilder = getPageLog(ActionType.UPDATED, "Update a page with name " + pageId);
        final Properties pageProperties = readPageZip(content, false);
        final SPageWithContent sPageContent;
        try {
            sPageContent = persistenceService.selectById(new SelectByIdDescriptor<>(SPageWithContent.class, pageId));

            // Need to read previous version permissions from page properties before deleting the page:
            Properties previousPageProperties;
            try {
                previousPageProperties = getPreviousPageProperties(sPageContent.getContent());
            } catch (SInvalidPageZipMissingPropertiesException | IOException e) {
                previousPageProperties = null;
            }

            EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
            entityUpdateDescriptor.addField("content", content);
            recorder.recordUpdate(UpdateRecord.buildSetFields(sPageContent, entityUpdateDescriptor), PAGE);

            // remove previous permissions before adding the new ones:
            if (previousPageProperties != null) {
                // this can happen when update-tool puts empty content in new inserted pages,
                // so that Engine startup update the page content, as it is detected as newer:
                removePermissionsDeclaredInPageProperties(previousPageProperties);
            }
            addPermissionsFromPageProperties(pageProperties.getProperty(PROPERTIES_NAME), pageProperties);

            initiateLogBuilder(pageId, SQueriableLog.STATUS_OK, logBuilder, METHOD_UPDATE_PAGE);
        } catch (SRecorderException | SBonitaReadException re) {
            initiateLogBuilder(pageId, SQueriableLog.STATUS_FAIL, logBuilder, METHOD_UPDATE_PAGE);
            throw new SObjectModificationException(re);
        }

        if (pageUpdateBuilder == null) {
            pageUpdateBuilder = BuilderFactory.get(SPageUpdateBuilderFactory.class)
                    .createNewInstance(new EntityUpdateDescriptor());
        }
        if (contentName == null) {
            pageUpdateBuilder.updateContentName(sPageContent.getContentName());
        } else {
            pageUpdateBuilder.updateContentName(contentName);
        }
        pageUpdateBuilder.updateDescription(pageProperties.getProperty(PROPERTIES_DESCRIPTION));
        pageUpdateBuilder.updateDisplayName(pageProperties.getProperty(PROPERTIES_DISPLAY_NAME));
        pageUpdateBuilder.updateContentType(pageProperties.getProperty(PROPERTIES_CONTENT_TYPE, SContentType.PAGE));
        if (sPageContent.isProvided()) {
            //update the md5 sum of the page only if it is provided
            pageUpdateBuilder.updatePageHash(DigestUtils.md5DigestAsHex(content));
        }
        final AbstractSPage sPage = updatePage(pageUpdateBuilder.done(), sPageContent);
        for (final PageServiceListener pageServiceListener : pageServiceListeners) {
            pageServiceListener.pageUpdated(sPage, content);
        }
    }

    Properties getPreviousPageProperties(SPage sPage) throws IOException, SInvalidPageZipMissingPropertiesException,
            SBonitaReadException, SObjectNotFoundException {
        return getPreviousPageProperties(getPageContent(sPage.getId()));
    }

    Properties getPreviousPageProperties(byte[] content) throws IOException, SInvalidPageZipMissingPropertiesException {
        return helper.loadPageProperties(content);
    }

    @Override
    public void init() throws SBonitaException {
        try {
            List<ImportStatus> importFinalPagesStatuses = importProvidedNonRemovableNonEditablePagesFromClasspath();

            List<ImportStatus> importStatuses = new ArrayList<>(importFinalPagesStatuses);
            importStatuses.addAll(importProvidedNonRemovableEditablePagesFromClasspath());

            boolean addRemovableIfMissing = importFinalPagesStatuses.stream().map(ImportStatus::getStatus)
                    .allMatch(importStatus -> importStatus == ImportStatus.Status.ADDED);
            if (addRemovableIfMissing) {
                log.info(
                        "Detected a first run (a tenant creation or an installation from scratch), importing provided removable pages");
            }
            importStatuses.addAll(importProvidedRemovablePagesFromClasspath(addRemovableIfMissing));

            List<String> createdOrReplaced = importStatuses.stream()
                    .filter(importStatus -> importStatus.getStatus() != ImportStatus.Status.SKIPPED)
                    .map(importStatus -> importStatus.getName() + " " + importStatus.getStatus())
                    .collect(Collectors.toList());
            if (createdOrReplaced.isEmpty()) {
                log.info("No page updated");
            } else {
                log.info("Page updated or created : {}", createdOrReplaced);
            }
        } catch (BonitaException | IOException e) {
            log.error(
                    ExceptionUtils.printLightWeightStacktrace(e));
            log.debug("Stacktrace of the import issue is:", e);
        }
        initialized = true;
    }

    private List<ImportStatus> importProvidedNonRemovableNonEditablePagesFromClasspath()
            throws BonitaException, IOException {
        return importProvidedPagesFromResourcePattern(true, false, false, NON_EDITABLE_NON_REMOVABLE_RESOURCES_PATH);
    }

    private List<ImportStatus> importProvidedNonRemovableEditablePagesFromClasspath()
            throws IOException, BonitaException {
        return importProvidedPagesFromResourcePattern(true, false, true, EDITABLE_NON_REMOVABLE_RESOURCES_PATH);
    }

    private List<ImportStatus> importProvidedRemovablePagesFromClasspath(boolean addIfMissing)
            throws IOException, BonitaException {
        return importProvidedPagesFromResourcePattern(addIfMissing, true, true, EDITABLE_REMOVABLE_RESOURCES_PATH);
    }

    private List<ImportStatus> importProvidedPagesFromResourcePattern(boolean addIfMissing, boolean removable,
            boolean editable, String resourcesPath) throws IOException, BonitaException {
        List<ImportStatus> importStatuses = new ArrayList<>();
        Resource[] resources = cpResourceResolver
                .getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/" + resourcesPath + "/*.zip");
        for (Resource resource : resources) {
            if (resource.exists() && resource.isReadable() && resource.contentLength() > 0) {
                String resourceName = resource.getFilename();
                try (InputStream resourceAsStream = resource.getInputStream()) {
                    log.debug("Found provided page '{}' in classpath", resourceName);
                    final byte[] content = org.apache.commons.io.IOUtils.toByteArray(resourceAsStream);
                    importStatuses.add(importProvidedPage(resourceName, content, removable, editable, addIfMissing));
                } catch (IOException | SBonitaException e) {
                    throw new BonitaException("Unable to import the page " + resourceName, e);
                }
            } else {
                throw new BonitaException(
                        "A resource " + resource.getDescription() + " could not be read when loading default pages");
            }
        }
        return importStatuses;
    }

    ImportStatus importProvidedPage(String pageZipName, final byte[] providedPageContent,
            boolean removable, boolean editable, boolean addIfMissing) throws SBonitaException {

        SPage page = buildPage(providedPageContent, pageZipName, -1, true, removable, editable);
        ImportStatus importStatus = new ImportStatus(page.getName());
        SPage sPageInDb = checkIfPageAlreadyExists(page);
        if (sPageInDb == null && addIfMissing) {
            log.debug("Provided page {} does not exist yet, importing it.", page.getName());
            page.setPageHash(DigestUtils.md5DigestAsHex(providedPageContent));
            insertPage(page, providedPageContent);
        } else if (sPageInDb == null) {
            log.debug("Provided page {} has been deleted by the user, and will not be imported", page.getName());
            importStatus.setStatus(ImportStatus.Status.SKIPPED);
        } else if (sPageInDb.isProvided()) {
            String md5Sum = DigestUtils.md5DigestAsHex(providedPageContent);
            if (Objects.equals(sPageInDb.getPageHash(), md5Sum)) {
                log.debug("Provided page exists and is up to date, nothing to do");
                importStatus.setStatus(ImportStatus.Status.SKIPPED);
            } else {
                log.info("Provided page {} exists but the content is not up to date, updating it.", page.getName());
                updatePageContent(sPageInDb.getId(), providedPageContent, pageZipName);
                importStatus.setStatus(ImportStatus.Status.REPLACED);
            }
        } else {
            log.debug("Page {} was updated by the user, and will not be updated", page.getName());
            importStatus.setStatus(ImportStatus.Status.SKIPPED);
        }
        return importStatus;
    }

    @Override
    public boolean initialized() {
        // Used only in tests
        return initialized;
    }

    @Autowired
    public void setPageServiceListeners(final List<PageServiceListener> pageServiceListeners) {
        this.pageServiceListeners = pageServiceListeners;
    }

}
