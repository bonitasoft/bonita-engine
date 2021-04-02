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
package org.bonitasoft.engine.business.application.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.impl.cleaner.ApplicationDestructor;
import org.bonitasoft.engine.business.application.impl.cleaner.ApplicationMenuCleaner;
import org.bonitasoft.engine.business.application.impl.cleaner.ApplicationMenuDestructor;
import org.bonitasoft.engine.business.application.impl.cleaner.ApplicationPageDestructor;
import org.bonitasoft.engine.business.application.impl.converter.MenuIndexConverter;
import org.bonitasoft.engine.business.application.model.*;
import org.bonitasoft.engine.business.application.model.builder.SApplicationLogBuilder;
import org.bonitasoft.engine.business.application.model.builder.SApplicationMenuLogBuilder;
import org.bonitasoft.engine.business.application.model.builder.SApplicationPageLogBuilder;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationLogBuilderImpl;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuLogBuilderImpl;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationPageLogBuilderImpl;
import org.bonitasoft.engine.commons.exceptions.*;
import org.bonitasoft.engine.persistence.*;
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
import org.springframework.stereotype.Service;

/**
 * @author Elias Ricken de Medeiros
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

    public static final int MAX_RESULTS = 1000;
    private final Recorder recorder;
    private final ReadPersistenceService persistenceService;
    private final QueriableLoggerService queriableLoggerService;
    private final IndexManager indexManager;
    private final MenuIndexConverter menuIndexConverter;
    private final ApplicationDestructor applicationDestructor;
    private final ApplicationPageDestructor applicationPageDestructor;
    private final ApplicationMenuDestructor applicationMenuDestructor;

    @Autowired
    public ApplicationServiceImpl(Recorder recorder, ReadPersistenceService persistenceService,
            final QueriableLoggerService queriableLoggerService) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.queriableLoggerService = queriableLoggerService;
        indexManager = new IndexManager(new IndexUpdater(this, MAX_RESULTS), new MenuIndexValidator());
        menuIndexConverter = new MenuIndexConverter(this);
        final ApplicationMenuCleaner applicationMenuCleaner = new ApplicationMenuCleaner(this);
        applicationDestructor = new ApplicationDestructor(applicationMenuCleaner);
        applicationPageDestructor = new ApplicationPageDestructor(applicationMenuCleaner, new HomePageChecker(this));
        applicationMenuDestructor = new ApplicationMenuDestructor(applicationMenuCleaner);
    }

    //Visible for tests only
    ApplicationServiceImpl(Recorder recorder, ReadPersistenceService persistenceService,
            QueriableLoggerService queriableLoggerService,
            IndexManager indexManager,
            MenuIndexConverter menuIndexConverter,
            ApplicationDestructor applicationDestructor,
            ApplicationPageDestructor applicationPageDestructor,
            ApplicationMenuDestructor applicationMenuDestructor) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.queriableLoggerService = queriableLoggerService;
        this.indexManager = indexManager;
        this.menuIndexConverter = menuIndexConverter;
        this.applicationDestructor = applicationDestructor;
        this.applicationPageDestructor = applicationPageDestructor;
        this.applicationMenuDestructor = applicationMenuDestructor;
    }

    @Override
    public SApplicationWithIcon createApplication(final SApplicationWithIcon application)
            throws SObjectCreationException, SObjectAlreadyExistsException {
        final String methodName = "createApplication";
        final SApplicationLogBuilder logBuilder = getApplicationLogBuilder(ActionType.CREATED,
                "Creating application named " + application.getToken());
        try {
            validateApplication(application);
            recorder.recordInsert(new InsertRecord(application), APPLICATION);
            log(application.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SObjectAlreadyExistsException e) {
            return logAndRetrowException(application.getId(), methodName, logBuilder, e);
        } catch (final SBonitaException e) {
            handleCreationException(application, logBuilder, e, methodName);
        }
        return application;
    }

    private void validateApplication(final SApplicationWithIcon application)
            throws SBonitaReadException, SObjectAlreadyExistsException {
        validateApplicationToken(application.getToken());

    }

    private void validateApplicationToken(final String applicationToken)
            throws SBonitaReadException, SObjectAlreadyExistsException {
        if (hasApplicationWithToken(applicationToken)) {
            throw new SObjectAlreadyExistsException(
                    "An application already exists with token '" + applicationToken + "'.");
        }
    }

    private void handleCreationException(final PersistentObject persistentObject,
            final SPersistenceLogBuilder logBuilder, final Exception e,
            final String methodName)
            throws SObjectCreationException {
        log(persistentObject.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
        throw new SObjectCreationException(e);
    }

    private void throwModificationException(final long persitentObjectId, final SPersistenceLogBuilder logBuilder,
            final String methodName, final Exception e)
            throws SObjectModificationException {
        log(persitentObjectId, SQueriableLog.STATUS_FAIL, logBuilder, methodName);
        throw new SObjectModificationException(e);
    }

    public boolean hasApplicationWithToken(final String name) throws SBonitaReadException {
        final SApplication application = getApplicationByToken(name);
        return application != null;
    }

    @Override
    public SApplication getApplicationByToken(final String token) throws SBonitaReadException {
        return persistenceService.selectOne(new SelectOneDescriptor<>("getApplicationByToken", Collections
                .singletonMap("token",
                        token),
                SApplication.class));
    }

    private <T extends SLogBuilder & HasCRUDEAction> void initializeLogBuilder(final T logBuilder, final String message,
            final ActionType actionType) {
        logBuilder.actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
        logBuilder.setActionType(actionType);
    }

    private SApplicationLogBuilder getApplicationLogBuilder(final ActionType actionType, final String message) {
        final SApplicationLogBuilder logBuilder = new SApplicationLogBuilderImpl();
        initializeLogBuilder(logBuilder, message, actionType);
        return logBuilder;
    }

    private SApplicationPageLogBuilder getApplicationPageLogBuilder(final ActionType actionType, final String message) {
        final SApplicationPageLogBuilderImpl logBuilder = new SApplicationPageLogBuilderImpl();
        initializeLogBuilder(logBuilder, message, actionType);
        return logBuilder;
    }

    private SApplicationMenuLogBuilder getApplicationMenuLogBuilder(final ActionType actionType, final String message) {
        final SApplicationMenuLogBuilderImpl logBuilder = new SApplicationMenuLogBuilderImpl();
        initializeLogBuilder(logBuilder, message, actionType);
        return logBuilder;
    }

    private void log(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder,
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
    public SApplication getApplication(final long applicationId)
            throws SBonitaReadException, SObjectNotFoundException {
        SApplication application = persistenceService
                .selectById(new SelectByIdDescriptor<>(SApplication.class, applicationId));
        if (application == null) {
            throw new SObjectNotFoundException("No application found with id '" + applicationId + "'.");
        }
        return application;
    }

    @Override
    public SApplicationWithIcon getApplicationWithIcon(long applicationId)
            throws SBonitaReadException, SObjectNotFoundException {
        SApplicationWithIcon application = persistenceService
                .selectById(new SelectByIdDescriptor<>(SApplicationWithIcon.class, applicationId));
        if (application == null) {
            throw new SObjectNotFoundException("No application found with id '" + applicationId + "'.");
        }
        return application;
    }

    @Override
    public void deleteApplication(final long applicationId)
            throws SObjectModificationException, SObjectNotFoundException {
        final String methodName = "deleteApplication";
        final SApplicationLogBuilder logBuilder = getApplicationLogBuilder(ActionType.DELETED,
                "Deleting application with id " + applicationId);
        try {
            final SApplication application = getApplication(applicationId);
            applicationDestructor.onDeleteApplication(application);
            recorder.recordDelete(new DeleteRecord(application), APPLICATION);
            log(application.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SObjectNotFoundException e) {
            logAndRetrowException(applicationId, methodName, logBuilder, e);
        } catch (final SBonitaException e) {
            throwModificationException(applicationId, logBuilder, methodName, e);
        }

    }

    @Override
    public SApplicationWithIcon updateApplication(final long applicationId,
            final EntityUpdateDescriptor updateDescriptor)
            throws SObjectModificationException, SObjectAlreadyExistsException, SObjectNotFoundException {
        final String methodName = "updateApplication";
        final SApplicationLogBuilder logBuilder = getApplicationLogBuilder(ActionType.UPDATED,
                "Updating application with id " + applicationId);

        try {
            handleHomePageUpdate(updateDescriptor);
            final SApplicationWithIcon application = getApplicationWithIcon(applicationId);
            return updateApplication(application, updateDescriptor);
        } catch (final SObjectNotFoundException | SObjectAlreadyExistsException | SObjectModificationException e) {
            throw e;
        } catch (final SBonitaException e) {
            log(applicationId, SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SObjectModificationException(e);
        }
    }

    private void handleHomePageUpdate(final EntityUpdateDescriptor updateDescriptor)
            throws SBonitaReadException, SObjectModificationException {
        final Long homePageId = (Long) updateDescriptor.getFields().get(AbstractSApplication.HOME_PAGE_ID);
        if (homePageId != null) {
            final SApplicationPage applicationPage = executeGetApplicationPageById(homePageId);
            if (applicationPage == null) {
                throw new SObjectModificationException(
                        "Invalid home page id: No application page found with id '" + homePageId + "'");
            }
        }
    }

    @Override
    public SApplicationWithIcon updateApplication(final SApplicationWithIcon application,
            final EntityUpdateDescriptor updateDescriptor)
            throws SObjectModificationException, SObjectAlreadyExistsException {
        final String methodName = "updateApplication";
        final long now = System.currentTimeMillis();
        final SApplicationLogBuilder logBuilder = getApplicationLogBuilder(ActionType.UPDATED,
                "Updating application with id " + application.getId());
        try {
            validateUpdatedFields(updateDescriptor, application);
            updateDescriptor.addField(AbstractSApplication.LAST_UPDATE_DATE, now);

            recorder.recordUpdate(UpdateRecord.buildSetFields(application,
                    updateDescriptor), APPLICATION);
            log(application.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
            return application;

        } catch (final SObjectAlreadyExistsException e) {
            return logAndRetrowException(application.getId(), methodName, logBuilder, e);
        } catch (final SBonitaException e) {
            log(application.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SObjectModificationException(e);
        }
    }

    private <T, E extends SBonitaException> T logAndRetrowException(final long objectId, final String methodName,
            final SPersistenceLogBuilder logBuilder,
            final E e) throws E {
        log(objectId, SQueriableLog.STATUS_FAIL, logBuilder, methodName);
        throw e;
    }

    private void validateUpdatedFields(final EntityUpdateDescriptor updateDescriptor,
            final SApplicationWithIcon application)
            throws SBonitaReadException,
            SObjectAlreadyExistsException {
        if (updateDescriptor.getFields().containsKey(AbstractSApplication.TOKEN)
                && !application.getToken().equals(updateDescriptor.getFields().get(AbstractSApplication.TOKEN))) {
            validateApplicationToken((String) updateDescriptor.getFields().get(AbstractSApplication.TOKEN));
        }
    }

    @Override
    public long getNumberOfApplications(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SApplication.class, options, null);
    }

    @Override
    public long getNumberOfApplicationsOfUser(long userId, QueryOptions options) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("userId",
                (Object) userId);
        return persistenceService.getNumberOfEntities(SApplication.class, "OfUser", options, parameters);
    }

    @Override
    public List<SApplication> searchApplications(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.searchEntity(SApplication.class, options, null);
    }

    @Override
    public List<SApplication> searchApplicationsOfUser(long userId, QueryOptions options) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("userId",
                (Object) userId);
        return persistenceService.searchEntity(SApplication.class, "OfUser", options, parameters);
    }

    @Override
    public SApplicationPage createApplicationPage(final SApplicationPage applicationPage)
            throws SObjectCreationException, SObjectAlreadyExistsException {
        final String methodName = "createApplicationPage";
        final SApplicationPageLogBuilder logBuilder = getApplicationPageLogBuilder(ActionType.CREATED,
                "Creating application page with token " + applicationPage.getToken());
        try {
            validateApplicationPage(applicationPage);
            recorder.recordInsert(new InsertRecord(applicationPage), APPLICATION_PAGE);
            log(applicationPage.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SObjectAlreadyExistsException e) {
            return logAndRetrowException(applicationPage.getId(), methodName, logBuilder, e);
        } catch (final SBonitaException e) {
            handleCreationException(applicationPage, logBuilder, e, methodName);
        }
        return applicationPage;
    }

    private void validateApplicationPage(final SApplicationPage applicationPage)
            throws SBonitaReadException, SObjectAlreadyExistsException {
        final String applicationPageToken = applicationPage.getToken();
        if (hasApplicationPage(applicationPage.getApplicationId(), applicationPageToken)) {
            String stb = "An application page with token '" + applicationPageToken +
                    "' already exists for the application with id '" + applicationPage.getApplicationId() + "'";
            throw new SObjectAlreadyExistsException(stb);
        }
    }

    private boolean hasApplicationPage(final long applicationId, final String name) throws SBonitaReadException {
        final SApplicationPage applicationPage = getApplicationPage(applicationId, name);
        return applicationPage != null;
    }

    public SApplicationPage getApplicationPage(final long applicationId, final String applicationPageToken)
            throws SBonitaReadException {
        final Map<String, Object> inputParameters = new HashMap<>(2);
        inputParameters.put("applicationId", applicationId);
        inputParameters.put("applicationPageToken", applicationPageToken);
        return persistenceService.selectOne(new SelectOneDescriptor<>("getApplicationPageByTokenAndApplicationId",
                inputParameters, SApplicationPage.class));
    }

    @Override
    public SApplicationPage getApplicationPage(final String applicationToken, final String applicationPageToken)
            throws SBonitaReadException,
            SObjectNotFoundException {
        final Map<String, Object> inputParameters = new HashMap<>(2);
        inputParameters.put("applicationToken", applicationToken);
        inputParameters.put("applicationPageToken", applicationPageToken);
        final SApplicationPage applicationPage = persistenceService
                .selectOne(new SelectOneDescriptor<>("getApplicationPageByTokenAndApplicationToken", inputParameters,
                        SApplicationPage.class));
        if (applicationPage == null) {
            throw new SObjectNotFoundException("No application page found with name '" + applicationPageToken
                    + "' and application token '" + applicationToken + "'.");
        }
        return applicationPage;
    }

    @Override
    public SApplicationPage getApplicationPage(final long applicationPageId)
            throws SBonitaReadException, SObjectNotFoundException {
        final SApplicationPage applicationPage = executeGetApplicationPageById(applicationPageId);
        if (applicationPage == null) {
            throw new SObjectNotFoundException("No application page found with id '" + applicationPageId + "'.");
        }
        return applicationPage;
    }

    private SApplicationPage executeGetApplicationPageById(final long applicationPageId) throws SBonitaReadException {
        return persistenceService
                .selectById(new SelectByIdDescriptor<>(SApplicationPage.class, applicationPageId));
    }

    @Override
    public SApplicationPage deleteApplicationPage(final long applicationPageId)
            throws SObjectModificationException, SObjectNotFoundException {
        final String methodName = "deleteApplicationPage";
        final SApplicationPageLogBuilder logBuilder = getApplicationPageLogBuilder(ActionType.DELETED,
                "Deleting application page with id "
                        + applicationPageId);
        SApplicationPage applicationPage = null;
        try {
            applicationPage = getApplicationPage(applicationPageId);
            deleteApplicationPage(applicationPage);
        } catch (final SObjectNotFoundException e) {
            logAndRetrowException(applicationPageId, methodName, logBuilder, e);
        } catch (final SObjectModificationException e) {
            throw e;
        } catch (final SBonitaException e) {
            throwModificationException(applicationPageId, logBuilder, methodName, e);
        }
        return applicationPage;

    }

    @Override
    public void deleteApplicationPage(final SApplicationPage applicationPage) throws SObjectModificationException {
        final String methodName = "deleteApplicationPage";
        final SApplicationPageLogBuilder logBuilder = getApplicationPageLogBuilder(ActionType.DELETED,
                "Deleting application page with id "
                        + applicationPage.getId());
        try {
            applicationPageDestructor.onDeleteApplicationPage(applicationPage);
            recorder.recordDelete(new DeleteRecord(applicationPage), APPLICATION_PAGE);
            log(applicationPage.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SObjectModificationException e) {
            logAndRetrowException(applicationPage.getId(), methodName, logBuilder, e);
        } catch (final SBonitaException e) {
            throwModificationException(applicationPage.getId(), logBuilder, methodName, e);
        }

    }

    @Override
    public SApplicationPage getApplicationHomePage(final long applicationId)
            throws SBonitaReadException, SObjectNotFoundException {
        final Map<String, Object> inputParameters = new HashMap<>(2);
        inputParameters.put("applicationId", applicationId);
        final SApplicationPage applicationPage = persistenceService
                .selectOne(
                        new SelectOneDescriptor<>("getApplicationHomePage", inputParameters, SApplicationPage.class));
        if (applicationPage == null) {
            throw new SObjectNotFoundException("No home page found for application with id '" + applicationId + "'.");
        }
        return applicationPage;
    }

    @Override
    public long getNumberOfApplicationPages(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SApplicationPage.class, options, null);
    }

    @Override
    public List<SApplicationPage> searchApplicationPages(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.searchEntity(SApplicationPage.class, options, null);
    }

    @Override
    public SApplicationMenu createApplicationMenu(final SApplicationMenu applicationMenu)
            throws SObjectCreationException {
        final String methodName = "createApplicationMenu";
        final SApplicationMenuLogBuilder logBuilder = getApplicationMenuLogBuilder(ActionType.CREATED,
                "Creating application menu with display name " + applicationMenu.getDisplayName());
        try {
            recorder.recordInsert(new InsertRecord(applicationMenu), APPLICATION_MENU);
            log(applicationMenu.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SRecorderException e) {
            handleCreationException(applicationMenu, logBuilder, e, methodName);
        }
        return applicationMenu;
    }

    @Override
    public SApplicationMenu updateApplicationMenu(final long applicationMenuId,
            final EntityUpdateDescriptor updateDescriptor)
            throws SObjectModificationException,
            SObjectNotFoundException {
        final String methodName = "updateApplicationMenu";
        final SApplicationMenuLogBuilder logBuilder = getApplicationMenuLogBuilder(ActionType.UPDATED,
                "Updating application menu with id " + applicationMenuId);

        try {
            final SApplicationMenu applicationMenu = getApplicationMenu(applicationMenuId);

            updateApplicationMenu(applicationMenu, updateDescriptor, true);
            return applicationMenu;
        } catch (final SObjectNotFoundException e) {
            return logAndRetrowException(applicationMenuId, methodName, logBuilder, e);
        } catch (final SBonitaReadException e) {
            log(applicationMenuId, SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SObjectModificationException(e);
        }
    }

    @Override
    public SApplicationMenu updateApplicationMenu(final SApplicationMenu applicationMenu,
            final EntityUpdateDescriptor updateDescriptor,
            final boolean organizeIndexes)
            throws SObjectModificationException {
        final String methodName = "updateApplicationMenu";
        final SApplicationMenuLogBuilder logBuilder = getApplicationMenuLogBuilder(ActionType.UPDATED,
                "Updating application menu with id " + applicationMenu.getId());

        try {
            organizeIndexesOnUpdate(applicationMenu, updateDescriptor, organizeIndexes);
            recorder.recordUpdate(UpdateRecord.buildSetFields(applicationMenu,
                    updateDescriptor), APPLICATION_MENU);
            log(applicationMenu.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
            return applicationMenu;
        } catch (final SBonitaException e) {
            log(applicationMenu.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SObjectModificationException(e);
        }
    }

    private void organizeIndexesOnUpdate(final SApplicationMenu applicationMenu,
            final EntityUpdateDescriptor updateDescriptor, final boolean organizeIndexes)
            throws SObjectModificationException, SBonitaReadException {
        final Map<String, Object> fields = updateDescriptor.getFields();
        if (fields.containsKey(SApplicationMenu.PARENT_ID) && !fields.containsKey(SApplicationMenu.INDEX)) {
            //we need to force the update of index, as it has change of parent
            fields.put(SApplicationMenu.INDEX, getNextAvailableIndex((Long) fields.get(SApplicationMenu.PARENT_ID)));
        }
        final Integer newIndexValue = (Integer) fields.get(SApplicationMenu.INDEX);
        if (newIndexValue != null && organizeIndexes) {
            final MenuIndex oldIndex = menuIndexConverter.toMenuIndex(applicationMenu);
            final MenuIndex newIndex = menuIndexConverter.toMenuIndex(applicationMenu, updateDescriptor);
            indexManager.organizeIndexesOnUpdate(oldIndex, newIndex);
        }
    }

    @Override
    public SApplicationMenu getApplicationMenu(final long applicationMenuId)
            throws SBonitaReadException, SObjectNotFoundException {
        final SApplicationMenu applicationMenu = persistenceService
                .selectById(new SelectByIdDescriptor<>(SApplicationMenu.class, applicationMenuId));
        if (applicationMenu == null) {
            throw new SObjectNotFoundException("No application found with id '" + applicationMenuId + "'.");
        }
        return applicationMenu;
    }

    @Override
    public SApplicationMenu deleteApplicationMenu(final long applicationMenuId)
            throws SObjectModificationException, SObjectNotFoundException {
        final String methodName = "deleteApplicationMenu";
        final SApplicationMenuLogBuilder logBuilder = getApplicationMenuLogBuilder(ActionType.DELETED,
                "Deleting application menu with id " + applicationMenuId);
        SApplicationMenu applicationMenu = null;
        try {
            applicationMenu = getApplicationMenu(applicationMenuId);
            deleteApplicationMenu(applicationMenu);
            log(applicationMenu.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SObjectNotFoundException e) {
            logAndRetrowException(applicationMenuId, methodName, logBuilder, e);
        } catch (final SBonitaReadException e) {
            throwModificationException(applicationMenuId, logBuilder, methodName, e);
        }
        return applicationMenu;
    }

    @Override
    public void deleteApplicationMenu(final SApplicationMenu applicationMenu) throws SObjectModificationException {
        final String methodName = "deleteApplicationMenu";
        final SApplicationMenuLogBuilder logBuilder = getApplicationMenuLogBuilder(ActionType.DELETED,
                "Deleting application menu with id " + applicationMenu.getId());
        try {
            applicationMenuDestructor.onDeleteApplicationMenu(applicationMenu);
            final int lastUsedIndex = getLastUsedIndex(applicationMenu.getParentId());
            indexManager.organizeIndexesOnDelete(
                    new MenuIndex(applicationMenu.getParentId(), applicationMenu.getIndex(), lastUsedIndex));
            recorder.recordDelete(new DeleteRecord(applicationMenu), APPLICATION_MENU);
        } catch (final SBonitaException e) {
            throwModificationException(applicationMenu.getId(), logBuilder, methodName, e);
        }
    }

    @Override
    public long getNumberOfApplicationMenus(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SApplicationMenu.class, options, null);
    }

    @Override
    public List<SApplicationMenu> searchApplicationMenus(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.searchEntity(SApplicationMenu.class, options, null);
    }

    @Override
    public int getNextAvailableIndex(final Long parentMenuId) throws SBonitaReadException {
        final int lastIndex = getLastUsedIndex(parentMenuId);
        return lastIndex + 1;
    }

    @Override
    public List<String> getAllPagesForProfile(final long profileId) throws SBonitaReadException {
        final SelectListDescriptor<String> selectList = new SelectListDescriptor<>("getAllPagesForProfile",
                Collections.singletonMap(
                        "profileId", profileId),
                SApplicationPage.class, new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS));
        return persistenceService.selectList(selectList);
    }

    @Override
    public List<String> getAllPagesForProfile(String profile) throws SBonitaReadException {
        final SelectListDescriptor<String> selectList = new SelectListDescriptor<>("getAllPagesForProfileName",
                Collections.singletonMap(
                        "profileName", profile),
                SApplicationPage.class, new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS));
        return persistenceService.selectList(selectList);
    }

    protected Integer executeGetLastUsedIndexQuery(final Long parentMenuId) throws SBonitaReadException {
        SelectOneDescriptor<Integer> selectDescriptor;
        if (parentMenuId == null) {
            selectDescriptor = new SelectOneDescriptor<>("getLastIndexForRootMenu", Collections.emptyMap(),
                    SApplicationMenu.class);
        } else {
            selectDescriptor = new SelectOneDescriptor<>("getLastIndexForChildOf",
                    Collections.singletonMap(SApplicationMenu.PARENT_ID, parentMenuId),
                    SApplicationMenu.class);
        }
        return persistenceService.selectOne(selectDescriptor);
    }

    @Override
    public int getLastUsedIndex(final Long parentMenuId) throws SBonitaReadException {
        final Integer lastUsedIndex = executeGetLastUsedIndexQuery(parentMenuId);
        return lastUsedIndex == null ? 0 : lastUsedIndex;
    }

}
