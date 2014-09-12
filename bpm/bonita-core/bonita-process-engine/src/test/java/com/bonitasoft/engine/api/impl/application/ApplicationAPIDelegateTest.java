package com.bonitasoft.engine.api.impl.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.api.impl.convertor.ApplicationConvertor;
import com.bonitasoft.engine.api.impl.transaction.application.SearchApplications;
import com.bonitasoft.engine.business.application.Application;
import com.bonitasoft.engine.business.application.ApplicationCreator;
import com.bonitasoft.engine.business.application.ApplicationNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.SInvalidNameException;
import com.bonitasoft.engine.business.application.impl.ApplicationImpl;
import com.bonitasoft.engine.business.application.model.SApplicationState;
import com.bonitasoft.engine.business.application.model.impl.SApplicationImpl;
import com.bonitasoft.engine.exception.InvalidNameException;
import com.bonitasoft.engine.service.TenantServiceAccessor;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationAPIDelegateTest {

    @Mock
    private TenantServiceAccessor accessor;

    @Mock
    private ApplicationConvertor convertor;

    @Mock
    private SearchApplications searchApplications;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private SearchResult<Application> appSearchResult;

    private ApplicationAPIDelegate delegate;

    private static final long APPLICATION_ID = 15;

    private static final long LOGGED_USER_ID = 10;

    private static final String APP_NAME = "app";

    private static final String APP_DISP_NAME = "My app";

    private static final String VERSION = "1.0";

    private static final String PATH = "/app";

    private static final String DESCRIPTION = "app desc";

    @Before
    public void setUp() throws Exception {
        given(accessor.getApplicationService()).willReturn(applicationService);
        delegate = new ApplicationAPIDelegate(accessor, convertor, LOGGED_USER_ID, searchApplications);
    }

    @Test
    public void createApplication_should_call_applicationService_createApplication_and_return_created_application() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(APP_NAME, APP_DISP_NAME, VERSION, PATH);
        creator.setDescription(DESCRIPTION);
        final SApplicationImpl sApp = getDefaultApplication();
        sApp.setDescription(DESCRIPTION);
        final ApplicationImpl application = new ApplicationImpl(APP_NAME, VERSION, PATH, DESCRIPTION);
        given(convertor.buildSApplication(creator, LOGGED_USER_ID)).willReturn(sApp);
        given(convertor.toApplication(sApp)).willReturn(application);
        given(applicationService.createApplication(sApp)).willReturn(sApp);

        //when
        final Application createdApplication = delegate.createApplication(creator);

        //then
        assertThat(createdApplication).isEqualTo(application);
    }

    private SApplicationImpl getDefaultApplication() {
        final SApplicationImpl sApp = new SApplicationImpl(APP_NAME, APP_DISP_NAME, VERSION, PATH, System.currentTimeMillis(),
                LOGGED_USER_ID, SApplicationState.DEACTIVATED.name());
        return sApp;
    }

    @Test(expected = AlreadyExistsException.class)
    public void createApplication_should_throw_AlreadyExistsException_when_applicationService_throws_SObjectAlreadyExistsException() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(APP_NAME, APP_DISP_NAME, VERSION, PATH);
        final SApplicationImpl sApp = getDefaultApplication();
        given(convertor.buildSApplication(creator, LOGGED_USER_ID)).willReturn(sApp);
        given(applicationService.createApplication(sApp)).willThrow(new SObjectAlreadyExistsException(""));

        //when
        delegate.createApplication(creator);

        //then exception
    }

    @Test(expected = CreationException.class)
    public void createApplication_should_throw_CreationException_when_applicationService_throws_SObjectCreationException() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(APP_NAME, APP_DISP_NAME, VERSION, PATH);
        final SApplicationImpl sApp = getDefaultApplication();
        given(convertor.buildSApplication(creator, LOGGED_USER_ID)).willReturn(sApp);
        given(applicationService.createApplication(sApp)).willThrow(new SObjectCreationException(""));

        //when
        delegate.createApplication(creator);

        //then exception
    }

    @Test(expected = InvalidNameException.class)
    public void createApplication_should_throw_InvalidNameException_when_applicationService_throws_SInvalidNameException() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(APP_NAME, APP_DISP_NAME, VERSION, PATH);
        final SApplicationImpl sApp = getDefaultApplication();
        given(convertor.buildSApplication(creator, LOGGED_USER_ID)).willReturn(sApp);
        given(applicationService.createApplication(sApp)).willThrow(new SInvalidNameException(""));

        //when
        delegate.createApplication(creator);

        //then exception
    }

    @Test
    public void delete_Application_should_call_applicationService_delete() throws Exception {
        //when
        delegate.deleteApplication(APPLICATION_ID);

        //then
        verify(applicationService, times(1)).deleteApplication(APPLICATION_ID);
    }

    @Test(expected = DeletionException.class)
    public void delete_Application_should_throw_DeletionException_when_applicationService_throws_SObjectModificationException() throws Exception {
        doThrow(new SObjectModificationException()).when(applicationService).deleteApplication(APPLICATION_ID);

        //when
        delegate.deleteApplication(APPLICATION_ID);

        //then exception
    }

    @Test(expected = DeletionException.class)
    public void delete_Application_should_throw_DeletionException_when_applicationService_throws_SObjectNotFoundException() throws Exception {
        doThrow(new SObjectNotFoundException()).when(applicationService).deleteApplication(APPLICATION_ID);

        //when
        delegate.deleteApplication(APPLICATION_ID);

        //then exception
    }

    @Test
    public void getApplication_should_return_the_application_returned_by_applicationService_coverted() throws Exception {
        final SApplicationImpl sApp = getDefaultApplication();
        final ApplicationImpl application = new ApplicationImpl(APP_NAME, VERSION, PATH, null);
        given(applicationService.getApplication(APPLICATION_ID)).willReturn(sApp);
        given(convertor.toApplication(sApp)).willReturn(application);

        //when
        final Application retriedApp = delegate.getApplication(APPLICATION_ID);

        //then
        assertThat(retriedApp).isEqualTo(application);

    }

    @Test(expected = RetrieveException.class)
    public void getApplication_should_throw_RetrieveException_when_applicationService_throws_SBonitaReadException() throws Exception {
        //given
        given(applicationService.getApplication(APPLICATION_ID)).willThrow(new SBonitaReadException(""));

        //when
        delegate.getApplication(APPLICATION_ID);

        //then exception
    }

    @Test(expected = ApplicationNotFoundException.class)
    public void getApplication_should_throw_ApplicationNotFoundException_when_applicationService_throws_SObjectNotFoundException() throws Exception {
        //given
        given(applicationService.getApplication(APPLICATION_ID)).willThrow(new SObjectNotFoundException());

        //when
        delegate.getApplication(APPLICATION_ID);

        //then exception
    }

    @Test
    public void searchApplications_should_return_the_result_of_searchApplications_getResult() throws Exception {
        //given
        given(searchApplications.getResult()).willReturn(appSearchResult);

        //when
        final SearchResult<Application> retrievedSearchResult = delegate.searchApplications();

        //then
        assertThat(retrievedSearchResult).isEqualTo(appSearchResult);
        verify(searchApplications, times(1)).execute();
    }

    @Test(expected = SearchException.class)
    public void searchApplications_should_throw_SearchException_when_searchApplications_throws_SBonitaException() throws Exception {
        //given
        doThrow(new SBonitaReadException("")).when(searchApplications).execute();

        //when
        delegate.searchApplications();

        //then exception
    }

}
