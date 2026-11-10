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

    private static final String FAKE_CLASS_NAME = "org.bonitasoft.pojo.Employee";
    private static final long FAKE_ID = 1983L;
    private static final String FAKE_EXCEPTION_MESSAGE = "fake exception message";

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
        parameters.put("entityClassName", FAKE_CLASS_NAME);
        parameters.put("businessDataId", FAKE_ID);
        parameters.put("businessDataURIPattern", "/API/bdm/businessData/{className}/{id}/{field}");
        when(commandAPI.execute("getBusinessDataById", parameters)).thenReturn("{\"name\":\"たこ焼き\"}");

        //when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}/{id}", FAKE_CLASS_NAME, FAKE_ID)
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                //then
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"name":"たこ焼き"}
                        """, true));
    }

    @Test
    void should_get_return_a_not_found_error_status_when_command_is_not_found() throws Exception {
        doThrow(new CommandNotFoundException(null)).when(commandAPI).execute(anyString(), anyMap());

        //when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}/{id}", FAKE_CLASS_NAME, FAKE_ID)
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                //then
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(CommandNotFoundException.class.toString()));
    }

    @Test
    void should_get_return_an_error_when_id_is_not_a_integer() throws Exception {
        var id = "not_a_number";
        //when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}/{id}", FAKE_CLASS_NAME, id)
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                //then
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(IllegalArgumentException.class.toString()))
                .andExpect(jsonPath("$.message").value("[ %s ] must be a number".formatted(id)));
    }

    @Test
    void should_get_return_an_internal_server_error_status_when_command_is_not_well_parameterized()
            throws Exception {
        doThrow(new CommandParameterizationException(FAKE_EXCEPTION_MESSAGE))
                .when(commandAPI).execute(anyString(), anyMap());

        //when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}/{id}", FAKE_CLASS_NAME, FAKE_ID)
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                //then
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(CommandParameterizationException.class.toString()))
                .andExpect(jsonPath("$.message").value(FAKE_EXCEPTION_MESSAGE));
    }

    @Test
    void should_get_return_a_not_found_status_when_command_fails_business_data_not_found() throws Exception {
        doThrow(newCommandExecutionException(
                new BusinessDataNotFoundException(new RuntimeException(FAKE_EXCEPTION_MESSAGE))))
                .when(commandAPI).execute(anyString(), anyMap());

        //when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}/{id}", FAKE_CLASS_NAME, FAKE_ID)
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                //then
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(BusinessDataNotFoundException.class.toString()))
                .andExpect(jsonPath("$.message").value(FAKE_EXCEPTION_MESSAGE));
    }

    @Test
    void should_get_return_an_internal_server_error_status_when_command_fails_during_execution() throws Exception {
        var exception = new BusinessDataRepositoryException(FAKE_EXCEPTION_MESSAGE);
        doThrow(newCommandExecutionException(exception))
                .when(commandAPI).execute(anyString(), anyMap());

        //when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}/{id}", FAKE_CLASS_NAME, FAKE_ID)
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                //then
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(CommandExecutionException.class.toString()))
                .andExpect(jsonPath("$.message").value(exception.toString()));
    }

    // =================================================================================================================
    // Get field
    // =================================================================================================================

    @Test
    void should_fetch_business_data_child_if_it_is_specified() throws Exception {
        var fieldName = "child";
        final Map<String, Serializable> parameters = new HashMap<>();
        parameters.put("entityClassName", FAKE_CLASS_NAME);
        parameters.put("businessDataId", FAKE_ID);
        parameters.put("businessDataChildName", fieldName);
        parameters.put("businessDataURIPattern", "/API/bdm/businessData/{className}/{id}/{field}");
        when(commandAPI.execute("getBusinessDataById", parameters)).thenReturn("{\"child\":\"Leo\"}");

        //when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}/{id}/{fieldName}", FAKE_CLASS_NAME, FAKE_ID, fieldName)
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                //then
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"child":"Leo"}
                        """, true));
    }

    @Test
    void should_fetch_business_data_child_return_an_error_when_id_is_not_a_integer() throws Exception {
        var id = "wrong_id";
        //when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}/{id}/{fieldName}", FAKE_CLASS_NAME, id, "child")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                //then
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(IllegalArgumentException.class.toString()))
                .andExpect(jsonPath("$.message").value("[ %s ] must be a number".formatted(id)));
    }

    // =================================================================================================================
    // Get list by ids
    // =================================================================================================================

    @Test
    void should_return_the_list_of_business_objects() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<>();
        parameters.put("entityClassName", FAKE_CLASS_NAME);
        parameters.put("businessDataIds", (Serializable) List.of(1983L, 547862L));
        parameters.put("businessDataURIPattern", BusinessDataFieldValue.URI_PATTERN);

        String jsonResponse = """
                    [
                        {"id": 1983, "name": "Matti"},
                        {"id": 547862, "name": "Fred"}
                    ]
                """;
        when(commandAPI.execute("getBusinessDataByIds", parameters)).thenReturn(jsonResponse);

        //when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}/findByIds", FAKE_CLASS_NAME)
                        .param("ids", "1983,547862")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                //then
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonResponse, true));
    }

    @Test
    public void should_return_the_list_throw_an_error_when_no_ids_are_passed() throws Exception {
        // when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}/findByIds", FAKE_CLASS_NAME)
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                // then
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(IllegalArgumentException.class.toString()))
                .andExpect(jsonPath("$.message").value("query parameter ids is mandatory"));
    }

    @Test
    public void should_return_the_list_throw_an_error_when_ids_not_integer_are_passed() throws Exception {
        var ids = "1983,abc";
        // when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}/findByIds", FAKE_CLASS_NAME)
                        .param("ids", ids)
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                // then
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(IllegalArgumentException.class.toString()))
                .andExpect(jsonPath("$.message").value("Bad parameter ids=" + ids));
    }

    @Test
    public void should_return_the_list_throw_not_found_when_CommandNotFoundException() throws Exception {
        // given
        when(commandAPI.execute(anyString(), anyMap()))
                .thenThrow(new CommandNotFoundException(new RuntimeException(FAKE_EXCEPTION_MESSAGE)));

        // when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}/findByIds", FAKE_CLASS_NAME)
                        .param("ids", "1983,1984")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                // then
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(CommandNotFoundException.class.toString()))
                .andExpect(jsonPath("$.message").value(FAKE_EXCEPTION_MESSAGE));
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
            assertThat(parameters).containsEntry("entityClassName", FAKE_CLASS_NAME);
            assertThat(parameters).containsKey("queryParameters");

            final Map<String, Serializable> queryParameters = (Map<String, Serializable>) parameters
                    .get("queryParameters");
            assertThat(queryParameters).as("should compute search filters").hasSize(2);
            assertThat(queryParameters).containsEntry("name", "John");
            assertThat(queryParameters).containsEntry("country", "US");

            return new BusinessDataQueryResultImpl(jsonResponse, new BusinessDataQueryMetadataImpl(1, 2, 4L));
        };
        when(commandAPI.execute(anyString(), anyMap())).then(answer);

        //when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}", FAKE_CLASS_NAME)
                        .param("q", "findByName")
                        .param("c", "5")
                        .param("p", "3")
                        .param("f", "name=John")
                        .param("f", "country=US")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                //then
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonResponse, true))
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
            assertThat(parameters).containsEntry("entityClassName", FAKE_CLASS_NAME);
            assertThat(parameters).containsKey("queryParameters");

            final Map<String, Serializable> queryParameters = (Map<String, Serializable>) parameters
                    .get("queryParameters");
            assertThat(queryParameters).as("should compute search filters").hasSize(1);
            assertThat(queryParameters).containsEntry("names", "Harry,Anna");

            return new BusinessDataQueryResultImpl(jsonResponse, new BusinessDataQueryMetadataImpl(1, 2, 26L));
        };
        when(commandAPI.execute(anyString(), anyMap())).then(answer);

        //when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}", FAKE_CLASS_NAME)
                        .param("q", "findByNames")
                        .param("c", "6")
                        .param("p", "2")
                        .param("f", "names=Harry,Anna")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                //then
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonResponse, true))
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "2-6/26"));
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
            assertThat(parameters).containsEntry("entityClassName", FAKE_CLASS_NAME);
            assertThat(parameters).containsKey("queryParameters");

            final Map<String, Serializable> queryParameters = (Map<String, Serializable>) parameters
                    .get("queryParameters");
            assertThat(queryParameters).as("should compute search filters").hasSize(2);
            assertThat(queryParameters).containsEntry("names", "Harry,Anna");
            assertThat(queryParameters).containsEntry("countries", "Catalunya,Corsica,Scotland,Quebec");

            return new BusinessDataQueryResultImpl(jsonResponse, new BusinessDataQueryMetadataImpl(1, 2, 36L));
        };
        when(commandAPI.execute(anyString(), anyMap())).then(answer);

        //when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}", FAKE_CLASS_NAME)
                        .param("q", "findByNamesAndCountries")
                        .param("c", "8")
                        .param("p", "4")
                        .param("f", "names=Harry,Anna", "countries=Catalunya,Corsica,Scotland,Quebec")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                //then
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonResponse, true))
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "4-8/36"));
    }

    @Test
    void should_call_custom_query_throw_exception_when_missing_count() throws Exception {
        // when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}", FAKE_CLASS_NAME)
                        .param("q", "findByName")
                        .param("p", "0")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                // then
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(IllegalArgumentException.class.toString()))
                .andExpect(jsonPath("$.message").value("query parameter c (count) is mandatory"));
    }

    @Test
    void should_call_custom_query_throw_exception_when_count_conversion_fails() throws Exception {
        // when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}", FAKE_CLASS_NAME)
                        .param("q", "findByName")
                        .param("p", "0")
                        .param("c", "abc")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                // then
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(IllegalArgumentException.class.toString()))
                .andExpect(jsonPath("$.message").value("query parameter c (count) should be a number"));
    }

    @Test
    public void should_call_custom_query_throw_exception_when_missing_page() throws Exception {
        // when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}", FAKE_CLASS_NAME)
                        .param("q", "findByName")
                        .param("c", "0")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                // then
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(IllegalArgumentException.class.toString()))
                .andExpect(jsonPath("$.message").value("query parameter p (page) is mandatory"));
    }

    @Test
    public void should_call_custom_query_throw_exception_when_page_conversion_fails() throws Exception {
        // when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}", FAKE_CLASS_NAME)
                        .param("q", "findByName")
                        .param("c", "0")
                        .param("p", "abc")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                // then
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(IllegalArgumentException.class.toString()))
                .andExpect(jsonPath("$.message").value("query parameter p (page) should be a number"));
    }

    @Test
    void should_call_custom_query_throw_not_found_when_CommandNotFoundException() throws Exception {
        // given
        when(commandAPI.execute(anyString(), anyMap()))
                .thenThrow(new CommandNotFoundException(new RuntimeException(FAKE_EXCEPTION_MESSAGE)));

        // when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}", FAKE_CLASS_NAME)
                        .param("q", "findByName")
                        .param("c", "5")
                        .param("p", "3")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                // then
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(CommandNotFoundException.class.toString()))
                .andExpect(jsonPath("$.message").value(FAKE_EXCEPTION_MESSAGE));
    }

    @Test
    public void should_call_custom_query_return_an_internal_server_error_status_when_command_fails_during_execution()
            throws Exception {
        // given
        var exception = new BusinessDataRepositoryException(FAKE_EXCEPTION_MESSAGE);
        when(commandAPI.execute(anyString(), anyMap()))
                .thenThrow(newCommandExecutionException(exception));

        // when
        mockMvc.perform(
                get("/API/bdm/businessData/{className}", FAKE_CLASS_NAME)
                        .param("q", "findByName")
                        .param("c", "5")
                        .param("p", "3")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))

                // then
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(CommandExecutionException.class.toString()))
                .andExpect(jsonPath("$.message").value(exception.toString()));
    }

    // wrap the root cause in the same way the command api does
    private static CommandExecutionException newCommandExecutionException(Exception cause) {
        return new CommandExecutionException(new SCommandExecutionException((cause)));
    }

}
