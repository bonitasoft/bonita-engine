/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.login.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.junit.Test;

public class RestAPIAuthorizationFilterMappingTest {

    @Test
    public void webXml_should_map_authorization_filter_on_every_servlet_path_with_a_declared_permission()
            throws Exception {
        AuthorizationFilterMapping mapping = AuthorizationFilterMapping
                .read(Paths.get("src/main/webapp/WEB-INF/web.xml"));

        assertThat(mapping.getServletPathsWithDeclaredPermission()).contains("/portal/formsDocumentImage",
                "/portal/bdmUpload", "/portal/exportActors", "/portal/exportApplications");
        assertThat(mapping.getServletPathsNotCoveredByAuthorizationFilter()).isEmpty();
    }

    @Test
    public void webXml_should_map_authorization_filter_on_every_exact_portal_servlet_path() throws Exception {
        AuthorizationFilterMapping mapping = AuthorizationFilterMapping
                .read(Paths.get("src/main/webapp/WEB-INF/web.xml"));

        assertThat(mapping.getPortalServletPathsNotCoveredByAuthorizationFilter()).isEmpty();
    }
}
