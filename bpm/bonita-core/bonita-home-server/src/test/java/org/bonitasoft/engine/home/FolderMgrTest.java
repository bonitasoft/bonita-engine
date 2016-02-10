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

package org.bonitasoft.engine.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Baptiste Mesta
 */
public class FolderMgrTest {

    private FolderMgr folderMgr = new FolderMgr();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File bonitaHome;

    @Before
    public void before() throws IOException {
        bonitaHome = temporaryFolder.newFolder();
    }


    @Test
    public void testCreateTenantTempProcessFolder_create_all_parents() throws Exception {
        FolderMgr.createTenantTempProcessFolder(bonitaHome,15l,457l);
        assertThat(new File(bonitaHome, "engine-server/temp/tenants/15/processes/457/")).exists().isDirectory();
    }

    @Test(expected = IOException.class)
    public void testcreateTenantWorkProcessFolder_do_not_create_parents() throws Exception {
        FolderMgr.createTenantWorkProcessFolder(bonitaHome,15l,457l);
    }
}