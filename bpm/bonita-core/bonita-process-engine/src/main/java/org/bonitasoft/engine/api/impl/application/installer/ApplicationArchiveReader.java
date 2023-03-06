/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl.application.installer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.impl.application.installer.detector.ArtifactTypeDetector;
import org.bonitasoft.engine.io.IOUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

/**
 * @author Baptiste Mesta.
 */
@Slf4j
@Component
@ConditionalOnSingleCandidate(ApplicationArchiveReader.class)
public class ApplicationArchiveReader {

    private final ArtifactTypeDetector artifactTypeDetector;

    public ApplicationArchiveReader(ArtifactTypeDetector artifactTypeDetector) {
        this.artifactTypeDetector = artifactTypeDetector;
    }

    public ApplicationArchive read(byte[] applicationArchiveFile) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(applicationArchiveFile)) {
            return read(inputStream);
        }
    }

    public ApplicationArchive read(InputStream inputStream) throws IOException {
        final ApplicationArchive applicationArchive = createNewApplicationArchive();
        File destDir = IOUtil.createTempDirectory(Files.createTempDirectory("temp-custom-application").toUri());
        IOUtil.unzipToFolder(inputStream, destDir);
        try (Stream<Path> walker = Files.walk(destDir.toPath())) {
            walker.filter(p -> p.toFile().isFile()).forEach(path -> {
                try {
                    artifactTypeDetector.checkFileAndAddToArchive(path.toFile(), applicationArchive);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return applicationArchive;
    }

    protected ApplicationArchive createNewApplicationArchive() {
        return new ApplicationArchive();
    }

}
