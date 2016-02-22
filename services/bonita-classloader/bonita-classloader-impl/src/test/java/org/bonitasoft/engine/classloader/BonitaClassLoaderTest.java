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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BonitaClassLoaderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void releaseShouldRemoveAllScopeFolderAndItsContent() throws IOException {
        final BonitaClassLoader bonitaClassLoader = new BonitaClassLoader(Collections.singletonMap("myJar.jar", "Salut le monde".getBytes()), "here", 154L,
                temporaryFolder.newFolder().toURI(), BonitaClassLoader.class.getClassLoader());
        File temporaryFolder = bonitaClassLoader.getTemporaryFolder();
        assertThat(temporaryFolder).as("bonitaClassLoader tempDir:%s should exists after bonitaClassLoader creation", temporaryFolder.getAbsolutePath())
                .exists();
        // when
        bonitaClassLoader.destroy();

        // then
        assertThat(temporaryFolder).as("bonitaClassLoader tempDir:%s should not exists after bonitaClassLoader release", temporaryFolder.getAbsolutePath())
                .doesNotExist();
    }

    @Test
    public void should_create_second_classloader_use_other_folder() throws Exception {
        //given
        File tempFolder = temporaryFolder.newFolder();
        //when
        BonitaClassLoader classLoader1 = new BonitaClassLoader(Collections.singletonMap("myJar1.jar", "content".getBytes()), "type", 12L, tempFolder.toURI(),
                BonitaClassLoaderTest.class.getClassLoader());
        BonitaClassLoader classLoader2 = new BonitaClassLoader(Collections.singletonMap("myJar2.jar", "content".getBytes()), "type", 13L, tempFolder.toURI(),
                BonitaClassLoaderTest.class.getClassLoader());
        //then
        assertThat(classLoader1.getTemporaryFolder().getAbsolutePath()).isNotEqualTo(classLoader2.getTemporaryFolder().getAbsolutePath());
        assertThat(classLoader1.getTemporaryFolder().getParentFile()).isEqualTo(tempFolder);
        assertThat(classLoader2.getTemporaryFolder().getParentFile()).isEqualTo(tempFolder);
    }

    @Test
    public void should_init_create_unique_folder() throws Exception {
        //given
        File folder = temporaryFolder.newFolder();
        BonitaClassLoader classLoader = spy(new BonitaClassLoader(Collections.singletonMap("myJar1.jar", "content".getBytes()), "type", 12L, folder.toURI(),
                BonitaClassLoaderTest.class.getClassLoader()));
        when(classLoader.generateUUID()).thenReturn("firstCall", "firstCall", "secondCall");
        //create the folder that should be used by the classloader
        new File(folder, "first").mkdir();
        //when
        File temporaryDirectory = classLoader.createTemporaryDirectory(folder.toURI());

        //then
        assertThat(temporaryDirectory).isEqualTo(new File(folder, "secon"));
    }

}
