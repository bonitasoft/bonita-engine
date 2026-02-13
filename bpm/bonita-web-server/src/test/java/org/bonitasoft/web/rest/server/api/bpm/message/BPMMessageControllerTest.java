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
package org.bonitasoft.web.rest.server.api.bpm.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.SendEventException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.web.rest.server.api.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.http.MediaType;

class BPMMessageControllerTest extends AbstractControllerTest<BPMMessageController> {

    @Mock
    protected ProcessAPI processAPI;

    @Override
    protected BPMMessageController createController() {
        return spy(new BPMMessageController());
    }

    @Override
    protected void configureMocks(BPMMessageController controller) throws Exception {
        doReturn(processAPI).when(controller).getProcessAPI(apiSession);
    }

    @Test
    void should_send_message_with_target_flow_node() throws Exception {
        mockMvc.perform(post("/API/bpm/message")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "messageName": "msg",
                          "targetProcess": "myProcess",
                          "targetFlowNode": "activity"
                        }
                        """))
                .andExpect(status().isNoContent());

        verify(processAPI).sendMessage(eq("msg"),
                eq(new ExpressionBuilder().createConstantStringExpression("myProcess")),
                eq(new ExpressionBuilder().createConstantStringExpression("activity")),
                eq(Collections.emptyMap()), eq(Collections.emptyMap()));
    }

    @Test
    void should_return_error_when_engine_throws_SendEventException() throws Exception {
        doThrow(new SendEventException("wrong params")).when(processAPI).sendMessage(eq("test"), any(Expression.class),
                any(Expression.class), anyMap(), anyMap());

        mockMvc.perform(post("/API/bpm/message")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "messageName": "test",
                          "targetProcess": "test",
                          "targetFlowNode": "test"
                        }
                        """))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void should_return_bad_request_when_messageName_is_not_set() throws Exception {
        mockMvc.perform(post("/API/bpm/message")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "targetProcess": "myProcess",
                          "targetFlowNode": "activity"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("'messageName' attribute is mandatory")));
    }

    @Test
    void should_return_bad_request_when_targetProcess_is_not_set() throws Exception {
        mockMvc.perform(post("/API/bpm/message")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "messageName": "my msg",
                          "targetFlowNode": "activity"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("'targetProcess' attribute is mandatory")));
    }

    @Test
    void should_return_bad_request_with_error_body_when_request_body_is_empty() throws Exception {
        mockMvc.perform(post("/API/bpm/message")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Unable to parse the JSON body")));
    }

    @Test
    void should_return_bad_request_when_json_is_malformed() throws Exception {
        mockMvc.perform(post("/API/bpm/message")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not valid json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Unable to parse the JSON body")));
    }

    @Test
    void should_accept_primitive_types_in_message_content_values() throws Exception {
        mockMvc.perform(post("/API/bpm/message")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {
                                  "messageName": "msg",
                                  "targetProcess": "myProcess",
                                  "targetFlowNode": "activity",
                                  "messageContent": {
                                    "id": { "value": 123, "type": "java.lang.Long" },
                                    "name": { "value": "john", "type": "java.lang.String" },
                                    "amount": { "value": 1243.234, "type": "java.lang.Double" },
                                    "nbDay": { "value": 34, "type": "java.lang.Integer" },
                                    "validated": { "value": true, "type": "java.lang.Boolean" },
                                    "rate": { "value": 3.0, "type": "java.lang.Float" },
                                    "expectedDate": { "value": "2018-09-09", "type": "java.time.LocalDate" },
                                    "startDate": { "value": "2018-08-09T14:30:00", "type": "java.time.LocalDateTime" },
                                    "globalEndDate": { "value": "2018-08-09T14:30:00+01:00", "type": "java.time.OffsetDateTime" }
                                  }
                                }
                                """))
                .andExpect(status().isNoContent());

        ArgumentCaptor<Map<Expression, Expression>> msgContentCaptor = ArgumentCaptor.forClass(Map.class);

        verify(processAPI).sendMessage(eq("msg"),
                eq(expression("myProcess", "myProcess", String.class)),
                eq(expression("activity", "activity", String.class)),
                msgContentCaptor.capture(),
                eq(Collections.emptyMap()));

        assertThat(msgContentCaptor.getValue())
                .containsValue(expression("123", "123", Long.class))
                .containsValue(expression("john", "john", String.class))
                .containsValue(expression("1243.234", "1243.234", Double.class))
                .containsValue(expression("34", "34", Integer.class))
                .containsValue(expression("true", "true", Boolean.class))
                .containsValue(expression("3.0", "3.0", Float.class))
                .containsValue(expression("2018-09-09", "2018-09-09", LocalDate.class))
                .containsValue(expression("2018-08-09T14:30:00", "2018-08-09T14:30:00", LocalDateTime.class))
                .containsValue(
                        expression("2018-08-09T14:30:00+01:00", "2018-08-09T14:30:00+01:00", OffsetDateTime.class));
    }

    @Test
    void should_return_bad_request_when_message_content_value_is_missing() throws Exception {
        mockMvc.perform(post("/API/bpm/message")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "messageName": "msg",
                          "targetProcess": "myProcess",
                          "targetFlowNode": "activity",
                          "messageContent": {
                            "id": null
                          }
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_bad_request_when_message_content_value_is_null() throws Exception {
        mockMvc.perform(post("/API/bpm/message")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "messageName": "msg",
                          "targetProcess": "myProcess",
                          "targetFlowNode": "activity",
                          "messageContent": {
                            "id": { "value": null }
                          }
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_accept_empty_string_in_message_content_values() throws Exception {
        mockMvc.perform(post("/API/bpm/message")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "messageName": "msg",
                          "targetProcess": "myProcess",
                          "targetFlowNode": "activity",
                          "messageContent": {
                            "id": { "value": "" }
                          }
                        }
                        """))
                .andExpect(status().isNoContent());

        ArgumentCaptor<Map<Expression, Expression>> msgContentCaptor = ArgumentCaptor.forClass(Map.class);

        verify(processAPI).sendMessage(eq("msg"),
                eq(expression("myProcess", "myProcess", String.class)),
                eq(expression("activity", "activity", String.class)),
                msgContentCaptor.capture(),
                eq(Collections.emptyMap()));

        assertThat(msgContentCaptor.getValue())
                .containsValue(expression("empty-value", "", String.class));
    }

    @Test
    void should_accept_primitive_types_in_correlation_values() throws Exception {
        mockMvc.perform(post("/API/bpm/message")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "messageName": "msg",
                          "targetProcess": "myProcess",
                          "targetFlowNode": "activity",
                          "correlations": {
                            "id": { "value": 123, "type": "java.lang.Long" },
                            "name": { "value": "john", "type": "java.lang.String" }
                          }
                        }
                        """))
                .andExpect(status().isNoContent());

        ArgumentCaptor<Map<Expression, Expression>> correlationsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(processAPI).sendMessage(eq("msg"),
                eq(expression("myProcess", "myProcess", String.class)),
                eq(expression("activity", "activity", String.class)),
                eq(Collections.emptyMap()),
                correlationsCaptor.capture());

        assertThat(correlationsCaptor.getValue())
                .containsValue(expression("123", "123", Long.class))
                .containsValue(expression("john", "john", String.class));
    }

    @Test
    void should_return_error_when_messageContent_has_unsupported_value_type() throws Exception {
        mockMvc.perform(post("/API/bpm/message")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "messageName": "msg",
                          "targetProcess": "myProcess",
                          "targetFlowNode": "activity",
                          "messageContent": {
                            "array": { "value": "someValue", "type": "java.lang.String[]" }
                          }
                        }
                        """))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void should_return_bad_request_when_more_than_5_correlations() throws Exception {
        mockMvc.perform(post("/API/bpm/message")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "messageName": "msg",
                          "targetProcess": "myProcess",
                          "targetFlowNode": "activity",
                          "correlations": {
                            "k1": { "value": "1", "type": "java.lang.String" },
                            "k2": { "value": "1", "type": "java.lang.String" },
                            "k3": { "value": "1", "type": "java.lang.String" },
                            "k4": { "value": "1", "type": "java.lang.String" },
                            "k5": { "value": "1", "type": "java.lang.String" },
                            "k6": { "value": "1", "type": "java.lang.String" }
                          }
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_send_message_with_message_content() throws Exception {
        mockMvc.perform(post("/API/bpm/message")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "messageName": "message",
                          "targetProcess": "myProcess",
                          "messageContent": { "id": { "value": 12 } }
                        }
                        """))
                .andExpect(status().isNoContent());
    }

    @Test
    void should_guess_LocalDateTime_type_when_not_specified() throws Exception {
        mockMvc.perform(post("/API/bpm/message")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "messageName": "msg",
                          "targetProcess": "myProcess",
                          "targetFlowNode": "activity",
                          "messageContent": {
                            "startDate": { "value": "2018-08-09T14:30:00" }
                          }
                        }
                        """))
                .andExpect(status().isNoContent());

        ArgumentCaptor<Map<Expression, Expression>> msgContentCaptor = ArgumentCaptor.forClass(Map.class);

        verify(processAPI).sendMessage(eq("msg"),
                eq(expression("myProcess", "myProcess", String.class)),
                eq(expression("activity", "activity", String.class)),
                msgContentCaptor.capture(),
                eq(Collections.emptyMap()));

        assertThat(msgContentCaptor.getValue())
                .containsValue(expression("2018-08-09T14:30:00", "2018-08-09T14:30:00", LocalDateTime.class));
    }

    @Test
    void should_support_date_type() throws Exception {
        mockMvc.perform(post("/API/bpm/message")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "messageName": "message",
                          "targetProcess": "myProcess",
                          "targetFlowNode": "wait",
                          "messageContent": {
                            "updated": { "value": "2012-04-23T18:25:43.511Z", "type": "java.time.OffsetDateTime" }
                          }
                        }
                        """))
                .andExpect(status().isNoContent());
    }

    private Expression expression(String name, String content, Class<?> returnType) throws InvalidExpressionException {
        return new ExpressionBuilder().createExpression(name, content, returnType.getName(),
                ExpressionType.TYPE_CONSTANT);
    }
}
