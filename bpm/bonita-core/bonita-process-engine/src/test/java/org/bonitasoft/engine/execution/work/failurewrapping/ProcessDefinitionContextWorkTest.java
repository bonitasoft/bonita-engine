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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Map;

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
public class ProcessDefinitionContextWorkTest extends AbstractContextWorkTest {

    private static final long PROCESS_DEFINITION_ID = 2;

    private static final String VERSION = "version";

    private static final String NAME = "name";

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Mock
    private SProcessDefinitionDeployInfo sProcessDefinitionDeployInfo;

    @Override
    @Before
    public void before() throws SBonitaException {
        doReturn(NAME).when(sProcessDefinitionDeployInfo).getName();
        doReturn(VERSION).when(sProcessDefinitionDeployInfo).getVersion();

        doReturn(sProcessDefinitionDeployInfo).when(processDefinitionService).getProcessDeploymentInfo(PROCESS_DEFINITION_ID);
        doReturn(processDefinitionService).when(tenantAccessor).getProcessDefinitionService();

        txBonitawork = spy(new ProcessDefinitionContextWork(wrappedWork, PROCESS_DEFINITION_ID));

        super.before();
    }

    @Test
    public void handleFailure() throws Throwable {
        final Map<String, Object> context = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        final SBonitaException e = new SBonitaException() {

            private static final long serialVersionUID = -6748168976371554636L;
        };

        txBonitawork.handleFailure(e, context);

        assertTrue(e.getMessage().contains("PROCESS_DEFINITION_ID=" + PROCESS_DEFINITION_ID));
        assertTrue(e.getMessage().contains("PROCESS_NAME=" + NAME));
        assertTrue(e.getMessage().contains("PROCESS_VERSION=" + VERSION));
        verify(wrappedWork).handleFailure(e, context);
    }
}
