/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server;

import static org.junit.Assert.assertTrue;

import org.bonitasoft.web.rest.server.api.organization.APICustomUserInfoDefinition;
import org.bonitasoft.web.rest.server.api.organization.APICustomUserInfoUser;
import org.bonitasoft.web.rest.server.api.organization.APICustomUserInfoValue;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Vincent Elcrin
 */
public class BonitaRestAPIFactoryTest extends APITestWithMock {

    BonitaRestAPIFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new BonitaRestAPIFactory();
    }

    @Test
    public void should_provide_an_APICustomUserInfoDefinition_when_requesting_custom_user_info_definition()
            throws Exception {
        assertTrue(factory.defineApis("customuserinfo", "definition") instanceof APICustomUserInfoDefinition);
    }

    @Test
    public void should_provide_an_APICustomUserInfoUser_when_requesting_custom_user_info() throws Exception {
        assertTrue(factory.defineApis("customuserinfo", "user") instanceof APICustomUserInfoUser);
    }

    @Test
    public void should_provide_an_APICustomUserInfoValue_when_requesting_custom_user_info_value() throws Exception {
        assertTrue(factory.defineApis("customuserinfo", "value") instanceof APICustomUserInfoValue);
    }
}
