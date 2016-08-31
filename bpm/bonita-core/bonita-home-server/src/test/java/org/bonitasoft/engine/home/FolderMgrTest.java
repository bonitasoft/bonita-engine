/**
 * Copyright (C) 2015-2016 BonitaSoft S.A.
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

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TemporaryFolder;

/**
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 */
public class FolderMgrTest {

    @Rule
    public RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();
    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void getPlatformGlobalClassLoaderFolder_should_create_all_parents() throws Exception {
        final Folder platformGlobalClassLoaderFolder = FolderMgr.getPlatformGlobalClassLoaderFolder();
        assertThat(platformGlobalClassLoaderFolder.getFile()).exists().isDirectory();
    }

    @Test
    public void getTempFolder_should_WARN_when_old_folder_still_exists() throws Exception {
        //given
        File tempFolder = temporaryFolder.newFolder();
        System.setProperty("java.io.tmpdir", tempFolder.getAbsolutePath());
        File bonita_engine_old1 = new File(tempFolder, "bonita_engine_old1");
        File bonita_engine_old2 = new File(tempFolder, "bonita_engine_old2");
        bonita_engine_old1.mkdir();
        bonita_engine_old2.mkdir();
        //when
        Folder tempFolder1 = FolderMgr.getTempFolder();
        //then
        assertThat(systemOutRule.getLog()).contains("Delete these folders to free up space:");
        assertThat(systemOutRule.getLog()).contains(bonita_engine_old1.getAbsolutePath());
        assertThat(systemOutRule.getLog()).contains(bonita_engine_old2.getAbsolutePath());
        assertThat(systemOutRule.getLog()).doesNotContain(tempFolder1.getFile().getName());

    }

}
