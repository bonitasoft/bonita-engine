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
package org.bonitasoft.web.rest.server.api.bpm.flownode;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstance;
import org.bonitasoft.engine.bpm.flownode.impl.internal.TimerEventTriggerInstanceImpl;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.web.rest.server.BonitaRestletApplication;
import org.bonitasoft.web.rest.server.utils.RestletTest;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;

@RunWith(MockitoJUnitRunner.class)
public class TimerEventTriggerResourceTest extends RestletTest {

    @Mock
    ProcessAPI processAPI;

    private TimerEventTriggerResource timerEventTriggerResource;

    @Before
    public void initializeMocks() {
        timerEventTriggerResource = spy(new TimerEventTriggerResource(processAPI));
    }

    @Override
    protected ServerResource configureResource() {
        return new TimerEventTriggerResource(processAPI);
    }

    @Test
    public void searchTimerEventTriggersShouldCallEngine() throws Exception {
        // given:
        final SearchOptions searchOptions = mock(SearchOptions.class);
        doReturn(1L).when(timerEventTriggerResource).getLongParameter(anyString(), anyBoolean());
        doReturn(1).when(timerEventTriggerResource).getIntegerParameter(anyString(), anyBoolean());
        Response mockResponse = mock(Response.class);
        doReturn(mockResponse).when(timerEventTriggerResource).getResponse();
        doReturn(mock(Representation.class)).when(mockResponse).getEntity();
        doReturn(searchOptions).when(timerEventTriggerResource).buildSearchOptions();
        SearchResult<TimerEventTriggerInstance> searchResult = mock(SearchResult.class);
        doReturn(Collections.emptyList()).when(searchResult).getResult();
        doReturn(1L).when(searchResult).getCount();
        doReturn(searchResult).when(processAPI).searchTimerEventTriggerInstances(anyLong(), eq(searchOptions));

        // when:
        timerEventTriggerResource.searchTimerEventTriggers();

        // then:
        verify(processAPI).searchTimerEventTriggerInstances(1L, searchOptions);
    }

    @Test(expected = APIException.class)
    public void searchTimerEventTriggersShouldThrowExceptionIfParameterNotFound() throws Exception {
        // given:
        doThrow(APIException.class).when(timerEventTriggerResource).getParameter(anyString(), anyBoolean());

        // when:
        timerEventTriggerResource.searchTimerEventTriggers();
    }

    @Test(expected = APIException.class)
    public void updateShouldHandleNullID() throws Exception {
        doReturn(Collections.EMPTY_MAP).when(timerEventTriggerResource).getRequestAttributes();

        timerEventTriggerResource.updateTimerEventTrigger(null);
    }

    @Test
    public void updateTimerEventTriggersShouldReturnStatusEngineReturnedDate() throws Exception {
        final long timerEventTriggerId = 1L;
        final Date date = new Date();
        final TimerEventTrigger timerEventTrigger = new TimerEventTrigger();
        timerEventTrigger.setExecutionDate(date.getTime());

        doReturn("" + timerEventTriggerId).when(timerEventTriggerResource)
                .getAttribute(TimerEventTriggerResource.ID_PARAM_NAME);
        doReturn(date).when(processAPI).updateExecutionDateOfTimerEventTriggerInstance(eq(timerEventTriggerId),
                any(Date.class));
        TimerEventTrigger eventTriggerActual = timerEventTriggerResource.updateTimerEventTrigger(timerEventTrigger);

        assertThat(eventTriggerActual.getExecutionDate()).isEqualTo(timerEventTrigger.getExecutionDate());
    }

    @Test
    public void should_TimerEventTrigger_return_number_as_strings() throws Exception {
        //given
        final SearchResult<TimerEventTriggerInstance> searchResult = createSearchResult();
        doReturn(searchResult).when(processAPI).searchTimerEventTriggerInstances(anyLong(), any(SearchOptions.class));

        //when
        final Response response = request(BonitaRestletApplication.BPM_TIMER_EVENT_TRIGGER_URL + "?caseId=2&p=0&c=10")
                .get();

        //then
        assertJsonEquals(getJson("timerEventTriggerInstances.json"), response.getEntityAsText());
    }

    @Test
    public void should_update_TimerEventTrigger_return_number_as_strings() throws Exception {
        //given
        doReturn(new Date(Long.MAX_VALUE)).when(processAPI).updateExecutionDateOfTimerEventTriggerInstance(anyLong(),
                any(Date.class));

        //when
        final Response response = request(BonitaRestletApplication.BPM_TIMER_EVENT_TRIGGER_URL + "/2")
                .put("{\"executionDate\": 9223372036854775807}");

        //then
        assertJsonEquals(getJson("timerEventTriggerInstance.json"), response.getEntityAsText());
    }

    private SearchResult<TimerEventTriggerInstance> createSearchResult() {
        final TimerEventTriggerInstance timerEventTriggerInstance1 = new TimerEventTriggerInstanceImpl(1L, 2L, "name",
                new Date(123L));
        final List<TimerEventTriggerInstance> instances = new ArrayList<>();
        instances.add(timerEventTriggerInstance1);

        return new SearchResultImpl<>(1L, instances);

    }
}
