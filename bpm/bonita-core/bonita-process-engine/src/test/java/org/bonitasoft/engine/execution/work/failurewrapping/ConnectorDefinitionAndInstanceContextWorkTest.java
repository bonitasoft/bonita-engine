/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.execution.work.failurewrapping;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * 
 */
@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.class)
public class ConnectorDefinitionAndInstanceContextWorkTest extends AbstractContextWorkTest {

    private static final String CONNECTOR_DEFINITION_NAME = "connector_name";

    private static final ConnectorEvent ACTIVATION_EVENT = ConnectorEvent.ON_ENTER;

    private static final long CONNECTOR_INSTANCE_ID = 10L;

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Mock
    private SProcessDefinitionDeployInfo sProcessDefinitionDeployInfo;

    @Override
    @Before
    public void before() throws SBonitaException {
        txBonitawork = spy(new ConnectorDefinitionAndInstanceContextWork(wrappedWork, CONNECTOR_DEFINITION_NAME, CONNECTOR_INSTANCE_ID));
        super.before();
    }

    @Test
    public void handleFailureWithNameAndId() throws Throwable {
        final Map<String, Object> context = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        final SBonitaException e = new SBonitaException() {

            private static final long serialVersionUID = -6748168976371554636L;
        };

        txBonitawork.handleFailure(e, context);

        assertTrue(e.getMessage().contains("CONNECTOR_DEFINITION_IMPLEMENTATION_CLASS_NAME=" + CONNECTOR_DEFINITION_NAME));
        assertTrue(e.getMessage().contains("CONNECTOR_INSTANCE_ID=" + CONNECTOR_INSTANCE_ID));
        verify(wrappedWork).handleFailure(e, context);
    }

    @Test
    public void handleFailureWithNameAndIdAndActivationEvent() throws Throwable {
        txBonitawork = spy(new ConnectorDefinitionAndInstanceContextWork(wrappedWork, CONNECTOR_DEFINITION_NAME, CONNECTOR_INSTANCE_ID,
                ACTIVATION_EVENT));
        doReturn("The description").when(txBonitawork).getDescription();
        final Map<String, Object> context = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        final SBonitaException e = new SBonitaException() {

            private static final long serialVersionUID = -6748168976371554636L;
        };
        doThrow(e).when(wrappedWork).work(context);

        txBonitawork.handleFailure(e, context);

        assertTrue(e.getMessage().contains("CONNECTOR_DEFINITION_IMPLEMENTATION_CLASS_NAME=" + CONNECTOR_DEFINITION_NAME));
        assertTrue(e.getMessage().contains("CONNECTOR_INSTANCE_ID=" + CONNECTOR_INSTANCE_ID));
        assertTrue(e.getMessage().contains("CONNECTOR_ACTIVATION_EVENT=" + ACTIVATION_EVENT));
        verify(wrappedWork, times(1)).handleFailure(e, context);
    }
}
