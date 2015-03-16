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
package org.bonitasoft.engine.api.impl.transaction.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.api.impl.converter.ApplicationPageModelConverter;
import org.bonitasoft.engine.business.application.ApplicationPage;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SearchApplicationPagesTest {

    private static final int START_INDEX = 0;

    private static final int MAX_RESULTS = 10;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private ApplicationPageModelConverter convertor;

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
