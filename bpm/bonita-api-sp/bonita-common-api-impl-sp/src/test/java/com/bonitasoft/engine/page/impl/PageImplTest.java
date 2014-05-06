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
    public void testGetId() throws Exception {
        assertThat(pageImpl.getId()).isEqualTo(PAGE_ID);
    }

    @Test
    public void testGetName() throws Exception {
        assertThat(pageImpl.getName()).isEqualTo(NAME);
    }

    @Test
    public void testIsProvided() throws Exception {
        assertThat(pageImpl.isProvided()).isEqualTo(PROVIDED);
    }

    @Test
    public void testGetDescription() throws Exception {
        assertThat(pageImpl.getDescription()).isEqualTo(DESCRIPTION);
    }

    @Test
    public void testGetInstallationDate() throws Exception {
        assertThat(pageImpl.getInstallationDate()).isEqualTo(new Date(installationDate));
    }

    @Test
    public void testGetInstalledBy() throws Exception {
        assertThat(pageImpl.getInstalledBy()).isEqualTo(USER_ID);
    }

    @Test
    public void testGetLastModificationDate() throws Exception {
        assertThat(pageImpl.getLastModificationDate()).isEqualTo(new Date(modificationDate));
    }

    @Test
    public void testGetDisplayName() throws Exception {
        assertThat(pageImpl.getDisplayName()).isEqualTo(DISPLAY_NAME);
    }
}
