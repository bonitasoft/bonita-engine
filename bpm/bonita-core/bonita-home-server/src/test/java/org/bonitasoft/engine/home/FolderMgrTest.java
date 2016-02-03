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

import org.junit.Test;

/**
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 */
public class FolderMgrTest {

    @Test
    public void getPlatformGlobalClassLoaderFolder_should_create_all_parents() throws Exception {
        final Folder platformGlobalClassLoaderFolder = FolderMgr.getPlatformGlobalClassLoaderFolder();
        assertThat(platformGlobalClassLoaderFolder.getFile()).exists().isDirectory();
    }

}
