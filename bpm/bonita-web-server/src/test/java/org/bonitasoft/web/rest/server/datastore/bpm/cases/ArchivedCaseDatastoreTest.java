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
package org.bonitasoft.web.rest.server.datastore.bpm.cases;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCaseItem;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ArchivedCaseDatastoreTest extends APITestWithMock {

    private ArchivedCaseDatastore datastore;

    @Mock
    private ProcessAPI processAPI;

    @Mock
    private ArchivedProcessInstance archivedProcessInstance1;
    @Mock
    private ArchivedProcessInstance archivedProcessInstance2;
    @Mock
    private ArchivedProcessInstance archivedProcessInstance3;

    private long archivedProcessInstanceId1;

    private long archivedProcessInstanceId2;

    private long archivedProcessInstanceId3;

    private long sourceProcessInstanceId1;

    private long sourceProcessInstanceId2;

    private long sourceProcessInstanceId3;

    @Before
    public void initializeMocks() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException,
            ArchivedProcessInstanceNotFoundException {
        initMocks(this);
        datastore = spy(new ArchivedCaseDatastore(null));
        doReturn(processAPI).when(datastore).getProcessApi();

        archivedProcessInstanceId1 = 1L;
        archivedProcessInstanceId2 = 2L;
        archivedProcessInstanceId3 = 3L;
        sourceProcessInstanceId1 = 11L;
        sourceProcessInstanceId2 = 12L;
        sourceProcessInstanceId3 = 13L;

        doReturn(sourceProcessInstanceId1).when(archivedProcessInstance1).getSourceObjectId();
        doReturn(sourceProcessInstanceId2).when(archivedProcessInstance2).getSourceObjectId();
        doReturn(sourceProcessInstanceId3).when(archivedProcessInstance3).getSourceObjectId();

        doReturn(archivedProcessInstance1).when(processAPI).getArchivedProcessInstance(archivedProcessInstanceId1);
        doReturn(archivedProcessInstance2).when(processAPI).getArchivedProcessInstance(archivedProcessInstanceId2);
        doReturn(archivedProcessInstance3).when(processAPI).getArchivedProcessInstance(archivedProcessInstanceId3);
    }

    @Test
    public void search_should_pass_long_parameters_as_long_to_engine_and_not_as_string() {
        // given:
        doReturn(mock(SearchResult.class)).when(datastore).runSearch(any(), any());

        // when:
        datastore.search(0, 10, null, null, emptyMap());

        // then:
        verify(datastore).addLongFilterToSearchBuilder(any(), any(), anyString(),
                eq(ProcessInstanceSearchDescriptor.PROCESS_DEFINITION_ID));
        verify(datastore).addLongFilterToSearchBuilder(any(), any(), anyString(),
                eq(ProcessInstanceSearchDescriptor.STARTED_BY));
        verify(datastore).addLongFilterToSearchBuilder(any(), any(), anyString(),
                eq(ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID));
    }

    @Test
    public void should_delete_archive_case_call_right_engine_method()
            throws DeletionException, ArchivedProcessInstanceNotFoundException {
        //given

        final List<APIID> idList = Arrays.asList(APIID.makeAPIID(archivedProcessInstanceId1),
                APIID.makeAPIID(archivedProcessInstanceId2),
                APIID.makeAPIID(archivedProcessInstanceId3));

        //when
        datastore.delete(idList);

        //then
        verify(processAPI, times(3)).getArchivedProcessInstance(anyLong());
        verify(processAPI).deleteArchivedProcessInstancesInAllStates(
                Arrays.asList(
                        sourceProcessInstanceId1,
                        sourceProcessInstanceId2,
                        sourceProcessInstanceId3));
    }

    @Test
    public void should_delete_all_archived_cases_when_one_id_is_given() throws DeletionException,
            ArchivedProcessInstanceNotFoundException {
        //given
        final List<APIID> idList = Collections.singletonList(APIID.makeAPIID(archivedProcessInstanceId1));

        //when
        datastore.delete(idList);

        //then
        verify(processAPI).getArchivedProcessInstance(archivedProcessInstanceId1);
        verify(processAPI, times(1)).getArchivedProcessInstance(archivedProcessInstanceId1);
        verify(processAPI)
                .deleteArchivedProcessInstancesInAllStates(Collections.singletonList(sourceProcessInstanceId1));
    }

    @Test(expected = APIException.class)
    public void should_throw_an_api_exception_when_deletion_exception_is_rised() throws DeletionException {
        //given
        doThrow(new DeletionException("exception!")).when(processAPI)
                .deleteArchivedProcessInstancesInAllStates(anyList());
        final List<APIID> idList = Collections.singletonList(APIID.makeAPIID(archivedProcessInstanceId1));

        //when
        datastore.delete(idList);

    }

    @Test
    public void testConvertEngineToConsoleItem() {
        //given
        ArchivedProcessInstance archivedProcessInstance = mock(ArchivedProcessInstance.class);
        doReturn("labelOne").when(archivedProcessInstance).getStringIndexLabel(1);
        doReturn("labelTwo").when(archivedProcessInstance).getStringIndexLabel(2);
        doReturn("labelThree").when(archivedProcessInstance).getStringIndexLabel(3);
        doReturn("labelFour").when(archivedProcessInstance).getStringIndexLabel(4);
        doReturn("labelFive").when(archivedProcessInstance).getStringIndexLabel(5);
        doReturn("valueOne").when(archivedProcessInstance).getStringIndexValue(1);
        doReturn("valueTwo").when(archivedProcessInstance).getStringIndexValue(2);
        doReturn("valueThree").when(archivedProcessInstance).getStringIndexValue(3);
        doReturn("valueFour").when(archivedProcessInstance).getStringIndexValue(4);
        doReturn("valueFive").when(archivedProcessInstance).getStringIndexValue(5);

        // when
        final ArchivedCaseItem archivedCaseItem = datastore.convertEngineToConsoleItem(archivedProcessInstance);

        // then
        //check labels
        assertThat(archivedCaseItem.getSearchIndex1Label()).isEqualTo("labelOne");
        assertThat(archivedCaseItem.getSearchIndex2Label()).isEqualTo("labelTwo");
        assertThat(archivedCaseItem.getSearchIndex3Label()).isEqualTo("labelThree");
        assertThat(archivedCaseItem.getSearchIndex4Label()).isEqualTo("labelFour");
        assertThat(archivedCaseItem.getSearchIndex5Label()).isEqualTo("labelFive");
        //check values
        assertThat(archivedCaseItem.getSearchIndex1Value()).isEqualTo("valueOne");
        assertThat(archivedCaseItem.getSearchIndex2Value()).isEqualTo("valueTwo");
        assertThat(archivedCaseItem.getSearchIndex3Value()).isEqualTo("valueThree");
        assertThat(archivedCaseItem.getSearchIndex4Value()).isEqualTo("valueFour");
        assertThat(archivedCaseItem.getSearchIndex5Value()).isEqualTo("valueFive");
    }

    @Test
    public void testConvertEngineToConsoleItemWithNullValues() {
        //given
        ArchivedProcessInstance archivedProcessInstance = mock(ArchivedProcessInstance.class);
        doReturn(null).when(archivedProcessInstance).getStringIndexLabel(1);
        doReturn(null).when(archivedProcessInstance).getStringIndexValue(1);

        // when
        final ArchivedCaseItem archivedCaseItem = datastore.convertEngineToConsoleItem(archivedProcessInstance);

        // then
        //check labels
        assertThat(archivedCaseItem.getSearchIndex1Label()).isNull();
        assertThat(archivedCaseItem.getSearchIndex1Value()).isNull();

    }

}
