/*
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;

import org.bonitasoft.engine.api.permission.APICallContext;
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
    private File scriptFile;
    @Mock
    private TenantServiceAccessor tenantServiceAccessor;
    @Mock
    private PermissionService permissionService;
    private PermissionAPIImpl permissionAPI;
    private APICallContext apiCallContext = new APICallContext("GET","identity","user","1","query","body");

    @Before
    public void before() throws Exception {
        permissionAPI = spy(new PermissionAPIImpl());
        doReturn(tenantServiceAccessor).when(permissionAPI).getTenantServiceAccessor();
        doReturn(true).when(scriptFile).exists();
        doReturn(permissionService).when(tenantServiceAccessor).getPermissionService();
        doReturn(scriptFile).when(permissionAPI).getScriptFile("myScript", tenantServiceAccessor);
        doReturn("the script content").when(permissionAPI).readFile(scriptFile);
    }

    @Test
    public void should_executeSecurityScript_call_the_service() throws Exception {
        //given all ok
        doReturn(true).when(permissionService).checkAPICallWithScript("the script content", apiCallContext);

        //when
        boolean isAllowed = permissionAPI.checkAPICallWithScript("myScript", apiCallContext);

        //then
        verify(permissionService).checkAPICallWithScript("the script content", apiCallContext);
        assertThat(isAllowed).isTrue();
    }

    @Test
    public void should_executeSecurityScript_throw_execution_exception_when_file_does_not_exists() throws Exception {
        //given
        doReturn(false).when(scriptFile).exists();

        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(containsString("file is not found"));
        //when
        permissionAPI.checkAPICallWithScript("myScript", apiCallContext);

        //then
        verifyZeroInteractions(permissionService);
    }
    @Test
    public void should_executeSecurityScript_throw_execution_exception_when_file_cannot_be_read() throws Exception {
        //given
        doThrow(ExecutionException.class).when(permissionAPI).readFile(scriptFile);

        expectedException.expect(ExecutionException.class);
        //when
        permissionAPI.checkAPICallWithScript("myScript", apiCallContext);

        //then
    }

}
