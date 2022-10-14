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
package org.bonitasoft.console.common.server.servlet;

import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.bonitasoft.engine.session.APISession;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PageUploadServletTest {

    @Mock
    HttpServletRequest request;

    @Mock
    APISession apiSession;

    @Spy
    PageUploadServlet pageUploadServlet = new PageUploadServlet();

    @Test
    public void should_getPermissions_work_with_valid_zip() throws Exception {

        final File zipFile = new File(getClass().getResource("/pageWithPermissions.zip").toURI());

        doReturn(apiSession).when(pageUploadServlet).getAPISession(request);
        doReturn("edit").when(request).getParameter("action");
        final Set<String> permissionsSet = new HashSet<>();
        permissionsSet.add("Organisation visualization");
        permissionsSet.add("Organisation management");
        doReturn(permissionsSet).when(pageUploadServlet).getPagePermissions(request, zipFile, false);

        final String permissions = pageUploadServlet.getPermissions(request, zipFile);

        Assert.assertTrue("[Organisation visualization, Organisation management]".equals(permissions)
                || "[Organisation management, Organisation visualization]".equals(permissions));
    }

    @Test
    public void should_getPermissions_work_with_invalid_zip() throws Exception {

        final File zipFile = new File(getClass().getResource("/pageWithPermissions.zip").toURI());

        doReturn(apiSession).when(pageUploadServlet).getAPISession(request);
        doReturn("add").when(request).getParameter("action");
        final Set<String> permissionsSet = new HashSet<>();
        doReturn(permissionsSet).when(pageUploadServlet).getPagePermissions(request, zipFile, true);

        final String permissions = pageUploadServlet.getPermissions(request, zipFile);

        Assert.assertEquals("[]", permissions);
    }

    @Test
    public void should_generateResponseString_work_with_permissions() throws Exception {

        final File zipFile = new File(getClass().getResource("/pageWithPermissions.zip").toURI());
        final String permissions = "[Organisation visualization,Organisation management]";
        doReturn(permissions).when(pageUploadServlet).getPermissions(request, zipFile);

        final String responseString = pageUploadServlet.generateResponseString(request, "fileName", zipFile);

        Assert.assertEquals(zipFile.getName() + "::" + permissions, responseString);
    }
}
