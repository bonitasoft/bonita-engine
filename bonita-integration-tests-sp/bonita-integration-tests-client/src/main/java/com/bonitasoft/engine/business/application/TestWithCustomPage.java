/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.After;
import org.junit.Before;

import com.bonitasoft.engine.BPMTestSPUtil;
import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.api.ApplicationAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.page.Page;
import com.bonitasoft.engine.page.PageCreator;

/**
 * @author Elias Ricken de Medeiros
 */
public class TestWithCustomPage extends TestWithApplication {

    private Page page;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        try {
            page = createPage("custompage_MyPage");
        } catch (final AlreadyExistsException e) {
            throw e;
        }
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
        final Page page = getPageAPI().createPage(new PageCreator(pageName, "content.zip").setDisplayName(pageName), createPageContent(pageName));
        return page;
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
