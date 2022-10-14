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
package org.bonitasoft.web.rest.server.api.bpm.process;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.rest.server.utils.ResponseAssert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessExecutionException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessInstanceImpl;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.server.utils.RestletTest;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;

@RunWith(MockitoJUnitRunner.class)
public class ProcessInstantiationResourceTest extends RestletTest {

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    private static final long PROCESS_DEFINITION_ID = 2L;

    private static final String ID_PROCESS_DEFINITION = "2";

    private static final String URL_API_PROCESS_INSTANTIATION_TEST = "/bpm/process/" + ID_PROCESS_DEFINITION
            + "/instantiation";

    private static final String URL_API_PROCESS_INSTANTIATION_TEST_WITH_USER = URL_API_PROCESS_INSTANTIATION_TEST
            + "?user=1";

    private static final String VALID_COMPLEX_POST_BODY = "{\"aBoolean\":true, \"aString\":\"hello world\", \"a_complex_type\":{\"aNumber\":2, \"aBoolean\":false}}";

    private static final String VALID_POST_BODY = "{ \"key\": \"value\", \"key2\": \"value2\" }";

    @Mock
    ProcessAPI processAPI;

    ProcessInstantiationResource processInstantiationResource;

    @Mock
    Response response;

    @Mock
    ContractDefinition contractDefinition;

    @Mock
    APISession apiSession;

    @Mock
    ProcessInstance processInstance;

    @BeforeClass
    public static void initClass() {
        I18n.getInstance();
    }

    @Before
    public void initializeMocks() {
        processInstantiationResource = spy(new ProcessInstantiationResource(processAPI));
        // this allows us to track method calls on this internal dependency
        processInstantiationResource.typeConverterUtil = spy(processInstantiationResource.typeConverterUtil);
        when(contractDefinition.getInputs()).thenReturn(Collections.<InputDefinition> emptyList());
    }

    @Override
    protected ServerResource configureResource() {
        return new ProcessInstantiationResource(processAPI);
    }

    private Map<String, Serializable> aComplexInput() {
        final HashMap<String, Serializable> aComplexInput = new HashMap<>();
        aComplexInput.put("aBoolean", true);
        aComplexInput.put("aString", "hello world");

        final HashMap<String, Serializable> childMap = new HashMap<>();
        childMap.put("aNumber", 2);
        childMap.put("aBoolean", false);

        aComplexInput.put("a_complex_type", childMap);

        return aComplexInput;
    }

    @Test
    public void should_instanciate_a_process_with_given_inputs() throws Exception {
        final Map<String, Serializable> expectedComplexInput = aComplexInput();
        when(processAPI.startProcessWithInputs(PROCESS_DEFINITION_ID, expectedComplexInput))
                .thenReturn(new ProcessInstanceImpl("complexProcessInstance"));
        when(processAPI.getProcessContract(PROCESS_DEFINITION_ID)).thenReturn(contractDefinition);

        final Response response = request(URL_API_PROCESS_INSTANTIATION_TEST).post(VALID_COMPLEX_POST_BODY);

        assertThat(response).hasStatus(Status.SUCCESS_OK);
        assertThat(response.getEntityAsText())
                .isEqualTo(
                        "{\"caseId\":0}");
        verify(processAPI).startProcessWithInputs(PROCESS_DEFINITION_ID, expectedComplexInput);
    }

    @Test
    public void should_instanciate_a_process_with_given_inputs_for_a_specific_user() throws Exception {
        final Map<String, Serializable> expectedComplexInput = aComplexInput();
        when(processAPI.startProcessWithInputs(1L, PROCESS_DEFINITION_ID, expectedComplexInput))
                .thenReturn(new ProcessInstanceImpl("complexProcessInstance"));
        when(processAPI.getProcessContract(PROCESS_DEFINITION_ID)).thenReturn(contractDefinition);

        final Response response = request(URL_API_PROCESS_INSTANTIATION_TEST_WITH_USER).post(VALID_COMPLEX_POST_BODY);

        assertThat(response).hasStatus(Status.SUCCESS_OK);
        assertThat(response.getEntityAsText())
                .isEqualTo(
                        "{\"caseId\":0}");
        verify(processAPI).startProcessWithInputs(1L, PROCESS_DEFINITION_ID, expectedComplexInput);
        verify(processAPI, times(0)).startProcessWithInputs(PROCESS_DEFINITION_ID, expectedComplexInput);
    }

    @Test
    public void should_respond_400_Bad_request_when_contract_is_not_validated_when_instanciate_a_process()
            throws Exception {
        doThrow(new ContractViolationException("aMessage", "aMessage",
                asList("first explanation", "second explanation"), null))
                        .when(processAPI).startProcessWithInputs(anyLong(), anyMapOf(String.class, Serializable.class));
        when(processAPI.getProcessContract(PROCESS_DEFINITION_ID)).thenReturn(contractDefinition);

        final Response response = request(URL_API_PROCESS_INSTANTIATION_TEST).post(VALID_POST_BODY);

        assertThat(response).hasStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        assertThat(response)
                .hasJsonEntityEqualTo(
                        "{\"exception\":\"class org.bonitasoft.engine.bpm.contract.ContractViolationException\",\"message\":\"aMessage\",\"explanations\":[\"first explanation\",\"second explanation\"]}");
        verify(processInstantiationResource.typeConverterUtil, times(0)).deleteTemporaryFiles(anyMap());
    }

