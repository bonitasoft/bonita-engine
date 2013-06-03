/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.platform;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.builder.DependencyBuilder;
import org.bonitasoft.engine.dependency.model.builder.DependencyBuilderAccessor;
import org.bonitasoft.engine.dependency.model.builder.DependencyMappingBuilder;

/**
 * @author Zhang Bole
 */
public class AddSPlatformCommandDependency implements TransactionContent {

    private final DependencyService dependencyService;

    private final DependencyBuilderAccessor dependencyBuilderAccessor;

    private final String name;

    private final byte[] jar;

    private final long artifactId;

    private final String artifactType;

    public AddSPlatformCommandDependency(final DependencyService dependencyService, final DependencyBuilderAccessor dependencyBuilderAccessor,
            final String name, final byte[] jar, final long artifactId, final String artifactType) {
        this.dependencyService = dependencyService;
        this.dependencyBuilderAccessor = dependencyBuilderAccessor;
        this.name = name;
        this.jar = jar;
        this.artifactId = artifactId;
        this.artifactType = artifactType;
    }

    @Override
    public void execute() throws SBonitaException {
        final DependencyBuilder dependencyBuilder = dependencyBuilderAccessor.getDependencyBuilder();
        final SDependency sDependency = dependencyBuilder.createNewInstance(name, "1.0", name + ".jar", jar).done();
        this.dependencyService.createDependency(sDependency);
        final DependencyMappingBuilder dependencyMappingBuilder = dependencyBuilderAccessor.getDependencyMappingBuilder();
        final SDependencyMapping sDependencyMapping = dependencyMappingBuilder.createNewInstance(sDependency.getId(), artifactId, artifactType).done();
        this.dependencyService.createDependencyMapping(sDependencyMapping);
    }

}
