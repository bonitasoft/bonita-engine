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
package org.bonitasoft.engine.api.impl.projectdeployer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bonitasoft.engine.api.impl.projectdeployer.descriptor.DeploymentDescriptorReader;
import org.bonitasoft.engine.api.impl.projectdeployer.validator.ArtifactValidator;
import org.bonitasoft.engine.api.impl.projectdeployer.validator.InvalidArtifactException;
import org.bonitasoft.engine.api.utils.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baptiste Mesta.
 */
public class ApplicationArchiveReader {

    private ArtifactValidator artifactValidator;
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationArchiveReader.class);

    public ApplicationArchiveReader(ArtifactValidator validator) {
        this.artifactValidator = validator;
    }

    public ApplicationArchive read(byte[] applicationArchiveFile) throws IOException, InvalidArtifactException {
        try (InputStream inputStream = new ByteArrayInputStream(applicationArchiveFile)) {
            return read(inputStream);
        }
    }

    @VisibleForTesting
    ApplicationArchive read(InputStream inputStream) throws IOException, InvalidArtifactException {
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ApplicationArchive applicationArchive = new ApplicationArchive();
        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            if (zipEntry.getName().equals("deploy.json")) {
                applicationArchive.setDeploymentDescriptor(new DeploymentDescriptorReader().fromJson(zipInputStream));
            } else if (!zipEntry.isDirectory()) {
                applicationArchive.addFile(zipEntry.getName(), zipInputStream);
            }
        }
        validate(applicationArchive);
        return applicationArchive;
    }

    private void validate(ApplicationArchive applicationArchive) throws IOException, InvalidArtifactException {
        if (applicationArchive.isEmpty()) {
            applicationArchive.close(); // ensure to free all temp resources
            throw new IllegalArgumentException("Application archive is empty or is not a valid file");
        }
        if (applicationArchive.getDeploymentDescriptor() != null) {
            LOGGER.info("Starting artifacts validation...");
            artifactValidator.validate(applicationArchive);
            LOGGER.info("Artifacts validation completed successfully.");
        } else {
            LOGGER.info("No deployment descriptor has been provided");
            applicationArchive.generateDeploymentDescriptor();
        }
    }

}
