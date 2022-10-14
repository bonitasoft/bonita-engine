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
package org.bonitasoft.web.rest.server.datastore.bpm.process;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.console.common.server.page.CustomPageService;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.console.common.server.utils.PlatformManagementUtils;
import org.bonitasoft.console.common.server.utils.UnauthorizedFolderException;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.process.ProcessItem;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.rest.server.engineclient.ProcessEngineClient;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIForbiddenException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessDatastoreTest extends APITestWithMock {

    private ProcessDatastore processDatastore;

    @Mock
    private APISession engineSession;

    @Mock
    private ProcessEngineClient processEngineClient;

    @Mock
    private BonitaHomeFolderAccessor tenantFolder;

    @Mock
    private PageAPI pageAPI;

    @Mock
    private CustomPageService customPageService;

    @Mock
    private SearchResult<Page> searchResult;

    @Mock
    private PlatformManagementUtils platformManagementUtils;

    private final ProcessItem processItem = new ProcessItem();

    @Before
    public void setUp() throws Exception {
        processDatastore = spy(new ProcessDatastore(engineSession));
        doReturn(tenantFolder).when(processDatastore).getTenantFolder();
        doReturn(platformManagementUtils).when(processDatastore).getPlatformManagementUtils();
        doReturn(processEngineClient).when(processDatastore).getProcessEngineClient();
        doReturn(customPageService).when(processDatastore).getCustomPageService();
        doReturn(pageAPI).when(processDatastore).getPageAPI();
        doReturn(searchResult).when(pageAPI).searchPages(any(SearchOptions.class));
    }

    @Test(expected = APIForbiddenException.class)
    public void it_throws_an_exception_adding_a_bar_file_with_unauthorized_path() throws IOException {
        // Given
        doThrow(new UnauthorizedFolderException("")).when(tenantFolder).getTempFile(any(String.class));

        // When
        processDatastore.add(processItem);

    }

    @Test(expected = APIException.class)
    public void it_throws_an_exception_adding_a_bar_file_with_ioException() throws IOException {
        // Given
        doThrow(new IOException("")).when(tenantFolder).getTempFile(any(String.class));

        // When
        try {
            processDatastore.add(processItem);
        } catch (final APIForbiddenException e) {
            fail("Exception is not supposed to be an APIForbiddenException");
        }
    }

    @Test
    public void it_removes_the_pages_when_deleting_a_process() throws IOException {

        final Page page1 = mock(Page.class);
        doReturn("page1").when(page1).getName();
        final Page page2 = mock(Page.class);
        doReturn("page2").when(page2).getName();
        doReturn(Arrays.asList(page1, page2)).when(searchResult).getResult();
        doReturn(2L).when(searchResult).getCount();

        final APIID id = APIID.makeAPIID(2L);
        processDatastore.delete(List.of(id));

        verify(processDatastore).removeProcessPagesFromHome(id);
        verify(customPageService, times(1)).removePageLocally(page1);
        verify(customPageService, times(1)).removePageLocally(page2);
    }

    @Test
    public void it_removes_the_pages_when_deleting_a_process_with_pagination() throws IOException {

        final long nbOfPages = 130L;
        final Page page = mock(Page.class);
        doReturn("page").when(page).getName();
        final List<Page> pages = new ArrayList<>();
        for (int i = 0; i < nbOfPages; i++) {
            pages.add(page);
        }
        doReturn(pages).when(searchResult).getResult();
        doReturn(nbOfPages).when(searchResult).getCount();

        final APIID id = APIID.makeAPIID(2L);
        processDatastore.delete(List.of(id));

        verify(processDatastore).removeProcessPagesFromHome(id);
        verify(customPageService, times((int) nbOfPages)).removePageLocally(page);
    }
}
