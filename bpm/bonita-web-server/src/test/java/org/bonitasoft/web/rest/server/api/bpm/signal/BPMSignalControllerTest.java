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
package org.bonitasoft.web.rest.server.api.bpm.signal;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.web.rest.server.api.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

class BPMSignalControllerTest extends AbstractControllerTest<BPMSignalController> {

    @Mock
    protected ProcessAPI processAPI;

    @Override
    protected BPMSignalController createController() {
        return spy(new BPMSignalController());
    }

    @Override
    protected void configureMocks(BPMSignalController controller) throws Exception {
        doReturn(processAPI).when(controller).getProcessAPI(apiSession);
    }

    @Test
    void should_broadcast_signal_of_a_given_name() throws Exception {
        mockMvc.perform(post("/API/bpm/signal")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "signalName"}
                        """))
                .andExpect(status().isNoContent());

        verify(processAPI).sendSignal("signalName");
    }

    @Test
    void should_return_bad_request_if_signal_name_not_set() throws Exception {
        mockMvc.perform(post("/API/bpm/signal")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": null}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("'name' attribute is mandatory")));
    }

    @Test
    void should_return_bad_request_if_request_body_is_empty() throws Exception {
        mockMvc.perform(post("/API/bpm/signal")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Unable to parse the JSON body")));
    }

    @Test
    void should_return_bad_request_when_json_is_malformed() throws Exception {
        mockMvc.perform(post("/API/bpm/signal")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Unable to parse the JSON body")));
    }

    @Test
    void should_support_encoded_characters_for_signal_name() throws Exception {
        mockMvc.perform(post("/API/bpm/signal")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "My Sign\u00e0l"}
                        """))
                .andExpect(status().isNoContent());

        verify(processAPI).sendSignal("My Sign\u00e0l");
    }
}
