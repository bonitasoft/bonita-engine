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
package org.bonitasoft.engine.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.api.impl.APIAccessorImpl;
import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
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

    private File securityFolder;

    @Before
    public void before() throws IOException, SClassLoaderException, SSessionNotFoundException, BonitaHomeNotSetException {
        securityFolder = temporaryFolder.newFolder("security");

        doReturn(Thread.currentThread().getContextClassLoader()).when(classLoaderService).getLocalClassLoader(anyString(), anyLong());
        permissionService = spy(new PermissionServiceImpl(classLoaderService, logger, sessionAccessor, sessionService, TENANT_ID));
        doReturn(bonitaHomeServer).when(permissionService).getBonitaHomeServer();
        doReturn(apiIAccessorImpl).when(permissionService).createAPIAccessorImpl();
        doReturn(mock(SSession.class)).when(sessionService).getSession(anyLong());

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
    public void should_checkAPICallWithScript_throw_exception_if_not_started() throws SExecutionException, ClassNotFoundException {
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
        expectedException.expectMessage(containsString("does not implements org.bonitasoft.engine.api.permission.PermissionRule"));

        //when
        permissionService.checkAPICallWithScript(String.class.getName(), new APICallContext(), false);
    }

    @Test
    public void should_checkAPICallWithScript_run_the_class_in_script_folder() throws SBonitaException, ClassNotFoundException, IOException {
        //given
        final String methodBody = "        return true\n";
        FileUtils.writeStringToFile(new File(securityFolder, "MyCustomRule.groovy"), getRuleContent(methodBody), Charset.defaultCharset());

        permissionService.start();

        //when
        final boolean myCustomRule = permissionService.checkAPICallWithScript("MyCustomRule", new APICallContext(), false);

        assertThat(myCustomRule).isTrue();
        verify(logger).log(argThat(rule -> rule.getName().equals("MyCustomRule")), eq(TechnicalLogSeverity.WARNING), eq("Executing my custom rule"));
    }

    @Test
    public void should_checkAPICallWithScript_run_the_class_with_package_in_script_root_folder() throws SBonitaException, ClassNotFoundException, IOException {
        //given
        File test = new File(securityFolder, "test");
        test.mkdir();
        FileUtils.writeStringToFile(new File(test, "MyCustomRule.groovy"), getRuleContent("test", "        return true\n"), Charset.defaultCharset());

        permissionService.start();

        //when
        final boolean myCustomRule = permissionService.checkAPICallWithScript("test.MyCustomRule", new APICallContext(), false);

        assertThat(myCustomRule).isTrue();
        verify(logger).log(argThat(rule -> rule.getName().equals("test.MyCustomRule")), eq(TechnicalLogSeverity.WARNING), eq("Executing my custom rule"));
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
     * "    boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {\n" +
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
        FileUtils.writeStringToFile(new File(securityFolder, "MyCustomRule.groovy"), getRuleContent("        return true\n"), Charset.defaultCharset());

        //when
        boolean myCustomRule = permissionService.checkAPICallWithScript("MyCustomRule", new APICallContext(), true);

        assertThat(myCustomRule).isTrue();
        FileUtils.writeStringToFile(new File(securityFolder, "MyCustomRule.groovy"), getRuleContent("        return false\n"), Charset.defaultCharset());

        myCustomRule = permissionService.checkAPICallWithScript("MyCustomRule", new APICallContext(), true);

        assertThat(myCustomRule).isFalse();
        verify(bonitaHomeServer, times(3)).getSecurityScriptsFolder(TENANT_ID);

    }

    @Test
    public void should_checkAPICallWithScript_that_throw_exception() throws Exception {
        //given
        FileUtils.writeStringToFile(new File(securityFolder, "MyCustomRule.groovy"), getRuleContent("        throw new RuntimeException()\n"),
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
                .append("}")
                .append("");
        return content.toString();
    }
}
