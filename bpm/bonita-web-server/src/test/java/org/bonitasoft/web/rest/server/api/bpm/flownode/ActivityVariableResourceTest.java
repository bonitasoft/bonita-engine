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
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.bpm.data.impl.DataInstanceImpl;
import org.bonitasoft.engine.bpm.data.impl.DoubleDataInstanceImpl;
import org.bonitasoft.engine.bpm.data.impl.FloatDataInstanceImpl;
import org.bonitasoft.engine.bpm.data.impl.LongDataInstanceImpl;
import org.bonitasoft.engine.bpm.data.impl.ShortTextDataInstanceImpl;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.web.rest.server.BonitaRestletApplication;
import org.bonitasoft.web.rest.server.utils.RestletTest;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.restlet.Response;
import org.restlet.resource.ServerResource;

@RunWith(MockitoJUnitRunner.class)
public class ActivityVariableResourceTest extends RestletTest {

    @Mock
    ProcessAPI processAPI;

    @Spy
    @InjectMocks
    private ActivityVariableResource activityVariableResource;

    @Override
    protected ServerResource configureResource() {
        return new ActivityVariableResource(processAPI);
    }

    @Test(expected = APIException.class)
    public void shouldDoGetWithNothingThowsAnApiException() throws DataNotFoundException {
        //when
        activityVariableResource.getTaskVariable();
    }

    @Test(expected = APIException.class)
    public void should_throw_exception_if_attribute_is_not_found() throws Exception {
        // given:
        doReturn(null).when(activityVariableResource).getAttribute(anyString());

        // when:
        activityVariableResource.getTaskVariable();
    }

    @Test
    public void should_do_get_call_getAttribute() throws DataNotFoundException {
        //given
        doReturn("").when(activityVariableResource).getAttribute(ActivityVariableResource.ACTIVITYDATA_DATA_NAME);
        doReturn("1").when(activityVariableResource).getAttribute(ActivityVariableResource.ACTIVITYDATA_ACTIVITY_ID);
        doReturn(null).when(processAPI).getActivityDataInstance(anyString(), anyLong());

        //when
        activityVariableResource.getTaskVariable();

        //then
        verify(activityVariableResource).getAttribute(ActivityVariableResource.ACTIVITYDATA_ACTIVITY_ID);
        verify(activityVariableResource).getAttribute(ActivityVariableResource.ACTIVITYDATA_DATA_NAME);

    }

    @Test
    public void should_return() throws DataNotFoundException {
        // given
        doReturn("").when(activityVariableResource).getAttribute(ActivityVariableResource.ACTIVITYDATA_DATA_NAME);
        doReturn("1").when(activityVariableResource).getAttribute(ActivityVariableResource.ACTIVITYDATA_ACTIVITY_ID);
        final DataInstanceImpl dataInstance = createLongDataInstance(123L);
        doReturn(dataInstance).when(processAPI).getActivityDataInstance(anyString(), anyLong());

        // when
        final DataInstance dataInstanceResult = activityVariableResource.getTaskVariable();

        // then
        assertThat(dataInstanceResult).isEqualTo(dataInstance);
    }

    private DataInstanceImpl createShortTextDataInstance(String value) {
        final ShortTextDataInstanceImpl dataInstance = new ShortTextDataInstanceImpl(createDataDefinition(), value);
        fillIds(dataInstance);
        return dataInstance;
    }

    private DataInstanceImpl createLongDataInstance(Long value) {
        final LongDataInstanceImpl dataInstance = new LongDataInstanceImpl(createDataDefinition(), value);
        fillIds(dataInstance);
        return dataInstance;
    }

    private DataInstanceImpl createFloatDataInstance(float value) {
        final FloatDataInstanceImpl dataInstance = new FloatDataInstanceImpl(createDataDefinition(), value);
        fillIds(dataInstance);
        return dataInstance;
    }

    private DataInstanceImpl createDoubleDataInstance(double value) {
        final DoubleDataInstanceImpl dataInstance = new DoubleDataInstanceImpl(createDataDefinition(), value);
        fillIds(dataInstance);
        return dataInstance;
    }

    private void fillIds(final DataInstanceImpl dataInstance) {
        dataInstance.setId(5L);
        dataInstance.setTenantId(2L);
        dataInstance.setContainerId(7L);
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

    @Test
    public void should_DataInstance_return_number_as_strings() throws Exception {
        checkJsonDataInstance(createLongDataInstance(123L), "longDataInstance.json");
        checkJsonDataInstance(createShortTextDataInstance("abc"), "stringDataInstance.json");
        checkJsonDataInstance(createFloatDataInstance(123.456F), "floatDataInstance.json");
        checkJsonDataInstance(createDoubleDataInstance(123.5D), "doubleDataInstance.json");
        checkJsonDataInstance(createLongDataInstance(null), "nullDataInstance.json");
    }

    private void checkJsonDataInstance(final DataInstanceImpl dataInstance, final String jsonFile) throws Exception {
        //given
        doReturn(dataInstance).when(processAPI).getActivityDataInstance(anyString(), anyLong());

        //when
        final Response response = request(BonitaRestletApplication.BPM_ACTIVITY_VARIABLE_URL + "/10/variableName")
                .get();

        //then
        assertJsonEquals(getJson(jsonFile), response.getEntityAsText());
    }

}
