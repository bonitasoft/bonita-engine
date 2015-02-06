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
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.api.impl.converter.ApplicationPageModelConverter;
import org.bonitasoft.engine.api.impl.transaction.application.SearchApplicationPages;
import org.bonitasoft.engine.business.application.ApplicationPage;
import org.bonitasoft.engine.business.application.ApplicationPageNotFoundException;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.impl.ApplicationPageImpl;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.impl.SApplicationPageImpl;
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
public class ApplicationPageAPIDelegateTest {

    @Mock
    private TenantServiceAccessor accessor;

    @Mock
    private ApplicationPageModelConverter converter;

    @Mock
    private SearchApplicationPages searchApplicationPages;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private SearchResult<ApplicationPage> appPageSearchResult;

    private ApplicationPageAPIDelegate delegate;

    private static final long APPLICATION_ID = 15;

    private static final long APPLICATION_PAGE_ID = 35;

    private static final long PAGE_ID = 20;

    private static final String APP_NAME = "app";

    private static final String APP_PAGE_TOKEN = "firstPage";

    @Before
    public void setUp() throws Exception {
        given(accessor.getApplicationService()).willReturn(applicationService);
        delegate = new ApplicationPageAPIDelegate(accessor, converter, 9999L);
    }

    @Test
    public void setApplicationHomePage_should_call_applicationService_update_application_with_homePageId_key() throws Exception {
        //when
        delegate.setApplicationHomePage(APPLICATION_ID, APPLICATION_PAGE_ID);

        //then
        verify(applicationService, times(1)).updateApplication(eq(APPLICATION_ID), any(EntityUpdateDescriptor.class));
    }

    @Test(expected = UpdateException.class)
    public void setApplicationHomePage_should_throw_UpdateException_when_applicationService_throws_SObjectModificationException() throws Exception {
        //given
        given(applicationService.updateApplication(anyLong(), any(EntityUpdateDescriptor.class))).willThrow(new SObjectModificationException(""));

        //when
        delegate.setApplicationHomePage(APPLICATION_ID, APPLICATION_PAGE_ID);

        //then exception
    }

    @Test
    public void createApplicationPage_should_call_applicationService_createApplicationPage_and_return_created_applicationPage() throws Exception {
        //given
        final ApplicationPageImpl appPage = new ApplicationPageImpl(APPLICATION_ID, PAGE_ID, APP_PAGE_TOKEN);
        final SApplicationPageImpl sAppPage = new SApplicationPageImpl(APPLICATION_ID, PAGE_ID, APP_PAGE_TOKEN);
        given(converter.toApplicationPage(sAppPage)).willReturn(appPage);
        given(applicationService.createApplicationPage(sAppPage)).willReturn(sAppPage);

        //when
        final ApplicationPage createdAppPage = delegate.createApplicationPage(APPLICATION_ID, PAGE_ID, APP_PAGE_TOKEN);

        //then
        assertThat(createdAppPage).isEqualTo(appPage);
    }

    @Test
    public void createApplicationPage_should_update_application() throws Exception {
        //when
        delegate.createApplicationPage(APPLICATION_ID, PAGE_ID, APP_PAGE_TOKEN);

        //then
        verify(applicationService).updateApplication(eq(APPLICATION_ID), any(EntityUpdateDescriptor.class));
    }

    @Test
    public void deleteApplicationPage_should_update_application() throws Exception {
        // given
        final SApplicationPage appPage = mock(SApplicationPage.class);
        given(appPage.getApplicationId()).willReturn(APPLICATION_ID);
        given(applicationService.deleteApplicationPage(APPLICATION_PAGE_ID)).willReturn(appPage);

        //when
        delegate.deleteApplicationPage(APPLICATION_PAGE_ID);

        //then
        verify(applicationService).updateApplication(eq(APPLICATION_ID), any(EntityUpdateDescriptor.class));
    }

    @Test(expected = CreationException.class)
    public void createApplicationPage_should_throw_CreationException_when_applicationService_throws_SObjectCreationException() throws Exception {
        //given
        final SApplicationPageImpl sAppPage = new SApplicationPageImpl(APPLICATION_ID, PAGE_ID, APP_PAGE_TOKEN);
        given(applicationService.createApplicationPage(sAppPage)).willThrow(new SObjectCreationException());

        //when
        delegate.createApplicationPage(APPLICATION_ID, PAGE_ID, APP_PAGE_TOKEN);

        //then exception
    }

