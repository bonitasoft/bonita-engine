/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.api.impl.transaction.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.api.impl.converter.ApplicationConvertor;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SearchApplicationsTest {

    private static final int START_INDEX = 0;

    private static final int MAX_RESULTS = 10;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private SearchEntityDescriptor descriptor;

    @Mock
    private SearchOptions options;

    @Mock
    private ApplicationConvertor convertor;

    @InjectMocks
    private SearchApplications searchApplications;

    @Before
    public void setUp() throws Exception {
        when(options.getStartIndex()).thenReturn(START_INDEX);
        when(options.getMaxResults()).thenReturn(MAX_RESULTS);
    }

    @Test
    public void executeCount_should_return_result_of_applicationService_getNumberOfApplications() throws Exception {
        //given
        final QueryOptions options = new QueryOptions(START_INDEX, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS);
        given(applicationService.getNumberOfApplications(options)).willReturn(10L);

        //when
        final long count = searchApplications.executeCount(options);

        //then
        assertThat(count).isEqualTo(10L);
    }

    @Test(expected = SBonitaReadException.class)
    public void executeCount_should_throw_SBonitaSeachException_when_applicationService_throws_SBonitaReadException() throws Exception {
        //given
        final QueryOptions options = new QueryOptions(START_INDEX, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS);
        given(applicationService.getNumberOfApplications(options)).willThrow(new SBonitaReadException(""));

        //when
        searchApplications.executeCount(options);

        //then exception
    }

    @Test
    public void executeSearch_should_return_the_result_of_applicationService_searchApplications() throws Exception {
        //given
        final QueryOptions queryOptions = new QueryOptions(START_INDEX, MAX_RESULTS);
        final List<SApplication> applications = new ArrayList<SApplication>(1);
        applications.add(mock(SApplication.class));
        given(applicationService.searchApplications(queryOptions)).willReturn(applications);

        //when
        final List<SApplication> retrievedApplications = searchApplications.executeSearch(queryOptions);

        //then
        assertThat(retrievedApplications).isEqualTo(applications);
    }

    @Test
    public void convetToClientObjects_should_return_result_of_convertor_toApplication() throws Exception {
        //given
        final SApplication sApp = mock(SApplication.class);
        final Application app = mock(Application.class);
        final List<SApplication> sApplications = Arrays.asList(sApp);
        given(convertor.toApplication(sApplications)).willReturn(Arrays.asList(app));

        //when
        final List<Application> applications = searchApplications.convertToClientObjects(sApplications);

        //then
        assertThat(applications).containsExactly(app);
    }

}
