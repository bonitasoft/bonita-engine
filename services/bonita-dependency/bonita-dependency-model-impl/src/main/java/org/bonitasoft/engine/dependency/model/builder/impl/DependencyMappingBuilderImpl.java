/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.dependency.model.builder.impl;

import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.builder.DependencyMappingBuilder;
import org.bonitasoft.engine.dependency.model.impl.SDependencyMappingImpl;

public class DependencyMappingBuilderImpl implements DependencyMappingBuilder {

    private SDependencyMappingImpl object;

    @Override
    public DependencyMappingBuilder createNewInstance(final long dependencyId, final long artifactId, final String artifactType) {
        this.object = new SDependencyMappingImpl(artifactId, artifactType, dependencyId);
        return this;
    }

    @Override
    public SDependencyMapping done() {
        return this.object;
    }

    @Override
    public String getArtifactIdKey() {
        return "artifactId";
    }

    @Override
    public String getArtifactTypeKey() {
        return "artifactType";
    }

    @Override
    public String getDependencyIdKey() {
        return "dependencyId";
    }

    @Override
    public String getIdKey() {
        return "id";
    }

}
