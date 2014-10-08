package com.bonitasoft.engine.api.impl.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
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

import com.bonitasoft.engine.api.impl.convertor.ApplicationMenuConvertor;
import com.bonitasoft.engine.api.impl.transaction.application.SearchApplicationMenus;
import com.bonitasoft.engine.business.application.ApplicationMenu;
import com.bonitasoft.engine.business.application.ApplicationMenuCreator;
import com.bonitasoft.engine.business.application.ApplicationMenuNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.impl.ApplicationMenuImpl;
import com.bonitasoft.engine.business.application.model.impl.SApplicationMenuImpl;
import com.bonitasoft.engine.service.TenantServiceAccessor;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationMenuAPIDelegateTest {

    @Mock
    private TenantServiceAccessor accessor;

    @Mock
    private ApplicationMenuConvertor convertor;

    @Mock
    private SearchApplicationMenus searchApplicatonMenus;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private SearchResult<ApplicationMenu> appMenuSearchResult;

    private ApplicationMenuAPIDelegate delegate;

    private static final long APPLICATION_PAGE_ID = 35;

    @Before
    public void setUp() throws Exception {
        given(accessor.getApplicationService()).willReturn(applicationService);
        delegate = new ApplicationMenuAPIDelegate(accessor, convertor, searchApplicatonMenus);
    }

    @Test
    public void createApplicationMenu_should_call_applicationService_createApplicationMenu_and_return_created_applicationMenu() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator("Main", APPLICATION_PAGE_ID, 1);
        final SApplicationMenuImpl sAppMenu = new SApplicationMenuImpl("Main", APPLICATION_PAGE_ID, 1);
        final ApplicationMenuImpl appMenu = new ApplicationMenuImpl("Main", APPLICATION_PAGE_ID, 1);
        given(convertor.buildSApplicationMenu(creator)).willReturn(sAppMenu);
        given(convertor.toApplicationMenu(sAppMenu)).willReturn(appMenu);
        given(applicationService.createApplicationMenu(sAppMenu)).willReturn(sAppMenu);

        //when
        final ApplicationMenu createdAppMenu = delegate.createApplicationMenu(creator);

        //then
        assertThat(createdAppMenu).isNotNull();
        assertThat(createdAppMenu).isEqualTo(appMenu);
    }

    @Test(expected = CreationException.class)
    public void createApplicationMenu_should_throw_CreationException_when_applicationService_throws_SObjectCreationException() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator("Main", APPLICATION_PAGE_ID, 1);
        final SApplicationMenuImpl sAppMenu = new SApplicationMenuImpl("Main", APPLICATION_PAGE_ID, 1);
        given(convertor.buildSApplicationMenu(creator)).willReturn(sAppMenu);
        given(applicationService.createApplicationMenu(sAppMenu)).willThrow(new SObjectCreationException());

        //when
        delegate.createApplicationMenu(creator);

        //then exception
    }

    @Test
    public void getApplicationMenu_should_return_result_of_applicationService() throws Exception {
        //given
        final SApplicationMenuImpl sAppMenu = new SApplicationMenuImpl("Main", APPLICATION_PAGE_ID, 1);
        final ApplicationMenuImpl appMenu = new ApplicationMenuImpl("Main", APPLICATION_PAGE_ID, 1);
        given(applicationService.getApplicationMenu(10)).willReturn(sAppMenu);
        given(convertor.toApplicationMenu(sAppMenu)).willReturn(appMenu);

        //when
        final ApplicationMenu retrievedMenu = delegate.getApplicationMenu(10);

        //then
        assertThat(retrievedMenu).isNotNull();
        assertThat(retrievedMenu).isEqualTo(appMenu);
    }

    @Test(expected = ApplicationMenuNotFoundException.class)
    public void getApplicationMenu_should_throw_ApplicationMenuNotFoundException_when_applicationService_throws_SObjectNotFoundException() throws Exception {
        //given
        given(applicationService.getApplicationMenu(10)).willThrow(new SObjectNotFoundException());

        //when
        delegate.getApplicationMenu(10);

        //then exception
    }

    @Test(expected = RetrieveException.class)
    public void getApplicationMenu_should_throw_RetriveException_when_applicationService_throws_SBonitaReadException() throws Exception {
        //given
        given(applicationService.getApplicationMenu(10)).willThrow(new SBonitaReadException(""));

        //when
        delegate.getApplicationMenu(10);

        //then exception
    }

    @Test
    public void deleteApplicationMenu_should_call_applicationService_deleteApplicationMenu() throws Exception {
        //when
        delegate.deleteApplicationMenu(15);

        //then
        verify(applicationService, times(1)).deleteApplicationMenu(15);
    }

    @Test(expected = DeletionException.class)
    public void deleteApplicationMenu_should_throw_DeletionException_when_applicationService_throws_SObjectModificationException() throws Exception {
        //given
        doThrow(new SObjectModificationException()).when(applicationService).deleteApplicationMenu(15);

        //when
        delegate.deleteApplicationMenu(15);

        //then exception
    }

    @Test(expected = DeletionException.class)
    public void deleteApplicationMenu_should_throw_DeletionException_when_applicationService_throws_SObjectNotFoundException() throws Exception {
        //given
        doThrow(new SObjectNotFoundException()).when(applicationService).deleteApplicationMenu(15);

        //when
        delegate.deleteApplicationMenu(15);

        //then exception
    }

    @Test
    public void searchApplicationMenus_should_return_result_of_searchApplicationMenus_getResult() throws Exception {
        //given
        given(searchApplicatonMenus.getResult()).willReturn(appMenuSearchResult);

        //when
        final SearchResult<ApplicationMenu> retrievedResult = delegate.searchApplicationMenus();

        //then
        assertThat(retrievedResult).isEqualTo(appMenuSearchResult);
        verify(searchApplicatonMenus, times(1)).execute();
    }

    @Test(expected = SearchException.class)
    public void searchApplicationMenus_should_throw_SearchException_when_searchApplicationMenus_execute_throws_exception() throws Exception {
        //given
        doThrow(new SBonitaReadException("")).when(searchApplicatonMenus).execute();

        //when
        delegate.searchApplicationMenus();

        //then exception
    }

}
