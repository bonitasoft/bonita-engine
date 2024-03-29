/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.authorization.PermissionService;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PermissionAPIImplTest {

    @Mock
    private ServiceAccessor serviceAccessor;
    @Mock
    private PermissionService permissionService;
    private PermissionAPIImpl permissionAPI;
    private APICallContext apiCallContext;

    @Before
    public void before() {
        permissionAPI = spy(new PermissionAPIImpl());
        doReturn(serviceAccessor).when(permissionAPI).getServiceAccessor();
        doReturn(permissionService).when(serviceAccessor).getPermissionService();
        apiCallContext = new APICallContext("GET", "identity", "user", "1", "query", "{\"body\":\"value\"}");
    }

    @Test
    public void should_isAuthorized_throw_execution_exception() throws Exception {
        //given
        final APICallContext apiCallContext = new APICallContext("GET", "bpm", "case", null, "", "");
        doThrow(SExecutionException.class).when(permissionService).isAuthorized(apiCallContext);

        assertThrows(ExecutionException.class,
                () -> permissionAPI.isAuthorized(apiCallContext));
    }
}
