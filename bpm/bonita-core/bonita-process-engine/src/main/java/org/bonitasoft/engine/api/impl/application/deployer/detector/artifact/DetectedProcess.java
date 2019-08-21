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
package org.bonitasoft.engine.api.impl.application.deployer.detector.artifact;

import org.bonitasoft.engine.api.impl.application.deployer.descriptor.DeploymentDescriptor.DeploymentDescriptorBuilder;
import org.bonitasoft.engine.api.impl.application.deployer.model.Process;

public class DetectedProcess extends DefaultDetectedArtifact {

    public DetectedProcess(String fileName) {
        super(fileName);
    }

    @Override
    public void build(DeploymentDescriptorBuilder builder) {
        LOGGER.info("Found process file: '{}'.", fileName);
        builder.process(new Process().setFile(fileName));
    }

}
