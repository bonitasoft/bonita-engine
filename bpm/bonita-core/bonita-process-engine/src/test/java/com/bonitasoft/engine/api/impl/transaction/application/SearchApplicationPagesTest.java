/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.api.impl.convertor.ApplicationPageConvertor;
import com.bonitasoft.engine.business.application.ApplicationPage;
import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.model.SApplicationPage;

@RunWith(MockitoJUnitRunner.class)
public class SearchApplicationPagesTest {

    private static final int START_INDEX = 0;

    private static final int MAX_RESULTS = 10;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private ApplicationPageConvertor convertor;

    @InjectMocks
    private SearchApplicationPages search;

    @Test
    public void executeCount_should_return_the_result_of_applicationService_getNumberOfApplicationPages() throws Exception {
        //given
        final QueryOptions options = new QueryOptions(START_INDEX, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS);
        given(applicationService.getNumberOfApplicationPages(options)).willReturn(8L);

        //when
        final long count = search.executeCount(options);

        //then
        assertThat(count).isEqualTo(8L);
    }

    @Test(expected = SBonitaReadException.class)
    public void executeCount_should_throw_SBonitaSeachException_when_applicationService_throws_SBonitaReadException() throws Exception {
        //given
        final QueryOptions options = new QueryOptions(START_INDEX, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS);
        given(applicationService.getNumberOfApplicationPages(options)).willThrow(new SBonitaReadException(""));

        //when
        search.executeCount(options);

        //then exception
    }

    @Test
    public void executeSearch_should_return_the_result_of_applicationService_searchApplicationPages() throws Exception {
        //given
        final QueryOptions queryOptions = new QueryOptions(START_INDEX, MAX_RESULTS);
        final List<SApplicationPage> appPages = new ArrayList<SApplicationPage>(1);
        appPages.add(mock(SApplicationPage.class));
        given(applicationService.searchApplicationPages(queryOptions)).willReturn(appPages);

        //when
        final List<SApplicationPage> retrievedAppPages = search.executeSearch(queryOptions);

        //then
        assertThat(retrievedAppPages).isEqualTo(appPages);
    }

    @Test
    public void convetToClientObjects_should_return_result_of_convertor_toApplicationPage() throws Exception {
        //given
        final SApplicationPage sAppPage = mock(SApplicationPage.class);
        final ApplicationPage appPage = mock(ApplicationPage.class);
        final List<SApplicationPage> sApplicationPages = Arrays.asList(sAppPage);
        given(convertor.toApplicationPage(sApplicationPages)).willReturn(Arrays.asList(appPage));

        //when
        final List<ApplicationPage> applicationPages = search.convertToClientObjects(sApplicationPages);

        //then
        assertThat(applicationPages).containsExactly(appPage);
    }

}
