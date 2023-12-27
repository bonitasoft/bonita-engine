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
package org.bonitasoft.console.common.server.page.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.page.Page;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class PageResourceProviderImplTest {

    public static final String PAGE_NAME = "pageName";
    public static final long PROCESS_DEFINITION_ID = 123L;
    public static final long PAGE_ID = 123L;

    @Mock
    private Page page;

    @Mock
    private PageAPI pageApi;

    PageResourceProviderImpl pageResourceProvider;

    PageResourceProviderImpl pageResourceProviderWithProcessDefinition;

    @Before
    public void before() throws Exception {
        doReturn(PROCESS_DEFINITION_ID).when(page).getProcessDefinitionId();
        doReturn(PAGE_NAME).when(page).getName();
        doReturn(PAGE_ID).when(page).getId();

        pageResourceProvider = new PageResourceProviderImpl(PAGE_NAME);
        pageResourceProviderWithProcessDefinition = new PageResourceProviderImpl(page);
    }

    @Test
    public void should_pagedirectory_be_distinct() throws Exception {
        assertThat(pageResourceProvider.getPageDirectory()).as("should be distinct")
                .isNotEqualTo(pageResourceProviderWithProcessDefinition.getPageDirectory());
    }

    @Test
    public void should_temp_page_file_be_distinct() throws Exception {
        assertThat(pageResourceProvider.getTempPageFile()).as("should be page name")
                .isNotEqualTo(pageResourceProviderWithProcessDefinition.getTempPageFile());
    }

    @Test
    public void should_testGetFullPageName_return_unique_key() throws Exception {
        assertThat(pageResourceProvider.getFullPageName()).as("should be page name").isEqualTo(PAGE_NAME);
        assertThat(pageResourceProvider.getFullPageName()).as("should be page name")
                .isNotEqualTo(pageResourceProviderWithProcessDefinition.getFullPageName());
    }

    @Test
    public void should_getPage_by_name() throws Exception {
        // when
        pageResourceProvider.getPage(pageApi);

        //then
        verify(pageApi).getPageByName(PAGE_NAME);
    }

    @Test
    public void should_getPage_by_id() throws Exception {
        // when
        pageResourceProviderWithProcessDefinition.getPage(pageApi);

        //then
        verify(pageApi, never()).getPageByName(anyString());
        verify(pageApi).getPage(PAGE_ID);

    }

}
