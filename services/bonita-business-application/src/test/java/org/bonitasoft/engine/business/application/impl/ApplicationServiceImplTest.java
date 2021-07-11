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

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.business.application.ApplicationService.APPLICATION;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.*;

import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.impl.cleaner.ApplicationDestructor;
import org.bonitasoft.engine.business.application.impl.cleaner.ApplicationMenuDestructor;
import org.bonitasoft.engine.business.application.impl.cleaner.ApplicationPageDestructor;
import org.bonitasoft.engine.business.application.impl.converter.MenuIndexConverter;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.SApplicationState;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;
import org.bonitasoft.engine.business.application.model.builder.SApplicationMenuUpdateBuilder;
import org.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilder;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceImplTest {

    private static final int CREATED_BY = 10;

    private static final String APPLICATION_TOKEN = "app";

    private static final String APPLICATION_DISPLAY_NAME = "My app";

    public static final long LAYOUT_ID = 15L;

    public static final long THEME_ID = 16L;

    @Mock
    private Recorder recorder;

    @Mock
    private ReadPersistenceService persistenceService;

    @Mock
    private QueriableLoggerService queriableLogService;

    @Mock
    private IndexManager indexManager;

    @Mock
    private MenuIndexConverter convertor;

    @Mock
    private ApplicationDestructor applicationDestructor;

    @Mock
    private ApplicationPageDestructor applicationPageDestructor;

    @Mock
    private ApplicationMenuDestructor applicationMenuDestructor;

    private SApplicationWithIcon application;

    private ApplicationServiceImpl applicationServiceImpl;

    @Before
    public void setUp() {
        applicationServiceImpl = new ApplicationServiceImpl(recorder, persistenceService, queriableLogService,
                indexManager, convertor, applicationDestructor, applicationPageDestructor, applicationMenuDestructor);

        when(queriableLogService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(true);
        application = buildApplication(APPLICATION_TOKEN, APPLICATION_DISPLAY_NAME);
        application.setId(10L);
    }

    private SApplicationWithIcon buildApplication(final String applicationName, final String applicationDisplayName) {
        final long currentDate = System.currentTimeMillis();
        SApplicationWithIcon application = new SApplicationWithIcon();
        application.setToken(applicationName);
        application.setDisplayName(applicationDisplayName);
        application.setVersion("1.0");
        application.setCreationDate(currentDate);
        application.setLastUpdateDate(currentDate);
        application.setCreatedBy(CREATED_BY);
        application.setState(SApplicationState.ACTIVATED.name());
        application.setLayoutId(LAYOUT_ID);
        application.setThemeId(THEME_ID);
        return application;
    }

    @Test
    public void createApplication_should_call_recordInsert_and_return_created_object() throws Exception {
        //given
        final InsertRecord record = new InsertRecord(application);

        //when
        final SApplicationWithIcon createdApplication = applicationServiceImpl.createApplication(application);

        //then
        assertThat(createdApplication).isEqualTo(application);
        verify(recorder, times(1)).recordInsert(record, APPLICATION);
    }

    @Test(expected = SObjectCreationException.class)
    public void createApplication_should_throw_SObjectCreationException_when_record_insert_throws_Exception()
            throws Exception {
        //given
        doThrow(new SRecorderException("")).when(recorder).recordInsert(any(InsertRecord.class), anyString());

        //when
        applicationServiceImpl.createApplication(application);

        //then exception
    }

    @Test
    public void createApplication_should_throw_SObjectAlreadyExistsException_when_an_application_with_the_same_name_already_exists()
            throws Exception {
        //given
        SApplication app = new SApplication();
        app.setId(125);
        given(persistenceService.selectOne(new SelectOneDescriptor<SApplication>("getApplicationByToken",
                Collections.singletonMap("name",
                        APPLICATION_TOKEN),
                SApplication.class))).willReturn(app);

        final SApplicationWithIcon newApp = buildApplication(APPLICATION_TOKEN, APPLICATION_DISPLAY_NAME);

        //when
        try {
            applicationServiceImpl.createApplication(newApp);
            fail("Exception expected");
        } catch (final SObjectAlreadyExistsException e) {
            //then
            assertThat(e.getMessage())
                    .isEqualTo("An application already exists with token '" + APPLICATION_TOKEN + "'.");
            verify(recorder, never()).recordInsert(any(InsertRecord.class), anyString());
        }

    }

    @Test
    public void getApplication_should_return_result_of_persistence_service_selectById() throws Exception {
        //given
        SApplication app = new SApplication();
        app.setId(10L);
        given(persistenceService.selectById(new SelectByIdDescriptor<>(SApplication.class, 10L)))
                .willReturn(app);

        //when
        final SApplication retrievedApp = applicationServiceImpl.getApplication(10L);

        //then
        assertThat(retrievedApp).isEqualTo(app);
    }

    @Test
    public void getApplication_should_throw_SObjectNotFoundException_when_persitence_service_returns_null()
            throws Exception {
        //given
        final long applicationId = 10L;
        given(persistenceService.selectById(new SelectByIdDescriptor<>(SApplication.class, applicationId)))
                .willReturn(null);

        //when
        try {
            applicationServiceImpl.getApplication(applicationId);
            fail("Exception expected");
        } catch (final SObjectNotFoundException e) {
            //then
            assertThat(e.getMessage()).isEqualTo("No application found with id '" + applicationId + "'.");
        }

    }

    @Test
    public void getApplicationByToken_should_return_result_of_persistence_service_getApplicationByToken()
            throws Exception {
        //given
        SApplication app = new SApplication();
        app.setToken("name");
        given(persistenceService.selectOne(new SelectOneDescriptor<SApplication>("getApplicationByToken",
                singletonMap("name", APPLICATION_TOKEN),
                SApplication.class))).willReturn(app);

        //when
        SApplication retriedApplication = applicationServiceImpl.getApplicationByToken(APPLICATION_TOKEN);

        //then
        assertThat(retriedApplication).isEqualTo(app);

    }

    @Test
    public void deleteApplication_should_call_record_delete() throws Exception {
        //given
        final long applicationId = 10L;
        SApplication app = new SApplication();
        app.setId(10L);
        given(persistenceService.selectById(argThat(s -> s.getId() == 10L))).willReturn(app);

        //when
        applicationServiceImpl.deleteApplication(applicationId);

        //then
        verify(recorder, times(1)).recordDelete(new DeleteRecord(app), APPLICATION);
    }

    @Test
    public void deleteApplication_should_throw_exception_when_targeting_non_editable_app() throws Exception {

        //given
        final long applicationId = 10L;
        SApplication app = new SApplication();
        app.setId(10L);
        app.setEditable(false);
        given(persistenceService.selectById(argThat(s -> s.getId() == 10L))).willReturn(app);

        //when
        String exceptionMessage = assertThrows("Not the right exception", SObjectModificationException.class,
                () -> applicationServiceImpl.deleteApplication(applicationId)).getMessage();

        //then
        assertThat(exceptionMessage).contains("The application is set as non modifiable. It cannot be deleted");

    }

    @Test
    public void deleteApplication_should_call_applicationDestructor() throws Exception {
        //given
        final ApplicationServiceImpl applicationService = spy(applicationServiceImpl);

        final long applicationId = 10L;
        SApplication app = new SApplication();
        app.setId(10L);
        doReturn(app).when(applicationService).getApplication(applicationId);

        //when
        applicationService.deleteApplication(applicationId);

        //then
        verify(applicationDestructor, times(1)).onDeleteApplication(app);
    }

    @Test(expected = SObjectNotFoundException.class)
    public void deleteApplication_should_throw_SObjectNotFoundException_when_no_application_with_the_given_id_is_found()
            throws Exception {
        //given
        final long applicationId = 10L;
        given(persistenceService.selectById(new SelectByIdDescriptor<>(SApplication.class, applicationId)))
                .willReturn(null);

        //when
        applicationServiceImpl.deleteApplication(applicationId);

        //then exception
    }

    @Test(expected = SObjectModificationException.class)
    public void deleteApplication_should_throw_SObjectModificationException_when_recorder_throws_SRecorderException()
            throws Exception {
        //given
        given(persistenceService.selectById(new SelectByIdDescriptor<>(SApplication.class, 10L)))
                .willReturn(new SApplication());
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), anyString());

        //when
        applicationServiceImpl.deleteApplication(10L);

        //then exception
    }

    @Test
    public void getNumberOfApplications_should_return_the_result_of_persitenceService_getNumberOfEntities()
            throws Exception {
        //given
        final QueryOptions options = new QueryOptions(0, 10);
        final long count = 7;
        given(persistenceService.getNumberOfEntities(SApplication.class, options, null)).willReturn(count);

        //when
        final long nbOfApp = applicationServiceImpl.getNumberOfApplications(options);

        //then
        assertThat(nbOfApp).isEqualTo(count);
    }

    @Test
    public void searchApplications_should_return_the_result_of_persitenceService_searchEntity() throws Exception {
        //given
        final QueryOptions options = new QueryOptions(0, 10);
        final List<SApplication> applications = new ArrayList<>(1);
        applications.add(mock(SApplication.class));
        given(persistenceService.searchEntity(SApplication.class, options, null)).willReturn(applications);

        //when
        final List<SApplication> retrievedApplications = applicationServiceImpl.searchApplications(options);

        //then
        assertThat(retrievedApplications).isEqualTo(applications);
    }

    @Test(expected = SBonitaReadException.class)
    public void searchApplications_should_throw_SBonitaReadException_when_persistenceSevice_throws_SBonitaReadException()
            throws Exception {
        //given
        final QueryOptions options = new QueryOptions(0, 10);
        given(applicationServiceImpl.searchApplications(options)).willThrow(new SBonitaReadException(""));

        //when
        applicationServiceImpl.searchApplications(options);

        //then exception
    }

    private SApplicationPage buildApplicationPage(final long applicationId, final long pageId, final String name) {
        return SApplicationPage.builder().applicationId(applicationId).pageId(pageId).token(name).build();
    }

    private SApplicationPage buildApplicationPage(final long applicationPageId, final long applicationId,
            final long pageId, final String pageToken) {
        final SApplicationPage applicationPage = buildApplicationPage(applicationId, pageId, pageToken);
        applicationPage.setId(applicationPageId);
        return applicationPage;
    }

    @Test
    public void createApplicationPage_should_call_recordInsert_and_return_created_object() throws Exception {
        //given
        final SApplicationPage applicationPage = buildApplicationPage(15, 5, 15, "mainDashBoard");
        final InsertRecord record = new InsertRecord(applicationPage);

        //when
        final SApplicationPage createdApplicationPage = applicationServiceImpl.createApplicationPage(applicationPage);

        //then
        assertThat(createdApplicationPage).isEqualTo(applicationPage);
        verify(recorder, times(1)).recordInsert(record, ApplicationService.APPLICATION_PAGE);
    }

    @Test(expected = SObjectCreationException.class)
    public void createApplicationPage_should_throw_SObjectCreationException_when_recorder_throws_SBonitaException()
            throws Exception {
        //given
        final SApplicationPage applicationPage = buildApplicationPage(15, 5, 15, "mainDashBoard");
        doThrow(new SRecorderException("")).when(recorder).recordInsert(any(InsertRecord.class), anyString());

        //when
        applicationServiceImpl.createApplicationPage(applicationPage);

        //then exception
    }

    @Test(expected = SObjectAlreadyExistsException.class)
    public void createApplicationPage_should_throw_SObjectAlreadyExistsException_when_an_applicationPage_with_the_same_name_in_the_same_application_exists()
            throws Exception {
        //given
        final SApplicationPage applicationPage = buildApplicationPage(5, 15, "mainDashBoard");
        final Map<String, Object> inputParameters = new HashMap<>(2);
        inputParameters.put("applicationId", 5);
        inputParameters.put("applicationPageToken", "mainDashBoard");
        given(persistenceService.selectOne(
                new SelectOneDescriptor<SApplicationPage>("getApplicationPageByTokenAndApplicationId", inputParameters,
                        SApplicationPage.class))).willReturn(applicationPage);

        //when
        final SApplicationPage applicationPageToCreate = buildApplicationPage(7, 5, 16, "mainDashBoard");
        applicationServiceImpl.createApplicationPage(applicationPageToCreate);

        //then exception
    }

    @Test
    public void getApplicationPage_should_return_result_of_persitence_service_selectById() throws Exception {
        //given
        final SApplicationPage applicationPage = buildApplicationPage(10, 20, "myPage");
        given(persistenceService.selectById(new SelectByIdDescriptor<>(SApplicationPage.class, 10L))).willReturn(
                applicationPage);

        //when
        final SApplicationPage retrievedAppPage = applicationServiceImpl.getApplicationPage(10L);

        //then
        assertThat(retrievedAppPage).isEqualTo(applicationPage);
    }

    @Test
    public void getApplicationPage_by_name_and_appName_should_return_result_of_persistence_service_selectOne()
            throws Exception {
        //given
        final SApplicationPage applicationPage = buildApplicationPage(10, 20, "myPage");
        final Map<String, Object> inputParameters = new HashMap<>(2);
        inputParameters.put("applicationName", "app");
        inputParameters.put("applicationPageToken", "firstPage");
        given(persistenceService.selectOne(new SelectOneDescriptor<SApplicationPage>(
                "getApplicationPageByTokenAndApplicationToken", inputParameters,
                SApplicationPage.class))).willReturn(applicationPage);

        //when
        final SApplicationPage retrievedAppPage = applicationServiceImpl.getApplicationPage("app", "firstPage");

        //then
        assertThat(retrievedAppPage).isEqualTo(applicationPage);
    }

    @Test(expected = SObjectNotFoundException.class)
    public void getApplicationPage_by_name_and_appName_should_throw_SObjectNotFoundException_when_persitence_service_selectOne_returns_null()
            throws Exception {
        //given
        given(persistenceService.selectOne(ArgumentMatchers.<SelectOneDescriptor<SApplicationPage>> any()))
                .willReturn(null);

        //when
        applicationServiceImpl.getApplicationPage("app", "firstPage");

        //then exception
    }

    @Test
    public void deleteApplicationPage_should_call_record_delete_with_applicationPage_identified_by_the_given_id()
            throws Exception {
        //given
        final ApplicationServiceImpl applicationService = spy(applicationServiceImpl);
        final long applicationPageId = 10L;
        final SApplicationPage applicationPage = buildApplicationPage(applicationPageId, 20, 30, "myPage");
        doReturn(applicationPage).when(applicationService).getApplicationPage(applicationPageId);
        final long applicationId = 20L;

        //when
        applicationService.deleteApplicationPage(applicationPageId);

        //then
        final ArgumentCaptor<DeleteRecord> deleteRecordCaptor = ArgumentCaptor.forClass(DeleteRecord.class);
        verify(recorder, times(1)).recordDelete(deleteRecordCaptor.capture(), eq(ApplicationService.APPLICATION_PAGE));
        assertThat(deleteRecordCaptor.getValue().getEntity()).isEqualTo(applicationPage);
    }

    @Test
    public void deleteApplicationPage_should_call_applicationPageDestructor() throws Exception {
        //given
        final ApplicationServiceImpl applicationService = spy(applicationServiceImpl);

        //application page
        final long applicationPageId = 10L;
        final long applicationId = 20L;
        final SApplicationPage applicationPage = buildApplicationPage(applicationPageId, applicationId, 30, "myPage");

        //when
        applicationService.deleteApplicationPage(applicationPage);

        //then
        verify(applicationPageDestructor, times(1)).onDeleteApplicationPage(applicationPage);
    }

    @Test(expected = SObjectModificationException.class)
    public void deleteApplicationPage_should_throw_SObjectModificationException_when_applicationPageDestructor_throws_SObjectModificationException()
            throws Exception {
        //given
        final ApplicationServiceImpl applicationService = spy(applicationServiceImpl);

        //application page
        final long applicationPageId = 10L;
        final long applicationId = 20L;
        final SApplicationPage applicationPage = buildApplicationPage(applicationPageId, applicationId, 30, "myPage");
        doThrow(new SObjectModificationException()).when(applicationPageDestructor)
                .onDeleteApplicationPage(applicationPage);

        //when
        applicationService.deleteApplicationPage(applicationPage);

        //then exception
    }

    @Test(expected = SObjectNotFoundException.class)
    public void deleteApplicationPage_should_throw_SObjectNotFound_when_there_is_no_applicationPage_for_the_given_id()
            throws Exception {
        //given
        final long applicationPageId = 10L;
        given(persistenceService.selectById(new SelectByIdDescriptor<>(SApplicationPage.class, applicationPageId)))
                .willReturn(null);

        //when
        applicationServiceImpl.deleteApplicationPage(applicationPageId);

        //then exception
    }

    @Test(expected = SObjectModificationException.class)
    public void deleteApplicationPage_should_throw_SObjectModificationException_when_recorder_throws_SRecorderException()
            throws Exception {
        //given
        final long applicationPageId = 10L;
        final SApplicationPage applicationPage = buildApplicationPage(27, 20, 30, "myPage");
        given(persistenceService.selectById(new SelectByIdDescriptor<>(SApplicationPage.class, applicationPageId)))
                .willReturn(applicationPage);
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), anyString());

        //when
        applicationServiceImpl.deleteApplicationPage(applicationPageId);

        //then exception
    }

    @Test
    public void updateApplication_should_call_recorder_recordUpdate_and_return_updated_object() throws Exception {
        //given
        EntityUpdateDescriptor updateDescriptor = new SApplicationUpdateBuilder(0L)
                .updateDisplayName("new display name").done();

        long applicationId = 17;
        given(persistenceService.selectById(new SelectByIdDescriptor<>(SApplicationWithIcon.class, applicationId)))
                .willReturn(
                        application);

        //when
        final SApplicationWithIcon updatedApplication = applicationServiceImpl.updateApplication(applicationId,
                updateDescriptor);

        //then
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(application,
                updateDescriptor);
        verify(recorder, times(1)).recordUpdate(updateRecord, APPLICATION);
        assertThat(updatedApplication).isEqualTo(application);
    }

    @Test
    public void update_application_display_name_should_throw_exception_called_on_non_editable_application()
            throws Exception {
        //given
        application.setEditable(false);
        EntityUpdateDescriptor updateDescriptor = new SApplicationUpdateBuilder(0L)
                .updateDisplayName("new display name").done();

        long applicationId = 17;
        given(persistenceService.selectById(new SelectByIdDescriptor<>(SApplicationWithIcon.class, applicationId)))
                .willReturn(application);
        //when
        String exceptionMessage = assertThrows("Not the right exception", SObjectModificationException.class,
                () -> applicationServiceImpl.updateApplication(applicationId, updateDescriptor)).getMessage();
        assertThat(exceptionMessage)
                .contains("The application is provided. Only the theme, the layout, and the icon can be updated");

        //cleanup
        application.setEditable(true);
    }

    @Test
    public void update_non_editable_application_should_allow_to_change_icon_theme_and_layout() throws Exception {
        //given
        application.setEditable(false);
        EntityUpdateDescriptor updateDescriptor = new SApplicationUpdateBuilder(0L)
                .updateThemeId(1L).updateLayoutId(2L).updateIconMimeType("image/jpg")
                .updateIconContent("toto".getBytes()).done();

        long applicationId = 17;
        given(persistenceService.selectById(new SelectByIdDescriptor<>(SApplicationWithIcon.class, applicationId)))
                .willReturn(application);
        //when
        applicationServiceImpl.updateApplication(applicationId, updateDescriptor);

        //then:
        verify(recorder).recordUpdate(UpdateRecord.buildSetFields(application, updateDescriptor), APPLICATION);

        //cleanup
        application.setEditable(true);
    }

    @Test(expected = SObjectModificationException.class)
    public void updateApplication_should_throw_SObjectModificationException_if_set_homepage_references_invalid_page()
            throws Exception {
        //given
        final SApplicationUpdateBuilder updateBuilder = new SApplicationUpdateBuilder(0L);
        final long homePageId = 150L;
        updateBuilder.updateHomePageId(homePageId);
        final EntityUpdateDescriptor updateDescriptor = updateBuilder.done();

        final int applicationId = 17;

        final SApplicationWithIcon applicationWithIcon = mock(SApplicationWithIcon.class);
        doReturn(true).when(applicationWithIcon).isEditable();
        given(persistenceService.selectById(new SelectByIdDescriptor<>(SApplicationWithIcon.class, applicationId)))
                .willReturn(applicationWithIcon);

        given(persistenceService.selectById(new SelectByIdDescriptor<>(SApplicationPage.class, homePageId)))
                .willReturn(null);

        //when
        applicationServiceImpl.updateApplication(applicationId, updateDescriptor);

        //then exception
    }

    @Test(expected = SObjectNotFoundException.class)
    public void updateApplication_should_throw_SObjectModificationException_when_recorder_throws_SRecorderException()
            throws Exception {
        //given
        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        updateDescriptor.addField("name", "newName");
        final int applicationId = 17;
        //when
        applicationServiceImpl.updateApplication(applicationId, updateDescriptor);

        //then exception
    }

    @Test(expected = SObjectAlreadyExistsException.class)
    public void updateApplication_should_throw_SObjectAlreadyExistsException_when_another_application_exists_with_the_same_name()
            throws Exception {
        //given
        final EntityUpdateDescriptor updateDescriptor = new SApplicationUpdateBuilder(0L).updateToken("newToken")
                .done();

        SApplicationWithIcon applicationToUpdate = new SApplicationWithIcon();
        applicationToUpdate.setToken("oldToken");
        doReturn(applicationToUpdate).when(persistenceService).selectById(argThat(s -> s.getId() == 17));
        SApplication existingApplication = new SApplication();
        existingApplication.setToken("newToken");
        doReturn(existingApplication).when(persistenceService)
                .selectOne(argThat(s -> s.getQueryName().equals("getApplicationByToken") &&
                        s.getInputParameter("token").equals("newToken")));

        //when
        applicationServiceImpl.updateApplication(17, updateDescriptor);

        //then exception
    }

    @Test
    public void getApplicationHomePage_should_return_result_of_persitence_service_selectOne() throws Exception {
        //given
        final SApplicationPage applicationPage = buildApplicationPage(10, 20, "myPage");
        final Map<String, Object> inputParameters = new HashMap<>(2);
        inputParameters.put("applicationId", 100);
        given(persistenceService
                .selectOne(new SelectOneDescriptor<SApplicationPage>("getApplicationHomePage", inputParameters,
                        SApplicationPage.class))).willReturn(applicationPage);

        //when
        final SApplicationPage homePage = applicationServiceImpl.getApplicationHomePage(100);

        //then
        assertThat(homePage).isEqualTo(applicationPage);
    }

    @Test(expected = SObjectNotFoundException.class)
    public void getApplicationHomePage_should_throw_SObjectNotFoundException_when_persitence_service_selectOne_returns_null()
            throws Exception {
        //given
        final Map<String, Object> inputParameters = new HashMap<>(2);
        inputParameters.put("applicationId", 100);
        given(persistenceService
                .selectOne(new SelectOneDescriptor<SApplicationPage>("getApplicationHomePage", inputParameters,
                        SApplicationPage.class))).willReturn(null);

        //when
        applicationServiceImpl.getApplicationHomePage(100);

        //then exception
    }

    @Test
    public void getNumberOfApplicationPages_should_return_the_result_of_persitenceService_getNumberOfEntities()
            throws Exception {
        //given
        final QueryOptions options = new QueryOptions(0, 10);
        final long count = 7;
        given(persistenceService.getNumberOfEntities(SApplicationPage.class, options, null)).willReturn(count);

        //when
        final long nbOfApp = applicationServiceImpl.getNumberOfApplicationPages(options);

        //then
        assertThat(nbOfApp).isEqualTo(count);
    }

    @Test
    public void searchApplicationPages_should_return_the_result_of_persitenceService_searchEntity() throws Exception {
        //given
        final QueryOptions options = new QueryOptions(0, 10);
        final List<SApplicationPage> applicationPages = new ArrayList<>(1);
        applicationPages.add(mock(SApplicationPage.class));
        given(persistenceService.searchEntity(SApplicationPage.class, options, null)).willReturn(applicationPages);

        //when
        final List<SApplicationPage> retrievedAppPages = applicationServiceImpl.searchApplicationPages(options);

        //then
        assertThat(retrievedAppPages).isEqualTo(applicationPages);
    }

    @Test(expected = SBonitaReadException.class)
    public void searchApplicationPages_should_throw_SBonitaReadException_when_persistenceSevice_throws_SBonitaReadException()
            throws Exception {
        //given
        final QueryOptions options = new QueryOptions(0, 10);
        given(applicationServiceImpl.searchApplicationPages(options)).willThrow(new SBonitaReadException(""));

        //when
        applicationServiceImpl.searchApplicationPages(options);

        //then exception
    }

    @Test
    public void createApplicationMenu_should_call_recordInsert_and_return_created_object() throws Exception {
        //given
        final SApplicationMenu appMenu = buildApplicationMenu("main", 5, 1, 12);
        appMenu.setId(15);

        //when
        final SApplicationMenu createdAppMenu = applicationServiceImpl.createApplicationMenu(appMenu);

        //then
        assertThat(createdAppMenu).isEqualTo(appMenu);

        final ArgumentCaptor<InsertRecord> insertRecordCaptor = ArgumentCaptor.forClass(InsertRecord.class);
        verify(recorder, times(1)).recordInsert(insertRecordCaptor.capture(), eq(ApplicationService.APPLICATION_MENU));
        assertThat(insertRecordCaptor.getValue().getEntity()).isEqualTo(appMenu);
    }

    private SApplicationMenu buildApplicationMenu(final String displayName, final long applicationPageId,
            final int index, final long applicationId) {
        return SApplicationMenu.builder().displayName(displayName).applicationId(applicationId)
                .applicationPageId(applicationPageId).index(index).build();
    }

    @Test(expected = SObjectCreationException.class)
    public void createApplicationMenu_should_throw_SObjectCreationException_when_recorder_throws_Exception()
            throws Exception {
        //given
        doThrow(new SRecorderException("")).when(recorder).recordInsert(any(InsertRecord.class), anyString());

        //when
        applicationServiceImpl.createApplicationMenu(buildApplicationMenu("main", 4, 1, 12));

        //then exception
    }

    @Test
    public void updateApplicationMenu_should_call_recorder_recordUpdate_and_return_updated_object() throws Exception {
        //given
        final EntityUpdateDescriptor updateDescriptor = new SApplicationMenuUpdateBuilder()
                .updateDisplayName("new display name").done();

        final SApplicationMenu appMenu = buildApplicationMenu("main", 5, 1, 12);
        appMenu.setId(17);

        final int applicationMenuId = 17;
        given(persistenceService.selectById(new SelectByIdDescriptor<>(SApplicationMenu.class, applicationMenuId)))
                .willReturn(
                        appMenu);

        //when
        final SApplicationMenu updatedApplicationMenu = applicationServiceImpl.updateApplicationMenu(applicationMenuId,
                updateDescriptor);

        //then
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(appMenu,
                updateDescriptor);
        verify(recorder, times(1)).recordUpdate(updateRecord, ApplicationService.APPLICATION_MENU);
        assertThat(updatedApplicationMenu).isEqualTo(appMenu);
    }

    @Test
    public void updateApplicationMenu_should_organize_indexes_when_field_index_is_updated() throws Exception {
        //given
        final ApplicationServiceImpl applicationService = spy(applicationServiceImpl);
        final int newIndexValue = 1;
        final int oldIndexValue = 5;
        final EntityUpdateDescriptor updateDescriptor = new SApplicationMenuUpdateBuilder().updateIndex(newIndexValue)
                .done();

        final int applicationMenuId = 17;
        final SApplicationMenu appMenu = buildApplicationMenu("main", 5, oldIndexValue, 12);
        appMenu.setId(applicationMenuId);

        doReturn(appMenu).when(applicationService).getApplicationMenu(applicationMenuId);

        final MenuIndex newIndex = new MenuIndex(null, newIndexValue, 5);
        final MenuIndex oldIndex = new MenuIndex(null, oldIndexValue, 6);
        given(convertor.toMenuIndex(appMenu)).willReturn(oldIndex);
        given(convertor.toMenuIndex(appMenu, updateDescriptor)).willReturn(newIndex);

        //when
        applicationService.updateApplicationMenu(applicationMenuId, updateDescriptor);

        //then
        verify(indexManager, times(1)).organizeIndexesOnUpdate(oldIndex, newIndex);
    }

    @Test
    public void updateApplicationMenu_should_force_update_index_when_field_parent_is_updated() throws Exception {
        //given
        final ApplicationServiceImpl applicationService = spy(applicationServiceImpl);
        final EntityUpdateDescriptor updateDescriptor = new SApplicationMenuUpdateBuilder().updateParentId(28L).done();

        final int applicationMenuId = 17;
        final SApplicationMenu appMenu = buildApplicationMenu("main", 5, 4, 12);
        appMenu.setId(applicationMenuId);

        doReturn(appMenu).when(applicationService).getApplicationMenu(applicationMenuId);

        final MenuIndex newIndex = new MenuIndex(28L, 6, 5);
        final MenuIndex oldIndex = new MenuIndex(null, 4, 6);
        given(convertor.toMenuIndex(appMenu)).willReturn(oldIndex);
        given(convertor.toMenuIndex(appMenu, updateDescriptor)).willReturn(newIndex);
        given(applicationService.getLastUsedIndex(28L)).willReturn(5);

        //when
        applicationService.updateApplicationMenu(applicationMenuId, updateDescriptor);

        //then
        final ArgumentCaptor<UpdateRecord> updateRecordCaptor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder, times(1)).recordUpdate(updateRecordCaptor.capture(), anyString());

        final UpdateRecord updateRecord = updateRecordCaptor.getValue();
        assertThat(updateRecord.getFields().get(SApplicationMenu.INDEX)).isEqualTo(6);
    }

    @Test
    public void updateApplicationMenu_should_not_organize_indexes_when_field_index_is_not_updated() throws Exception {
        //given
        final EntityUpdateDescriptor updateDescriptor = new SApplicationMenuUpdateBuilder()
                .updateDisplayName("new display name").done();

        final SApplicationMenu appMenu = buildApplicationMenu("main", 5, 1, 12);
        appMenu.setId(17);

        final int applicationMenuId = 17;
        given(persistenceService.selectById(new SelectByIdDescriptor<>(SApplicationMenu.class, applicationMenuId)))
                .willReturn(
                        appMenu);

        //when
        applicationServiceImpl.updateApplicationMenu(applicationMenuId, updateDescriptor);

        //then
        verifyZeroInteractions(indexManager);
    }

    @Test(expected = SObjectNotFoundException.class)
    public void updateApplicationMenu_should_throw_SObjectNotFoundException_when_recorder_returns_null()
            throws Exception {
        //given
        final EntityUpdateDescriptor updateDescriptor = new SApplicationMenuUpdateBuilder()
                .updateDisplayName("new display name").done();

        final int applicationMenuId = 17;
        given(persistenceService.selectById(new SelectByIdDescriptor<>(SApplicationMenu.class, applicationMenuId)))
                .willReturn(
                        null);

        //when
        applicationServiceImpl.updateApplicationMenu(applicationMenuId, updateDescriptor);

        //then exception
    }

    @Test(expected = SObjectModificationException.class)
    public void updateApplicationMenu_should_throw_SObjectModificationException_when_recorder_throws_SRecorderException()
            throws Exception {
        //given
        final EntityUpdateDescriptor updateDescriptor = new SApplicationMenuUpdateBuilder()
                .updateDisplayName("new display name").done();

        final SApplicationMenu appMenu = buildApplicationMenu("main", 5, 1, 12);
        appMenu.setId(17);

        final int applicationMenuId = 17;
        given(persistenceService.selectById(new SelectByIdDescriptor<>(SApplicationMenu.class, applicationMenuId)))
                .willReturn(
                        appMenu);
        doThrow(new SRecorderException("")).when(recorder).recordUpdate(any(UpdateRecord.class), anyString());

        //when
        applicationServiceImpl.updateApplicationMenu(applicationMenuId, updateDescriptor);

        //then exception
    }

    @Test
    public void getApplicationMenu_by_id_should_return_result_of_persitence_service() throws Exception {
        //given
        final SApplicationMenu applicationMenu = buildApplicationMenu("main", 2, 1, 12);
        final SelectByIdDescriptor<SApplicationMenu> selectDescriptor = new SelectByIdDescriptor<>(
                SApplicationMenu.class, 3);
        given(persistenceService.selectById(selectDescriptor)).willReturn(applicationMenu);

        //when
        final SApplicationMenu retrievedAppMenu = applicationServiceImpl.getApplicationMenu(3);

        //then
        assertThat(retrievedAppMenu).isEqualTo(applicationMenu);
    }

    @Test(expected = SObjectNotFoundException.class)
    public void getApplicationMenu_by_id_should_throw_SObjectNotFoundException_when_persistence_service_returns_null()
            throws Exception {
        //given
        final SelectByIdDescriptor<SApplicationMenu> selectDescriptor = new SelectByIdDescriptor<>(
                SApplicationMenu.class, 3);
        given(persistenceService.selectById(selectDescriptor)).willReturn(null);

        //when
        applicationServiceImpl.getApplicationMenu(3);

        //then exception
    }

    @Test
    public void deleteApplicationMenu_should_delete_application_menu_identified_for_the_given_identifier()
            throws Exception {
        //given
        final int applicationMenuId = 3;
        final SApplicationMenu applicationMenu = buildApplicationMenu("main", 2, 1, 12);
        applicationMenu.setId(applicationMenuId);
        final SelectByIdDescriptor<SApplicationMenu> selectDescriptor = new SelectByIdDescriptor<>(
                SApplicationMenu.class, applicationMenuId);
        given(persistenceService.selectById(selectDescriptor)).willReturn(applicationMenu);
        final SelectOneDescriptor<Integer> descriptor = new SelectOneDescriptor<>("getLastIndexForRootMenu",
                Collections.emptyMap(),
                SApplicationMenu.class);
        given(persistenceService.selectOne(descriptor)).willReturn(2);

        //when
        applicationServiceImpl.deleteApplicationMenu(applicationMenuId);

        //then
        final ArgumentCaptor<DeleteRecord> deleteRecordCaptor = ArgumentCaptor.forClass(DeleteRecord.class);
        verify(recorder, times(1)).recordDelete(deleteRecordCaptor.capture(), eq(ApplicationService.APPLICATION_MENU));
        assertThat(deleteRecordCaptor.getValue().getEntity()).isEqualTo(applicationMenu);
    }

    @Test
    public void deleteApplicationMenu_should_call_applicationMenuDestructor() throws Exception {
        //given
        final ApplicationServiceImpl applicationService = spy(applicationServiceImpl);

        final int applicationMenuId = 3;
        final SApplicationMenu applicationMenu = buildApplicationMenu("main", 2, 1, 12);
        applicationMenu.setId(applicationMenuId);

        //when
        applicationService.deleteApplicationMenu(applicationMenu);

        //then
        verify(applicationMenuDestructor, times(1)).onDeleteApplicationMenu(applicationMenu);
    }

    @Test
    public void deleteApplicationMenu_without_parent_should_update_indexes() throws Exception {
        //given
        final int indexValue = 6;
        final int lastUsedIndex = 10;
        final int applicationId = 3;
        final SApplicationMenu applicationMenu = buildApplicationMenu("main", 2, indexValue, 12);
        applicationMenu.setId(applicationId);
        final SelectByIdDescriptor<SApplicationMenu> selectDescriptor = new SelectByIdDescriptor<>(
                SApplicationMenu.class, applicationId);
        given(persistenceService.selectById(selectDescriptor)).willReturn(applicationMenu);

        final SelectOneDescriptor<Integer> descriptor = new SelectOneDescriptor<>("getLastIndexForRootMenu",
                Collections.emptyMap(),
                SApplicationMenu.class);
        given(persistenceService.selectOne(descriptor)).willReturn(lastUsedIndex);

        final MenuIndex menuIndex = new MenuIndex(null, indexValue, lastUsedIndex);

        //when
        applicationServiceImpl.deleteApplicationMenu(applicationId);

        //then
        verify(indexManager, times(1)).organizeIndexesOnDelete(menuIndex);
    }

    @Test(expected = SObjectNotFoundException.class)
    public void deleteApplicationMenu_should_throw_SObjectObjectNotFoundException_if_no_application_menu_is_found_with_the_given_id()
            throws Exception {
        //given
        given(persistenceService.selectById(ArgumentMatchers.<SelectByIdDescriptor<SApplicationMenu>> any()))
                .willReturn(null);

        //when
        applicationServiceImpl.deleteApplicationMenu(5);

        //then exception
    }

    @Test(expected = SObjectModificationException.class)
    public void deleteApplicationMenu_should_throw_SObjectObjectModificationException_recorder_throws_exception()
            throws Exception {
        //given
        final SApplicationMenu applicationMenu = buildApplicationMenu("main", 1, 1, 12);
        given(persistenceService.selectById(ArgumentMatchers.<SelectByIdDescriptor<SApplicationMenu>> any()))
                .willReturn(applicationMenu);

        final SelectOneDescriptor<Integer> descriptor = new SelectOneDescriptor<>("getLastIndexForRootMenu",
                Collections.emptyMap(),
                SApplicationMenu.class);
        given(persistenceService.selectOne(descriptor)).willReturn(2);

        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), anyString());

        //when
        applicationServiceImpl.deleteApplicationMenu(5);

        //then exception
    }

    @Test
    public void getNumberOfApplicationMenus_should_return_the_result_of_persistenc_service() throws Exception {
        //given
        final QueryOptions options = new QueryOptions(0, 10);
        final long count = 7;
        given(persistenceService.getNumberOfEntities(SApplicationMenu.class, options, null)).willReturn(count);

        //when
        final long numberOfMenus = applicationServiceImpl.getNumberOfApplicationMenus(options);

        //then
        assertThat(numberOfMenus).isEqualTo(count);
    }

    @Test
    public void searchApplicationMenus_should_return_the_result_of_persistence_service() throws Exception {
        //given
        final QueryOptions options = new QueryOptions(0, 2);
        final List<SApplicationMenu> applicationMenus = new ArrayList<>(1);
        applicationMenus.add(mock(SApplicationMenu.class));
        given(persistenceService.searchEntity(SApplicationMenu.class, options, null)).willReturn(applicationMenus);

        //when
        final List<SApplicationMenu> retrievedAppMenus = applicationServiceImpl.searchApplicationMenus(options);

        //then
        assertThat(retrievedAppMenus).isEqualTo(applicationMenus);
    }

    @Test(expected = SBonitaReadException.class)
    public void searchApplicationMenus_should_throw_SBonitaSearchException_when_persistence_service_throws_exception()
            throws Exception {
        //given
        final QueryOptions options = new QueryOptions(0, 2);
        given(persistenceService.searchEntity(SApplicationMenu.class, options, null))
                .willThrow(new SBonitaReadException(""));

        //when
        applicationServiceImpl.searchApplicationMenus(options);

        //then exception
    }

    @Test
    public void getNextIndex_should_return_getLastUsedIndex_more_one() throws Exception {
        //given
        final ApplicationServiceImpl applicationService = spy(applicationServiceImpl);
        given(applicationService.getLastUsedIndex(4L)).willReturn(7);

        //when
        final int next = applicationServiceImpl.getNextAvailableIndex(4L);

        //then
        assertThat(next).isEqualTo(8);
    }

    @Test
    public void executeGetLastUsedIndexQuery_should_use_persistence_service_getLastIndexForRootMenu_if_parent_is_null()
            throws Exception {
        //given
        final SelectOneDescriptor<Integer> descriptor = new SelectOneDescriptor<>("getLastIndexForRootMenu",
                Collections.emptyMap(),
                SApplicationMenu.class);
        given(persistenceService.selectOne(descriptor)).willReturn(1);

        //when
        final Integer result = applicationServiceImpl.executeGetLastUsedIndexQuery(null);

        //then
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void executeGetLastUsedIndexQuery_should_use_persistence_service_getLastIndexForChildOf_if_parent_is_not_null()
            throws Exception {
        //given
        final long parentId = 10;
        final SelectOneDescriptor<Integer> descriptor = new SelectOneDescriptor<>("getLastIndexForChildOf",
                Collections.singletonMap(
                        SApplicationMenu.PARENT_ID, parentId),
                SApplicationMenu.class);
        given(persistenceService.selectOne(descriptor)).willReturn(1);

        //when
        final Integer result = applicationServiceImpl.executeGetLastUsedIndexQuery(parentId);

        //then
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void getLastUsedIndex_should_return_executeGetLastUsedIndexQuery_if_result_is_not_null() throws Exception {
        //given
        final ApplicationServiceImpl applicationService = spy(applicationServiceImpl);
        given(applicationService.executeGetLastUsedIndexQuery(1L)).willReturn(4);

        //when
        final int lastUsedIndex = applicationService.getLastUsedIndex(1L);

        //then
        assertThat(lastUsedIndex).isEqualTo(4);
    }

    @Test
    public void getLastUsedIndex_should_return_zero_executeGetLastUsedIndexQuery_if_result_is_null() throws Exception {
        //given
        final ApplicationServiceImpl applicationService = spy(applicationServiceImpl);
        given(applicationService.executeGetLastUsedIndexQuery(1L)).willReturn(null);

        //when
        final int lastUsedIndex = applicationService.getLastUsedIndex(1L);

        //then
        assertThat(lastUsedIndex).isEqualTo(0);
    }

    @Test
    public void getLastUsedIndex_should_return_zero_when_persistence_service_getLastIndexForChildOf_return_null()
            throws Exception {
        //given
        final SelectOneDescriptor<Integer> descriptor = new SelectOneDescriptor<>("getLastIndexForRootMenu",
                Collections.emptyMap(),
                SApplicationMenu.class);
        given(persistenceService.selectOne(descriptor)).willReturn(null);

        //when
        final Integer lastUsedIndex = applicationServiceImpl.getLastUsedIndex(null);

        //then
        assertThat(lastUsedIndex).isEqualTo(0);
    }

    @Test
    public void getLastUsedIndex_for_child_menu_should_return_zero_when_persistence_service_getLastIndexForChildOf_return_null()
            throws Exception {
        //given
        final long parentId = 10;
        final SelectOneDescriptor<Integer> descriptor = new SelectOneDescriptor<>("getLastIndexForChildOf",
                Collections.singletonMap(
                        SApplicationMenu.PARENT_ID, parentId),
                SApplicationMenu.class);
        given(persistenceService.selectOne(descriptor)).willReturn(null);

        //when
        final int next = applicationServiceImpl.getLastUsedIndex(parentId);

        //then
        assertThat(next).isEqualTo(0);
    }

}
