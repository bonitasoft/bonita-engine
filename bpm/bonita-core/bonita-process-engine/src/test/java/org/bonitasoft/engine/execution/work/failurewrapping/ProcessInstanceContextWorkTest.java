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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessInstanceContextWorkTest extends AbstractContextWorkTest {

    static final long PROCESS_INSTANCE_ID = 2;

    static final long ROOT_PROCESS_INSTANCE_ID = 3;

    @Mock
    ProcessInstanceService processInstanceService;

    @Mock
    SProcessInstance sProcessInstance;

    @Override
    @Before
    public void before() throws SBonitaException {
        doReturn(ROOT_PROCESS_INSTANCE_ID).when(sProcessInstance).getRootProcessInstanceId();
        doReturn(sProcessInstance).when(processInstanceService).getProcessInstance(PROCESS_INSTANCE_ID);

        doReturn(processInstanceService).when(tenantAccessor).getProcessInstanceService();
        txBonitawork = spy(new ProcessInstanceContextWork(wrappedWork, PROCESS_INSTANCE_ID));
        super.before();
    }

    @Test
    public void handleFailureWithProcessInstanceId() throws Throwable {
        final Map<String, Object> context = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        final SBonitaException e = new SBonitaException() {

            private static final long serialVersionUID = -6748168976371554636L;
        };

        txBonitawork.handleFailure(e, context);

        assertTrue(e.getMessage().contains("PROCESS_INSTANCE_ID=" + PROCESS_INSTANCE_ID));
        assertTrue(e.getMessage().contains("ROOT_PROCESS_INSTANCE_ID=" + ROOT_PROCESS_INSTANCE_ID));
        verify(wrappedWork).handleFailure(e, context);
    }

    @Test
    public void handleFailureWithProcessInstanceAndRootIds() throws Throwable {
        txBonitawork = spy(new ProcessInstanceContextWork(wrappedWork, PROCESS_INSTANCE_ID, 5));
        final Map<String, Object> context = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        final SBonitaException e = new SBonitaException() {

            private static final long serialVersionUID = -6748168976371554636L;
        };

        txBonitawork.handleFailure(e, context);

        assertTrue(e.getMessage().contains("PROCESS_INSTANCE_ID=" + PROCESS_INSTANCE_ID));
        assertTrue(e.getMessage().contains("ROOT_PROCESS_INSTANCE_ID=" + 5));
        verify(wrappedWork, times(1)).handleFailure(e, context);
    }

}
