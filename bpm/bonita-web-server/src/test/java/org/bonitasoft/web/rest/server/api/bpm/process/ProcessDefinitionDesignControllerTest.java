/**
 * Copyright (C) 2026 Bonitasoft S.A.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.web.rest.server.api.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

class ProcessDefinitionDesignControllerTest extends AbstractControllerTest<ProcessDefinitionDesignController> {

    private static final long PROCESS_DEFINITION_ID = 4L;

    @Mock
    protected ProcessAPI processAPI;

    @Override
    protected ProcessDefinitionDesignController createController() {
        return spy(new ProcessDefinitionDesignController());
    }

    @Override
    protected void configureMocks(ProcessDefinitionDesignController controller) throws Exception {
        doReturn(processAPI).when(controller).getProcessAPI(apiSession);
    }

    @Test
    void should_respond_404_Not_found_when_process_definition_is_not_found() throws Exception {
        when(processAPI.getDesignProcessDefinition(PROCESS_DEFINITION_ID))
                .thenThrow(new ProcessDefinitionNotFoundException("process definition not found"));

        mockMvc.perform(get("/APISpringInternal/bpm/process/" + PROCESS_DEFINITION_ID + "/design")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_return_utf8_string_when_get_process_design_is_called()
            throws Exception {
        // given
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processWithAccents", "1.0");
        processDefinitionBuilder.addUserTask("étape1", "employee");
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();
        when(processAPI.getDesignProcessDefinition(PROCESS_DEFINITION_ID)).thenReturn(designProcessDefinition);

        // when/then
        mockMvc.perform(get("/APISpringInternal/bpm/process/" + PROCESS_DEFINITION_ID + "/design")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertThat(content).contains("étape1");
                    assertThat(content).doesNotContain("Ã©tape1");
                });
    }

    @Test
    void replace_long_ids_to_string() {
        ProcessDefinitionDesignController controller = new ProcessDefinitionDesignController();

        assertThat(controller.replaceLongIdToString("""
                { "id": 123}"""))
                .isEqualToIgnoringCase("""
                        { "id": "123"}""");
        assertThat(controller.replaceLongIdToString("""
                { "id":123, "test": [ otherid: "zerze"]}"""))
                .isEqualToIgnoringCase("""
                        { "id":"123", "test": [ otherid: "zerze"]}""");
        assertThat(controller.replaceLongIdToString("""
                { "iaed": 123}"""))
                .isEqualToIgnoringCase("""
                        { "iaed": 123}""");
        assertThat(controller.replaceLongIdToString("""
                { "name": "\\"id\\": 123"}"""))
                .isEqualToIgnoringCase("""
                        { "name": "\\"id\\": 123"}""");
    }

    @Test
    void replaceLongIdToString_should_handle_edge_cases() {
        ProcessDefinitionDesignController controller = new ProcessDefinitionDesignController();

        // Empty string
        assertThat(controller.replaceLongIdToString("")).isEmpty();

        // Zero value
        assertThat(controller.replaceLongIdToString("""
                { "id": 0}"""))
                .isEqualTo("""
                        { "id": "0"}""");

        // Large number (Long.MAX_VALUE)
        assertThat(controller.replaceLongIdToString("""
                { "id": 9223372036854775807}"""))
                .isEqualTo("""
                        { "id": "9223372036854775807"}""");

        // Multiple ids in nested structures
        assertThat(controller.replaceLongIdToString("""
                { "id": 123, "nested": { "id": 456 }}"""))
                .isEqualTo("""
                        { "id": "123", "nested": { "id": "456" }}""");

        // Ids in array of objects
        assertThat(controller.replaceLongIdToString("""
                [{"id": 1}, {"id": 2}, {"id": 3}]"""))
                .isEqualTo("""
                        [{"id": "1"}, {"id": "2"}, {"id": "3"}]""");

        // No whitespace around colon
        assertThat(controller.replaceLongIdToString("""
                {"id":123}"""))
                .isEqualTo("""
                        {"id":"123"}""");

        // Multiple whitespace around colon
        assertThat(controller.replaceLongIdToString("""
                { "id"  :  456 }"""))
                .isEqualTo("""
                        { "id"  :  "456" }""");

        // Id at end of object
        assertThat(controller.replaceLongIdToString("""
                { "name": "test", "id": 789 }"""))
                .isEqualTo("""
                        { "name": "test", "id": "789" }""");

        // Id followed by comma
        assertThat(controller.replaceLongIdToString("""
                { "id": 999, "name": "test" }"""))
                .isEqualTo("""
                        { "id": "999", "name": "test" }""");

        // Multiline JSON
        assertThat(controller.replaceLongIdToString("""
                {
                  "id": 111,
                  "name": "test"
                }"""))
                .isEqualTo("""
                        {
                          "id": "111",
                          "name": "test"
                        }""");

        // Complex nested structure with multiple ids
        assertThat(controller.replaceLongIdToString("""
                { "id": 1, "items": [{ "id": 2 }, { "id": 3 }], "parent": { "id": 4, "child": { "id": 5 }}}"""))
                .isEqualTo(
                        """
                                { "id": "1", "items": [{ "id": "2" }, { "id": "3" }], "parent": { "id": "4", "child": { "id": "5" }}}""");

        // Id is not replaced when part of longer field name
        assertThat(controller.replaceLongIdToString("""
                { "userId": 123, "id": 456, "customId": 789 }"""))
                .isEqualTo("""
                        { "userId": 123, "id": "456", "customId": 789 }""");

        // Case sensitivity - only lowercase "id" is replaced
        assertThat(controller.replaceLongIdToString("""
                { "Id": 123, "ID": 456, "id": 789 }"""))
                .isEqualTo("""
                        { "Id": 123, "ID": 456, "id": "789" }""");
    }

    @Test
    void replaceLongIdToString_should_not_replace_negative_numbers() {
        ProcessDefinitionDesignController controller = new ProcessDefinitionDesignController();

        // Negative numbers are not converted (regex limitation - \d+ doesn't match minus sign)
        // This documents current behavior - negative ids remain numeric
        assertThat(controller.replaceLongIdToString("""
                { "id": -123}"""))
                .isEqualTo("""
                        { "id": -123}""");
    }
}
