/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.test.runner.BonitaTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BonitaTestRunner.class)
@Initializer(TestsInitializer.class)
@SuppressWarnings("javadoc")
public class PageAPILocalIT extends CommonAPIIT {

    @Before
    public void before() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    @After
    public void after() throws BonitaException {
        final SearchResult<Page> searchPages = getOrgPageAPI().searchPages(new SearchOptionsBuilder(0, 1000).done());
        for (final Page page : searchPages.getResult()) {
            if (!page.isProvided()) {
                getOrgPageAPI().deletePage(page.getId());
            }
        }
        logoutOnTenant();
    }

    /*
     * when the tenant is created the provided page "bonita-groovy-page-example.zip" should be imported from classpath
     */
    @Test
    public void should_provided_page_be_imported() throws BonitaException {
        // given
        // engine started

        // when
        final Page pageGroovy = getOrgPageAPI().getPageByName("custompage_groovyexample");
        final Page pageHtml = getOrgPageAPI().getPageByName("custompage_htmlexample");
        final Page homePage = getOrgPageAPI().getPageByName("custompage_home");

        // then
        assertThat(pageGroovy).isNotNull();
        assertThat(pageGroovy.isProvided()).isTrue();

        assertThat(pageHtml).isNotNull();
        assertThat(pageHtml.isProvided()).isTrue();

        assertThat(homePage).isNotNull();
        assertThat(homePage.isProvided()).isTrue();
    }

}
