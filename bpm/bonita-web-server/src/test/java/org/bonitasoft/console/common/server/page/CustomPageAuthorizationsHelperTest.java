/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationPage;
import org.bonitasoft.engine.business.application.ApplicationPageSearchDescriptor;
import org.bonitasoft.engine.page.ContentType;
import org.bonitasoft.engine.page.impl.PageImpl;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.impl.SearchFilter;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.livingapps.ApplicationModel;
import org.bonitasoft.livingapps.ApplicationModelFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CustomPageAuthorizationsHelperTest {

    @Mock
    ApplicationAPI applicationAPI;

    @Mock
    PageAPI pageAPI;

    @Mock
    ApplicationModelFactory applicationFactory;

    @InjectMocks
    CustomPageAuthorizationsHelper customPageAuthorizationsHelper;

    @Mock
    ApplicationModel applicationModel;

    @Mock
    Application application;

    @Mock
    SearchResult applicationResult;

    @Test
    public void should_authorize_page_when_appToken_not_null_and_page_authorized_in_application() throws Exception {
        given(pageAPI.getPageByName("pageToken"))
                .willReturn(new PageImpl(2L, "", "", false, "", 0, 0,
                        0, 0, "", ContentType.PAGE, null));
        given(applicationAPI.searchApplicationPages(any()))
                .willReturn(new SearchResultImpl<>(1, Collections.<ApplicationPage> emptyList()));
        given(applicationAPI.searchIApplications(any()))
                .willReturn(applicationResult);

        given(applicationResult.getResult()).willReturn(Arrays.asList(application));
        given(application.getId()).willReturn(1L);

        given(applicationFactory.createApplicationModel(any())).willReturn(applicationModel);
        when(applicationModel.authorize(any())).thenReturn(true);
        final boolean isPageAuthorized = customPageAuthorizationsHelper.isPageAuthorized("appToken", "pageToken");

        final ArgumentCaptor<SearchOptions> captor = ArgumentCaptor.forClass(SearchOptions.class);
        verify(applicationAPI).searchApplicationPages(captor.capture());

        SearchFilter filter = captor.getValue().getFilters().get(0);
        assertThat(filter.getField()).isEqualTo(ApplicationPageSearchDescriptor.APPLICATION_ID);
        assertThat(filter.getValue()).isEqualTo(1L);

        filter = captor.getValue().getFilters().get(1);
        assertThat(filter.getField()).isEqualTo(ApplicationPageSearchDescriptor.PAGE_ID);
        assertThat(filter.getValue()).isEqualTo(2L);

        assertThat(isPageAuthorized).isTrue();

        verify(applicationModel).authorize(any());
    }

    @Test
    public void should_unAuthorize_page_when_appToken_not_null_and_page_not_authorized_in_application()
            throws Exception {

        given(applicationAPI.searchIApplications(any()))
                .willReturn(applicationResult);
        given(applicationResult.getResult()).willReturn(Arrays.asList(application));
        given(applicationFactory.createApplicationModel(any(String.class))).willReturn(applicationModel);
        when(applicationModel.authorize(any())).thenReturn(false);
        final boolean isPageAuthorized = customPageAuthorizationsHelper.isPageAuthorized("appToken", "pageToken");

        assertThat(isPageAuthorized).isFalse();
        verify(applicationModel).authorize(any());
        verify(applicationAPI, never()).searchApplicationPages(any());
    }

    @Test
    public void should_not_authorize_page_when_appToken_not_null_and_page_unauthorized_in_application() {

        final boolean isPageAuthorized = customPageAuthorizationsHelper.isPageAuthorized("appToken", "pageToken");

        assertThat(isPageAuthorized).isFalse();
    }

    @Test
    public void should_not_authorize_page_when_appToken_is_null() {

        final boolean isPageAuthorized = customPageAuthorizationsHelper.isPageAuthorized("", "pageToken");

        assertThat(isPageAuthorized).isFalse();
    }

}
