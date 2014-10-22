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

package org.bonitasoft.engine.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.BPMRemoteTests;
import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.identity.User;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Baptiste Mesta
 */
public class PermissionAPIIT extends CommonAPILocalTest {

    private File securityScriptsFolder;
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private APICallContext apiCallContext;

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        securityScriptsFolder = new File(new File(BonitaHomeServer.getInstance().getTenantConfFolder(getSession().getTenantId())), "security-scripts");
        securityScriptsFolder.mkdirs();
        apiCallContext = new APICallContext("GET", "identity", "user", "1", "query", "body");
    }

    @Test
    @Ignore("NYI")
    public void execute_security_script_that_throw_exception() throws Exception {
        //given
        writeScriptToBonitaHome("throw new java.lang.IllegalStateException()", "myScript");

        exception.expect(ExecutionException.class);
        exception.expectCause(CoreMatchers.any(IllegalStateException.class));
        //when
        getPermissionAPI().checkAPICallWithScript("myScript", apiCallContext);

        //then: ExecutionException
    }

    @Test
    public void execute_security_script_with_dependencies() throws Exception {

        //given
        writeScriptToBonitaHome(getContentOfResource("/MyRule.groovy"), "MyRule");
        User john = createUser("john", "bpm");
        User jack = createUser("jack", "bpm");


        //when
        loginOnDefaultTenantWith("jack","bpm");
        boolean jackResult = getPermissionAPI().checkAPICallWithScript("MyRule", new APICallContext("GET", "identity", "user", String.valueOf(jack.getId()), "query", "body"));
        logoutOnTenant();
        loginOnDefaultTenantWith("john","bpm");
        boolean johnResult = getPermissionAPI().checkAPICallWithScript("MyRule", new APICallContext("GET", "identity", "user", String.valueOf(john.getId()), "query", "body"));
        boolean johnResultOnOtherAPI = getPermissionAPI().checkAPICallWithScript("MyRule", new APICallContext("GET", "identity", "user", String.valueOf(jack.getId()), "query", "body"));

        //then: ExecutionException
        assertThat(jackResult).isFalse();
        assertThat(johnResult).isTrue();
        assertThat(johnResultOnOtherAPI).isFalse();

        deleteUser(john);
        deleteUser(jack);
    }


    @Test
    public void execute_security_script_with_not_found_script() throws Exception {
        //given

        exception.expect(NotFoundException.class);
        //when
        getPermissionAPI().checkAPICallWithScript("unknown", apiCallContext);

        //then: ExecutionException
    }

    private void writeScriptToBonitaHome(String script, String scriptName) throws IOException {
        File file = new File(securityScriptsFolder, scriptName + ".groovy");
        file.createNewFile();
        IOUtil.writeFile(file, script);
        System.out.println("write to file " + file.getPath());
    }
}
