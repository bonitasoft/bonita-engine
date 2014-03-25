/**
 * Copyright (C) 2014 Bonitasoft S.A.
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
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Aurelien Pupier
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class GlobalContextWorkTest extends AbstractContextWorkTest {

    private static final long TENANT_ID = 2L;

    private static final long THREAD_ID = 51L;

    @Override
    @Before
    public void before() throws Exception {
        txBonitawork = spy(new GlobalContextWork(wrappedWork));
        super.before();
        ((GlobalContextWork) doReturn(THREAD_ID).when(txBonitawork)).retrieveThreadId();
        doReturn(TENANT_ID).when(tenantAccessor).getTenantId();
        when(wrappedWork.getTenantId()).thenReturn(TENANT_ID);
    }

    @Test
    public void handleFailure() throws Throwable {
        final Map<String, Object> context = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        final SBonitaException e = new SBonitaException() {

            private static final long serialVersionUID = -6748168976371554636L;
        };

        txBonitawork.handleFailure(e, context);

        assertTrue("TENANT_ID is not available in context " + e.getMessage(), e.getMessage().contains("TENANT_ID=" + TENANT_ID));
        assertTrue(e.getMessage().contains("HOSTNAME=" + InetAddress.getLocalHost().getHostName()));
        assertTrue(e.getMessage().contains("THREAD_ID=" + THREAD_ID));
        verify(wrappedWork).handleFailure(e, context);
    }

}
