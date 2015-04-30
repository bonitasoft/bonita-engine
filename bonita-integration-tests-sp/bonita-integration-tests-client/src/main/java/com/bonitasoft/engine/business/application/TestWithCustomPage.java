/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application;

import com.bonitasoft.engine.page.Page;
import com.bonitasoft.engine.page.PageCreator;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.junit.After;
import org.junit.Before;

/**
 * @author Elias Ricken de Medeiros
 */
public class TestWithCustomPage extends TestWithApplication {

    private Page page;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        try {
            page = createPage("custompage_MyPage");
        } catch (final AlreadyExistsException e) {
            throw e;
        }
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        loginOnDefaultTenantWithDefaultTechnicalUser();
        if (page != null) {
            getSubscriptionPageAPI().deletePage(page.getId());
        }
        logoutOnTenant();
    }

    public Page getPage() {
        return page;
    }

    protected Page createPage(final String pageName) throws Exception {
        final Page page = getSubscriptionPageAPI().createPage(new PageCreator(pageName, "content.zip").setDisplayName(pageName), createPageContent(pageName));
        return page;
    }

    private byte[] createPageContent(final String pageName) throws Exception {
        return createTestPageContent(pageName, "no display name", "empty desc");
    }

}
