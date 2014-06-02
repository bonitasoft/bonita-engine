/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.BonitaTestRunner;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.bonitasoft.engine.page.Page;

@RunWith(BonitaTestRunner.class)
@Initializer(TestsInitializerSP.class)
@SuppressWarnings("javadoc")
public class PageAPILocalIT extends CommonAPISPTest {

    @Before
    public void before() throws BonitaException {
        login();
    }

    @After
    public void after() throws BonitaException {
        final SearchResult<Page> searchPages = getPageAPI().searchPages(new SearchOptionsBuilder(0, 1000).done());
        for (final Page page : searchPages.getResult()) {
            if (!page.isProvided()) {
                getPageAPI().deletePage(page.getId());
            }
        }
        logout();
    }

    /*
     * when the tenant is created the provided page "bonita-groovy-page-example.zip" should be imported from classpath
     */
    @Test
    public void should_provided_page_be_imported() throws BonitaException {
        // given
        // engine started

        // when
        final Page page = getPageAPI().getPageByName("custompage_groovy-example");

        // then
        assertThat(page).isNotNull();
        assertThat(page.isProvided()).isTrue();
    }

}