    @Test(expected = AlreadyExistsException.class)
    public void createApplicationPage_should_throw_AlreadyExistsException_when_applicationService_throws_SObjectAlreadyExistsException() throws Exception {
        //given
        final SApplicationPageImpl sAppPage = new SApplicationPageImpl(APPLICATION_ID, PAGE_ID, APP_PAGE_TOKEN);
        given(applicationService.createApplicationPage(sAppPage)).willThrow(new SObjectAlreadyExistsException());

        //when
        delegate.createApplicationPage(APPLICATION_ID, PAGE_ID, APP_PAGE_TOKEN);

        //then exception
    }

    @Test(expected = CreationException.class)
    public void createApplicationPage_should_throw_InvalidNameException_when_applicationService_throws_SInvalidNameException() throws Exception {
        //when
        delegate.createApplicationPage(APPLICATION_ID, PAGE_ID, "token with spaces");

        //then exception
    }

    @Test
    public void getApplicationPage_byNameAndAppName_should_return_the_result_of_applicationService_getApplicationPage() throws Exception {
        //given
        final ApplicationPageImpl appPage = new ApplicationPageImpl(APPLICATION_ID, PAGE_ID, APP_PAGE_TOKEN);
        final SApplicationPageImpl sAppPage = new SApplicationPageImpl(APPLICATION_ID, PAGE_ID, APP_PAGE_TOKEN);
        given(applicationService.getApplicationPage(APP_NAME, APP_PAGE_TOKEN)).willReturn(sAppPage);
        given(converter.toApplicationPage(sAppPage)).willReturn(appPage);

        //when
        final ApplicationPage retrievedAppPage = delegate.getApplicationPage(APP_NAME, APP_PAGE_TOKEN);

        //then
        assertThat(retrievedAppPage).isEqualTo(appPage);
    }

    @Test(expected = RetrieveException.class)
    public void getApplicationPage_byNameAndAppName_should_throw_RetrieveException_when_applicationService_throws_SBonitaReadException() throws Exception {
        //given
        given(applicationService.getApplicationPage(APP_NAME, APP_PAGE_TOKEN)).willThrow(new SBonitaReadException(""));

        //when
        delegate.getApplicationPage(APP_NAME, APP_PAGE_TOKEN);

        //then exception
    }

    @Test(expected = ApplicationPageNotFoundException.class)
    public void getApplicationPage_byNameAndAppName_should_throw_SObjectNotFoundException_when_applicationService_throws_SObjectNotFoundException()
            throws Exception {
        //given
        given(applicationService.getApplicationPage(APP_NAME, APP_PAGE_TOKEN)).willThrow(new SObjectNotFoundException());

        //when
        delegate.getApplicationPage(APP_NAME, APP_PAGE_TOKEN);

        //then exception
    }

    @Test
    public void getApplicationPage_byId_should_return_the_result_of_applicationService_getApplicationPage_byId() throws Exception {
        //given
        final ApplicationPageImpl appPage = new ApplicationPageImpl(APPLICATION_ID, PAGE_ID, APP_PAGE_TOKEN);
        final SApplicationPageImpl sAppPage = new SApplicationPageImpl(APPLICATION_ID, PAGE_ID, APP_PAGE_TOKEN);
        given(applicationService.getApplicationPage(APPLICATION_PAGE_ID)).willReturn(sAppPage);
        given(converter.toApplicationPage(sAppPage)).willReturn(appPage);

        //when
        final ApplicationPage retrievedAppPage = delegate.getApplicationPage(APPLICATION_PAGE_ID);

        //then
        assertThat(retrievedAppPage).isEqualTo(appPage);
    }

    @Test(expected = RetrieveException.class)
    public void getApplicationPage_byId_should_throw_RetrieveException_when_applicationService_throws_SBonitaReadException() throws Exception {
        //given
        given(applicationService.getApplicationPage(APPLICATION_PAGE_ID)).willThrow(new SBonitaReadException(""));

        //when
        delegate.getApplicationPage(APPLICATION_PAGE_ID);

        //then exception
    }

