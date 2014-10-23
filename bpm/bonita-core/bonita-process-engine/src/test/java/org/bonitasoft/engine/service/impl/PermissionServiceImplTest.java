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

package org.bonitasoft.engine.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PermissionServiceImplTest {

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
    private String scriptFolder;

    private PermissionServiceImpl permissionService;

    @Before
    public void before() throws IOException, SClassLoaderException, SSessionNotFoundException {
        scriptFolder = temporaryFolder.newFolder().getAbsolutePath();
        doReturn(Thread.currentThread().getContextClassLoader()).when(classLoaderService).getLocalClassLoader(anyString(), anyLong());
        permissionService = new PermissionServiceImpl(classLoaderService, logger, sessionAccessor, sessionService, scriptFolder, 1);
        doReturn(mock(SSession.class)).when(sessionService).getSession(anyLong());

    }

    @Test
    public void should_start_then_stop_forbid_to_check_api() throws SBonitaException, ClassNotFoundException {
        //given service not started
        permissionService.start();
        permissionService.stop();

        //when
        expectedException.expect(SExecutionException.class);
        expectedException.expectMessage(containsString("not started"));
        permissionService.checkAPICallWithScript("plop", new APICallContext());
    }

    @Test
    public void should_resume_call_start() throws SBonitaException, ClassNotFoundException {
        //given
        PermissionServiceImpl permissionServiceSpy = spy(permissionService);

        //when
        permissionServiceSpy.resume();
        //then
        verify(permissionServiceSpy).start();
    }

    @Test
    public void should_pause_call_stop() throws SBonitaException, ClassNotFoundException {
        //given
        permissionService.start();
        PermissionServiceImpl permissionServiceSpy = spy(permissionService);

        //when
        permissionServiceSpy.pause();
        //then
        verify(permissionServiceSpy).stop();
    }

    @Test
    public void should_start_with_no_folder_log() throws SBonitaException, ClassNotFoundException, IOException {
        //given
        FileUtils.deleteDirectory(new File(scriptFolder));
        //when
        permissionService.start();
        //then
        verify(logger).log(PermissionServiceImpl.class,TechnicalLogSeverity.INFO,"The security script folder " + scriptFolder
                + " does not exists or is a file, PermissionRules will be loaded only from the tenant classloader");
    }

    @Test
    public void should_checkAPICallWithScript_throw_exception_if_not_started() throws SExecutionException, ClassNotFoundException {
        //given service not started
        expectedException.expect(SExecutionException.class);
        expectedException.expectMessage(containsString("not started"));

        //when
        permissionService.checkAPICallWithScript("plop", new APICallContext());
    }

    @Test
    public void should_checkAPICallWithScript_with_wrong_class() throws SBonitaException, ClassNotFoundException {
        //given
        permissionService.start();
        expectedException.expect(SExecutionException.class);
        expectedException.expectMessage(containsString("does not implements org.bonitasoft.engine.api.permission.PermissionRule"));

        //when
        permissionService.checkAPICallWithScript(String.class.getName(), new APICallContext());
    }

    @Test
    public void should_checkAPICallWithScript_run_the_class_in_script_folder() throws SBonitaException, ClassNotFoundException, IOException {
        //given
        FileUtils.writeStringToFile(new File(scriptFolder, "MyCustomRule.groovy"), "" +
                "import org.bonitasoft.engine.api.APIAccessor\n" +
                "import org.bonitasoft.engine.api.Logger\n" +
                "import org.bonitasoft.engine.api.permission.APICallContext\n" +
                "import org.bonitasoft.engine.api.permission.PermissionRule\n" +
                "import org.bonitasoft.engine.session.APISession\n" +
                "\n" +
                "class MyCustomRule implements PermissionRule {\n" +
                "    @Override\n" +
                "    boolean check(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {\n" +
                "        logger.warning(\"Executing my custom rule\")\n" +
                "        return true\n" +
                "    }\n" +
                "}" +
                "");

        permissionService.start();

        //when
        boolean myCustomRule = permissionService.checkAPICallWithScript("MyCustomRule", new APICallContext());

        assertThat(myCustomRule).isTrue();
        verify(logger).log(argThat(new HasName("MyCustomRule")), eq(TechnicalLogSeverity.WARNING), eq("Executing my custom rule"));
    }


    @Test
    public void should_checkAPICallWithScript_that_throw_exception() throws SBonitaException, ClassNotFoundException, IOException {
        //given
        FileUtils.writeStringToFile(new File(scriptFolder, "MyCustomRule.groovy"), "" +
                "import org.bonitasoft.engine.api.APIAccessor\n" +
                "import org.bonitasoft.engine.api.Logger\n" +
                "import org.bonitasoft.engine.api.permission.APICallContext\n" +
                "import org.bonitasoft.engine.api.permission.PermissionRule\n" +
                "import org.bonitasoft.engine.session.APISession\n" +
                "\n" +
                "class MyCustomRule implements PermissionRule {\n" +
                "    @Override\n" +
                "    boolean check(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {\n" +
                "        throw new RuntimeException()\n" +
                "    }\n" +
                "}" +
                "");

        permissionService.start();


        expectedException.expect(SExecutionException.class);
        expectedException.expectCause(CoreMatchers.<Throwable>instanceOf(RuntimeException.class));
        //when
        permissionService.checkAPICallWithScript("MyCustomRule", new APICallContext());
    }

    @Test
    public void should_checkAPICallWithScript_with_unknown_class() throws SBonitaException, ClassNotFoundException {
        //given
        permissionService.start();
        expectedException.expect(ClassNotFoundException.class);

        //when
        permissionService.checkAPICallWithScript("plop", new APICallContext());
    }

    private static class HasName extends BaseMatcher<Class> {

        private String myCustomRule;

        private HasName(String myCustomRule) {
            this.myCustomRule = myCustomRule;
        }

        @Override
        public void describeTo(Description description) {
        }

        @Override
        public boolean matches(Object item) {
            myCustomRule = "MyCustomRule";
            return (item instanceof Class<?>) && ((Class) item).getName().equals(myCustomRule);
        }
    }
}
