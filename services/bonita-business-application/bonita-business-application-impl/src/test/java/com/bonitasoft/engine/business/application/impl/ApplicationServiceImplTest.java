package com.bonitasoft.engine.business.application.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.SApplication;
import com.bonitasoft.engine.business.application.SApplicationPage;


@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceImplTest {

    private static final int CREATED_BY = 10;

    private static final String APPLICATION_NAME = "app";

    @Mock
    private Recorder recorder;

    @Mock
    private ReadPersistenceService persistenceService;

    @Mock
    private QueriableLoggerService queriableLogService;

    private SApplication application;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    @Before
    public void setUp() throws Exception {
        when(queriableLogService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(true);
        application = buildApplication(APPLICATION_NAME);
        application.setId(10L);
    }

    private SApplication buildApplication(final String applicationName) {
        return new SApplicationBuilderFactoryImpl().createNewInstance(applicationName, "1.0", "/" + applicationName, CREATED_BY).done();
    }

    @Test
    public void createApplication_should_call_recordInsert_and_return_created_object() throws Exception {
        //given
        final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(ApplicationService.APPLICATION)
                .setObject(application).done();
        final InsertRecord record = new InsertRecord(application);

        //when
        final SApplication createdApplication = applicationService.createApplication(application);

        //then
        assertThat(createdApplication).isEqualTo(application);
        verify(recorder, times(1)).recordInsert(record, insertEvent);
    }

    @Test(expected = SObjectCreationException.class)
    public void createApplication_should_throw_SObjectCreationException_when_record_insert_throws_Exception() throws Exception {
        //given
        doThrow(new SRecorderException("")).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));

        //when
        applicationService.createApplication(application);

        //then exception
    }

    @Test
    public void createApplication_should_throw_SObjectAlreadyExistsException_when_an_application_with_the_same_name_already_exists() throws Exception {
        //given
        final String name = APPLICATION_NAME;
        given(persistenceService.selectOne(new SelectOneDescriptor<SApplication>("getApplicationByName", Collections.<String, Object> singletonMap("name",
                name), SApplication.class))).willReturn(application);

        final SApplication newApp = buildApplication(APPLICATION_NAME);

        //when
        try {
            applicationService.createApplication(newApp);
            fail("Exception expected");
        } catch (final SObjectAlreadyExistsException e) {
            //then
            assertThat(e.getMessage()).isEqualTo("An application already exists with name '" + name + "'.");
            verify(recorder, never()).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
        }

    }

    @Test
    public void getApplication_should_return_result_of_persitence_service_selectById() throws Exception {
        //given
        given(persistenceService.selectById(new SelectByIdDescriptor<SApplication>("getApplicationById", SApplication.class, 10L))).willReturn(application);

        //when
        final SApplication retrievedApp = applicationService.getApplication(10L);

        //then
        assertThat(retrievedApp).isEqualTo(application);
    }

    @Test
    public void getApplication_should_throw_SObjectNotFoundException_when_persitence_service_returns_null() throws Exception {
        //given
        final long applicationId = 10L;
        given(persistenceService.selectById(new SelectByIdDescriptor<SApplication>("getApplicationById", SApplication.class, applicationId))).willReturn(null);

        //when
        try {
            applicationService.getApplication(applicationId);
            fail("Exception expected");
        } catch (final SObjectNotFoundException e) {
            //then
            assertThat(e.getMessage()).isEqualTo("No application found with id '" + applicationId + "'.");
        }

    }

    @Test
    public void deleteApplication_should_call_record_delete() throws Exception {
        //given
        final long applicationId = 10L;
        given(persistenceService.selectById(new SelectByIdDescriptor<SApplication>("getApplicationById", SApplication.class, applicationId))).willReturn(application);

        //when
        applicationService.deleteApplication(applicationId);

        //then
        final SDeleteEvent event = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(ApplicationService.APPLICATION)
                .setObject(application).done();
        verify(recorder, times(1)).recordDelete(new DeleteRecord(application), event);
    }

    @Test
    public void getNumberOfApplications_should_return_the_result_of_persitenceService_getNumberOfEntities() throws Exception {
        //given
        final QueryOptions options = new QueryOptions(0, 10);
        final long count = 7;
        given(persistenceService.getNumberOfEntities(SApplication.class, options, null)).willReturn(count);

        //when
        final long nbOfApp = applicationService.getNumberOfApplications(options);

        //then
        assertThat(nbOfApp).isEqualTo(count);
    }

    @Test
    public void searchApplicatons_should_return_the_result_of_persitenceService_searchEntity() throws Exception {
        //given
        final QueryOptions options = new QueryOptions(0, 10);
        final List<SApplication> applications = new ArrayList<SApplication>(1);
        applications.add(mock(SApplication.class));
        given(persistenceService.searchEntity(SApplication.class, options, null)).willReturn(applications);

        //when
        final List<SApplication> retrievedApplications = applicationService.searchApplications(options);

        //then
        assertThat(retrievedApplications).isEqualTo(applications);
    }

    @Test(expected = SBonitaSearchException.class)
    public void searchApplicatons_should_SBonitaSearchException_when_persistenceSevice_throws_SBonitaReadException() throws Exception {
        //given
        final QueryOptions options = new QueryOptions(0, 10);
        given(applicationService.searchApplications(options)).willThrow(new SBonitaReadException(""));

        //when
        applicationService.searchApplications(options);

        //then exception
    }

    private SApplicationPage buildApplicationPage(final long applicationId, final long pageId, final String name) {
        return new SApplicationPageBuilderFactoryImpl().createNewInstance(applicationId, pageId, name).done();
    }

    @Test
    public void createApplicationPage_should_call_recordInsert_and_return_created_object() throws Exception {
        //given
        final SApplicationPage applicationPage = buildApplicationPage(5, 15, "mainDashBoard");
        applicationPage.setId(15);
        final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(ApplicationService.APPLICATION_PAGE)
                .setObject(applicationPage).done();
        final InsertRecord record = new InsertRecord(applicationPage);

        //when
        final SApplicationPage createdApplicationPage = applicationService.createApplicationPage(applicationPage);

        //then
        assertThat(createdApplicationPage).isEqualTo(applicationPage);
        verify(recorder, times(1)).recordInsert(record, insertEvent);
    }

    @Test(expected = SObjectCreationException.class)
    public void createApplicationPage_should_throw_SObjectCreationException_when_recorder_throws_SBonitaException() throws Exception {
        //given
        final SApplicationPage applicationPage = buildApplicationPage(5, 15, "mainDashBoard");
        applicationPage.setId(15);
        doThrow(new SRecorderException("")).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));

        //when
        applicationService.createApplicationPage(applicationPage);

        //then exception
    }

    @Test(expected = SObjectAlreadyExistsException.class)
    public void createApplicationPage_should_throw_SObjectAlreadyExistsException_when_an_applicationPage_with_the_same_name_in_the_same_application_exists()
            throws Exception {
        //given
        final SApplicationPage applicationPage = buildApplicationPage(5, 15, "mainDashBoard");
        final Map<String, Object> inputParameters = new HashMap<String, Object>(2);
        inputParameters.put("applicationId", 5);
        inputParameters.put("applicationPageName", "mainDashBoard");
        given(persistenceService.selectOne(new SelectOneDescriptor<SApplicationPage>("getApplicationPageByNameAndApplicationId", inputParameters,
                SApplicationPage.class
                ))).willReturn(applicationPage);

        //when
        final SApplicationPage applicationPageToCreate = buildApplicationPage(5, 16, "mainDashBoard");
        applicationPageToCreate.setId(7);
        applicationService.createApplicationPage(applicationPageToCreate);

        //then exception
    }

    @Test
    public void getApplicationPage_should_return_result_of_persitence_service_selectById() throws Exception {
        //given
        final SApplicationPage applicationPage = buildApplicationPage(10, 20, "myPage");
        given(persistenceService.selectById(new SelectByIdDescriptor<SApplicationPage>("getApplicationPageById", SApplicationPage.class, 10L))).willReturn(
                applicationPage);

        //when
        final SApplicationPage retrievedAppPage = applicationService.getApplicationPage(10L);

        //then
        assertThat(retrievedAppPage).isEqualTo(applicationPage);
    }

    @Test
    public void getApplicationPage_by_name_and_appName_should_return_result_of_persitence_service_selectOne() throws Exception {
        //given
        final SApplicationPage applicationPage = buildApplicationPage(10, 20, "myPage");
        final Map<String, Object> inputParameters = new HashMap<String, Object>(2);
        inputParameters.put("applicationName", "app");
        inputParameters.put("applicationPageName", "firstPage");
        given(persistenceService.selectOne(new SelectOneDescriptor<SApplicationPage>("getApplicationPageByNameAndApplicationName", inputParameters,
                SApplicationPage.class
                ))).willReturn(applicationPage);

        //when
        final SApplicationPage retrievedAppPage = applicationService.getApplicationPage("app", "firstPage");

        //then
        assertThat(retrievedAppPage).isEqualTo(applicationPage);
    }

    @Test(expected = SObjectNotFoundException.class)
    public void getApplicationPage_by_name_and_appName_should_throw_SObjectNotFoundException_when_persitence_service_selectOne_returns_null() throws Exception {
        //given
        given(persistenceService.selectOne(Matchers.<SelectOneDescriptor<SApplicationPage>> any())).willReturn(null);

        //when
        applicationService.getApplicationPage("app", "firstPage");

        //then exception
    }

    @Test
    public void deleteApplicationPage_should_call_record_delete_with_applicationPage_idenfied_by_the_given_id() throws Exception {
        //given
        final long applicationPageId = 10L;
        final SApplicationPage applicationPage = buildApplicationPage(20, 30, "myPage");
        applicationPage.setId(27);
        given(persistenceService.selectById(new SelectByIdDescriptor<SApplicationPage>("getApplicationPageById", SApplicationPage.class, applicationPageId)))
        .willReturn(applicationPage);

        //when
        applicationService.deleteApplicationPage(applicationPageId);

        //then
        final SDeleteEvent event = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(ApplicationService.APPLICATION_PAGE)
                .setObject(applicationPage).done();
        verify(recorder, times(1)).recordDelete(new DeleteRecord(applicationPage), event);
    }

}
