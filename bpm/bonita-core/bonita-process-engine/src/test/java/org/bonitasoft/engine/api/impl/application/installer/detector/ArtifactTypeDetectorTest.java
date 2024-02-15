/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl.application.installer.detector;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArtifactTypeDetectorTest {

    @Spy
    @InjectMocks
    private ArtifactTypeDetector detector;

    @Test
    void readApplicationPropertiesVersion(@TempDir Path tempFolder) throws IOException {
        var applicationPropertyFile = tempFolder.resolve("application.properties");
        Files.writeString(applicationPropertyFile, "version=1.0.0");

        var version = detector.readVersion(applicationPropertyFile.toFile());

        assertThat(version).isEqualTo("1.0.0");
    }

    @Test
    void readMissingApplicationPropertiesVersion(@TempDir Path tempFolder) throws IOException {
        var applicationPropertyFile = tempFolder.resolve("application.properties");
        Files.writeString(applicationPropertyFile, "");

        var version = detector.readVersion(applicationPropertyFile.toFile());

        assertThat(version).isNull();
    }

}
