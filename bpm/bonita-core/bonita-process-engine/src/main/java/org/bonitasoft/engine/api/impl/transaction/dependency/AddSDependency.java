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
package org.bonitasoft.engine.api.impl.transaction.dependency;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.dependency.model.builder.SDependencyBuilderFactory;
import org.bonitasoft.engine.dependency.model.builder.SDependencyMappingBuilderFactory;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class AddSDependency implements TransactionContent {

    private final DependencyService dependencyService;

    private final String name;

    private final byte[] jar;

    private final long artifactId;

    private final ScopeType artifactType;

    public AddSDependency(final DependencyService dependencyService, final String name, final byte[] jar, final long artifactId, final ScopeType artifactType) {
        this.dependencyService = dependencyService;
        this.name = name;
        this.jar = jar;
        this.artifactId = artifactId;
        this.artifactType = artifactType;
    }

    @Override
    public void execute() throws SDependencyException {
        final SDependency sDependency = BuilderFactory.get(SDependencyBuilderFactory.class)
                // add a .jar here because we need to add a new method in command API to have name and filename
                // see BS-7393
                .createNewInstance(name, artifactId, artifactType, name + ".jar", jar)
                .done();
        dependencyService.createDependency(sDependency);
        final SDependencyMapping sDependencyMapping = BuilderFactory.get(SDependencyMappingBuilderFactory.class)
                .createNewInstance(sDependency.getId(), artifactId, artifactType).done();
        dependencyService.createDependencyMapping(sDependencyMapping);
    }

}
