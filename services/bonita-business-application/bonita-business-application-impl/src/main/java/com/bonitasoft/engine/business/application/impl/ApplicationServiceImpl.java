/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bonitasoft.engine.business.application.impl.converter.MenuIndexConverter;
import org.bonitasoft.engine.builder.BuilderFactory;
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

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.SInvalidDisplayNameException;
import com.bonitasoft.engine.business.application.SInvalidTokenException;
import com.bonitasoft.engine.business.application.impl.cleaner.ApplicationDestructor;
import com.bonitasoft.engine.business.application.impl.cleaner.ApplicationMenuCleaner;
import com.bonitasoft.engine.business.application.impl.cleaner.ApplicationMenuDestructor;
import com.bonitasoft.engine.business.application.impl.cleaner.ApplicationPageDestructor;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.SApplicationMenu;
import com.bonitasoft.engine.business.application.model.SApplicationPage;
import com.bonitasoft.engine.business.application.model.builder.SApplicationBuilderFactory;
import com.bonitasoft.engine.business.application.model.builder.SApplicationLogBuilder;
import com.bonitasoft.engine.business.application.model.builder.SApplicationMenuLogBuilder;
import com.bonitasoft.engine.business.application.model.builder.SApplicationPageLogBuilder;
import com.bonitasoft.engine.business.application.model.builder.impl.SApplicationFields;
import com.bonitasoft.engine.business.application.model.builder.impl.SApplicationLogBuilderImpl;
import com.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuBuilderFactoryImpl;
import com.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuFields;
import com.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuLogBuilderImpl;
import com.bonitasoft.engine.business.application.model.builder.impl.SApplicationPageLogBuilderImpl;
import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationServiceImpl implements ApplicationService {

    public static final int MAX_RESULTS = 1000;
    private final Recorder recorder;

    private final ReadPersistenceService persistenceService;

    private final QueriableLoggerService queriableLoggerService;

    private final SApplicationBuilderFactory applicationKeyProvider;

    private final boolean active;

    private IndexManager indexManager;
    private MenuIndexConverter menuIndexConverter;
    private ApplicationDestructor applicationDestructor;
    private ApplicationPageDestructor applicationPageDestructor;
    private ApplicationMenuDestructor applicationMenuDestructor;

    public ApplicationServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceService, final QueriableLoggerService queriableLoggerService) {
        this(Manager.getInstance(), recorder, persistenceService, queriableLoggerService, null, null, null, null, null);
        this.indexManager = new IndexManager(new IndexUpdater(this, MAX_RESULTS), new MenuIndexValidator());
        this.menuIndexConverter = new MenuIndexConverter(this);
        ApplicationMenuCleaner applicationMenuCleaner = new ApplicationMenuCleaner(this);
        this.applicationDestructor = new ApplicationDestructor(applicationMenuCleaner);
        this.applicationPageDestructor = new ApplicationPageDestructor(applicationMenuCleaner, new HomePageChecker(this));
        this.applicationMenuDestructor = new ApplicationMenuDestructor(applicationMenuCleaner);
    }

    ApplicationServiceImpl(final Manager manager, final Recorder recorder, final ReadPersistenceService persistenceService,
            final QueriableLoggerService queriableLoggerService, IndexManager indexManager, MenuIndexConverter menuIndexConverter,
            ApplicationDestructor applicationDestructor, ApplicationPageDestructor applicationPageDestructor,
            ApplicationMenuDestructor applicationMenuDestructor) {
        this.indexManager = indexManager;
        this.menuIndexConverter = menuIndexConverter;
        this.applicationDestructor = applicationDestructor;
        this.applicationPageDestructor = applicationPageDestructor;
        this.applicationMenuDestructor = applicationMenuDestructor;
        active = manager.isFeatureActive(Features.BUSINESS_APPLICATIONS);
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.queriableLoggerService = queriableLoggerService;
        applicationKeyProvider = BuilderFactory.get(SApplicationBuilderFactory.class);
    }

    @Override
    public SApplication createApplication(final SApplication application) throws SObjectCreationException, SObjectAlreadyExistsException,
            SInvalidTokenException, SInvalidDisplayNameException {
        checkLicense();
        final String methodName = "createApplication";
        final SApplicationLogBuilder logBuilder = getApplicationLogBuilder(ActionType.CREATED, "Creating application named " + application.getToken());
        try {
            validateApplication(application);
            final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(ApplicationService.APPLICATION)
                    .setObject(application).done();
            recorder.recordInsert(new InsertRecord(application), insertEvent);
            log(application.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SInvalidTokenException e) {
            return logAndRetrowException(application.getId(), methodName, logBuilder, e);
        } catch (final SInvalidDisplayNameException e) {
            return logAndRetrowException(application.getId(), methodName, logBuilder, e);
        } catch (final SObjectAlreadyExistsException e) {
            return logAndRetrowException(application.getId(), methodName, logBuilder, e);
        } catch (final SBonitaException e) {
            handleCreationException(application, logBuilder, e, methodName);
        }
        return application;
    }

    private final void checkLicense() {
        if (!active) {
            throw new IllegalStateException("Living application is not an active feature.");
        }
    }

    private void validateApplication(final SApplication application) throws SInvalidTokenException, SBonitaReadException, SObjectAlreadyExistsException,
            SObjectCreationException, SInvalidDisplayNameException {
        final String applicationName = application.getToken();
        validateApplicationToken(applicationName);
        validateApplicationDisplayName(application.getDisplayName());

    }

    private void validateApplicationToken(final String applicationToken) throws SInvalidTokenException, SBonitaReadException, SObjectAlreadyExistsException {
        if (!URLValidator.isValid(applicationToken)) {
            throw new SInvalidTokenException(
                    "Invalid application token '"
                            + applicationToken
                            + "': the token can not be null or empty and should contain only alpha numeric characters and the following special characters '-', '.', '_' or '~'");
        }
        if (hasApplicationWithToken(applicationToken)) {
            throw new SObjectAlreadyExistsException("An application already exists with token '" + applicationToken + "'.");
        }
    }

    private void validateApplicationDisplayName(final String applicationDisplayName) throws SInvalidDisplayNameException {
        if (applicationDisplayName == null || applicationDisplayName.trim().isEmpty()) {
            throw new SInvalidDisplayNameException("The application display name can not be null or empty");
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

    public SApplication getApplicationByToken(String token) throws SBonitaReadException {
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
        checkLicense();
        final SApplication application = persistenceService
                .selectById(new SelectByIdDescriptor<SApplication>("getApplicationById", SApplication.class, applicationId));
        if (application == null) {
            throw new SObjectNotFoundException("No application found with id '" + applicationId + "'.");
        }
        return application;
    }

    @Override
    public void deleteApplication(final long applicationId) throws SObjectModificationException, SObjectNotFoundException {
        checkLicense();
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
            throws SObjectModificationException, SInvalidTokenException, SInvalidDisplayNameException, SObjectNotFoundException, SObjectAlreadyExistsException {
        checkLicense();
        final String methodName = "updateApplication";
        final SApplicationLogBuilder logBuilder = getApplicationLogBuilder(ActionType.UPDATED, "Updating application with id " + applicationId);

        try {
            Long homePageId = (Long) updateDescriptor.getFields().get(SApplicationFields.HOME_PAGE_ID);
            if(homePageId != null) {
                SApplicationPage applicationPage = executeGetApplicationPageById(homePageId);
                if(applicationPage == null) {
                    throw new SObjectModificationException("Invalid home page id: No application page found with id '" + homePageId + "'");
                }
            }
            final SApplication application = getApplication(applicationId);
            return updateApplication(application, updateDescriptor);

        } catch (SObjectNotFoundException e) {
            throw e;
        } catch (SInvalidTokenException e) {
            throw e;
        } catch (SInvalidDisplayNameException e) {
            throw e;
        } catch (SObjectAlreadyExistsException e) {
            throw e;
        } catch (SObjectModificationException e) {
            throw e;
        } catch (final SBonitaException e) {
            log(applicationId, SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SObjectModificationException(e);
        }
    }

    @Override
    public SApplication updateApplication(SApplication application, final EntityUpdateDescriptor updateDescriptor)
            throws SObjectModificationException, SInvalidTokenException, SInvalidDisplayNameException, SObjectAlreadyExistsException {
        checkLicense();
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

        } catch (SInvalidTokenException e) {
            return logAndRetrowException(application.getId(), methodName, logBuilder, e);
        } catch (SInvalidDisplayNameException e) {
            return logAndRetrowException(application.getId(), methodName, logBuilder, e);
        } catch (SObjectAlreadyExistsException e) {
            return logAndRetrowException(application.getId(), methodName, logBuilder, e);
        } catch (final SBonitaException e) {
            log(application.getId(), SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SObjectModificationException(e);
        }
    }

    private <T, E extends SBonitaException> T logAndRetrowException(long objectId, String methodName, SPersistenceLogBuilder logBuilder, E e) throws E {
        log(objectId, SQueriableLog.STATUS_FAIL, logBuilder, methodName);
        throw e;
    }

    private void validateUpdatedFields(EntityUpdateDescriptor updateDescriptor, SApplication application) throws SInvalidTokenException, SBonitaReadException,
            SObjectAlreadyExistsException, SInvalidDisplayNameException {
        if (updateDescriptor.getFields().containsKey(SApplicationFields.TOKEN)
                && !application.getToken().equals(updateDescriptor.getFields().get(SApplicationFields.TOKEN))) {
            validateApplicationToken((String) updateDescriptor.getFields().get(SApplicationFields.TOKEN));
        }
        if (updateDescriptor.getFields().containsKey(SApplicationFields.DISPLAY_NAME)
                && !application.getDisplayName().equals(updateDescriptor.getFields().get(SApplicationFields.DISPLAY_NAME))) {
            validateApplicationDisplayName((String) updateDescriptor.getFields().get(SApplicationFields.DISPLAY_NAME));
        }
    }

    @Override
    public long getNumberOfApplications(final QueryOptions options) throws SBonitaReadException {
        checkLicense();
        return persistenceService.getNumberOfEntities(SApplication.class, options, null);
    }

    @Override
    public List<SApplication> searchApplications(final QueryOptions options) throws SBonitaReadException {
        checkLicense();
        return persistenceService.searchEntity(SApplication.class, options, null);
    }

    @Override
    public SApplicationPage createApplicationPage(final SApplicationPage applicationPage) throws SObjectCreationException, SObjectAlreadyExistsException,
            SInvalidTokenException {
        checkLicense();
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
        } catch (final SInvalidTokenException e) {
            return logAndRetrowException(applicationPage.getId(), methodName, logBuilder, e);
        } catch (final SObjectAlreadyExistsException e) {
            return logAndRetrowException(applicationPage.getId(), methodName, logBuilder, e);
        } catch (final SBonitaException e) {
            handleCreationException(applicationPage, logBuilder, e, methodName);
        }
        return applicationPage;
    }

    @Override
    public SApplicationPage updateApplicationPage(long applicationPageId, EntityUpdateDescriptor updateDescriptor) throws SObjectModificationException,
            SInvalidTokenException, SObjectAlreadyExistsException, SObjectNotFoundException {
        return null;
    }

    private void validateApplicationPage(final SApplicationPage applicationPage) throws SInvalidTokenException, SBonitaReadException,
            SObjectAlreadyExistsException {
        final String applicationPageToken = applicationPage.getToken();
        if (!URLValidator.isValid(applicationPageToken)) {
            throw new SInvalidTokenException(
                    "Invalid application page token'"
                            + applicationPageToken
                            + "': the token can not be null or empty and should contain only alpha numeric characters and the following special characters '-', '.', '_' or '~'");
        }
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
        checkLicense();
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

    private SApplicationPage executeGetApplicationPageById(long applicationPageId) throws SBonitaReadException {
        return persistenceService
                .selectById(new SelectByIdDescriptor<SApplicationPage>("getApplicationPageById", SApplicationPage.class, applicationPageId));
    }

    @Override
    public void deleteApplicationPage(final long applicationPageId) throws SObjectModificationException, SObjectNotFoundException {
        checkLicense();
        final String methodName = "deleteApplicationPage";
        final SApplicationPageLogBuilder logBuilder = getApplicationPageLogBuilder(ActionType.DELETED, "Deleting application page with id "
                + applicationPageId);
        try {
            final SApplicationPage applicationPage = getApplicationPage(applicationPageId);
            deleteApplicationPage(applicationPage);
        } catch (final SObjectNotFoundException e) {
            logAndRetrowException(applicationPageId, methodName, logBuilder, e);
        } catch (final SObjectModificationException e) {
            throw e;
        } catch (final SBonitaException e) {
            throwModificationException(applicationPageId, logBuilder, methodName, e);
        }

    }

    @Override
    public void deleteApplicationPage(final SApplicationPage applicationPage) throws SObjectModificationException {
        checkLicense();
        final String methodName = "deleteApplicationPage";
        final SApplicationPageLogBuilder logBuilder = getApplicationPageLogBuilder(ActionType.DELETED, "Deleting application page with id "
                + applicationPage.getId());
        try {
            final SDeleteEvent event = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(ApplicationService.APPLICATION_PAGE)
                    .setObject(applicationPage).done();
            applicationPageDestructor.onDeleteApplicationPage(applicationPage);
            recorder.recordDelete(new DeleteRecord(applicationPage), event);
            log(applicationPage.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (SObjectModificationException e) {
            logAndRetrowException(applicationPage.getId(), methodName, logBuilder, e);
        } catch (final SBonitaException e) {
            throwModificationException(applicationPage.getId(), logBuilder, methodName, e);
        }

    }

    @Override
    public SApplicationPage getApplicationHomePage(final long applicationId) throws SBonitaReadException, SObjectNotFoundException {
        checkLicense();
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
        checkLicense();
        return persistenceService.getNumberOfEntities(SApplicationPage.class, options, null);
    }

    @Override
    public List<SApplicationPage> searchApplicationPages(final QueryOptions options) throws SBonitaReadException {
        checkLicense();
        return persistenceService.searchEntity(SApplicationPage.class, options, null);
    }

    @Override
    public SApplicationMenu createApplicationMenu(final SApplicationMenu applicationMenu) throws SObjectCreationException {
        checkLicense();
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
    public SApplicationMenu updateApplicationMenu(long applicationMenuId, EntityUpdateDescriptor updateDescriptor) throws SObjectModificationException,
            SObjectNotFoundException {
        checkLicense();
        final String methodName = "updateApplicationMenu";
        final SApplicationMenuLogBuilder logBuilder = getApplicationMenuLogBuilder(ActionType.UPDATED, "Updating application menu with id " + applicationMenuId);

        try {
            final SApplicationMenu applicationMenu = getApplicationMenu(applicationMenuId);

            updateApplicationMenu(applicationMenu, updateDescriptor, true);
            return applicationMenu;
        } catch (SObjectNotFoundException e) {
            return logAndRetrowException(applicationMenuId, methodName, logBuilder, e);
        } catch (SBonitaReadException e) {
            log(applicationMenuId, SQueriableLog.STATUS_FAIL, logBuilder, methodName);
            throw new SObjectModificationException(e);
        }
    }

    @Override
    public SApplicationMenu updateApplicationMenu(SApplicationMenu applicationMenu, EntityUpdateDescriptor updateDescriptor, boolean organizeIndexes)
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

    private void organizeIndexesOnUpdate(SApplicationMenu applicationMenu, EntityUpdateDescriptor updateDescriptor, boolean organizeIndexes)
            throws SObjectModificationException, SBonitaReadException {
        Map<String, Object> fields = updateDescriptor.getFields();
        if (fields.containsKey(SApplicationMenuFields.PARENT_ID) && !fields.containsKey(SApplicationMenuFields.INDEX)) {
            //we need to force the update of index, as it has change of parent
            fields.put(SApplicationMenuFields.INDEX, getNextAvailableIndex((Long) fields.get(SApplicationMenuFields.PARENT_ID)));
        }
        Integer newIndexValue = (Integer) fields.get(SApplicationMenuFields.INDEX);
        if (newIndexValue != null && organizeIndexes) {
            MenuIndex oldIndex = menuIndexConverter.toMenuIndex(applicationMenu);
            MenuIndex newIndex = menuIndexConverter.toMenuIndex(applicationMenu, updateDescriptor);
            indexManager.organizeIndexesOnUpdate(oldIndex, newIndex);
        }
    }

    @Override
    public SApplicationMenu getApplicationMenu(final long applicationMenuId) throws SBonitaReadException, SObjectNotFoundException {
        checkLicense();
        final SApplicationMenu applicationMenu = persistenceService
                .selectById(new SelectByIdDescriptor<SApplicationMenu>("getApplicationMenuById", SApplicationMenu.class, applicationMenuId));
        if (applicationMenu == null) {
            throw new SObjectNotFoundException("No application found with id '" + applicationMenuId + "'.");
        }
        return applicationMenu;
    }

    @Override
    public void deleteApplicationMenu(final long applicationMenuId) throws SObjectModificationException, SObjectNotFoundException {
        checkLicense();
        final String methodName = "deleteApplicationMenu";
        final SApplicationMenuLogBuilder logBuilder = getApplicationMenuLogBuilder(ActionType.DELETED, "Deleting application menu with id " + applicationMenuId);
        try {
            final SApplicationMenu applicationMenu = getApplicationMenu(applicationMenuId);
            deleteApplicationMenu(applicationMenu);
            log(applicationMenu.getId(), SQueriableLog.STATUS_OK, logBuilder, methodName);
        } catch (final SObjectNotFoundException e) {
            logAndRetrowException(applicationMenuId, methodName, logBuilder, e);
        } catch (SBonitaReadException e) {
            throwModificationException(applicationMenuId, logBuilder, methodName, e);
        }
    }

    @Override
    public void deleteApplicationMenu(SApplicationMenu applicationMenu) throws SObjectModificationException {
        final String methodName = "deleteApplicationMenu";
        final SApplicationMenuLogBuilder logBuilder = getApplicationMenuLogBuilder(ActionType.DELETED,
                "Deleting application menu with id " + applicationMenu.getId());
        try {
            final SDeleteEvent event = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(ApplicationService.APPLICATION_MENU)
                    .setObject(applicationMenu).done();
            applicationMenuDestructor.onDeleteApplicationMenu(applicationMenu);
            int lastUsedIndex = getLastUsedIndex(applicationMenu.getParentId());
            indexManager.organizeIndexesOnDelete(new MenuIndex(applicationMenu.getParentId(), applicationMenu.getIndex(), lastUsedIndex));
            recorder.recordDelete(new DeleteRecord(applicationMenu), event);
        } catch (final SBonitaException e) {
            throwModificationException(applicationMenu.getId(), logBuilder, methodName, e);
        }
    }

    @Override
    public long getNumberOfApplicationMenus(final QueryOptions options) throws SBonitaReadException {
        checkLicense();
        return persistenceService.getNumberOfEntities(SApplicationMenu.class, options, null);
    }

    @Override
    public List<SApplicationMenu> searchApplicationMenus(final QueryOptions options) throws SBonitaReadException {
        checkLicense();
        return persistenceService.searchEntity(SApplicationMenu.class, options, null);
    }

    public int getNextAvailableIndex(Long parentMenuId) throws SBonitaReadException {
        int lastIndex = getLastUsedIndex(parentMenuId);
        return lastIndex + 1;
    }

    protected Integer executeGetLastUsedIndexQuery(Long parentMenuId) throws SBonitaReadException {
        SelectOneDescriptor<Integer> selectDescriptor;
        if (parentMenuId == null) {
            selectDescriptor = new SelectOneDescriptor<Integer>("getLastIndexForRootMenu", Collections.<String, Object> emptyMap(),
                    SApplicationMenu.class);
        } else {
            SApplicationMenuBuilderFactoryImpl factory = new SApplicationMenuBuilderFactoryImpl();
            selectDescriptor = new SelectOneDescriptor<Integer>("getLastIndexForChildOf", Collections.<String, Object> singletonMap(
                    factory.getParentIdKey(), parentMenuId),
                    SApplicationMenu.class);
        }
        Integer lastUsedIndex = persistenceService.selectOne(selectDescriptor);
        return lastUsedIndex;
    }

    public int getLastUsedIndex(Long parentMenuId) throws SBonitaReadException {
        Integer lastUsedIndex = executeGetLastUsedIndexQuery(parentMenuId);
        return lastUsedIndex == null ? 0 : lastUsedIndex;
    }

}
