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
package org.bonitasoft.engine.business.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageCreator;
import org.junit.After;
import org.junit.Before;

/**
 * @author Elias Ricken de Medeiros
 */
public class TestWithCustomPage extends TestWithApplication {

    private Page page;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        page = createPage("custompage_MyPage");
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        loginOnDefaultTenantWithDefaultTechnicalUser();
        if (page != null) {
            getPageAPI().deletePage(page.getId());
        }
        logoutOnTenant();
    }

    public Page getPage() {
        return page;
    }

    protected Page createPage(final String pageName) throws Exception {
        return getPageAPI().createPage(new PageCreator(pageName, "content.zip").setDisplayName(pageName), createPageContent(pageName));
    }

    private byte[] createPageContent(final String pageName)
            throws BonitaException {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ZipOutputStream zos = new ZipOutputStream(baos);
            zos.putNextEntry(new ZipEntry("Index.groovy"));
            zos.write("return \"\";".getBytes());

            zos.putNextEntry(new ZipEntry("page.properties"));
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("name=");
            stringBuilder.append(pageName);
            stringBuilder.append("\n");
            stringBuilder.append("displayName=");
            stringBuilder.append("no display name");
            stringBuilder.append("\n");
            stringBuilder.append("description=");
            stringBuilder.append("empty desc");
            stringBuilder.append("\n");
            zos.write(stringBuilder.toString().getBytes());

            zos.closeEntry();
            return baos.toByteArray();
        } catch (final IOException e) {
            throw new BonitaException(e);
        }
    }

}