    @Test(expected = ApplicationPageNotFoundException.class)
    public void getApplicationPage_byId_should_throw_SObjectNotFoundException_when_applicationService_throws_SObjectNotFoundException()
            throws Exception {
        //given
        given(applicationService.getApplicationPage(APPLICATION_PAGE_ID)).willThrow(new SObjectNotFoundException());

        //when
        delegate.getApplicationPage(APPLICATION_PAGE_ID);

        //then exception
    }

    @Test
    public void deleteApplicationPage_should_call_applicationService_deleteApplicationPage() throws Exception {
        // given
        final SApplicationPage appPage = mock(SApplicationPage.class);
        given(appPage.getApplicationId()).willReturn(APPLICATION_ID);
        given(applicationService.deleteApplicationPage(APPLICATION_PAGE_ID)).willReturn(appPage);

        //when
        delegate.deleteApplicationPage(APPLICATION_PAGE_ID);

        //then
        verify(applicationService, times(1)).deleteApplicationPage(APPLICATION_PAGE_ID);
    }

    @Test(expected = DeletionException.class)
    public void deleteApplicationPage_should_throw_DeletionException_when_applicationService_throws_SObjectModificationException() throws Exception {
        doThrow(new SObjectModificationException()).when(applicationService).deleteApplicationPage(APPLICATION_PAGE_ID);

        //when
        delegate.deleteApplicationPage(APPLICATION_PAGE_ID);

        //then exception
    }

    @Test(expected = DeletionException.class)
    public void deleteApplicationPage_should_throw_DeletionException_when_applicationService_throws_SObjectNotFoundException() throws Exception {
        doThrow(new SObjectNotFoundException()).when(applicationService).deleteApplicationPage(APPLICATION_PAGE_ID);

        //when
        delegate.deleteApplicationPage(APPLICATION_PAGE_ID);

        //then exception
    }

    @Test
    public void getApplicationHomePage_should_return_the_result_of_applicationService_getApplicationHomePage() throws Exception {
        //given
        final ApplicationPageImpl appPage = new ApplicationPageImpl(APPLICATION_ID, PAGE_ID, APP_PAGE_TOKEN);
        final SApplicationPageImpl sAppPage = new SApplicationPageImpl(APPLICATION_ID, PAGE_ID, APP_PAGE_TOKEN);
        given(applicationService.getApplicationHomePage(APPLICATION_ID)).willReturn(sAppPage);
        given(converter.toApplicationPage(sAppPage)).willReturn(appPage);

        //when
        final ApplicationPage retrievedAppPage = delegate.getApplicationHomePage(APPLICATION_ID);

        //then
        assertThat(retrievedAppPage).isEqualTo(appPage);
    }

    @Test(expected = RetrieveException.class)
    public void getApplicationHomePage_should_throw_RetrieveException_when_applicationService_throws_SBonitaReadException() throws Exception {
        //given
        given(applicationService.getApplicationHomePage(APPLICATION_ID)).willThrow(new SBonitaReadException(""));

        //when
        delegate.getApplicationHomePage(APPLICATION_ID);

        //then exception
    }

    @Test(expected = ApplicationPageNotFoundException.class)
    public void getApplicationHomePage_should_throw_ApplicationPageNotFoundException_when_applicationService_throws_SObjectNotFoundException() throws Exception {
        //given
        given(applicationService.getApplicationHomePage(APPLICATION_ID)).willThrow(new SObjectNotFoundException(""));

        //when
        delegate.getApplicationHomePage(APPLICATION_ID);

        //then exception
    }

    @Test
    public void searchApplicationPages_should_return_the_result_of_searchApplicationPages_getResult() throws Exception {
        //given
        given(searchApplicationPages.getResult()).willReturn(appPageSearchResult);

        //when
        final SearchResult<ApplicationPage> retrievedSearchResult = delegate.searchApplicationPages(searchApplicationPages);

        //then
        assertThat(retrievedSearchResult).isEqualTo(appPageSearchResult);
        verify(searchApplicationPages, times(1)).execute();
    }

    @Test(expected = SearchException.class)
    public void searchApplicationPages_should_throw_SearchException_when_searchApplicationPages_throws_SBonitaException() throws Exception {
        //given
        doThrow(new SBonitaReadException("")).when(searchApplicationPages).execute();

        //when
        delegate.searchApplicationPages(searchApplicationPages);

        //then exception
    }

}
