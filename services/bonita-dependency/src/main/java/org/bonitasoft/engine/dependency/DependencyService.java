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
package org.bonitasoft.engine.dependency;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.ScopeType;
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
    void deleteDependency(SDependency dependency) throws SDependencyException;

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
    SDependency getDependency(long id) throws SDependencyNotFoundException;

    /**
     * Get dependencies for the specified ids
     * 
     * @param ids
     *        Identifiers of dependencies
     * @return a list of SDependency object
     * @throws SDependencyException
     */
    List<SDependency> getDependencies(Collection<Long> ids) throws SDependencyException;

    /**
     * Get all dependencyMappings for specific the queryOptions
     * 
     * @param queryOptions
     *        QueryOptions object, it contains some query conditions.
     * @return a list of SDependencyMapping objects
     * @throws SDependencyException
     */
    List<SDependencyMapping> getDependencyMappings(QueryOptions queryOptions) throws SDependencyException;

    /**
     * Refresh classloader after a dependency update
     * difference with #refreshClassLoader is that this one is done on all nodes
     *
     * @param type
     * @param id
     * @throws SDependencyException
     */
    void refreshClassLoaderAfterUpdate(ScopeType type, long id) throws SDependencyException;

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
    List<Long> getDependencyIds(long artifactId, ScopeType artifactType, int startIndex, int maxResult) throws SDependencyException;

    /**
     * @param id
     * @param type
     * @throws SDependencyException
     */
    void deleteDependencies(long id, ScopeType type) throws SDependencyException;

    /**
     * refresh classloader on this node only
     * 
     * @param type
     * @param id
     * @throws SDependencyException
     */
    void refreshClassLoader(ScopeType type, long id) throws SDependencyException;

    SDependency createMappedDependency(String name, byte[] jarContent, String fileName, long artifactId, ScopeType scopeType) throws SDependencyException;

    SDependency updateDependencyOfArtifact(String name, byte[] jarContent, String fileName, long artifactId, ScopeType scopeType) throws SDependencyException;

    SDependency getDependencyOfArtifact(long artifactId, ScopeType artifactType, String fileName) throws SBonitaReadException;

    Optional<Long> getIdOfDependencyOfArtifact(Long artifactId, ScopeType artifactType, String fileName) throws SBonitaReadException;
}
