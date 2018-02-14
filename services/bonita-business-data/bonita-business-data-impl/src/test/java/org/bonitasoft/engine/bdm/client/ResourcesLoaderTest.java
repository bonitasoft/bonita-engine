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
package org.bonitasoft.engine.bdm.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.io.IOUtils;
import org.junit.Test;

public class ResourcesLoaderTest {

    @Test
    public void should_copy_files_from_package_in_classpath_to_destination_directory() throws Exception {
        String packageName = "org.bonitasoft.engine.package";
        File directory = IOUtils.createTempDirectory("bdmtest");

        new ResourcesLoader().copyJavaFilesToDirectory(packageName, directory);

        assertThat(new File(directory, "org/bonitasoft/engine/package/Test.java")).exists();
        assertThat(new File(directory, "org/bonitasoft/engine/package/subfolder/Other.java")).exists();
        FileUtils.deleteDirectory(directory);
    }
}
