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
package org.bonitasoft.engine.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.naming.NamingException;

import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.service.PermissionService;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.platform.configuration.ConfigurationService;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.bonitasoft.platform.setup.PlatformSetupAccessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Baptiste Mesta
 */
public class PermissionAPIIT extends CommonAPILocalIT {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private APICallContext apiCallContext;

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
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
    public void execute_provided_security_script_works() throws Exception {
        //given
        apiCallContext = new APICallContext("GET", "identity", "user", null, "query", "body") {

            @Override
            public Map<String, String> getFilters() {
                return Collections.singletonMap("user_id", String.valueOf(getSession().getUserId()));
            }
        };
        //when
        boolean processPermissionRule = getPermissionAPI().checkAPICallWithScript("org.bonitasoft.permissions.ProcessPermissionRule", apiCallContext, false);

        //then
        assertThat(processPermissionRule).isTrue();
    }

    @Test
    public void execute_security_script_with_dependencies() throws Exception {

        //given
        writeScriptToBonitaHome(getContentOfResource("/MyRule"), "MyRule", "org", "test");
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

    private void writeScriptToBonitaHome(final String scriptFileContent, final String fileName, final String... folders)
            throws IOException, SBonitaException, BonitaHomeNotSetException, NamingException {
        ConfigurationService configurationService = PlatformSetupAccessor.getConfigurationService();
        String path = "";
        for (String folder : folders) {
            path += folder + "/";
        }
        path += fileName + ".groovy";

        configurationService.storeTenantSecurityScripts(Collections.singletonList(new BonitaConfiguration(path, scriptFileContent.getBytes())),
                getTenantAccessor().getTenantId());

        final PermissionService permissionService = TenantServiceSingleton.getInstance().getPermissionService();
        //restart the service to reload scripts
        permissionService.stop();
        permissionService.start();

    }
}
