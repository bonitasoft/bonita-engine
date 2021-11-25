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
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.api.impl.APIAccessorImpl;
import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.authorization.properties.CompoundPermissionsMapping;
import org.bonitasoft.engine.authorization.properties.ResourcesPermissionsMapping;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.page.ContentType;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PermissionServiceImplTest {

    public static final long TENANT_ID = 12L;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private ClassLoaderService classLoaderService;
    @Mock
    private TechnicalLoggerService logger;
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private SessionService sessionService;

    @Mock
    private APIAccessorImpl apiIAccessorImpl;

    private PermissionServiceImpl permissionService;

    @Mock
    private BonitaHomeServer bonitaHomeServer;

    @Mock
    private SSession session;

    @Mock
    private ResourcesPermissionsMapping resourcesPermissionsMapping;

    @Mock
    private CompoundPermissionsMapping compoundPermissionsMapping;

    private File securityFolder;

    @Before
    public void before()
            throws IOException, SClassLoaderException, SSessionNotFoundException, BonitaHomeNotSetException {
        securityFolder = temporaryFolder.newFolder("security");

        doReturn(Thread.currentThread().getContextClassLoader()).when(classLoaderService)
                .getClassLoader(any());
        permissionService = spy(
                new PermissionServiceImpl(classLoaderService, logger, sessionAccessor, sessionService, TENANT_ID,
                        compoundPermissionsMapping, resourcesPermissionsMapping));
        doReturn(bonitaHomeServer).when(permissionService).getBonitaHomeServer();
        doReturn(apiIAccessorImpl).when(permissionService).createAPIAccessorImpl();
        doReturn(session).when(sessionService).getSession(anyLong());

        doReturn(securityFolder).when(bonitaHomeServer).getSecurityScriptsFolder(anyLong());
    }

    @Test
    public void should_start_then_stop_forbid_to_check_api() throws SBonitaException, ClassNotFoundException {
        //given service not started
        permissionService.start();
        permissionService.stop();

        //when
        expectedException.expect(SExecutionException.class);
        expectedException.expectMessage(containsString("not started"));
        permissionService.checkAPICallWithScript("plop", new APICallContext(), false);
    }

    @Test
    public void should_resume_call_start() throws SBonitaException {
        //when
        permissionService.resume();
        //then
        verify(permissionService).start();
    }

    @Test
    public void should_pause_call_stop() throws SBonitaException {
        //given
        permissionService.start();

        //when
        permissionService.pause();

        //then
        verify(permissionService).stop();
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
        expectedException.expect(SExecutionException.class);
        expectedException.expectMessage(containsString("not started"));

        //when
        permissionService.checkAPICallWithScript("plop", new APICallContext(), false);
    }

    @Test
    public void should_checkAPICallWithScript_with_wrong_class() throws SBonitaException, ClassNotFoundException {
        //given
        permissionService.start();
        expectedException.expect(SExecutionException.class);
        expectedException.expectMessage(
                containsString("does not implements org.bonitasoft.engine.api.permission.PermissionRule"));

        //when
        permissionService.checkAPICallWithScript(String.class.getName(), new APICallContext(), false);
    }

    @Test
    public void should_checkAPICallWithScript_run_the_class_in_script_folder()
            throws SBonitaException, ClassNotFoundException, IOException {
        //given
        final String methodBody = "        return true\n";
        FileUtils.writeStringToFile(new File(securityFolder, "MyCustomRule.groovy"), getRuleContent(methodBody),
                Charset.defaultCharset());

        permissionService.start();

        //when
        final boolean myCustomRule = permissionService.checkAPICallWithScript("MyCustomRule", new APICallContext(),
                false);

        assertThat(myCustomRule).isTrue();
        verify(logger).log(argThat(rule -> rule.getName().equals("MyCustomRule")), eq(TechnicalLogSeverity.WARNING),
                eq("Executing my custom rule"));
    }

    @Test
    public void should_checkAPICallWithScript_run_the_class_with_package_in_script_root_folder()
            throws SBonitaException, ClassNotFoundException, IOException {
        //given
        File test = new File(securityFolder, "test");
        test.mkdir();
        FileUtils.writeStringToFile(new File(test, "MyCustomRule.groovy"),
                getRuleContent("test", "        return true\n"), Charset.defaultCharset());

        permissionService.start();

        //when
        final boolean myCustomRule = permissionService.checkAPICallWithScript("test.MyCustomRule", new APICallContext(),
                false);

        assertThat(myCustomRule).isTrue();
        verify(logger).log(argThat(rule -> rule.getName().equals("test.MyCustomRule")),
                eq(TechnicalLogSeverity.WARNING), eq("Executing my custom rule"));
    }

    /*
     * @Test
     * public void perf() throws SBonitaException, ClassNotFoundException, IOException {
     * //given
     * FileUtils.writeStringToFile(new File(scriptFolder, "MyCustomRule.groovy"), "" +
     * "import org.bonitasoft.engine.api.APIAccessor\n" +
     * "import org.bonitasoft.engine.api.Logger\n" +
     * "import org.bonitasoft.engine.api.permission.APICallContext\n" +
     * "import org.bonitasoft.engine.api.permission.PermissionRule\n" +
     * "import org.bonitasoft.engine.session.APISession\n" +
     * "\n" +
     * "class MyCustomRule implements PermissionRule {\n" +
     * "    @Override\n" +
     * "    boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {\n"
     * +
     * "        logger.warning(\"Executing my custom rule\")\n" +
     * "        return true\n" +
     * "    }\n" +
     * "}" +
     * "");
     * permissionService.start();
     * long before = System.nanoTime();
     * //when
     * for (int i = 0; i < 25000; i++) {
     * boolean myCustomRule = permissionService.checkAPICallWithScript("MyCustomRule", new APICallContext(), false);
     * assertThat(myCustomRule).isTrue();
     * }
     * fail("time= "+(System.nanoTime()-before)/250000);
     * }
     */

    @Test
    public void should_checkAPICallWithScript_reload_classes() throws Exception {
        //given
        permissionService.start();
        FileUtils.writeStringToFile(new File(securityFolder, "MyCustomRule.groovy"),
                getRuleContent("        return true\n"), Charset.defaultCharset());

        //when
        boolean myCustomRule = permissionService.checkAPICallWithScript("MyCustomRule", new APICallContext(), true);

        assertThat(myCustomRule).isTrue();
        FileUtils.writeStringToFile(new File(securityFolder, "MyCustomRule.groovy"),
                getRuleContent("        return false\n"), Charset.defaultCharset());

        myCustomRule = permissionService.checkAPICallWithScript("MyCustomRule", new APICallContext(), true);

        assertThat(myCustomRule).isFalse();
        verify(bonitaHomeServer, times(3)).getSecurityScriptsFolder(TENANT_ID);

    }

    @Test
    public void should_checkAPICallWithScript_that_throw_exception() throws Exception {
        //given
        FileUtils.writeStringToFile(new File(securityFolder, "MyCustomRule.groovy"),
                getRuleContent("        throw new RuntimeException()\n"),
                Charset.defaultCharset());

        permissionService.start();

        expectedException.expect(SExecutionException.class);
        expectedException.expectCause(CoreMatchers.<Throwable> instanceOf(RuntimeException.class));
        //when
        permissionService.checkAPICallWithScript("MyCustomRule", new APICallContext(), false);

        //then
        verify(bonitaHomeServer).getSecurityScriptsFolder(TENANT_ID);
    }

    @Test
    public void should_checkAPICallWithScript_with_unknown_class() throws SBonitaException, ClassNotFoundException {
        //given
        permissionService.start();
        expectedException.expect(ClassNotFoundException.class);

        //when
        permissionService.checkAPICallWithScript("plop", new APICallContext(), false);
    }

    @Test
    public void should_isAllowed_return_true_if_static_authorized() throws Exception {
        //given
        final List<String> resourcePermissions = List.of("CasePermission", "AnOtherPermission");
        returnPermissionsFor("GET", "bpm", "case", null, resourcePermissions);
        returnUserPermissionsFromSession("MyPermission", "AnOtherPermission");

        //when
        final boolean isAuthorized = permissionService.isAuthorized(new APICallContext("GET", "bpm", "case", null),
                false);

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
        final boolean isAuthorized = permissionService.isAuthorized(new APICallContext("GET", "bpm", "case", null),
                false);

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
        final boolean isAuthorized = permissionService.isAuthorized(new APICallContext("GET", "bpm", "case", "12"),
                false);

        //then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    public void should_isAllowed_return_true_on_resource_with_id() throws Exception {
        //given
        returnPermissionsFor("GET", "bpm", "case", List.of("12"), List.of("CasePermission", "MyPermission"));
        returnUserPermissionsFromSession("MyPermission", "AnOtherPermission");

        //when
        final boolean isAuthorized = permissionService.isAuthorized(new APICallContext("GET", "bpm", "case", "12"),
                false);

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
        final boolean isAuthorized = permissionService.isAuthorized(new APICallContext("GET", "bpm", "case", "12"),
                false);

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
                .isAuthorized(new APICallContext("GET", "bpm", "case", "12/instantiation"), false);

        //then
        assertThat(isAuthorized).isTrue();
    }

    private void returnPermissionsFor(final String method, final String apiName, final String resourceName,
            final List<String> resourceQualifiers,
            final List<String> toBeReturned) {
        if (resourceQualifiers != null) {
            doReturn(new HashSet<>(toBeReturned)).when(resourcesPermissionsMapping).getResourcePermissions(method,
                    apiName, resourceName,
                    resourceQualifiers);
        } else {
            doReturn(new HashSet<>(toBeReturned)).when(resourcesPermissionsMapping).getResourcePermissions(method,
                    apiName, resourceName);
        }
    }

    private String getRuleContent(String methodBody) {
        return getRuleContent(null, methodBody);
    }

    private String getRuleContent(String packageName, String methodBody) {
        StringBuilder content = new StringBuilder();
        if (packageName != null) {
            content.append("package ").append(packageName).append(";\n");

        }
        content.append("import org.bonitasoft.engine.api.APIAccessor\n")
                .append("import org.bonitasoft.engine.api.Logger\n")
                .append("import org.bonitasoft.engine.api.permission.APICallContext\n")
                .append("import org.bonitasoft.engine.api.permission.PermissionRule\n")
                .append("import org.bonitasoft.engine.session.APISession\n")
                .append("\n")
                .append("class MyCustomRule implements PermissionRule {\n")
                .append("    @Override\n")
                .append("    boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {\n")
                .append("        logger.warning(\"Executing my custom rule\")\n")
                .append(methodBody)
                .append("    }\n")
                .append("}");
        return content.toString();
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
        verify(resourcesPermissionsMapping).setProperty("GET|extension/restApiGet", "[permission1]");
        verify(resourcesPermissionsMapping).setProperty("POST|extension/restApiPost", "[permission2,permission3]");
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
        verify(resourcesPermissionsMapping).removeProperty("GET|extension/restApiGet");
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
        verify(compoundPermissionsMapping).removeProperty("custompage_page44");
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
        verify(compoundPermissionsMapping).setPropertyAsSet(eq("myPage"), anySet());
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
}
