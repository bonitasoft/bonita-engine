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
package org.bonitasoft.engine.api.impl.projectdeployer.descriptor;

import java.io.File;

import org.bonitasoft.engine.api.impl.projectdeployer.detector.ArtifactTypeDetector;
import org.bonitasoft.engine.api.impl.projectdeployer.detector.ArtifactTypeDetectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baptiste Mesta.
 */
public class DeploymentDescriptorGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentDescriptorGenerator.class);
    private ArtifactTypeDetector artifactTypeDetector;

    private DeploymentDescriptorGenerator(ArtifactTypeDetector artifactTypeDetector) {
        this.artifactTypeDetector = artifactTypeDetector;
    }

    public static DeploymentDescriptorGenerator create() {
        return new DeploymentDescriptorGenerator(ArtifactTypeDetectorFactory.artifactTypeDetector());
    }

    public DeploymentDescriptor fromDirectory(File directory) {
        LOGGER.info("Generating deployment descriptor from directory {}...", directory.getAbsolutePath());
        DeploymentDescriptor.DeploymentDescriptorBuilder builder = DeploymentDescriptor.builder();
        builder.name(directory.getName());
        detectArtifacts(directory, builder);
        LOGGER.info("Deployment descriptor has been generated.");
        return builder.build();
    }

    public void detectArtifacts(File directory, DeploymentDescriptor.DeploymentDescriptorBuilder builder) {
        for (File file : directory.listFiles()) {
            if (!file.isFile()) {
                detectArtifacts(file, builder);
            } else {
                artifactTypeDetector.detect(file).build(builder);
            }
        }
    }

}