    @Test
    public void should_respond_500_Internal_server_error_when_error_occurs_on_process_instanciation() throws Exception {
        doThrow(new ProcessExecutionException("aMessage"))
                .when(processAPI).startProcessWithInputs(anyLong(), anyMapOf(String.class, Serializable.class));

        final Response response = request(URL_API_PROCESS_INSTANTIATION_TEST).post(VALID_POST_BODY);

        assertThat(response).hasStatus(Status.SERVER_ERROR_INTERNAL);
        verify(processInstantiationResource.typeConverterUtil, times(0)).deleteTemporaryFiles(anyMap());
    }

    @Test
    public void should_respond_400_Bad_request_when_trying_to_execute_with_not_json_payload() throws Exception {
        final Response response = request(URL_API_PROCESS_INSTANTIATION_TEST).post("invalid json string");

        assertThat(response).hasStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        verify(processInstantiationResource.typeConverterUtil, times(0)).deleteTemporaryFiles(anyMap());
    }

    @Test
    public void should_respond_404_Not_found_when_task_is_not_found_when_trying_to_execute_it() throws Exception {
        doThrow(new ProcessDefinitionNotFoundException("process not found")).when(processAPI)
                .startProcessWithInputs(anyLong(), anyMapOf(String.class, Serializable.class));
        when(processAPI.getProcessContract(PROCESS_DEFINITION_ID)).thenReturn(contractDefinition);

        final Response response = request(URL_API_PROCESS_INSTANTIATION_TEST).post(VALID_POST_BODY);

        assertThat(response).hasStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    @Test
    public void should_contract_violation_exception_log_explanations_when_logger_is_info() throws Exception {
        // given
        final String message = "contract violation !!!!";
        final List<String> explanations = Arrays.asList("explanation1", "explanation2");
        doThrow(new ContractViolationException(message, message, explanations, null)).when(processAPI)
                .startProcessWithInputs(anyLong(), anyMapOf(String.class, Serializable.class));
        doReturn(Long.toString(PROCESS_DEFINITION_ID)).when(processInstantiationResource)
                .getAttribute(ProcessInstantiationResource.PROCESS_DEFINITION_ID);
        doReturn(contractDefinition).when(processAPI).getProcessContract(PROCESS_DEFINITION_ID);
        doReturn(response).when(processInstantiationResource).getResponse();
        final Map<String, Serializable> inputs = new HashMap<>();
        inputs.put("testKey", "testValue");

        systemOutRule.clearLog();
        // when
        processInstantiationResource.instantiateProcess(inputs);

        // then
        assertThat(systemOutRule.getLog()).contains(message + "\nExplanations:\nexplanation1explanation2");
        verify(processInstantiationResource.typeConverterUtil, times(0)).deleteTemporaryFiles(anyMap());
    }

    @Test
    public void should_call_deleteFiles() throws ProcessDefinitionNotFoundException, FileNotFoundException,
            ProcessExecutionException, ProcessActivationException, ContractViolationException {
        //given
        doReturn(Long.toString(PROCESS_DEFINITION_ID)).when(processInstantiationResource)
                .getAttribute(ProcessInstantiationResource.PROCESS_DEFINITION_ID);
        doReturn(contractDefinition).when(processAPI).getProcessContract(PROCESS_DEFINITION_ID);
        doReturn(response).when(processInstantiationResource).getResponse();
        final Map<String, Serializable> inputs = new HashMap<>();
        inputs.put("testKey", "testValue");
        when(processAPI.startProcessWithInputs(PROCESS_DEFINITION_ID, inputs)).thenReturn(processInstance);

        //when
        processInstantiationResource.instantiateProcess(inputs);

        verify(processInstantiationResource.typeConverterUtil, times(1)).deleteTemporaryFiles(anyMap());
    }

    @Test
    public void should_getProcessDefinitionIdParameter_throws_an_exception_when_task_id_parameter_is_null()
            throws Exception {
        // given
        doReturn(null).when(processInstantiationResource)
                .getAttribute(ProcessInstantiationResource.PROCESS_DEFINITION_ID);

        try {
            // when
            processInstantiationResource.getProcessDefinitionIdParameter();
        } catch (final Exception e) {
            // then
            assertThat(e).isInstanceOf(APIException.class);
            assertThat(e.getMessage())
                    .isEqualTo("Attribute '" + ProcessInstantiationResource.PROCESS_DEFINITION_ID + "' is mandatory");
        }

    }
}
