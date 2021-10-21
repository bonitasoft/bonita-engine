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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.service.PermissionService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PermissionAPIImplTest {

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
        doThrow(ClassNotFoundException.class).when(permissionService).checkAPICallWithScript("myScript", apiCallContext,
                false);

        final String message = assertThrows(NotFoundException.class,
                () -> permissionAPI.checkAPICallWithScript("myScript", apiCallContext, false)).getMessage();
        assertThat(message).contains("the class myScript is not found");
    }

    @Test
    public void should_executeSecurityScript_throw_execution_exception() throws Exception {
        //given
        doThrow(SExecutionException.class).when(permissionService).checkAPICallWithScript("myScript", apiCallContext,
                false);

        assertThrows(ExecutionException.class,
                () -> permissionAPI.checkAPICallWithScript("myScript", apiCallContext, false));
    }

    @Test
    public void dynamicCheck_authorized_with_script() throws Exception {
        final Set<String> dynamicAuthorizations = new HashSet<>(List.of("check|className"));
        final APICallContext apiCallContext = new APICallContext("GET", "bpm", "case", null, "", "");
        doReturn(true).when(permissionAPI).checkAPICallWithScript("className", apiCallContext, false);

        final boolean isAuthorized = permissionAPI.isAuthorized(apiCallContext, false, new HashSet<>(),
                dynamicAuthorizations);

        assertThat(isAuthorized).isTrue();
    }

    @Test
    public void isAuthorized_should_return_false_if_no_script_declared() {
        // when:
        final boolean authorized = permissionAPI.isAuthorized(apiCallContext, false, new HashSet<>(), new HashSet<>());

        // then:
        assertThat(authorized).isFalse();
    }

    //    Maybe move those tests when code is refactored in PermissionService:

    //    @Test
    //    public void test_dynamicCheck_authorized_with_profile() {
    //        final Set<String> dynamicAuthorizations = new HashSet<>(Arrays.asList("profile|admin", "check|className"));
    //        final APICallContext apiCallContext = new APICallContext("GET", "bpm", "case", null, "", "");
    //
    //        final boolean isAuthorized = permissionAPI.dynamicCheck(new APICallContext("GET", "bpm", "case", null, "", ""),
    //                new HashSet<>(List.of("profile|admin")),
    //                dynamicAuthorizations);
    //
    //        assertThat(isAuthorized).isTrue();
    //        verify(permissionAPI, never()).executeScript(apiSession, "className", apiCallContext);
    //    }
    //
    //    @Test
    //    public void test_dynamicCheck_authorized_with_user() {
    //        final Set<String> dynamicAuthorizations = new HashSet<>(Arrays.asList("user|" + apiSession.getUserName(), "check|className"));
    //        final RestAPIAuthorizationFilter restAPIAuthorizationFilterSpy = spy(restAPIAuthorizationFilter);
    //        final APICallContext apiCallContext = new APICallContext("GET", "bpm", "case", null, "", "");
    //
    //        final boolean isAuthorized = restAPIAuthorizationFilterSpy.dynamicCheck(new APICallContext("GET", "bpm", "case", null, "", ""),
    //                new HashSet<>(List.of("user|" + apiSession.getUserName())),
    //                dynamicAuthorizations, apiSession);
    //
    //        assertThat(isAuthorized).isTrue();
    //        verify(restAPIAuthorizationFilterSpy, never()).executeScript(apiSession, "className", apiCallContext);
    //    }
    //
    //    @Test
    //    public void test_dynamicCheck_unauthorized_with_script() {
    //        final Set<String> dynamicAuthorizations = new HashSet<>(List.of("check|className"));
    //        final RestAPIAuthorizationFilter restAPIAuthorizationFilterSpy = spy(restAPIAuthorizationFilter);
    //        final APICallContext apiCallContext = new APICallContext("GET", "bpm", "case", null, "", "");
    //        doReturn(false).when(restAPIAuthorizationFilterSpy).executeScript(apiSession, "className", apiCallContext);
    //
    //        final boolean isAuthorized = restAPIAuthorizationFilterSpy.dynamicCheck(new APICallContext("GET", "bpm", "case", null, "", ""), new HashSet<>(),
    //                dynamicAuthorizations, apiSession);
    //
    //        assertThat(isAuthorized).isFalse();
    //    }
    //
    //    @Test
    //    public void should_dynamicCheck_return_false_if_the_script_execution_fails() {
    //        final Set<String> dynamicAuthorizations = new HashSet<>(List.of("check|className"));
    //        final RestAPIAuthorizationFilter restAPIAuthorizationFilterSpy = spy(restAPIAuthorizationFilter);
    //        final APICallContext apiCallContext = new APICallContext("GET", "bpm", "case", null, "", "");
    //        doThrow(ExecutionException.class).when(restAPIAuthorizationFilterSpy).executeScript(apiSession, "className", apiCallContext);
    //
    //        final boolean isAuthorized = restAPIAuthorizationFilterSpy.dynamicCheck(new APICallContext("GET", "bpm", "case", null, "", ""), new HashSet<>(),
    //                dynamicAuthorizations, apiSession);
    //
    //        assertThat(isAuthorized).isFalse();
    //    }
    //
    //    @Test
    //    public void should_dynamicCheck_return_false_if_the_script_is_not_found() {
    //        final Set<String> dynamicAuthorizations = new HashSet<>(List.of("check|className"));
    //        final RestAPIAuthorizationFilter restAPIAuthorizationFilterSpy = spy(restAPIAuthorizationFilter);
    //        final APICallContext apiCallContext = new APICallContext("GET", "bpm", "case", null, "", "");
    //        doThrow(NotFoundException.class).when(restAPIAuthorizationFilterSpy).executeScript(apiSession, "className", apiCallContext);
    //
    //        final boolean isAuthorized = restAPIAuthorizationFilterSpy.dynamicCheck(new APICallContext("GET", "bpm", "case", null, "", ""), new HashSet<>(),
    //                dynamicAuthorizations, apiSession);
    //
    //        assertThat(isAuthorized).isFalse();
    //    }
    //
    //    @Test
    //    public void should_dynamicCheck_return_false_if_the_script_syntax_is_invalid() {
    //        final Set<String> dynamicAuthorizations = new HashSet<>(List.of("anyText"));
    //        final RestAPIAuthorizationFilter restAPIAuthorizationFilterSpy = spy(restAPIAuthorizationFilter);
    //        final APICallContext apiCallContext = new APICallContext("GET", "bpm", "case", null, "", "");
    //        doThrow(NotFoundException.class).when(restAPIAuthorizationFilterSpy).executeScript(apiSession, "className", apiCallContext);
    //
    //        final boolean isAuthorized = restAPIAuthorizationFilterSpy.dynamicCheck(new APICallContext("GET", "bpm", "case", null, "", ""), new HashSet<>(),
    //                dynamicAuthorizations, apiSession);
    //
    //        assertThat(isAuthorized).isFalse();
    //    }
    //
    //    @Test
    //    public void checkResourceAuthorizationsSyntax_should_return_false_if_syntax_is_invalid() {
    //        final RestAPIAuthorizationFilter restAPIAuthorizationFilterSpy = spy(restAPIAuthorizationFilter);
    //        final Set<String> resourceAuthorizations = new HashSet<>();
    //        resourceAuthorizations.add("any string");
    //
    //        final boolean isValid = restAPIAuthorizationFilterSpy.checkResourceAuthorizationsSyntax(resourceAuthorizations);
    //
    //        assertThat(isValid).isFalse();
    //    }
    //
    //    @Test
    //    public void checkResourceAuthorizationsSyntax_should_return_true_if_syntax_is_valid() {
    //        final RestAPIAuthorizationFilter restAPIAuthorizationFilterSpy = spy(restAPIAuthorizationFilter);
    //        final Set<String> resourceAuthorizations = new HashSet<>(Arrays.asList("user|any.username", "profile|any.profile", "check|className"));
    //
    //        final boolean isValid = restAPIAuthorizationFilterSpy.checkResourceAuthorizationsSyntax(resourceAuthorizations);
    //
    //        assertThat(isValid).isTrue();
    //    }
}
