/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.bdm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.bpm.businessdata.impl.BusinessDataQueryMetadataImpl;
import org.bonitasoft.engine.bpm.businessdata.impl.BusinessDataQueryResultImpl;
import org.bonitasoft.engine.business.data.BusinessDataNotFoundException;
import org.bonitasoft.engine.business.data.BusinessDataRepositoryException;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.web.rest.server.api.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class BusinessDataControllerTest extends AbstractControllerTest<BusinessDataController> {

    @Mock
    protected CommandAPI commandAPI;

    @Override
    protected BusinessDataController createController() {
        return spy(new BusinessDataController());
    }

    @Override
    protected void configureMocks(BusinessDataController controller) throws Exception {
        doReturn(commandAPI).when(controller).getCommandAPI(apiSession);
    }

    @Test
    void should_return_the_business_data_based_on_its_id() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<>();
        parameters.put("entityClassName", "org.bonitasoft.pojo.Employee");
        parameters.put("businessDataId", 1983L);
        parameters.put("businessDataURIPattern", "/API/bdm/businessData/{className}/{id}/{field}");
        when(commandAPI.execute("getBusinessDataById", parameters)).thenReturn("{\"name\":\"たこ焼き\"}");

        mockMvc.perform(get("/API/bdm/businessData/org.bonitasoft.pojo.Employee/1983").sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())

                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content()
                        .json("{\"name\":\"たこ焼き\"}"));
    }

    @Test
    void should_get_return_a_not_found_error_status_when_command_is_not_found() throws Exception {
        doThrow(new CommandNotFoundException(null)).when(commandAPI).execute(anyString(), anyMap());

        mockMvc.perform(get("/API/bdm/businessData/org.bonitasoft.pojo.Employee/1983").sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_get_return_an_error_when_id_is_not_a_integer() throws Exception {
        mockMvc.perform(
                get("/API/bdm/businessData/org.bonitasoft.pojo.Employee/not_a_number").sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content()
                        .json("""
                                {"exception":"class java.lang.IllegalArgumentException","message":"[ not_a_number ] must be a number"}"""));
    }

    @Test
    void should_get_return_an_internal_server_error_status_when_command_is_not_well_parameterized()
            throws Exception {
        doThrow(new CommandParameterizationException("id is missing")).when(commandAPI).execute(anyString(), anyMap());

        mockMvc.perform(get("/API/bdm/businessData/org.bonitasoft.pojo.Employee/1983").sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void should_get_return_a_not_found_status_when_command_fails_business_data_not_found() throws Exception {
        doThrow(newCommandExecutionException(new BusinessDataNotFoundException(new RuntimeException("not found"))))
                .when(commandAPI).execute(anyString(),
                        anyMap());

        mockMvc.perform(get("/API/bdm/businessData/org.bonitasoft.pojo.Employee/1983").sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_get_return_an_internal_server_error_status_when_command_fails_during_execution() throws Exception {
        doThrow(newCommandExecutionException(new BusinessDataRepositoryException("repository error"))).when(commandAPI)
                .execute(anyString(), anyMap());

        mockMvc.perform(get("/API/bdm/businessData/org.bonitasoft.pojo.Employee/1983").sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content()
                        .json("""
                                {"exception":"class org.bonitasoft.engine.command.CommandExecutionException","message":"org.bonitasoft.engine.business.data.BusinessDataRepositoryException: repository error"}"""));
    }

    // =================================================================================================================
    // Get field
    // =================================================================================================================

    @Test
    void should_fetch_business_data_child_if_it_is_specified() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<>();
        parameters.put("entityClassName", "org.bonitasoft.pojo.Employee");
        parameters.put("businessDataId", 1983L);
        parameters.put("businessDataChildName", "child");
        parameters.put("businessDataURIPattern", "/API/bdm/businessData/{className}/{id}/{field}");
        when(commandAPI.execute("getBusinessDataById", parameters)).thenReturn("{\"child\":\"Leo\"}");

        mockMvc.perform(
                get("/API/bdm/businessData/org.bonitasoft.pojo.Employee/1983/child").sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content()
                        .json("""
                                {"child":"Leo"}"""));
    }

    @Test
    void should_fetch_business_data_child_return_an_error_when_id_is_not_a_integer() throws Exception {
        mockMvc.perform(
                get("/API/bdm/businessData/org.bonitasoft.pojo.Employee/wrong_id/child").sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content()
                        .json("""
                                {"exception":"class java.lang.IllegalArgumentException","message":"[ wrong_id ] must be a number"}"""));
    }

    // =================================================================================================================
    // Get list by ids
    // =================================================================================================================

    @Test
    void should_return_the_list_of_business_objects() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<>();
        parameters.put("entityClassName", "org.bonitasoft.pojo.Employee");
        parameters.put("businessDataIds", (Serializable) List.of(1983L, 547862L));
        parameters.put("businessDataURIPattern", BusinessDataFieldValue.URI_PATTERN);

        String jsonResponse = """
                    [
                        {"id": 1983, "name": "Matti"},
                        {"id": 547862, "name": "Fred"}
                    ]
                """;
        when(commandAPI.execute("getBusinessDataByIds", parameters)).thenReturn(jsonResponse);

        mockMvc.perform(get("/API/bdm/businessData/org.bonitasoft.pojo.Employee/findByIds?ids=1983,547862")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonResponse));
    }

    @Test
    public void should_return_the_list_throw_an_error_when_no_ids_are_passed() throws Exception {
        // when
        final var perform = mockMvc.perform(get("/API/bdm/businessData/org.bonitasoft.pojo.Employee/findByIds")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON));

        // then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content()
                        .json("""
                                {"exception":"class java.lang.IllegalArgumentException","message":"query parameter ids is mandatory"}"""));
    }

    @Test
    public void should_return_the_list_throw_an_error_when_ids_not_integer_are_passed() throws Exception {
        // when
        final var perform = mockMvc
                .perform(get("/API/bdm/businessData/org.bonitasoft.pojo.Employee/findByIds?ids=1983,abc")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON));

        // then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content()
                        .json("""
                                {"exception":"class java.lang.IllegalArgumentException","message":"Bad parameter ids=1983,abc"}"""));
    }

    @Test
    public void should_return_the_list_throw_not_found_when_CommandNotFoundException() throws Exception {
        // given
        when(commandAPI.execute(anyString(), anyMap())).thenThrow(new CommandNotFoundException(new RuntimeException("not found")));

        // when
        final var  perform = mockMvc.perform(get("/API/bdm/businessData/org.bonitasoft.pojo.Employee/findByIds?ids=1983,1984")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON));

        // then
        perform
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                                {"exception":"class org.bonitasoft.engine.command.CommandNotFoundException","message":"not found"}""")
                );

    }

    // =================================================================================================================
    // Get with custom query
    // =================================================================================================================

    @Test
    @SuppressWarnings("unchecked")
    void should_call_custom_query() throws Exception {
        String jsonResponse = """
                    [
                        {"id": 123, "name": "Harry"},
                        {"id": 124, "name": "Anna"},
                        {"id": 125, "name": "John"}
                    ]
                """;

        final Answer<Serializable> answer = invocation -> {
            assertThat(invocation.getArguments()).as("should have 2 parameters").hasSize(2);
            assertThat(invocation.getArguments()[0]).as("should call command")
                    .isEqualTo("getBusinessDataByQueryCommand");
            final Map<String, Serializable> parameters = (Map<String, Serializable>) invocation.getArguments()[1];
            assertThat(parameters).as("should have required  parameters").hasSize(6);
            assertThat(parameters).as("should compute start index").containsEntry("startIndex", 3 * 5);
            assertThat(parameters).containsEntry("maxResults", 5);
            assertThat(parameters).containsEntry("queryName", "findByName");
            assertThat(parameters).containsEntry("businessDataURIPattern", BusinessDataFieldValue.URI_PATTERN);
            assertThat(parameters).containsEntry("entityClassName", "org.bonitasoft.pojo.Employee");
            assertThat(parameters).containsKey("queryParameters");

            final Map<String, Serializable> queryParameters = (Map<String, Serializable>) parameters
                    .get("queryParameters");
            assertThat(queryParameters).as("should compute search filters").hasSize(2);
            assertThat(queryParameters).containsEntry("name", "John");
            assertThat(queryParameters).containsEntry("country", "US");

            return new BusinessDataQueryResultImpl(jsonResponse, new BusinessDataQueryMetadataImpl(1, 2, 4L));
        };
        when(commandAPI.execute(anyString(), anyMap())).then(answer);

        //then
        mockMvc.perform(
                get("/API/bdm/businessData/org.bonitasoft.pojo.Employee?q=findByName&c=5&p=3&f=name=John&f=country=US")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonResponse))
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "3-5/4"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_call_custom_query_single_multivalued_query_parameter() throws Exception {
        String jsonResponse = """
                    [
                        {"id": 123, "name": "Harry", "country": "US"},
                        {"id": 124, "name": "Anna", "country": "US"},
                        {"id": 129, "name": "Harry", "country": "Scotland"},
                        {"id": 130, "name": "Anna", "country": "Catalunya"},
                        {"id": 137, "name": "Anna", "country": "Quebec"},
                        {"id": 145, "name": "Anna", "country": "Corsica"}
                    ]
                """;

        final Answer<Serializable> answer = invocation -> {
            assertThat(invocation.getArguments()).as("should have 2 parameters").hasSize(2);
            assertThat(invocation.getArguments()[0]).as("should call command")
                    .isEqualTo("getBusinessDataByQueryCommand");
            final Map<String, Serializable> parameters = (Map<String, Serializable>) invocation.getArguments()[1];
            assertThat(parameters).as("should have required  parameters").hasSize(6);
            assertThat(parameters).as("should compute start index").containsEntry("startIndex", 2 * 6);
            assertThat(parameters).containsEntry("maxResults", 6);
            assertThat(parameters).containsEntry("queryName", "findByNames");
            assertThat(parameters).containsEntry("businessDataURIPattern", BusinessDataFieldValue.URI_PATTERN);
            assertThat(parameters).containsEntry("entityClassName", "org.bonitasoft.pojo.Employee");
            assertThat(parameters).containsKey("queryParameters");

            final Map<String, Serializable> queryParameters = (Map<String, Serializable>) parameters
                    .get("queryParameters");
            assertThat(queryParameters).as("should compute search filters").hasSize(1);
            assertThat(queryParameters).containsEntry("names", "Harry,Anna");

            return new BusinessDataQueryResultImpl(jsonResponse, new BusinessDataQueryMetadataImpl(1, 2, 26L));
        };
        when(commandAPI.execute(anyString(), anyMap())).then(answer);

        //then
        mockMvc.perform(
                get("/API/bdm/businessData/org.bonitasoft.pojo.Employee?q=findByNames&c=6&p=2&f=names=Harry,Anna")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonResponse))
                .andExpect(header().string("Content-Range", "2-6/26"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_call_custom_query_multiple_multivalued_query_parameters() throws Exception {
        String jsonResponse = """
                    [
                        {"id": 129, "name": "Harry", "country": "Scotland"},
                        {"id": 130, "name": "Anna", "country": "Catalunya"},
                        {"id": 137, "name": "Anna", "country": "Quebec"},
                        {"id": 145, "name": "Anna", "country": "Corsica"}
                    ]
                """;

        final Answer<Serializable> answer = invocation -> {
            assertThat(invocation.getArguments()).as("should have 2 parameters").hasSize(2);
            assertThat(invocation.getArguments()[0]).as("should call command")
                    .isEqualTo("getBusinessDataByQueryCommand");
            final Map<String, Serializable> parameters = (Map<String, Serializable>) invocation.getArguments()[1];
            assertThat(parameters).as("should have required  parameters").hasSize(6);
            assertThat(parameters).as("should compute start index").containsEntry("startIndex", 4 * 8);
            assertThat(parameters).containsEntry("maxResults", 8);
            assertThat(parameters).containsEntry("queryName", "findByNamesAndCountries");
            assertThat(parameters).containsEntry("businessDataURIPattern", BusinessDataFieldValue.URI_PATTERN);
            assertThat(parameters).containsEntry("entityClassName", "org.bonitasoft.pojo.Employee");
            assertThat(parameters).containsKey("queryParameters");

            final Map<String, Serializable> queryParameters = (Map<String, Serializable>) parameters
                    .get("queryParameters");
            assertThat(queryParameters).as("should compute search filters").hasSize(2);
            assertThat(queryParameters).containsEntry("names", "Harry,Anna");
            assertThat(queryParameters).containsEntry("countries", "Catalunya,Corsica,Scotland,Quebec");

            return new BusinessDataQueryResultImpl(jsonResponse, new BusinessDataQueryMetadataImpl(1, 2, 36L));
        };
        when(commandAPI.execute(anyString(), anyMap())).then(answer);

        //then
        mockMvc.perform(
                get("/API/bdm/businessData/org.bonitasoft.pojo.Employee?q=findByNamesAndCountries&c=8&p=4&f=names=Harry,Anna&f=countries=Catalunya,Corsica,Scotland,Quebec")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonResponse))
                .andExpect(header().string("Content-Range", "4-8/36"));
    }

    @Test
    void should_call_custom_query_throw_exception_when_missing_count() throws Exception {
        // when
        final var perform = mockMvc.perform(get("/API/bdm/businessData/org.bonitasoft.pojo.Employee?q=findByName&p=0")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON));

        // then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content()
                        .json("""
                                {"exception":"class java.lang.IllegalArgumentException","message":"query parameter c (count) is mandatory"}"""));
    }

    @Test
    void should_call_custom_query_throw_exception_when_count_conversion_fails() throws Exception {
        // when
        final var perform = mockMvc
                .perform(get("/API/bdm/businessData/org.bonitasoft.pojo.Employee?q=findByName&p=0&c=abc")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON));

        // then
        perform
                .andExpect(status().isBadRequest()) // Status.CLIENT_ERROR_NOT_FOUND
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content()
                        .json("""
                                {"exception":"class java.lang.IllegalArgumentException","message":"query parameter c (count) should be a number"}"""));
    }

    @Test
    public void should_call_custom_query_throw_exception_when_missing_page() throws Exception {
        // when
        final var perform = mockMvc.perform(get("/API/bdm/businessData/org.bonitasoft.pojo.Employee?q=findByName&c=0")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON));

        // then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content()
                        .json("""
                                {"exception":"class java.lang.IllegalArgumentException","message":"query parameter p (page) is mandatory"}"""));
    }

    @Test
    public void should_call_custom_query_throw_exception_when_page_conversion_fails() throws Exception {
        // when
        final var perform = mockMvc
                .perform(get("/API/bdm/businessData/org.bonitasoft.pojo.Employee?q=findByName&c=0&p=abc")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON));

        // then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content()
                        .json("""
                                {"exception":"class java.lang.IllegalArgumentException","message":"query parameter p (page) should be a number"}"""));
    }

    @Test
    void should_call_custom_query_throw_not_found_when_CommandNotFoundException() throws Exception {
        // given
        when(commandAPI.execute(anyString(), anyMap())).thenThrow(new CommandNotFoundException(new RuntimeException("Unable to read configuration file")));

        // when
        final var perform = mockMvc.perform(get("/API/bdm/businessData/org.bonitasoft.pojo.Employee?q=findByName&c=5&p=3")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON));

        // then
        perform
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                            {"exception":"class org.bonitasoft.engine.command.CommandNotFoundException","message":"Unable to read configuration file"}"""));
    }

    @Test
    public void should_call_custom_query_return_an_internal_server_error_status_when_command_fails_during_execution() throws Exception {
        // given
        when(commandAPI.execute(anyString(), anyMap())).thenThrow(newCommandExecutionException(new BusinessDataRepositoryException("repository error")));

        // when
        final var perform = mockMvc.perform(get("/API/bdm/businessData/org.bonitasoft.pojo.Employee?q=findByName&c=5&p=3")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON));

        // then
        perform
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                            {"exception":"class org.bonitasoft.engine.command.CommandExecutionException","message":"org.bonitasoft.engine.business.data.BusinessDataRepositoryException: repository error"}"""));
    }

    // wrap the root cause in the same way the command api does
    private static CommandExecutionException newCommandExecutionException(Exception cause) {
        return new CommandExecutionException(new SCommandExecutionException((cause)));
    }

}
