package com.bonitasoft.engine.api.impl.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.api.impl.convertor.ApplicationMenuConvertor;
import com.bonitasoft.engine.api.impl.transaction.application.SearchApplicationMenus;
import com.bonitasoft.engine.api.impl.validator.ApplicationMenuCreatorValidator;
import com.bonitasoft.engine.business.application.ApplicationMenu;
import com.bonitasoft.engine.business.application.ApplicationMenuCreator;
import com.bonitasoft.engine.business.application.ApplicationMenuNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationMenuUpdater;
import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.impl.ApplicationMenuImpl;
import com.bonitasoft.engine.business.application.model.SApplicationMenu;
import com.bonitasoft.engine.business.application.model.impl.SApplicationMenuImpl;
import com.bonitasoft.engine.service.TenantServiceAccessor;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationMenuAPIDelegateTest {

    @Mock
    private TenantServiceAccessor accessor;

    @Mock
    private ApplicationMenuConvertor convertor;

    @Mock
    private ApplicationMenuCreatorValidator creatorValidator;

    @Mock
    private SearchApplicationMenus searchApplicatonMenus;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private SearchResult<ApplicationMenu> appMenuSearchResult;

    private ApplicationMenuAPIDelegate delegate;

    private static final long APPLICATION_ID = 34;

    private static final long APPLICATION_PAGE_ID = 35;

    @Before
    public void setUp() throws Exception {
        given(accessor.getApplicationService()).willReturn(applicationService);
        delegate = new ApplicationMenuAPIDelegate(accessor, convertor, searchApplicatonMenus, creatorValidator);
        given(creatorValidator.isValid(any(ApplicationMenuCreator.class))).willReturn(true);
    }

    @Test
    public void createApplicationMenu_with_no_parent_should_call_applicationService_createApplicationMenu_with_nexIndex_and_return_created_applicationMenu() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator(APPLICATION_ID, "Main", APPLICATION_PAGE_ID);
        final SApplicationMenuImpl sAppMenu = new SApplicationMenuImpl("Main", APPLICATION_ID, APPLICATION_PAGE_ID, 1);
        final ApplicationMenuImpl appMenu = new ApplicationMenuImpl("Main", APPLICATION_ID, APPLICATION_PAGE_ID, 1);
        given(applicationService.getNextAvailableIndex(null)).willReturn(5);
        given(convertor.buildSApplicationMenu(creator, 5)).willReturn(sAppMenu);
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
        final ApplicationMenuCreator creator = new ApplicationMenuCreator(APPLICATION_ID, "Main", APPLICATION_PAGE_ID);
        final SApplicationMenuImpl sAppMenu = new SApplicationMenuImpl("Main", APPLICATION_ID, APPLICATION_PAGE_ID, 1);
        given(applicationService.getNextAvailableIndex(null)).willReturn(1);
        given(convertor.buildSApplicationMenu(creator, 1)).willReturn(sAppMenu);
        given(applicationService.createApplicationMenu(sAppMenu)).willThrow(new SObjectCreationException());

        //when
        delegate.createApplicationMenu(creator);

        //then exception
    }

    @Test(expected = CreationException.class)
    public void createApplicationMenu_should_throw_CreationException_when_creator_is_not_valid() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator(APPLICATION_ID, "Main", APPLICATION_PAGE_ID);
        given(creatorValidator.isValid(creator)).willReturn(false);

        //when
        delegate.createApplicationMenu(creator);

        //then exception
    }

    @Test
    public void updateApplicationMenu_should_return_result_of_applicationService_updateApplicationMenu_converted_to_client_object() throws Exception {
        //given
        ApplicationMenuUpdater updater = mock(ApplicationMenuUpdater.class);
        EntityUpdateDescriptor updateDescriptor = mock(EntityUpdateDescriptor.class);
        SApplicationMenu sApplicationMenu = mock(SApplicationMenu.class);
        ApplicationMenu applicationMenu = mock(ApplicationMenu.class);

        given(convertor.toApplicationMenuUpdateDescriptor(updater)).willReturn(updateDescriptor);
        given(applicationService.updateApplicationMenu(4, updateDescriptor)).willReturn(sApplicationMenu);
        given(convertor.toApplicationMenu(sApplicationMenu)).willReturn(applicationMenu);

        //when
        ApplicationMenu updatedMenu = delegate.updateApplicationMenu(4, updater);

        //then
        assertThat(updatedMenu).isEqualTo(applicationMenu);

    }

    @Test(expected = ApplicationMenuNotFoundException.class)
    public void updateApplicationMenu_should_throw_ApplicationNotFoundException_when_applicationService_throws_SObjectNotFoundException() throws Exception {
        //given
        ApplicationMenuUpdater updater = mock(ApplicationMenuUpdater.class);
        EntityUpdateDescriptor updateDescriptor = mock(EntityUpdateDescriptor.class);

        given(convertor.toApplicationMenuUpdateDescriptor(updater)).willReturn(updateDescriptor);
        given(applicationService.updateApplicationMenu(4, updateDescriptor)).willThrow(new SObjectNotFoundException());

        //when
         delegate.updateApplicationMenu(4, updater);

        //then exception

    }

    @Test(expected = UpdateException.class)
    public void updateApplicationMenu_should_throw_UpdateException_when_applicationService_throws_SObjectModificationException() throws Exception {
        //given
        ApplicationMenuUpdater updater = mock(ApplicationMenuUpdater.class);
        EntityUpdateDescriptor updateDescriptor = mock(EntityUpdateDescriptor.class);

        given(convertor.toApplicationMenuUpdateDescriptor(updater)).willReturn(updateDescriptor);
        given(applicationService.updateApplicationMenu(4, updateDescriptor)).willThrow(new SObjectModificationException());

        //when
        delegate.updateApplicationMenu(4, updater);

        //then exception

    }

    @Test
    public void getApplicationMenu_should_return_result_of_applicationService() throws Exception {
        //given
        final SApplicationMenuImpl sAppMenu = new SApplicationMenuImpl("Main", APPLICATION_ID, APPLICATION_PAGE_ID, 1);
        final ApplicationMenuImpl appMenu = new ApplicationMenuImpl("Main", APPLICATION_ID, APPLICATION_PAGE_ID, 1);
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
