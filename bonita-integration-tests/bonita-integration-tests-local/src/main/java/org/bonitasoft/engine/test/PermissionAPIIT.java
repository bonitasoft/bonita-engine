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

import java.io.File;
import java.io.IOException;

import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.service.PermissionService;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Baptiste Mesta
 */
public class PermissionAPIIT extends CommonAPILocalIT {

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
    public void execute_security_script_that_throw_exception() throws Exception {
        //given
        writeScriptToBonitaHome(getContentOfResource("/RuleWithException"), "RuleWithException");

        exception.expect(ExecutionException.class);
        //when
        getPermissionAPI().checkAPICallWithScript("RuleWithException", apiCallContext, false);

        //then: ExecutionException
    }

    @Test
    public void execute_security_script_with_dependencies() throws Exception {

        //given
        writeScriptToBonitaHome(getContentOfResource("/MyRule"), "org", "test", "MyRule");
        final User john = createUser("john", "bpm");
        final User jack = createUser("jack", "bpm");

        //when
        loginOnDefaultTenantWith("jack", "bpm");
        final boolean jackResult = getPermissionAPI().checkAPICallWithScript("org.test.MyRule",
                new APICallContext("GET", "identity", "user", String.valueOf(jack.getId()), "query", "body"), false);
        logoutOnTenant();
        loginOnDefaultTenantWith("john", "bpm");
        final boolean johnResult = getPermissionAPI().checkAPICallWithScript("org.test.MyRule",
                new APICallContext("GET", "identity", "user", String.valueOf(john.getId()), "query", "body"), false);
        final boolean johnResultOnOtherAPI = getPermissionAPI().checkAPICallWithScript("org.test.MyRule",
                new APICallContext("GET", "identity", "user", String.valueOf(jack.getId()), "query", "body"), false);

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
        getPermissionAPI().checkAPICallWithScript("unknown", apiCallContext, false);

        //then: ExecutionException
    }

    private void writeScriptToBonitaHome(final String script, final String... path) throws IOException, SBonitaException {
        File file = securityScriptsFolder;
        for (int i = 0; i < path.length; i++) {
            file = new File(file, i == path.length - 1 ? path[i] + ".groovy" : path[i]);
        }
        file.getParentFile().mkdirs();
        file.createNewFile();
        IOUtil.writeFile(file, script);
        System.out.println("write to file " + file.getPath());
        final PermissionService permissionService = TenantServiceSingleton.getInstance().getPermissionService();
        //restart the service to reload scripts
        permissionService.stop();
        permissionService.start();

    }
}
