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
package org.bonitasoft.engine.business.application.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.impl.cleaner.ApplicationDestructor;
import org.bonitasoft.engine.business.application.impl.cleaner.ApplicationMenuCleaner;
import org.bonitasoft.engine.business.application.impl.cleaner.ApplicationMenuDestructor;
import org.bonitasoft.engine.business.application.impl.cleaner.ApplicationPageDestructor;
import org.bonitasoft.engine.business.application.impl.converter.MenuIndexConverter;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.builder.SApplicationBuilderFactory;
import org.bonitasoft.engine.business.application.model.builder.SApplicationLogBuilder;
import org.bonitasoft.engine.business.application.model.builder.SApplicationMenuLogBuilder;
import org.bonitasoft.engine.business.application.model.builder.SApplicationPageLogBuilder;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationFields;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationLogBuilderImpl;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuBuilderFactoryImpl;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuFields;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuLogBuilderImpl;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationPageLogBuilderImpl;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;
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
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationServiceImpl implements ApplicationService {

    public static final int MAX_RESULTS = 1000;
    private final Recorder recorder;

    private final ReadPersistenceService persistenceService;

    private final QueriableLoggerService queriableLoggerService;

    private final SApplicationBuilderFactory applicationKeyProvider;

    private IndexManager indexManager;
    private MenuIndexConverter menuIndexConverter;
    private ApplicationDestructor applicationDestructor;
    private ApplicationPageDestructor applicationPageDestructor;
    private ApplicationMenuDestructor applicationMenuDestructor;

    public ApplicationServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceService, final QueriableLoggerService queriableLoggerService) {
        this(recorder, persistenceService, queriableLoggerService, null, null, null, null, null);
        indexManager = new IndexManager(new IndexUpdater(this, MAX_RESULTS), new MenuIndexValidator());
        menuIndexConverter = new MenuIndexConverter(this);
        final ApplicationMenuCleaner applicationMenuCleaner = new ApplicationMenuCleaner(this);
        applicationDestructor = new ApplicationDestructor(applicationMenuCleaner);
        applicationPageDestructor = new ApplicationPageDestructor(applicationMenuCleaner, new HomePageChecker(this));
        applicationMenuDestructor = new ApplicationMenuDestructor(applicationMenuCleaner);
    }

    ApplicationServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceService,
            final QueriableLoggerService queriableLoggerService, final IndexManager indexManager, final MenuIndexConverter menuIndexConverter,
            final ApplicationDestructor applicationDestructor, final ApplicationPageDestructor applicationPageDestructor,
            final ApplicationMenuDestructor applicationMenuDestructor) {
        this.indexManager = indexManager;
        this.menuIndexConverter = menuIndexConverter;
        this.applicationDestructor = applicationDestructor;
        this.applicationPageDestructor = applicationPageDestructor;
        this.applicationMenuDestructor = applicationMenuDestructor;
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.queriableLoggerService = queriableLoggerService;
        applicationKeyProvider = BuilderFactory.get(SApplicationBuilderFactory.class);
    }

    @Override
    public SApplication createApplication(final SApplication application) throws SObjectCreationException, SObjectAlreadyExistsException {
        final String methodName = "createApplication";
        final SApplicationLogBuilder logBuilder = getApplicationLogBuilder(ActionType.CREATED, "Creating application named " + application.getToken());
        try {
            validateApplication(application);
            final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(ApplicationService.APPLICATION)
                    .setObject(application).done();
            recorder.recordInsert(new InsertRecord(application), insertEvent);
            log(application.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SObjectAlreadyExistsException e) {
            return logAndRetrowException(application.getId(), methodName, logBuilder, e);
        } catch (final SBonitaException e) {
            handleCreationException(application, logBuilder, e, methodName);
        }
        return application;
    }

    private void validateApplication(final SApplication application) throws SBonitaReadException, SObjectAlreadyExistsException,
            SObjectCreationException {
        final String applicationName = application.getToken();
        validateApplicationToken(applicationName);

    }

    private void validateApplicationToken(final String applicationToken) throws SBonitaReadException, SObjectAlreadyExistsException {
        if (hasApplicationWithToken(applicationToken)) {
            throw new SObjectAlreadyExistsException("An application already exists with token '" + applicationToken + "'.");
        }
    }

    private void handleCreationException(final PersistentObject persitentObject, final SPersistenceLogBuilder logBuilder, final Exception e,
            final String methodName)
            throws SObjectCreationException {
        log(persitentObject.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
        throw new SObjectCreationException(e);
    }

    private void throwModificationException(final long persitentObjectId, final SPersistenceLogBuilder logBuilder, final String methodName, final Exception e)
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
        return persistenceService.selectOne(new SelectOneDescriptor<SApplication>("getApplicationByToken", Collections
                .<String, Object> singletonMap("token",
                        token), SApplication.class));
    }

    private <T extends SLogBuilder & HasCRUDEAction> void initializeLogBuilder(final T logBuilder, final String message, final ActionType actionType) {
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
        final SApplicationLogBuilder logBuilder = getApplicationLogBuilder(ActionType.DELETED, "Deleting application with id " + applicationId);
        try {
            final SApplication application = getApplication(applicationId);
            final SDeleteEvent event = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(ApplicationService.APPLICATION)
                    .setObject(application).done();
            applicationDestructor.onDeleteApplication(application);
            recorder.recordDelete(new DeleteRecord(application), event);
            log(application.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SObjectNotFoundException e) {
            logAndRetrowException(applicationId, methodName, logBuilder, e);
        } catch (final SBonitaException e) {
            throwModificationException(applicationId, logBuilder, methodName, e);
        }

    }

    @Override
    public SApplication updateApplication(final long applicationId, final EntityUpdateDescriptor updateDescriptor)
            throws SObjectModificationException, SObjectAlreadyExistsException, SObjectNotFoundException {
        final String methodName = "updateApplication";
        final SApplicationLogBuilder logBuilder = getApplicationLogBuilder(ActionType.UPDATED, "Updating application with id " + applicationId);

        try {
            handleHomePageUpdate(updateDescriptor);
            final SApplication application = getApplication(applicationId);
            return updateApplication(application, updateDescriptor);
        } catch (final SObjectNotFoundException e) {
            throw e;
        } catch (final SObjectAlreadyExistsException e) {
            throw e;
        } catch (final SObjectModificationException e) {
            throw e;
        } catch (final SBonitaException e) {
            log(applicationId, SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SObjectModificationException(e);
        }
    }

    private void handleHomePageUpdate(final EntityUpdateDescriptor updateDescriptor) throws SBonitaReadException, SObjectModificationException {
        final Long homePageId = (Long) updateDescriptor.getFields().get(SApplicationFields.HOME_PAGE_ID);
        if (homePageId != null) {
            final SApplicationPage applicationPage = executeGetApplicationPageById(homePageId);
            if (applicationPage == null) {
                throw new SObjectModificationException("Invalid home page id: No application page found with id '" + homePageId + "'");
            }
        }
    }

    @Override
    public SApplication updateApplication(final SApplication application, final EntityUpdateDescriptor updateDescriptor)
            throws SObjectModificationException, SObjectAlreadyExistsException {
        final String methodName = "updateApplication";
        final long now = System.currentTimeMillis();
        final SApplicationLogBuilder logBuilder = getApplicationLogBuilder(ActionType.UPDATED, "Updating application with id " + application.getId());

        try {
            validateUpdatedFields(updateDescriptor, application);
            updateDescriptor.addField(applicationKeyProvider.getLastUpdatedDateKey(), now);

            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(application,
                    updateDescriptor);
            final SUpdateEvent updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(ApplicationService.APPLICATION)
                    .setObject(application).done();
            recorder.recordUpdate(updateRecord, updateEvent);
            log(application.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
            return application;

        } catch (final SObjectAlreadyExistsException e) {
            return logAndRetrowException(application.getId(), methodName, logBuilder, e);
        } catch (final SBonitaException e) {
            log(application.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SObjectModificationException(e);
        }
    }

    private <T, E extends SBonitaException> T logAndRetrowException(final long objectId, final String methodName, final SPersistenceLogBuilder logBuilder,
            final E e) throws E {
        log(objectId, SQueriableLog.STATUS_FAIL, logBuilder, methodName);
        throw e;
    }

    private void validateUpdatedFields(final EntityUpdateDescriptor updateDescriptor, final SApplication application) throws SBonitaReadException,
            SObjectAlreadyExistsException {
        if (updateDescriptor.getFields().containsKey(SApplicationFields.TOKEN)
                && !application.getToken().equals(updateDescriptor.getFields().get(SApplicationFields.TOKEN))) {
            validateApplicationToken((String) updateDescriptor.getFields().get(SApplicationFields.TOKEN));
        }
    }

    @Override
    public long getNumberOfApplications(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SApplication.class, options, null);
    }

    @Override
    public List<SApplication> searchApplications(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.searchEntity(SApplication.class, options, null);
    }

    @Override
    public SApplicationPage createApplicationPage(final SApplicationPage applicationPage) throws SObjectCreationException, SObjectAlreadyExistsException {
        final String methodName = "createApplicationPage";
        final SApplicationPageLogBuilder logBuilder = getApplicationPageLogBuilder(ActionType.CREATED,
                "Creating application page with token " + applicationPage.getToken());
        try {
            validateApplicationPage(applicationPage);
            final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class)
                    .createInsertEvent(ApplicationService.APPLICATION_PAGE)
                    .setObject(applicationPage).done();
            recorder.recordInsert(new InsertRecord(applicationPage), insertEvent);
            log(applicationPage.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SObjectAlreadyExistsException e) {
            return logAndRetrowException(applicationPage.getId(), methodName, logBuilder, e);
        } catch (final SBonitaException e) {
            handleCreationException(applicationPage, logBuilder, e, methodName);
        }
        return applicationPage;
    }

    private void validateApplicationPage(final SApplicationPage applicationPage) throws SBonitaReadException, SObjectAlreadyExistsException {
        final String applicationPageToken = applicationPage.getToken();
        if (hasApplicationPage(applicationPage.getApplicationId(), applicationPageToken)) {
            final StringBuilder stb = new StringBuilder();
            stb.append("An application page with token '");
            stb.append(applicationPageToken);
            stb.append("' already exists for the application with id '");
            stb.append(applicationPage.getApplicationId());
            stb.append("'");
            throw new SObjectAlreadyExistsException(stb.toString());
        }
    }

    private boolean hasApplicationPage(final long applicationId, final String name) throws SBonitaReadException {
        final SApplicationPage applicationPage = getApplicationPage(applicationId, name);
        return applicationPage != null;
    }

    public SApplicationPage getApplicationPage(final long applicationId, final String applicationPageToken) throws SBonitaReadException {
        final Map<String, Object> inputParameters = new HashMap<String, Object>(2);
        inputParameters.put("applicationId", applicationId);
        inputParameters.put("applicationPageToken", applicationPageToken);
        final SApplicationPage applicationPage = persistenceService
                .selectOne(new SelectOneDescriptor<SApplicationPage>("getApplicationPageByTokenAndApplicationId", inputParameters, SApplicationPage.class));
        return applicationPage;
    }

    @Override
    public SApplicationPage getApplicationPage(final String applicationToken, final String applicationPageToken) throws SBonitaReadException,
            SObjectNotFoundException {
        final Map<String, Object> inputParameters = new HashMap<String, Object>(2);
        inputParameters.put("applicationToken", applicationToken);
        inputParameters.put("applicationPageToken", applicationPageToken);
        final SApplicationPage applicationPage = persistenceService
                .selectOne(new SelectOneDescriptor<SApplicationPage>("getApplicationPageByTokenAndApplicationToken", inputParameters, SApplicationPage.class));
        if (applicationPage == null) {
            final StringBuilder stb = new StringBuilder();
            stb.append("No application page found with name '");
            stb.append(applicationPageToken);
            stb.append("' and application token '");
            stb.append(applicationToken);
            stb.append("'.");
            throw new SObjectNotFoundException(stb.toString());
        }
        return applicationPage;
    }

    @Override
    public SApplicationPage getApplicationPage(final long applicationPageId) throws SBonitaReadException, SObjectNotFoundException {
        final SApplicationPage applicationPage = executeGetApplicationPageById(applicationPageId);
        if (applicationPage == null) {
            throw new SObjectNotFoundException("No application page found with id '" + applicationPageId + "'.");
        }
        return applicationPage;
    }

    private SApplicationPage executeGetApplicationPageById(final long applicationPageId) throws SBonitaReadException {
        return persistenceService
                .selectById(new SelectByIdDescriptor<SApplicationPage>("getApplicationPageById", SApplicationPage.class, applicationPageId));
    }

    @Override
    public SApplicationPage deleteApplicationPage(final long applicationPageId) throws SObjectModificationException, SObjectNotFoundException {
        final String methodName = "deleteApplicationPage";
        final SApplicationPageLogBuilder logBuilder = getApplicationPageLogBuilder(ActionType.DELETED, "Deleting application page with id "
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
        final SApplicationPageLogBuilder logBuilder = getApplicationPageLogBuilder(ActionType.DELETED, "Deleting application page with id "
                + applicationPage.getId());
        try {
            final SDeleteEvent event = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(ApplicationService.APPLICATION_PAGE)
                    .setObject(applicationPage).done();
            applicationPageDestructor.onDeleteApplicationPage(applicationPage);
            recorder.recordDelete(new DeleteRecord(applicationPage), event);
            log(applicationPage.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SObjectModificationException e) {
            logAndRetrowException(applicationPage.getId(), methodName, logBuilder, e);
        } catch (final SBonitaException e) {
            throwModificationException(applicationPage.getId(), logBuilder, methodName, e);
        }

    }

    @Override
    public SApplicationPage getApplicationHomePage(final long applicationId) throws SBonitaReadException, SObjectNotFoundException {
        final Map<String, Object> inputParameters = new HashMap<String, Object>(2);
        inputParameters.put("applicationId", applicationId);
        final SApplicationPage applicationPage = persistenceService
                .selectOne(new SelectOneDescriptor<SApplicationPage>("getApplicationHomePage", inputParameters, SApplicationPage.class));
        if (applicationPage == null) {
            final StringBuilder stb = new StringBuilder();
            stb.append("No home page found for application with id '");
            stb.append(applicationId);
            stb.append("'.");
            throw new SObjectNotFoundException(stb.toString());
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
    public SApplicationMenu createApplicationMenu(final SApplicationMenu applicationMenu) throws SObjectCreationException {
        final String methodName = "createApplicationMenu";
        final SApplicationMenuLogBuilder logBuilder = getApplicationMenuLogBuilder(ActionType.CREATED,
                "Creating application menu with display name " + applicationMenu.getDisplayName());
        try {
            final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class)
                    .createInsertEvent(ApplicationService.APPLICATION_MENU)
                    .setObject(applicationMenu).done();
            recorder.recordInsert(new InsertRecord(applicationMenu), insertEvent);
            log(applicationMenu.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SRecorderException e) {
            handleCreationException(applicationMenu, logBuilder, e, methodName);
        }
        return applicationMenu;
    }

    @Override
    public SApplicationMenu updateApplicationMenu(final long applicationMenuId, final EntityUpdateDescriptor updateDescriptor)
            throws SObjectModificationException,
            SObjectNotFoundException {
        final String methodName = "updateApplicationMenu";
        final SApplicationMenuLogBuilder logBuilder = getApplicationMenuLogBuilder(ActionType.UPDATED, "Updating application menu with id " + applicationMenuId);

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
    public SApplicationMenu updateApplicationMenu(final SApplicationMenu applicationMenu, final EntityUpdateDescriptor updateDescriptor,
            final boolean organizeIndexes)
            throws SObjectModificationException {
        final String methodName = "updateApplicationMenu";
        final SApplicationMenuLogBuilder logBuilder = getApplicationMenuLogBuilder(ActionType.UPDATED,
                "Updating application menu with id " + applicationMenu.getId());

        try {
            organizeIndexesOnUpdate(applicationMenu, updateDescriptor, organizeIndexes);
            final SUpdateEvent updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class)
                    .createUpdateEvent(ApplicationService.APPLICATION_MENU)
                    .setObject(applicationMenu).done();
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(applicationMenu,
                    updateDescriptor);
            recorder.recordUpdate(updateRecord, updateEvent);
            log(applicationMenu.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
            return applicationMenu;
        } catch (final SBonitaException e) {
            log(applicationMenu.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SObjectModificationException(e);
        }
    }

    private void organizeIndexesOnUpdate(final SApplicationMenu applicationMenu, final EntityUpdateDescriptor updateDescriptor, final boolean organizeIndexes)
            throws SObjectModificationException, SBonitaReadException {
        final Map<String, Object> fields = updateDescriptor.getFields();
        if (fields.containsKey(SApplicationMenuFields.PARENT_ID) && !fields.containsKey(SApplicationMenuFields.INDEX)) {
            //we need to force the update of index, as it has change of parent
            fields.put(SApplicationMenuFields.INDEX, getNextAvailableIndex((Long) fields.get(SApplicationMenuFields.PARENT_ID)));
        }
        final Integer newIndexValue = (Integer) fields.get(SApplicationMenuFields.INDEX);
        if (newIndexValue != null && organizeIndexes) {
            final MenuIndex oldIndex = menuIndexConverter.toMenuIndex(applicationMenu);
            final MenuIndex newIndex = menuIndexConverter.toMenuIndex(applicationMenu, updateDescriptor);
            indexManager.organizeIndexesOnUpdate(oldIndex, newIndex);
        }
    }

    @Override
    public SApplicationMenu getApplicationMenu(final long applicationMenuId) throws SBonitaReadException, SObjectNotFoundException {
        final SApplicationMenu applicationMenu = persistenceService
                .selectById(new SelectByIdDescriptor<SApplicationMenu>("getApplicationMenuById", SApplicationMenu.class, applicationMenuId));
        if (applicationMenu == null) {
            throw new SObjectNotFoundException("No application found with id '" + applicationMenuId + "'.");
        }
        return applicationMenu;
    }

    @Override
    public SApplicationMenu deleteApplicationMenu(final long applicationMenuId) throws SObjectModificationException, SObjectNotFoundException {
        final String methodName = "deleteApplicationMenu";
        final SApplicationMenuLogBuilder logBuilder = getApplicationMenuLogBuilder(ActionType.DELETED, "Deleting application menu with id " + applicationMenuId);
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
            final SDeleteEvent event = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(ApplicationService.APPLICATION_MENU)
                    .setObject(applicationMenu).done();
            applicationMenuDestructor.onDeleteApplicationMenu(applicationMenu);
            final int lastUsedIndex = getLastUsedIndex(applicationMenu.getParentId());
            indexManager.organizeIndexesOnDelete(new MenuIndex(applicationMenu.getParentId(), applicationMenu.getIndex(), lastUsedIndex));
            recorder.recordDelete(new DeleteRecord(applicationMenu), event);
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
        final SelectListDescriptor<String> selectList = new SelectListDescriptor<String>("getAllPagesForProfile", Collections.<String, Object> singletonMap(
                "profileId", profileId), SApplicationPage.class, new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS));
        return persistenceService.selectList(selectList);
    }

    protected Integer executeGetLastUsedIndexQuery(final Long parentMenuId) throws SBonitaReadException {
        SelectOneDescriptor<Integer> selectDescriptor;
        if (parentMenuId == null) {
            selectDescriptor = new SelectOneDescriptor<Integer>("getLastIndexForRootMenu", Collections.<String, Object> emptyMap(),
                    SApplicationMenu.class);
        } else {
            final SApplicationMenuBuilderFactoryImpl factory = new SApplicationMenuBuilderFactoryImpl();
            selectDescriptor = new SelectOneDescriptor<Integer>("getLastIndexForChildOf", Collections.<String, Object> singletonMap(
                    factory.getParentIdKey(), parentMenuId),
                    SApplicationMenu.class);
        }
        final Integer lastUsedIndex = persistenceService.selectOne(selectDescriptor);
        return lastUsedIndex;
    }

    @Override
    public int getLastUsedIndex(final Long parentMenuId) throws SBonitaReadException {
        final Integer lastUsedIndex = executeGetLastUsedIndexQuery(parentMenuId);
        return lastUsedIndex == null ? 0 : lastUsedIndex;
    }

}
