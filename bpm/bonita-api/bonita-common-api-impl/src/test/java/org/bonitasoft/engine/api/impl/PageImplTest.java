/*******************************************************************************
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
 ******************************************************************************/
package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.bonitasoft.engine.impl.PageImpl;
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
