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
package org.bonitasoft.engine.api.impl.transaction.platform;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.dependency.model.builder.SPlatformDependencyBuilderFactory;
import org.bonitasoft.engine.dependency.model.builder.SPlatformDependencyMappingBuilderFactory;

/**
 * @author Zhang Bole
 * @author Celine Souchet
 */
public class AddSPlatformCommandDependency implements TransactionContent {

    private final DependencyService dependencyService;

    private final String name;

    private final byte[] jar;

    private final long artifactId;

    private final ScopeType artifactType;

    public AddSPlatformCommandDependency(final DependencyService dependencyService, final String name, final byte[] jar, final long artifactId,
            final ScopeType artifactType) {
        this.dependencyService = dependencyService;
        this.name = name;
        this.jar = jar;
        this.artifactId = artifactId;
        this.artifactType = artifactType;
    }

    @Override
    public void execute() throws SBonitaException {
        final SDependency sDependency = BuilderFactory.get(SPlatformDependencyBuilderFactory.class).createNewInstance(name, name, jar).done();
        this.dependencyService.createDependency(sDependency);
        final SDependencyMapping sDependencyMapping = BuilderFactory.get(SPlatformDependencyMappingBuilderFactory.class)
                .createNewInstance(sDependency.getId(), artifactId, artifactType).done();
        this.dependencyService.createDependencyMapping(sDependencyMapping);
    }

}
