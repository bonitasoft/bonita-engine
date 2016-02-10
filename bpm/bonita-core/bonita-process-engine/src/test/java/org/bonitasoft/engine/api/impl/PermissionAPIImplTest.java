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
package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.service.PermissionService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PermissionAPIImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private TenantServiceAccessor tenantServiceAccessor;
    @Mock
    private PermissionService permissionService;
    private PermissionAPIImpl permissionAPI;
    private APICallContext apiCallContext;

    @Before
    public void before() {
        permissionAPI = spy(new PermissionAPIImpl());
        doReturn(tenantServiceAccessor).when(permissionAPI).getTenantServiceAccessor();
        doReturn(permissionService).when(tenantServiceAccessor).getPermissionService();
        apiCallContext = new APICallContext("GET", "identity", "user", "1", "query", "{\"body\":\"value\"}");
    }

    @Test
    public void should_executeSecurityScript_call_the_service() throws Exception {
        //given all ok
        doReturn(true).when(permissionService).checkAPICallWithScript("myScript", apiCallContext, false);

        //when
        final boolean isAllowed = permissionAPI.checkAPICallWithScript("myScript", apiCallContext, false);

        //then
        assertThat(isAllowed).isTrue();
    }

    @Test
    public void should_executeSecurityScript_throw_not_found_exception_when_file_does_not_exists() throws Exception {
        //given
        doThrow(ClassNotFoundException.class).when(permissionService).checkAPICallWithScript("myScript", apiCallContext, false);

        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(containsString("the class myScript is not found"));
        //when
        permissionAPI.checkAPICallWithScript("myScript", apiCallContext, false);
    }

    @Test
    public void should_executeSecurityScript_throw_execution_exception() throws Exception {
        //given
        doThrow(SExecutionException.class).when(permissionService).checkAPICallWithScript("myScript", apiCallContext, false);

        expectedException.expect(ExecutionException.class);
        //when
        permissionAPI.checkAPICallWithScript("myScript", apiCallContext, false);
    }
}
