/**
 * Copyright (C) 2017 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.platform.configuration.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 */
public class AutoUpdateConfigurationVisitorTest {

    private AutoUpdateConfigurationVisitor autoUpdateVisitor = new AutoUpdateConfigurationVisitor(null);
    private static Path initial;

    @BeforeClass
    public static void createFolders() throws IOException {
        initial = Paths.get(System.getProperty("java.io.tmpdir")).resolve("initial");
        Files.createDirectories(initial);
    }

    @AfterClass
    public static void deleteFolders() throws IOException {
        FileUtils.deleteDirectory(initial.toFile());
    }

    @Test
    public void isAutoUpdateConfigurationFile_should_return_true_for_compound_permissions_mapping() throws Exception {
        // given:
        final Path path = initial.resolve("compound-permissions-mapping.properties");
        Files.createFile(path);

        // when:
        final boolean autoUpdateConfigurationFile = autoUpdateVisitor.isAutoUpdateConfigurationFile(path);

        // then:
        assertThat(autoUpdateConfigurationFile).isTrue();
    }

    @Test
    public void isAutoUpdateConfigurationFile_should_return_true_for_dynamic_permissions_checks() throws Exception {
        // given:
        final Path path = initial.resolve("dynamic-permissions-checks.properties");
        Files.createFile(path);

        // when:
        final boolean autoUpdateConfigurationFile = autoUpdateVisitor.isAutoUpdateConfigurationFile(path);

        // then:
        assertThat(autoUpdateConfigurationFile).isTrue();
    }

    @Test
    public void isAutoUpdateConfigurationFile_should_return_true_for_resources_permissions_mapping() throws Exception {
        // given:
        final Path path = initial.resolve("resources-permissions-mapping.properties");
        Files.createFile(path);

        // when:
        final boolean autoUpdateConfigurationFile = autoUpdateVisitor.isAutoUpdateConfigurationFile(path);

        // then:
        assertThat(autoUpdateConfigurationFile).isTrue();
    }

    @Test
    public void isAutoUpdateConfigurationFile_should_return_false_for_other_file() throws Exception {
        // given:
        final Path path = initial.resolve("wrong_file_name.lst");
        Files.createFile(path);

        // when:
        final boolean autoUpdateConfigurationFile = autoUpdateVisitor.isAutoUpdateConfigurationFile(path);

        // then:
        assertThat(autoUpdateConfigurationFile).isFalse();
    }

    @Test
    public void isAutoUpdateConfigurationFile_should_return_false_for_non_existing_file() throws Exception {
        // given:
        final Path path = initial.resolve("non-existing-folder/resources-permissions-mapping.properties");

        // when:
        final boolean autoUpdateConfigurationFile = autoUpdateVisitor.isAutoUpdateConfigurationFile(path);

        // then:
        assertThat(autoUpdateConfigurationFile).isFalse();
    }
}
