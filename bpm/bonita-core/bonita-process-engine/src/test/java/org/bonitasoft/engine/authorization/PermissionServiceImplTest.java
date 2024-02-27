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
package org.bonitasoft.engine.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.anyString;

import java.io.IOException;
import java.util.*;

import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.authorization.properties.CompoundPermissionsMapping;
import org.bonitasoft.engine.authorization.properties.CustomPermissionsMapping;
import org.bonitasoft.engine.authorization.properties.DynamicPermissionsChecks;
import org.bonitasoft.engine.authorization.properties.ResourcesPermissionsMapping;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.page.ContentType;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PermissionServiceImplTest {

    public static final long TENANT_ID = 12L;

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Mock
    private ClassLoaderService classLoaderService;

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private SessionService sessionService;

    private PermissionServiceImpl permissionService;

    @Mock
    private SSession session;

    @Mock
    private ResourcesPermissionsMapping resourcesPermissionsMapping;

    @Mock
    private CompoundPermissionsMapping compoundPermissionsMapping;

    @Mock
    private CustomPermissionsMapping customPermissionsMapping;

    @Mock
    private DynamicPermissionsChecks dynamicPermissionsChecks;

    @Before
    public void before()
            throws IOException, SClassLoaderException, SSessionNotFoundException, BonitaHomeNotSetException {

        doReturn(Thread.currentThread().getContextClassLoader()).when(classLoaderService)
                .getClassLoader(any());
        permissionService = spy(
                new PermissionServiceImpl(classLoaderService, sessionAccessor, sessionService, TENANT_ID,
                        compoundPermissionsMapping, resourcesPermissionsMapping, customPermissionsMapping,
                        dynamicPermissionsChecks, true));
        doReturn(session).when(sessionService).getSession(anyLong());
    }

    @Test
    public void should_start_then_stop_forbid_to_check_api() throws SBonitaException, ClassNotFoundException {
        //given service not started
        permissionService.start();
        permissionService.stop();

        //when
        Throwable throwable = assertThrows(SExecutionException.class,
                () -> permissionService.checkAPICallWithScript("plop", new APICallContext()));
        assertThat(throwable.getMessage()).contains("not started");
    }

    @Test
    public void should_pause_call_stop_tow_times() {
        //when
        permissionService.stop();
        permissionService.stop();
    }

    @Test
    public void should_checkAPICallWithScript_throw_exception_if_not_started()
            throws SExecutionException, ClassNotFoundException {
        //given service not started

        //when
        Throwable throwable = assertThrows(SExecutionException.class,
                () -> permissionService.checkAPICallWithScript("plop", new APICallContext()));
        assertThat(throwable.getMessage()).contains("not started");
    }

    @Test
    public void should_checkAPICallWithScript_with_wrong_class() throws SBonitaException, ClassNotFoundException {
        //given
        permissionService.start();

        //when
        Throwable throwable = assertThrows(SExecutionException.class,
                () -> permissionService.checkAPICallWithScript(String.class.getName(), new APICallContext()));
        assertThat(throwable.getMessage())
                .contains("does not implements org.bonitasoft.engine.api.permission.PermissionRule");
    }

    @Test
    public void should_checkAPICallWithScript_with_unknown_class() throws SBonitaException, ClassNotFoundException {
        //given
        permissionService.start();

        //when
        assertThrows(ClassNotFoundException.class,
                () -> permissionService.checkAPICallWithScript("plop", new APICallContext()));
    }

    @Test
    public void should_isAllowed_return_true_if_static_authorized() throws Exception {
        //given
        final List<String> resourcePermissions = List.of("CasePermission", "AnOtherPermission");
        returnPermissionsFor("GET", "bpm", "case", null, resourcePermissions);
        returnUserPermissionsFromSession("MyPermission", "AnOtherPermission");

        //when
        final boolean isAuthorized = permissionService.isAuthorized(new APICallContext("GET", "bpm", "case", null));

        //then
        assertThat(isAuthorized).isTrue();
    }

    private void returnUserPermissionsFromSession(String... sessionUserPermissions) {
        doReturn(Set.of(sessionUserPermissions)).when(session).getUserPermissions();
    }

    @Test
    public void should_isAllowed_return_false_if_static_not_authorized() throws Exception {
        //given
        final List<String> resourcePermissions = List.of("CasePermission", "SecondPermission");
        returnPermissionsFor("GET", "bpm", "case", null, resourcePermissions);
        returnUserPermissionsFromSession("MyPermission", "AnOtherPermission");

        //when
        final boolean isAuthorized = permissionService.isAuthorized(new APICallContext("GET", "bpm", "case", null));

        //then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    public void should_isAllowed_return_false_on_resource_with_id_even_if_permission_in_general_is_there()
            throws Exception {
        //given
        returnPermissionsFor("GET", "bpm", "case", null, List.of("CasePermission", "AnOtherPermission"));
        returnPermissionsFor("GET", "bpm", "case", List.of("12"), List.of("CasePermission", "SecondPermission"));
        returnUserPermissionsFromSession("MyPermission", "AnOtherPermission");

        //when
        final boolean isAuthorized = permissionService.isAuthorized(new APICallContext("GET", "bpm", "case", "12"));

        //then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    public void should_isAllowed_return_true_on_resource_with_id() throws Exception {
        //given
        returnPermissionsFor("GET", "bpm", "case", List.of("12"), List.of("CasePermission", "MyPermission"));
        returnUserPermissionsFromSession("MyPermission", "AnOtherPermission");

        //when
        final boolean isAuthorized = permissionService.isAuthorized(new APICallContext("GET", "bpm", "case", "12"));

        //then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_isAllowed_for_resource_with_id_check_parent_if_no_rule() throws Exception {
        //given
        final List<String> resourcePermissions = List.of("CasePermission", "MyPermission");
        returnPermissionsFor("GET", "bpm", "case", null, resourcePermissions);
        returnUserPermissionsFromSession("MyPermission", "AnOtherPermission");

        //when
        final boolean isAuthorized = permissionService.isAuthorized(new APICallContext("GET", "bpm", "case", "12"));

        //then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_isAllowed_work_on_resource_with_wildcard() throws Exception {
        //given
        final List<String> resourcePermissions = List.of("CasePermission", "MyPermission");
        doReturn(new HashSet<>(resourcePermissions)).when(resourcesPermissionsMapping)
                .getResourcePermissionsWithWildCard("GET", "bpm", "case",
                        List.of("12", "instantiation"));
        returnUserPermissionsFromSession("MyPermission", "AnOtherPermission");

        //when
        final boolean isAuthorized = permissionService
                .isAuthorized(new APICallContext("GET", "bpm", "case", "12/instantiation"));

        //then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    public void isAuthorized_should_call_script_when_declared() throws Exception {
        returnDynamicPermissionsFor("GET", "bpm", "case", null, List.of("check|className"));
        final APICallContext apiCallContext = new APICallContext("GET", "bpm", "case", null, "", "");
        doReturn(true).when(permissionService).checkAPICallWithScript("className", apiCallContext);

        final boolean isAuthorized = permissionService.isAuthorized(apiCallContext);

        assertThat(isAuthorized).isTrue();
        verify(permissionService).checkAPICallWithScript("className", apiCallContext);
    }

    @Test
    public void isAuthorized_should_return_false_when_script_returns_false() throws Exception {
        returnDynamicPermissionsFor("GET", "bpm", "case", null, List.of("check|className"));
        final APICallContext apiCallContext = new APICallContext("GET", "bpm", "case", null, "", "");
        doReturn(false).when(permissionService).checkAPICallWithScript("className", apiCallContext);

        final boolean isAuthorized = permissionService.isAuthorized(apiCallContext);

        assertThat(isAuthorized).isFalse();
        verify(permissionService).checkAPICallWithScript("className", apiCallContext);
    }

    @Test
    public void isAuthorized_should_return_false_when_script_execution_fails() throws Exception {
        returnDynamicPermissionsFor("GET", "bpm", "case", null, List.of("check|className"));
        final APICallContext apiCallContext = new APICallContext("GET", "bpm", "case", null, "", "");
        doThrow(ClassNotFoundException.class).when(permissionService).checkAPICallWithScript("className",
                apiCallContext);

        assertThrows(SExecutionException.class,
                () -> permissionService.isAuthorized(apiCallContext));
    }

    @Test
    public void isAuthorized_should_check_static_permissions_if_no_script_declared() throws Exception {
        //given
        doReturn(false).when(permissionService).isAuthorizedByStaticPermissions(any(APICallContext.class));
        final APICallContext apiCallContext = new APICallContext("GET", "bpm", "case", null, "", "");

        // when:
        final boolean authorized = permissionService.isAuthorized(apiCallContext);

        // then:
        assertThat(authorized).isFalse();
        verify(permissionService).isAuthorizedByStaticPermissions(eq(apiCallContext));
    }

    @Test
    public void isAuthorized_should_return_true_with_profile_check() throws Exception {
        final String sessionUserPermissions = "profile|admin";
        returnPermissionsFor("GET", "bpm", "case", null, List.of(sessionUserPermissions, "check|className"));
        final APICallContext apiCallContext = new APICallContext("GET", "bpm", "case", null, "", "");
        returnUserPermissionsFromSession(sessionUserPermissions);

        final boolean isAuthorized = permissionService.isAuthorized(apiCallContext);

        assertThat(isAuthorized).isTrue();
        verify(permissionService, never()).checkDynamicPermissionsWithScript(any(), any());
    }

    @Test
    public void isAuthorized_should_return_true_with_user_check() throws Exception {
        // given
        final String userPermissions = "user|Juan-Carlos";
        returnPermissionsFor("GET", "bpm", "case", null, List.of(userPermissions, "check|className"));
        returnUserPermissionsFromSession(userPermissions);

        // when
        final boolean isAuthorized = permissionService.isAuthorized(
                new APICallContext("GET", "bpm", "case", null, "", ""));

        // then
        assertThat(isAuthorized).isTrue();
        verify(permissionService, never()).checkDynamicPermissionsWithScript(any(), anyString());
    }

    @Test
    public void isAuthorized_should_work_with_default_rule() throws Exception {
        // given
        long userId = 10L;
        doReturn(userId).when(session).getUserId();
        final List<String> dynamicAuthorizations = List.of("check|org.bonitasoft.permissions.UserPermissionRule");
        returnDynamicPermissionsFor("GET", "identity", "user", null, dynamicAuthorizations);

        // when
        permissionService.start();
        final boolean isAuthorizedOnAllUsers = permissionService.isAuthorized(
                new APICallContext("GET", "identity", "user", null, "", ""));

        // then
        assertThat(isAuthorizedOnAllUsers).isFalse();

        // when
        final boolean isAuthorizedOnCurrentUser = permissionService.isAuthorized(
                new APICallContext("GET", "identity", "user", String.valueOf(userId), "", ""));

        // then
        assertThat(isAuthorizedOnCurrentUser).isTrue();
    }

    @Test
    public void isAuthorized_should_throw_exception_if_the_script_is_not_found() throws Exception {
        returnDynamicPermissionsFor("GET", "bpm", "case", null, List.of("check|className"));
        final APICallContext apiCallContext = new APICallContext("GET", "bpm", "case", null, "", "");
        doThrow(ClassNotFoundException.class).when(permissionService).checkAPICallWithScript("className",
                apiCallContext);

        assertThrows(SExecutionException.class,
                () -> permissionService.isAuthorized(apiCallContext));
    }

    @Test
    public void isAuthorized_should_throw_exception_if_syntax_is_invalid() {
        returnDynamicPermissionsFor("GET", "bpm", "case", null, List.of("anyText"));
        final APICallContext apiCallContext = new APICallContext("GET", "bpm", "case", null, "", "");

        assertThrows(IllegalArgumentException.class,
                () -> permissionService.isAuthorized(apiCallContext));
    }

    @Test
    public void checkResourceAuthorizationsSyntax_should_throw_exception_if_syntax_is_invalid() {
        final Set<String> resourceAuthorizations = new HashSet<>();
        resourceAuthorizations.add("any string");

        assertThrows(IllegalArgumentException.class,
                () -> permissionService.checkResourceAuthorizationsSyntax(resourceAuthorizations));
    }

    @Test
    public void checkResourceAuthorizationsSyntax_should_not_throw_exception_if_syntax_is_valid() {
        final Set<String> resourceAuthorizations = Set.of("user|any.username", "profile|any.profile",
                "check|className");

        permissionService.checkResourceAuthorizationsSyntax(resourceAuthorizations);

        // no exception
    }

    @Test
    public void should_checkPermissions_return_true_if_dynamic_authorized() throws Exception {
        //given
        final List<String> dynamicAuthorizations = List.of("check|className");
        returnDynamicPermissionsFor("GET", "bpm", "case", null, dynamicAuthorizations);
        final APICallContext apiCallContext = new APICallContext("GET", "bpm", "case", null, "", "");
        doReturn(true).when(permissionService).isAuthorizedByDynamicPermissions(eq(apiCallContext),
                anySet(), anySet());

        //when
        final boolean isAuthorized = permissionService.isAuthorized(apiCallContext);

        //then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_checkPermissions_return_false_if_dynamic_not_authorized() throws Exception {
        //given
        final List<String> dynamicAuthorizations = List.of("check|className");
        returnDynamicPermissionsFor("GET", "bpm", "case", null, dynamicAuthorizations);
        final APICallContext apiCallContext = new APICallContext("GET", "bpm", "case", null, "", "");
        doReturn(false).when(permissionService).isAuthorizedByDynamicPermissions(eq(apiCallContext),
                anySet(), anySet());

        //when
        final boolean isAuthorized = permissionService.isAuthorized(apiCallContext);

        //then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    public void should_not_return_resource_when_unknown_resource_is_declared_in_PageProperties() {
        // Given
        doReturn(Collections.emptySet()).when(resourcesPermissionsMapping).getPropertyAsSet("GET|unknown/resource");
        doReturn(Set.of("Organization Visualization", "Organization Management"))
                .when(resourcesPermissionsMapping).getPropertyAsSet("PUT|identity/user");

        // When
        final Set<String> customPagePermissions = permissionService.getCustomPagePermissions(
                "[GET|unknown/resource, PUT|identity/user]", resourcesPermissionsMapping);

        // Then
        assertThat(customPagePermissions).containsOnly("Organization Visualization", "Organization Management");
    }

    @Test
    public void should_add_api_extension_permissions() {
        //given
        Properties properties = new Properties();
        properties.put(PermissionServiceImpl.PROPERTY_CONTENT_TYPE, ContentType.API_EXTENSION);
        properties.put(PermissionServiceImpl.PROPERTY_API_EXTENSIONS, "restResource1,restResource2");
        properties.put("restResource1.method", "GET");
        properties.put("restResource1.pathTemplate", "restApiGet");
        properties.put("restResource1.permissions", "permission1");
        properties.put("restResource2.method", "POST");
        properties.put("restResource2.pathTemplate", "restApiPost");
        properties.put("restResource2.permissions", "permission2,permission3");

        //when
        permissionService.addRestApiExtensionPermissions(resourcesPermissionsMapping, properties);

        //then
        verify(resourcesPermissionsMapping).setInternalProperty("GET|extension/restApiGet", "[permission1]");
        verify(resourcesPermissionsMapping).setInternalProperty("POST|extension/restApiPost",
                "[permission2,permission3]");
    }

    @Test
    public void should_add_api_extension_permissions_for_absolute_pathTemplate() {
        //given
        Properties properties = new Properties();
        properties.put(PermissionServiceImpl.PROPERTY_CONTENT_TYPE, ContentType.API_EXTENSION);
        properties.put(PermissionServiceImpl.PROPERTY_API_EXTENSIONS, "restResource1,restResource2");
        properties.put("restResource1.method", "GET");
        properties.put("restResource1.pathTemplate", "/restApiGet");
        properties.put("restResource1.permissions", "permission1");

        //when
        permissionService.addRestApiExtensionPermissions(resourcesPermissionsMapping, properties);

        //then
        verify(resourcesPermissionsMapping).setInternalProperty("GET|extension/restApiGet", "[permission1]");
    }

    @Test
    public void should_not_add_non_api_extension_permissions_to_resource_permission_mapping() {
        //given
        Properties properties = new Properties();
        properties.put(PermissionServiceImpl.PROPERTY_CONTENT_TYPE, "page");
        properties.put(PermissionServiceImpl.PROPERTY_API_EXTENSIONS, "restResource1");
        properties.put("restResource1.method", "GET");
        properties.put("restResource1.pathTemplate", "restApiGet");
        properties.put("restResource1.permissions", "permission1");

        //when
        permissionService.addRestApiExtensionPermissions(resourcesPermissionsMapping, properties);

        //then
        verifyNoInteractions(resourcesPermissionsMapping);
    }

    @Test
    public void removePermissions_should_remove_all_rest_api_keys_from_properties() {
        // given:
        Properties properties = new Properties();
        properties.put(PermissionServiceImpl.PROPERTY_CONTENT_TYPE, ContentType.API_EXTENSION);
        properties.put(PermissionServiceImpl.PROPERTY_API_EXTENSIONS, "restResource");
        properties.put("restResource.method", "GET");
        properties.put("restResource.pathTemplate", "restApiGet");
        properties.put("restResource.permissions", "permission");

        // when:
        permissionService.removePermissions(properties);

        // then:
        verify(resourcesPermissionsMapping).removeInternalProperty("GET|extension/restApiGet");
    }

    @Test
    public void removePermissions_should_remove_key_from_compound_permission_mappings() {
        // given:
        Properties properties = new Properties();
        properties.put("name", "custompage_page44");
        properties.put(PermissionServiceImpl.PROPERTY_CONTENT_TYPE, ContentType.API_EXTENSION);
        properties.put(PermissionServiceImpl.PROPERTY_API_EXTENSIONS, "restResource");
        properties.put("restResource.method", "GET");
        properties.put("restResource.pathTemplate", "restApiGet");
        properties.put("restResource.permissions", "permission");

        // when:
        permissionService.removePermissions(properties);

        // then:
        verify(compoundPermissionsMapping).removeInternalProperty("custompage_page44");
    }

    @Test
    public void addPermissions_should_update_compound_permissions_for_pages() {
        // given:
        Properties properties = new Properties();
        properties.put("name", "myPage");
        properties.put(PermissionServiceImpl.PROPERTY_CONTENT_TYPE, ContentType.PAGE);
        properties.put(PermissionServiceImpl.RESOURCES_PROPERTY, "[]");

        // when:
        permissionService.addPermissions("myPage", properties);

        // then:
        verify(compoundPermissionsMapping).setInternalPropertyAsSet(eq("myPage"), anySet());
    }

    @Test
    public void addPermissions_should_update_compound_permissions_for_layouts() {
        // given:
        Properties properties = new Properties();
        properties.put("name", "myLayout");
        properties.put(PermissionServiceImpl.PROPERTY_CONTENT_TYPE, ContentType.LAYOUT);
        properties.put(PermissionServiceImpl.RESOURCES_PROPERTY, "[]");

        // when:
        permissionService.addPermissions("myLayout", properties);

        // then:
        verify(compoundPermissionsMapping).setInternalPropertyAsSet(eq("myLayout"), anySet());
    }

    @Test
    public void addPermissions_should_not_update_compound_permissions_for_forms() {
        // given:
        Properties properties = new Properties();
        properties.put("name", "myForm");
        properties.put(PermissionServiceImpl.PROPERTY_CONTENT_TYPE, ContentType.FORM);
        properties.put(PermissionServiceImpl.RESOURCES_PROPERTY, "[]");

        // when:
        permissionService.addPermissions("myForm", properties);

        // then:
        verify(compoundPermissionsMapping, never()).setPropertyAsSet(eq("myForm"), anySet());
    }

    @Test
    public void should_return_permissions_associated_with_the_resource() {
        //given
        final List<String> resourcePermissions = List.of("CasePermission", "MyPermission");
        doReturn(new HashSet<>(resourcePermissions)).when(resourcesPermissionsMapping)
                .getPropertyAsSet("GET|bpm/case");

        //then
        assertThat(permissionService.getResourcePermissions("GET|bpm/case")).containsExactlyInAnyOrder("CasePermission",
                "MyPermission");
    }

    @Rule
    public RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public EnvironmentVariables envVar = new EnvironmentVariables();

    @Test
    public void initDynamicPermissionsEnabledProperty_should_take_System_property_if_set() {
        // given:
        System.setProperty("bonita.runtime.authorization.dynamic-check.enabled", "false");
        envVar.set("bonita.runtime.authorization.dynamic-check.enabled", "true");

        // when:
        final boolean property = permissionService.initDynamicPermissionsEnabledProperty(true).isEnabled();

        // then:
        assertThat(property).isFalse();
    }

    @Test
    public void initDynamicPermissionsEnabledProperty_should_take_envVar_if_no_System_property_if_set() {
        // given:
        envVar.set("BONITA_RUNTIME_AUTHORIZATION_DYNAMICCHECK_ENABLED", "false");

        // when:
        final boolean property = permissionService.initDynamicPermissionsEnabledProperty(true).isEnabled();

        // then:
        assertThat(property).isFalse();
    }

    @Test
    public void initDynamicPermissionsEnabledProperty_should_take_file_property_if_no_System__nor_env_property_if_set() {
        // when:
        final boolean property = permissionService.initDynamicPermissionsEnabledProperty(false).isEnabled();

        // then:
        assertThat(property).isFalse();
    }

    private void returnPermissionsFor(final String method, final String apiName, final String resourceName,
            final List<String> resourceQualifiers,
            final List<String> toBeReturned) {
        returnPermissionsFor(method, apiName, resourceName, resourceQualifiers, toBeReturned,
                resourcesPermissionsMapping);
    }

    private void returnDynamicPermissionsFor(final String method, final String apiName, final String resourceName,
            final List<String> resourceQualifiers,
            final List<String> toBeReturned) {
        returnPermissionsFor(method, apiName, resourceName, resourceQualifiers, toBeReturned, dynamicPermissionsChecks);
    }

    private void returnPermissionsFor(final String method, final String apiName, final String resourceName,
            final List<String> resourceQualifiers,
            final List<String> toBeReturned, final ResourcesPermissionsMapping resourcesPermissionsMapping) {
        if (resourceQualifiers != null) {
            doReturn(new HashSet<>(toBeReturned)).when(resourcesPermissionsMapping).getResourcePermissions(method,
                    apiName, resourceName,
                    resourceQualifiers);
        } else {
            doReturn(new HashSet<>(toBeReturned)).when(resourcesPermissionsMapping).getResourcePermissions(method,
                    apiName, resourceName);
        }
    }
}
