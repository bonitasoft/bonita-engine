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
package org.bonitasoft.engine.classloader;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.junit.Test;

public class BonitaClassLoaderTest {

    @Test
    public void releaseShouldRemoveAllScopeFolderAndItsContent() throws IOException {
        final Map<String, byte[]> resources = new HashMap<String, byte[]>(1);
        resources.put("myJar.jar", "Salut le monde".getBytes());
        final File tempDir = new File(IOUtil.TMP_DIRECTORY, "BonitaClassLoaderTest_JVM_" + ManagementFactory.getRuntimeMXBean().getName());
        if (tempDir.exists()) {
            FileUtils.deleteDirectory(tempDir);
        }

        final BonitaClassLoader bonitaClassLoader = new BonitaClassLoader(resources, "here", 154L, tempDir.toURI(), BonitaClassLoader.class.getClassLoader());
        assertThat(tempDir).as("bonitaClassLoader tempDir:%s should exists after bonitaClassLoader creation", tempDir.getAbsolutePath()).exists();

        // when
        bonitaClassLoader.destroy();

        // then
        assertThat(tempDir).as("bonitaClassLoader tempDir:%s should not exists after bonitaClassLoader release", tempDir.getAbsolutePath()).doesNotExist();
    }

}
