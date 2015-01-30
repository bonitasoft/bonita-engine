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
package org.bonitasoft.engine.api.impl.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.api.impl.converter.ApplicationModelConverter;
import org.bonitasoft.engine.api.impl.transaction.application.SearchApplications;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationCreator;
import org.bonitasoft.engine.business.application.ApplicationNotFoundException;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.ApplicationUpdater;
import org.bonitasoft.engine.business.application.impl.ApplicationImpl;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationState;
import org.bonitasoft.engine.business.application.model.impl.SApplicationImpl;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationAPIDelegateTest {

    @Mock
    private TenantServiceAccessor accessor;

    @Mock
    private ApplicationModelConverter convertor;

    @Mock
    private SearchApplications searchApplications;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private SearchResult<Application> appSearchResult;

    private ApplicationAPIDelegate delegate;

    private static final long APPLICATION_ID = 15;

    private static final long LOGGED_USER_ID = 10;

    private static final String APP_TOKEN = "app";

    private static final String APP_DISP_NAME = "My app";

    private static final String VERSION = "1.0";

    private static final String DESCRIPTION = "app desc";

    @Before
    public void setUp() throws Exception {
        given(accessor.getApplicationService()).willReturn(applicationService);
        delegate = new ApplicationAPIDelegate(accessor, convertor, LOGGED_USER_ID);
    }

    @Test
    public void createApplication_should_call_applicationService_createApplication_and_return_created_application() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(APP_TOKEN, APP_DISP_NAME, VERSION);
        creator.setDescription(DESCRIPTION);
        final SApplicationImpl sApp = getDefaultApplication();
        sApp.setDescription(DESCRIPTION);
        final ApplicationImpl application = new ApplicationImpl(APP_TOKEN, VERSION, DESCRIPTION);
        given(convertor.buildSApplication(creator, LOGGED_USER_ID)).willReturn(sApp);
        given(convertor.toApplication(sApp)).willReturn(application);
        given(applicationService.createApplication(sApp)).willReturn(sApp);

        //when
        final Application createdApplication = delegate.createApplication(creator);

        //then
        assertThat(createdApplication).isEqualTo(application);
    }

    private SApplicationImpl getDefaultApplication() {
        final SApplicationImpl sApp = new SApplicationImpl(APP_TOKEN, APP_DISP_NAME, VERSION, System.currentTimeMillis(), LOGGED_USER_ID,
                SApplicationState.DEACTIVATED.name());
        return sApp;
    }

    @Test(expected = AlreadyExistsException.class)
    public void createApplication_should_throw_AlreadyExistsException_when_applicationService_throws_SObjectAlreadyExistsException() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(APP_TOKEN, APP_DISP_NAME, VERSION);
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
        final ApplicationCreator creator = new ApplicationCreator(APP_TOKEN, APP_DISP_NAME, VERSION);
        final SApplicationImpl sApp = getDefaultApplication();
        given(convertor.buildSApplication(creator, LOGGED_USER_ID)).willReturn(sApp);
        given(applicationService.createApplication(sApp)).willThrow(new SObjectCreationException(""));

        //when
        delegate.createApplication(creator);

        //then exception
    }

    @Test
    public void createApplication_should_throw_CreationException_when_token_has_spaces() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("token with spaces", APP_DISP_NAME, VERSION);

        //when
        try {
            delegate.createApplication(creator);
            fail("exception expected");
        } catch (final CreationException e) {
            assertThat(e.getMessage()).contains("The token");
        }

        //then exception
    }

    @Test
    public void createApplication_should_throw_CreationException_when_token_isEmpty() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("", APP_DISP_NAME, VERSION);

        //when
        try {
            delegate.createApplication(creator);
            fail("exception expected");
        } catch (final CreationException e) {
            assertThat(e.getMessage()).contains("The token");
        }

        //then exception
    }

    @Test
    public void createApplication_should_throw_CreationException_when_token_isEmpty_after_trim() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(" ", APP_DISP_NAME, VERSION);

        //when
        try {
            delegate.createApplication(creator);
            fail("exception expected");
        } catch (final CreationException e) {
            assertThat(e.getMessage()).contains("The token");
        }

        //then exception
    }

    @Test(expected = CreationException.class)
    public void createApplication_should_throw_CreationException_when_display_name_is_null() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(APP_TOKEN, null, VERSION);

        //when
        delegate.createApplication(creator);

        //then exception
    }

    @Test(expected = CreationException.class)
    public void createApplication_should_throw_CreationException_when_display_name_is_empty() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(APP_TOKEN, "", VERSION);

        //when
        delegate.createApplication(creator);

        //then exception
    }

    @Test
    public void createApplication_should_throw_CreationException_when_token_is_null() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(null, APP_DISP_NAME, VERSION);

        //when
        try {
            delegate.createApplication(creator);
            fail("exception expected");
        } catch (final CreationException e) {
            assertThat(e.getMessage()).contains("The token");
        }

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
        final ApplicationImpl application = new ApplicationImpl(APP_TOKEN, VERSION, null);
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
    public void updateApplication_should_return_result_of_applicationservice_updateApplication() throws Exception {
        //given
        final SApplication sApplication = mock(SApplication.class);
        final Application application = mock(Application.class);
        final ApplicationUpdater updater = new ApplicationUpdater();
        updater.setToken("newToken");
        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        given(convertor.toApplicationUpdateDescriptor(updater, LOGGED_USER_ID)).willReturn(updateDescriptor);
        given(applicationService.updateApplication(APPLICATION_ID, updateDescriptor)).willReturn(sApplication);
        given(convertor.toApplication(sApplication)).willReturn(application);

        //when
        final Application updatedApplication = delegate.updateApplication(APPLICATION_ID, updater);

        //then
        assertThat(updatedApplication).isEqualTo(application);
    }

    @Test
    public void updateApplication_should_return_result_of_applicationservice_getApplication_when_updater_is_empty() throws Exception {
        //given
        final SApplication sApplication = mock(SApplication.class);
        final Application application = mock(Application.class);
        final ApplicationUpdater updater = new ApplicationUpdater();
        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        given(convertor.toApplicationUpdateDescriptor(updater, LOGGED_USER_ID)).willReturn(updateDescriptor);
        given(applicationService.getApplication(APPLICATION_ID)).willReturn(sApplication);
        given(convertor.toApplication(sApplication)).willReturn(application);

        //when
        final Application updatedApplication = delegate.updateApplication(APPLICATION_ID, updater);

        //then
        assertThat(updatedApplication).isEqualTo(application);
    }

    @Test(expected = UpdateException.class)
    public void updateApplication_should_throw_UpdateException_when_applicationService_throws_SObjectModificationException() throws Exception {
        //given
        final ApplicationUpdater updater = new ApplicationUpdater();
        updater.setToken("newToken");
        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        given(convertor.toApplicationUpdateDescriptor(updater, LOGGED_USER_ID)).willReturn(updateDescriptor);
        given(applicationService.updateApplication(APPLICATION_ID, updateDescriptor)).willThrow(new SObjectModificationException());

        //when
        delegate.updateApplication(APPLICATION_ID, updater);

        //then exception
    }

    @Test(expected = AlreadyExistsException.class)
    public void updateApplication_should_throw_UpdateException_when_applicationService_throws_SObjectAlreadyExistsException() throws Exception {
        //given
        final ApplicationUpdater updater = new ApplicationUpdater();
        updater.setToken("newToken");
        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        given(convertor.toApplicationUpdateDescriptor(updater, LOGGED_USER_ID)).willReturn(updateDescriptor);
        given(applicationService.updateApplication(APPLICATION_ID, updateDescriptor)).willThrow(new SObjectAlreadyExistsException());

        //when
        delegate.updateApplication(APPLICATION_ID, updater);

        //then exception
    }

    @Test(expected = ApplicationNotFoundException.class)
    public void updateApplication_should_throw_ApplicationNotFoundException_when_applicationservice_throws_SObjectNotFoundException() throws Exception {
        //given
        final ApplicationUpdater updater = new ApplicationUpdater();
        updater.setToken("newToken");
        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        given(convertor.toApplicationUpdateDescriptor(updater, LOGGED_USER_ID)).willReturn(updateDescriptor);
        given(applicationService.updateApplication(APPLICATION_ID, updateDescriptor)).willThrow(new SObjectNotFoundException());

        //when
        delegate.updateApplication(APPLICATION_ID, updater);

        //then exception
    }

    @Test(expected = UpdateException.class)
    public void updateApplication_should_throw_UpdateException_when_applicationService_token_is_invalid() throws Exception {
        //given
        final ApplicationUpdater updater = new ApplicationUpdater();
        updater.setToken("token with spaces");

        //when
        delegate.updateApplication(APPLICATION_ID, updater);

        //then exception
    }

    @Test(expected = UpdateException.class)
    public void updateApplication_should_throw_UpdateException_when_applicationService_displayName_is_empty() throws Exception {
        //given
        final ApplicationUpdater updater = new ApplicationUpdater();
        updater.setDisplayName("");

        //when
        delegate.updateApplication(APPLICATION_ID, updater);

        //then exception
    }

    @Test(expected = UpdateException.class)
    public void updateApplication_should_throw_UpdateException_when_applicationService_displayName_is_null() throws Exception {
        //given
        final ApplicationUpdater updater = new ApplicationUpdater();
        updater.setDisplayName(null);

        //when
        delegate.updateApplication(APPLICATION_ID, updater);

        //then exception
    }

    @Test
    public void searchApplications_should_return_the_result_of_searchApplications_getResult() throws Exception {
        //given
        given(searchApplications.getResult()).willReturn(appSearchResult);

        //when
        final SearchResult<Application> retrievedSearchResult = delegate.searchApplications(searchApplications);

        //then
        assertThat(retrievedSearchResult).isEqualTo(appSearchResult);
        verify(searchApplications, times(1)).execute();
    }

    @Test(expected = SearchException.class)
    public void searchApplications_should_throw_SearchException_when_searchApplications_throws_SBonitaException() throws Exception {
        //given
        doThrow(new SBonitaReadException("")).when(searchApplications).execute();

        //when
        delegate.searchApplications(searchApplications);

        //then exception
    }

}
