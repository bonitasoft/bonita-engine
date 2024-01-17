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
package org.bonitasoft.engine.dependency;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.bonitasoft.engine.dependency.model.AbstractSDependency;
import org.bonitasoft.engine.dependency.model.DependencyContent;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.home.BonitaResource;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @since 6.0
 */
public interface DependencyService {

    String DEPENDENCY = "DEPENDENCY";

    String DEPENDENCYMAPPING = "DEPENDENCYMAPPING";

    /**
     * Delete the specific dependency
     *
     * @param dependency
     *        The dependency will be deleted
     * @throws SDependencyNotFoundException
     *         Error thrown if the dependency not found.
     * @throws SDependencyDeletionException
     *         Error thrown if has exception during the dependency deletion.
     */
    void deleteDependency(AbstractSDependency dependency) throws SDependencyException;

    /**
     * Delete the dependency specified by name
     *
     * @param name
     * @throws SDependencyNotFoundException
     *         Error thrown if no dependency have a name corresponding to the parameter.
     * @throws SDependencyDeletionException
     *         Error thrown if has exception during the dependency deletion.
     */
    void deleteDependency(String name) throws SDependencyException;

    /**
     * Get dependency by its id
     *
     * @param id
     *        Identifier of dependency
     * @return
     * @throws SDependencyNotFoundException
     *         Error thrown if no dependency have an id corresponding to the parameter.
     */
    AbstractSDependency getDependency(long id) throws SDependencyNotFoundException;

    /**
     * Get only the content and file name of a dependency.
     * This object will not be connected to the hibernate session and therefore will avoid
     * issues related to dirty checking mechanism, see https://bonitasoft.atlassian.net/browse/BS-19262
     *
     * @param id of the dependency
     * @return an object containing the file content and name
     * @throws SDependencyNotFoundException
     */
    DependencyContent getDependencyContentOnly(long id) throws SDependencyNotFoundException, SBonitaReadException;

    /**
     * Get dependencies for the specified ids
     *
     * @param ids
     *        Identifiers of dependencies
     * @return a list of SDependency object
     * @throws SDependencyException
     */
    List<AbstractSDependency> getDependencies(Collection<Long> ids) throws SDependencyException;

    /**
     * Get all dependencyMappings for specific the queryOptions
     *
     * @param queryOptions
     *        QueryOptions object, it contains some query conditions.
     * @return a list of SDependencyMapping objects
     * @throws SDependencyException
     */
    List<SDependencyMapping> getDependencyMappings(QueryOptions queryOptions) throws SDependencyException;

    Stream<BonitaResource> getDependenciesResources(ScopeType type, long id) throws SDependencyException;

    /**
     * Get all dependency ids for specific artifact
     *
     * @param artifactId
     *        Identifier of artifact
     * @param artifactType
     *        Type of artifact
     * @param startIndex
     * @param maxResult
     * @return a list of Long objects
     * @throws SDependencyException
     */
    List<Long> getDependencyIds(long artifactId, ScopeType artifactType, int startIndex, int maxResult)
            throws SDependencyException;

    /**
     * @param id
     * @param type
     * @throws SDependencyException
     */
    void deleteDependencies(long id, ScopeType type) throws SDependencyException;

    AbstractSDependency createMappedDependency(String name, byte[] jarContent, String fileName, long artifactId,
            ScopeType scopeType) throws SDependencyException;

    AbstractSDependency updateDependencyOfArtifact(String name, byte[] jarContent, String fileName, long artifactId,
            ScopeType scopeType) throws SDependencyException;

    AbstractSDependency getDependencyOfArtifact(long artifactId, ScopeType artifactType, String fileName)
            throws SBonitaReadException;

    Optional<Long> getIdOfDependencyOfArtifact(Long artifactId, ScopeType artifactType, String fileName)
            throws SBonitaReadException;
}
