/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.classloader;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.junit.Test;

public class BonitaClassLoaderTest {

    @Test
    public void releaseShouldRemoveAllScopeFolderAndItsContent() {
        final Map<String, byte[]> resources = new HashMap<String, byte[]>(1);
        resources.put("myJar.jar", "Salut le monde".getBytes());
        final File tempDir = new File(IOUtil.TMP_DIRECTORY, "BonitaClassLoaderTest");
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
