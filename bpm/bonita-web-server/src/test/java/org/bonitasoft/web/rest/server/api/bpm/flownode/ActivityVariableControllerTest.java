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
package org.bonitasoft.web.rest.server.api.bpm.flownode;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.bpm.data.impl.LongDataInstanceImpl;
import org.bonitasoft.engine.bpm.data.impl.ShortTextDataInstanceImpl;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.web.rest.server.api.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

class ActivityVariableControllerTest extends AbstractControllerTest<ActivityVariableController> {

    private static final String API_URL = "/API/bpm/activityVariable";

    @Mock
    protected ProcessAPI processAPI;

    @Override
    protected ActivityVariableController createController() {
        return spy(new ActivityVariableController());
    }

    @Override
    protected void configureMocks(ActivityVariableController controller) throws Exception {
        doReturn(processAPI).when(controller).getProcessAPI(apiSession);
    }

    @Test
    void should_return_Long_data_instance() throws Exception {
        var dataInstance = createLongDataInstance(123L);
        when(processAPI.getActivityDataInstance("myVar", 10L)).thenReturn(dataInstance);

        mockMvc.perform(get(API_URL + "/10/myVar")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("dataInstanceName"))
                .andExpect(jsonPath("$.value").value(123))
                .andExpect(jsonPath("$.className").value("com.company.Model"));
    }

    @Test
    void should_return_String_data_instance() throws Exception {
        var dataInstance = createShortTextDataInstance("abc");
        when(processAPI.getActivityDataInstance("myVar", 10L)).thenReturn(dataInstance);

        mockMvc.perform(get(API_URL + "/10/myVar")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("dataInstanceName"))
                .andExpect(jsonPath("$.value").value("abc"))
                .andExpect(jsonPath("$.value_string").value("abc"));
    }

    @Test
    void should_return_Float_data_instance() throws Exception {
        var dataInstance = new org.bonitasoft.engine.bpm.data.impl.FloatDataInstanceImpl(createDataDefinition(),
                123.456F);
        dataInstance.setId(5L);
        dataInstance.setContainerId(7L);
        when(processAPI.getActivityDataInstance("myVar", 10L)).thenReturn(dataInstance);

        mockMvc.perform(get(API_URL + "/10/myVar")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(123.456F))
                .andExpect(jsonPath("$.value_string").value("123.456"));
    }

    @Test
    void should_return_Double_data_instance() throws Exception {
        var dataInstance = new org.bonitasoft.engine.bpm.data.impl.DoubleDataInstanceImpl(createDataDefinition(),
                123.5D);
        dataInstance.setId(5L);
        dataInstance.setContainerId(7L);
        when(processAPI.getActivityDataInstance("myVar", 10L)).thenReturn(dataInstance);

        mockMvc.perform(get(API_URL + "/10/myVar")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(123.5D))
                .andExpect(jsonPath("$.value_string").value("123.5"));
    }

    @Test
    void should_return_null_data_instance() throws Exception {
        var dataInstance = createLongDataInstance(null);
        when(processAPI.getActivityDataInstance("myVar", 10L)).thenReturn(dataInstance);

        mockMvc.perform(get(API_URL + "/10/myVar")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").isEmpty())
                .andExpect(jsonPath("$.value_string").value("null"));
    }

    @Test
    void should_return_LocalDate_data_instance() throws Exception {
        var dataInstance = new LocalDateDataInstanceImpl(createDataDefinition(),
                java.time.LocalDate.parse("2016-08-16"));
        dataInstance.setId(5L);
        dataInstance.setContainerId(7L);
        when(processAPI.getActivityDataInstance("myVar", 10L)).thenReturn(dataInstance);

        mockMvc.perform(get(API_URL + "/10/myVar")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("2016-08-16"))
                .andExpect(jsonPath("$.value_string").value("2016-08-16"));
    }

    @Test
    void should_respond_404_when_data_not_found() throws Exception {
        when(processAPI.getActivityDataInstance("unknownVar", 10L))
                .thenThrow(new DataNotFoundException(new Exception("not found")));

        mockMvc.perform(get(API_URL + "/10/unknownVar")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_respond_404_when_flownode_not_found() throws Exception {
        final long activityInstanceId = 10L;
        when(processAPI.getActivityDataInstance("unknownFlownode", activityInstanceId))
                .thenThrow(new DataNotFoundException(new SFlowNodeNotFoundException(activityInstanceId)));

        mockMvc.perform(get(API_URL + "/10/unknownFlownode")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_respond_400_when_activity_id_is_not_a_number() throws Exception {
        mockMvc.perform(get(API_URL + "/notANumber/myVar")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private LongDataInstanceImpl createLongDataInstance(Long value) {
        var dataInstance = new LongDataInstanceImpl(createDataDefinition(), value);
        dataInstance.setId(5L);
        dataInstance.setContainerId(7L);
        return dataInstance;
    }

    private ShortTextDataInstanceImpl createShortTextDataInstance(String value) {
        var dataInstance = new ShortTextDataInstanceImpl(createDataDefinition(), value);
        dataInstance.setId(5L);
        dataInstance.setContainerId(7L);
        return dataInstance;
    }

    private DataDefinition createDataDefinition() {
        return new DataDefinition() {

            @Override
            public String getClassName() {
                return "com.company.Model";
            }

            @Override
            public boolean isTransientData() {
                return false;
            }

            @Override
            public Expression getDefaultValueExpression() {
                return null;
            }

            @Override
            public String getDescription() {
                return "description";
            }

            @Override
            public String getName() {
                return "dataInstanceName";
            }

            @Override
            public void accept(ModelFinderVisitor visitor, long modelId) {
            }
        };
    }

    // Custom DataInstance for LocalDate (not provided by the engine)
    static class LocalDateDataInstanceImpl extends org.bonitasoft.engine.bpm.data.impl.DataInstanceImpl {

        private java.time.LocalDate value;

        public LocalDateDataInstanceImpl(DataDefinition dataDefinition, java.time.LocalDate value) {
            super(dataDefinition);
            this.value = value;
        }

        @Override
        public java.time.LocalDate getValue() {
            return value;
        }

        @Override
        public void setValue(java.io.Serializable value) {
            this.value = (java.time.LocalDate) value;
        }
    }
}
