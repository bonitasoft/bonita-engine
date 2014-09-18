/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

public class PageImplTest {

    private static PageImpl pageImpl;

    private static final long USER_ID = 1l;

    private static final String DESCRIPTION = "description";

    private static final boolean PROVIDED = true;

    private static final String NAME = "name";

    private static final String DISPLAY_NAME = "display name";

    private static final long PAGE_ID = -1l;

    private static long installationDate;

    private static long modificationDate;

    @BeforeClass
    public static void before() {

        installationDate = System.currentTimeMillis();
        modificationDate = installationDate + 10000;

        pageImpl = new PageImpl(PAGE_ID, NAME, DISPLAY_NAME, PROVIDED, DESCRIPTION, installationDate, USER_ID, modificationDate, USER_ID, "content.zip");
    }

    @Test
    public void testGetId() {
        assertThat(pageImpl.getId()).isEqualTo(PAGE_ID);
    }

    @Test
    public void testGetName() {
        assertThat(pageImpl.getName()).isEqualTo(NAME);
    }

    @Test
    public void testIsProvided() {
        assertThat(pageImpl.isProvided()).isEqualTo(PROVIDED);
    }

    @Test
    public void testGetDescription() {
        assertThat(pageImpl.getDescription()).isEqualTo(DESCRIPTION);
    }

    @Test
    public void testGetInstallationDate() {
        assertThat(pageImpl.getInstallationDate()).isEqualTo(new Date(installationDate));
    }

    @Test
    public void testGetInstalledBy() {
        assertThat(pageImpl.getInstalledBy()).isEqualTo(USER_ID);
    }

    @Test
    public void testGetLastModificationDate() {
        assertThat(pageImpl.getLastModificationDate()).isEqualTo(new Date(modificationDate));
    }

    @Test
    public void testGetDisplayName() {
        assertThat(pageImpl.getDisplayName()).isEqualTo(DISPLAY_NAME);
    }
}
