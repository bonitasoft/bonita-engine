/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PageAPILocalIT extends CommonAPIIT {

    @Before
    public void before() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    @After
    public void after() throws BonitaException {
        final SearchResult<Page> searchPages = getPageAPI().searchPages(new SearchOptionsBuilder(0, 1000).done());
        for (final Page page : searchPages.getResult()) {
            if (!page.isProvided()) {
                getPageAPI().deletePage(page.getId());
            }
        }
        logoutOnTenant();
    }

    /*
     * when the tenant is created the provided page "bonita-home-page.zip" should be imported from classpath
     */
    @Test
    public void should_provided_page_be_imported() throws BonitaException {
        // given
        // engine started

        // when
        final Page homePage = getPageAPI().getPageByName("custompage_home");

        // then
        assertThat(homePage).isNotNull();
        assertThat(homePage.isProvided()).isTrue();
    }

}
