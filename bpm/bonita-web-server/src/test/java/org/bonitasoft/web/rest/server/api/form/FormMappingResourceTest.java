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
package org.bonitasoft.web.rest.server.api.form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.form.FormMapping;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.web.rest.server.utils.RestletTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;

@RunWith(MockitoJUnitRunner.class)
public class FormMappingResourceTest extends RestletTest {

    @Mock
    protected ProcessAPI processAPI;

    @Override
    protected ServerResource configureResource() {
        return new FormMappingResource(processAPI);
    }

    @Test
    public void searchFormMappingShouldReturnContentRange() throws Exception {

        final SearchResult<FormMapping> searchResult = mock(SearchResult.class);
        final FormMapping formMapping = mock(FormMapping.class);
        final List<FormMapping> formMappings = new ArrayList<>();
        formMappings.add(formMapping);
        doReturn(formMappings).when(searchResult).getResult();
        doReturn(1L).when(searchResult).getCount();
        doReturn(searchResult).when(processAPI).searchFormMappings(any(SearchOptions.class));

        final Response response = request("/form/mapping?p=2&c=10&f=type=TASK").get();

        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS_OK);
        assertThat(response.getHeaders().getFirstValue("Content-range")).isEqualTo("2-10/1");
    }

    @Test
    public void searchFormMappingShouldReturnMapping() throws Exception {

        final SearchResult<FormMapping> searchResult = mock(SearchResult.class);
        final FormMapping formMapping = new FormMapping();
        formMapping.setTask("myTask");
        final List<FormMapping> formMappings = new ArrayList<>();
        formMappings.add(formMapping);
        doReturn(formMappings).when(searchResult).getResult();
        doReturn(1L).when(searchResult).getCount();
        doReturn(searchResult).when(processAPI).searchFormMappings(any(SearchOptions.class));

        final Response response = request("/form/mapping?p=0&c=10").get();

        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS_OK);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        response.getEntity().write(outputStream);
        final String content = outputStream.toString();
        outputStream.close();
        assertThat(content).isNotNull();
        assertThat(content).contains("\"task\":\"myTask\"");
        verify(processAPI).searchFormMappings(any(SearchOptions.class));
    }

    @Test
    public void searchFormMappingShouldReturnLongIdAsString() throws Exception {

        final SearchResult<FormMapping> searchResult = mock(SearchResult.class);
        final FormMapping formMapping = new FormMapping();
        formMapping.setId(11125555888888L);
        formMapping.setTask("myTask");
        formMapping.setProcessDefinitionId(4871148324840256385L);
        formMapping.setPageId(1L);
        formMapping.setLastUpdatedBy(1L);
        final List<FormMapping> formMappings = new ArrayList<>();
        formMappings.add(formMapping);
        doReturn(formMappings).when(searchResult).getResult();
        doReturn(1L).when(searchResult).getCount();
        doReturn(searchResult).when(processAPI).searchFormMappings(any(SearchOptions.class));

        final Response response = request("/form/mapping?p=0&c=10").get();

        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS_OK);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        response.getEntity().write(outputStream);
        final String content = outputStream.toString();
        outputStream.close();
        assertThat(content).isNotNull();
        assertThat(content).contains("\"id\":\"11125555888888\"");
        assertThat(content).contains("\"processDefinitionId\":\"4871148324840256385\"");
        assertThat(content).contains("\"pageId\":\"1\"");
        assertThat(content).contains("\"lastUpdatedBy\":\"1\"");
        verify(processAPI).searchFormMappings(any(SearchOptions.class));
    }

}
