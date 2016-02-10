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
        return getPageAPI().createPage(new PageCreator(pageName, "content.zip").setDisplayName(pageName),
                createTestPageContent(pageName, "no display name", "empty desc"));
    }

}
